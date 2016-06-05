package com.abs.crawler.renren.actor;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import com.abs.crawler.akka.Actor;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.commons.message.Params;
import com.abs.crawler.commons.retry.Retryer;
import com.abs.crawler.commons.utils.JsonUtils;
import com.abs.crawler.renren.Constants;
import com.abs.crawler.renren.domain.Universities;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hao.wang
 * @since 2016/5/13 00:56
 */
@Actor
public class UniversityReader extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniversityReader.class);

    private static final String RENREN_UNIVERSITIES = UniversityReader.class.getClassLoader().getResource("renrenUniversities").getPath();

    @Resource
    private ActorSystem actorSystem;

    @Resource
    private Retryer retryer;

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof CrawlerMessage) {
            List<Universities> universitieses = this.read(RENREN_UNIVERSITIES);
            if (CollectionUtils.isEmpty(universitieses)) {
                return;
            }
            for (Universities universities : universitieses) {
                if (!CollectionUtils.isEmpty(universities.getUnivs())) {
                    for (Universities.University university : universities.getUnivs()) {
                        this.send(university.getId(), university.getName(), universities.getName(), StringUtils.EMPTY);
                    }
                }
                if (!CollectionUtils.isEmpty(universities.getProvs())) {
                    for (Universities.Province province : universities.getProvs()) {
                        if (!CollectionUtils.isEmpty(province.getUnivs())) {
                            for (Universities.University university : province.getUnivs()) {
                                this.send(university.getId(), university.getName(), universities.getName(), province.getName());
                            }
                        }
                    }
                }
            }
        } else {
            unhandled(o);
        }
    }

    private static AtomicInteger COUNT = new AtomicInteger(0);

    private void send(int universityId, String university, String country, String province) {
        int count = COUNT.incrementAndGet();
        Params departmentParams = new Params();
        departmentParams.param(Constants.DepartmentParamKeys.COUNTRY, country);
        departmentParams.param(Constants.DepartmentParamKeys.NAME, university);
        departmentParams.param(Constants.DepartmentParamKeys.UNIVERSITY_ID, universityId);
        departmentParams.param(Constants.DepartmentParamKeys.PROVINCE, province);
        CrawlerMessage departmentMessage = new CrawlerMessage(departmentParams, DepartmentCrawler.class.getName(), (count / 50));
        retryer.retry(departmentMessage);
        LOGGER.info("university count = {}", COUNT.get());
    }

    private List<Universities> read(String file) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ( (line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return JsonUtils.readValue(sb.toString(), new TypeReference<List<Universities>>() {});
    }

}
