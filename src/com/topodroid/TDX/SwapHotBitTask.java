/* @file SwapHotBitTask.java
 *
 * @author marco corvi
 * @date apr 2016
 *
 * @brief TopoDroid DistoX A3 swap hot bit in a given range
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.dev.Device;

// import com.topodroid.utils.TDLog;

import java.lang.ref.WeakReference;

// import android.app.Activity;
import android.os.AsyncTask;
// import android.content.Context;

class SwapHotBitTask extends AsyncTask<Void, Integer, Integer>
{
  private final WeakReference<TopoDroidApp> mApp;
  private int mType; // DistoX type
  private String mAddress;
  private int mFrom;
  private int mTo;
  private boolean mOnOff;

  /** cstr
   * @param app      application
   * @param type     device type
   * @param address  device luetooth address
   * @param ht       head_tail - head at index 0, tail at index 1
   * @param on_off
   */
  SwapHotBitTask( TopoDroidApp app, int type, String address, int[] ht, boolean on_off )
  {
    mApp     = new WeakReference<TopoDroidApp>( app );
    mAddress = address;
    mType    = type;
    mFrom    = ht[1]; // tail
    mTo      = ht[0]; // head
    mOnOff   = on_off;
    // // TDLog.v("do reset from " + from + " to " + to );
  }

  /** execute the swapping task
   * @return the number of data hot-bits that have been swapped
   */
  @Override
  protected Integer doInBackground(Void... v)
  {
    int res = 0;
    if ( mType == Device.DISTO_X310 ) {
      res = -1;
    } else if ( mType == Device.DISTO_A3 && mApp.get() != null ) {
      res = mApp.get().swapA3HotBit( mAddress, mFrom, mTo, mOnOff );
    }
    return res;
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  /** post-execution user feedback
   * @param result   execution result (number of data hot-bits that have been swapped)
   */
  @Override
  protected void onPostExecute( Integer result )
  {
    if ( result >= 0 ) {
      TDToast.make( R.string.swap_hotbit_ok );
    } else {
      TDToast.makeBad( R.string.read_failed );
    }
  }

}
