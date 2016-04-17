package com.abs.crawler.tripadvisor.domain;

/**
 * @author hao.wang
 * @since 2016/4/16 13:39
 */
public class Rating {

    private float total;

    private int rank;

    private float food;

    private float service;

    private float value;

    private float atmosphere;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(float atmosphere) {
        this.atmosphere = atmosphere;
    }

    public float getFood() {
        return food;
    }

    public void setFood(float food) {
        this.food = food;
    }

    public float getService() {
        return service;
    }

    public void setService(float service) {
        this.service = service;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
