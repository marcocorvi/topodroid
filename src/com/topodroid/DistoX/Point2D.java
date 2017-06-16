/* @file Point2D.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid a 2D point on the canvas
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.PointF;

class Point2D extends PointF
{
  // from PointF
  // public float x;  // X coord
  // public float y;  // Y coord

  Point2D( )
  {
    x = 0f;
    y = 0f;
  }

  Point2D( float x0, float y0 ) 
  {
    x = x0;
    y = y0;
  }

  Point2D( Point2D p ) // copy cstr.
  { 
    x = p.x;
    y = p.y;
  }

  void set( Point2D p ) // copy assignment
  {
    x = p.x;
    y = p.y;
  }

  // use PointF implementation
  // void set( float x0, float y0 ) { x = x0; y = y0; }
  // void negate() { x = -x; y = -y; }; 

  Point2D times( float t ) { return new Point2D( x*t, y*t ); }             // this * t

  Point2D divideBy( float t ) { return new Point2D( x/t, y/t ); }          // this / t

  Point2D add( Point2D c ) { return new Point2D( x + c.x, y + c.y ); } // this + c
  Point2D add( float x0, float y0 ) { return new Point2D( x + x0, y + y0 ); } // this + (x0,y0)
  void add2( Point2D a, Point2D b ) { x = a.x+b.x; y=a.y+b.y; }          // this = a + b

  Point2D sub( Point2D c ) { return new Point2D( x - c.x, y - c.y ); } // this - c
  Point2D sub( float x0, float y0 ) { return new Point2D( x - x0, y - y0 ); } // this - (x0,y0)

  float dot( Point2D c )       { return x * c.x + y * c.y; }                   // this * c
  float dot( float x0, float y0 )       { return x * x0 + y * y0; }                   // this * (x0,y0)

  float cross( Point2D c )     { return x * c.y - y * c.x; }                   // this ^ c
  float cross( float x0, float y0 )     { return x * y0 - y * x0; }                   // this ^ (x0,y0)


  void shiftBy( Point2D  c )                             // this += c
  {
    x += c.x;
    y += c.y;
  }

  // use PointF implemenetation (Euclidean length)
  // float length() 
  // { 
  //   float l2 = x*x + y*y;
  //   return ( l2 > 0 )? (float)Math.sqrt( l2 ) : 0;
  // }

  float squareLength() { return x*x + y*y; }

  float distance( Point2D p ) // { return distance( p.x, p.y ); }
  {
    float dx = x - p.x;
    float dy = y - p.y;
    float d = dx*dx + dy*dy;
    return ( d > 0 )? (float)Math.sqrt(d) : 0;
  }

  float distance( float x, float y )
  { 
    float dx = x - x;
    float dy = y - y;
    float d = dx*dx + dy*dy;
    return ( d > 0 )? (float)Math.sqrt(d) : 0;
  }

  void normalize()
  {
    float d = length();
    if ( d > 0.0001f ) {
      x /= d;
      y /= d;
    } else {
      x = 0.0f;
      y = 0.0f;
    }
  }
}
