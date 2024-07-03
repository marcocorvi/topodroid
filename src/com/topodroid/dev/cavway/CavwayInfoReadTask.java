/* @file DistoCavwayInfoReadTask.java
 *
 * @author Siwei Tian
 * @date Jul 2022
 *
 * @brief TopoDroid DistoX BLE info read-task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.cavway;

import android.os.AsyncTask;

import com.topodroid.TDX.TopoDroidApp;

import java.lang.ref.WeakReference;

public class CavwayInfoReadTask extends AsyncTask<Void, Integer, Boolean>
{
  private final WeakReference<TopoDroidApp> mApp; // FIXME LEAK
  private final WeakReference<CavwayInfoDialog> mDialog;

  /** cstr
   * @param app    application
   * @param dialog info display dialog
   */
  public CavwayInfoReadTask( TopoDroidApp app, CavwayInfoDialog dialog )
  {
    mApp    = new WeakReference<TopoDroidApp>( app );
    mDialog = new WeakReference<CavwayInfoDialog>( dialog );
  }

  /** execute the task in background
   */
  @Override
  protected Boolean doInBackground( Void... v )
  {
    if ( mApp.get() == null ) return false;
    return mApp.get().getCavwayInfo( mDialog.get() );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    mDialog.get().updateHwFw();
  }

}
