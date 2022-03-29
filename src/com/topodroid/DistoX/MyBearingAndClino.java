/* @file MyBearingAndClino.java
 *
 * @author marco corvi 
 * @date nov 2013
 *
 * @brief TopoDroid bearing and clino interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDVersion;
import com.topodroid.ui.ExifInfo;

// import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Locale;

// import android.widget.ImageView;

// import android.graphics.Matrix;
// import android.graphics.Bitmap;

// import android.media.ExifInterface; // REQUIRES android.support

public class MyBearingAndClino implements IBearingAndClino
{

  private final TopoDroidApp mApp;
  // private File  mFile;
  private String mFilepath;  // file full pathname
  // long  mPid;             // plot id
  private ExifInfo mExif;

  /** cstr
   * @param app         application
   * @param imagefile   image file full path
   */
  MyBearingAndClino( TopoDroidApp app, String imagefile /*, long pid */ )
  {
    mApp  = app; 
    // mFile = imagefile;
    mFilepath = imagefile; // mFile.getPath();
    // mPid  = pid;
    mExif = new ExifInfo();
  }

  /** set azimuth/clino and orientation index 
   * @param b0 azimuth [degrees]
   * @param c0 clino [degrees]
   * @param o0 camera orientation [degrees], 0: up, 90: right, etc.
   * @param a0 accuracy 
   */
  public void setBearingAndClino( float b0, float c0, int o0, int a0 )
  {
    // TDLog.v( "BearingClino UI set orientation " + o0 + " bearing " + b0 + " clino " + c0 );
    // TDLog.v( "Bearing and Clino orientation " + o0 );
    // this is not good for photo because it might alter azimuth/clino of xsection sketch
    // mApp.mData.updatePlotAzimuthClino( TDInstance.sid, mPid, b0, c0 );
    mExif.setExifValues( b0, c0, o0, a0 );
  }

  /** write the image data to the (output) file - and stores azimuth/clino as well
   * @param data    image data
   * @return true on success
   */
  public boolean setJpegData( byte[] data )
  {
    if ( data == null ) return false; // FIXME crash 2020-08-09
    try {
      FileOutputStream fos = TDFile.getFileOutputStream( mFilepath );
      fos.write( data );
      fos.flush();
      fos.close();
      // TDLog.v( "BearingClino UI saved JPEG file " + mFile.getPath() );
    } catch ( IOException e ) {
      TDLog.Error( "IO exception " + e.getMessage() );
      return false;
    }
    mExif.writeExif( mFilepath );
    return true;
  }

}
