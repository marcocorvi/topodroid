/* @file InfoReadA3Task.java
 *
 * @author marco corvi
 * @date apr 2016
 *
 * @brief TopoDroid DistoX info A3 read task
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Activity;
import android.os.AsyncTask;
import android.content.Context;

// import android.util.Log;

class InfoReadA3Task extends AsyncTask<Void, Integer, Boolean>
{
  TopoDroidApp   mApp;
  private DeviceA3InfoDialog  mDialog;
  private DeviceA3Info mInfo = null;
  // int mType; // DistoX type
  String mAddress;

  InfoReadA3Task( TopoDroidApp app, DeviceA3InfoDialog dialog, String address )
  {
    mApp      = app;
    mDialog   = dialog;
    mAddress  = address;
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    mInfo = mApp.readDeviceA3Info( mAddress );
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
    }
  }

}
