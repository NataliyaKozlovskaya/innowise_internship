package com.java.core.skynet;

/**
 * Class simulating the creation of an army of robots
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

    // Determine the winner by the strength value
    // (obtained by summing the strength value of each part of the robot)
    int worldStrength = world.getTotalStrength();
    int wednesdayStrength = wednesday.getTotalStrength();

    System.out.println("\n=== RESULTS ===");
    System.out.println(
        "World: " + world.getRobotsCount() + " robots, total force: " + worldStrength);
    System.out.println(
        "Wednesday: " + wednesday.getRobotsCount() + " robots, total force: " + wednesdayStrength);

    if (worldStrength > wednesdayStrength) {
      System.out.println("WINNER: World!");
    } else if (wednesdayStrength > worldStrength) {
      System.out.println("WINNER: Wednesday!");
    } else {
      System.out.println("DRAW!");
    }
  }
}
