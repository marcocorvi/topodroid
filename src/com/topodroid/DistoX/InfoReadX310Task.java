/* @file InfoReadX310Task.java
 *
 * @author marco corvi
 * @date apr 2016
 *
 * @brief TopoDroid DistoX info X310 read task
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Activity;
import android.os.AsyncTask;
// import android.content.Context;

import android.widget.Toast;

// import android.util.Log;

class InfoReadX310Task extends AsyncTask<Void, Integer, Boolean>
{
  TopoDroidApp   mApp; // FIXME LEAK
  private DeviceX310InfoDialog  mDialog;
  private DeviceX310Info mInfo = null;
  // int mType; // DistoX type
  String mAddress;

  InfoReadX310Task( TopoDroidApp app, DeviceX310InfoDialog dialog, String address )
  {
    mApp      = app;
    mDialog   = dialog;
    mAddress  = address;
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    mInfo = mApp.readDeviceX310Info( mAddress );
    return ( mInfo != null );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( result && mDialog != null ) {
      mDialog.updateInfo( mInfo );
    } else {
      Toast.makeText( mApp, R.string.read_failed, Toast.LENGTH_SHORT ).show();
    }
  }

}
