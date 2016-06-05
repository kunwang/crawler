package com.abs.crawler.renren.actor;

import akka.actor.UntypedActor;
import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.retry.Retryer;
import com.abs.crawler.renren.Constants;
import com.abs.crawler.renren.domain.City;
import com.abs.crawler.renren.domain.Province;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hao.wang
 * @since 2016/5/13 02:58
 */
@Actor
public class ProvinceCityReader extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProvinceCityReader.class);

    private static final String RENREN_PROVINCE = UniversityReader.class.getClassLoader().getResource("renrenProvince").getPath();
    private static final String RENREN_CITY = UniversityReader.class.getClassLoader().getResource("renrenCity").getPath();

    private static AtomicInteger COUNT = new AtomicInteger(0);

    @Resource
    private Retryer retryer;

    @Override
    public void onReceive(Object o) throws Exception {
        List<Province> provinces =  readProvince(RENREN_PROVINCE);
        List<List<City>> cityLists = readCity(RENREN_CITY);
        for (int i = 0; i < provinces.size(); i ++) {
            Province province = provinces.get(i);
            List<City> cities = cityLists.get(i);
            for (int j = 1; j <= cities.size(); j ++ ) {
                int count = COUNT.incrementAndGet();
                String cityId = province.getId() + this.fill2Byte(j);
                Params params = new Params();
                params.param(Constants.HCJEParamKeys.CITY, cities.get(j - 1).getName());
                params.param(Constants.HCJEParamKeys.PROVINCE, province.getName());
                params.param(Constants.HCJEParamKeys.CITY_ID, cityId);
                CrawlerMessage hMessage = new CrawlerMessage(params, HighSchoolCrawler.class.getName(), (count / 5));
                CrawlerMessage cMessage = new CrawlerMessage(params, CollegeCrawler.class.getName(), (count / 5));
                CrawlerMessage eMessage = new CrawlerMessage(params, ElementarySchoolCrawler.class.getName(), (count / 5));
                CrawlerMessage jMessage = new CrawlerMessage(params, JuniorSchoolCrawler.class.getName(), (count / 5));
                retryer.retry(hMessage);
                retryer.retry(cMessage);
                retryer.retry(eMessage);
                retryer.retry(jMessage);
                LOGGER.info("province = {}, city = {} ,city count = {}",province.getName(), cities.get(j-1).getName(),count);
            }
        }
    }

    private String fill2Byte(int number) {
        if (number >= 100) {
            return String.valueOf(number % 100);
        } else if ( number < 10) {
            return "0" + String.valueOf(number);
        } else {
            return String.valueOf(number);
        }
    }

    private static List<Province> readProvince(String file) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
        String line;
        List<Province> provinces = Lists.newArrayList();
        while ( (line = br.readLine()) != null) {
            String [] arr = line.split("id:");
            if (arr.length != 2) {
                continue;
            }
            String [] arr1 = arr[1].split(",");
            if (arr1.length < 2) {
                continue;
            }
            String id = StringUtils.trimToEmpty(arr1[0]);
            String arr2 [] = arr1[1].split("'");
            if (arr2.length < 2) {
                continue;
            }
            String name = arr2[1];
            Province province = new Province();
            province.setId(id);
            province.setName(name);
            provinces.add(province);
        }
        br.close();
        return provinces;
    }

    private static List<List<City>> readCity(String file) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
        String line;
        List<List<City>> cities = Lists.newArrayList();
        while ( (line = br.readLine()) != null) {
            String [] arr = line.split("\\[");
            if (arr.length != 2) {
                continue;
            }
            String [] arr1 = arr[1].split("\\]");
            if (arr1.length != 2) {
                continue;
            }
            List<City> cityList = Lists.newArrayList();
            String [] arr2 = arr1[0].split(",");
            for (String str : arr2) {
                String tmp = StringUtils.replace(str, "\"", "");
                String [] arr3 = tmp.split(":");
                if (arr3.length != 2) {
                    continue;
                }
                UnicodeUnescaper unicodeUnescaper = new UnicodeUnescaper();
                String name = unicodeUnescaper.translate(arr3[1]);

                City city = new City();
                city.setId(arr3[0]);
                city.setName(name);

                cityList.add(city);
            }
            cities.add(cityList);
        }
        br.close();
        return cities;
    }

}
