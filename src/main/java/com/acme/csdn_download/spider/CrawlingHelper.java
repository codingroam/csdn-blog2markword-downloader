package com.acme.csdn_download.spider;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 爬csdn博客工具
 *
 *
 * 爬所有
 *climb("qq_45774645");
 *
 * 爬单个
 * climbOne("unique_perfect","109380996");
 *
 * 根据Url爬取单个
 * climbDetailByUrl("https://blog.csdn.net/qq_44973159/article/details/121357388");
 */
@Slf4j
public class CrawlingHelper {


    public static void climb(String userName) {
        // 把下面这个base_url换成你csdn的地址
        String baseUrl = "https://blog.csdn.net/" + userName + "/";
        String secondUrl = baseUrl + "article/list/";
        // 创建文件夹
        File file = new File("./csdndownload_posts/");
        if (!file.exists()) {
            file.mkdir();
        }
        for (int i = 1; ; i++) {
            // 从第一页开始爬取
            String startUrl = secondUrl + i;
            Document doc = null;
            try {
                doc = Jsoup.connect(startUrl).get();
            } catch (IOException e) {
                log.info("jsoup获取url失败" + e.getMessage());
            }
            Element element = doc.body();
            //找到div class='article-list'
            element = element.select("div.article-list").first();
            if (element == null) {
                break;
            }
            Elements elements = element.children();
            for (Element e : elements) {
                // 拿到文章id
                String articleId = e.attr("data-articleid");
                // 爬取单篇文章
                climbDetailById(baseUrl, articleId);
            }
        }
    }

    private static void climbOne(String userName,String articleId) {
        log.info("》》》》》》》爬虫开始《《《《《《《");
        // 把下面这个base_url换成你csdn的地址
        String baseUrl = "https://blog.csdn.net/" + userName + "/";
        String secondUrl = baseUrl + "article/list/";
        // 创建文件夹
        File file = new File("./csdndownload_posts/");
        if (!file.exists()) {
            file.mkdir();
        }
        log.info(articleId);
        // 爬取单篇文章
        climbDetailById(baseUrl, articleId);
        log.info("》》》》》》》爬虫结束《《《《《《《");
    }

    public static void climbDetailByUrl(String csdnUrl) {
        File file = new File("./csdndownload_posts/");
        if (!file.exists()) {
            file.mkdir();
        }
        String startUrl = csdnUrl;
        Document doc = null;
        try {
            doc = Jsoup.connect(startUrl).get();
        } catch (IOException e) {
            log.info("jsoup获取url失败" + e.getMessage());
        }
        Element element = doc.body();
        Element htmlElement = element.select("div#content_views").first();
        Element titleElement = element.selectFirst(".title-article");
        String fileName = titleElement.text();
        log.info(fileName);
        // 设置jekyll格式博客title
        String jekyllTitle = "title= \"" + fileName + "\"\n";

        // 设置jekyll格式博客categories
        Elements elements = element.select("div.tags-box");
        String jekyllCategories = "";
        if (elements.size() > 1) {
            jekyllCategories = "categories:\n";
            jekyllCategories = getTagsBoxValue(elements, 1, jekyllCategories);
        }

        // 设置jekyll格式博客tags
        String jekyllTags = "tags =[";
        jekyllTags = getTagsBoxValue(elements, 0, jekyllTags);
        String jekyllCate = "categories =[";
        jekyllCate = getTagsBoxValue(elements, 0, jekyllCate);
        String jekyllSeries = "series =[";
        jekyllSeries = getTagsBoxValue(elements, 0, jekyllSeries);

        // 获取时间
//        Element timeElement = element.selectFirst("span.time");
//        String time = timeElement.text().substring(5);
//        log.info(time);
        String time = getNowTime();

        // 设置jekyll格式博客date
        String jekyllDate = "date= \"" + time + "\"\n";;
        String md = Html2Md.getMarkDownText(htmlElement);
        // String md = HtmlToMd.getTextContent(htmlElement); 转出来的效果不满意，弃用

        log.info(md);

        String jekylltr = "+++\n" + "author=\"wang\"\n" + jekyllTitle + jekyllDate
                +  jekyllTags + jekyllCate + jekyllSeries + "aliases = [\"migrate-from-jekyl\"]\n" + "+++\n" + fileName + "\n<!--more-->\n";
        String date = time.split(" ")[0];
        String mdFileName = "./csdndownload_posts/" + fileName + ".markdown";
        md = jekylltr + md;
        FileWriter writer;
        try {
            writer = new FileWriter(mdFileName);
            writer.write(md);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String getNowTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String format = simpleDateFormat.format(date);
        return format;
    }

    private static void climbDetailById(String baseUrl, String articleId) {
        String startUrl = baseUrl + "article/details/" + articleId;
        Document doc = null;
        try {
            doc = Jsoup.connect(startUrl).get();
        } catch (IOException e) {
            log.info("jsoup获取url失败" + e.getMessage());
        }
        Element element = doc.body();
        Element htmlElement = element.select("div#content_views").first();
        Element titleElement = element.selectFirst(".title-article");
        String fileName = titleElement.text();
        log.info(fileName);
        // 设置jekyll格式博客title
        String jekyllTitle = "title:   " + fileName + "\n";

        // 设置jekyll格式博客categories
        Elements elements = element.select("div.tags-box");
        String jekyllCategories = "";
        if (elements.size() > 1) {
            jekyllCategories = "categories:\n";
            jekyllCategories = getTagsBoxValue(elements, 1, jekyllCategories);
        }

        // 设置jekyll格式博客tags
        String jekyllTags = "tags:\n";
        jekyllTags = getTagsBoxValue(elements, 0, jekyllTags);

        // 获取时间
        Element timeElement = element.selectFirst("span.time");
        String time = timeElement.text().substring(5);
        log.info(time);

        // 设置jekyll格式博客date
        String jekyllDate = "date:   " + time + "\n";
        String md = Html2Md.getMarkDownText(htmlElement);
        // String md = HtmlToMd.getTextContent(htmlElement); 转出来的效果不满意，弃用

        log.info(md);

        String jekylltr = "---\n" + "layout:  post\n" + jekyllTitle + jekyllDate
                + "author:  'zhangtao'\nheader-img: 'img/post-bg-2015.jpg'\ncatalog:   false\n"
                + jekyllCategories + jekyllTags + "\n---\n";
        String date = time.split(" ")[0];
        String mdFileName = "./csdndownload_posts/" + date + '-' + fileName + ".markdown";
        md = jekylltr + md;
        FileWriter writer;
        try {
            writer = new FileWriter(mdFileName);
            writer.write(md);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Map<String,String> getDownloadFileMap(){
        Map<String,String> map = new LinkedHashMap<>();
        File dirFile = new File("./csdndownload_posts/");
        if(dirFile.isDirectory()){
            File[] files = dirFile.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    //long diff = getFileCreateTime(f1) - getFileCreateTime(f2);
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0)
                        return -1;
                    else if (diff == 0)
                        return 0;
                    else
                        return 1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减,如果 if 中修改为 返回1 同时此处修改为返回 -1  排序就会是递增,
                }

                private long getFileCreateTime(File file) {
                    Path path = Paths.get(file.getAbsolutePath());
                    // 根据path获取文件的基本属性类
                    BasicFileAttributes attrs = null;
                    try {
                        attrs = Files.readAttributes(path, BasicFileAttributes.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 从基本属性类中获取文件创建时间
                    FileTime fileTime = attrs.creationTime();
                    // 将文件创建时间转成毫秒
                    long millis = fileTime.toMillis();
                    return millis;

                }

                public boolean equals(Object obj) {
                    return true;
                }

            });

            for(File file : files){
                String fileName = file.getName();
                if(fileName.endsWith("markdown")){
                    map.put(fileName,file.getAbsolutePath());
                }


            }
        }
        return map;
    }

    private static String getTagsBoxValue(Elements elements, int index, String jekyllCategories) {
        Elements categories = elements.get(index).select("a.tag-link");
        for (Element e : categories) {
            String temp = e.text().replace("\t", "").replace("\n", "").replace("\r", "").replace(" ","");
            if(temp.startsWith("#")){
                continue;
            }
            jekyllCategories += "\"" + temp + "\",";
        }
        jekyllCategories = jekyllCategories.substring(0,jekyllCategories.length()-1)+"]\n";

        return jekyllCategories;
    }


}
