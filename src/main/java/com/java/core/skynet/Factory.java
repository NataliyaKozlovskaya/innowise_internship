package com.java.core.skynet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс отвечающий за создание/производство и взатие частей робота в работу
 */
public class Factory {
    private final BlockingQueue<RobotPart> storage = new LinkedBlockingQueue<>();
    private final AtomicInteger daysPassed = new AtomicInteger(0);
    private static final int MAX_DAYS = 100;

    public void produceParts() throws InterruptedException {
        while (daysPassed.get() < MAX_DAYS) {
            // Производим до 10 частей в день
            int partsToProduce = ThreadLocalRandom.current().nextInt(1, 11);

            synchronized (storage) {
                for (int i = 0; i < partsToProduce; i++) {
                    PartType randomType = PartType.values()[
                        ThreadLocalRandom.current().nextInt(PartType.values().length)
                        ];
                    storage.put(new RobotPart(randomType));
                }
                System.out.println("Factory produced " + partsToProduce + " parts. Total: " + storage.size());

                // Ожидаем ночи (когда фабрики заберут части)
                storage.wait();
            }

            daysPassed.incrementAndGet();
            Thread.sleep(100); // Имитация дня
        }
    }

    public List<RobotPart> takeParts(int maxParts) throws InterruptedException {
        synchronized (storage) {
            List<RobotPart> takenParts = new ArrayList<>();
            int partsToTake = Math.min(maxParts, storage.size());

            for (int i = 0; i < partsToTake; i++) {
                takenParts.add(storage.take());
            }

            storage.notifyAll(); // Уведомляем фабрику, что части забрали
            return takenParts;
        }
    }

    public boolean isSimulationOver() {
        return daysPassed.get() >= MAX_DAYS;
    }
}
