package com.java.core.skynet;

import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Class of the robot being created
 */
@Getter
@Setter
public class Robot {

  private final Map<PartType, RobotPart> parts = new EnumMap<>(PartType.class);
  private int strength = 0;// Strength is made up of the quality value

  public void addPart(RobotPart part) {
    parts.put(part.getType(), part);
    strength += part.getQuality();
  }

  public int getStrength() {
    return strength;
  }

  public boolean isComplete() {
    return parts.size() == PartType.values().length;
  }
}
