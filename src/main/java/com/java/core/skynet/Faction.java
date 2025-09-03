package com.java.core.skynet;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;

/**
 * Родительский класс, отвечающий за создание роботов фракциями Wednesday и World
 */
@Getter
@Setter
public class Faction {

    protected final String name;
    protected final Factory factory;
    protected final Map<PartType, List<RobotPart>> inventory = new ConcurrentHashMap<>();
    protected final AtomicInteger robotsBuilt = new AtomicInteger(0);

    public Faction(String name, Factory factory) {
        this.name = name;
        this.factory = factory;
        for (PartType type : PartType.values()) {
            inventory.put(type, new CopyOnWriteArrayList<>());
        }
    }

    public void collectParts() throws InterruptedException {
        while (!factory.isSimulationOver()) {
            List<RobotPart> parts = factory.takeParts(5);

            synchronized (inventory) {
                for (RobotPart part : parts) {
                    inventory.get(part.getType()).add(part);
                }
                System.out.println(name + " collected " + parts.size() + " parts");
            }

            buildRobots();
            Thread.sleep(50); // Имитация ночи
        }
    }

    protected void buildRobots() {
        synchronized (inventory) {
            int robotsBuiltThisNight = 0;

            while (canBuildRobot()) {
                Robot robot = buildRobot();
                if (robot != null) {
                    robotsBuiltThisNight++;
                    robotsBuilt.incrementAndGet();
                }
            }

            if (robotsBuiltThisNight > 0) {
                System.out.println(name + " built " + robotsBuiltThisNight + " robots. Total: "
                    + robotsBuilt.get());
            }
        }
    }

    protected boolean canBuildRobot() {
        for (PartType type : PartType.values()) {
            if (inventory.get(type).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    protected Robot buildRobot() {
        Robot robot = new Robot();
        for (PartType type : PartType.values()) {
            List<RobotPart> parts = inventory.get(type);
            if (!parts.isEmpty()) {
                RobotPart bestPart = Collections.max(parts,
                    Comparator.comparingInt(RobotPart::getQuality));
                robot.addPart(bestPart);
                parts.remove(bestPart);
            }
        }
        return robot.getStrength() > 0 ? robot : null;
    }

    public int getTotalStrength() {
        synchronized (inventory) {
            int strength = 0;
            // Сила собранных роботов + сила оставшихся частей
            for (List<RobotPart> parts : inventory.values()) {
                for (RobotPart part : parts) {
                    strength += part.getQuality();
                }
            }
            return strength
                + robotsBuilt.get() * 100; // Задаем свое условие: каждый робот дает 100 силы/strength
        }
    }

    public int getRobotsCount() {
        return robotsBuilt.get();
    }

    public String getName() {
        return name;
    }
}
