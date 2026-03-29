package io.MHWilds.mhwbuilder.crawler.common.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class JsoupTestService {

    public String fetchTitle() throws Exception {
        // 1. 테스트용 URL
        String url = "https://www.google.com";

        // 2. HTML 가져오기
        Document doc = Jsoup.connect(url).get();
        /*url에 http 요청 보내고 응답으로 html 전체 문서를 받아다 documents 객체화*/

        // 3. <title> 태그 내용 반환
        return doc.title();
    }
}