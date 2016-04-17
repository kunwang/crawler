package com.abs.crawler.tripadvisor.actor;

import akka.actor.ActorSystem;
import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.crawler.AbstractCrawler;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.retry.Retryer;
import com.abs.crawler.commons.utils.HttpRequestType;
import com.abs.crawler.commons.utils.HttpUtils;
import com.abs.crawler.tripadvisor.Constants;
import com.abs.crawler.tripadvisor.Utils;
import com.abs.crawler.tripadvisor.domain.Review;
import com.abs.crawler.tripadvisor.repository.ReviewRepository;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.ning.http.client.Param;
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
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author hao.wang
 * @since 2016/4/17 13:33
 */
@Actor
public class ExpandReviewCrawler extends AbstractCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpandReviewCrawler.class);

    private static final String REVIEW_URL_TEMPLATE = "https://www.tripadvisor.com.sg/ExpandedUserReviews-g%s-d%s?target=%s&context=1&reviews=%s&servlet=Restaurant_Review&expand=1";

    private static final String IDENTIFIER = "tripadvisor_expand_review";

    private static final Joiner ID_JOINER = Joiner.on(",");

    @Resource
    private ActorSystem actorSystem;

    @Resource
    private ReviewRepository reviewRepository;

    @Resource
    private Retryer retryer;

    @Override
    public Request buildRequest(Params params) {
        String restaurantId = (String) params.get(Constants.ExpandReviewParamKeys.RESTAURANT_ID);
        String geoId = (String) params.get(Constants.ExpandReviewParamKeys.GEO_ID);
        String uid = (String) params.get(Constants.ExpandReviewParamKeys.UID);

        List<String> reviewIds = (List<String>) params.get(Constants.ExpandReviewParamKeys.REVIEW_IDS);
        if (CollectionUtils.isEmpty(reviewIds)) {
            return null;
        }
        List<Cookie> cookies = (List<Cookie>) params.get(Constants.ExpandReviewParamKeys.COOKIES);
        String targetReviewId = reviewIds.get(0);
        String reviewIdsJoin = ID_JOINER.join(reviewIds);
        String url = String.format(REVIEW_URL_TEMPLATE, geoId, restaurantId, targetReviewId, reviewIdsJoin);

        List<Param> httpParams = Lists.newArrayList();
        httpParams.add(new Param("gac", "Reviews"));
        httpParams.add(new Param("gaa", "expand"));
        httpParams.add(new Param("gass", "Restaurant_Review"));
        httpParams.add(new Param("gams", "0"));
        httpParams.add(new Param("gasl", restaurantId));
        httpParams.add(new Param("gapu", uid));
        return HttpUtils.buildRequest(HttpRequestType.POST, url, httpParams, cookies);
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
            LOGGER.warn("expand review crawler parse error, identifier = {}, message = {}", IDENTIFIER, message);
            return;
        }
        Elements reviewElements = doc.select("div[id^=expanded_review_]");
        if (reviewElements.isEmpty()) {
            LOGGER.warn("expand review crawler review empty, identifier = {}, message = {}", IDENTIFIER, message);
            return;
        }
        int i = 0;

        String restaurantId = (String) message.getParams().get(Constants.ExpandReviewParamKeys.RESTAURANT_ID);
        for (Element element : reviewElements) {
            Review review = new Review();
            review.setRestaurantId(restaurantId);

            String id = StringUtils.replace(element.attr("id"), "expanded_review_", "");
            review.setId(id);

            Element usernameElement = element.select("div[class=username mo]").first();
            if (usernameElement != null) {
                review.setAuthor(usernameElement.text());
            }

            Element titleElement = element.select("span[class=noQuotes]").first();
            if (titleElement != null) {
                review.setTitle(titleElement.text());
            }

            Element dateElement = element.select("span[class=ratingDate]").first();
            if (dateElement != null) {
                review.setDate(StringUtils.replace(dateElement.text(), "Reviewed ", ""));
            } else {
                dateElement = element.select("span[class=ratingDate relativeDate]").first();
                if (dateElement != null) {
                    review.setDate(dateElement.attr("title"));
                }
            }

            Element contentElement = element.select("div[class=entry]").first();
            if (contentElement != null) {
                review.setContent(contentElement.text());
            }

            Element totalElement = element.select("span[class=rate sprite-rating_s rating_s]").first();
            if (totalElement != null) {
                review.setTotal(Utils.parseRatingValue(totalElement));
            }

            Elements ratingElements = element.select("li[class=recommend-answer]");
            if (!ratingElements.isEmpty()) {
                for (Element ratingElement : ratingElements) {
                    String text = ratingElement.text();
                    if (StringUtils.equals("Value", text)) {
                        Element valueElement = ratingElement.select("span[class=rate sprite-rating_ss rating_ss]").first();
                        if (valueElement != null) {
                            review.setValue(Utils.parseRatingValue(valueElement));
                        }
                    } else if (StringUtils.equals("Food", text)) {
                        Element foodElement = ratingElement.select("span[class=rate sprite-rating_ss rating_ss]").first();
                        if (foodElement != null) {
                            review.setFood(Utils.parseRatingValue(foodElement));
                        }
                    } else if (StringUtils.equals("Service", text)) {
                        Element serviceElement = ratingElement.select("span[class=rate sprite-rating_ss rating_ss]").first();
                        if (serviceElement != null) {
                            review.setService(Utils.parseRatingValue(serviceElement));
                        }
                    } else if (StringUtils.equals("Atmosphere", text)) {
                        Element atmosphereElement = ratingElement.select("span[class=rate sprite-rating_ss rating_ss]").first();
                        if (atmosphereElement != null) {
                            review.setAtmosphere(Utils.parseRatingValue(atmosphereElement));
                        }
                    }
                }
            }

            Elements imageElements = element.select("span[class^=u_/LocationPhotoDirectLink-]");
            if (!imageElements.isEmpty()) {
                List<Review.ReviewImage> images = Lists.newArrayList();
                for (Element imageElement : imageElements) {
                    String idStr = imageElement.attr("class");
                    String [] arr = StringUtils.split(idStr, "#");
                    if (arr == null || arr.length < 2) {
                        continue;
                    }
                    String [] arr1 = StringUtils.split(arr[1], " ");
                    if (arr1 == null || arr1.length < 2) {
                        continue;
                    }
                    String imageId = arr1[0];

                    Element srcElement = imageElement.select("img").first();
                    if (srcElement == null) {
                        continue;
                    }
                    String url = srcElement.attr("src");

                    Review.ReviewImage image = new Review.ReviewImage();
                    image.setImageId(imageId);
                    image.setUrl(url);
                    images.add(image);
                }
                review.setImages(images);
            }
            reviewRepository.saveOrUpdate(review);
            i ++;
        }

        int offset = (int) message.getParams().get(Constants.ExpandReviewParamKeys.OFFSET);
        int reviewCount = (int) message.getParams().get(Constants.ExpandReviewParamKeys.REVIEW_COUNT);
        String geoId = (String) message.getParams().get(Constants.ExpandReviewParamKeys.GEO_ID);

        LOGGER.info("expand review crawler,restaurantId = {}, offset = {}, count = {}, totalCount = {}", restaurantId, offset, i, reviewCount);


        String restaurantName = (String) message.getParams().get(Constants.ExpandReviewParamKeys.RESTAURANT_NAME);
        int nowOffset = offset + 10;
        if (nowOffset < reviewCount) {
            Params reviewParams = new Params();
            reviewParams.param(Constants.ReviewParamKeys.RESTAURANT_ID, restaurantId);
            reviewParams.param(Constants.ReviewParamKeys.OFFSET, nowOffset);
            reviewParams.param(Constants.ReviewParamKeys.GEO_ID, geoId);
            reviewParams.param(Constants.ReviewParamKeys.RESTAURANT_NAME, restaurantName);
            reviewParams.param(Constants.ReviewParamKeys.REVIEW_COUNT, reviewCount);
            reviewParams.param(Constants.ReviewParamKeys.COOKIES, response.getCookies());
            CrawlerMessage reviewMessage = new CrawlerMessage(reviewParams, ReviewCrawler.class.getName(), 1);
            retryer.retry(reviewMessage);
//            ActorRef reviewCrawler = actorSystem.actorOf(SpringProps.create(actorSystem, ReviewCrawler.class));
//            reviewCrawler.tell(reviewMessage, null);
        }
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
