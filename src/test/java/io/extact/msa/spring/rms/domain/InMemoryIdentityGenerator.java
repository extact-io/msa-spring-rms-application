package io.extact.msa.spring.rms.domain;

import io.extact.msa.spring.platform.fw.domain.service.IdentityGenerator;

public class InMemoryIdentityGenerator implements IdentityGenerator {
    private int value;
    @Override
    public int nextIdentity() {
        return ++value;
    }
}