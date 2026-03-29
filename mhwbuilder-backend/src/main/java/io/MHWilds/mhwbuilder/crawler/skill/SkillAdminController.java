package io.MHWilds.mhwbuilder.crawler.skill;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("api/admin/crawl/skills")
@RestController
public class SkillAdminController {
    /* 1. SkillCrawlService.previewSkills(url)
            -> List<RawSkillDto> 반환
       2. SkillImportService.importSkills(url)
            -> SkillCrawlService가 RawSkillDto 목록 생성
            -> SkillImportService가 Skill 엔티티로 변환 후 저장*/

    private final SkillCrawlService skillCrawlService;
    private final SkillImportService skillImportService;

    public SkillAdminController(SkillCrawlService skillCrawlService, SkillImportService skillImportService) {
        this.skillCrawlService = skillCrawlService;
        this.skillImportService = skillImportService;
    }


    @GetMapping("/preview")
    public List<RawSkillDto> previewSkills() throws Exception{
        return skillCrawlService.crawlSkills();
    }

    @GetMapping("/import")
    public String importSkills() throws Exception {
        int count = skillImportService.importSkills();
        return "저장된 스킬 수: " + count;
    }

}
