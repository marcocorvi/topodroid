/* @file ListerHandler.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid handler for a data lister
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.util.Log;

class ListerHandler extends Handler
{
  private ILister mLister = null;

  // default cstr
  ListerHandler( ) { mLister = null; }

  ListerHandler( ILister lister )
  { 
    mLister = lister;
  }

  void refreshDisplay( int nr, boolean toast )
  {
    if ( mLister != null ) mLister.refreshDisplay( nr, toast );
  }

  @Override
  public void handleMessage( Message msg )
  {
    if ( mLister == null ) return;
    Bundle bundle = msg.getData();
    switch ( msg.what ) {
      case Lister.REFRESH:
        int nr = bundle.getInt( Lister.NUMBER );
        mLister.refreshDisplay( nr, false );
        break;
      case Lister.STATUS:
        int status = bundle.getInt( Lister.STATE );
        mLister.setConnectionStatus( status );
        break;
      case Lister.UPDATE:
        long blk_id = bundle.getLong( Lister.BLOCK_ID );
        // Log.v("DistoX", "lister handler message blk id " + blk_id );
        mLister.updateBlockList( blk_id );
        break;
      case Lister.REF_AZIMUTH:
        float azimuth =  bundle.getFloat( Lister.AZIMUTH );
        long fixed_extend = bundle.getLong( Lister.FIXED_EXTEND );
        mLister.setRefAzimuth( azimuth, fixed_extend );
        break;
    }
  }

}

