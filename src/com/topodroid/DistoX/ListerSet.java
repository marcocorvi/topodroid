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

// import android.util.Log;

public class ListerSet
{
  private ArrayList<ILister> mLister;

  ListerSet()
  {
    mLister = new ArrayList< ILister >();
  }

  int size() { return mLister.size(); }

  void updateList( DistoXDBlock blk ) 
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Lister updateBlock " + size() );
    for ( ILister lister : mLister ) lister.updateBlockList( blk );
  }

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

  void setConnectionStatus( int status )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Lister set conn. status " + size() + " " + status );
    for ( ILister lister : mLister ) lister.setConnectionStatus( status );
  }

  void refreshDisplay( int r, boolean b )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Lister refresh display " + size() + " R " + r + " " + b );
    for ( ILister lister : mLister ) lister.refreshDisplay( r, b );
  }


}

