package io.MHWilds.mhwbuilder.crawler.equipment;

import io.MHWilds.mhwbuilder.crawler.equipment.dto.RawArmorDto;
import io.MHWilds.mhwbuilder.crawler.equipment.dto.RawCharmDto;
import io.MHWilds.mhwbuilder.crawler.equipment.dto.RawDecorationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/admin/crawl/equipment")
@RestController
@RequiredArgsConstructor
public class EquipmentAdminController {
    private final ArmorCrawlService armorCrawlService;
    private final ArmorImportService armorImportService;
    private final CharmCrawlService charmCrawlService;
    private final CharmImportService charmImportService;
    private final DecorationCrawlService decorationCrawlService;
    private final DecorationImportService decorationImportService;

    @GetMapping("/armor/preview")
    public RawArmorDto previewArmor() throws Exception {
        return armorCrawlService.crawlArmors()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("데이터 없음"));
    }

    @GetMapping("/armor/import")
    public String importArmor() throws Exception {
        int count = armorImportService.importArmor();
        return "저장된 방어구 수: " + count;
    }
    @GetMapping("/charm/preview")
    public RawCharmDto previewCharm() throws Exception {
        return charmCrawlService.crawlCharms()
                .stream()
                .findFirst()
                .orElseThrow(() ->new IllegalStateException("데이터 없음"));
    }

    @GetMapping("/charm/import")
    public String importCharm() throws Exception{
        int count = charmImportService.importCharms();
        return "저장된 호석 수: " + count;
    }

    @GetMapping("/decoration/preview")
    public RawDecorationDto previewDeco() throws Exception{
        return decorationCrawlService.crawlDecorations()
                .stream()
                .findFirst()
                .orElseThrow(() ->new IllegalStateException("데이터 없음"));
    }

    @GetMapping("/decoration/import")
    public String importDecoration() throws Exception{
        int count = decorationImportService.importDecorations();
        return "저장된 장식주 수: " + count;

    }
}
