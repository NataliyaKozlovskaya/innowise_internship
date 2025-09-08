package com.java.core.skynet;

import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.Setter;

/**
 * Class responsible for the parts that make up the robot and their quality
 */
@Getter
@Setter
public class RobotPart {

  private final PartType type;
  private final int quality; // Set values from 1 to 100

  public RobotPart(PartType type) {
    this.type = type;
    this.quality = ThreadLocalRandom.current().nextInt(1, 101);
  }

  public PartType getType() {
    return type;
  }

  public int getQuality() {
    return quality;
  }

  @Override
  public String toString() {
    return type + "(" + quality + ")";
  }
}
