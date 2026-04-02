package io.MHWilds.mhwbuilder.domain.recommend.model;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Armor;

public class ArmorCandidate {

    private final Armor armor;
    private final int score;

    public ArmorCandidate(Armor armor, int score) {
        this.armor = armor;
        this.score = score;
    }

    public Armor getArmor() {
        return armor;
    }

    public int getScore() {
        return score;
    }
}