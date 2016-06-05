package com.abs.crawler.renren.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * @author hao.wang
 * @since 2016/5/13 22:24
 */
@XStreamAlias("ResultSet")
public class ResultSet {

    @XStreamImplicit
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @XStreamAlias("Result")
    public static class Result {
        @XStreamAlias("Id")
        private String id;
        @XStreamAlias("Name")
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
