package com.abs.crawler.renren.actor;

import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.crawler.AbstractCrawler;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.utils.HttpUtils;
import com.abs.crawler.renren.Constants;
import com.abs.crawler.renren.domain.University;
import com.abs.crawler.renren.repository.UniversityRepository;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author hao.wang
 * @since 2016/5/13 01:21
 */
@Actor
public class DepartmentCrawler extends AbstractCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentCrawler.class);

    private static final String URL_TEMPLATE = "http://www.renren.com/GetDep.do?id=%s&requestToken=486986603&_rtk=e8c7d1c5";

    @Resource
    private UniversityRepository universityRepository;

    @Override
    public Request buildRequest(Params params) {
        Integer universityId = params.get(Constants.DepartmentParamKeys.UNIVERSITY_ID, Integer.class);
        University university = universityRepository.query(String.valueOf(universityId));
        if (university != null) {
            return null;
        }
        String url = String.format(URL_TEMPLATE, universityId);
        return HttpUtils.buildRequest(url);
    }

    @Override
    public String getHttpClientKey() {
        return "Department";
    }

    @Override
    public void onSuccess(CrawlerMessage message, Response response) throws IOException {
        String result = response.getResponseBody();
        Document doc = Jsoup.parse(result);
        if (doc == null) {
            LOGGER.warn("department crawler parse error, message = {}", message);
            return;
        }
        Elements options = doc.select("option");
        if (options.isEmpty()) {
            LOGGER.warn("department crawler options empty, message = {}", message);
            return;
        }
        String name = message.getParams().get(Constants.DepartmentParamKeys.NAME, String.class);
        Integer id = message.getParams().get(Constants.DepartmentParamKeys.UNIVERSITY_ID, Integer.class);
        String country = message.getParams().get(Constants.DepartmentParamKeys.COUNTRY, String.class);
        String province = message.getParams().get(Constants.DepartmentParamKeys.PROVINCE, String.class);
        University university = new University();
        university.setId(String.valueOf(id));
        university.setName(name);
        university.setProvince(province);
        university.setCountry(country);
        for (Element element : options) {
            String department = element.attr("value");
            if (StringUtils.isNotBlank(department)) {
                university.addDepartment(department);
            }
        }
        universityRepository.saveOrUpdate(university);
        LOGGER.info("department success, University={}", university.toString());
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
