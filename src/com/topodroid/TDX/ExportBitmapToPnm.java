/* @file ExportBitmapToPnm.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid plot export as bitmap
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
// import com.topodroid.prefs.TDSetting;

// import java.io.File; // ONLY TEMP_FILE
import java.io.FileOutputStream;

// import android.content.Context;

import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
// import android.os.Handler;
import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

class ExportBitmapToPnm extends AsyncTask<Void,Void,Boolean> 
{
  static final byte[] P6 = {'P', '6', ' '};  

  private Bitmap mBitmap;
  private float  mScale;
  private String mFullName;
  private String filename = null;
  private boolean mToast;
  private String  mFormat;
  private Uri     mUri = null;

  /** cstr
   * @param uri      export URI or null (to export to private folder)
   * @param format   toast message format
   * @param bitmap   bitmap to export
   * @param scale    ...
   * @param name     plot fullname
   * @param toast    whether to toast to tell the user the result
   */
  ExportBitmapToPnm( Uri uri, String format, Bitmap bitmap, float scale, String name, boolean toast )
  {
     /* if ( TDSetting.mExportUri ) */ mUri = uri; // FIXME_URI
     mFormat   = format;
     mBitmap   = bitmap;
     mScale    = scale;
     mFullName = name;   // plot fullname
     mToast    = toast;
     // TDLog.Log( TDLog.LOG_PLOT, "Export bitmap to file " + mFullName );
     // TDLog.v( "Export bitmap to file " + mFullName );
  }

  /** execute the export task
   */
  @Override
  protected Boolean doInBackground(Void... arg0)
  {
    return exec();
  }

  /** execute the export task
   * @return true on success
   */
  boolean exec()
  {
    ParcelFileDescriptor pfd = null;
    if ( mUri != null ) {
      pfd = TDsafUri.docWriteFileDescriptor( mUri );
      if ( pfd == null ) return false;
    }
    try {
      // TDLog.v( "export bitmap - path <" + TDPath.getPngFileWithExt( mFullName ) + ">" );
      // TDLog.v( "export bitmap - uri <" + ((mUri != null)? mUri.toString() : "null") + ">" );

      FileOutputStream out = (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : TDFile.getPrivateFileOutputStream( "export", mFullName + ".pnm" );
      // FileOutputStream out = TDsafUri.docFileOutputStream( pfd );
      int w = mBitmap.getWidth();
      int h = mBitmap.getHeight();
      // TDLog.v("Bitmap " + w + "x" + h ); 

      out.write( P6 );
      StringBuilder sb = new StringBuilder();
      sb.append( Integer.toString( w ) );
      sb.append( " " );
      sb.append( Integer.toString( h ) );
      sb.append( " 255\n" );
      out.write( sb.toString().getBytes() );
      // int[] pixels = new int[ w ];
      byte[] bytes = new byte[ 3 * w ];
      for ( int j=0; j<h; ++j ) {
        // mBitmap.getPixels( pixels, 0, 0, 0, j, w, 1 );
        int ib = 0;
        for ( int i=0; i<w; ++i ) {
          int pixel = mBitmap.getPixel( i, j );
          bytes[ ib++ ] = (byte)( (pixel >> 16) & 0xff );
          bytes[ ib++ ] = (byte)( (pixel >>  8) & 0xff );
          bytes[ ib++ ] = (byte)( (pixel      ) & 0xff );
        }
        out.write( bytes );
      }
      out.flush();
      out.close();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if ( pfd != null ) TDsafUri.closeFileDescriptor( pfd );
    }
    return false;
  }

  /** post execution (on UI thread)
   * @param bool  execution result
   */
  @Override
  protected void onPostExecute(Boolean bool) {
    super.onPostExecute(bool);
    if ( mToast ) {
      if ( bool ) {
        TDToast.make( String.format( mFormat, "pnm", mScale) );
      } else {
        TDToast.makeBad( R.string.saving_file_failed );
      }
    }
  }
}
