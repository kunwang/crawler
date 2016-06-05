package com.abs.crawler.renren.actor;

import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.crawler.AbstractCrawler;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.utils.HttpUtils;
import com.abs.crawler.renren.Constants;
import com.abs.crawler.renren.domain.College;
import com.abs.crawler.renren.repository.CollegeRepository;
import com.google.common.collect.Maps;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @author hao.wang
 * @since 2016/5/13 04:24
 */
@Actor
public class CollegeCrawler extends AbstractCrawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollegeCrawler.class);

    private static final String TEMPLATE = "http://support.renren.com/collegeschool/%s.html?requestToken=486986603&_rtk=e8c7d1c5";

    @Resource
    private CollegeRepository collegeRepository;

    @Override
    public Request buildRequest(Params params) {
        String cityId = params.get(Constants.HCJEParamKeys.CITY_ID, String.class);
        String url = String.format(TEMPLATE, cityId);
        return HttpUtils.buildRequest(url);
    }

    @Override
    public String getHttpClientKey() {
        return "College";
    }

    @Override
    public void onSuccess(CrawlerMessage message, Response response) throws IOException {
        String result = response.getResponseBody();
        Document doc = Jsoup.parse(result);
        if (doc == null) {
            LOGGER.warn("college crawler parse error, message = {}",  message);
            return;
        }
        Map<String, String> areaMap = Maps.newHashMap();
        Elements areaElements = doc.select("#schoolCityQuList li a");
        if (!areaElements.isEmpty()) {
            for (Element element : areaElements) {
                String onclick = element.attr("onclick");
                if (StringUtils.isBlank(onclick)) {
                    continue;
                }
                String [] arr = onclick.split("'");
                if (arr.length < 2) {
                    continue;
                }
                String areaId = arr[1];
                UnicodeUnescaper unicodeUnescaper = new UnicodeUnescaper();
                String name = unicodeUnescaper.translate(element.text());
                areaMap.put(areaId, name);
            }
        }
        String cityId = message.getParams().get(Constants.HCJEParamKeys.CITY_ID, String.class);
        String city = message.getParams().get(Constants.HCJEParamKeys.CITY, String.class);
        String province = message.getParams().get(Constants.HCJEParamKeys.PROVINCE, String.class);
        Elements areaDetailElements = doc.select("ul");
        int count = 0;
        if (!areaDetailElements.isEmpty()) {
            for (Element areaDetail : areaDetailElements) {
                if (StringUtils.equals(areaDetail.attr("style"), "display:none;")) {
                    String areaId = areaDetail.attr("id");
                    String areaName = areaMap.get(areaId);
                    Elements schoolElements = areaDetail.select("li a");
                    if (!schoolElements.isEmpty()) {
                        for (Element schoolElement : schoolElements) {
                            UnicodeUnescaper unicodeUnescaper = new UnicodeUnescaper();
                            String school = unicodeUnescaper.translate(schoolElement.text());
                            String id = schoolElement.attr("href");
                            College college = new College();
                            college.setId(id);
                            college.setName(school);
                            college.setCity(city);
                            college.setProvince(province);
                            college.setArea(areaName);
                            collegeRepository.saveOrUpdate(college);
                            count ++;
                        }
                    }
                }
            }
        }
        LOGGER.info("college crawler,province={},city={},cityId={},count={}", province, city, cityId, count);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
