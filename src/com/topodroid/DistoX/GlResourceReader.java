/** @file GlResourceReader.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D resource reader
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
// import com.topodroid.c3in.ParserBluetooth;
// import com.topodroid.c3in.ParserSketch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;

class GlResourceReader
{
  static String readRaw( Context ctx, int res )
  {
    StringBuilder sb = new StringBuilder();
    try {
      InputStream is = ctx.getResources().openRawResource( res );
      InputStreamReader isr = new InputStreamReader( is );
      BufferedReader br = new BufferedReader( isr );
      String line;
      while ( ( line = br.readLine() ) != null ) { 
        sb.append( line );
        sb.append( '\n' );
      }
    } catch ( IOException e1 ) {
      throw new RuntimeException( "IO error. res " + res );
    } catch ( Resources.NotFoundException e2 ) {
      throw new RuntimeException( "Not found. res " + res );
    }
    return sb.toString();
  }

  static Bitmap readTexture( Context ctx, int res )
  {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;
    Bitmap bitmap = BitmapFactory.decodeResource( ctx.getResources(), res, options );
    return bitmap;
  }

}
