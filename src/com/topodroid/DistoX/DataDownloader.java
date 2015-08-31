/** @file DataDownloader.java
 *
 * @author marco corvi
 * @date sept 2014
 *
 * @brief TopoDroid survey shots data downloader (continuous mode)
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import android.bluetooth.BluetoothDevice;

import android.widget.Button;
// import android.widget.Toast;

import android.os.Bundle;

import android.util.Log;

class DataDownloader
{
  private boolean mConnected = false;  // whetehr it is "connected"
  private boolean mDownload  = false;  // whether it is "downloading"

  boolean isConnected() { return mConnected; }
  boolean isDownloading() { return mDownload; }
  boolean needReconnect() { return mDownload && ! mConnected; }
  void setConnected( boolean connected ) { mConnected = connected; }
  void setDownload( boolean download ) { mDownload = download; }

  // int mStatus = 0; // 0 disconnected, 1 connecting, 2 connected
  int getStatus()
  {
    if ( ! mDownload ) return 0;
    if ( ! mConnected ) return 2;
    return 1;
  }

  private Context mContext;
  private TopoDroidApp mApp;
  // private BroadcastReceiver mBTReceiver = null;

  DataDownloader( Context context, TopoDroidApp app )
  {
    mContext = context;
    mApp     = app;
    // mBTReceiver = null;
  }

  void toggleDownload()
  {
    mDownload = ! mDownload;
  }

  void doDataDownload()
  {
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
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "**** download data. status: " + mStatus );
    if ( TopoDroidSetting.mConnectionMode == TopoDroidSetting.CONN_MODE_BATCH ) {
      tryDownloadData( );
    } else { // TopoDroidSetting.mConnectionMode == TopoDroidSetting.CONN_MODE_CONTINUOUS
      if ( TopoDroidSetting.mAutoReconnect ) {
        new ReconnectTask( this ).execute();
      } else {
        tryConnect();
      }
      notifyConnectionStatus( mConnected );
    }
  }

  void stopDownloadData()
  {
    // Log.v("DistoX", "stopDownloadData() connected " + mConnected );
    // if ( ! mConnected ) return;
    if ( TopoDroidSetting.mConnectionMode == TopoDroidSetting.CONN_MODE_CONTINUOUS ) {
      mApp.disconnectComm();
      notifyConnectionStatus( false );
    }
  }

  // called also by ReconnectTask
  void tryConnect()
  {
    // Log.v("DistoX", "tryConnect() download " + mDownload + " connected " + mConnected );
    if ( mApp.mDevice != null && mApp.mBTAdapter.isEnabled() ) {
      mApp.disconnectComm();
      if ( ! mDownload ) {
        mConnected = false;
        return;
      }
      if ( ! mConnected ) {
        mConnected = mApp.connectDevice( mApp.mDevice.mAddress );
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "**** tryConnect status 0 --> " + mStatus );
      } else {
        mConnected = false;
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "**** tryConnect status " + mStatus + " --> 0 " );
      }
    }
  }

  void notifyConnectionStatus( boolean connected )
  {
    mConnected = connected;
    mApp.notifyStatus( );
  }

  private void tryDownloadData( )
  {
    // mSecondLastShotId = mApp.lastShotId( );
    if ( mApp.mDevice != null && mApp.mBTAdapter.isEnabled() ) {
      notifyConnectionStatus( true );
      // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "shot menu DOWNLOAD" );
      new DistoXRefresh( mApp, mApp.mLister ).execute();
    } else {
      mDownload = false;
      notifyConnectionStatus( false );
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: no device selected" );
      if ( mApp.mSID < 0 ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: no survey selected" );
      } else {
        // DistoXDBlock last_blk = mApp.mData.selectLastLegShot( mApp.mSID );
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
    // Log.v("DistoX", "DataDownloader onResume()");
  }

}
