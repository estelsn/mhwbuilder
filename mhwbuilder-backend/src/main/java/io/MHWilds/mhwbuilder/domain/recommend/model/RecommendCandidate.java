package io.MHWilds.mhwbuilder.domain.recommend.model;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Charm;

public class RecommendCandidate {

    private final SetSkillBundle armorBundle;
    private final Charm charm;

    public RecommendCandidate(SetSkillBundle armorBundle, Charm charm) {
        this.armorBundle = armorBundle;
        this.charm = charm;
    }

    public SetSkillBundle getArmorBundle() {
        return armorBundle;
    }

    public Charm getCharm() {
        return charm;
    }
}