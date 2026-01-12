/* @file CavwaySyncDateTimeTask.java
 *
 * @author Siwei Tian
 * @date Jul 2024
 *
 * @brief TopoDroid Cavway BLE info read-task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.cavway;

import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

import android.os.AsyncTask;
import android.content.Context;

import java.lang.ref.WeakReference;

public class CavwaySyncDateTimeTask extends AsyncTask<Void, Integer, Boolean>
{
  // private final WeakReference<TopoDroidApp> mApp; // FIXME LEAK
  // private final WeakReference<CavwayInfoDialog> mDialog;
  Context mContext;
  CavwayComm mComm;
  String mAddress; // device address

  /** cstr
   * @param comm   communication object
   */
  public CavwaySyncDateTimeTask( Context ctx, CavwayComm comm, String address )
  {
    mContext = ctx;
    mComm    = comm;
    mAddress = address;
  }

  /** execute the task in background
   */
  @Override
  protected Boolean doInBackground( Void... v )
  {
    return mComm.syncDateTime( mAddress ); 
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( result ) {
      TDToast.make( mContext.getResources().getString( R.string.cavway_synchronized ) );
    } else {
      TDToast.make( mContext.getResources().getString( R.string.cavway_sync_failed ) );
    }
  }

}
