package com.topodroid.num;

import java.util.ArrayList;
import java.util.HashMap;

public class Tri2StationAxle {
  String name;
  String axleName;
  HashMap< String, TriShot > shots;

  Tri2StationAxle( String n, String a, HashMap< String, TriShot > shs ) {
    name = n;
    axleName = a;
    shots = shs;
  }

  public String getName() {
    return name;
  }

  public String getAxleName() {
    return axleName;
  }

  public HashMap< String, TriShot > getShots() {
    return shots;
  }
}
