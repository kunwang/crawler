package com.abs.crawler.web;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.abs.crawler.akka.SpringProps;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.tripadvisor.Constants;
import com.abs.crawler.tripadvisor.actor.ListCrawler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * @author hao.wang
 * @since 2016/4/15 00:23
 */
@Controller
public class IndexController {

    @Resource
    private ActorSystem actorSystem;

    @RequestMapping("start")
    public ModelAndView start() {
        ActorRef listActor = actorSystem.actorOf(SpringProps.create(actorSystem, ListCrawler.class));
        listActor.tell(new CrawlerMessage(new Params().param(Constants.ListParamKeys.INDEX, 0), ListCrawler.class.getName()), null);
        return new ModelAndView();
    }

}
