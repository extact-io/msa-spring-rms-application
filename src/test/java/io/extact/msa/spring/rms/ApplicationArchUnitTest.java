package io.extact.msa.spring.rms;

import static com.tngtech.archunit.base.DescribedPredicate.*;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.ArchCondition.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.*;
import static io.extact.msa.spring.test.archunit.ArchUnitUtils.*;

import java.util.Optional;

import org.springframework.web.service.annotation.HttpExchange;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClass.Predicates;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.Slice;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import io.extact.msa.spring.platform.fw.application.ApplicationService;
import io.extact.msa.spring.platform.fw.domain.model.EntityModelView;
import io.extact.msa.spring.platform.fw.domain.model.Identity;
import io.extact.msa.spring.platform.fw.domain.model.ModelCreator;
import io.extact.msa.spring.platform.fw.domain.model.ValueModel;
import io.extact.msa.spring.platform.fw.domain.repository.GenericRepository;
import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.PhysicalEntity;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.JpaRepositoryDelegator;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RmsRestController;
import io.extact.msa.spring.platform.fw.interfaces.webapi.StartupLogRunner;
import io.extact.msa.spring.rms.interfaces.console.MainScreenRunner;

@AnalyzeClasses(packages = "io.extact.msa.spring.rms", importOptions = ImportOption.DoNotIncludeTests.class)
class ApplicationArchUnitTest {


    // ---------------------------------------------------------------------
    // アーキテクチャールールの検証
    // ---------------------------------------------------------------------

    /**
     * オニオンアーキテクチャが遵守されているかの検証。
     * @see https://www.archunit.org/userguide/html/000_Index.html#_onion_architecture
     */
    @ArchTest
    static final ArchRule architecture_respect_onion = onionArchitecture()
            .domainModels(
                    "..domain..model..",
                    "..domain..constraint..")
            .domainServices(
                    // domainパッケージ直下のクラス
                    "..domain.item",
                    "..domain.reservation",
                    "..domain.user")
            .applicationServices(
                    "..application..")
            // それぞれのapapterは独立し、相互に依存関係がないこともチェックされる
            .adapter("interface-console", "..interfaces.console..")
            .adapter("interface-webapi", "..interfaces.webapi..")
            .adapter("persistence-file", "..infrastructure.persistence.file..")
            .adapter("persistence-jpa", "..infrastructure.persistence.jpa..")
            .adapter("persistence-remote", "..infrastructure.persistence.remote..")
            .adapter("external", "..infrastructure.external..")
            .adapter("framework", "..infrastructure.framework..")
            // Cofigurationクラスからの依存は無視する
            .ensureAllClassesAreContainedInArchitectureIgnoring(configurationClasses())
            .ignoreDependency(configurationClasses(), alwaysTrue());

    /**
     * オニオンアーキテクチャに対する独自の追加制約の検証。
     * interfaceレイヤからdomainrレイヤへの依存関係が下記のとおりになっているかを検証する。
     * <p>
     * オニオンアーキテクチャはdomainレイヤはどのレイヤからも依存を許可する開放レイヤースタイルを採っているが、
     * rmsアプリケーションではinterfacesからdomainに対しては一部のモジュールのアクセスを許容するが、それ以外は
     * interfaces→application→domianの閉鎖レイヤースタイルを採っている
     */
    @ArchTest
    static final ArchRule architecture_respect_addon_rule_for_onion = noClasses()
            // 対象条件(that)に対するパッチングは@AnalyzeClassesのpackagesに対して行われる
            .that()
            .resideInAPackage("..interfaces..")
            // 制約条件(should)はfwなども含めclassパス上のすべてのクラスに対してマッチングされる
            .should().dependOnClassesThat(
                    resideInAnyPackage("io.extact.msa.spring.rms.domain..")
                            // ValueModelインターフェースの実装クラス
                            .and(not(implement(ValueModel.class)))
                            // EntityModelViewのサブインターフェース
                            .and(not(subInterface(EntityModelView.class)))
                            // fw.domin.constraintとrms.domain.*.constraintの両方が含まれる
                            .and(not(resideInAnyPackage("..domain..constraint.."))) //
            );

    /**
     * EntityModelのインスタンス化を行えるCreatableインターフェースの制限に対する検証。
     * modelパッケージにあるXxxxCreatableインターフェースを実装して良いのは、TableEntity
     * もしくはModelArrayMapperインターフェースの実装クラス、またはModelCreatorインターフェース
     * の実装クラスの匿名クラスのみ。
     */
    @ArchTest
    static final ArchRule architecture_respect_addon_rule_for_creator = classes()
            .that()
            // modelパッケージにあるXxxxCreatableインターフェースの実装クラスは・・の条件
            .implement(
                    resideInAnyPackage("..domain..model..")
                            .and(INTERFACES)
                            .and(simpleNameEndingWith("Creatable")))
            .should().beAssignableTo(type(PhysicalEntity.class).or(type(ModelArrayMapper.class)))
            .orShould(from(anonymousClassInImplementationClassOf(ModelCreator.class)));

    // ---------------------------------------------------------------------
    // モジュール単位の独立性の検証(Slice Isolation)
    // ---------------------------------------------------------------------

    /**
     * webapi配下のパッケージ(admin/member/universal)は独立し相互に依存していないこと。
     * <p>
     * ・adminパッケージがmemberパッケージを利用しているといったことがないこと
     */
    @ArchTest
    static final ArchRule isolate_webapi_not_depend_on_each_other = SlicesRuleDefinition.slices()
            .matching("..rms.interfaces.webapi.(*)..")
            .should().notDependOnEachOther();

    /**
     * application配下のパッケージはsupportパッケージを除きそれぞれが独立し相互に依存していないこと。
     * <p>
     * ・adminパッケージがsupportパッケージに依存するのはよいがmemberパッケージを利用しているといったことがないこと
     */
    @ArchTest
    static final ArchRule isolate_application_not_depend_on_each_other = SlicesRuleDefinition.slices()
            .matching("..rms.application.(*)..").namingSlices("Application $1")
            .that(not(containDescription("Application support")))
            .should().notDependOnEachOther();

    /**
     * application配下のパッケージが循環参照していないこと。
     */
    @ArchTest
    static final ArchRule isolate_application_be_free_of_cycles = SlicesRuleDefinition
            .slices()
            .matching("..rms.application.(*)..")
            .should().beFreeOfCycles();

    /**
     * domainパッケージ内の集約(item/reservation/user)間で循環参照が発生していないかの検証
     */
    @ArchTest
    static final ArchRule isolate_domain_be_free_of_cycles = SlicesRuleDefinition.slices()
            .matching("..rms.domain.(*)..")
            .should().beFreeOfCycles();

    /**
     * persistence.file配下のパッケージ(item/reservation/user)は独立し相互に依存していないこと。
     * <p>
     * ・reservationパッケージがitemパッケージを利用しているといったことがないこと
     */
    @ArchTest
    static final ArchRule isolate_persistence_file_not_depend_on_each_other = SlicesRuleDefinition.slices()
            .matching("..rms.infrastructure.persistence.file.(*)..")
            .should().notDependOnEachOther();

    /**
     * persistence.jpa配下のパッケージ(item/reservation/user)は独立し相互に依存していないこと。
     * <p>
     * ・reservationパッケージがitemパッケージを利用しているといったことがないこと
     */
    @ArchTest
    static final ArchRule isolate_each_persistence_jpa_not_depend_on_each_other = SlicesRuleDefinition.slices()
            .matching("..rms.infrastructure.persistence.jpa.(*)..")
            .should().notDependOnEachOther();

    /**
     * persistence.remote配下のパッケージ(item/reservation/user)は独立し相互に依存していないこと。
     * <p>
     * ・reservationパッケージがitemパッケージを利用しているといったことがないこと
     */
    @ArchTest
    static final ArchRule isolate_each_persistence_remote_not_depend_on_each_other = SlicesRuleDefinition.slices()
            .matching("..rms.infrastructure.persistence.remote.(*)..")
            .should().notDependOnEachOther();

    // ---------------------------------------------------------------------
    // レイヤごとの依存可能モジュールの検証
    // ---------------------------------------------------------------------

    /**
     * webapiパッケージから依存してOKなモジュールの検証
     */
    @ArchTest
    static final ArchRule dependency_webapi = classes()
            .that()
            .resideInAPackage("..interfaces.webapi..")
            .and(not(configurationClasses()))
            .and(not(type(StartupLogRunner.class)))
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                    "java..",
                    "jakarta.validation..",
                    "org.springframework.web..",    // Spring MVCには依存してOK
                    "org.springframework.http..",   // Spring MVCには依存してOK
                    "org.slf4j..",
                    "lombok..",
                    "..core.generic..",
                    "..core.env..",
                    "..core.auth..",
                    "..core.jwt..",
                    "..fw.domain..",
                    "..fw.exception..",
                    "..fw.application..",
                    "..fw.interfaces",
                    "..fw.interfaces.webapi..",
                    "..fw.feature.exception..",
                    "..rms.domain..",
                    "..rms.application..",
                    "..rms.interfaces.webapi.." //
            );

    /**
     * consoleパッケージから依存してOKなモジュールの検証
     */
    @ArchTest
    static final ArchRule dependency_console = classes()
            .that()
            .resideInAPackage("..interfaces.console..")
            .and(not(configurationClasses()))
            .and(not(type(MainScreenRunner.class)))
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                    "java..",
                    "org.beryx.textio..", // コンソールFWには依存してOK
                    "lombok..",
                    "..core.env..",
                    "..fw.exception",
                    "..fw.interfaces",
                    "..rms.domain..",
                    "..rms.application..",
                    "..rms.interfaces.console.." //
            );

    /**
     * applicationパッケージから依存してOKなモジュールの検証
     */
    @ArchTest
    static final ArchRule dependency_application = classes()
            .that()
            .resideInAPackage("..application..")
            .and(not(configurationClasses()))
            .should().onlyDependOnClassesThat(
                    resideInAnyPackage(
                            "java..",
                            "lombok..",
                            "..core.generic..",
                            "..core.auth..",
                            "..core.async..",
                            "..fw.domain..",
                            "..fw.application..",
                            "..fw.exception..",
                            "..rms.domain..",
                            "..rms.application..")
                                    .or(type(org.springframework.context.event.EventListener.class)) //
                                    // @ApplicationServiceにデフォルト属性(propagation = Propagation.requirede)が設定されるため
                                    .or(type(org.springframework.transaction.annotation.Propagation.class)) //
            );

    /**
     * domainパッケージから依存してOKなモジュールの検証
     */
    @ArchTest
    static final ArchRule dependency_domain = classes()
            .that()
            .resideInAPackage("..domain..")
            .and(not(configurationClasses()))
            .should().onlyDependOnClassesThat(
                    resideInAnyPackage(
                            "java..",
                            "jakarta.validation..",
                            "lombok..",
                            "..core.generic..",
                            "..fw.domain..",
                            "..fw.exception..",
                            "..rms.domain..")
                                    .or(type(org.apache.commons.lang3.Range.class)) //
            );

    /**
     * persistence.fileパッケージから依存してOKなモジュールの検証
     */
    @ArchTest
    static final ArchRule dependency_persistence_file = classes()
            .that()
            .resideInAPackage("..infrastructure.persistence.file..")
            .and(not(configurationClasses()))
            .should().onlyDependOnClassesThat(
                    resideInAnyPackage(
                            "java..",
                            "lombok..",
                            "..fw.exception..",
                            "..fw.domain.model..",
                            "..fw.infrastructure.persistence",
                            "..fw.infrastructure.persistence.file..",
                            "..rms.application..",
                            "..rms.domain..model..",
                            "..rms.infrastructure.persistence",
                            "..rms.infrastructure.persistence.file..")
                                    .or(type(io.extact.msa.spring.rms.domain.item.ItemRepository.class))
                                    .or(type(io.extact.msa.spring.rms.domain.reservation.ReservationRepository.class)) //
                                    .or(type(io.extact.msa.spring.rms.domain.user.UserRepository.class)) //
            );

    /**
     * persistence.jpaパッケージから依存してOKなモジュールの検証
     */
    @ArchTest
    static final ArchRule dependency_persistence_jpa = classes()
            .that()
            .resideInAPackage("..infrastructure.persistence.jpa..")
            .and(not(configurationClasses()))
            .should()
            .onlyDependOnClassesThat(resideInAnyPackage(
                    "java..",
                    "jakarta.persistence..",
                    "org.springframework.data..", // Spring dataなのでOK
                    "lombok..",
                    "..fw.exception..",
                    "..fw.domain.model..",
                    "..fw.infrastructure.persistence",
                    "..fw.infrastructure.persistence.jpa..",
                    "..rms.domain..model..",
                    "..rms.application..",
                    "..rms.infrastructure.persistence.jpa..")
                            .or(type(io.extact.msa.spring.rms.domain.item.ItemRepository.class))
                            .or(type(io.extact.msa.spring.rms.domain.reservation.ReservationRepository.class))
                            .or(type(io.extact.msa.spring.rms.domain.user.UserRepository.class)) //
            );

    /**
     * persistence.remoteパッケージから依存してOKなモジュールの検証
     */
    @ArchTest
    static final ArchRule dependency_persistence_remote = classes()
            .that()
            .resideInAPackage("..infrastructure.persistence.remote..")
            .and(not(configurationClasses()))
            .should()
            .onlyDependOnClassesThat(resideInAnyPackage(
                    "java..",
                    "org.springframework.web..", // Spring WebなのでOK
                    "com.fasterxml.jackson.*",
                    "lombok..",
                    "..fw.exception..",
                    "..fw.domain.model..",
                    "..fw.infrastructure.persistence",
                    "..fw.infrastructure.persistence.remote..",
                    "..rms.domain..model..",
                    "..rms.application..",
                    "..rms.infrastructure.persistence.remote..")
                            .or(type(io.extact.msa.spring.rms.domain.item.ItemRepository.class))
                            .or(type(io.extact.msa.spring.rms.domain.reservation.ReservationRepository.class))
                            .or(type(io.extact.msa.spring.rms.domain.user.UserRepository.class)) //
            );

    // ---------------------------------------------------------------------
    // ネーミングの検証
    // ---------------------------------------------------------------------

    /**
     * webapiパッケージ配下でRmsRestControllerアノテーションが付いているクラスの
     * サフィックスは"Controller"となっていること。
     */
    @ArchTest
    static final ArchRule naming_controller_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..interfaces.webapi..")
            .and().areAnnotatedWith(RmsRestController.class)
            .should().haveSimpleNameEndingWith("Controller");

    /**
     * {@link #naming_controller_should_be_suffixed}の逆引きの検証
     */
    @ArchTest
    static final ArchRule naming_controller_should_be_suffixed_reverse = classes()
            .that()
            .resideInAPackage("..interfaces.webapi..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith(RmsRestController.class);

    /**
     * applicationパッケージ配下でApplicationServiceアノテーションが付いているクラスの
     * サフィックスは"Service"となっていること。
     */
    @ArchTest
    static final ArchRule naming_service_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..application..")
            .and().areAnnotatedWith(ApplicationService.class)
            .should().haveSimpleNameEndingWith("Service");

    /**
     * {@link #naming_service_should_be_suffixed}の逆引きの検証
     */
    @ArchTest
    static final ArchRule naming_service_should_be_suffixed_reverse = classes()
            .that()
            .resideInAPackage("..application..")
            .and().haveSimpleNameEndingWith("Service")
            .and(not(simpleNameEndingWith("QueryService")))
            .should().beAnnotatedWith(ApplicationService.class);

    /**
     * domainパッケージ配下のModelCreatorの実装クラスのサフィックスは"Creator"となっていること。
     */
    @ArchTest
    static final ArchRule naming_model_creator_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..domain..")
            .and().implement(ModelCreator.class)
            .should().haveSimpleNameEndingWith("Creator");

    /**
     * {@link #naming_model_creator_should_be_suffixed}の逆引きの検証
     */
    @ArchTest
    static final ArchRule naming_model_creator_should_be_suffixed_reverse = classes()
            .that()
            .resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Creator")
            .should().beAssignableTo(ModelCreator.class);

    /**
     * domainパッケージ配下のGenericRepositoryのサブインターフェースのサフィックスは"Repository"と
     * なっていること。
     */
    @ArchTest
    static final ArchRule naming_reqpository_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..domain..")
            .and(subInterface(GenericRepository.class))
            .should().haveSimpleNameEndingWith("Repository");

    /**
     * {@link #naming_reqpository_should_be_suffixed}の逆引きの検証
     */
    @ArchTest
    static final ArchRule naming_reqpository_should_be_suffixed_reverse = classes()
            .that()
            .resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Repository")
            .should().beAssignableTo(GenericRepository.class);

    /**
     * domainパッケージ配下のDuplicateCheckerの実装クラスのサフィックスは"DuplicateChecker"と
     * なっていること。
     */
    @ArchTest
    static final ArchRule naming_duplicate_checker_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..domain..")
            .and().implement(DuplicateChecker.class)
            .should().haveSimpleNameEndingWith("DuplicateChecker");

    /**
     * {@link #naming_duplicate_checker_should_be_suffixed}の逆引きの検証
     */
    @ArchTest
    static final ArchRule naming_duplicate_checker_should_be_suffixed_reverse = classes()
            .that()
            .resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("DuplicateChecker")
            .should().beAssignableTo(DuplicateChecker.class);

    /**
     * domainパッケージ配下のEntityModelViewのサブインターフェースのサフィックスは"ModelView"と
     * なっていること。
     */
    @ArchTest
    static final ArchRule naming_model_view_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..domain..")
            .and(subInterface(EntityModelView.class))
            .should().haveSimpleNameEndingWith("ModelView");

    /**
     * {@link #naming_model_view_should_be_suffixed}の逆引きの検証
     */
    @ArchTest
    static final ArchRule naming_model_view_should_be_suffixed_reverse = classes()
            .that()
            .resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("ModelView")
            .should().beAssignableTo(EntityModelView.class);

    /**
     * domainパッケージ配下のIdentityの実装クラスのサフィックスは"Id"となっていること。
     */
    @ArchTest
    static final ArchRule naming_idenity_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..domain..")
            .and().implement(Identity.class)
            .should().haveSimpleNameEndingWith("Id");

    /**
     * {@link #naming_idenity_should_be_suffixed}の逆引きの検証
     */
    @ArchTest
    static final ArchRule naming_idenity_should_be_suffixed_reverse = classes()
            .that()
            .resideInAPackage("..domain..model..")
            .and().haveSimpleNameEndingWith("Id")
            .should().beAssignableTo(Identity.class);

    /**
     * persistence.fileパッケージ配下のModelArrayMapperの実装クラスのサフィックスは"ArrayMapper"となっていること。
     */
    @ArchTest
    static final ArchRule naming_model_mapper_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..persistence.file..")
            .and().implement(ModelArrayMapper.class)
            .should().haveSimpleNameEndingWith("ArrayMapper");

    /**
     * persistence.fileパッケージ配下のGenericRepositoryの実装クラスのサフィックスは"FileRepository"となっていること。
     */
    @ArchTest
    static final ArchRule naming_file_repository_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..persistence.file..")
            .and().implement(GenericRepository.class)
            .should().haveSimpleNameEndingWith("FileRepository");

    /**
     * persistence.jpaパッケージ配下のTableEntityの実装クラスのサフィックスは"Entity"となっていること。
     */
    @ArchTest
    static final ArchRule naming_table_entity_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..persistence.jpa..")
            .and().implement(PhysicalEntity.class)
            .should().haveSimpleNameEndingWith("Entity");

    /**
     * persistence.jpaパッケージ配下のGenericRepositoryの実装クラスのサフィックスは"JpaRepository"となっていること。
     */
    @ArchTest
    static final ArchRule naming_jpa_repository_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..persistence.jpa..")
            .and().implement(GenericRepository.class)
            .should().haveSimpleNameEndingWith("JpaRepository");

    /**
     * persistence.jpaパッケージ配下のJpaRepositoryDelegatorのサブインターフェースのサフィックスは
     * "JpaRepositoryDelegator"となっていること。
     */
    @ArchTest
    static final ArchRule naming_jpa_repository_delegator_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..persistence.jpa..")
            .and(subInterface(JpaRepositoryDelegator.class))
            .should().haveSimpleNameEndingWith("JpaRepositoryDelegator");

    /**
     * persistence.remoteパッケージ配下のPhysicalEntityの実装クラスのプレフィックスは"Remote"となっていること。
     */
    @ArchTest
    static final ArchRule naming_remote_entity_should_be_prefixed = classes()
            .that()
            .resideInAPackage("..persistence.remote..")
            .and().implement(PhysicalEntity.class)
            .should().haveSimpleNameStartingWith("Remote");

    /**
     * persistence.remoteパッケージ配下のGenericRepositoryの実装クラスのFQCNの
     * サフィックスが".Remoteで始まり、Repositoryで終わる"となっていること。
     */
    @ArchTest
    static final ArchRule naming_remote_repository_should_be_matched = classes()
            .that()
            .resideInAPackage("..persistence.remote..")
            .and().implement(GenericRepository.class)
            .should().haveNameMatching("^.*\\.Remote.*Repository$");

    /**
     * persistence.remoteパッケージ配下のHttpInterfaceのインターフェースのサフィックスは
     * "ClientApi"となっていること。
     */
    @ArchTest
    static final ArchRule naming_remote_repository_httpinterface_should_be_suffixed = classes()
            .that()
            .resideInAPackage("..persistence.remote..")
            .and().areAnnotatedWith(HttpExchange.class)
            .should().haveSimpleNameEndingWith("ClientApi");

    // --------------------------------------------------------------- private methods

    private static DescribedPredicate<JavaClass> subInterface(Class<?> rootInterface) {
        JavaClass rootJavaClass = new ClassFileImporter().importClass(rootInterface);
        if (!rootJavaClass.isInterface()) {
            return alwaysFalse();
        }
        return new DescribedPredicate<JavaClass>("is a sub-interface of " + rootInterface.getName()) {
            @Override
            public boolean test(JavaClass input) {
                if (!input.isInterface()) {
                    return false;
                }
                DescribedPredicate<JavaClass> subInterfacePredicat = Predicates.equivalentTo(rootInterface);
                return input.getAllRawInterfaces().stream().anyMatch(subInterfacePredicat);
            }
        };
    }

    private static DescribedPredicate<JavaClass> anonymousClassInImplementationClassOf(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return alwaysFalse();
        }
        return new DescribedPredicate<JavaClass>("is inner class of the implementation class of " + clazz.getName()) {
            @Override
            public boolean test(JavaClass input) {
                if (!input.isAnonymousClass()) {
                    return false;
                }
                if (input.isInterface() || input.isEnum()) {
                    return false;
                }
                if (!isTargetImplement(input.getEnclosingClass())) {
                    return false;
                }
                return true;
            }

            private boolean isTargetImplement(Optional<JavaClass> input) {
                if (input.isEmpty()) {
                    return false;
                }
                DescribedPredicate<JavaClass> testInterfacePredicat = Predicates.equivalentTo(clazz);
                return input.get().getAllRawInterfaces().stream().anyMatch(testInterfacePredicat);
            }
        };
    }

    private static DescribedPredicate<Slice> containDescription(String descriptionPart) {
        return new DescribedPredicate<Slice>("contain description '%s'", descriptionPart) {
            @Override
            public boolean test(Slice input) {
                return input.getDescription().contains(descriptionPart);
            }
        };
    }
}
