package com.abs.crawler.commons.repository;

import org.springframework.data.annotation.Id;

/**
 * @author hao.wang
 * @since 2016/1/10 16:10
 */
public class MongoCollection {

    @Id
    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
