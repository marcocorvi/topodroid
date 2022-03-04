/* @file DxfColor.java
 *
 * @author Balazs Holl
 * @date apr 2021
 *
 * @brief TopoDroid dxf color functions
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.dxf;

public class DxfColor
{
  /** covert RGB color to index
   * @param rgb   RGB [hex]
   * @return color index
   */
  public static int rgbToIndex( long rgb )
  { 
    double r = ((rgb >> 16)&0xff) / 255.0; 
    double g = ((rgb >>  8)&0xff) / 255.0; 
    double b = ((rgb      )&0xff) / 255.0; 

    double min = Math.min(Math.min(r, g), b);
    double max = Math.max(Math.max(r, g), b);
    double delta = max - min;

    // Saturation
    if ( max == 0 ) return 250;      // ci=250 black
    double s = delta / max;

    // Ligtness
    double l = max;
    if( delta < 0.1 ) {
      if (max >= 0.88 ) {
        return 7;       // ci=7 white 255 (or black if background white)
      }
      if (max >= 0.73 ) {
        return 9;       // ci=9 grey 192
      }
      if (max == 0.5 ) {
        return 8;       // ci=8 grey 128
      }
      int ci = (int)( max * 7 + 0.5) + 250; // not exact DXF
      return ( ci > 255 )? 255 : ci;        // ci=255 grey 179
                                            // ci=250-255 gray scale
    }

    // Hue
    double h = ( r == max ) ?  ( g - b ) / delta // red, between yellow & magenta
             : ( g == max ) ?  2 + ( b - r ) / delta // green, between cyan & yellow
             : 4 + ( r - g ) / delta; //blue, between magenta & cyan
    h *= 4;    // 0-23.99
    if( h < 0 ) h += 24;
    return ((int)(h))*10+10 + (int)((1-l)*4) * 2 + 1-(int)(s+0.5);
  }
}
