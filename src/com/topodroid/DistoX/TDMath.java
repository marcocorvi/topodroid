/* @file TDMath.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief math utilities
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.Math;

// import android.util.Log;

class TDMath
{
  static final float M_PI  = (float)Math.PI;     // 3.1415926536f;
  static final float M_2PI = (2*M_PI); // 6.283185307f; 
  static final float M_PI2 = M_PI/2;        // Math.PI/2
  static final float M_PI4 = M_PI/4;        // Math.PI/4
  static final float M_PI8 = M_PI/8;        // Math.PI/8
  static final float RAD2DEG = (180.0f/M_PI);
  static final float DEG2RAD = (M_PI/180.0f);

  static float abs( float x )   { return (float)( Math.abs(x) ); }
  static float cos( float x )   { return (float)Math.cos( x ); }
  static float cosd( float xd ) { return (float)Math.cos( xd * DEG2RAD ); }
  static float sin( float x )   { return (float)Math.sin( x ); }
  static float sind( float xd ) { return (float)Math.sin( xd * DEG2RAD ); }
  static float atan2( float y, float x ) { return (float)( Math.atan2( y, x ) ); }
  static float atan2d( float y, float x ) { return (float)( RAD2DEG * Math.atan2( y, x ) ); }
  static float acos( float x )   { return (float)( Math.acos( x ) ); }
  static float acosd( float x )  { return (float)( RAD2DEG * Math.acos( x ) ); }
  static float asind( float x )  { return (float)( RAD2DEG * Math.asin( x ) ); }
  static float sqrt( float x )   { return (float)Math.sqrt( x ); }

  static int in360( int f )
  {
    while ( f >= 360 ) f -= 360;
    while ( f < 0 )    f += 360;
    return f;
  }

  static float in360( float f )
  {
    while ( f >= 360 ) f -= 360;
    while ( f < 0 )    f += 360;
    return f;
  }

  static double in360( double f )
  {
    while ( f >= 360 ) f -= 360;
    while ( f < 0 )    f += 360;
    return f;
  }

  static float add90( float a )
  {
    a += 90;
    if ( a >= 360 ) a -= 360;
    return a;
  }

  static int add180( int a )
  {
    a += 180;
    if ( a >= 360 ) a -= 360;
    return a;
  }

  static float add180( float a )
  {
    a += 180;
    if ( a >= 360 ) a -= 360;
    return a;
  }

  static double add180( double a )
  {
    a += 180;
    if ( a >= 360 ) a -= 360;
    return a;
  }

  static float sub90( float a )
  {
    a -= 90;
    if ( a < 0 ) a += 360;
    return a;
  }


  static float around( float f, float f0 ) 
  {
    if ( f - f0 > 180 ) return f - 360;
    if ( f0 - f > 180 ) return f + 360;
    return f;
  }

  static float degree2slope( float deg ) { return (float)(100 * Math.tan( deg * DEG2RAD ) ); }
  static float slope2degree( float slp ) { return (float)( Math.atan( slp/100 ) * RAD2DEG ); }

}
