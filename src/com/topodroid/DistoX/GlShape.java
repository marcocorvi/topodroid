/** @file GlShape.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D (abstract) shape 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;

class GlShape
{
  protected FloatBuffer dataBuffer = null;
  protected ShortBuffer orderBuffer;

  protected Context mContext;


  // -----------------------------------------------------------------
  GlShape( Context ctx ) { mContext = ctx; }

  void initDataBuffer( float[] data )
  {
    dataBuffer = DataBuffer.getFloatBuffer( data.length );
    if ( dataBuffer != null ) {
      dataBuffer.put( data );
    }
  }

}
