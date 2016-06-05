package com.abs.crawler.web;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import au.com.bytecode.opencsv.CSVWriter;
import com.abs.crawler.akka.SpringProps;
import com.abs.crawler.commons.message.CrawlerMessage;
import com.abs.crawler.renren.actor.ProvinceCityReader;
import com.abs.crawler.renren.actor.UniversityReader;
import com.abs.crawler.renren.domain.College;
import com.abs.crawler.renren.domain.ElementarySchool;
import com.abs.crawler.renren.domain.HighSchool;
import com.abs.crawler.renren.domain.JuniorSchool;
import com.abs.crawler.renren.domain.University;
import com.abs.crawler.renren.repository.CollegeRepository;
import com.abs.crawler.renren.repository.ElementarySchoolRepository;
import com.abs.crawler.renren.repository.HighSchoolRepository;
import com.abs.crawler.renren.repository.JuniorSchoolRepository;
import com.abs.crawler.renren.repository.UniversityRepository;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hao.wang
 * @since 2016/5/13 09:43
 */
@Controller
@RequestMapping("renren")
public class RenrenController {

    @Resource
    private ActorSystem actorSystem;

    @Resource
    private UniversityRepository universityRepository;

    @Resource
    private HighSchoolRepository highSchoolRepository;

    @Resource
    private JuniorSchoolRepository juniorSchoolRepository;

    @Resource
    private CollegeRepository collegeRepository;

    @Resource
    private ElementarySchoolRepository elementarySchoolRepository;

    @RequestMapping("start")
    public ModelAndView start() {
        ActorRef universityActor = actorSystem.actorOf(SpringProps.create(actorSystem, UniversityReader.class));
        universityActor.tell(new CrawlerMessage(), null);
        return new ModelAndView();
    }

    @RequestMapping("hcje")
    public ModelAndView hcje() {
        ActorRef provinceCityActor = actorSystem.actorOf(SpringProps.create(actorSystem, ProvinceCityReader.class));
        provinceCityActor.tell(new CrawlerMessage(), null);
        return new ModelAndView();
    }

    @RequestMapping("export/university")
    public ModelAndView exportUniversity() throws IOException {
        List<University> universities = universityRepository.query();
        String fileName = "/Users/wanghao/Desktop/renrenUniversity.csv";
        storeFile(fileName, universities);
        return new ModelAndView();
    }

    @RequestMapping("export/hcje")
    public ModelAndView exportHCJE() throws IOException {
        List<HighSchool> highSchools = highSchoolRepository.query();
        String highSchoolFile = "/Users/wanghao/Desktop/renrenHighSchool.csv";
        storeH(highSchoolFile, highSchools);

        List<College> colleges = collegeRepository.query();
        String collegeFile = "/Users/wanghao/Desktop/renrenCollegeSchool.csv";
        storeC(collegeFile, colleges);

        List<JuniorSchool> juniorSchools = juniorSchoolRepository.query();
        String juniorFile = "/Users/wanghao/Desktop/renrenJuniorSchool.csv";
        storeJ(juniorFile, juniorSchools);

        List<ElementarySchool> elementarySchools = elementarySchoolRepository.query();
        String elementaryFile = "/Users/wanghao/Desktop/renrenElementarySchool.csv";
        storeE(elementaryFile, elementarySchools);

        return new ModelAndView();
    }

    private static final String[] H_HEADER = {"省会", "城市", "区", "学校类型", "学校名称"};

    private void storeH(String fileName, List<HighSchool> highSchools) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName), Charset.forName("UTF-8"));
        CSVWriter writer = new CSVWriter(out);
        writer.writeNext(H_HEADER);
        List<List<HighSchool>> hss = Lists.partition(highSchools,5000);
        for (List<HighSchool> hs : hss) {
            List<String[]> dataList = getHDataList(hs);
            writer.writeAll(dataList);
        }
        writer.close();
    }

    private List<String[]> getHDataList(List<HighSchool> hs) throws UnsupportedEncodingException {
        List<String[]> dataList = new ArrayList<String[]>();
        for (HighSchool h : hs) {
            String[] data = new String[] {
                    escapeCsvSC(h.getProvince()),
                    escapeCsvSC(h.getCity()),
                    escapeCsvSC(h.getArea()),
                    "高中",
                    escapeCsvSC(h.getName()),
            };
            dataList.add(data);
        }
        return dataList;
    }

    private static final String[] C_HEADER = {"省会", "城市", "区", "学校类型", "学校名称"};

    private void storeC(String fileName, List<College> colleges) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName), Charset.forName("UTF-8"));
        CSVWriter writer = new CSVWriter(out);
        writer.writeNext(C_HEADER);
        List<List<College>> hss = Lists.partition(colleges,5000);
        for (List<College> hs : hss) {
            List<String[]> dataList = getCDataList(hs);
            writer.writeAll(dataList);
        }
        writer.close();
    }

    private List<String[]> getCDataList(List<College> hs) throws UnsupportedEncodingException {
        List<String[]> dataList = new ArrayList<String[]>();
        for (College h : hs) {
            String[] data = new String[] {
                    escapeCsvSC(h.getProvince()),
                    escapeCsvSC(h.getCity()),
                    escapeCsvSC(h.getArea()),
                    "中专",
                    escapeCsvSC(h.getName()),
            };
            dataList.add(data);
        }
        return dataList;
    }

    private static final String[] J_HEADER = {"省会", "城市", "区", "学校类型", "学校名称"};

    private void storeJ(String fileName, List<JuniorSchool> juniorSchools) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName), Charset.forName("UTF-8"));
        CSVWriter writer = new CSVWriter(out);
        writer.writeNext(J_HEADER);
        List<List<JuniorSchool>> hss = Lists.partition(juniorSchools,5000);
        for (List<JuniorSchool> hs : hss) {
            List<String[]> dataList = getJDataList(hs);
            writer.writeAll(dataList);
        }
        writer.close();
    }

    private List<String[]> getJDataList(List<JuniorSchool> hs) throws UnsupportedEncodingException {
        List<String[]> dataList = new ArrayList<String[]>();
        for (JuniorSchool h : hs) {
            String[] data = new String[] {
                    escapeCsvSC(h.getProvince()),
                    escapeCsvSC(h.getCity()),
                    escapeCsvSC(h.getArea()),
                    "初中",
                    escapeCsvSC(h.getName()),
            };
            dataList.add(data);
        }
        return dataList;
    }

    private static final String[] E_HEADER = {"省会", "城市", "学校类型", "学校名称"};

    private void storeE(String fileName, List<ElementarySchool> elementarySchools) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName), Charset.forName("UTF-8"));
        CSVWriter writer = new CSVWriter(out);
        writer.writeNext(E_HEADER);
        List<List<ElementarySchool>> hss = Lists.partition(elementarySchools,5000);
        for (List<ElementarySchool> hs : hss) {
            List<String[]> dataList = getEDataList(hs);
            writer.writeAll(dataList);
        }
        writer.close();
    }

    private List<String[]> getEDataList(List<ElementarySchool> hs) throws UnsupportedEncodingException {
        List<String[]> dataList = new ArrayList<String[]>();
        for (ElementarySchool h : hs) {
            String[] data = new String[] {
                    escapeCsvSC(h.getProvince()),
                    escapeCsvSC(h.getCity()),
                    "小学",
                    escapeCsvSC(h.getName()),
            };
            dataList.add(data);
        }
        return dataList;
    }



    private void storeFile(String fileName, List<University> universities) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName), Charset.forName("UTF-8"));
        CSVWriter writer = new CSVWriter(out);
        writer.writeNext(HEADER);
        List<List<University>> univsList = Lists.partition(universities,5000);
        for (List<University> univs : univsList) {
            List<String[]> dataList = getDataList(univs);
            writer.writeAll(dataList);
        }
        writer.close();
    }

    private static final String[] HEADER = {"国家", "省/市", "学校类型", "学校名称", "学院"};

    private static final Joiner DEPARTMENT_JOINER = Joiner.on("、");


    private List<String[]> getDataList(List<University> universities) throws UnsupportedEncodingException {
        List<String[]> dataList = new ArrayList<String[]>();
        for (University university : universities) {
            String[] data = new String[] {
                    escapeCsvSC(university.getCountry()),
                    escapeCsvSC(university.getProvince()),
                    "大学",
                    escapeCsvSC(university.getName()),
                    escapeCsvSC(DEPARTMENT_JOINER.join(university.getDepartments()))
            };
            dataList.add(data);
        }
        return dataList;
    }

    private String escapeCsvSC(String content) throws UnsupportedEncodingException {
        if(StringUtils.isEmpty(content)) {
            return StringUtils.EMPTY;
        }
        String tmpContent = StringUtils.replace(content,"\"","“");
        tmpContent = StringUtils.replace(tmpContent,",","，");
        tmpContent = StringUtils.replace(tmpContent,"\n"," ");
        tmpContent = StringUtils.replace(tmpContent,"\t"," ");
        return tmpContent;
    }

}
