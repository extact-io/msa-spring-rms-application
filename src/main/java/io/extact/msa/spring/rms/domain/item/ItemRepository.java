package io.extact.msa.spring.rms.domain.item;

import io.extact.msa.spring.platform.fw.domain.repository.GenericRepository;
import io.extact.msa.spring.platform.fw.domain.service.DuplicationDataFinder;
import io.extact.msa.spring.platform.fw.domain.service.IdentityGenerator;
import io.extact.msa.spring.rms.domain.item.model.Item;

public interface ItemRepository extends GenericRepository<Item>, DuplicationDataFinder<Item>, IdentityGenerator {
}
