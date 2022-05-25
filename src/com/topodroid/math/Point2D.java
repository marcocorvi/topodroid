/* @file Point2D.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid a 2D point on the canvas
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * 2017-06-16
 * renamed from BezierPoint and inherited coords from android.graphics.PointF
 */
package com.topodroid.math;

// import android.graphics.PointF;

public class Point2D // extends PointF
{
  // from PointF
  public float x;  // X coord
  public float y;  // Y coord

  public Point2D( )
  {
    x = 0f;
    y = 0f;
  }

  public Point2D( float x0, float y0 ) 
  {
    x = x0;
    y = y0;
  }

  public Point2D( Point2D p ) // copy cstr.
  { 
    x = p.x;
    y = p.y;
  }

  public void set( Point2D p ) // copy assignment
  {
    x = p.x;
    y = p.y;
  }

  // use PointF implementation
  public void set( float x0, float y0 ) { x = x0; y = y0; }
  public void negate() { x = -x; y = -y; }

  public Point2D times( float t ) { return new Point2D( x*t, y*t ); }             // this * t

  public Point2D divideBy( float t ) { return new Point2D( x/t, y/t ); }          // this / t

  public Point2D add( Point2D c )          { return new Point2D( x + c.x, y + c.y ); } // this + c
  public Point2D add( float x0, float y0 ) { return new Point2D( x + x0, y + y0 ); } // this + (x0,y0)
  public void add2( Point2D a, Point2D b ) { x = a.x+b.x; y=a.y+b.y; }          // this = a + b

  public Point2D sub( Point2D c )          { return new Point2D( x - c.x, y - c.y ); } // this - c
  public Point2D sub( float x0, float y0 ) { return new Point2D( x - x0, y - y0 ); } // this - (x0,y0)

  public float dot( Point2D c )          { return x * c.x + y * c.y; }                   // this * c
  public float dot( float x0, float y0 ) { return x * x0 + y * y0; }                   // this * (x0,y0)

  public float cross( Point2D c )          { return x * c.y - y * c.x; }                   // this ^ c
  public float cross( float x0, float y0 ) { return x * y0 - y * x0; }                   // this ^ (x0,y0)


  // public void shiftBy( Point2D  c )                             // this += c
  // {
  //   x += c.x;
  //   y += c.y;
  // }

  // use PointF implementation (Euclidean length)
  public float length() 
  { 
    float l2 = x*x + y*y;
    return ( l2 > 0 )? (float)Math.sqrt( l2 ) : 0;
  }

  public float squareLength() { return x*x + y*y; }

  public float distance( Point2D p ) // { return distance( p.x, p.y ); }
  {
    float dx = x - p.x;
    float dy = y - p.y;
    float d = dx*dx + dy*dy;
    return ( d > 0 )? (float)Math.sqrt(d) : 0;
  }

  public float distance( float x0, float y0 )
  { 
    float dx = x - x0;
    float dy = y - y0;
    float d = dx*dx + dy*dy;
    return ( d > 0 )? (float)Math.sqrt(d) : 0;
  }

  public void normalize()
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

  /*
  // intersection of two lines in the plane (was Geometry2D.java)
  // x = p1.x + s ( p2.x - p1.x ) = q1.x + t ( q2.x - q1.x )
  // y = p1.y + s ( p2.y - p1.y ) = q1.y + t ( q2.y - q1.y )
  //
  //  a s + b t = e
  //  c s + d t = f
  // inverse:
  //   s =  d  -b  *  e  / det
  //   t   -c   a     f
  //
  // @param pt point of intersection [out]
  //
  static float intersect( PointF p1, PointF p2, PointF q1, PointF q2, PointF pt )
  {
     float a = p2.x - p1.x;
     float b = q1.x - q2.x;
     float c = p2.y - p1.y;
     float d = q1.y - q2.y;
     float e = q1.x - p1.x;
     float f = q1.y - p1.y;
     float det = a * d - b * c;
     float t = ( -c * e + a * f ) / det;
     if ( pt != null ) {
       pt.x = (1-t) * q1.x + t * q2.x;
       pt.y = (1-t) * q1.y + t * q2.y;
     }
     return t;
  }
  */
}
