package com.abs.crawler.renren.domain;

import java.util.List;

/**
 * @author hao.wang
 * @since 2016/5/13 00:04
 */
public class Universities {
    private int id;
    private String name;
    private List<University> univs;
    private List<Province> provs;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Province> getProvs() {
        return provs;
    }

    public void setProvs(List<Province> provs) {
        this.provs = provs;
    }

    public List<University> getUnivs() {
        return univs;
    }

    public void setUnivs(List<University> univs) {
        this.univs = univs;
    }

    public static class Province {
        private int  id;
        private int country_id;
        private String name;
        private List<University> univs;

        public int getCountry_id() {
            return country_id;
        }

        public void setCountry_id(int country_id) {
            this.country_id = country_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<University> getUnivs() {
            return univs;
        }

        public void setUnivs(List<University> univs) {
            this.univs = univs;
        }
    }

    public static class University {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
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
