/** @file SketchBorder.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: surface border
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130216 created
 * 20130830 getCenter
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

class SketchBorder
{
  ArrayList< SketchSide > sides;
  Vector mCenter;

  SketchBorder()
  {
    sides = new ArrayList<SketchSide>();
    mCenter = null;
  }

  void add( SketchSide s ) { sides.add( s ); }

  SketchSide get( int k ) { return sides.get(k); }

  int size() { return sides.size(); }

  /** get the "center" of the border
   * @param surface surface of this border (to get the vertices)
   */
  Vector getCenter( SketchSurface surface )
  {
    if ( mCenter == null ) {
      mCenter = new Vector();
      int n = 0;
      for ( SketchSide side : sides ) {
        SketchVertex v1 = surface.getVertex( side.v1 );
        if ( v1 != null ) {
          mCenter.add( v1 );
          ++ n;
        }
        SketchVertex v2 = surface.getVertex( side.v2 );
        if ( v2 != null ) {
          mCenter.add( v2 );
          ++ n;
        }
      }
      mCenter.times( 1.0f/(2.0f*n) );
    }
    return mCenter;
  }

  Vector getCenter() { return mCenter; }

}
