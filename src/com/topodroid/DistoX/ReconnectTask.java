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
package com.topodroid.DistoX;

import com.topodroid.prefs.TDSetting;

// import android.util.Log;

import android.os.AsyncTask;

class ReconnectTask extends AsyncTask< String, Integer, Integer >
{
  private DataDownloader mDownloader;
  private ReconnectTask  running;
  private int mDataType; // data type, passed to data_douwnloader try_connect()

  ReconnectTask( DataDownloader downloader, int data_type )
  {
    mDownloader = downloader;
    mDataType = data_type;
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
          Thread.sleep( 500 );
          // Log.v("DistoX", "notify disconnected: try reconnect status " + mDownloader.isDownloading() );
          mDownloader.tryConnect( mDataType ); 
        } catch ( InterruptedException e ) { }
      }
    }
    // Log.v("DistoX", "reconnect task exits");
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
    // mDownloader.notifyConnectionStatus( mDownloader.isConnected()? DataDownloader.STATUS_ON : DataDownloader.STATUS_WAIT );
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

