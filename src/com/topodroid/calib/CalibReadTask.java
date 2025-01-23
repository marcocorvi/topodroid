/* @file CalibReadTask.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Cave3D calib coeff read task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.utils.TDLog;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.DeviceActivity;
import com.topodroid.TDX.GMActivity;
import com.topodroid.TDX.R;

import java.lang.ref.WeakReference;
// import java.util.List;

// import android.app.Activity;
// import android.app.ActivityManager;
// import android.app.ActivityManager.RunningTaskInfo;
import android.os.AsyncTask;
// import android.content.Context;

// import android.widget.Button;

public class CalibReadTask extends AsyncTask<Void, Integer, Boolean>
{
  public static final int PARENT_DEVICE = 1;
  public static final int PARENT_GM     = 2;
  // public static final int PARENT_AUTO   = 3; // AUTO-CALIB

  private byte[]   mCoeff;
  private final WeakReference<TopoDroidApp> mApp; // FIXME LEAK
  // private final WeakReference<Context> mContext;  // FIXME LEAK
  private final WeakReference< ICoeffDisplayer > mParent;
  private final int mParentType;
  private final boolean mTwoSensors;
  // String comp_name;

  public CalibReadTask( ICoeffDisplayer parent, TopoDroidApp app, int parent_type, boolean two_sensors ) // TWO_SENSORS
  {
    int len = ( two_sensors ? 104 : 52 );
    // mContext = new WeakReference<Context>( context );
    mParent = new WeakReference<ICoeffDisplayer>( parent );
    mApp    = new WeakReference<>( app );
    mCoeff  = new byte[len]; 
    mParentType = parent_type;
    mTwoSensors = two_sensors;
    // comp_name = "ComponentInfo{com.topodroid.TDX/com.topodroid.DistoX." + act_name + "}";
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    TDLog.v("Calib Read Task - two sensors " + mTwoSensors + " coeff length " + mCoeff.length );
    if ( mApp.get() == null ) return false;
    boolean ret = mApp.get().readCalibCoeff( mCoeff ); // 20250123 dropped mTwoSensors
    return ret;
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  // TODO TWO_SENSORS use second set of coeffs
  @Override
  protected void onPostExecute( Boolean result )
  {
    try {
      if (mParent.get() != null && !mParent.get().isActivityFinishing()) {
        mParent.get().enableButtons(true);
      }
    } catch ( NullPointerException e ) {
      TDLog.e( e.getMessage() );
    }

    if ( result ) {
      String[] items = new String[8];

      // TODO TWO_SENSORS if twoSensors must store somewhere also the second sensor-set coeffs
      //                  maybe inside the ICoeffDisplayer ...

      switch ( mParentType ) {
        case PARENT_DEVICE:
          if ( DeviceActivity.mDeviceActivityVisible && mParent.get() != null && !mParent.get().isActivityFinishing() ) {
            mParent.get().displayCoeff( mCoeff );
            // (new CalibCoeffDialog( mContext.get(), null, bg, ag, bm, am, nL, null, 0.0f, 0.0f, 0.0f, 0.0f, 0, null /*, false */ ) ).show();
          }
          break;
        case PARENT_GM:
          if ( GMActivity.mGMActivityVisible && mParent.get() != null && !mParent.get().isActivityFinishing() ) {
            mParent.get().displayCoeff( mCoeff );
            // (new CalibCoeffDialog( mContext.get(), null, bg, ag, bm, am, nL, null, 0.0f, 0.0f, 0.0f, 0.0f, 0, null /*, false */ ) ).show();
          }
          break;
        // case PARENT_AUTO: // AUTO-CALIB
        //     mParent.get().displayCoeff( bg, ag, bm, am, nL );
        //   break;
      }
    } else {
      TDToast.makeBad( R.string.read_failed );
    }
  }
}
