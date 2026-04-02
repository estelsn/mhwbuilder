package io.MHWilds.mhwbuilder.domain.recommend.model;

public class SkillGap {

    private final String skillId;
    private final String skillName;
    private final int targetLevel;
    private final int currentLevel;
    private final int remainingLevel;

    public SkillGap(String skillId, String skillName, int targetLevel, int currentLevel, int remainingLevel) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.targetLevel = targetLevel;
        this.currentLevel = currentLevel;
        this.remainingLevel = remainingLevel;
    }

    public String getSkillId() {
        return skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public int getTargetLevel() {
        return targetLevel;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getRemainingLevel() {
        return remainingLevel;
    }

    public boolean isSatisfied() {
        return remainingLevel <= 0;
    }
}