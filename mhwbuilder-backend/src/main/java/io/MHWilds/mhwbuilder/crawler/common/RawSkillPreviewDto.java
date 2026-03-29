package io.MHWilds.mhwbuilder.crawler.common;

public class RawSkillPreviewDto {
    private String name;
    private Integer sourceId;
    private String href;

    public RawSkillPreviewDto(String name, Integer sourceId, String href) {
        this.name = name;
        this.sourceId = sourceId;
        this.href = href;
    }

    public String getName() { return name; }
    public Integer getSourceId() { return sourceId; }
    public String getHref() { return href; }
}