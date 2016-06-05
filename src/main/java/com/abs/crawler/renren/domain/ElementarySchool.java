package com.abs.crawler.renren.domain;

import com.abs.crawler.commons.repository.MongoCollection;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author hao.wang
 * @since 2016/5/13 22:51
 */
@Document(collection = "elementary_school")
public class ElementarySchool extends MongoCollection{

    private String province;
    private String city;
    private String name;

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
