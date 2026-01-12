/* @file CavwayDetails.java
 *
 * @author siwei tian
 * @date july 2024
 *
 * @brief TopoDroid Cavway BLE internal details
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.cavway;

import com.topodroid.math.TDVector;
import com.topodroid.math.TDMatrix;
import com.topodroid.utils.TDUtil;


public class CavwayDetails
{
  static final int MAX_INDEX_XBLE = 1064;

  public final static int TIMESTAMP_ADDRESS = 0x8000;
  public final static int COEFF_ADDRESS     = 0x9080;
  public final static int FIRMWARE_ADDRESS  = 0xe000;
  public final static int HARDWARE_ADDRESS  = 0xe004;
  //public final static int STATUS_ADDRESS   = 0xC044;

  /** the coeffs are stored in 128 bytes stating at position 0x9080
   * the coeffs of the first set are at offset 0
   * the coeffs of the second set are at offset 64
   */
  final static int COEFF_SIZE = 128; // size of coeff buffer
  final static int COEFF_OFF1 =   0; // offset of first coeffs
  final static int COEFF_OFF2 =  64; // offset of second coeffs
  final static int COEFF_LEN  =  52; // length of each coeffs
  

  public static int boundNumber( int nr )
  {
    if ( nr < 0 ) return 0;
    if ( nr > 2048 ) return 2048;
    return nr;
  }

  // status word (high bits in the next byte)
  final static int MASK_DIST_UNIT    = 0x0007; // distance unit mask
  final static int BIT_ANGLE_UNIT    = 0x0008; // angle unit
  final static int BIT_ENDPIECE_REF  = 0x0010; // endpiece reference
  final static int BIT_CALIB_MODE    = 0x0020; // calib-mode on
  final static int BIT_DISPLAY_LIGHT = 0x0040; // display illumination on
  final static int BIT_BEEP          = 0x0080; // beep on
  final static int BIT_TRIPLE_SHOT   = 0x0100; // triple shot check on (2.4+)
  final static int BIT_BLUETOOTH     = 0x0200; // bluetooth on
  final static int BIT_LOCKED_POWER  = 0x0400; // locked power off
  final static int BIT_CALIB_SESSION = 0x0800; // new calibration session
  final static int BIT_ALKALINE      = 0x1000; // battery = alkaline
  final static int BIT_SILENT_MODE   = 0x2000; // silent mode
  final static int BIT_REVERSE_SHOT  = 0x4000; // reverse measurement (2.4+)

  private static final byte CALIB_BIT = (byte)0x20; // X2 calib bit

  static boolean isCalibMode( byte b ) { return ( ( b & CALIB_BIT ) == CALIB_BIT ); }
  static boolean isNotCalibMode( byte b ) { return ( ( b & CALIB_BIT ) == 0 ); }


  private static int getCoeff( byte[] coeff, int pos )
  {
    return ( (int)(coeff[pos+1]) << 8 ) | (int)(coeff[pos]);
  }

  // the coefficients are
  //  0: bG.x*Fv aG.x.x*FM aG.x.y*FM aG.x.z*fM 
  //  8: bG.y*FV ag.y.x*FM ...
  // 16: bG.z*FV ...
  // 24: bM.x*FV aM.x.x*FM aM.x.y*FM ...
  // 32  bM.y*FV ...
  // 40: bM.z*FV ...
  // the second set is at offset 64
 
  /** @param off   sensosr-set offset (0 or 64)
   */
  static void parseCoeff( byte[] coeff, int off, TDVector bg, TDVector bm, TDMatrix ag, TDMatrix am )
  {
    float FV = TDUtil.FV;
    float FM = TDUtil.FM;
    bg.x   = getCoeff( coeff, off+ 0 ) / FV;
    ag.x.x = getCoeff( coeff, off+ 2 ) / FM;
    ag.x.y = getCoeff( coeff, off+ 4 ) / FM;
    ag.x.z = getCoeff( coeff, off+ 6 ) / FM;
    bg.y   = getCoeff( coeff, off+ 8 ) / FV;
    ag.y.x = getCoeff( coeff, off+10 ) / FM;
    ag.y.y = getCoeff( coeff, off+12 ) / FM;
    ag.y.z = getCoeff( coeff, off+14 ) / FM;
    bg.z   = getCoeff( coeff, off+16 ) / FV;
    ag.z.x = getCoeff( coeff, off+18 ) / FM;
    ag.z.y = getCoeff( coeff, off+20 ) / FM;
    ag.z.z = getCoeff( coeff, off+22 ) / FM;
    bm.x   = getCoeff( coeff, off+24 ) / FV;
    am.x.x = getCoeff( coeff, off+26 ) / FM;
    am.x.y = getCoeff( coeff, off+28 ) / FM;
    am.x.z = getCoeff( coeff, off+30 ) / FM;
    bm.y   = getCoeff( coeff, off+32 ) / FV;
    am.y.x = getCoeff( coeff, off+34 ) / FM;
    am.y.y = getCoeff( coeff, off+36 ) / FM;
    am.y.z = getCoeff( coeff, off+38 ) / FM;
    bm.z   = getCoeff( coeff, off+40 ) / FV;
    am.z.x = getCoeff( coeff, off+42 ) / FM;
    am.z.y = getCoeff( coeff, off+44 ) / FM;
    am.z.z = getCoeff( coeff, off+46 ) / FM;
  }

}
