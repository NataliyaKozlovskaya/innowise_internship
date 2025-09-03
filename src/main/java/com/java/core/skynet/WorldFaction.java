package com.java.core.skynet;

import java.util.Collections;
import java.util.Comparator;


/**
 * Класс, отвечающий за создание роботов фракцией World
 */
class WorldFaction extends Faction {
    public WorldFaction(Factory factory) {
        super("World", factory);
    }

    @Override
    protected Robot buildRobot() {
        // Сами задаем условие: World отдает приоритет качеству голов и торсов
        Robot robot = new Robot();

        // Берем лучшую голову (значения/показатель quality присваивается рандомно)
        RobotPart head = Collections.max(inventory.get(PartType.HEAD),
            Comparator.comparingInt(RobotPart::getQuality));
        robot.addPart(head);
        inventory.get(PartType.HEAD).remove(head);

        // Берем лучший торс
        RobotPart torso = Collections.max(inventory.get(PartType.TORSO),
            Comparator.comparingInt(RobotPart::getQuality));
        robot.addPart(torso);
        inventory.get(PartType.TORSO).remove(torso);

        // Остальные части робота берем любые
        for (PartType type : new PartType[]{PartType.HAND, PartType.FEET}) {
            if (!inventory.get(type).isEmpty()) {
                RobotPart part = inventory.get(type).get(0);
                robot.addPart(part);
                inventory.get(type).remove(0);
            }
        }

        return robot.getStrength() > 0 ? robot : null;
    }
}