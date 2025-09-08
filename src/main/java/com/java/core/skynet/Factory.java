package com.java.core.skynet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class responsible for creating/producing and putting robot parts into operation
 */
public class Factory {

  private final BlockingQueue<RobotPart> storage = new LinkedBlockingQueue<>();
  private final AtomicInteger daysPassed = new AtomicInteger(0);
  private static final int MAX_DAYS = 100;
  private final Object lock = new Object(); // Separate object for synchronization

  public void produceParts() throws InterruptedException {
    while (daysPassed.get() < MAX_DAYS) {
      // We produce up to 10 parts per day
      int partsToProduce = ThreadLocalRandom.current().nextInt(1, 11);

      for (int i = 0; i < partsToProduce; i++) {
        PartType randomType = PartType.values()[
            ThreadLocalRandom.current().nextInt(PartType.values().length)
            ];
        storage.put(new RobotPart(randomType));
      }
      System.out.println(
          "Factory produced " + partsToProduce + " parts. Total: " + storage.size());

      synchronized (lock) {
        lock.wait(); // Waiting for notification from takeParts
      }

      daysPassed.incrementAndGet();
      Thread.sleep(100); // Imitation of the day
    }
  }

  public List<RobotPart> takeParts(int maxParts) throws InterruptedException {
    List<RobotPart> takenParts = new ArrayList<>();
    int partsToTake = Math.min(maxParts, storage.size());

    for (int i = 0; i < partsToTake; i++) {
      takenParts.add(storage.take());// BlockingQueue manages locks itself
    }

    // We notify the factory that the parts have been taken away
    synchronized (lock) {
      lock.notifyAll();
    }

    return takenParts;
  }

  public boolean isSimulationOver() {
    return daysPassed.get() >= MAX_DAYS;
  }
}
