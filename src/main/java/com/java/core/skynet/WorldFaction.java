package com.java.core.skynet;

import java.util.Collections;
import java.util.Comparator;


/**
 * Class responsible for creating robots for the World faction
 */
class WorldFaction extends Faction {

  public WorldFaction(Factory factory) {
    super("World", factory);
  }

  @Override
  protected Robot buildRobot() {
    // We set the condition ourselves: World gives priority to the quality of heads and torsos
    Robot robot = new Robot();

    // Take the best head (quality values/indicator are assigned randomly)
    RobotPart head = Collections.max(inventory.get(PartType.HEAD),
        Comparator.comparingInt(RobotPart::getQuality));
    robot.addPart(head);
    inventory.get(PartType.HEAD).remove(head);

    // We take the best torso
    RobotPart torso = Collections.max(inventory.get(PartType.TORSO),
        Comparator.comparingInt(RobotPart::getQuality));
    robot.addPart(torso);
    inventory.get(PartType.TORSO).remove(torso);

    // We take any other parts of the robot
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