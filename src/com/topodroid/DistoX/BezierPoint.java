/* @file BezierPoint.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid a 2D point on the canvas
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.util.FloatMath;

class BezierPoint
{
  public float mX;  // X coord
  public float mY;  // Y coord

  BezierPoint( )
  {
    mX = 0f;
    mY = 0f;
  }

  BezierPoint( float x0, float y0 ) 
  {
    mX = x0;
    mY = y0;
  }

  BezierPoint( BezierPoint p ) // copy cstr.
  { 
    mX = p.mX;
    mY = p.mY;
  }

  void set( BezierPoint p ) // copy assignment
  {
    mX = p.mX;
    mY = p.mY;
  }

  void negate() { mX = -mX; mY = -mY; }; 

  BezierPoint times( float t ) { return new BezierPoint( mX*t, mY*t ); }             // this * t

  BezierPoint divideBy( float t ) { return new BezierPoint( mX/t, mY/t ); }          // this / t

  BezierPoint add( BezierPoint c ) { return new BezierPoint( mX + c.mX, mY + c.mY ); } // this + c
  BezierPoint add( float x0, float y0 ) { return new BezierPoint( mX + x0, mY + y0 ); } // this + (x0,y0)
  void add2( BezierPoint a, BezierPoint b ) { mX = a.mX+b.mX; mY=a.mY+b.mY; }          // this = a + b

  BezierPoint sub( BezierPoint c ) { return new BezierPoint( mX - c.mX, mY - c.mY ); } // this - c
  BezierPoint sub( float x0, float y0 ) { return new BezierPoint( mX - x0, mY - y0 ); } // this - (x0,y0)

  float dot( BezierPoint c )       { return mX * c.mX + mY * c.mY; }                   // this * c
  float dot( float x0, float y0 )       { return mX * x0 + mY * y0; }                   // this * (x0,y0)

  float cross( BezierPoint c )     { return mX * c.mY - mY * c.mX; }                   // this ^ c
  float cross( float x0, float y0 )     { return mX * y0 - mY * x0; }                   // this ^ (x0,y0)


  void shiftBy( BezierPoint  c )                             // this += c
  {
    mX += c.mX;
    mY += c.mY;
  }

  float length() 
  { 
    float l2 = mX*mX + mY*mY;
    if ( l2 > 0.000001f ) return FloatMath.sqrt( l2 );
    return 0.0f;
  }

  float squareLength() { return mX*mX + mY*mY; }

  float distance( BezierPoint p )
  {
    float x = mX - p.mX;
    float y = mY - p.mY;
    float d = x*x + y*y;
    if ( d > 0.0f ) return FloatMath.sqrt(d);
    return 0.0f;
  }

  float distance( float x, float y )
  { 
    x -= mX;
    y -= mY;
    return FloatMath.sqrt( x*x + y*y + 0.000001f );
  }

  void normalize()
  {
    float d = length();
    if ( d > 0.0001f ) {
      mX /= d;
      mY /= d;
    } else {
      mX = 0.0f;
      mY = 0.0f;
    }
  }
}
