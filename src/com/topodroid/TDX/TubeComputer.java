/** @file TubeComputer.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief Cave3D simple Tube computer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

import java.util.ArrayList;

class TubeComputer implements WallComputer
{
  private TglParser mParser;
  private ArrayList< Cave3DShot > shots;
  private ArrayList< Triangle3D > triangles = null;

  /** cstr
   * @param parser    model parser
   * @param s         list of model shots
   */
  TubeComputer( TglParser parser, ArrayList< Cave3DShot > s )
  {
    mParser = parser;
    shots = s;
  }

  /** @return the list of truiangles
   */
  public ArrayList< Triangle3D > getTriangles() { return triangles; }

  // colors:             red         yellow      green       cyan        blue        violet
  final int[] colors = { 0xffff0000, 0xffffff00, 0xff00ff00, 0xff00ffff, 0xff0000ff, 0xffff00ff };

  /** compute the walls
   */
  public boolean computeWalls()
  {
    if ( mParser.getXSectionNumber() == 0 ) return false;
    ArrayList< Cave3DTube > tubes = new ArrayList<>();
    int kcol = 0;
    for ( Cave3DShot sh : shots ) {
      Cave3DStation sf = sh.from_station;
      Cave3DStation st = sh.to_station;
      if ( sf != null && st != null ) {
        Cave3DXSection xsf = mParser.getXSectionAt( sf );
        Cave3DXSection xst = mParser.getXSectionAt( st );
        if ( xsf != null && xst != null ) {
          Cave3DTube tube = new Cave3DTube( sh, xsf, xst );
          // tube.color = colors[ kcol ];
          kcol = ( kcol + 1) % 6;
          tubes.add( tube );
          // tube.dumpTube();
        }
      }
    }

    triangles = new ArrayList< Triangle3D >();
    for ( Cave3DTube h : tubes ) {
      for ( Triangle3D t : h.triangles ) triangles.add( t );
    }
    return ( triangles.size() > 0 );
  }

}
