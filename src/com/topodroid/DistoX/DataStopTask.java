/* @file DataStopTask.java
 *
 * @author marco corvi
 * @date nov 2020
 *
 * @brief TopoDroid data-download stop task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
// import android.os.Handler;

class DataStopTask extends AsyncTask< String, Void, Void >
{
  private final WeakReference<TopoDroidApp> mApp; // FIXME LEAK
  private static DataStopTask running = null; // static reference to an instance if this class - used to lock/unlock
  private final ILister mLister; 
  private final DataDownloader mDataDownloader;

  /** cstr
   * @param app       TopoDroid app
   * @param lister    data lister
   * @param data_downloader data downloader
   */
  DataStopTask( TopoDroidApp app, ILister lister, DataDownloader data_downloader )
  {
    // TDLog.Error( "Data Download Task cstr" );
    // TDLog.v( "data download task cstr");
    mApp    = new WeakReference<TopoDroidApp>( app );
    mDataDownloader = data_downloader;
    mLister = lister;
  }

// -------------------------------------------------------------------
  /** task background execution
   * @param statuses  (unused)
   */
  @Override
  protected Void doInBackground( String... statuses )
  {
    if ( ! lock() ) return null;
    TopoDroidApp app = mApp.get();
    if ( app != null ) {
      if ( mDataDownloader != null ) {
        if ( mLister != null ) app.unregisterLister( mLister );
        mDataDownloader.onStop();
      }
      app.disconnectRemoteDevice( false );
    }
    unlock();
    return null;
  }

  // @Override
  // protected void onProgressUpdate( Void... values)
  // {
  //   super.onProgressUpdate( values );
  //   // TDLog.Log( TDLog.LOG_COMM, "onProgressUpdate " + values );
  // }

  // /** post-exec - (empty)
  //  */
  // @Override
  // protected void onPostExecute( Void res )
  // {
  // }

  /** lock the static reference
   * @return true if successful, false if it was already locked
   */
  private synchronized boolean lock()
  {
    if ( running != null ) return false;
    running = this;
    return true;
  }

  /** unlock the static reference
   */
  private synchronized void unlock()
  {
    if ( running == this ) running = null;
  }

}
