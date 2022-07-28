/* @file ListerSetHandler.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid handler for a set of listers
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDUtil;

import java.util.ArrayList;

// import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

class ListerSetHandler extends ListerHandler
{
  private ArrayList< ILister > mListers;

  ListerSetHandler()
  {
    mListers = new ArrayList<>();
  }

  int size() { return mListers.size(); }

  synchronized void registerLister( ILister lister )
  {
    for ( ILister l : mListers ) {
      if ( l == lister ) return; // already registered
    }
    mListers.add( lister );
  }

  synchronized void unregisterLister( ILister lister )
  {
    mListers.remove( lister );
  }

  synchronized public void setConnectionStatus( int status )
  {
    for ( ILister lister : mListers ) lister.setConnectionStatus( status );
  }

  @Override
  synchronized public void refreshDisplay( int nr, boolean toast )
  {
    for ( ILister lister : mListers ) lister.refreshDisplay( nr, toast );
  }

  @Override
  synchronized public void handleMessage( Message msg )
  {
    if ( size() == 0 ) return;

    Bundle bundle = msg.getData();
    switch ( msg.what ) {
      case Lister.LIST_REFRESH:
        int nr = bundle.getInt( Lister.NUMBER );
        for ( ILister lister : mListers ) lister.refreshDisplay( nr, false );
        break;
      case Lister.LIST_STATUS:
        int status = bundle.getInt( Lister.STATE );
        for ( ILister lister : mListers ) lister.setConnectionStatus( status );
        break;
      case Lister.LIST_UPDATE:
        long blk_id = bundle.getLong( Lister.BLOCK_ID );
        // TDLog.v("DATA " + "lister set handler msg blk id " + blk_id + " size " + mListers.size() );
        if ( TDLog.isStreamFile() ) TDLog.f("LISTER SET " + TDLog.threadId() + " handle: blk id " + blk_id );
        // TDUtil.slowDown( (int)(400 + Math.random() * 600) );
        for ( ILister lister : mListers ) lister.updateBlockList( blk_id );
        break;
      case Lister.LIST_REF_AZIMUTH:
        float azimuth =  bundle.getFloat( Lister.AZIMUTH );
        long fixed_extend = bundle.getLong( Lister.FIXED_EXTEND );
        for ( ILister lister : mListers ) lister.setRefAzimuth( azimuth, fixed_extend );
        break;
    }
  }


}

