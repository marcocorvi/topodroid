/* @file ExportBitmapToFile.java
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
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
import com.topodroid.prefs.TDSetting;

// import java.io.File; // ONLY TEMP_FILE
import java.io.FileOutputStream;

// import android.content.Context;

import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
// import android.os.Handler;
import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

class ExportBitmapToFile extends AsyncTask<Void,Void,Boolean> 
{
    private Bitmap mBitmap;
    private float  mScale;
    private String mFullName;
    private String filename = null;
    private boolean mToast;
    private String  mFormat;
    private Uri     mUri = null;

    ExportBitmapToFile( Uri uri, String format, Bitmap bitmap, float scale, String name, boolean toast )
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

    @Override
    protected Boolean doInBackground(Void... arg0)
    {
      return exec();
    }

    boolean exec()
    {
      ParcelFileDescriptor pfd = TDsafUri.docWriteFileDescriptor( mUri );
      if ( pfd == null ) return false;
      try {
        /*
        // File temp = File.createTempFile( "tmp", ".png", TDFile.getFile( TDPath.getPngFile("") ) );
        File dir = TDPath.getPngDir();
        if ( dir == null ) return false;
        File temp = File.createTempFile( "tmp", ".png", dir );
        // File temp = File.createTempFile( "tmp", ".png" );
        // TDLog.v( "temp file <" + temp.getPath() + ">" );
        final FileOutputStream out = TDFile.getFileOutputStream( temp );
        */
        // TDLog.v( "export bitmap - path <" + TDPath.getPngFileWithExt( mFullName ) + ">" );
        // TDLog.v( "export bitmap - uri <" + ((mUri != null)? mUri.toString() : "null") + ">" );
        // FileOutputStream out = (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : new FileOutputStream( TDPath.getPngFileWithExt( mFullName ) );
        FileOutputStream out = TDsafUri.docFileOutputStream( pfd );
        mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        out.flush();
        out.close();
        /*
        filename = TDPath.getPngFileWithExt( mFullName );
        TDPath.checkPath( filename );
        TDFile.renameTempFile( temp, filename );
        */
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        TDsafUri.closeFileDescriptor( pfd );
      }
      return false;
    }

    @Override
    protected void onPostExecute(Boolean bool) {
      super.onPostExecute(bool);
      if ( mToast ) {
        if ( bool ) {
          TDToast.make( String.format( mFormat, "png", mScale) );
        } else {
          TDToast.makeBad( R.string.saving_file_failed );
        }
      }
    }
}
