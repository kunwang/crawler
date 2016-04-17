package com.abs.crawler.commons.retry;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import com.abs.crawler.akka.SpringProps;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.repository.DelayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.DelayQueue;

/**
 * @author hao.wang
 * @since 2016/4/14 22:36
 */
@Service
public class Retryer implements IRetryer, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Retryer.class);

    private static final DelayQueue<DelayItem> DELAY_ITEMS = new DelayQueue<DelayItem>();

    private static volatile boolean run = true;

    @Resource
    private DelayRepository delayRepository;

    @Resource
    private ActorSystem actorSystem;


    @PostConstruct
    public void init() {
        List<DelayItem> delays = delayRepository.query();
        if (!CollectionUtils.isEmpty(delays)) {
            for (DelayItem delay : delays) {
                DELAY_ITEMS.offer(delay);
            }
        }
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }


    @Override
    public void retry(CrawlerMessage message) {
        if (message.getRetry() <= 0) {
            LOGGER.warn("resend crawler no times, message = {}", message);
            return;
        }
        DelayItem delay = new DelayItem(message);
        delayRepository.saveOrUpdate(delay);
        DELAY_ITEMS.offer(delay);
    }

    @Override
    public void run() {
        while(this.run) {
            try {
                DelayItem delayItem = DELAY_ITEMS.take();
                ActorRef crawlerActor = actorSystem.actorOf(SpringProps.create(actorSystem, (Class<? extends UntypedActor>) Class.forName(delayItem.getMessage().getActorClassName())));
                delayRepository.remove(delayItem.getId());
                crawlerActor.tell(delayItem.getMessage(), null);
//                LOGGER.info("resend crawler actor , delay = {}", delayItem);
            } catch (Exception e) {
                LOGGER.warn("resend crawler actor error ", e);
            } finally {
                // do something
            }
        }
    }



}
