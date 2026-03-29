package io.MHWilds.mhwbuilder.crawler.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController //rest api 요청 처리 컨트롤러. 매서드 반환값을 뷰가 아니라 json 등으로 바로 내보냄.
public class SkillCrawlPreviewController {

    //클래스에 이 필드가 필요하다 선언
    private final SkillCrawlPreviewService service;
    //의존성 주입. 초기화. 내 필드= 스프링이 넣어준 객체
    public SkillCrawlPreviewController(SkillCrawlPreviewService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/crawl/skills/preview/test")//요청 주소
    public List<RawSkillPreviewDto> preview() throws Exception {
        String url = "https://mhf.inven.co.kr/db/mhwilds/skill";
        return service.previewSkillNames(url);
    }
}