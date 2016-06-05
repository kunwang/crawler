package com.abs.crawler.tripadvisor.actor;

import akka.actor.ActorSystem;
import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.crawler.AbstractCrawler;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.retry.Retryer;
import com.abs.crawler.commons.utils.HttpUtils;
import com.abs.crawler.tripadvisor.Constants;
import com.abs.crawler.tripadvisor.domain.DetailPage;
import com.abs.crawler.tripadvisor.repository.DetailPageRepository;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author hao.wang
 * @since 2016/4/15 00:04
 */
@Actor
public class ListCrawler extends AbstractCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCrawler.class);

    private static final String IDENTIFIER = "tripadvisor_list";

    private static final String GEO_ID = "294265";

    private static final String LIST_URL_TEMPLATE = "https://www.tripadvisor.com.sg/RestaurantSearch?Action=PAGE&geo=294265&ajax=1&itags=10591&sortOrder=popularity&o=a%s&availSearchEnabled=false";

    private static final int PAGE_COUNT = 30;


    @Resource
    private ActorSystem actorSystem;

    @Resource
    private Retryer retryer;

    @Resource
    private DetailPageRepository detailPageRepository;


    @Override
    public Request buildRequest(Params params) {
        int index = (Integer) params.get(Constants.ListParamKeys.INDEX);

        List<Cookie> cookies = (List<Cookie>)params.get(Constants.ListParamKeys.COOKIES);
        String url = String.format(LIST_URL_TEMPLATE, index * PAGE_COUNT);
        LOGGER.info("list crawler start , index = {}, url = {}", index, url);
        return HttpUtils.buildRequest(url, cookies);
    }

    @Override
    public String getHttpClientKey() {
        return IDENTIFIER;
    }

    @Override
    public void onSuccess(CrawlerMessage message, Response response) throws IOException{
        String result = response.getResponseBody();
        Document doc = Jsoup.parse(result);
        if (doc == null) {
            LOGGER.warn("list crawler parse error, identifier = {}, message = {}", IDENTIFIER, message);
            return;
        }
        Elements elements = doc.select(".shortSellDetails .title a");
        if (elements.size() <= 0) {
            LOGGER.warn("list crawler items error, identifier = {}, result = {}", IDENTIFIER, result);
            return;
        }

        int count = 0;
        for (Element element : elements) {
            String url = element.attr("href");
            if (StringUtils.isBlank(url) || StringUtils.startsWith(url,"/Guide-")) {
                LOGGER.warn("list crawler item empty, identifier = {}, item = {}", IDENTIFIER, element);
                continue;
            }

            String arr [] = StringUtils.split(url, "-");
            if (arr == null || arr.length < 3 || !StringUtils.startsWith(arr[2], "d")) {
                LOGGER.warn("list crawler not format detail url error, identifier = {}, url = {}", IDENTIFIER, url);
                continue;
            }

            String id = StringUtils.replace(arr[2], "d", "");

            DetailPage detailPage = detailPageRepository.query(id);
            if (detailPage == null) {
                Params detailParams = new Params();
                detailParams.param(Constants.DetailParamKeys.URL, url);
                detailParams.param(Constants.DetailParamKeys.ID, id);
                detailParams.param(Constants.DetailParamKeys.GEO_ID, GEO_ID);
                detailParams.param(Constants.DetailParamKeys.COOKIES, response.getCookies());
                CrawlerMessage detailMessage = new CrawlerMessage(detailParams, DetailCrawler.class.getName(), 5 * (count / 5));

                retryer.retry(detailMessage);
                count ++;
            }
//            ActorRef detailCrawler = actorSystem.actorOf(SpringProps.create(actorSystem, DetailCrawler.class));
//            detailCrawler.tell(detailMessage, null);

        }

        Element totalPageElement = doc.select(".pageNumbers a").last();
        if (totalPageElement == null) {
            LOGGER.warn("list crawler total page count error, identifier = {}", IDENTIFIER);
            return;
        }
        int totalPageCount = NumberUtils.toInt(totalPageElement.text());
        int index = (Integer) message.getParams().get(Constants.ListParamKeys.INDEX);

        LOGGER.warn("list crawler pageIndex = {}, totalPageCount = {}, count= {}", index, totalPageCount, count);
        int nextPageIndex = index + 1;
        if (nextPageIndex > (totalPageCount - 1)) {
            LOGGER.info("list crawler pages crawl done, total page = {}, identifier = {}", totalPageCount, IDENTIFIER);
            return;
        }

        Params params = new Params();
        params.param(Constants.ListParamKeys.INDEX, nextPageIndex);
        params.param(Constants.ListParamKeys.COOKIES, response.getCookies());
        CrawlerMessage listMessage = new CrawlerMessage(params, ListCrawler.class.getName(), count + 1);
        // for delay
        retryer.retry(listMessage);

    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
