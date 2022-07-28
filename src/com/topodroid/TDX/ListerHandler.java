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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

public class ListerHandler extends Handler
{
  private ILister mLister = null;

  // default cstr
  public ListerHandler( ) { mLister = null; }

  public ListerHandler( ILister lister )
  { 
    mLister = lister;
  }

  public void refreshDisplay( int nr, boolean toast )
  {
    if ( mLister != null ) mLister.refreshDisplay( nr, toast );
  }

  @Override
  public void handleMessage( Message msg )
  {
    if ( mLister == null ) return;
    Bundle bundle = msg.getData();
    switch ( msg.what ) {
      case Lister.LIST_REFRESH:
        int nr = bundle.getInt( Lister.NUMBER );
        mLister.refreshDisplay( nr, false );
        break;
      case Lister.LIST_STATUS:
        int status = bundle.getInt( Lister.STATE );
        mLister.setConnectionStatus( status );
        break;
      case Lister.LIST_UPDATE:
        long blk_id = bundle.getLong( Lister.BLOCK_ID );
        if ( TDLog.isStreamFile() ) TDLog.f("LISTER " + TDLog.threadId() + " lister " + this.toString() + " blk id " + blk_id );
        mLister.updateBlockList( blk_id );
        break;
      case Lister.LIST_REF_AZIMUTH:
        float azimuth =  bundle.getFloat( Lister.AZIMUTH );
        long fixed_extend = bundle.getLong( Lister.FIXED_EXTEND );
        mLister.setRefAzimuth( azimuth, fixed_extend );
        break;
    }
  }

}

