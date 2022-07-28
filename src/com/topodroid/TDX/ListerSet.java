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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

import java.util.ArrayList;

class ListerSet implements ILister
{
  private ArrayList< ILister > mLister;

  /** cstr
   */
  ListerSet()
  {
    mLister = new ArrayList<>();
  }

  /** @return the number of listers in this set
   */
  int size() { return mLister.size(); }

  /** set the window title - default implementation does not do anything
   */
  public void setTheTitle() { }

  /** add a lister to the set, if not already present
   * @param lister   lister to add
   */
  void registerLister( ILister lister )
  {
    for ( ILister l : mLister ) {
      if ( l == lister ) return; // already registered
    }
    mLister.add( lister );
  }

  /** drop a lister from the set, if present
   * @param lister   lister to drop
   */
  void unregisterLister( ILister lister )
  {
    mLister.remove( lister );
  }

  /** forward list update to the listers in the set
   * @param blk_id  block ID
   */
  public void updateBlockList( long blk_id ) 
  {
    if ( TDLog.isStreamFile() ) TDLog.f("LISTER SET " + TDLog.threadId() + " update block list: blk id " + blk_id );
    for ( ILister lister : mLister ) lister.updateBlockList( blk_id );
  }

  /** forward the connection status update to the listers in the set
   * @param status  new connection status
   */
  public void setConnectionStatus( int status )
  {
    // TDLog.Error( "Lister set conn. status " + size() + " " + status );
    for ( ILister lister : mLister ) lister.setConnectionStatus( status );
  }

  /** forward a display refresh to the listers in the set
   * @param r       ???
   * @param toast   whether to toast any message
   */
  public void refreshDisplay( int r, boolean toast )
  {
    // TDLog.Error( "Lister refresh display " + size() + " R " + r + " " + toast );
    for ( ILister lister : mLister ) lister.refreshDisplay( r, toast );
  }

  /** forward a reference azimuth to the listers in the set
   * @param azimuth       azimuth value of the reference
   * @param fixed_extend  whether the extend has to be fixed
   */
  public void setRefAzimuth( float azimuth, long fixed_extend )
  {
    for ( ILister lister : mLister ) lister.setRefAzimuth( azimuth, fixed_extend );
  }

  /** forward a BT button enable to the listers in the set
   * @param enable   whether the BT button should be enabled
   */
  public void enableBluetoothButton( boolean enable )
  {
    for ( ILister lister : mLister ) lister.enableBluetoothButton( enable );
  }

}

