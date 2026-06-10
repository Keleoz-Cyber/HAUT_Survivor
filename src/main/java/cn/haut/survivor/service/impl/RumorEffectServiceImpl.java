package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.AttributeChange;
import cn.haut.survivor.domain.entity.ExplorationInfluence;
import cn.haut.survivor.domain.entity.Rumor;
import cn.haut.survivor.service.RumorEffectService;
import cn.haut.survivor.service.RumorService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RumorEffectServiceImpl implements RumorEffectService {

    private final RumorService rumorService;

    public RumorEffectServiceImpl(RumorService rumorService) {
        this.rumorService = rumorService;
    }

    @Override
    public List<ExplorationInfluence> getExplorationInfluences(Long userId, int weekNumber, Long locationId) {
        List<Rumor> visibleRumors = rumorService.pickVisibleRumorsForUser(userId, weekNumber);
        List<ExplorationInfluence> influences = new ArrayList<>();
        for (Rumor rumor : visibleRumors) {
            if (rumor.getLocationId() != null && !rumor.getLocationId().equals(locationId)) {
                continue;
            }
            ExplorationInfluence influence = toInfluence(rumor);
            if (influence.hasEffect()) {
                influences.add(influence);
            }
        }
        return influences;
    }

    private ExplorationInfluence toInfluence(Rumor rumor) {
        String type = rumor.getEffectType();
        int amount = rumor.getEffectValue() != null ? rumor.getEffectValue() : 0;
        String target = rumor.getEffectTarget();
        String description = "传闻生效：" + rumor.getRumorTitle() + "，" + rumor.getEffectHint();

        if ("explore_bonus".equals(type)) {
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description, AttributeChange.EMPTY, amount);
        }
        if ("safe_zone".equals(type) && "pressure".equals(target)) {
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description,
                    new AttributeChange(0, 0, 0, 0, 0, -amount, 0, 0), 0);
        }
        if ("attr_bonus".equals(type)) {
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description, attrChange(target, amount), 0);
        }
        if ("npc_boost".equals(type)) {
            // npc_boost gives a social attribute bonus so it appears in the influence panel
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description,
                    new AttributeChange(0, 0, 0, 1, 0, 0, 0, 0), 0);
        }
        if ("event_hint".equals(type)) {
            return new ExplorationInfluence("rumor", rumor.getRumorTitle(), description, attrChange(target, amount), 0);
        }
        return new ExplorationInfluence("rumor", rumor.getRumorTitle(), "", AttributeChange.EMPTY, 0);
    }

    @Override
    public int getNpcBoostForLocation(Long userId, int weekNumber, Long locationId) {
        List<Rumor> visibleRumors = rumorService.pickVisibleRumorsForUser(userId, weekNumber);
        int total = 0;
        for (Rumor rumor : visibleRumors) {
            if (rumor.getLocationId() != null && !rumor.getLocationId().equals(locationId)) {
                continue;
            }
            if ("npc_boost".equals(rumor.getEffectType())) {
                total += rumor.getEffectValue() != null ? rumor.getEffectValue() : 0;
            }
        }
        return total;
    }

    @Override
    public String getEventHintTarget(Long userId, int weekNumber, Long locationId) {
        List<Rumor> visibleRumors = rumorService.pickVisibleRumorsForUser(userId, weekNumber);
        for (Rumor rumor : visibleRumors) {
            if (rumor.getLocationId() != null && !rumor.getLocationId().equals(locationId)) {
                continue;
            }
            if ("event_hint".equals(rumor.getEffectType())) {
                return rumor.getEffectTarget();
            }
        }
        return null;
    }

    @Override
    public String getEventHintPreferredEventType(Long userId, int weekNumber, Long locationId) {
        return preferredEventTypeForTarget(getEventHintTarget(userId, weekNumber, locationId));
    }

    private AttributeChange attrChange(String target, int amount) {
        return switch (target != null ? target : "") {
            case "academic" -> new AttributeChange(amount, 0, 0, 0, 0, 0, 0, 0);
            case "health" -> new AttributeChange(0, amount, 0, 0, 0, 0, 0, 0);
            case "money" -> new AttributeChange(0, 0, amount, 0, 0, 0, 0, 0);
            case "social" -> new AttributeChange(0, 0, 0, amount, 0, 0, 0, 0);
            case "skill" -> new AttributeChange(0, 0, 0, 0, amount, 0, 0, 0);
            case "pressure" -> new AttributeChange(0, 0, 0, 0, 0, amount, 0, 0);
            case "discipline" -> new AttributeChange(0, 0, 0, 0, 0, 0, amount, 0);
            default -> AttributeChange.EMPTY;
        };
    }

    private String preferredEventTypeForTarget(String target) {
        return switch (target != null ? target : "") {
            case "academic" -> "academic_crisis";
            case "social" -> "社交";
            case "skill" -> "技能";
            case "health" -> "健康";
            case "money" -> "金钱";
            case "pressure" -> "学习";
            default -> null;
        };
    }
}
