/* @file ListerSetHandler.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid handler for a set of listers
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.util.Log;

public class ListerSetHandler extends ListerHandler
{
  private ArrayList<ILister> mListers;

  ListerSetHandler()
  {
    mListers = new ArrayList< ILister >();
  }

  int size() { return mListers.size(); }

  void registerLister( ILister lister )
  {
    // Log.v("DistoX", "register lister " + lister.toString() + " size " + size() );
    for ( ILister l : mListers ) {
      if ( l == lister ) return; // already registered
    }
    mListers.add( lister );
  }

  void unregisterLister( ILister lister )
  {
    // Log.v("DistoX", "unregister lister " + lister.toString() + " size " + size() );
    mListers.remove( lister );
  }

  // public void updateBlockList( long blk_id ) 
  // {
  //   for ( ILister lister : mListers ) lister.updateBlockList( blk_id );
  // }

  public void setConnectionStatus( int status )
  {
    for ( ILister lister : mListers ) lister.setConnectionStatus( status );
  }

  @Override
  public void refreshDisplay( int nr, boolean toast )
  {
    for ( ILister lister : mListers ) lister.refreshDisplay( nr, toast );
  }

  @Override
  public void handleMessage( Message msg )
  {
    if ( size() == 0 ) return;

    Bundle bundle = msg.getData();
    switch ( msg.what ) {
      case Lister.REFRESH:
        int nr = bundle.getInt( Lister.NUMBER );
        for ( ILister lister : mListers ) lister.refreshDisplay( nr, false );
        break;
      case Lister.STATUS:
        int status = bundle.getInt( Lister.STATE );
        for ( ILister lister : mListers ) lister.setConnectionStatus( status );
        break;
      case Lister.UPDATE:
        long blk_id = bundle.getLong( Lister.BLOCK_ID );
        for ( ILister lister : mListers ) lister.updateBlockList( blk_id );
        break;
      case Lister.REF_AZIMUTH:
        float azimuth =  bundle.getFloat( Lister.AZIMUTH );
        long fixed_extend = bundle.getLong( Lister.FIXED_EXTEND );
        for ( ILister lister : mListers ) lister.setRefAzimuth( azimuth, fixed_extend );
        break;
    }
  }


}

