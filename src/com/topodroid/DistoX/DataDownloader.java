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

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.DeviceUtil;

import android.util.Log;

// import java.util.ArrayList;

import android.content.Context;


public class DataDownloader
{
  // int mStatus = 0; // 0 disconnected, 1 connected, 2 connecting
  public final static int STATUS_OFF  = 0;
  public final static int STATUS_ON   = 1;
  public final static int STATUS_WAIT = 2;

  private int mConnected = STATUS_OFF;  // whetehr it is "connected": 0 unconnected, 1 connecting, 2 connected
  private boolean mDownload  = false;  // whether it is "downloading"

  boolean isConnected() { return mConnected == STATUS_ON; }
  boolean isDownloading() { return mDownload; }
  boolean needReconnect() { return mDownload && mConnected != STATUS_ON; }
  public void setConnected( int connected ) { mConnected = connected; }
  void setDownload( boolean download ) { mDownload = download; }

  public void updateConnected( boolean on_off )
  {
    if ( on_off ) {
      mConnected = STATUS_ON;
    } else {
      if ( mDownload ) {
        // mConnected = ( TDSetting.isConnectionModeContinuous() && TDSetting.mAutoReconnect )? STATUS_WAIT : STATUS_OFF;
        mConnected = STATUS_WAIT; // auto-reconnect is always true
      } else {
        mConnected = STATUS_OFF;
      }
    }
  }

  int getStatus()
  {
    if ( ! mDownload ) return STATUS_OFF;
    return mConnected;
    // if ( mConnected == 0 ) {
    //   if ( TDSetting.mAutoReconnect ) return STATUS_WAIT;
    //   return STATUS_OFF;
    // } else if ( mConnected == 1 ) {
    //   return STATUS_WAIT;
    // } // else mConnected == 2
    // return STATUS_ON;
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

  boolean toggleDownload()
  {
    mDownload = ! mDownload;
    return mDownload;
    // Log.v("DistoX", "toggle download to " + mDownload );
  }

  void doDataDownload( int data_type )
  {
    Log.v("DistoX-BLE-DD", "do data download " + mDownload + " connected " + mConnected );
    if ( mDownload ) {
      startDownloadData( data_type );
    } else {
      stopDownloadData();
    }
  }

  /** called with mDownload == true
   */
  private void startDownloadData( int data_type )
  {
    // TDLog.Log( TDLog.LOG_COMM, "**** download data. status: " + mStatus );
    if ( TDInstance.isContinuousMode() ) {
      Log.v("DistoX-BLE-DD", "start download continuous. autoreconnect " + TDSetting.mAutoReconnect );
      if ( TDSetting.mAutoReconnect ) {
        TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
        new ReconnectTask( this, data_type ).execute();
      } else {
        notifyConnectionStatus( STATUS_WAIT );
        tryConnect( data_type );
      }
    } else if ( TDSetting.isConnectionModeBatch() ) {
      tryDownloadData( data_type );
    } else if ( TDSetting.isConnectionModeMulti() ) {
      tryDownloadData( data_type );
    }
  }

  void stopDownloadData()
  {
    // Log.v("DistoX", "stop Download Data() connected " + mConnected );
    // if ( ! mConnected ) return;
    // if ( TDSetting.isConnectionModeBatch() ) {
      mApp.disconnectComm();
      notifyConnectionStatus( STATUS_OFF );
    // }
  }

  // called also by ReconnectTask
  // @param data_type ...
  void tryConnect( int data_type )
  {
    Log.v("DistoX-BLE-DD", "try Connect() download " + mDownload + " connected " + mConnected );
    if ( TDInstance.deviceA != null && DeviceUtil.isAdapterEnabled() ) {
      mApp.disconnectComm();
      if ( ! mDownload ) {
        mConnected = STATUS_OFF;
        return;
      }
      if ( mConnected == STATUS_ON ) {
        mConnected = STATUS_OFF;
        // Log.v( "DistoXDOWN", "**** toggle: connected " + mConnected );
      } else {
        // if this runs the RFcomm thread, it returns true
        int connected = TDSetting.mAutoReconnect ? STATUS_WAIT : STATUS_OFF;

        if ( mApp.connectDevice( TDInstance.deviceAddress(), data_type ) ) {
          connected = STATUS_ON;
        }
        Log.v( "DistoX-BLE-DD", "**** connect device returns " + connected );
        if ( TDInstance.isDeviceSap() && connected == STATUS_ON ) {
          mConnected = connected;
          mApp.notifyStatus( STATUS_WAIT );
        } else {
          notifyUiThreadConnectionStatus( connected );
        }
      }
    }
  }

  private void notifyUiThreadConnectionStatus( int connected )
  {
    mConnected = connected;
    mApp.notifyStatus( getStatus() ); // this is run on UI thread
  }

  // this must be called on UI thread (onPostExecute)
  void notifyConnectionStatus( int connected )
  {
    mConnected = connected;
    mApp.notifyStatus( getStatus() ); // this is run on UI thread
  }

  // BATCH ON-DEMAND DOWNLOAD
  // non-private to allow the DistoX select dialog
  // @param data_type   packet datatype
  private void tryDownloadData( int data_type )
  {
    TDInstance.secondLastShotId = TopoDroidApp.lastShotId( ); // FIXME-LATEST
    if ( TDInstance.deviceA != null && DeviceUtil.isAdapterEnabled() ) {
      notifyConnectionStatus( STATUS_WAIT );
      // TDLog.Log( TDLog.LOG_COMM, "shot menu DOWNLOAD" );
      // Log.v( "DistoX-BLE-DD", "try download type " + data_type );
      new DataDownloadTask( mApp, mApp.mListerSet, null, data_type ).execute();
    } else {
      mDownload = false;
      notifyConnectionStatus( STATUS_OFF );
      TDLog.Error( "download data: no device selected" );
      // if ( TDInstance.sid < 0 ) {
      //   TDLog.Error( "download data: no survey selected" );
      // // } else {
      //   // DBlock last_blk = mApp.mData.selectLastLegShot( TDInstance.sid );
      //   // (new ShotNewDialog( mContext, mApp, lister, last_blk, -1L )).show();
      // }
    }
  }

  // static boolean mConnectedSave = false;

  void onStop()
  {
    // Log.v("DistoX", "DataDownloader onStop()");
    mDownload = false;
    if ( mConnected  > STATUS_OFF ) { // mConnected == STATUS_ON || mConnected == STATUS_WAIT
      stopDownloadData();
      mConnected = STATUS_OFF;
    }
  }

  void onResume()
  {
    // Log.v("DistoX", "Data Downloader onResume()");
  }

}
