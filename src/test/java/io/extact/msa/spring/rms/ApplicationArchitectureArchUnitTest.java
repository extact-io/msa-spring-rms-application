package io.extact.msa.spring.rms;

import static com.tngtech.archunit.base.DescribedPredicate.*;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Configuration;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClass.Predicates;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import io.extact.msa.spring.platform.fw.application.ApplicationService;
import io.extact.msa.spring.platform.fw.domain.model.EntityModelView;
import io.extact.msa.spring.platform.fw.domain.model.ValueModel;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RmsRestController;
import io.extact.msa.spring.rms.interfaces.console.MainScreenRunner;
import io.extact.msa.spring.rms.interfaces.webapi.StartupLogRunner;

@AnalyzeClasses(packages = "io.extact.msa.spring.rms", importOptions = ImportOption.DoNotIncludeTests.class)
class ApplicationArchitectureArchUnitTest {

    // ---------------------------------------------------------------------
    // オニオンアーキテクチャの検証
    // ---------------------------------------------------------------------

    /**
     * オニオンアーキテクチャが遵守されているかの検証。
     * @see https://www.archunit.org/userguide/html/000_Index.html#_onion_architecture
     */
    @ArchTest
    static final ArchRule onion_architecture_is_respected = Architectures.onionArchitecture()
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

    // ---------------------------------------------------------------------
    // レイヤーごとの依存関係の検証
    // ---------------------------------------------------------------------

    /**
     * interfaceレイヤからdomainrレイヤの依存関係の検証。
     * オニオンアーキテクチャはdomainレイヤはどのレイヤからも依存を許可する開放レイヤースタイルを採っているが、
     * rmsアプリケーションではinterfacesからdomainに対しては一部のモジュールのアクセスを許容するが、それ以外は
     * interfaces→application→domianの閉鎖レイヤースタイルを採っている
     */
    @ArchTest
    static final ArchRule dependency_intaface_to_domain = noClasses()
            // 対象条件(that)に対する省略は@AnalyzeClassesのpackagesに対しての省略となる
            .that()
            .resideInAPackage("..interfaces..")
            // 制約条件(should)はfwなども含めclassパス上のすべてのクラスに対してマッチングされる
            .should()
            .dependOnClassesThat(
                    resideInAnyPackage("io.extact.msa.spring.rms.domain..")
                            // ValueModelインターフェースの実装クラス
                            .and(not(implement(ValueModel.class)))
                            // EntityModelViewのサブインターフェース
                            .and(not(subInterface(EntityModelView.class)))
                            // fw.domin.constraintとrms.domain.*.constraintの両方が含まれる
                            .and(not(resideInAnyPackage("..domain..constraint.."))) //
            );

    /**
     * webapiパッケージから依存してOKなモジュールの検証
     */
    @ArchTest
    static final ArchRule dependency_webapi = classes()
            .that()
            .resideInAPackage("..interfaces.webapi..")
            .and(not(configurationClasses()))
            .and(not(type(StartupLogRunner.class)))
            .should()
            .onlyDependOnClassesThat().resideInAnyPackage(
                    "java..",
                    "jakarta.validation..",
                    "org.springframework.web..", // Spring MVCには依存してOK
                    "lombok..",
                    "..core.generic..",
                    "..core.env..",
                    "..core.auth..",
                    "..core.jwt..",
                    "..fw.domain..",
                    "..fw.application..",
                    "..fw.interfaces",
                    "..fw.interfaces.webapi..",
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
            .should()
            .onlyDependOnClassesThat().resideInAnyPackage(
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
            .should()
            .onlyDependOnClassesThat(resideInAnyPackage(
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
                            .or(type(org.springframework.transaction.annotation.Propagation.class)) //
            );

    /**
     * domainパッケージ内の集約(item/reservation/user)間で循環参照が発生していないかの検証
     */
    @ArchTest
    static final ArchRule free_of_cycles_domain_modules = SlicesRuleDefinition
            .slices()
            .matching("..domain.(*)..")
            .should()
            .beFreeOfCycles();

    /**
     * domainパッケージから依存してOKなモジュールの検証
     */
    @ArchTest
    static final ArchRule dependency_domain = classes()
            .that()
            .resideInAPackage("..domain..")
            .and(not(configurationClasses()))
            .should()
            .onlyDependOnClassesThat(resideInAnyPackage(
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
            .should()
            .onlyDependOnClassesThat(resideInAnyPackage(
                    "java..",
                    "lombok..",
                    "..fw.exception..",
                    "..fw.domain.model..",
                    "..fw.infrastructure.persistence.file..",
                    "..rms.domain..model..",
                    "..rms.infrastructure.persistence.file..")
                            .or(type(io.extact.msa.spring.rms.domain.item.ItemRepository.class))
                            .or(type(io.extact.msa.spring.rms.domain.reservation.ReservationRepository.class))
                            .or(type(io.extact.msa.spring.rms.domain.user.UserRepository.class)) //
            );

    /**
     * persistence.persistenceパッケージから依存してOKなモジュールの検証
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
                    "lombok..",
                    "..fw.exception..",
                    "..fw.domain.model..",
                    "..fw.infrastructure.persistence.jpa..",
                    "..rms.domain..model..",
                    "..rms.infrastructure.persistence.jpa..")
                            .or(type(io.extact.msa.spring.rms.domain.item.ItemRepository.class))
                            .or(type(io.extact.msa.spring.rms.domain.reservation.ReservationRepository.class))
                            .or(type(io.extact.msa.spring.rms.domain.user.UserRepository.class)) //
            );

    // ---------------------------------------------------------------------
    // ネーミングの検証
    // ---------------------------------------------------------------------

    @ArchTest
    static final ArchRule naming_controller_should_be_suffixed_forward = classes()
            .that()
            .resideInAPackage("..interfaces.webapi..")
            .and().areAnnotatedWith(RmsRestController.class)
            .should()
            .haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule naming_controller_should_be_suffixed_reverse = noClasses()
            .that()
            .resideInAPackage("..interfaces.webapi..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().notBeAnnotatedWith(RmsRestController.class);

    @ArchTest
    static final ArchRule naming_service_should_be_suffixed_forward = classes()
            .that()
            .resideInAPackage("..application..")
            .and().areAnnotatedWith(ApplicationService.class)
            .should()
            .haveSimpleNameEndingWith("Service");

    @ArchTest
    static final ArchRule naming_controller_should_be_suffixed_reserve = noClasses()
            .that()
            .resideInAPackage("..application..")
            .and().haveSimpleNameEndingWith("Service")
            .should().notBeAnnotatedWith(ApplicationService.class);

    // --------------------------------------------------------------- private methods

    private static DescribedPredicate<JavaClass> configurationClasses() {
        return belongTo(annotatedWith(Configuration.class)
                .or(annotatedWith(SpringBootConfiguration.class)));
    }

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
}
