/* @file DataDownloader.java
 *
 * @author marco corvi
 * @date sept 2014
 *
 * @brief TopoDroid survey shots data downloader (continuous mode)
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.DeviceUtil;
import com.topodroid.dev.DataType;
import com.topodroid.dev.DeviceOperation;

// import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;


public class DataDownloader
{
  final static boolean MM = false; // package
  private final static String TAG = "DOWNLOAD ";
  private final static boolean LOG = false;
  // int mStatus = 0; // 0 disconnected, 1 connected, 2 connecting

  private final TopoDroidApp mApp;
  private ListerHandler mLister = null;

  private volatile int mConnected = ConnectionState.CONN_DISCONNECTED;  // whether it is "connected": 0 unconnected, 1 connecting, 2 connected
  private volatile boolean mDownloading  = false;  // whether it is "downloading"

  private ReconnectTask mReconnectTask = null;
  private volatile long mGeneration; // MM RECONNECT add
 
  // boolean isConnected() { return mConnected == ConnectionState.CONN_CONNECTED; }

  /** cstr
   * @param context context (unused)
   * @param app     app
   */
  DataDownloader( Context context, TopoDroidApp app ) 
  {
    mApp = app;
  }

  /** @return true if it is downloading
   */
  public boolean isDownloading() { return mDownloading; }

  /** @return true if it needs to re-connect, ie, it is downloading but not CONNECTED
   */
  boolean needReconnect() 
  { 
    if ( LOG ) TDLog.v( TAG +"need reconnect: doenloading " + mDownloading + " connected " + mConnected );
    return mDownloading && mConnected != ConnectionState.CONN_CONNECTED;
  }

  /** set the "connection" status
   * @param connected   connection status: DISCONNECTED, WAITING, or CONNECTED
   */
  public void setConnected( int connected ) { mConnected = connected; }

  /** set the "download" flag
   * @param downloading   value of the downloading flag
   */
  public void setDownloading( boolean downloading ) 
  { 
    // if ( LOG ) TDLog.v( TAG +"set downloading: " + downloading );
    mDownloading = downloading; 
    if ( MM && ! downloading ) mConnected = ConnectionState.CONN_DISCONNECTED; // MM RECONNECT add
  }

  /** @return the generation counter
   */
  long generation() { return mGeneration; } // MM RECONNECT add

  /** @return true if the given generation is the current generation and i am downloading
   * @param generation    input generation
   */
  boolean isCurrent( long generation ) { return generation == mGeneration && mDownloading; } // MM RECONNECT add
   
  /** update the "connected" state
   * @param connected   whether the downloader is connected
   */
  public void updateConnected( boolean connected )
  {
    mConnected = connected ? ConnectionState.CONN_CONNECTED 
                        : mDownloading ? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED; 
  }

  /** @return the status of the downloader
   */
  int getStatus()
  {
    return mDownloading ? mConnected : ConnectionState.CONN_DISCONNECTED;
  }

  /** toggle the "download" status
   * @return the new download status
   */
  public boolean toggleDownloading()
  {
    if ( MM && mApp.isDeviceOperationBusy( ) ) return mDownloading; // MM RECONNECT add
    mDownloading = ! mDownloading;
    mConnected = mDownloading ? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED;
    if ( LOG ) TDLog.v( TAG + " toggle downloading " + mDownloading + " connected " + mConnected );
    if ( MM && ! mDownloading ) cancelReconnect(); // MM RECONNECT add
    return mDownloading;
  }

  /** download data: either start or stop data-download
   * @param lister       ...
   * @param data_type    expected type of the data
   */
  public void doDataDownload( ListerHandler lister, int data_type )
  {
    if ( LOG ) TDLog.v( TAG + " do Data Download() - downloading " + mDownloading + " connected " + mConnected + " data type " + data_type );
    if ( MM ) {
      if ( mApp.isDeviceOperationBusy( ) ) return; // MM RECONNECT replace body with
      mLister = lister;
      if ( ! mDownloading ) { stopDownloadData( lister ); return; }
      if ( TDInstance.isContinuousMode() ) {
        TDInstance.secondLastShotId = TopoDroidApp.lastShotId();
        requestReconnect( lister, data_type, 0 );
      } else {
        if ( TDInstance.getDeviceA() != null && DeviceUtil.isAdapterEnabled() ) {
          notifyConnectionStatus( lister, ConnectionState.CONN_WAITING );
          new DataDownloadTask( mApp, lister, null, data_type ).executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR );
        } else {
          connectionFailed( lister, mGeneration );
        }
      }
    } else { // not-MM
      if ( mDownloading ) {
        mLister = lister;
        startDownloadData( lister, data_type );
      } else {
        stopDownloadData( lister );
      }
    }
  }

  /** start to download data
   * @param lister      data lister
   * @param data_type   expected type of the data
   * @note called with mDownloading == true
   */
  private void startDownloadData( ListerHandler lister, int data_type ) // non-MM
  {
    TDLog.v( "start Download Data. mode continuous " + TDInstance.isContinuousMode() );
    if ( TDInstance.isContinuousMode() ) {
      if ( TDSetting.mAutoReconnect ) {
        if ( LOG ) TDLog.v( TAG + "start download continuous - autoreconnect ");
        TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
        requestReconnect( lister, data_type, 0 );
      } else {
        if ( LOG ) TDLog.v( TAG + "start download continuous - try connect ");
        notifyConnectionStatus( lister, ConnectionState.CONN_WAITING );
        tryConnect( lister, data_type, mGeneration ); // MM RECONNECT
      }
    } else if ( TDSetting.isConnectionModeBatch() ) {
      TDLog.v( "start download batch");
      tryDownloadData( lister, data_type );
    } else if ( TDSetting.isConnectionModeMulti() ) {
      TDLog.v( "start download multi-batch");
      tryDownloadData( lister, data_type );
    } else { // MM RECONNECT
      connectionFailed( lister, mGeneration );
    }
  }
  
  /** stop to download data
   * @param lister      data lister
   */
  void stopDownloadData( ListerHandler lister ) // non-MM
  {
    // if ( LOG ) TDLog.v( TAG + "stop download data - connected " + mConnected );
    if ( MM ) cancelReconnect(); // MM RECONNECT add
    if ( mApp.disconnectComm() ) {
      notifyConnectionStatus( lister, ConnectionState.CONN_DISCONNECTED );
    }
  }

  /** handle a reconnection request
   * @param lister     data lister
   * @param data_type  type of data
   * @param delay      reconnection delay
   */
  synchronized public void requestReconnect( ListerHandler lister, int data_type, int delay ) // MM RECONNCET new
  {
    if ( ! needReconnect() || /* mApp.isDeviceOperationBusyToast() || */ mReconnectTask != null ) {
      TDLog.v("request Reconnect no need or already reconnecting");
      return;
    }
    if ( MM && mApp.isDeviceOperationBusy( ) ) {
      TDLog.v("request Reconnect op busy");
      return;
    }
    TDLog.v("request Reconnect");
    mReconnectTask = new ReconnectTask( this, lister, data_type, delay );
    mReconnectTask.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR );
  }
  
  /** handle notification that a reconnect task has finished 
   * @param task   reconnection task that finished
   */
  synchronized void reconnectFinished( ReconnectTask task ) // MM RECONNCET new
  {
    if ( mReconnectTask == task ) mReconnectTask = null;
  }
  
  /** cancel the reconnection task (if any)
   */
  private synchronized void cancelReconnect() // MM RECONNCET new
  {
    ++mGeneration;
    if ( mReconnectTask != null ) mReconnectTask.cancel( true );
    mReconnectTask = null;
  }

  // /** Invalidate retries, but let accepted packets drain under the original context. 
  //  */
  void prepareForDeviceSwitch() { cancelReconnect(); } // MM SWITCH new


  /** try to connect to data device
   * @param lister       data lister
   * @param data_type    expected type of the data
   * @param generation   reconnect generation
   * @note called also by ReconnectTask
   */
  void tryConnect( ListerHandler lister, int data_type, long generation )
  {
    if ( ! isCurrent( generation ) ) { // MM RECONNECT add
      TDLog.v("try connect with " + generation + " while " + mGeneration );
      return;
    }
    if ( LOG ) TDLog.v( TAG + "try connect - download " + mDownloading + " connected " + mConnected );
    if ( MM ) {
      DeviceOperation operation = mApp.beginDeviceOperation();
      if ( operation == null ) {
        if ( ! mApp.isDeviceOperationBusy( ) ) connectionFailed( lister, generation );
        return;
      }
      try {
        if ( ! isCurrent( generation ) ) return;
        if ( ! DeviceUtil.isAdapterEnabled() ) { connectionFailed( lister, generation ); return; }
        if ( operation.mComm.isConnected() ) {
          mApp.notifyCommStatus( operation.mComm, lister, ConnectionState.CONN_CONNECTED );
          return;
        }
        notifyConnectionStatus( lister, ConnectionState.CONN_WAITING );
        boolean accepted = operation.mComm.connectDevice( operation.mAddress, lister, data_type, 10 );
        if ( accepted && TDInstance.isDeviceBLE() ) {
          long deadline = SystemClock.elapsedRealtime() + 10000;
          while ( isCurrent( generation ) && ! operation.mComm.isConnected() && SystemClock.elapsedRealtime() < deadline ) {
            try { Thread.sleep( 100 ); }
            catch ( InterruptedException e ) { Thread.currentThread().interrupt(); return; }
          }
          accepted = operation.mComm.isConnected();
        }
        if ( ! isCurrent( generation ) ) return;
        if ( accepted ) mApp.notifyCommStatus( operation.mComm, lister, ConnectionState.CONN_CONNECTED );
        else {
          operation.mComm.disconnectDevice();
          connectionFailed( lister, generation );
        }
      } catch ( RuntimeException e ) {
        TDLog.e( "Device connection: " + e.getMessage() );
        operation.mComm.disconnectDevice();
        connectionFailed( lister, generation );
      } finally {
        try { if ( ! isCurrent( generation ) ) operation.mComm.disconnectDevice(); }
        finally { operation.close(); }
      }
    } else { // not-MM
      if ( TDInstance.getDeviceA() != null && DeviceUtil.isAdapterEnabled() ) {
        mApp.disconnectComm();
        if ( ! mDownloading ) {
          mConnected = ConnectionState.CONN_DISCONNECTED;
          return;
        }
        if ( mConnected == ConnectionState.CONN_CONNECTED ) {
          mConnected = ConnectionState.CONN_DISCONNECTED;
          // if ( LOG ) TDLog.v( TAG + "toggle - connected " + mConnected );
        } else {
          // if this runs the RFcomm thread, it returns true
          int connected = TDSetting.mAutoReconnect ? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED;
          int timeout   = 100;
          if ( mApp.connectDevice( lister, TDInstance.deviceAddress(), data_type, timeout ) ) {
            connected = ConnectionState.CONN_CONNECTED;
          }
          mLister = lister;
          // if ( LOG ) TDLog.v( TAG + "connect device returns " + connected );
          if ( TDInstance.isDeviceBLE() && connected == ConnectionState.CONN_CONNECTED ) {
            // if ( LOG ) TDLog.v( TAG + "notify - connected " + connected );
            mConnected = connected;
            mApp.notifyListerStatus( lister, ConnectionState.CONN_WAITING );
          } else {
            notifyUiThreadConnectionStatus( lister, connected );
          }
        }
      }
    }
  }

  /** set the "connected" status and notify the UI 
   * @param connected  new connected status
   */
  private void notifyUiThreadConnectionStatus( ListerHandler lister, int connected ) // non-MM
  {
    // if ( LOG ) TDLog.v( TAG + "notify UI thread - connected " + connected );
    mConnected = connected;
    mApp.notifyListerStatus( lister, getStatus() ); // this is run on UI thread
  }
  
  /** handle a failure of connection
   * @param lister data lister
   * @param generation ...
   */
  private void connectionFailed( ListerHandler lister, long generation ) // MM RECONNECT new
  {
    if ( generation != mGeneration ) return;
    mDownloading = false;
    notifyConnectionStatus( lister, ConnectionState.CONN_DISCONNECTED );
    TDandroid.runOnMainThread(
      new Runnable() { public void run() {
        if ( generation == mGeneration ) TDToast.makeBad( R.string.device_connection_failed );
      }
    } );
  }

  /** set the "connected" status and notify the UI 
   * @param connected  new connected status
   * @note this must be called on UI thread (onPostExecute)
   */
  void notifyConnectionStatus( ListerHandler lister, int connected )
  {
    mConnected = connected;
    // if ( LOG ) TDLog.v( TAG + "notify this thread connected " + connected + " status " + getStatus() );
    if ( MM ) {
      final long generation = mGeneration;
      TDandroid.runOnMainThread( new Runnable() { public void run() {
        if ( generation == mGeneration && lister != null ) lister.setConnectionStatus( getStatus() );
      } } );
    } else { // non-MM
      mApp.notifyListerStatus( lister, getStatus() ); // this is run on UI thread
    }
  }

  /** batch on-demand download
   * @param lister      ...
   * @param data_type   packet datatype
   * @note the listers are taken from the app lister-set
   */
  private void tryDownloadData( ListerHandler lister, int data_type ) // non-MM
  {
    // if ( data_type != DataType.DATA_CALIB ) { // 20250118 not neceessary ? this is done by TopoDroidApp in downloadDataBatch
    //   TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
    // }
    if ( TDInstance.getDeviceA() != null && DeviceUtil.isAdapterEnabled() ) {
      notifyConnectionStatus( lister, ConnectionState.CONN_WAITING );
      TDLog.v( "try download data() - type " + data_type );
      // new DataDownloadTask( mApp, mApp.mListerSet, null, data_type ).execute();
      new DataDownloadTask( mApp, lister, null, data_type ).execute();
    } else {
      mDownloading = false;
      notifyConnectionStatus( lister, ConnectionState.CONN_DISCONNECTED );
      TDLog.e( "try download data: no device selected - set downloading false" );
      // if ( TDInstance.sid < 0 ) {
      //   TDLog.e( TAG + "no survey selected" );
      // // } else {
      //   // DBlock last_blk = mApp.mData.selectLastLegShot( TDInstance.sid );
      //   // (new ShotNewDialog( mContext, mActivity, mApp, lister, last_blk, -1L )).show(); // FIXME need the activity
      // }
    }
  }

  // static boolean mConnectedSave = false;

  /** lifecycle: on stop
   */
  void onStop()
  {    
	if ( MM ) {
	  mApp.cancelPendingDeviceSwitch();
	  mDownloading = false;
      stopDownloadData( mLister );
	} else { // non-MM
	  mDownloading = false;
      if ( mConnected  > ConnectionState.CONN_DISCONNECTED ) { // mConnected == ConnectionState.CONN_CONNECTED || mConnected == ConnectionState.CONN_WAITING
        stopDownloadData( mLister );
        mConnected = ConnectionState.CONN_DISCONNECTED; // 20221103 already in stopDownloadData()
      }
	}
    // if ( LOG ) TDLog.v( TAG + "onStop() - connected " + mConnected + " set downloadung false" );
  }

  /** lifecycle: on resume (empty method)
   */
  void onResume()
  {
    // if ( LOG ) TDLog.v( TAG + "onResume()");
  }

}
