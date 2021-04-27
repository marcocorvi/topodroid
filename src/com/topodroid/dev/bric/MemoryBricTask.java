/* @file MemoryBricTask.java
 *
 * @author marco corvi
 * @date mar 2021 
 *
 * @brief TopoDroid BRIC info read task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.R;
import com.topodroid.DistoX.TDToast;

import com.topodroid.utils.TDLog;

import android.util.Log;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

public class MemoryBricTask extends AsyncTask<Void, Integer, Boolean>
{
  private final WeakReference<TopoDroidApp>   mApp; // FIXME LEAK
  byte[] bytes;

  public MemoryBricTask( TopoDroidApp app ) // clear
  {
    mApp = new WeakReference<TopoDroidApp>( app );
    bytes = null;
    // Log.v("DistoX", "BRIC memory - clear ");
  }

  public MemoryBricTask( TopoDroidApp app, int yy, int mm, int dd, int HH, int MM, int SS ) // reset
  {
    mApp = new WeakReference<TopoDroidApp>( app );
    bytes = new byte[12];
    for ( int k=0; k<12; ++k ) bytes[k] = (byte)0x30;
    BricConst.setTimeBytes( bytes, (short)yy, (char)mm, (char)dd, (char)HH, (char)MM, (char)SS, (char)0 );
    // Log.v("DistoX", "BRIC memory - reset ");
  }


  @Override
  protected Boolean doInBackground(Void... v)
  {
    if ( mApp.get() == null ) {
      TDLog.Log( TDLog.LOG_BT, "BRIC memory - null app");
      return false;
    }
    // Log.v("DistoX", "BRIC memory - sending bytes " + ((bytes == null)? "null" : "non-null" ) );
    return mApp.get().setBricMemory( bytes );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( bytes != null ) {
      TDToast.make( result ? R.string.bric_reset_ok : R.string.bric_reset_fail );
    } else {
      TDToast.make( result ? R.string.bric_clear_ok : R.string.bric_clear_fail );
    }
  }

}

