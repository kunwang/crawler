package com.abs.crawler.commons.repository;

import java.util.Collection;
import java.util.List;

/**
 * @author hao.wang
 * @since 2016/1/8 16:25
 */
public interface BaseRepository<T> {

    T query(String id);

    List<T> query(Collection<String> ids);

    List<T> query();

    void update(Collection<T> objects);

    void update(T object);

    void saveOrUpdate(T object);

    void insert(T object);

    void insert(Collection<T> objects);

    void remove(String id);

}
