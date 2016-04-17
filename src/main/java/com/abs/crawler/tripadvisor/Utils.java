package com.abs.crawler.tripadvisor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Element;

/**
 * @author hao.wang
 * @since 2016/4/17 14:26
 */
public class Utils {


    public static float parseRatingValue(Element element) {
        Element imageElement = element.select("img").first();
        if (imageElement == null) {
            return 0;
        }
        String temp = imageElement.attr("alt");
        String [] arr = StringUtils.split(temp, " ");
        if (arr.length < 4) {
            return 0;
        }
        return NumberUtils.toFloat(arr[0], 0);
    }

}
