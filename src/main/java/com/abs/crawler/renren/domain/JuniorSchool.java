package com.abs.crawler.renren.domain;

import com.abs.crawler.commons.repository.MongoCollection;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author hao.wang
 * @since 2016/5/13 19:04
 */
@Document(collection = "junior_school")
public class JuniorSchool extends MongoCollection {

    private String province;
    private String city;
    private String area;
    private String name;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

}
