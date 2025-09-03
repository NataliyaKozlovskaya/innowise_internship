package com.java.core.skynet;

import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Класс создаваемого робота
 */
@Getter
@Setter
public class Robot {
    private final Map<PartType, RobotPart> parts = new EnumMap<>(PartType.class);
    private int strength = 0;// Сила, складывается из значения quality

    public void addPart(RobotPart part) {
        parts.put(part.getType(), part);
        strength += part.getQuality();
    }

    public int getStrength() {
        return strength;
    }

    public boolean isComplete() {
        return parts.size() == PartType.values().length;
    }
}
