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
  final static int WAIT_LASER = 1000;
  final static int WAIT_SHOT  = 4000;

  ListerHandler mLister; // lister that manages downloaded shots (if null shots are not downloaded)
  TopoDroidApp  mApp;
  int mNr;               // number of shots before download
 
  DeviceX310TakeShot( ListerHandler lister, TopoDroidApp app, int nr ) 
  {
    super();
    mLister = lister; 
    mApp    = app;
    mNr     = nr;
  } 

  @Override
  protected Integer doInBackground( Integer... ii )
  {
    int i = mNr;
    for ( ; i>1; --i ) {
      mApp.setX310Laser( 1, null );
      try { Thread.sleep( WAIT_LASER ); } catch( InterruptedException e ) { }
      mApp.setX310Laser( 2, null );   
      try { Thread.sleep( WAIT_SHOT ); } catch( InterruptedException e ) { }
    }
    mApp.setX310Laser( 1, null );
    try { Thread.sleep( WAIT_LASER ); } catch( InterruptedException e ) { }
    return 0;
  }

  @Override
  protected void onProgressUpdate(Integer... progress) { }

  @Override
  protected void onPostExecute( Integer result ) 
  {
    if ( mLister != null ) {
      mApp.setX310Laser( 3, mLister );
      // try { Thread.sleep( WAIT_SHOT ); } catch( InterruptedException e ) { }
    } else {
      mApp.setX310Laser( 2, null );
      // try { Thread.sleep( WAIT_LASER ); } catch( InterruptedException e ) { }
    }
  }
}
