package com.abs.crawler.renren.domain;

import com.abs.crawler.commons.repository.MongoCollection;
import com.google.common.collect.Lists;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author hao.wang
 * @since 2016/5/13 00:52
 */
@Document(collection = "university")
public class University extends MongoCollection{
    private String country;
    private String province;
    private String name;
    private List<String> departments = Lists.newArrayList();


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getDepartments() {
        return departments;
    }

    public void addDepartment(String department) {
        this.departments.add(department);
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments;
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

    @Override
    public String toString() {
        return "University{" +
                "country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", name='" + name + '\'' +
                ", departments=" + departments +
                '}';
    }
}
