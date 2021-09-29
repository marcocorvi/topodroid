/* @file InfoReadA3Task.java
 *
 * @author marco corvi
 * @date apr 2016
 *
 * @brief TopoDroid DistoX info A3 read task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox1;

import com.topodroid.DistoX.TopoDroidApp;

import java.lang.ref.WeakReference;

// import android.app.Activity;
import android.os.AsyncTask;
// import android.content.Context;

public class InfoReadA3Task extends AsyncTask<Void, Integer, Boolean>
{
  private final WeakReference<TopoDroidApp>  mApp; // FIXME LEAK
  private final WeakReference<DeviceA3InfoDialog> mDialog;
  private DeviceA3Info mInfo = null;
  // int mType; // DistoX type
  private String mAddress;

  public InfoReadA3Task( TopoDroidApp app, DeviceA3InfoDialog dialog, String address )
  {
    mApp      = new WeakReference<TopoDroidApp>( app );
    mDialog   = new WeakReference<DeviceA3InfoDialog>( dialog );
    mAddress  = address;
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    if ( mApp.get() == null ) return null;
    mInfo = mApp.get().readDeviceA3Info( mAddress );
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
    }
  }

}
