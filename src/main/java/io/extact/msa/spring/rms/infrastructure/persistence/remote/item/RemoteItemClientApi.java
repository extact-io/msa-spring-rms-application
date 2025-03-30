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
    RemoteItem get(@PathVariable Integer id);

    @GetExchange
    List<RemoteItem> getAll();

    @PostExchange
    void add(@RequestBody RemoteItem item);

    @PutExchange
    void update(@RequestBody RemoteItem item);

    @DeleteExchange("/{id}")
    void delete(@PathVariable Integer id);

    @GetExchange
    RemoteItem findDuplicationData(@RequestParam("serial-no") String serialNo);

    @GetExchange("/next-identity")
    int nextIdentity();
}
