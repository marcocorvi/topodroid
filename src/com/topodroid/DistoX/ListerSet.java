/* @file ListerSet.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid lister set
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

import java.util.ArrayList;

class ListerSet implements ILister
{
  private ArrayList< ILister > mLister;

  ListerSet()
  {
    mLister = new ArrayList<>();
  }

  int size() { return mLister.size(); }

  public void setTheTitle() { }

  void registerLister( ILister lister )
  {
    for ( ILister l : mLister ) {
      if ( l == lister ) return; // already registered
    }
    mLister.add( lister );
  }

  void unregisterLister( ILister lister )
  {
    mLister.remove( lister );
  }

  public void updateBlockList( long blk_id ) 
  {
    for ( ILister lister : mLister ) lister.updateBlockList( blk_id );
  }

  public void setConnectionStatus( int status )
  {
    // TDLog.Error( "Lister set conn. status " + size() + " " + status );
    for ( ILister lister : mLister ) lister.setConnectionStatus( status );
  }

  public void refreshDisplay( int r, boolean toast )
  {
    // TDLog.Error( "Lister refresh display " + size() + " R " + r + " " + toast );
    for ( ILister lister : mLister ) lister.refreshDisplay( r, toast );
  }

  public void setRefAzimuth( float azimuth, long fixed_extend )
  {
    for ( ILister lister : mLister ) lister.setRefAzimuth( azimuth, fixed_extend );
  }

  public void enableBluetoothButton( boolean enable )
  {
    for ( ILister lister : mLister ) lister.enableBluetoothButton( enable );
  }

}

