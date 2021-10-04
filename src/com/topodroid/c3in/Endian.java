/* @file Endian.java
 *
 * @author marco corvi
 * @date june 2020 
 *
 * @brief endianness conversions
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import java.io.DataInputStream;
import java.io.IOException;

class Endian
{
  static final int SIZEDBL = 8; // ( sizeof( double ) )
  static final int SIZE32  = 4; // ( sizeof( uint32_t ) ) // int
  static final int SIZE16  = 2; // ( sizeof( uint16_t ) ) // short

// one can safely assume that all Android are little endian (after ARM-3)
// #ifdef BIG_ENDIAN
//   int toIntLEndian( byte val[] )  // int32 to int
//   {
//     return val[3] | ( ((int)val[2]) << 8 ) | ( ((int)(val[1])) << 16 ) | ( ((int)(val[0])) << 24 );
//   }
//   
//   float toFloatFloatLEndian( byte val[] )
//   {
//     return (float)( val[3] | ( ((int)val[2]) << 8 ) | ( ((int)(val[1])) << 16 ) | ( ((int)(val[0])) << 24 ) );
//   }
// 
//   double toDoubleLEndian( byte val[] )
//   {
//     return (double)(
//       (long)(val[7]) | ( ((long)val[6]) << 8 ) | ( ((long)(val[5])) << 16 ) | ( ((long)(val[4])) << 24 ) |
//       ( ((long)(val[3])<<32) ) | ( ((long)val[2]) << 40 ) | ( ((long)(val[1])) << 48 ) | ( ((long)(val[0])) << 56 ) );
//   }
// #else

  // N.B. this returns an integer which contains the "unsigned short"
  // (java does not have unsigned, except byte)
  static int toShortLEndian( byte[] val ) 
  {
    // Log.v( "TopoGL-LOX", "toInt " + val[0] + " " + val[1] + " " + val[2] + " " + val[3] );
    int r0 = ( val[0] < 0 )? 256+val[0] : val[0];
    int r1 = ( val[1] < 0 )? 256+val[1] : val[1];
    return (r0 | ( r1 << 8 ));
  }

  static int toIntLEndian( byte[] val ) 
  {
    // Log.v( "TopoGL-LOX", "toInt " + val[0] + " " + val[1] + " " + val[2] + " " + val[3] );
    int r0 = ( val[0] < 0 )? 256+val[0] : val[0];
    int r1 = ( val[1] < 0 )? 256+val[1] : val[1];
    int r2 = ( val[2] < 0 )? 256+val[2] : val[2];
    int r3 = ( val[3] < 0 )? 256+val[3] : val[3];
    return r0 | ( r1 << 8 ) | ( r2 << 16 ) | ( r3 << 24 );
  }

  static float toFloatLEndian( byte[] val ) 
  {
    return Float.intBitsToFloat( (int)val[0] | ( ((int)val[1]) << 8 ) | ( ((int)(val[2])) << 16 ) | ( ((int)(val[3])) << 24 ) );
  }

  static double toDoubleLEndian( byte[] val )
  {
    int r0 = ( val[0] < 0 )? 256+val[0] : val[0];
    int r1 = ( val[1] < 0 )? 256+val[1] : val[1];
    int r2 = ( val[2] < 0 )? 256+val[2] : val[2];
    int r3 = ( val[3] < 0 )? 256+val[3] : val[3];
    int r4 = ( val[4] < 0 )? 256+val[4] : val[4];
    int r5 = ( val[5] < 0 )? 256+val[5] : val[5];
    int r6 = ( val[6] < 0 )? 256+val[6] : val[6];
    int r7 = ( val[7] < 0 )? 256+val[7] : val[7];
    // reinterpret as double
    return Double.longBitsToDouble(
      (long)(r0) | ( ((long)r1) << 8 ) | ( ((long)r2) << 16 ) | ( ((long)r3) << 24 ) |
      ( ((long)r4) << 32 ) | ( ((long)r5) << 40 ) | ( ((long)r6) << 48 ) | ( ((long)r7) << 56 ) 
    );
  }
// #endif

  static int toIntLEndian( byte[] val, int off ) 
  {
    byte[] tmp = new byte[4];
    for (int k=0; k<4; ++k ) tmp[k] = val[off+k];
    return toIntLEndian( tmp );
  }

  static double toDoubleLEndian( byte[] val, int off ) 
  {
    byte[] tmp = new byte[8];
    for (int k=0; k<8; ++k ) tmp[k] = val[off+k];
    return toDoubleLEndian( tmp );
  }

  // void convertToIntLEndian( byte[] val, int off )
  // {
  //   byte tmp = val[off+0]; val[off+0] = val[off+3]; val[off+3] = tmp;
  //        tmp = val[off+1]; val[off+1] = val[off+2]; val[off+2] = tmp;
  // }

  // void convertToDoubleLEndian( byte[] val, int off )
  // {
  //   byte tmp = val[off+0]; val[off+0] = val[off+7]; val[off+7] = tmp;
  //        tmp = val[off+1]; val[off+1] = val[off+6]; val[off+6] = tmp;
  //        tmp = val[off+2]; val[off+2] = val[off+5]; val[off+5] = tmp;
  //        tmp = val[off+3]; val[off+3] = val[off+4]; val[off+4] = tmp;
  // }

  // @param fis    file input stream
  // @param int32  array of SIZE32 bytes (preallocated)
  static int readInt32( DataInputStream fis, byte[] int32 ) throws IOException
  {
    return fis.read( int32, 0, SIZE32 );
  }

  static int readInt( DataInputStream fis, byte[] int32 ) throws IOException
  {
    fis.read( int32, 0, SIZE32 );
    return toIntLEndian( int32 );
  }

  static int readShort( DataInputStream fis, byte[] int16 ) throws IOException
  {
    fis.read( int16, 0, SIZE16 );
    return toShortLEndian( int16 );
  }

}
