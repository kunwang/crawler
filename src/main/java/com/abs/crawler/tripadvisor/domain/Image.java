package com.abs.crawler.tripadvisor.domain;

import com.abs.crawler.commons.repository.MongoCollection;
import com.abs.crawler.tripadvisor.Constants;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author hao.wang
 * @since 2016/4/16 23:27
 */
@Document(collection = Constants.CollectionNames.IMAGES)
public class Image extends MongoCollection {

    private String restaurantId;

    private String url;

    private String captionText;

    private String author;

    private String displayDate;

    public String getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(String displayDate) {
        this.displayDate = displayDate;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCaptionText() {
        return captionText;
    }

    public void setCaptionText(String captionText) {
        this.captionText = captionText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
