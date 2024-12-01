package com.topodroid.num;

import java.util.ArrayList;
import java.util.HashMap;

public class Tri2StationAxle {
  String name;
  String axleName;
  HashMap< String, TriShot > shots;
  HashMap< String, ArrayList< TriShot > > similarShots;

  Tri2StationAxle( String n, String a, HashMap< String, TriShot > shs, HashMap< String, ArrayList< TriShot > > sss ) {
    name = n;
    axleName = a;
    shots = shs;
    similarShots = sss;
  }
}
