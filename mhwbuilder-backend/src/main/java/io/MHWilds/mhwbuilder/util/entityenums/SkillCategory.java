package io.MHWilds.mhwbuilder.util.entityenums;

public enum SkillCategory {
    ATTACK(1, "공격"),
    GUNNER(2, "거너"),
    DEFENSE(3, "방어"),
    STAMINA(4, "스태미나"),
    GATHERING(5, "채집"),
    SPECIAL(6, "특수"),
    SERIES(7, "시리즈"),
    GROUP(8, "그룹");

    private final int code;
    private final String label;

    SkillCategory(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static SkillCategory fromCode(int code) {
        for (SkillCategory category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        throw new IllegalArgumentException("알 수 없는 스킬 카테고리 코드: " + code);
    }

    public boolean isSeries() {
        return this == SERIES;
    }

    public boolean isGroup() {
        return this == GROUP;
    }

    public boolean isNormalSkill() {
        return this != SERIES && this != GROUP;
    }
}

