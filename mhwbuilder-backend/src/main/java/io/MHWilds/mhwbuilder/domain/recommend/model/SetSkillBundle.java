package io.MHWilds.mhwbuilder.domain.recommend.model;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Armor;
import io.MHWilds.mhwbuilder.util.entityenums.EquipCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SetSkillBundle {

    private final List<Armor> armors = new ArrayList<>();
    private final Set<EquipCategory> usedParts = new HashSet<>();
    private final Set<String> armorIds = new HashSet<>();
    private final Map<String, Integer> satisfiedSetSkillCounts = new HashMap<>();

    public void addArmor(Armor armor) {
        armors.add(armor);
        usedParts.add(armor.getCategory());
        armorIds.add(armor.getId());
    }

    public boolean containsPart(EquipCategory category) {
        return usedParts.contains(category);
    }

    public boolean containsArmor(String armorId) {
        return armorIds.contains(armorId);
    }

    public void addSatisfiedSetSkill(String setSkillId, int requiredCount) {
        satisfiedSetSkillCounts.put(setSkillId, requiredCount);
    }

    public List<Armor> getArmors() {
        return armors;
    }

    public Set<EquipCategory> getUsedParts() {
        return usedParts;
    }

    public Set<String> getArmorIds() {
        return armorIds;
    }

    public Map<String, Integer> getSatisfiedSetSkillCounts() {
        return satisfiedSetSkillCounts;
    }

    public SetSkillBundle copy() {
        SetSkillBundle copied = new SetSkillBundle();
        copied.armors.addAll(this.armors);
        copied.usedParts.addAll(this.usedParts);
        copied.armorIds.addAll(this.armorIds);
        copied.satisfiedSetSkillCounts.putAll(this.satisfiedSetSkillCounts);
        return copied;
    }
}