/* @file CalibComputer.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid calibration coefficient computation
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

// import java.Thread;

// import android.widget.Toast;
import android.os.AsyncTask;


public class CalibComputer extends AsyncTask< String, Integer, Integer >
{
  private GMActivity mParent;
  private static CalibComputer running = null;
  private int mJob;

  CalibComputer( GMActivity parent, int job )
  {
    mParent = parent;
    mJob = job;
  }

// -------------------------------------------------------------------
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( ! lock() ) return null;
    int ret = 0;
    if ( mJob == GMActivity.CALIB_RESET_GROUPS ) {
      mParent.doResetGroups();
    } else if ( mJob == GMActivity.CALIB_COMPUTE_GROUPS ) {
      mParent.doComputeGroups();
    } else if ( mJob == GMActivity.CALIB_COMPUTE_CALIB ) {
      ret = mParent.computeCalib();
    }
    return ret;
  }

  @Override
  protected void onProgressUpdate( Integer... values)
  {
    super.onProgressUpdate( values );
  }

  @Override
  protected void onPostExecute( Integer res )
  {
    if ( res != null ) {
      int r = res.intValue();
      if ( mJob == GMActivity.CALIB_RESET_GROUPS || mJob == GMActivity.CALIB_COMPUTE_GROUPS ) {
        mParent.updateDisplay( );
      } else if ( mJob == GMActivity.CALIB_COMPUTE_CALIB ) {
        mParent.handleComputeCalibResult( r );
      }
    }
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
