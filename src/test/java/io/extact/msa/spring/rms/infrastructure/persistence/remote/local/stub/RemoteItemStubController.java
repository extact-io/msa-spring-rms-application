package io.extact.msa.spring.rms.infrastructure.persistence.remote.local.stub;

import static io.extact.msa.spring.rms.PersistedTestData.*;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.extact.msa.spring.platform.fw.interfaces.webapi.RmsRestController;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItem;

@RmsRestController("/remote/items")
public class RemoteItemStubController extends RemoteStubController<Item, RemoteItem> {

    private Map<Integer, RemoteItem> itemsMap;

    @PostConstruct
    void init() {
        itemsMap = new LinkedHashMap<>();
        itemsMap.put(item1.getId().id(), RemoteItem.from(item1));
        itemsMap.put(item2.getId().id(), RemoteItem.from(item2));
        itemsMap.put(item3.getId().id(), RemoteItem.from(item3));
        itemsMap.put(item4.getId().id(), RemoteItem.from(item4));
    }

    @GetMapping("/unique")
    public RemoteItem findBySerialNo(@RequestParam("serial-no") String serialNo) {
        return itemsMap.values().stream()
                .filter(item -> item.serialNo().equals(serialNo))
                .findAny()
                .orElse(null);
    }

    @Override
    public Map<Integer, RemoteItem> entityMap() {
        return itemsMap;
    }
}
