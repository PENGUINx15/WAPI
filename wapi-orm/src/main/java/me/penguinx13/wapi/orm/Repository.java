package me.penguinx13.wapi.orm;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Repository<T, ID> {

    Optional<T> findById(ID id);

    CompletableFuture<Optional<T>> findByIdAsync(ID id);

    void save(T entity);

    CompletableFuture<Void> saveAsync(T entity);

    void delete(ID id);

    List<T> findAll();
}
