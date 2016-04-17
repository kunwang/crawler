package com.abs.crawler.tripadvisor.actor;

import akka.actor.ActorSystem;
import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.crawler.AbstractCrawler;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.retry.Retryer;
import com.abs.crawler.commons.utils.HttpUtils;
import com.abs.crawler.tripadvisor.Constants;
import com.abs.crawler.tripadvisor.domain.Image;
import com.abs.crawler.tripadvisor.repository.ImageRepository;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
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
 * @since 2016/4/16 22:49
 */
@Actor
public class ImageCrawler extends AbstractCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCrawler.class);

    private static final String IDENTIFIER = "tripadvisor_image";

    private static final String URL = "https://www.tripadvisor.com.sg/MetaPlacementAjax";

    private static final String PARAM_TEMPLATE = "placementName=media_albums" +
            "&servletClass=com.TripResearch.servlet.LocationPhotoAlbum&servletName=LocationPhotoAlbum" +
            "&geo=294265&albumViewMode=heroThumbs&albumid=101&thumbnailMinWidth=50&cnt=30&filter=7" +
            "&heroMinWidth=1236&heroMinHeight=196&albumPartialsToUpdate=partial&detail=%s&offset=%s";


    @Resource
    private ImageRepository imageRepository;

    @Resource
    private ActorSystem actorSystem;

    @Resource
    private Retryer retryer;

    @Override
    public Request buildRequest(Params params) {
        String restaurantId = (String)params.get(Constants.ImageParamKeys.RESTAURANT_ID);
        int offset = (int) params.get(Constants.ImageParamKeys.OFFSET);
        List<Cookie> cookies = (List<Cookie>) params.get(Constants.ImageParamKeys.COOKIES);
        String httpParams = String.format(PARAM_TEMPLATE, restaurantId, offset);
        String url = URL + "?" + httpParams;
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
            LOGGER.warn("image crawler parse error, identifier = {}, message = {}", IDENTIFIER, message);
            return;
        }
        Elements imageElements = doc.select("div[id^=thumb-]");
        if (imageElements.isEmpty()) {
            LOGGER.warn("image crawler image elements empty, identifier = {}, message = {}", IDENTIFIER, message);
            return;
        }
        String restaurantId = (String)message.getParams().get(Constants.ImageParamKeys.RESTAURANT_ID);

        int imageCount = 0;
        for (Element element : imageElements) {
            String imageId = element.attr("data-mediaid");
            String url = element.attr("data-bigUrl");
            String captionText = element.attr("data-captiontext");
            String author = element.attr("data-authorname");
            String displayDate = element.attr("data-displaydate");

            Image image = new Image();
            image.setRestaurantId(restaurantId);
            image.setId(imageId);
            image.setUrl(url);
            image.setCaptionText(captionText);
            image.setAuthor(author);
            image.setDisplayDate(displayDate);
            imageRepository.saveOrUpdate(image);
            imageCount ++;
        }

        int offset = (int) message.getParams().get(Constants.ImageParamKeys.OFFSET);
        int count = (int) message.getParams().get(Constants.ImageParamKeys.COUNT);

        LOGGER.info("image crawler restaurantId = {}, offset = {}, imageCount = {}, totalCount = {}", restaurantId, offset, imageCount, count );
        offset = offset + 30;
        if (offset < count) {
            Params imageParams = new Params();
            imageParams.param(Constants.ImageParamKeys.RESTAURANT_ID, restaurantId);
            imageParams.param(Constants.ImageParamKeys.OFFSET, offset);
            imageParams.param(Constants.ImageParamKeys.COUNT, count);
            imageParams.param(Constants.ImageParamKeys.COOKIES, response.getCookies());
            CrawlerMessage imageMessage = new CrawlerMessage(imageParams, ImageCrawler.class.getName(), 3);
            retryer.retry(imageMessage);
//            ActorRef imageCrawler = actorSystem.actorOf(SpringProps.create(actorSystem, ImageCrawler.class));
//            imageCrawler.tell(imageMessage, null);
        }

    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
