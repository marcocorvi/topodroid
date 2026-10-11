/* @file DataDownloadTask.java
 *
 * @author marco corvi
 * @date feb 2012
 *
 * @brief TopoDroid batch data-download task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.calib.CalibInfo;
import com.topodroid.dev.Device;
import com.topodroid.dev.DeviceOperation;
import com.topodroid.util.TDUtil;
import com.topodroid.util.TDAnalytics;

import java.lang.ref.WeakReference;

// import java.util.ArrayList;

import android.os.AsyncTask;
// import android.os.Handler;

class DataDownloadTask extends AsyncTask< String, Integer, Integer >
{
  private final WeakReference<TopoDroidApp> mApp; // FIXME LEAK
  private final WeakReference<GMActivity> mGMactivity; // can be null
  private DeviceOperation mOperation;
  private final ListerHandler mLister; // FIXME_LISTER
  private int mDataType;

  private static String myDate = null;

  /** cstr
   * @param app       TopoDroid app
   * @param lister    data lister
   * @param data_type packet datatype
   */
  DataDownloadTask( TopoDroidApp app, ListerHandler lister, GMActivity gm_activity, int data_type ) // FIXME_LISTER
  {
    TDLog.v( "DataDownloadTask cstr has app " + ( app != null ) );
    mApp        = new WeakReference<TopoDroidApp>( app );
    mGMactivity = new WeakReference<GMActivity>( gm_activity );
    mLister = lister;
    mDataType = data_type;
    String date = TDUtil.currentDate();
    String today = app.mDData.getValue( "today" );
    if ( ! date.equals( today ) ) {
      app.mDData.setValue( "today", date );
      int dev = TDInstance.deviceType();
      switch ( dev ) {
        case Device.DISTO_CAVWAYX1: TopoDroidApp.updateAnalytic( TDAnalytics.D_CVWY1 ); break;
        case Device.DISTO_XBLE:     TopoDroidApp.updateAnalytic( TDAnalytics.D_XBLE ); break;
        case Device.DISTO_X310:     TopoDroidApp.updateAnalytic( TDAnalytics.D_X310 ); break;
        case Device.DISTO_BRIC5:    TopoDroidApp.updateAnalytic( TDAnalytics.D_BRIC5 ); break;
        case Device.DISTO_BRIC4:    TopoDroidApp.updateAnalytic( TDAnalytics.D_BRIC4 ); break;
        case Device.DISTO_SAP6:     TopoDroidApp.updateAnalytic( TDAnalytics.D_SAP6 ); break;
        case Device.DISTO_DISCOX:   TopoDroidApp.updateAnalytic( TDAnalytics.D_DISCO ); break;
        case Device.DISTO_JEDEYE:   TopoDroidApp.updateAnalytic( TDAnalytics.D_JEDEYE ); break;
        case Device.DISTO_A3:       TopoDroidApp.updateAnalytic( TDAnalytics.D_A3 ); break;
        case Device.DISTO_SAP5:     TopoDroidApp.updateAnalytic( TDAnalytics.D_SAP5 ); break;
        default:                    TopoDroidApp.updateAnalytic( TDAnalytics.D_UNKWN ); 
      }
    }
  }

// -------------------------------------------------------------------
  /** task background execution
   * @param statuses   (unused)
   * @return the number of downloaded packets - ( 0 if there is no app )
   */
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( mOperation == null ) {
      TDLog.v("DataDownloadTask do in background: null op" );
      return null;
    }
    try {
      GMActivity gm = mGMactivity.get();
      TopoDroidApp app = mApp.get();
      if ( gm != null && ! gm.isFinishing() && app != null ) {
        int algo = gm.getAlgo();
        if ( algo == CalibInfo.ALGO_AUTO ) { 
          algo = app.getCalibAlgoFromDevice();
          if ( algo < CalibInfo.ALGO_AUTO ) { // could not get the algo from the device type
            algo = CalibInfo.ALGO_LINEAR; 
          }
          app.updateCalibAlgo( algo );
          gm.setAlgo( algo );
        }
        // app.setGMdownload( true ); // GM_DOWNLOAD
      }
      return app.downloadDataBatch( mOperation, mLister, mDataType );
    } catch ( RuntimeException e ) {
      TDLog.e( "DataDownloadTask " + e.getMessage() );
      return -1;
    }
    // app.setGMdownload( false ); // GM_DOWNLOAD
  }

  @Override
  protected void onPreExecute() // MM
  {
    TopoDroidApp app = mApp.get();
    mOperation = ( app != null )? app.beginDeviceOperation() : null;
    TDLog.v("DataDownloadTask pre-exec has op " + ( mOperation != null ) );
  }

  // @Override
  // protected void onProgressUpdate( Integer... values)
  // {
  //   super.onProgressUpdate( values );
  //   // TDLog.Log( TDLog.LOG_COMM, "onProgressUpdate " + values );
  // }

  /** post-execution: forward the result to the lister and some housekeeping
   * @param res   result of the background execution
   */
  @Override
  protected void onPostExecute( Integer res )
  {
    if ( mOperation == null ) {
      TDLog.v("Data DownloadTask post-exec null op");
      return;
    }
    TDLog.v( "data download task: post execute: res " + res );
    try {
      TopoDroidApp app = mApp.get();
      if ( res != null && mLister != null ) mLister.refreshDisplay( res.intValue(), true );
      if ( app != null ) app.stopDownloading( mLister );
    } finally {
      mOperation.close();
    }
  }

  @Override
  protected void onCancelled( Integer res ) { onPostExecute( res ); }

  // MM 
  // private static DataDownloadTask running = null; // static reference to this class - to lock/unlock

  // /** lock the static reference
  //  * @return true if successful, false if already locked
  //  */
  // private synchronized boolean lock()
  // {
  //   // TDLog.v("DATA " + "data download task lock: running is " + ( (running == null )? "null" : (running == this)? "this" : "other") );
  //   if ( running != null ) return false;
  //   running = this;
  //   return true;
  // }

  // /** unlock the static reference
  //  */
  // private synchronized void unlock()
  // {
  //   // TDLog.v("DATA " + "data download task unlock: running is " + ( (running == null )? "null" : (running == this)? "this" : "other") );
  //   if ( running == this ) running = null;
  // }

}
