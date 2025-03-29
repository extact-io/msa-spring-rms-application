package io.extact.msa.spring.rms.infrastructure.persistence.remote.item;

import java.util.List;
import java.util.Optional;

import io.extact.msa.spring.platform.fw.domain.model.Identity;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;

public class ItemRemoteRepository implements ItemRepository {

    @Override
    public Optional<Item> find(Identity id) {
        return Optional.empty();
    }

    @Override
    public List<Item> findAll() {
        return null;
    }

    @Override
    public void add(Item model) {
    }

    @Override
    public void update(Item model) {
    }

    @Override
    public void delete(Item model) {
    }

    @Override
    public Optional<Item> findDuplicationData(Item checkModel) {
        return Optional.empty();
    }

    @Override
    public int nextIdentity() {
        return 0;
    }
}
