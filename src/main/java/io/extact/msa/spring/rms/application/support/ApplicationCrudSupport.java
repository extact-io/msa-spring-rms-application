package io.extact.msa.spring.rms.application.support;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.extact.msa.spring.platform.fw.domain.model.DomainModel;
import io.extact.msa.spring.platform.fw.domain.model.Identity;
import io.extact.msa.spring.platform.fw.domain.repository.GenericRepository;
import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import lombok.RequiredArgsConstructor;

/**
 * ApplicaitonServiceのCRUD処理をサポートするクラス。
 * ApplicaitonServiceがこのクラスを利用するかは任意としている。
 *
 * @param <M> Modelクラス
 */
@RequiredArgsConstructor
public class ApplicationCrudSupport<M extends DomainModel> {

    private final DuplicateChecker<M> duplicateChecker;
    private final GenericRepository<M> repository;

    public Optional<M> getById(Identity id) {
        return repository.find(id);
    }

    public List<M> getAll() {
        return repository.findAll();
    }

    public M add(Supplier<M> creator) {
        M model = creator.get();
        duplicateChecker.check(model);
        repository.add(model);
        return model;
    }

    public M update(Identity id, Consumer<M> editor) {
        M model = repository.find(id)
                .orElseThrow(() -> new BusinessFlowException("target does not exist for id", CauseType.NOT_FOUND));
        editor.accept(model);
        duplicateChecker.check(model);
        repository.update(model);
        return model;
    }

    public void delete(Identity id) {
        M target = repository.find(id)
                .orElseThrow(() -> new BusinessFlowException("target does not exist for id", CauseType.NOT_FOUND));
        repository.delete(target);
    }
}
