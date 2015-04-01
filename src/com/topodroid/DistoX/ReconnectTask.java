/* @file ReconnectTask.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

// import android.widget.Toast;
import android.os.AsyncTask;


public class ReconnectTask extends AsyncTask< String, Integer, Integer >
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
    if ( TopoDroidSetting.mAutoReconnect && TopoDroidSetting.mConnectionMode == TopoDroidSetting.CONN_MODE_CONTINUOUS ) {
      while ( mDownloader.mDownload && ! mDownloader.mConnected ) {
        try {
          Thread.sleep( 500 );
          // Log.v("DistoX", "notify disconnected: try reconnect status " + mDataDownloader.mDownload );
          mDownloader.tryConnect( ); 
        } catch ( InterruptedException e ) { }
      }
    }
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
    // if ( res != null ) {
    //   int r = res.intValue();
    // }
    mDownloader.notifyConnectionStatus( mDownloader.mConnected );
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

