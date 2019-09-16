/* @file CalibReadTask.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calib coeff read task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

import java.lang.ref.WeakReference;
// import java.util.List;

// import android.app.Activity;
// import android.app.ActivityManager;
// import android.app.ActivityManager.RunningTaskInfo;
import android.os.AsyncTask;
// import android.content.Context;

// import android.widget.Button;

class CalibReadTask extends AsyncTask<Void, Integer, Boolean>
{
  static final int PARENT_DEVICE = 1;
  static final int PARENT_GM     = 2;

  private byte[]   coeff;
  private WeakReference<TopoDroidApp> mApp; // FIXME LEAK
  // private WeakReference<Context> mContext;  // FIXME LEAK
  private WeakReference< ICoeffDisplayer > mParent;
  private int mParentType;
  // String comp_name;

  CalibReadTask( ICoeffDisplayer parent, TopoDroidApp app, int parent_type )
  {
    // mContext = new WeakReference<Context>( context );
    mParent = new WeakReference<ICoeffDisplayer>( parent );
    mApp    = new WeakReference<>( app );
    coeff = new byte[52]; // always read 52 bytes
    mParentType = parent_type;
    // comp_name = "ComponentInfo{com.topodroid.DistoX/com.topodroid.DistoX." + act_name + "}";
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    return mApp.get() != null && mApp.get().readCalibCoeff( coeff );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    try {
      if (mParent.get() != null && !mParent.get().isActivityFinishing()) {
        mParent.get().enableButtons(true);
      }
    } catch ( NullPointerException e ) {
      TDLog.Error( e.getMessage() );
    }

    if ( result ) {
      String[] items = new String[8];
      Vector bg = new Vector();
      Matrix ag = new Matrix();
      Vector bm = new Vector();
      Matrix am = new Matrix();
      Vector nL = new Vector();
      CalibAlgo.coeffToG( coeff, bg, ag );
      CalibAlgo.coeffToM( coeff, bm, am );
      CalibAlgo.coeffToNL( coeff, nL );

      switch ( mParentType ) {
        case PARENT_DEVICE:
          if ( DeviceActivity.mDeviceActivityVisible && mParent.get() != null && !mParent.get().isActivityFinishing() ) {
            mParent.get().displayCoeff( bg, ag, bm, am, nL );
            // (new CalibCoeffDialog( mContext.get(), null, bg, ag, bm, am, nL, null, 0.0f, 0.0f, 0.0f, 0.0f, 0, null /*, false */ ) ).show();
          }
          break;
        case PARENT_GM:
          if ( TopoDroidApp.mGMActivityVisible && mParent.get() != null && !mParent.get().isActivityFinishing() ) {
            mParent.get().displayCoeff( bg, ag, bm, am, nL );
            // (new CalibCoeffDialog( mContext.get(), null, bg, ag, bm, am, nL, null, 0.0f, 0.0f, 0.0f, 0.0f, 0, null /*, false */ ) ).show();
          }
          break;
      }
    } else {
      TDToast.makeBad( R.string.read_failed );
    }
  }
}
