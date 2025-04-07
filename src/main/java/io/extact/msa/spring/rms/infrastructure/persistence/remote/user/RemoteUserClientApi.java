package io.extact.msa.spring.rms.infrastructure.persistence.remote.user;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.remote.GenericClientApi;

@HttpExchange("/users")
public interface RemoteUserClientApi extends GenericClientApi<RemoteUser> {

    @GetExchange("/{id}")
    @Override
    RemoteUser get(@PathVariable Integer id);

    @GetExchange
    @Override
    List<RemoteUser> getAll();

    @PostExchange
    @Override
    void add(@RequestBody RemoteUser item);

    @PutExchange
    @Override
    boolean update(@RequestBody RemoteUser item);

    @DeleteExchange("/{id}")
    @Override
    boolean delete(@PathVariable Integer id);
    
    @GetExchange("/next-identity")
    @Override
    int nextIdentity();

    @GetExchange
    RemoteUser findByLoginId(@RequestParam("login-id") String loginId);

    @GetExchange
    RemoteUser findByLoginIdAndPassword(@RequestParam("login-id") String loginId, @RequestParam String password);
}
