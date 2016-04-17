package com.abs.crawler.tripadvisor.domain;

/**
 * @author hao.wang
 * @since 2016/4/16 17:10
 */
public class Geo {

    private String lat;

    private String lng;

    public Geo(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Geo(){

    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
