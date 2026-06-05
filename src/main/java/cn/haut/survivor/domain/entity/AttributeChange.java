package cn.haut.survivor.domain.entity;

/**
 * 一次行动导致的属性变化快照。
 * 正值表示增加，负值表示减少，0 不需要在页面上显示。
 */
public record AttributeChange(
        int academicChange,
        int healthChange,
        int moneyChange,
        int socialChange,
        int skillChange,
        int pressureChange,
        int disciplineChange,
        int expChange
) {
    /** 所有变化都为 0 的空对象 */
    public static final AttributeChange EMPTY = new AttributeChange(0, 0, 0, 0, 0, 0, 0, 0);

    /** 是否存在任何非零变化 */
    public boolean hasAnyChange() {
        return academicChange != 0 || healthChange != 0 || moneyChange != 0
                || socialChange != 0 || skillChange != 0 || pressureChange != 0
                || disciplineChange != 0 || expChange != 0;
    }
}
