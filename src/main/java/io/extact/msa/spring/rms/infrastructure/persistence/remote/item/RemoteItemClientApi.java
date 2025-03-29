package io.extact.msa.spring.rms.infrastructure.persistence.remote.item;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange("/items")
public interface RemoteItemClientApi {

    @GetExchange("/{id}")
    RemoteItemResponse get(@PathVariable("id") Integer id);

    @GetExchange
    List<RemoteItemResponse> getAll();

    @PostExchange
    RemoteItemResponse add(@RequestBody AddRemoteItemRequest req);

    @PutExchange
    RemoteItemResponse update(@RequestBody UpdateRemoteItemRequest req);

    @DeleteExchange("/{id}")
    RemoteItemResponse delete(@PathVariable("id") Integer itemId);

    @GetExchange
    RemoteItemResponse findDuplicationData(@RequestParam("serial-no") String serialNo);

    @GetExchange("/next-identity")
    int nextIdentity();
}
