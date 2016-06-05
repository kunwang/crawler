package com.abs.crawler.tripadvisor.domain;

/**
 * @author hao.wang
 * @since 2016/4/16 13:06
 */
public class OpenHour {

    private String day;

    private String hourRange;

    public OpenHour(String day, String hourRange) {
        this.day = day;
        this.hourRange = hourRange;
    }

    public OpenHour() {

    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHourRange() {
        return hourRange;
    }

    public void setHourRange(String hourRange) {
        this.hourRange = hourRange;
    }

}
