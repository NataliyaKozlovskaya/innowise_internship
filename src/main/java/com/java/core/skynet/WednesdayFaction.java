package com.java.core.skynet;

/**
 * Class responsible for creating robots for the Wednesday faction
 */
class WednesdayFaction extends Faction {

  public WednesdayFaction(Factory factory) {
    super("Wednesday", factory);
  }

  @Override
  protected Robot buildRobot() {
    // We set the condition ourselves: Wednesday gives priority to the number of robots
    Robot robot = new Robot();

    // Take the first available parts of each type
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