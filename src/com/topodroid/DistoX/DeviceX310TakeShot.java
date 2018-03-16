/* @file DeviceX310TakeShot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX310 shooting class
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.AsyncTask;
 
class DeviceX310TakeShot extends AsyncTask<Integer, Integer, Integer >
{
  private ILister mILister;
  private ListerHandler mLister; // lister that manages downloaded shots (if null shots are not downloaded)
  TopoDroidApp  mApp;
  private int mNr;               // number of shots before download
 
  DeviceX310TakeShot( ILister ilister, ListerHandler lister, TopoDroidApp app, int nr )
  {
    super();
    mILister  = ilister; 
    mLister   = lister; 
    mApp      = app;
    mNr       = nr;
  } 

  // 0 off
  // 1 on
  // 2 measure
  // 3 measure and download
  @Override
  protected Integer doInBackground( Integer... ii )
  {
    int i = mNr;
    for ( ; i>1; --i ) {
      mApp.setX310Laser( 1, null );
      try { Thread.sleep( TDSetting.mWaitLaser ); } catch( InterruptedException e ) { }
      mApp.setX310Laser( 2, null );   
      try { Thread.sleep( TDSetting.mWaitShot ); } catch( InterruptedException e ) { }
    }
    mApp.setX310Laser( 1, null );
    try { Thread.sleep( TDSetting.mWaitLaser ); } catch( InterruptedException e ) { }
    return 0;
  }

  @Override
  protected void onProgressUpdate(Integer... progress) { }

  @Override
  protected void onPreExecute( )
  {
    mILister.enableBluetoothButton(false);
  }

  @Override
  protected void onPostExecute( Integer result ) 
  {
    if ( mLister != null ) {
      mApp.setX310Laser( 3, mLister ); // 3 = measure and download
      // try { Thread.sleep( TDSetting.mWaitShot ); } catch( InterruptedException e ) { }
    } else {
      mApp.setX310Laser( 2, null ); // 2 = measure
      // try { Thread.sleep( TDSetting.mWaitLaser ); } catch( InterruptedException e ) { }
    }
    mILister.enableBluetoothButton(true);
  }
}
