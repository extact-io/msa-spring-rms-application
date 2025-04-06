package io.extact.msa.spring.rms.infrastructure.persistence.remote.item;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.ModelEntityMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.remote.AbstractRemoteRepository;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;

public class RemoteItemRepository extends AbstractRemoteRepository<Item, RemoteItem> implements ItemRepository {

    private final RemoteItemClientApi clientApi;
    private final ModelEntityMapper<Item, RemoteItem> entityMapper;

    public RemoteItemRepository(
            RemoteItemClientApi clientApi, 
            ModelEntityMapper<Item, RemoteItem> entityMapper) {
        
        super(clientApi, entityMapper);
        this.clientApi = clientApi;
        this.entityMapper = entityMapper;
    }

    public Optional<Item> findDuplicationData(Item checkModel) {
        RemoteItem found = clientApi.findDuplicationData(checkModel.getSerialNo());
        return Optional
                .ofNullable(found)
                .map(entityMapper::toModel);
    }
}
