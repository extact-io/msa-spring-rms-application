package io.extact.msa.spring.rms.infrastructure.item.file;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.AbstractFileRepository;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.FileOperator;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;

public class ItemFileRepository extends AbstractFileRepository<Item> implements ItemRepository {

    static final String FILE_ENTITY = "rental-item";

    public ItemFileRepository(FileOperator fileReadWriter, ModelArrayMapper<Item> mapper) {
        super(fileReadWriter, mapper);
    }

    @Override
    public String getEntityName() {
        return FILE_ENTITY;
    }

    @Override
    public Optional<Item> findDuplicationData(Item checkItem) {
        return this.findAll().stream()
                .filter(item -> item.getSerialNo().equals(checkItem.getSerialNo()))
                .findFirst();
    }
}
