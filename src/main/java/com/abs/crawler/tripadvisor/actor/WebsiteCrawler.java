package com.abs.crawler.tripadvisor.actor;

import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.crawler.AbstractCrawler;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.utils.HttpUtils;
import com.abs.crawler.tripadvisor.Constants;
import com.abs.crawler.tripadvisor.domain.DetailPage;
import com.abs.crawler.tripadvisor.repository.DetailPageRepository;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author hao.wang
 * @since 2016/4/16 19:32
 */
@Actor
public class WebsiteCrawler extends AbstractCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteCrawler.class);

    private static final String IDENTIFIER = "tripadvisor_website";

    @Resource
    private DetailPageRepository detailPageRepository;


    @Override
    public Request buildRequest(Params params) {
        String redirectUrl = (String)params.get(Constants.WebsiteParamKeys.REDIRECT_URL);
        List<Cookie> cookies = (List<Cookie>) params.get(Constants.WebsiteParamKeys.COOKIES);
        return HttpUtils.buildRequest(redirectUrl, cookies);
    }

    @Override
    public String getHttpClientKey() {
        return IDENTIFIER;
    }

    @Override
    public void onSuccess(CrawlerMessage message, Response response) throws IOException {
        String id = (String) message.getParams().get(Constants.WebsiteParamKeys.DETAIL_PAGE_ID);
        String url = response.getHeader("Location");
        if (!StringUtils.startsWith(url,"http")) {
            LOGGER.warn("website crawler location format error, id = {}, url = {}", id, url);
            return;
        }
        url = StringUtils.replace(url, "#_=_", "");
        DetailPage detailPage = detailPageRepository.query(id);
        if (detailPage == null) {
            LOGGER.warn("website crawler not detail page, id = {}, url = {}", id, url);
            return;
        }
        detailPage.getDetails().setWebsite(url);
        detailPageRepository.saveOrUpdate(detailPage);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
