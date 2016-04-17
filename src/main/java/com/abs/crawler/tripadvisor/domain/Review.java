package com.abs.crawler.tripadvisor.domain;

import com.abs.crawler.commons.repository.MongoCollection;
import com.abs.crawler.tripadvisor.Constants;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author hao.wang
 * @since 2016/4/17 13:54
 */
@Document(collection = Constants.CollectionNames.REVIEWS)
public class Review extends MongoCollection {

    private String restaurantId;

    private String author;

    private String title;

    private String content;

    private String date;

    private float total;

    private float food;

    private float service;

    private float value;

    private float atmosphere;

    private List<ReviewImage> images;

    public float getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(float atmosphere) {
        this.atmosphere = atmosphere;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getFood() {
        return food;
    }

    public void setFood(float food) {
        this.food = food;
    }

    public List<ReviewImage> getImages() {
        return images;
    }

    public void setImages(List<ReviewImage> images) {
        this.images = images;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public float getService() {
        return service;
    }

    public void setService(float service) {
        this.service = service;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public static class ReviewImage {

        private String imageId;

        private String url;


        public String getImageId() {
            return imageId;
        }

        public void setImageId(String imageId) {
            this.imageId = imageId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
