package com.abs.crawler.tripadvisor.domain;

import java.util.List;

/**
 * @author hao.wang
 * @since 2016/4/16 12:52
 */
public class RestaurantDetails {

    private String cuisine;

    private List<OpenHour> openHours;

    private String diningOptions;

    private String goodFor;

    private String averagePrices;

    private Rating rating;

    private int reviewCount;

    private String website;

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public List<OpenHour> getOpenHours() {
        return openHours;
    }

    public void setOpenHours(List<OpenHour> openHours) {
        this.openHours = openHours;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getDiningOptions() {
        return diningOptions;
    }

    public void setDiningOptions(String diningOptions) {
        this.diningOptions = diningOptions;
    }

    public String getGoodFor() {
        return goodFor;
    }

    public void setGoodFor(String goodFor) {
        this.goodFor = goodFor;
    }

    public String getAveragePrices() {
        return averagePrices;
    }

    public void setAveragePrices(String averagePrices) {
        this.averagePrices = averagePrices;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }
}
