/** @file TglColor.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D colors
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

public class TglColor
{
  float[] color;

  /** copy cstr
   * @param c   TGL color
   * @note copy the color-array of c
   */
  TglColor( TglColor c ) 
  {
    color = new float[4];
    color[0] = c.color[0];
    color[1] = c.color[1];
    color[2] = c.color[2];
    color[3] = c.color[3];
  }

  /** cstr
   * @param c   array of the color four (float) components (RGBA)
   * @note set the color-array in this object to point the passed array
   */
  TglColor( float[] c ) 
  {
    color = new float[4];
    color[0] = c[0];
    color[1] = c[1];
    color[2] = c[2];
    color[3] = c[3];
    // WAS color = c;
  }

  // TglColor( float red, float green, float blue, float alpha )
  // {
  //   color = new float[4];
  //   setColor( red, green, blue, alpha );
  // }

  // private void setColor( float r, float g, float b )
  // {
  //   color[0] = (r<0)? 0 : (r>1)? 1 : r;
  //   color[1] = (g<0)? 0 : (g>1)? 1 : g;
  //   color[2] = (b<0)? 0 : (b>1)? 1 : b;
  // }

  // private void setColor( float r, float g, float b, float a )
  // {
  //   setColor( r, g, b );
  //   color[3] = (a<0)? 0 : (a>1)? 1 : a;
  // }

  //-------------------------------------------------------
  //  0xff004949, 0xff009292, 0xffff6d36, 0xffffb677, 0xff490092, 0xff006ddb,
  static final int[] surveyColor = 
    { 0xfff5af6d, // RED
      0xffffb677,
      0xffff6d36,
      0xff6db6ff, // VIOLET
      0xffb6dbff,
      0xffb63dff,
      0xff49ff92, // GREEN
      0xff30ffdb, 
      0xff49ffce,
      0xff9224f2, // BLUE
      0xff24cff2,
      0xff40d1f2
    };
                                // red         green       blue        blue        violet
  static final int[] axisColor = { 0xffff0000, 0xff00ff00, 0xff0000ff, 0xff0033ff, 0xffff00cc }; // Up, East, North, Dem-proj, plan/profile-proj

  static final int SURVEY_COLOR_NR = surveyColor.length; 
  static final int AXIS_COLOR_NR   = axisColor.length; 

  /** @return a survey color
   * @param k index
   */
  public static int getSurveyColor( int k )
  {
    if ( k < 0 ) k = -k;
    return surveyColor[ k % SURVEY_COLOR_NR ];
  }

  /** @return a random survey color 
   */
  public static int getSurveyColor( )
  {
    return surveyColor[ (int)( Math.random() * TglColor.SURVEY_COLOR_NR ) ];
  }

  /** fill the RGB color-array with a survey color
   * @param index   survey index
   * @param color   color-array 
   */
  static void indexToSurveyColor( int index, float[] color ) 
  {
    int col = surveyColor[ index % SURVEY_COLOR_NR ];
    colorToSurveyColor( col, color );
  }

  /** fill the RGB color-array with a survey color
   * @param col    integer color
   * @param color   color-array 
   */
  static void colorToSurveyColor( int col, float[] color ) 
  {
    color[0] = (float)( (col>>16)&0xff )/255.0f;
    color[1] = (float)( (col>> 8)&0xff )/255.0f;
    color[2] = (float)( (col    )&0xff )/255.0f;
  }

  /** fill the RGB color-array with an axis color
   * @param index   axis index
   * @param color   color-array 
   */
  static void getAxisColor( int index, float[] color ) 
  {
    int col = axisColor[ index % AXIS_COLOR_NR ];
    color[0] = (float)( (col>>16)&0xff )/255.0f;
    color[1] = (float)( (col>> 8)&0xff )/255.0f;
    color[2] = (float)( (col    )&0xff )/255.0f;
  }
  
  // FIXME eventually there will be a color dialog to let user set these ...
  static final float[] ColorStation    = { 0.0f, 0.7f, 0.5f, 1.0f }; // green
  static final float[] ColorLeg        = { 1.0f, 1.0f, 1.0f, 1.0f }; // white
  static final float[] ColorLegS       = { 0.7f, 1.0f, 0.5f, 1.0f }; // green
  static final float[] ColorLegD       = { 1.0f, 0.7f, 0.5f, 1.0f }; // red
  static final float[] ColorLegC       = { 0.3f, 0.7f, 1.0f, 1.0f }; // blue
  static final float[] ColorLegB       = { 0.8f, 0.9f, 0.3f, 1.0f }; // yellow
  static final float[] ColorSplay      = { 0.7f, 0.7f, 0.7f, 1.0f }; // gray
  static final float[] ColorSurfaceLeg = { 0.0f, 0.3f, 1.0f, 1.0f };
  static final float[] ColorPlan       = { 1.0f, 0.0f, 0.8f, 1.0f }; // violet
  static final float[] ColorGPS        = { 1.0f, 0.7f, 0.0f, 1.0f }; // yellow

}
