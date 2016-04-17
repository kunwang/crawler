package com.abs.crawler.tripadvisor;

/**
 * @author hao.wang
 * @since 2016/4/15 00:04
 */
public interface Constants {

    interface CollectionNames {
        String DETAIL_PAGES = "tripadvisor_detail_page";
        String IMAGES = "tripadvisor_image";
        String REVIEWS = "tripadvisor_review";
    }

    interface WebsiteParamKeys {
        String DETAIL_PAGE_ID = "ID";
        String REDIRECT_URL = "REDIRECT_URL";
        String COOKIES = "COOKIES";
    }

    interface DetailParamKeys {
        String URL = "DETAIL_URL";
        String COOKIES = "COOKIES";
        String ID = "ID";
        String GEO_ID = "GEO_ID";
    }

    interface ListParamKeys {
        String INDEX = "INDEX";
        String COOKIES = "COOKIES";
    }

    interface ImageParamKeys {
        String RESTAURANT_ID = "RESTAURANT_ID";
        String OFFSET = "OFFSET";
        String COOKIES = "COOKIES";
        String COUNT = "COUNT";
    }


    interface ReviewParamKeys {
        String RESTAURANT_ID = "RESTAURANT_ID";
        String GEO_ID = "GEO_ID";
        String OFFSET = "OFFSET";
        String RESTAURANT_NAME = "RESTAURANT_NAME";
        String REVIEW_COUNT = "REVIEW_COUNT";
        String COOKIES = "COOKIES";
    }

    interface ExpandReviewParamKeys {
        String RESTAURANT_ID = "RESTAURANT_ID";
        String GEO_ID = "GEO_ID";
        String OFFSET = "OFFSET";
        String REVIEW_COUNT = "REVIEW_COUNT";
        String RESTAURANT_NAME = "RESTAURANT_NAME";
        String COOKIES = "COOKIES";
        String UID = "UID";
        String REVIEW_IDS = "REVIEW_IDS";
    }

}
