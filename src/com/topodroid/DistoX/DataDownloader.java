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
package com.topodroid.DistoX;

// import android.util.Log;

// import java.util.ArrayList;

import android.content.Context;


class DataDownloader
{
  private boolean mConnected = false;  // whetehr it is "connected"
  private boolean mDownload  = false;  // whether it is "downloading"

  boolean isConnected() { return mConnected; }
  boolean isDownloading() { return mDownload; }
  boolean needReconnect() { return mDownload && ! mConnected; }
  void setConnected( boolean connected ) { mConnected = connected; }
  void setDownload( boolean download ) { mDownload = download; }

  // int mStatus = 0; // 0 disconnected, 1 connected, 2 connecting
  int getStatus()
  {
    if ( ! mDownload ) return 0;
    if ( ! mConnected ) {
      if ( TDSetting.mAutoReconnect ) return 2;
      return 0;
    }
    // setDownload( false );
    return 1;
  }

  // private Context mContext; // UNUSED
  private final TopoDroidApp mApp;
  // private BroadcastReceiver mBTReceiver = null;

  DataDownloader( Context context, TopoDroidApp app )
  {
    // mContext = context;
    mApp     = app;
    // mBTReceiver = null;
  }

  void toggleDownload()
  {
    mDownload = ! mDownload;
    // Log.v("DistoX", "toggle download to " + mDownload );
  }

  void doDataDownload()
  {
    // Log.v("DistoX", "do data download " + mDownload + " connected " + mConnected );
    if ( mDownload ) {
      startDownloadData();
    } else {
      stopDownloadData();
    }
  }

  /** called with mDownload == true
   */
  private void startDownloadData( )
  {
    // TDLog.Log( TDLog.LOG_COMM, "**** download data. status: " + mStatus );
    if ( TDSetting.mConnectionMode == TDSetting.CONN_MODE_BATCH ) {
      tryDownloadData( );
    } else if ( TDSetting.mConnectionMode == TDSetting.CONN_MODE_CONTINUOUS ) {
      if ( TDSetting.mAutoReconnect ) {
        TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
        new ReconnectTask( this ).execute();
      } else {
        tryConnect();
      }
      notifyUiThreadConnectionStatus( mConnected );
    } else if ( TDSetting.mConnectionMode == TDSetting.CONN_MODE_MULTI ) {
      tryDownloadData( );
    }
  }

  void stopDownloadData()
  {
    // Log.v("DistoX", "stop Download Data() connected " + mConnected );
    // if ( ! mConnected ) return;
    // if ( TDSetting.isConnectionModeBatch() ) {
      mApp.disconnectComm();
      notifyUiThreadConnectionStatus( false );
    // }
  }

  // called also by ReconnectTask
  void tryConnect()
  {
    // Log.v("DistoX", "try Connect() download " + mDownload + " connected " + mConnected );
    if ( TDInstance.device != null && DeviceUtil.isAdapterEnabled() ) {
      mApp.disconnectComm();
      if ( ! mDownload ) {
        mConnected = false;
        return;
      }
      if ( ! mConnected ) {
        mConnected = mApp.connectDevice( TDInstance.device.mAddress );
        // Log.v( "DistoX", "**** try Connect " + mConnected );
      } else {
        mConnected = false;
        // Log.v( "DistoX", "**** try Connect false ");
      }
    }
  }

  private void notifyUiThreadConnectionStatus( boolean connected )
  {
    mConnected = connected;
    TopoDroidApp.mActivity.runOnUiThread( new Runnable() {
      public void run() {
        mApp.notifyStatus( );
      }
    } );
  }

  // this must be called on UI thread (onPostExecute)
  void notifyConnectionStatus( boolean connected )
  {
    mConnected = connected;
    mApp.notifyStatus( );
  }

  // BATCH ON-DEMAND DOWNLOAD
  // non-private to allow the DistoX select dialog
  private void tryDownloadData( )
  {
    TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
    if ( TDInstance.device != null && DeviceUtil.isAdapterEnabled() ) {
      notifyUiThreadConnectionStatus( true );
      // TDLog.Log( TDLog.LOG_COMM, "shot menu DOWNLOAD" );
      new DataDownloadTask( mApp, mApp.mListerSet, null ).execute();
    } else {
      mDownload = false;
      notifyUiThreadConnectionStatus( false );
      TDLog.Error( "download data: no device selected" );
      if ( TDInstance.sid < 0 ) {
        TDLog.Error( "download data: no survey selected" );
      // } else {
        // DBlock last_blk = mApp.mData.selectLastLegShot( TDInstance.sid );
        // (new ShotNewDialog( mContext, mApp, lister, last_blk, -1L )).show();
      }
    }
  }

  // static boolean mConnectedSave = false;

  void onStop()
  {
    // Log.v("DistoX", "DataDownloader onStop()");
    mDownload = false;
    if ( mConnected ) {
      stopDownloadData();
      mConnected = false;
    }
  }

  void onResume()
  {
    // Log.v("DistoX", "Data Downloader onResume()");
  }

}
