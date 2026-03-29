package io.MHWilds.mhwbuilder.crawler.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class EscapedJsonReader {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void readCode(String url, String mainName) throws IOException {


        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get();

        System.out.println("문서 title: " + doc.title());

        Element main = doc.selectFirst(mainName);
        if (main == null) {
            throw new IllegalStateException("main# 요소를 찾지 못했습니다.");
        }

        String rawData = main.attr("data-base-info");
        System.out.println("rawData 길이: " + rawData.length());

        if (rawData.isBlank()) {
            throw new IllegalStateException("data-base-info 값이 비어 있습니다.");
        }

        String decodedJson = StringEscapeUtils.unescapeHtml4(rawData);

        JsonNode root = objectMapper.readTree(decodedJson);

        System.out.println(root.toString());
    }

    public static void main(String[] args) {
        try {
            String url = "https://mhf.inven.co.kr/db/mhwilds/accessory/";
            String mainSelector = "main#mhwiDbAccessory";

            EscapedJsonReader escapedCode = new EscapedJsonReader();
            escapedCode.readCode(url, mainSelector);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}