package com.abs.crawler.tripadvisor.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.abs.crawler.akka.Actor;
import com.abs.crawler.akka.SpringProps;
import com.abs.crawler.commons.crawler.AbstractCrawler;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.utils.HttpUtils;
import com.abs.crawler.commons.utils.JsEngineFactory;
import com.abs.crawler.commons.utils.JsonUtils;
import com.abs.crawler.tripadvisor.Constants;
import com.abs.crawler.tripadvisor.Utils;
import com.abs.crawler.tripadvisor.domain.DetailPage;
import com.abs.crawler.tripadvisor.domain.Geo;
import com.abs.crawler.tripadvisor.domain.LocationAndContactInfo;
import com.abs.crawler.tripadvisor.domain.OpenHour;
import com.abs.crawler.tripadvisor.domain.Rating;
import com.abs.crawler.tripadvisor.domain.RestaurantDetails;
import com.abs.crawler.tripadvisor.repository.DetailPageRepository;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.type.TypeReference;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.script.Invocable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author hao.wang
 * @since 2016/4/15 00:05
 */
@Actor
public class DetailCrawler extends AbstractCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DetailCrawler.class);

    private static final String IDENTIFIER = "tripadvisor_detail";

    private static final String URL_PREFIX = "https://www.tripadvisor.com.sg";

    private static final String JS_CODE_PATH = DetailCrawler.class.getClassLoader().getResource("js.js").getPath();


    @Resource
    private DetailPageRepository detailPageRepository;

    @Resource
    private ActorSystem actorSystem;

    @Override
    public Request buildRequest(Params params) {
        String urlSuffix = (String)params.get(Constants.DetailParamKeys.URL);
        String url = URL_PREFIX + urlSuffix;
        List<Cookie> cookies = (List<Cookie>)params.get(Constants.ListParamKeys.COOKIES);
        LOGGER.info("tripadviser detail crawler , url = {}", url);
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
            LOGGER.warn("detail crawler parse error, identifier = {}, message = {}", IDENTIFIER, message);
            return;
        }
        Element nameElement = doc.select("div[class=warLocName]").first();
        if (nameElement == null) {
            LOGGER.warn("detail crawler no name error, identifier = {}, result = {}", IDENTIFIER, result);
            return;
        }

        List<String> headImages = this.parseHeadImage(doc);

        Geo geo = null;
        Element geoElement = doc.select("div[class=mapContainer]").first();
        if (geoElement != null) {
            geo = new Geo(geoElement.attr("data-lat"), geoElement.attr("data-lng"));
        }

        String name = StringUtils.trimToEmpty(nameElement.text());
        String id = (String)message.getParams().get(Constants.DetailParamKeys.ID);
        String geoId = (String)message.getParams().get(Constants.DetailParamKeys.GEO_ID);
        LocationAndContactInfo locationAndContactInfo = this.parseLocationAndContactInfo(doc);
        RestaurantDetails details = this.parseRestaurantDetails(doc);

        DetailPage detailPage = new DetailPage();
        detailPage.setId(id);
        detailPage.setGeoId(geoId);
        detailPage.setName(name);
        detailPage.setHeadImages(headImages);
        detailPage.setLocationAndContactInfo(locationAndContactInfo);
        detailPage.setDetails(details);
        detailPage.setGeo(geo);
        detailPageRepository.saveOrUpdate(detailPage);

        // website url crawler
        if (details != null && !StringUtils.isBlank(details.getWebsite())) {
            Params websiteParams = new Params();
            websiteParams.param(Constants.WebsiteParamKeys.DETAIL_PAGE_ID, detailPage.getId());
            websiteParams.param(Constants.WebsiteParamKeys.REDIRECT_URL, detailPage.getDetails().getWebsite());
            websiteParams.param(Constants.WebsiteParamKeys.COOKIES, response.getCookies());
            CrawlerMessage websiteMessage = new CrawlerMessage(websiteParams, WebsiteCrawler.class.getName());
            ActorRef websiteCrawler = actorSystem.actorOf(SpringProps.create(actorSystem, WebsiteCrawler.class));
            websiteCrawler.tell(websiteMessage, null);
        }

        // images crawler
        Element imageCountElement = doc.select(".flexible_photo_album_link .count").first();
        if (imageCountElement != null) {
            String count = StringUtils.replace(imageCountElement.text(), "(", "");
            count = StringUtils.replace(count, ")", "");
            int imageCount = NumberUtils.toInt(count);
            Params imageParams = new Params();
            imageParams.param(Constants.ImageParamKeys.RESTAURANT_ID, detailPage.getId());
            imageParams.param(Constants.ImageParamKeys.OFFSET, 0);
            imageParams.param(Constants.ImageParamKeys.COUNT, imageCount);
            imageParams.param(Constants.ImageParamKeys.COOKIES, response.getCookies());
            CrawlerMessage imageMessage = new CrawlerMessage(imageParams, ImageCrawler.class.getName());
            ActorRef imageCrawler = actorSystem.actorOf(SpringProps.create(actorSystem, ImageCrawler.class));
            imageCrawler.tell(imageMessage, null);
        }

        if (details != null && details.getReviewCount() > 0) {
            Params reviewParams = new Params();
            reviewParams.param(Constants.ReviewParamKeys.RESTAURANT_ID, detailPage.getId());
            reviewParams.param(Constants.ReviewParamKeys.OFFSET, 0);
            reviewParams.param(Constants.ReviewParamKeys.GEO_ID, geoId);
            reviewParams.param(Constants.ReviewParamKeys.RESTAURANT_NAME, name);
            reviewParams.param(Constants.ReviewParamKeys.REVIEW_COUNT, details.getReviewCount());
            reviewParams.param(Constants.ReviewParamKeys.COOKIES, response.getCookies());
            CrawlerMessage reviewMessage = new CrawlerMessage(reviewParams, ReviewCrawler.class.getName());
            ActorRef reviewCrawler = actorSystem.actorOf(SpringProps.create(actorSystem, ReviewCrawler.class));
            reviewCrawler.tell(reviewMessage, null);
        }


    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }


    private LocationAndContactInfo parseLocationAndContactInfo(Document doc) {
        Elements detailElements = doc.select(".additional_info .content .detailsContent li");
        if (detailElements.isEmpty()) {
            LOGGER.warn("detail crawler Location and Contact Information-Address not exist, identifier = {}", IDENTIFIER);
            return null;
        }

        String streetAddress = StringUtils.EMPTY;
        String locality = StringUtils.EMPTY;
        String countryName = StringUtils.EMPTY;
        String phoneNumber = StringUtils.EMPTY;
        String neighbourhood = StringUtils.EMPTY;
        List<String> location = Lists.newArrayListWithExpectedSize(4);

        LocationAndContactInfo info = new LocationAndContactInfo();

        for (Element element : detailElements) {
            String text = element.text();
            if (StringUtils.startsWith(text, "Address:")) {
                Element streetAddressElement = element.select(".detail .format_address span[class=street-address]").first();
                if (streetAddressElement != null) {
                    streetAddress = StringUtils.trimToEmpty(streetAddressElement.text());
                }
                Element localityElement = element.select(".detail .format_address span[class=locality]").first();
                if (localityElement != null) {
                    locality = StringUtils.trimToEmpty(localityElement.text());
                }
                Element countryNameElement = element.select(".detail .format_address span[class=country-name]").first();
                if (countryNameElement != null) {
                    countryName = StringUtils.trimToEmpty(countryNameElement.text());
                }
            } else if (StringUtils.startsWith(text, "Location:")) {
                Elements locationElements = element.select("span");
                if (!locationElements.isEmpty()) {
                    for (Element locationElement : locationElements) {
                        location.add(StringUtils.trimToEmpty(StringUtils.replace(locationElement.text(), ">", "")));
                    }
                }
            } else if (StringUtils.startsWith(text, "Phone Number:")) {
                Element phoneNumberElement = element.select("span").first();
                if (phoneNumberElement != null) {
                    phoneNumber = StringUtils.trimToEmpty(phoneNumberElement.text());
                }
            } else if (StringUtils.startsWith(text, "Neighbourhood:")) {
                neighbourhood = StringUtils.trimToEmpty(StringUtils.split(text, "Neighbourhood:")[0]);
            }
        }
        info.setStreetAddress(streetAddress);
        info.setCountryName(countryName);
        info.setLocality(locality);
        info.setLocation(location);
        info.setNeighbourhood(neighbourhood);
        info.setPhoneNumber(phoneNumber);
        return info;
    }

    private RestaurantDetails parseRestaurantDetails(Document doc) {
        Elements elements = doc.select(".details_tab .table_section .row");
        if (elements.isEmpty()) {
            LOGGER.warn("detail crawler details not exist, identifier = {}", IDENTIFIER);
            return null;
        }
        RestaurantDetails details = new RestaurantDetails();
        for (Element element : elements) {
            String text = element.text();
            if (StringUtils.startsWith(text,"Open Hours")) {
                details.setOpenHours(this.parseOpenHour(element));
            } else if (StringUtils.startsWith(text,"Cuisine")) {
                details.setCuisine(this.parseSingleContent(element));
            } else if (StringUtils.startsWith(text,"Dining options")) {
                details.setDiningOptions(this.parseSingleContent(element));
            } else if (StringUtils.startsWith(text,"Good for")) {
                details.setGoodFor(this.parseSingleContent(element));
            } else if (StringUtils.startsWith(text,"Average prices")) {
                details.setAveragePrices(this.parseSingleContent(element));
            } else if (StringUtils.startsWith(text,"Rating summary")) {
                details.setRating(this.parseRating(doc, element));
            }
        }

        Element reviewCountElement = doc.select("a[property=reviewCount]").first();
        if (reviewCountElement != null) {
            details.setReviewCount(NumberUtils.toInt(reviewCountElement.attr("content")));
        }

        Element websiteElement = doc.select("div[class=fl] span[class=taLnk] ").first();
        if (websiteElement != null && StringUtils.equals("Website", StringUtils.trimToEmpty(websiteElement.text()))) {
            String onClick = websiteElement.attr("onClick");
            String [] arr = onClick.split("'aHref':'");
            if (arr != null && arr.length == 2) {
                String [] arr1 = StringUtils.split(arr[1], "\'");
                if (arr1 != null && arr1.length > 1) {
                    String asdf = arr1[0];
                    String urlSuffix = asdf(asdf);
                    String websiteRedirectUrl = URL_PREFIX + urlSuffix;
                    details.setWebsite(websiteRedirectUrl);
                }
            }

        }

        return details;
    }

    private List<OpenHour> parseOpenHour(Element openHourElement) {
        Elements elements = openHourElement.select(".detail");
        if (elements.isEmpty()) {
            LOGGER.warn("detail crawler no open hour element, identifier = {}", IDENTIFIER);
            return Collections.emptyList();
        }
        List<OpenHour> openHours = Lists.newArrayListWithExpectedSize(elements.size());
        for (Element element : elements) {
            String day = StringUtils.trimToEmpty(element.select(".day").first().text());
            String range = StringUtils.trimToEmpty(element.select(".hoursRange").first().text());
            openHours.add(new OpenHour(day, range));
        }
        return openHours;
    }

    private String parseSingleContent(Element element) {
        Element contentElement = element.select(".content").first();
        if (contentElement == null) {
            return StringUtils.EMPTY;
        }
        return StringUtils.trimToEmpty(contentElement.text());
    }

    private Rating parseRating(Element doc,Element ratingElement) {
        Rating rating = new Rating();
        Element headRatingElement = doc.select(".heading_ratings div[property=aggregateRating] span[class=rate sprite-rating_rr rating_rr]").first();
        if (headRatingElement != null) {
            rating.setTotal(Utils.parseRatingValue(headRatingElement));
        }

        Element rankElement = doc.select(".slim_ranking span").first();
        if (rankElement != null) {
            String text = rankElement.text();
            String [] arr = StringUtils.split(text, "#");
            if (arr != null && arr.length > 0) {
                rating.setRank(NumberUtils.toInt(arr[0]));
            }
        }

        Elements elements = ratingElement.select("div[class=ratingRow wrap]");
        if (elements.isEmpty()) {
            LOGGER.warn("detail crawler no rating element, identifier = {}", IDENTIFIER);
            return  rating;
        }
        for (Element element : elements) {
            String text = element.text();
            if (StringUtils.startsWith(text,"Food")) {
                rating.setFood(Utils.parseRatingValue(element));
            } else if (StringUtils.startsWith(text,"Service")) {
                rating.setService(Utils.parseRatingValue(element));
            } else if (StringUtils.startsWith(text,"Value")) {
                rating.setValue(Utils.parseRatingValue(element));
            } else if (StringUtils.startsWith(text,"Atmosphere")) {
                rating.setAtmosphere(Utils.parseRatingValue(element));
            }
        }
        return rating;
    }

    public static String asdf(String str) {
        try {
            Invocable invoke = (Invocable) JsEngineFactory.get(JS_CODE_PATH);
            if (invoke == null) {
                return StringUtils.EMPTY;
            }
            return (String)invoke.invokeFunction("asdf", str);
        } catch (Exception e) {
            LOGGER.warn("detail crawler invoke js code error, str = {}", str, e);
        }
        return StringUtils.EMPTY;
    }

    private static final Set<String> HEAD_IMAGE_IDS = ImmutableSet.of("HERO_PHOTO","THUMB_PHOTO1","PHOTO_GRID1");

    private List<String> parseHeadImage(Document doc) {
        Elements scripts = doc.getElementsByTag("script");
        if (scripts.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> images = Lists.newArrayList();
        for (Element script : scripts) {
            String text = script.toString();
            if (text.contains("var lazyImgs =")) {
                String [] arr = StringUtils.split(text, ";");
                if (arr.length > 1) {
                    String json = arr[0].split("var lazyImgs =")[1];
                    json = StringUtils.replace(json, "\n", "");
                    List<LazyData> datas = JsonUtils.readValue(json, new TypeReference<List<LazyData>>() {
                    });
                    if (!CollectionUtils.isEmpty(datas)) {
                        for (LazyData data : datas) {
                            if (HEAD_IMAGE_IDS.contains(data.getId())) {
                                images.add(data.getData());
                            }
                        }
                    }
                }
            }
        }
        return images;
    }

    private static class LazyData {

        private String data;

        private String tagType;

        private String id;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTagType() {
            return tagType;
        }

        public void setTagType(String tagType) {
            this.tagType = tagType;
        }
    }

}
