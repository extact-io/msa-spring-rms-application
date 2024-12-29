package io.extact.msa.spring.rms.infrastructure.item.jpa;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.infrastructure.ModelEntityMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.AbstractJpaRepository;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;

public class ItemJpaRepository extends AbstractJpaRepository<Item, ItemEntity> implements ItemRepository {

    private final ItemJpaRepositoryDelegator springJpa;
    private final ModelEntityMapper<Item, ItemEntity> entityMapper;

    public ItemJpaRepository(ItemJpaRepositoryDelegator jpa,
            ModelEntityMapper<Item, ItemEntity> entityMapper) {
        super(jpa, entityMapper);
        this.springJpa = jpa;
        this.entityMapper = entityMapper;
    }

    @Override
    public Optional<Item> findDuplicationData(Item checkItem) {
        return springJpa.findBySerialNo(checkItem.getSerialNo())
                .map(entityMapper::toModel);
    }
}
