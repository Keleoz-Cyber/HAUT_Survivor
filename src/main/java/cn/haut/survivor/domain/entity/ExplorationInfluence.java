package cn.haut.survivor.domain.entity;

public record ExplorationInfluence(
        String sourceType,
        String sourceName,
        String description,
        AttributeChange attributeChange,
        int exploreBonus
) {
    public boolean hasEffect() {
        return exploreBonus != 0 || (attributeChange != null && attributeChange.hasAnyChange());
    }
}
