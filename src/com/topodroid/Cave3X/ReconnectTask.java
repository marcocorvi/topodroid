/* @file ReconnectTask.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid Data downlod reconnection task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.ConnectionState;

import android.os.AsyncTask;

class ReconnectTask extends AsyncTask< String, Integer, Integer >
{
  private DataDownloader mDownloader;
  private ReconnectTask  running;
  private int mDataType; // data type, passed to data_douwnloader try_connect()
  private int mDelay = 0;

  ReconnectTask( DataDownloader downloader, int data_type, int delay )
  {
    mDownloader = downloader;
    mDataType = data_type;
    mDelay = delay;
    running = null;
  }

// -------------------------------------------------------------------
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( ! lock() ) return null;
    if ( TDSetting.mAutoReconnect && TDInstance.isContinuousMode() ) {
      while ( mDownloader.needReconnect() ) {
        try {
          if ( mDelay > 0 ) Thread.sleep( mDelay );
          // TDLog.v( "notify disconnected: try reconnect status " + mDownloader.isDownloading() );
          mDownloader.tryConnect( mDataType ); 
        } catch ( InterruptedException e ) { }
      }
    }
    // TDLog.v( "reconnect task exits");
    return 0;
  }

  @Override
  protected void onProgressUpdate( Integer... values)
  {
    super.onProgressUpdate( values );
  }

  @Override
  protected void onPostExecute( Integer res )
  {
    // mDownloader.notifyConnectionStatus( mDownloader.isConnected()? ConnectionState.CONN_CONNECTED : ConnectionState.CONN_WAITING );
    // TDLog.v( "Reconnect Task done with delay " + mDelay );
    unlock();
  }

  private synchronized boolean lock()
  {
    if ( running != null ) return false;
    running = this;
    return true;
  }

  private synchronized void unlock()
  {
    if ( running == this ) running = null;
  }

}

