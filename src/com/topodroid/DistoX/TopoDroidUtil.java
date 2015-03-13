/** @file TopoDroidUtil.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief numerical utilities
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

public class TopoDroidUtil
{
  static final float M_PI  = 3.1415926536f; // Math.PI;
  static final float M_2PI = 6.283185307f;  // 2*Math.PI;
  static final float M_PI2 = M_PI/2;        // Math.PI/2
  static final float M_PI4 = M_PI/4;        // Math.PI/4
  static final float M_PI8 = M_PI/8;        // Math.PI/8
  static final float RAD2GRAD = (180.0f/M_PI);
  static final float GRAD2RAD = (M_PI/180.0f);

  static final long ZERO = 32768;
  static final long NEG  = 65536;
  static final float FV = 24000.0f;
  static final float FM = 16384.0f;
  static final float FN = 2796f; // 2^26 / FV

  static final float DEG2GRAD = 400.0f/360.0f;
  static final float GRAD2DEG = 360.0f/400.0f;

  static final float M2FT = 3.28083f; // meters to feet 
  static final float FT2M = 1/M2FT;
  static final float IN2M = 0.0254f;
  static final float YD2M = 0.914f;
  static float in360( float f )
  {
    while ( f >= 360 ) f -= 360;
    while ( f < 0 )    f += 360;
    return f;
  }

  static float around( float f, float f0 ) 
  {
    if ( f - f0 > 180 ) return f - 360;
    if ( f0 - f > 180 ) return f + 360;
    return f;
  }

  static float degree2slope( float deg )
  {
    return (float)(100 * Math.tan( deg * GRAD2RAD ) );
  }

  static float slope2degree( float slp )
  {
    return (float)( Math.atan( slp/100 ) * RAD2GRAD );
  }

}
