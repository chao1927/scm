package org.scm.common;

public interface BaseRepository<T> {

    void save(T t);

    T findById(Long id);

    void deleteById(Long id);
}
