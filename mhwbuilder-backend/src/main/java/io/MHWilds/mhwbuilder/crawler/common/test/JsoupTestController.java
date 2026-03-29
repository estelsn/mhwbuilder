package io.MHWilds.mhwbuilder.crawler.common.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsoupTestController {

    private final JsoupTestService jsoupTestService;

    public JsoupTestController(JsoupTestService jsoupTestService) {
        this.jsoupTestService = jsoupTestService;
    }

    @GetMapping("/api/admin/jsoup/test")
    public String testJsoup() throws Exception {
        return jsoupTestService.fetchTitle();
    }
}