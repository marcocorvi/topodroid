/** @file ConvexHullComputer.java
 *
 *e @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D  convex hull model computer
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.cw;

import com.topodroid.DistoX.TglParser;
import com.topodroid.DistoX.GlModel;
import com.topodroid.DistoX.Cave3DShot;
import com.topodroid.DistoX.Cave3DStation;

import com.topodroid.utils.TDLog;

import java.util.List;
import java.util.ArrayList;

public class ConvexHullComputer
{
  TglParser mParser;
  List<Cave3DShot> mShots;
  ArrayList<CWConvexHull> mWalls;
  ArrayList<CWBorder> mBorders;

  public ConvexHullComputer( TglParser parser, List<Cave3DShot> shots )
  {
    mParser  = parser;
    mShots   = shots;
    mWalls   = new ArrayList< CWConvexHull >();
    mBorders = new ArrayList< CWBorder >();
  }

  // boolean hasWalls() { return mWalls != null; }
  public ArrayList<CWConvexHull> getWalls()   { return mWalls; }
  public ArrayList<CWBorder>     getBorders() { return mBorders; }
  public int getWallsSize()   { return mWalls.size(); }
  public int getBordersSize() { return mBorders.size(); }

  public boolean computeConvexHull( )
  {
    for ( Cave3DShot sh : mShots ) {
      Cave3DStation sf = sh.from_station;
      Cave3DStation st = sh.to_station;
      if ( sf != null && st != null ) {
        ArrayList< Cave3DShot > legs1   = mParser.getLegsAt( sf, st );
        ArrayList< Cave3DShot > legs2   = mParser.getLegsAt( st, sf );
        ArrayList< Cave3DShot > splays1 = mParser.getSplayAt( sf, false );
        ArrayList< Cave3DShot > splays2 = mParser.getSplayAt( st, false );
        // TDLog.v( "Hull splays at " + sf.name + " " + splays1.size() + " at " + st.name + " " + splays2.size() );
        // if ( splays1.size() > 0 && splays2.size() > 0 ) 
        {
          try {
            CWConvexHull cw = new CWConvexHull( );
            cw.create( legs1, legs2, splays1, splays2, sf, st, GlModel.mAllSplay );
            // TODO make convex-concave hull
            mWalls.add( cw );
          } catch ( RuntimeException e ) { 
            TDLog.Error( "Hull create runtime exception [2] " + e.getMessage() );
            return false;
          }
        }
      }
    }
    // TDLog.v( "Hull done. split triangles " + GlModel.mSplitTriangles );

    // for ( CWConvexHull cv : mWalls ) cv.randomizePoints( 0.1f );
    if ( GlModel.mSplitTriangles ) {
      // synchronized( paths_borders ) 
      {
        // TDLog.v( "Hull borders. nr walls " + mWalls.size() );
        for ( int k1 = 0; k1 < mWalls.size(); ++ k1 ) {
          CWConvexHull cv1 = mWalls.get( k1 );
          for ( int k2 = k1+1; k2 < mWalls.size(); ++ k2 ) {
            CWConvexHull cv2 = mWalls.get( k2 );
            if ( cv1.mFrom == cv2.mFrom || cv1.mFrom == cv2.mTo || cv1.mTo == cv2.mFrom || cv1.mTo == cv2.mTo ) {
              CWBorder cwb = new CWBorder( cv1, cv2, 0.00001f );
              if ( cwb.makeBorder( ) ) {
                mBorders.add( cwb );
                cwb.splitCWTriangles();
              } 
            }
          }
        }
        // TDLog.v( "Hull borders done, nr borders " + mBorders.size() );
      }
    }
    return true;
  }


  public double getVolume()
  {
    double vol = 0;
    for ( CWConvexHull cw : mWalls ) {
      vol += cw.getVolume();
    }
    for ( CWBorder cb : mBorders ) {
      vol -= cb.getVolume();
    }
    return vol / 6;
  }

}
