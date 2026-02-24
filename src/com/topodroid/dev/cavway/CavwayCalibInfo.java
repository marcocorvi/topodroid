/* @file CavwayCalibInfo.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid Cavway utility to make the 16-byte calib info
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.cavway;

public class CavwayCalibInfo
{
  private static final int CALIINFO_FLAG    =  0;
  private static final int CALIINFO_VER     =  1;
  private static final int CALIINFO_DATE    =  2;
  private static final int CALIINFO_ERR_AVE =  6;
  private static final int CALIINFO_ERR_VAR =  8;
  private static final int CALIINFO_ERR_MAX = 10;
  private static final int CALIINFO_DIP     = 12;

  /**
   * @param date    seconds since the epoch
   * @param err_ave average error
   * @param err_stddev
   # @param err_max maximum error
   * @param dip     (TopoDroid) dip
   */
  static public byte[] makeCaliInfo( long date, float err_ave, float err_stddev, float err_max, float dip )
  {
    byte[] ret = new byte[16];
    ret[ CALIINFO_FLAG     ] = (byte)0x55;
    ret[ CALIINFO_VER      ] = (byte)0x01; // version
    ret[ CALIINFO_DATE     ] = (byte)( date & 0xff );
    ret[ CALIINFO_DATE + 1 ] = (byte)( (date>> 8) & 0xff );
    ret[ CALIINFO_DATE + 2 ] = (byte)( (date>>16) & 0xff );
    ret[ CALIINFO_DATE + 3 ] = (byte)( (date>>24) & 0xff );
    int tmp = (int)(err_ave * 100);
    ret[ CALIINFO_ERR_AVE     ] = (byte)( tmp & 0xff );
    ret[ CALIINFO_ERR_AVE + 1 ] = (byte)( (tmp>>8) & 0xff );
    tmp = (int)(err_stddev * 100);
    ret[ CALIINFO_ERR_VAR     ] = (byte)( tmp & 0xff );
    ret[ CALIINFO_ERR_VAR + 1 ] = (byte)( (tmp>>8) & 0xff );
    tmp = (int)(err_max * 100);
    ret[ CALIINFO_ERR_MAX     ] = (byte)( tmp & 0xff );
    ret[ CALIINFO_ERR_MAX + 1 ] = (byte)( (tmp>>8) & 0xff );
    tmp = (int)( dip * 100);
    ret[ CALIINFO_DIP     ] = (byte)( tmp & 0xff );
    ret[ CALIINFO_DIP + 1 ] = (byte)( (tmp>>8) & 0xff );
    return ret;
  }

}
