/** @file WireFrame.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D wire segment
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

import java.util.List;
import java.util.ArrayList;

public class WireFrame
{
  private ArrayList< Cave3DStation > mPoint;
  private ArrayList< WireSegment > mSegment;
  private double mEps;

  public WireFrame( double eps )
  {
    mPoint   = new ArrayList< Cave3DStation >();
    mSegment = new ArrayList< WireSegment >();
    mEps = eps;
  }

  void addPoint( Cave3DStation station )
  {
    for ( Cave3DStation st : mPoint ) {
      if ( st.coincide( station, mEps ) ) return;
    }
    mPoint.add( station );
  }

  void addSplayPoint( Cave3DStation station )
  {
    mPoint.add( station );
  }

  void makeFrame( double max, int n )
  {
    int np = mPoint.size();
    double max1 = max + 0.01;
    Cave3DStation min[] = new Cave3DStation[n];
    double dist[] = new double[ n ];
    for ( int k1 = 0; k1 < np; ++k1 ) {
      Cave3DStation s1 = mPoint.get(k1);
      if ( s1 == null ) continue;
      for ( int k=0; k<n; ++k ) dist[k] = max1;
      for ( int k2 = 0; k2 < np; ++k2 ) {
        if ( k1 == k2 ) continue;
        Cave3DStation s2 = mPoint.get(k2);
        if ( s2 == null ) continue;
        double d = s1.distance3D( s2 );
        for ( int k = 0; k<n; ++k) {
          if ( d < dist[k] ) {
            for ( int kk = n-1; kk > k; --kk ) {
              dist[kk] = dist[kk-1];
              min[kk]  = min[kk-1];
            }
            dist[k] = d;  min[k] = s2;
          }
        }
      }
      for ( int k=0; k<n; ++k ) {
        if ( dist[k] > max ) break;
        addSegment( new WireSegment( s1, min[k] ) );
      }
    }
  }
        

  boolean addSegment( WireSegment segment )
  {
    for ( WireSegment ws : mSegment ) {
      if ( ws.coincide( segment, mEps ) ) return false;
    }
    mSegment.add( segment );
    return true;
  }

  List< WireSegment > getSegments() { return mSegment; }


}

