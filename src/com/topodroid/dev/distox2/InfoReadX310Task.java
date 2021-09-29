/* @file InfoReadX310Task.java
 *
 * @author marco corvi
 * @date apr 2016
 *
 * @brief TopoDroid DistoX info X310 read task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox2;

import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.R;
import com.topodroid.DistoX.TDToast;

import com.topodroid.utils.TDLog;

import java.lang.ref.WeakReference;

// import android.app.Activity;
import android.os.AsyncTask;
// import android.content.Context;

public class InfoReadX310Task extends AsyncTask<Void, Integer, Boolean>
{
  private final WeakReference<TopoDroidApp>   mApp; // FIXME LEAK
  private final WeakReference<DeviceX310InfoDialog>  mDialog;
  private DeviceX310Info mInfo = null;
  // int mType; // DistoX type
  private String mAddress;

  public InfoReadX310Task( TopoDroidApp app, DeviceX310InfoDialog dialog, String address )
  {
    TDLog.Log( TDLog.LOG_BT, "X310 info read - address " + address );
    mApp      = new WeakReference<TopoDroidApp>( app );
    mDialog   = new WeakReference<DeviceX310InfoDialog>( dialog );
    mAddress  = address;
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    if ( mApp.get() == null ) return null;
    mInfo = mApp.get().readDeviceX310Info( mAddress );
    return ( mInfo != null );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( result && mDialog.get() != null ) {
      mDialog.get().updateInfo( mInfo );
    } else {
      TDToast.makeBad( R.string.read_failed );
    }
  }

}
