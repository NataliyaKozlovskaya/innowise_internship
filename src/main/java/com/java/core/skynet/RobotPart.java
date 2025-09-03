package com.java.core.skynet;

import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.Setter;

/**
 * Класс отвечающий за части из которых состоит робот и их качество
 */
@Getter
@Setter
public class RobotPart {
    private final PartType type;
    private final int quality; // Задаем значения от 1 до 100

    public RobotPart(PartType type) {
        this.type = type;
        this.quality = ThreadLocalRandom.current().nextInt(1, 101);
    }

    public PartType getType() {
        return type; }
    public int getQuality() { return quality; }

    @Override
    public String toString() {
        return type + "(" + quality + ")";
    }
}
