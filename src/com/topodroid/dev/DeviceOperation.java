/* @file DeviceOperation.java
 *
 * @author muddy mohawk
 * @date oct 2026
 *
 * @brief TopoDroid device operation
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

import com.topodroid.TDX.DataDownloader;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDandroid;
import com.topodroid.TDX.ListerHandler;
import com.topodroid.util.TDLog;

// AutoCloseable: the close method is called automatically when exiting a try-with-resource
//   but there are no try-with-resources in the called codes
public class DeviceOperation implements AutoCloseable
{
  final static boolean MM_SWITCH = true;

  public final TopoDroidApp mApp;
  public final TopoDroidComm mComm;
  public final DataDownloader mDataDownloader;
  public final String mAddress;
  public boolean mFinished;
  public boolean mRemote;
  public final boolean mKeepConnection;

  /** cstr
   * @param app      app
   * @param comm     communication class
   * @param downloader data downloader
   */
  public DeviceOperation( TopoDroidApp app, TopoDroidComm comm, DataDownloader downloader )
  {
    mFinished = false;
    mApp  = app;
    mComm = comm;
    mDataDownloader = downloader;
    mAddress = TDInstance.deviceAddress();
    mKeepConnection = mDataDownloader.isDownloading() && TDInstance.isContinuousMode();
    // mComm.bindDataContext( TDInstance.sid, TDInstance.cid, mAddress, TDInstance.deviceType() ); // MM
  }

  /** laser command
   * @param what    command code
   * @param to_read number of data to read
   * @param lister  data lister (can be null)
   * @param data_type type of data (unused)
   * @param close     whether to close the connect afterward
   * @return true on success
   */ 
  public boolean laser( int what, int to_read, ListerHandler lister, int data_type, boolean close )
  {
    mRemote = true;
    mComm.setRemoteOperation( true );
    if ( what == Device.DEVICE_OFF ) {
      mDataDownloader.setDownloading( false );
    }
    return mComm.setLaser( mAddress, what, to_read, lister, data_type, (close && ! mKeepConnection) );
  }

  /** close the device operation (implements AutoCloseable)
   */
  public void close()
  {
    TDLog.v("Device Op close()");
    synchronized( mApp.mDeviceSwitch )  // MM SWITCH
    {
      if ( mFinished ) return;
      mFinished = true;
      mComm.setRemoteOperation( false );
      /* if ( MM_SWITCH ) */ mApp.mDeviceSwitch.endOperation();  // MM SWITCH-2
    }
    if ( mRemote ) {
      if ( MM_SWITCH ) {
        mApp.notifyCommStatus( mComm, mApp.mListerSet, mComm.isConnected()? ConnectionState.CONN_CONNECTED : ConnectionState.CONN_DISCONNECTED ); // MM
      } else {
        mApp.notifyListerStatus( mApp.mListerSet, mComm.isConnected()? ConnectionState.CONN_CONNECTED : ConnectionState.CONN_DISCONNECTED );
      }
    }
    TDandroid.runOnMainThread( new Runnable() { public void run() {
      if ( MM_SWITCH ) {
        mApp.refreshDeviceUi(); // MM SWITCH
        mApp.processPendingDeviceSwitch(); // MM SWITCH
      }
      if ( TDInstance.isContinuousMode() ) mDataDownloader.requestReconnect( mApp.mListerSet, DataType.DATA_SHOT, 500 );
    } } );
  }

}
