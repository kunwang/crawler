package com.abs.crawler.tripadvisor.domain;

import com.abs.crawler.commons.repository.MongoCollection;
import com.abs.crawler.tripadvisor.Constants;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author hao.wang
 * @since 2016/4/16 15:49
 */
@Document(collection = Constants.CollectionNames.DETAIL_PAGES)
public class DetailPage extends MongoCollection {

    private String name;

    private String geoId;

    private List<String> headImages;

    private LocationAndContactInfo locationAndContactInfo;

    private RestaurantDetails details;

    private Geo geo;

    public String getGeoId() {
        return geoId;
    }

    public void setGeoId(String geoId) {
        this.geoId = geoId;
    }

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public RestaurantDetails getDetails() {
        return details;
    }

    public void setDetails(RestaurantDetails details) {
        this.details = details;
    }

    public List<String> getHeadImages() {
        return headImages;
    }

    public void setHeadImages(List<String> headImages) {
        this.headImages = headImages;
    }

    public LocationAndContactInfo getLocationAndContactInfo() {
        return locationAndContactInfo;
    }

    public void setLocationAndContactInfo(LocationAndContactInfo locationAndContactInfo) {
        this.locationAndContactInfo = locationAndContactInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
