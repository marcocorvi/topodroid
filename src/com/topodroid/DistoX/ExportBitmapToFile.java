/* @file ExportBitmapToFile.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid plot export as bitmap
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.FileOutputStream;

import android.content.Context;

import android.os.AsyncTask;
// import android.os.Handler;
import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;

import android.widget.Toast;

class ExportBitmapToFile extends AsyncTask<Void,Void,Boolean> 
{
    private Context mContext; // FIXME LEAK: used to Toast
    private Bitmap mBitmap;
    private float  mScale;
    private String mFullName;
    private String filename = null;
    private boolean mToast;

    ExportBitmapToFile( Context context, Bitmap bitmap, float scale, String name, boolean toast )
    {
       mContext  = context;
       mBitmap   = bitmap;
       mScale    = scale;
       mFullName = name;
       mToast    = toast;
       // TDLog.Log( TDLog.LOG_PLOT, "Export Bitmap To File " + mFullName );
    }

    @Override
    protected Boolean doInBackground(Void... arg0)
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
          Toast.makeText( mContext, 
                          String.format( mContext.getResources().getString(R.string.saved_file_2), filename, mScale),
                          Toast.LENGTH_SHORT ).show();
        } else {
          Toast.makeText( mContext, R.string.saving_file_failed, Toast.LENGTH_SHORT ).show();
        }
      }
    }
}
