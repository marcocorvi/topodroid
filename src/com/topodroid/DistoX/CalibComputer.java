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

// import android.widget.Toast;
import android.os.AsyncTask;


public class CalibComputer extends AsyncTask< String, Integer, Integer >
{
  static final int CALIB_COMPUTE_CALIB  = 0;
  static final int CALIB_COMPUTE_GROUPS = 1;
  static final int CALIB_RESET_GROUPS   = 2;
  static final int CALIB_RESET_AND_COMPUTE_GROUPS = 3;

  private GMActivity mParent;
  private static CalibComputer running = null;
  private long mStartId;
  private int mJob;

  CalibComputer( GMActivity parent, long start, int job )
  {
    mParent  = parent;
    mStartId = start;
    mJob     = job;
  }

// -------------------------------------------------------------------
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( ! lock() ) return null;
    int ret = 0;
    if ( mJob == CALIB_RESET_GROUPS ) {
      mParent.doResetGroups( mStartId );
    } else if ( mJob == CALIB_COMPUTE_GROUPS ) {
      ret = mParent.doComputeGroups( mStartId );
    } else if ( mJob == CALIB_RESET_AND_COMPUTE_GROUPS ) {
      mParent.doResetGroups( mStartId );
      ret = mParent.doComputeGroups( mStartId );
    } else if ( mJob == CALIB_COMPUTE_CALIB ) {
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
      mParent.handleComputeCalibResult( mJob, r );
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
