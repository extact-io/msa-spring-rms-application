package io.extact.msa.spring.rms.domain;

import io.extact.msa.spring.platform.fw.domain.model.Identity;
import io.extact.msa.spring.platform.fw.domain.repository.IdProvider;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.IdCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InMemoryIdentityGenerator<I extends Identity> implements IdProvider<I> {

    private final IdCreator<I> idCreator;
    private int value;

    @Override
    public I nextIdentity() {
        value++;
        return idCreator.create(value);
    }
}