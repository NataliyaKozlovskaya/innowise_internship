package com.java.core.skynet;

/**
 * Класс, отвечающий за создание роботов фракцией Wednesday
 */
class WednesdayFaction extends Faction {
    public WednesdayFaction(Factory factory) {
        super("Wednesday", factory);
    }

    @Override
    protected Robot buildRobot() {
        // Сами задаем условие: Wednesday отдает приоритет количеству роботов
        Robot robot = new Robot();

        // Берем первые доступные части каждого типа
        for (PartType type : PartType.values()) {
            if (!inventory.get(type).isEmpty()) {
                RobotPart part = inventory.get(type).get(0);
                robot.addPart(part);
                inventory.get(type).remove(0);
            }
        }

        return robot.getStrength() > 0 ? robot : null;
    }
}