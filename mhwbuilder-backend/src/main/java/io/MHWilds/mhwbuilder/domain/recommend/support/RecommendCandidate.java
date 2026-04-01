package io.MHWilds.mhwbuilder.domain.recommend.support;

import io.MHWilds.mhwbuilder.domain.equipment.entity.Armor;
import io.MHWilds.mhwbuilder.domain.equipment.entity.Charm;

public record RecommendCandidate(
        Armor head,
        Armor body,
        Armor arm,
        Armor waist,
        Armor leg,
        Charm charm
) {
}