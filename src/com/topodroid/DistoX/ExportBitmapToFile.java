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
import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

class ExportBitmapToFile extends AsyncTask<Void,Void,Boolean> 
{
    private Context mContext;
    private Handler mHandler;
    private Bitmap mBitmap;
    private String mFullName;

    public ExportBitmapToFile( Context context, Handler handler, Bitmap bitmap, String name )
    {
       mContext  = context;
       mBitmap   = bitmap;
       mHandler  = handler;
       mFullName = name;
       // TDLog.Log( TDLog.LOG_PLOT, "ExportBitmapToFile " + mFullName );
    }

    @Override
    protected Boolean doInBackground(Void... arg0)
    {
      try {
        String filename = TDPath.getPngFileWithExt( mFullName );
        TDPath.checkPath( filename );
        final FileOutputStream out = new FileOutputStream( filename );
        mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        out.flush();
        out.close();
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      }
      //mHandler.post(completeRunnable);
      return false;
    }

    @Override
    protected void onPostExecute(Boolean bool) {
      super.onPostExecute(bool);
      if ( mHandler != null ) mHandler.sendEmptyMessage( bool? 1 : 0 );
    }
}
