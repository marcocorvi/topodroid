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
  boolean mConnected = false;

  private Context mContext;
  private TopoDroidApp mApp;
  ArrayList<ILister> mLister; 
  private BroadcastReceiver mBTReceiver = null;

  int status; // 0 disconnected, 1 on-demand, 2 continuous

  DataDownloader( Context context, TopoDroidApp app ) // , ILister lister )
  {
    mContext = context;
    mApp     = app;
    mLister  = new ArrayList<ILister>();
    mBTReceiver = null;
  }

  void registerLister( ILister lister )
  {
    for ( ILister l : mLister ) {
      if ( l == lister ) return; // already registered
    }
    mLister.add( lister );
  }

  void unregisterLister( ILister lister )
  {
    mLister.remove( lister );
  }

  private void setConnectionStatus( boolean connected )
  {
    for ( ILister lister : mLister ) {
      lister.setConnectionStatus( connected );
    }
  }

  /** 
   */
  public void downloadData( )
  {
    if ( mBTReceiver == null ) registerBTreceiver();
    if ( TopoDroidSetting.mConnectionMode == TopoDroidSetting.CONN_MODE_BATCH ) {
      tryDownloadData( );
    } else {
      tryConnect();
    }
    if ( ! mConnected ) resetReceiver();
  }

  public void disconnect()
  {
    // Log.v("DistoX", "disconnect() connected was " + mConnected );
    if ( ! mConnected ) return;
    if ( TopoDroidSetting.mConnectionMode == TopoDroidSetting.CONN_MODE_CONTINUOUS ) {
      mApp.disconnect();
      resetReceiver();
      // mConnected = false;
      setConnectionStatus( mConnected );
    }
  }

  private void tryConnect()
  {
    if ( mApp.mDevice != null && mApp.mBTAdapter.isEnabled() ) {
      if ( mConnected == false ) {
        mApp.disconnect();
        mConnected = mApp.connect( mApp.mDevice.mAddress, mLister );
        // Log.v("DistoX", "tryConnect mConnected was false: connect --> " + mConnected );
      } else {
        mApp.disconnect( );
        resetReceiver();
        // mConnected = false;
        // Log.v("DistoX", "tryConnect mConnected was true: diconnect --> " + mConnected );
      }
      setConnectionStatus( mConnected );
    }
  }

  private void tryDownloadData( )
  {
    // mSecondLastShotId = mApp.lastShotId( );
    if ( mApp.mDevice != null && mApp.mBTAdapter.isEnabled() ) {
      setConnectionStatus( true );
      // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "shot menu DOWNLOAD" );
      new DistoXRefresh( mApp, mLister ).execute();
    } else {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: no device selected" );
      if ( mApp.mSID < 0 ) {
        // Toast.makeText( mContext, R.string.no_survey, Toast.LENGTH_SHORT ).show();
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: no survey selected" );
      } else {
        // DistoXDBlock last_blk = mApp.mData.selectLastLegShot( mApp.mSID );
        // (new ShotNewDialog( mContext, mApp, lister, last_blk, -1L )).show();
      }
    }
  }


  public void resetReceiver()
  {
    // Log.v("DistoX", "reset receiver() connected " + mConnected + " BT-receiver " + ((mBTReceiver!=null)? "not null":"null"));
    if ( mBTReceiver != null ) {
      mContext.unregisterReceiver( mBTReceiver );
      mBTReceiver = null;
    }
    mConnected = false;
  }

  static boolean mConnectedSave = false;

  void onPause()
  {
    // Log.v("DistoX", "Data Downloader onPause() connected " + mConnected + ( (mBTReceiver!=null)? "with BT receiver":""));
    mConnectedSave = mConnected;
    if ( mLister.size() == 0 ) {
      resetReceiver();
    }
  }

  void onResume()
  {
    // Log.v("DistoX", "Data Downloader onResume() connected-save " + mConnectedSave + " listers " + mLister.size() );
    if ( mConnectedSave ) {
      if ( mLister.size() > 0 ) mLister.get(0).notifyDisconnected();
    }
  }

  // called only if mBTReceiver == null
  private void registerBTreceiver()
  {
    mConnected  = false;
    mBTReceiver = new BroadcastReceiver() 
    {
      @Override
      public void onReceive( Context ctx, Intent data )
      {
        String action = data.getAction();
        // TopoDroidLog.Log( TopoDroidLog.LOG_BT, "onReceive action " + action );
        if ( BluetoothDevice.ACTION_ACL_CONNECTED.equals( action ) ) {
          // Log.v("DistoX", "DataDownloader ACL_CONNECTED");
          setConnectionStatus( true );
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals( action ) ) {
          // Log.v("DistoX", "DataDownloader ACL_DISCONNECT_REQUESTED");
          setConnectionStatus( false );
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECTED.equals( action ) ) {
          // Bundle extra = data.getExtras();
          // String device = extra.getString( BluetoothDevice.EXTRA_DEVICE );
          // Log.v("DistoX", "DataDownloader ACL_DISCONNECTED");
          setConnectionStatus( false );
          if ( mLister.size() > 0 ) mLister.get(0).notifyDisconnected();
        }
      }
    };

    IntentFilter connectedFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_CONNECTED );
    IntentFilter disconnectRequestFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED );
    IntentFilter disconnectedFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECTED );

    mContext.registerReceiver( mBTReceiver, connectedFilter );
    mContext.registerReceiver( mBTReceiver, disconnectRequestFilter );
    mContext.registerReceiver( mBTReceiver, disconnectedFilter );
  }

}
