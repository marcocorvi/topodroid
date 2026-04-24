/** @file Region.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief region in the 2D plane bounded by a polyline
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.util;

import java.util.ArrayList;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import android.graphics.PointF;
import android.content.Context;

public class Region
{
  ArrayList< PointF > mPts; // border
  float xmin, xmax; // bounding box
  float ymin, ymax;
  String name = null; // region name (optional)

  static private ArrayList< Region > mRegion = new ArrayList<>();

  /** create the regions from a resource
   * @param ctx  context
   * @param res  resource
   */
  public static void create( Context ctx, int res )
  {
    String[] names = ctx.getResources().getStringArray( res );
    for ( String name : names ) {
      createRegion( ctx, "region/" + name );
    }
  }

  /** @return the name of the region the point is inside
   * @param point  point
   */
  static public String get( String point )
  {
    if ( mRegion == null || mRegion.size() == 0 ) return null;
    String[] vals = point.split( " " );
    try {
      float x = Float.parseFloat( vals[0] );
      float y = Float.parseFloat( vals[1] );
      for ( Region region : mRegion ) {
        if ( region.isInside( x, y ) ) return region.name;
      }
    } catch ( NumberFormatException e ) {
      TDLog.e( e.getMessage() );
    }
    return null;
  }

  /** factory
   * @param ctx  context
   * @param name region asset filename (eg, "ai/c1.txt");
   */
  private static void createRegion( Context ctx, String name )
  {
    Region ret = new Region();
    try {
      InputStream is = ctx.getAssets().open( name );
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String line;
      ret.name = br.readLine().trim(); // first line is the region name
      while ( ( line = br.readLine() ) != null ) {
        line = line.trim();
        String[] vals = line.split(" ");
        try {
          float y = Float.parseFloat( vals[0] ); // lat
          float x = Float.parseFloat( vals[1] ); // lon
          ret.insertPoint( x, y );
        } catch ( NumberFormatException e1 ) { }
      }
    } catch ( IOException e ) {
      // TDLog.e("Error reading region " + name );
    }
    if ( ret.size() >= 3 ) {
      TDLog.v("Region " + name + " size " + ret.size() );
      mRegion.add( ret );
    } else {
      TDLog.v("Region " + name + " failed " );
    }
  }

  /** cstr
   */
  private Region( )
  {
    mPts = new ArrayList< PointF >();
  }

  private boolean isInside( float x, float y )
  {
    if ( x < xmin || x > xmax ) return false;
    if ( y < ymin || y > ymax ) return false;
    int npts = mPts.size();
    float area = 0;
    PointF p = mPts.get(npts-1);
    PointF p1 = new PointF( p.x - x, p.y - y );
    for ( int k = 0; k<npts; ++k ) {
      p = mPts.get(k);
      PointF p2 = new PointF( p.x - x, p.y - y );
      area += p1.x * p2.y - p1.y * p2.x;
      p1 = p2;
    }
    return area > 1.0f;
  }

  public int size() { return mPts.size(); }


  private void insertPoint( float x, float y )
  {
    if ( mPts.isEmpty() ) {
      xmin = xmax = x;
      ymin = ymax = y;
    } else {
      if ( x < xmin ) { xmin = x; } else if ( x > xmax ) { xmax = x; }
      if ( y < ymin ) { ymin = y; } else if ( y > ymax ) { ymax = y; }
    }
    mPts.add( new PointF( x, y ) );
  }
}    
  
