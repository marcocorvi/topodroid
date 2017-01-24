/* @file ListerSet.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid lister set
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.util.Log;

public class ListerSet implements ILister
{
  private ArrayList<ILister> mLister;

  ListerSet()
  {
    mLister = new ArrayList< ILister >();
  }

  int size() { return mLister.size(); }

  public void setTheTitle() { }

  void registerLister( ILister lister )
  {
    // Log.v("DistoX", "register lister " + lister.toString() + " size " + size() );
    for ( ILister l : mLister ) {
      if ( l == lister ) return; // already registered
    }
    mLister.add( lister );
  }

  void unregisterLister( ILister lister )
  {
    // Log.v("DistoX", "unregister lister " + lister.toString() + " size " + size() );
    mLister.remove( lister );
  }

  public void updateBlockList( long blk_id ) 
  {
    for ( ILister lister : mLister ) lister.updateBlockList( blk_id );
  }

  public void updateBlockList( DistoXDBlock blk ) 
  {
    for ( ILister lister : mLister ) lister.updateBlockList( blk );
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

}

