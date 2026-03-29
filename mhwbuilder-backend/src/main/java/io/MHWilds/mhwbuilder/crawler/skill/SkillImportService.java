package io.MHWilds.mhwbuilder.crawler.skill;

import io.MHWilds.mhwbuilder.crawler.skill.dto.RawSeriesSkillDto;
import io.MHWilds.mhwbuilder.crawler.skill.dto.RawSkillDto;
import io.MHWilds.mhwbuilder.domain.skill.entity.SeriesSkill;
import io.MHWilds.mhwbuilder.domain.skill.entity.SeriesSkillEffect;
import io.MHWilds.mhwbuilder.domain.skill.entity.Skill;
import io.MHWilds.mhwbuilder.domain.skill.repository.SeriesSkillEffectRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SeriesSkillRepository;
import io.MHWilds.mhwbuilder.domain.skill.repository.SkillRepository;
import io.MHWilds.mhwbuilder.util.entityenums.SkillCategory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillImportService {

    private final SkillCrawlService skillCrawlService;
    private final SkillRepository skillRepository;
    private final SeriesSkillRepository seriesSkillRepository;
    private final SeriesSkillEffectRepository seriesSkillEffectRepository;
// 1: 공격 2: 거너 3: 방어 4: 스태미나 5: 채집 6 : 특수 7 : 시리즈 8 : 그룹
  /*
{"code":171,"name":"물 내성","category":3,"category_text":"방어","slotsize":0,"max_level":3,"description":"플레이어의 물 내성을 올린다. 레벨이 오르면 방어력에도 영향을 준다.","levels":["물 내성+6","물 내성+12","물 내성+20 방어력+10"],"seriesskill1":null,"seriesskill2":null}
  * */
    @Transactional
    public int importSkills() throws Exception {
        List<RawSkillDto> rawSkills = skillCrawlService.crawlSkills();
        System.out.println("크롤링 개수: " + rawSkills.size());
        int count = 0;
        int normalCount1 = 0;
        int seriesCount1 = 0;


        for (RawSkillDto rawSkill : rawSkills) {
            SkillCategory category = SkillCategory.fromCode(rawSkill.getCategory());

            if (category.isNormalSkill()) {
                saveNormalSkill(rawSkill);
                count++;
                normalCount1++;
            } else if (category.isSeries() || category.isGroup()) {
                saveSeriesSkill(rawSkill, category);
                count++;
                seriesCount1++;
            }
        }
        System.out.println("일반분기" + normalCount1);
        System.out.println("시리즈분기"+seriesCount1);
        return count;
    }

    private void saveNormalSkill(RawSkillDto rawSkill) {
        if (skillRepository.existsByCode(rawSkill.getCode())) {
            return;
        }
        /*{"code":131,"name":"KO술","category":1,"category_text":"공격",
        "slotsize":0,"max_level":3,"description":"몬스터를 기절 상태로 만들 확률이 높아진다.",
        "levels":["기절 위력 1.2배","기절 위력 1.3배","기절 위력 1.4배"],"seriesskill1":null,"seriesskill2":null}
        */
        Skill skill = new Skill();
        skill.setCode(rawSkill.getCode());
        skill.setCategory(SkillCategory.fromCode(rawSkill.getCategory()));
        skill.setName(rawSkill.getName());
        skill.setDescription(rawSkill.getDescription());
        skill.setLevels(rawSkill.getLevels());
        skill.setMaxLevel(rawSkill.getMaxLevel());
        skill.setVersion("v1");
        System.out.println("일반저장");
        skillRepository.save(skill);
    }

    private void saveSeriesSkill(RawSkillDto rawSkill, SkillCategory category) {
        if(seriesSkillRepository.existsByCode(rawSkill.getCode())){
            return;
        }

        SeriesSkill seriesSkill = new SeriesSkill();
        seriesSkill.setCode(rawSkill.getCode());
        seriesSkill.setCategory(SkillCategory.fromCode(rawSkill.getCategory()));
        seriesSkill.setName(rawSkill.getName());
        seriesSkill.setVersion("v1");
        seriesSkillRepository.save(seriesSkill);
        System.out.println("시리즈저장");
        saveSeriesSkillEffect(seriesSkill, rawSkill);
    }
    private void saveSeriesSkillEffect(SeriesSkill seriesSkill, RawSkillDto rawSkill){
        saveSingleSeriesSkillEffect(seriesSkill, rawSkill.getSeriesSkill1());
        saveSingleSeriesSkillEffect(seriesSkill, rawSkill.getSeriesSkill2());
    }

    private void saveSingleSeriesSkillEffect(SeriesSkill seriesSkill, RawSeriesSkillDto rawSeriesSkill){
        if(rawSeriesSkill == null){
            return;
        }

        if (seriesSkillEffectRepository
                .existsBySeriesSkillAndRequiredCount(seriesSkill, rawSeriesSkill.getCount())) {
            return;
        }

        SeriesSkillEffect seriesSkillEffect = new SeriesSkillEffect();
        seriesSkillEffect.setSeriesSkill(seriesSkill);
        seriesSkillEffect.setEffectTitle(rawSeriesSkill.getName());
        seriesSkillEffect.setRequiredCount(rawSeriesSkill.getCount());
        seriesSkillEffect.setDescription(rawSeriesSkill.getDescription());

        seriesSkillEffectRepository.save(seriesSkillEffect);
    }


}