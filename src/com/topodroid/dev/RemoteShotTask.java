/* @file DeviceX310TakeShot.java
 *
 * @author muddy mohawk
 * @date oct 2026
 *
 * @brief TopoDroid remote shot class - extracted from the device TakeShot classes
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

import android.os.AsyncTask;
import com.topodroid.TDX.ILister;
import com.topodroid.TDX.ListerHandler;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;
import com.topodroid.prefs.TDSetting;
import com.topodroid.util.TDLog;
import com.topodroid.util.TDUtil;

/** Keeps the entire remote shot sequence on one captured instrument. */
public class RemoteShotTask extends AsyncTask<Integer, Integer, Boolean>
{
  private final ILister mILister;
  private final ListerHandler mLister;
  private final TopoDroidApp mApp;
  private final int mNr;
  private final int mDataType;

  private final Class<?> mDriverType;
  private DeviceOperation mOperation;

  /** cstr
   * @param ilister  data lister interface
   * @param lister   data lister
   * @param app      app
   * @param nr
   * @param data_type expected type of data
   * @param driver_type child class
   */
  protected RemoteShotTask( ILister ilister, ListerHandler lister, TopoDroidApp app, int nr, int data_type, Class<?> driver_type )
  {
    mILister = ilister;
    mLister = lister;
    mApp = app;
    mNr = nr;
    mDataType = data_type;
    mDriverType = driver_type; // MM
  }

  @Override protected void onPreExecute()
  {
    // mOperation = mApp.beginDeviceOperation(); // MM SWITCH
    mILister.enableBluetoothButton( false );
  }

  @Override protected Boolean doInBackground( Integer... ignored )
  {
    if ( mOperation == null || ! mDriverType.isInstance( mOperation.mComm ) || mNr <= 0 ) return false;
    try {
      boolean x310 = mOperation.mComm instanceof com.topodroid.dev.distox2.DistoX310Comm;
      if ( x310 || (mNr > 1 && mDataType == DataType.DATA_CALIB) ) {
        TDUtil.slowDown( TDSetting.mWaitShot );
      }
      for ( int i = mNr; i > 0; --i ) {
        if ( isCancelled() || ! mOperation.laser( Device.LASER_ON, 0, mLister, mDataType, false ) ) return false;
        if ( ! TDUtil.slowDown( TDSetting.mWaitLaser ) ) return false;
        boolean last = (i == 1);
        int toRead = ( last && mLister != null )? (x310 ? mNr : 1) : 0;
        if ( ! mOperation.laser( Device.MEASURE, toRead, ( x310 && ! last )? null : mLister, mDataType, last ) ) return false;
        if ( ! last && ! TDUtil.slowDown( TDSetting.mWaitShot ) ) return false;
      }
      return true;
    } catch ( RuntimeException e ) {
      TDLog.e( "Remote shot: " + e.getMessage() );
      return false;
    }
  }

  @Override
  protected void onProgressUpdate(Integer... progress) { }

  @Override protected void onPostExecute( Boolean success )
  {
    if ( mOperation != null ) mOperation.close(); // MM
    mILister.enableBluetoothButton( true );
    if ( ! Boolean.TRUE.equals( success ) ) TDToast.makeBad( R.string.device_command_failed );
  }

  @Override protected void onCancelled( Boolean success ) { onPostExecute( false ); }
}
