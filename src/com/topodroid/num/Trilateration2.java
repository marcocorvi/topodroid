package com.topodroid.num;

import java.util.ArrayList;
import java.util.HashMap;

public class Trilateration2 {
  HashMap< String, TriLeg > legs;
  private ArrayList< TriPoint > points;

  Trilateration2()
  {
    legs   = new HashMap< String, TriLeg >();
    points = new ArrayList<>();
  } 

  /** 
   * @return a point
   * @param n point name
   */
  private TriPoint getPoint( String n )
  {
    for ( TriPoint p : points ) if ( n.equals(p.name) ) return p;
    return null;
  }
  
  /** 
   * Calculates the azimuths using the lengths of the triangle legs
   * 
   * @brief Trilateration by lengths
   * @param cl  cluster
   */
/*  void trilaterate( TriCluster cl )
  {
    // populate
    for ( String n : cl.stations ) {
      points.add( new TriPoint( n ) );
    }
    for ( TriShot sh : cl.shots ) {
      TriPoint p1 = getPoint( sh.from );
      TriPoint p2 = getPoint( sh.to );
      TriLeg newLeg = new TriLeg( sh, p1, p2 );
      if ( ! legs.contains(newLeg)) legs.add( newLeg );
    }
    // initialize points
    initialize();
  }*/
}
