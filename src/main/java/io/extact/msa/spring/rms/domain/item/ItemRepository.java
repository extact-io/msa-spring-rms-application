package io.extact.msa.spring.rms.domain.item;

import io.extact.msa.spring.platform.fw.domain.repository.DuplicationDataFinder;
import io.extact.msa.spring.platform.fw.domain.repository.GenericRepository;
import io.extact.msa.spring.platform.fw.domain.repository.IdProvider;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemId;

public interface ItemRepository
        extends GenericRepository<Item>, DuplicationDataFinder<Item>, IdProvider<ItemId> {
}
