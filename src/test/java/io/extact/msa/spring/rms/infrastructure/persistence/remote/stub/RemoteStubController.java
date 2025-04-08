package io.extact.msa.spring.rms.infrastructure.persistence.remote.stub;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.extact.msa.spring.platform.fw.domain.model.EntityModel;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.PhysicalEntity;

public abstract class RemoteStubController<M extends EntityModel, E extends PhysicalEntity<M>> {
    
    public abstract Map<Integer, E> entityMap();
    
    @GetMapping("/{id}")
    public E get(@PathVariable Integer id) {
        return getAll().stream()
                .filter(entity -> entity.getId().equals(id))
                .findAny()
                .orElse(null);
    }
    
    @GetMapping
    public Collection<E> getAll() {
        return entityMap().values();
    }


    @PostMapping
    public void add(@RequestBody E entity) {
        if (entityMap().putIfAbsent(entity.getId(), entity) != null) {
            throw new IllegalArgumentException("already exists. key:" + entity.getId());
        }
    }

    @PutMapping
    public boolean update(@RequestBody E entity) {
        return entityMap().computeIfPresent(entity.getId(), (_, _) -> entity) != null;
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Integer id) {
        return entityMap().remove(id) != null;
    }
    
    @GetMapping("/next-identity")
    public int nextIdentity() {
        return Collections.max(entityMap().keySet()) + 1;
    }
}
