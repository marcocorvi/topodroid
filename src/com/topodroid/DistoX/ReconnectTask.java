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

// import java.lang.ref.WeakReference;

// import android.widget.Toast;
import android.os.AsyncTask;

// import android.util.Log;


class ReconnectTask extends AsyncTask< String, Integer, Integer >
{
  private DataDownloader mDownloader;
  private ReconnectTask  running;

  ReconnectTask( DataDownloader downloader )
  {
    mDownloader = downloader;
    running = null;
  }

// -------------------------------------------------------------------
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( ! lock() ) return null;
    if ( TDSetting.mAutoReconnect && TDSetting.mConnectionMode == TDSetting.CONN_MODE_CONTINUOUS ) {
      while ( mDownloader.needReconnect() ) {
        try {
          Thread.sleep( 500 );
          // Log.v("DistoX", "notify disconnected: try reconnect status " + mDownloader.isDownloading() );
          mDownloader.tryConnect( ); 
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
    // Log.v("DistoX", "reconnect task post-exec");
    // if ( res != null ) {
    //   int r = res.intValue();
    // }
    mDownloader.notifyConnectionStatus( mDownloader.isConnected() );
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

