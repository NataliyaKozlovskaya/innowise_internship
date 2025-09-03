package com.java.core.skynet;

/**
 * Класс симулирующий создание армии роботов
 */
public class RobotWarSimulation {
    public static void main(String[] args) throws InterruptedException {
        Factory factory = new Factory();
        WorldFaction world = new WorldFaction(factory);
        WednesdayFaction wednesday = new WednesdayFaction(factory);

        Thread factoryThread = new Thread(() -> {
            try {
                factory.produceParts();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread worldThread = new Thread(() -> {
            try {
                world.collectParts();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread wednesdayThread = new Thread(() -> {
            try {
                wednesday.collectParts();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        factoryThread.start();
        worldThread.start();
        wednesdayThread.start();

        factoryThread.join();
        worldThread.join();
        wednesdayThread.join();

        // Определяем победителя по значению strength
        // (получаем суммированием значения strength каждой части робота)
        int worldStrength = world.getTotalStrength();
        int wednesdayStrength = wednesday.getTotalStrength();

        System.out.println("\n=== РЕЗУЛЬТАТЫ ===");
        System.out.println("World: " + world.getRobotsCount() + " роботов, общая сила: " + worldStrength);
        System.out.println("Wednesday: " + wednesday.getRobotsCount() + " роботов, общая сила: " + wednesdayStrength);

        if (worldStrength > wednesdayStrength) {
            System.out.println("ПОБЕДИТЕЛЬ: World!");
        } else if (wednesdayStrength > worldStrength) {
            System.out.println("ПОБЕДИТЕЛЬ: Wednesday!");
        } else {
            System.out.println("НИЧЬЯ!");
        }
    }
}
