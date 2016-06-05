package com.abs.crawler.web;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.abs.crawler.akka.SpringProps;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.utils.JsonUtils;
import com.abs.crawler.tripadvisor.Constants;
import com.abs.crawler.tripadvisor.actor.ListCrawler;
import com.abs.crawler.tripadvisor.domain.DetailPage;
import com.abs.crawler.tripadvisor.domain.Geo;
import com.abs.crawler.tripadvisor.domain.Image;
import com.abs.crawler.tripadvisor.domain.LocationAndContactInfo;
import com.abs.crawler.tripadvisor.domain.OpenHour;
import com.abs.crawler.tripadvisor.domain.Rating;
import com.abs.crawler.tripadvisor.domain.RestaurantDetails;
import com.abs.crawler.tripadvisor.domain.Review;
import com.abs.crawler.tripadvisor.repository.DetailPageRepository;
import com.abs.crawler.tripadvisor.repository.ImageRepository;
import com.abs.crawler.tripadvisor.repository.ReviewRepository;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author hao.wang
 * @since 2016/4/15 00:23
 */
@Controller
public class IndexController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @Resource
    private ActorSystem actorSystem;

    @Resource
    private DetailPageRepository detailPageRepository;

    @Resource
    private ImageRepository imageRepository;

    @Resource
    private ReviewRepository reviewRepository;

    @RequestMapping("start")
    public ModelAndView start() {
        ActorRef listActor = actorSystem.actorOf(SpringProps.create(actorSystem, ListCrawler.class));
        listActor.tell(new CrawlerMessage(new Params().param(Constants.ListParamKeys.INDEX, 126), ListCrawler.class.getName()), null);
        return new ModelAndView();
    }

    @RequestMapping("exportImage")
    public ModelAndView exportImage() throws IOException {
        List<Image> images = imageRepository.query();
        FileWriter writer = new FileWriter("/Users/wanghao/Desktop/tripadvisor_images");
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        int lineCount = 0;
        for(Image image : images) {
            String result = JsonUtils.writeValueAsString(image);
            bufferedWriter.write(result);
            bufferedWriter.newLine();
            lineCount ++;
            LOGGER.info("write image line = {}", lineCount);
        }
        bufferedWriter.close();
        return new ModelAndView();
    }

    @RequestMapping("exportDetail")
    public ModelAndView exportDetail() throws IOException {
        List<DetailPage> detailPages = detailPageRepository.query();
        FileWriter writer = new FileWriter("/Users/wanghao/Desktop/tripadvisor_restaurant_detail");
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        ExportRestaurant restaurant;
        RestaurantDetails details;
        LocationAndContactInfo location;

        int lineCount = 0;
        for (DetailPage page : detailPages) {
            restaurant = new ExportRestaurant();
            restaurant.setId(page.getId());
            restaurant.setName(page.getName());
            restaurant.setGeoId(page.getGeoId());
            restaurant.setHeadImages(page.getHeadImages());
            restaurant.setGeo(page.getGeo());
            if (page.getDetails() != null) {
                details = page.getDetails();
                restaurant.setCuisine(details.getCuisine());
                restaurant.setOpenHours(details.getOpenHours());
                restaurant.setDiningOptions(details.getDiningOptions());
                restaurant.setGoodFor(details.getGoodFor());
                restaurant.setAveragePrices(details.getAveragePrices());
                restaurant.setRating(details.getRating());
                restaurant.setReviewCount(details.getReviewCount());
                restaurant.setWebsite(details.getWebsite());
            }

            if (page.getLocationAndContactInfo() != null) {
                location = page.getLocationAndContactInfo();
                restaurant.setStreetAddress(location.getStreetAddress());
                restaurant.setLocality(location.getLocality());
                restaurant.setCountryName(location.getCountryName());
                restaurant.setPhoneNumber(location.getPhoneNumber());
//                restaurant.setNeighbourhood(location.getNeighbourhood());
                if (!CollectionUtils.isEmpty(location.getLocation())) {
                    List<String> locations = Lists.newArrayListWithExpectedSize(location.getLocation().size());
                    for (String str : location.getLocation()) {
                        str = StringUtils.remove(str,(char)160);
                        locations.add(StringUtils.trim(str));
                    }
                    restaurant.setLocation(locations);
                    if (locations.size() == 4) {
                        restaurant.setNeighbourhood(locations.get(3));
                    }
                }

            }
            long count = imageRepository.count(new Query(Criteria.where("restaurantId").is(page.getId())));
            restaurant.setImageCount(new Long(count).intValue());
            String result = JsonUtils.writeValueAsString(restaurant);
            bufferedWriter.write(result);
            bufferedWriter.newLine();
            lineCount ++;
            LOGGER.info("write restaurant line = {}", lineCount);
        }

        bufferedWriter.close();
        return new ModelAndView();
    }

    @RequestMapping("export")
    public ModelAndView export() throws IOException {
        List<DetailPage> detailPages = detailPageRepository.query();

        ExportRestaurant restaurant;
        RestaurantDetails details;
        LocationAndContactInfo location;

        int lineCount = 0;
        for (DetailPage page : detailPages) {
            restaurant = new ExportRestaurant();
            restaurant.setId(page.getId());
            restaurant.setName(page.getName());
            restaurant.setGeoId(page.getGeoId());
            restaurant.setHeadImages(page.getHeadImages());
            restaurant.setGeo(page.getGeo());
            if (page.getDetails() != null) {
                details = page.getDetails();
                restaurant.setCuisine(details.getCuisine());
                restaurant.setOpenHours(details.getOpenHours());
                restaurant.setDiningOptions(details.getDiningOptions());
                restaurant.setGoodFor(details.getGoodFor());
                restaurant.setAveragePrices(details.getAveragePrices());
                restaurant.setRating(details.getRating());
                restaurant.setReviewCount(details.getReviewCount());
                restaurant.setWebsite(details.getWebsite());
            }

            if (page.getLocationAndContactInfo() != null) {
                location = page.getLocationAndContactInfo();
                restaurant.setStreetAddress(location.getStreetAddress());
                restaurant.setLocality(location.getLocality());
                restaurant.setCountryName(location.getCountryName());
                restaurant.setPhoneNumber(location.getPhoneNumber());
                restaurant.setNeighbourhood(location.getNeighbourhood());
                if (!CollectionUtils.isEmpty(location.getLocation())) {
                    List<String> locations = Lists.newArrayListWithExpectedSize(location.getLocation().size());
                    for (String str : location.getLocation()) {
                        str = StringUtils.remove(str,(char)160);
                        locations.add(StringUtils.trim(str));
                    }
                    restaurant.setLocation(locations);
                }
            }
            String folderName = this.mkdir(restaurant.getId());


            List<Review> reviews = reviewRepository.query(new Query(Criteria.where("restaurantId").is(restaurant.getId())));
            if (!CollectionUtils.isEmpty(reviews)) {
                String result = JsonUtils.writeValueAsString(reviews);
                FileWriter rWriter = new FileWriter(folderName + "reviews");
                BufferedWriter rBufferedWriter = new BufferedWriter(rWriter);
                rBufferedWriter.write(result);
                rBufferedWriter.close();
            }

            List<Image> images = imageRepository.query(new Query(Criteria.where("restaurantId").is(restaurant.getId())));
            if (!CollectionUtils.isEmpty(images)) {
                restaurant.setImageCount(images.size());

                String result = JsonUtils.writeValueAsString(images);
                FileWriter rWriter = new FileWriter(folderName + "images");
                BufferedWriter rBufferedWriter = new BufferedWriter(rWriter);
                rBufferedWriter.write(result);
                rBufferedWriter.close();
            }

            String result = JsonUtils.writeValueAsString(restaurant);
            FileWriter rWriter = new FileWriter(folderName + "restaurant");
            BufferedWriter rBufferedWriter = new BufferedWriter(rWriter);
            rBufferedWriter.write(result);
            rBufferedWriter.close();
            rBufferedWriter.close();

            lineCount ++;
            LOGGER.info("write restaurant line = {}", lineCount);
        }


        return new ModelAndView();
    }

    private static final String PATH = "/Users/wanghao/Desktop/tripadvisor_restaurants/";

    private String mkdir(String id) {
        String folderName = PATH + id;
        File folder = new File(folderName);
        folderName = folderName + "/";
        if (folder.exists() && folder.isDirectory()) {
            return folderName;
        }
        folder.mkdir();
        return folderName;
    }

    private static class ExportRestaurant {
        private String id;
        private String name;
        private String geoId;
        private List<String> headImages;
        private String streetAddress;
        private String locality;
        private String countryName;
        private String phoneNumber;
        private String neighbourhood;
        private List<String> location;
        private String cuisine;
        private List<OpenHour> openHours;
        private String diningOptions;
        private String goodFor;
        private String averagePrices;
        private Rating rating;
        private int reviewCount;
        private int imageCount;
        private String website;
        private Geo geo;

        public int getImageCount() {
            return imageCount;
        }

        public void setImageCount(int imageCount) {
            this.imageCount = imageCount;
        }

        public String getAveragePrices() {
            return averagePrices;
        }

        public void setAveragePrices(String averagePrices) {
            this.averagePrices = averagePrices;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        public String getCuisine() {
            return cuisine;
        }

        public void setCuisine(String cuisine) {
            this.cuisine = cuisine;
        }

        public String getDiningOptions() {
            return diningOptions;
        }

        public void setDiningOptions(String diningOptions) {
            this.diningOptions = diningOptions;
        }

        public Geo getGeo() {
            return geo;
        }

        public void setGeo(Geo geo) {
            this.geo = geo;
        }

        public String getGeoId() {
            return geoId;
        }

        public void setGeoId(String geoId) {
            this.geoId = geoId;
        }

        public String getGoodFor() {
            return goodFor;
        }

        public void setGoodFor(String goodFor) {
            this.goodFor = goodFor;
        }

        public List<String> getHeadImages() {
            return headImages;
        }

        public void setHeadImages(List<String> headImages) {
            this.headImages = headImages;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLocality() {
            return locality;
        }

        public void setLocality(String locality) {
            this.locality = locality;
        }

        public List<String> getLocation() {
            return location;
        }

        public void setLocation(List<String> location) {
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNeighbourhood() {
            return neighbourhood;
        }

        public void setNeighbourhood(String neighbourhood) {
            this.neighbourhood = neighbourhood;
        }

        public List<OpenHour> getOpenHours() {
            return openHours;
        }

        public void setOpenHours(List<OpenHour> openHours) {
            this.openHours = openHours;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public Rating getRating() {
            return rating;
        }

        public void setRating(Rating rating) {
            this.rating = rating;
        }

        public int getReviewCount() {
            return reviewCount;
        }

        public void setReviewCount(int reviewCount) {
            this.reviewCount = reviewCount;
        }

        public String getStreetAddress() {
            return streetAddress;
        }

        public void setStreetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }
    }

}
