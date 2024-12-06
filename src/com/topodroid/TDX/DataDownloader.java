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

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.DeviceUtil;

// import java.util.ArrayList;

import android.content.Context;


public class DataDownloader
{
  private final static String TAG = "DOWNLOAD ";
  private final static boolean LOG = false;
  // int mStatus = 0; // 0 disconnected, 1 connected, 2 connecting

  // private Context mContext; // UNUSED
  // private BroadcastReceiver mBTReceiver = null;
  private final TopoDroidApp mApp;
  private ListerHandler mLister = null;

  private int mConnected = ConnectionState.CONN_DISCONNECTED;  // whether it is "connected": 0 unconnected, 1 connecting, 2 connected
  private boolean mDownloading  = false;  // whether it is "downloading"

  // boolean isConnected() { return mConnected == ConnectionState.CONN_CONNECTED; }

  /** @return true if it is downloading
   */
  boolean isDownloading() { return mDownloading; }

  /** @return true if it needs to re-connect, ie, it is downloading but not CONNECTED
   */
  boolean needReconnect() { return mDownloading && mConnected != ConnectionState.CONN_CONNECTED; }

  /** set the "connection" status
   * @param connected   connection status: DISCONNECTED, WAITING, or CONNECTED
   */
  public void setConnected( int connected ) { mConnected = connected; }

  /** set the "download" flag
   * @param downloading   value of the downloading flag
   */
  public void setDownloading( boolean downloading ) 
  { 
    if ( LOG ) TDLog.v( TAG +"set downloading: " + downloading );
    mDownloading = downloading; 
  }

  // /** reset the downloader: not downloading, not connected - UNUSED
  //  * @note used only bt DistoXBLEComm
  //  */
  // void reset()
  // {
  //   mDownloading = false;
  //   mConnected = ConnectionState.CONN_DISCONNECTED;
  // }

  /** update the "connected" state
   * @param on_off   whether the downloader is connected
   */
  public void updateConnected( boolean on_off )
  {
    if ( on_off ) {
      mConnected = ConnectionState.CONN_CONNECTED;
    } else {
      if ( mDownloading ) {
        // mConnected = ( TDSetting.isConnectionModeContinuous() && TDSetting.mAutoReconnect )? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED;
        mConnected = ConnectionState.CONN_WAITING; // auto-reconnect is always true
      } else {
        mConnected = ConnectionState.CONN_DISCONNECTED;
      }
    }
  }

  /** @return the status of the downloader
   */
  int getStatus()
  {
    if ( ! mDownloading ) return ConnectionState.CONN_DISCONNECTED;
    return mConnected;
    // if ( mConnected == 0 ) {
    //   if ( TDSetting.mAutoReconnect ) return ConnectionState.CONN_WAITING;
    //   return ConnectionState.CONN_DISCONNECTED;
    // } else if ( mConnected == 1 ) {
    //   return ConnectionState.CONN_WAITING;
    // } // else mConnected == 2
    // return ConnectionState.CONN_CONNECTED;
  }

  /** cstr
   * @param context   context (unused)
   * @param app       application
   */
  DataDownloader( Context context, TopoDroidApp app )
  {
    // mContext = context;
    mApp     = app;
    // mBTReceiver = null;
  }

  /** toggle the "download" status
   * @return the new download status
   */
  public boolean toggleDownloading()
  {
    mDownloading = ! mDownloading;
    mConnected = mDownloading ? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED;
    if ( LOG ) TDLog.v( TAG + "toggle to download " + mDownloading + " connected " + mConnected );
    return mDownloading;
  }

  /** download data
   * @param lister       ...
   * @param data_type    expected type of the data
   */
  public void doDataDownload( ListerHandler lister, int data_type )
  {
    if ( LOG ) TDLog.v( TAG +"do Data Download() - downloading " + mDownloading + " connected " + mConnected + " data type " + data_type );
    if ( mDownloading ) {
      mLister = lister;
      startDownloadData( lister, data_type );
    } else {
      stopDownloadData( lister );
    }
  }

  /** start to download data
   * @param lister      data lister
   * @param data_type   expected type of the data
   * @note called with mDownloading == true
   */
  private void startDownloadData( ListerHandler lister, int data_type )
  {
    // if ( LOG ) TDLog.v( TAG + "start Download Data. status: " + mStatus );
    if ( TDInstance.isContinuousMode() ) {
      if ( TDSetting.mAutoReconnect ) {
        if ( LOG ) TDLog.v( TAG + "start download continuous - autoreconnect ");
        TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
        new ReconnectTask( this, lister, data_type, 0 ).execute();
      } else {
        if ( LOG ) TDLog.v( TAG + "start download continuous - try connect ");
        notifyConnectionStatus( lister, ConnectionState.CONN_WAITING );
        tryConnect( lister, data_type );
      }
    } else if ( TDSetting.isConnectionModeBatch() ) {
      if ( LOG ) TDLog.v( TAG + "start download batch");
      tryDownloadData( lister, data_type );
    } else if ( TDSetting.isConnectionModeMulti() ) {
      if ( LOG ) TDLog.v( TAG + "start download multi-batch");
      tryDownloadData( lister, data_type );
    }
  }

  /** stop to download data
   * @param lister      data lister
   */
  void stopDownloadData( ListerHandler lister )
  {
    // if ( LOG ) TDLog.v( TAG + "stop download data - connected " + mConnected );
    // if ( mConnected == ConnectionState.CONN_DISCONNECTED ) return;
    // if ( TDSetting.isConnectionModeBatch() ) {
      if ( mApp.disconnectComm() ) {
        notifyConnectionStatus( lister, ConnectionState.CONN_DISCONNECTED );
      }
      // mConnected = ConnectionState.CONN_DISCONNECTED; // 20221103
    // }
  }

  /** try to connect to data device
   * @param data_type    expected type of the data
   * @note called also by ReconnectTask
   */
  void tryConnect( ListerHandler lister, int data_type )
  {
    if ( LOG ) TDLog.v( TAG + "try connect - download " + mDownloading + " connected " + mConnected );
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

  /** set the "connected" status and notify the UI 
   * @param connected  new connected status
   */
  private void notifyUiThreadConnectionStatus( ListerHandler lister, int connected )
  {
    // if ( LOG ) TDLog.v( TAG + "notify UI thread - connected " + connected );
    mConnected = connected;
    mApp.notifyListerStatus( lister, getStatus() ); // this is run on UI thread
  }

  /** set the "connected" status and notify the UI 
   * @param connected  new connected status
   * @note this must be called on UI thread (onPostExecute)
   */
  void notifyConnectionStatus( ListerHandler lister, int connected )
  {
    mConnected = connected;
    // if ( LOG ) TDLog.v( TAG + "notify this thread connected " + connected + " status " + getStatus() );
    mApp.notifyListerStatus( lister, getStatus() ); // this is run on UI thread
  }

  /** batch on-demand download
   * @param lister      ...
   * @param data_type   packet datatype
   * @note the listers are taken from the app lister-set
   */
  private void tryDownloadData( ListerHandler lister, int data_type )
  {
    TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
    if ( TDInstance.getDeviceA() != null && DeviceUtil.isAdapterEnabled() ) {
      notifyConnectionStatus( lister, ConnectionState.CONN_WAITING );
      if ( LOG ) TDLog.v( TAG + "try download data() - type " + data_type );
      // new DataDownloadTask( mApp, mApp.mListerSet, null, data_type ).execute();
      new DataDownloadTask( mApp, lister, null, data_type ).execute();
    } else {
      mDownloading = false;
      notifyConnectionStatus( lister, ConnectionState.CONN_DISCONNECTED );
      TDLog.e( TAG + "try download data: no device selected - set downloading false" );
      // if ( TDInstance.sid < 0 ) {
      //   TDLog.e( TAG + "no survey selected" );
      // // } else {
      //   // DBlock last_blk = mApp.mData.selectLastLegShot( TDInstance.sid );
      //   // (new ShotNewDialog( mContext, mApp, lister, last_blk, -1L )).show();
      // }
    }
  }

  // static boolean mConnectedSave = false;

  /** lifecycle: on stop
   */
  void onStop()
  {
    mDownloading = false;
    if ( mConnected  > ConnectionState.CONN_DISCONNECTED ) { // mConnected == ConnectionState.CONN_CONNECTED || mConnected == ConnectionState.CONN_WAITING
      stopDownloadData( mLister );
      mConnected = ConnectionState.CONN_DISCONNECTED; // 20221103 already in stopDownloadData()
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
