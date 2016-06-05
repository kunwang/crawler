package com.abs.crawler.renren.actor;

import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.crawler.AbstractCrawler;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.utils.HttpUtils;
import com.abs.crawler.renren.Constants;
import com.abs.crawler.renren.domain.ElementarySchool;
import com.abs.crawler.renren.domain.ResultSet;
import com.abs.crawler.renren.repository.ElementarySchoolRepository;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author hao.wang
 * @since 2016/5/13 04:25
 */
@Actor
public class ElementarySchoolCrawler extends AbstractCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElementarySchoolCrawler.class);

    private static final String URL_TEMPLATE = "http://www.renren.com/autocomplete_elementaryschool.jsp?requestToken=486986603&_rtk=e8c7d1c5&query=%s";

    @Resource
    private ElementarySchoolRepository elementarySchoolRepository;


    @Override
    public Request buildRequest(Params params) {
        String city = params.get(Constants.HCJEParamKeys.CITY, String.class);
        String url = StringUtils.EMPTY;
        try {
            url = String.format(URL_TEMPLATE, URLEncoder.encode(city, "UTF-8"));
        } catch (UnsupportedEncodingException e) {}
        if (StringUtils.isBlank(url)) {
            return null;
        }
        return HttpUtils.buildRequest(url);
    }

    @Override
    public String getHttpClientKey() {
        return "ElementarySchool";
    }

    @Override
    public void onSuccess(CrawlerMessage message, Response response) throws IOException {
        XStream xs = new XStream(new DomDriver());
        xs.processAnnotations(ResultSet.class);
        xs.processAnnotations(ResultSet.Result.class);
        String city = message.getParams().get(Constants.HCJEParamKeys.CITY, String.class);
        String province = message.getParams().get(Constants.HCJEParamKeys.PROVINCE, String.class);
        String content = response.getResponseBody();
        ResultSet resultSet = (ResultSet)xs.fromXML(content);
        if (resultSet == null) {
            LOGGER.warn("ElementarySchool crawler no result, city={}", city);
            return;
        }
        int count = 0;
        if (!CollectionUtils.isEmpty(resultSet.getResults())) {
            for (ResultSet.Result result : resultSet.getResults()) {
                ElementarySchool elementarySchool = new ElementarySchool();
                elementarySchool.setId(result.getId());
                elementarySchool.setName(result.getName());
                elementarySchool.setCity(city);
                elementarySchool.setProvince(province);

                elementarySchoolRepository.saveOrUpdate(elementarySchool);
                count ++;
            }
        }
        LOGGER.info("elementary school crawler,province={},city={},count={}", province, city, count);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}

