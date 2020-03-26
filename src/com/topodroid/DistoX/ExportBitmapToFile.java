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

import java.io.FileOutputStream;

// import android.content.Context;

import android.os.AsyncTask;
// import android.os.Handler;
import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;

class ExportBitmapToFile extends AsyncTask<Void,Void,Boolean> 
{
    private Bitmap mBitmap;
    private float  mScale;
    private String mFullName;
    private String filename = null;
    private boolean mToast;
    private String  mFormat;

    ExportBitmapToFile( String format, Bitmap bitmap, float scale, String name, boolean toast )
    {
       mFormat   = format;
       mBitmap   = bitmap;
       mScale    = scale;
       mFullName = name;
       mToast    = toast;
       // TDLog.Log( TDLog.LOG_PLOT, "Export Bitmap To File " + mFullName );
    }

    @Override
    protected Boolean doInBackground(Void... arg0)
    {
      return exec();
    }

    boolean exec()
    {
      try {
        filename = TDPath.getPngFileWithExt( mFullName );
        TDPath.checkPath( filename );
        final FileOutputStream out = new FileOutputStream( filename );
        mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        out.flush();
        out.close();
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      }
      return false;
    }

    @Override
    protected void onPostExecute(Boolean bool) {
      super.onPostExecute(bool);
      if ( mToast ) {
        if ( bool ) {
          TDToast.make( String.format( mFormat, filename, mScale) );
        } else {
          TDToast.makeBad( R.string.saving_file_failed );
        }
      }
    }
}
