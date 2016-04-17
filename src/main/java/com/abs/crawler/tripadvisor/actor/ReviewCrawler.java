package com.abs.crawler.tripadvisor.actor;

import akka.actor.ActorSystem;
import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.crawler.AbstractCrawler;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.retry.Retryer;
import com.abs.crawler.commons.utils.HttpUtils;
import com.abs.crawler.tripadvisor.Constants;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.apache.commons.lang3.StringUtils;
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
 * @since 2016/4/17 12:07
 */
@Actor
public class ReviewCrawler extends AbstractCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewCrawler.class);

    private static final String REVIEW_URL_TEMPLATE_FIRST = "https://www.tripadvisor.com.sg/Restaurant_Review-g%s-d%s-Reviews-%s-Singapore.html";

    private static final String REVIEW_URL_TEMPLATE = "https://www.tripadvisor.com.sg/Restaurant_Review-g%s-d%s-Reviews-or%s-%s-Singapore.html";

    private static final String IDENTIFIER = "tripadvisor_review";

    private static final Joiner NAME_JOINER = Joiner.on("_");

    @Resource
    private ActorSystem actorSystem;

    @Resource
    private Retryer retryer;


    @Override
    public Request buildRequest(Params params) {
        String restaurantId = (String) params.get(Constants.ReviewParamKeys.RESTAURANT_ID);
        String geoId = (String) params.get(Constants.ReviewParamKeys.GEO_ID);
        String restaurantName = (String) params.get(Constants.ReviewParamKeys.RESTAURANT_NAME);
        restaurantName = restaurantName.replaceAll("[^0-9a-zA-Z]"," ");
        int offset = (int) params.get(Constants.ReviewParamKeys.OFFSET);
        List<Cookie> cookies = (List<Cookie>) params.get(Constants.ReviewParamKeys.COOKIES);

        String [] arr = StringUtils.split(restaurantName, " ");
        if (arr == null || arr.length < 1) {
            return null;
        }
        List<String> names = Lists.newArrayList();
        for (String name : arr) {
            if (StringUtils.isNoneBlank(name)) {
                names.add(name);
            }
        }
        String nameJoin = NAME_JOINER.join(names);
        String url;
        if (offset == 0) {
            url = String.format(REVIEW_URL_TEMPLATE_FIRST, geoId, restaurantId, nameJoin);
        } else {
            url = String.format(REVIEW_URL_TEMPLATE, geoId, restaurantId, offset, nameJoin);
        }
        return HttpUtils.buildRequest(url, cookies);
    }

    @Override
    public String getHttpClientKey() {
        return IDENTIFIER;
    }

    @Override
    public void onSuccess(CrawlerMessage message, Response response) throws IOException {
        String result = response.getResponseBody();
        Document doc = Jsoup.parse(result);
        if (doc == null) {
            LOGGER.warn("review crawler parse error, identifier = {}, message = {}", IDENTIFIER, message);
            return;
        }
        Elements reviewElements = doc.select("div[id^=review_]");
        if (reviewElements.isEmpty()) {
            LOGGER.warn("review crawler review empty, identifier = {}, message = {}, result = {}", IDENTIFIER, message, result);
            return;
        }

        String [] arr = result.split("ta.uid = '");
        if (arr.length < 2) {
            LOGGER.warn("review crawler no uid, identifier = {}, message = {}", IDENTIFIER, message);
            return;
        }

        String uid = arr[1].split("'")[0];

        List<String> reviewIds = Lists.newArrayList();
        for (Element element : reviewElements) {
            String id = element.attr("id");
            reviewIds.add(StringUtils.replace(id, "review_", ""));
        }

        String restaurantId = (String) message.getParams().get(Constants.ReviewParamKeys.RESTAURANT_ID);
        String geoId = (String) message.getParams().get(Constants.ReviewParamKeys.GEO_ID);
        int offset = (int) message.getParams().get(Constants.ReviewParamKeys.OFFSET);
        int reviewCount = (int) message.getParams().get(Constants.ReviewParamKeys.REVIEW_COUNT);
        String restaurantName = (String) message.getParams().get(Constants.ReviewParamKeys.RESTAURANT_NAME);

        Params expandParams = new Params();
        expandParams.param(Constants.ExpandReviewParamKeys.RESTAURANT_ID, restaurantId);
        expandParams.param(Constants.ExpandReviewParamKeys.GEO_ID, geoId);
        expandParams.param(Constants.ExpandReviewParamKeys.OFFSET, offset);
        expandParams.param(Constants.ExpandReviewParamKeys.UID, uid);
        expandParams.param(Constants.ExpandReviewParamKeys.RESTAURANT_NAME, restaurantName);
        expandParams.param(Constants.ExpandReviewParamKeys.REVIEW_IDS, reviewIds);
        expandParams.param(Constants.ExpandReviewParamKeys.REVIEW_COUNT, reviewCount);
        expandParams.param(Constants.ExpandReviewParamKeys.COOKIES, response.getCookies());
        CrawlerMessage expandReviewMessage = new CrawlerMessage(expandParams, ExpandReviewCrawler.class.getName(), 2);
        retryer.retry(expandReviewMessage);
//        ActorRef expandReviewCrawler = actorSystem.actorOf(SpringProps.create(actorSystem, ExpandReviewCrawler.class));
//        expandReviewCrawler.tell(expandReviewMessage, null);

    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
