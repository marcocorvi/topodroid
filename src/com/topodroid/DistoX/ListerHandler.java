/* @file ListerHandler.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid handler for a data lister
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

public class ListerHandler extends Handler 
{
  static final String LISTER_DATA_NUMBER    = "LISTER_DATA_NUMBER";
  static final String LISTER_DATA_STATUS    = "LISTER_DATA_STATUS";
  static final String LISTER_DATA_BLOCK_ID  = "LISTER_DATA_BLOCK_ID";
  static final String LISTER_DATA_AZIMUTH   = "LISTER_DATA_AZIMUTH";
  static final String LISTER_DATA_FIXED_EXTEND  = "LISTER_DATA_FIXED_EXTEND";

  static final int LISTER_REFRESH = 1;
  static final int LISTER_UPDATE  = 2;
  static final int LISTER_STATUS  = 3;
  static final int LISTER_REF_AZIMUTH = 4;

  ILister mLister = null;

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
      case LISTER_REFRESH:
        int nr = bundle.getInt( LISTER_DATA_NUMBER );
        mLister.refreshDisplay( nr, false );
        break;
      case LISTER_STATUS:
        int status = bundle.getInt( LISTER_DATA_STATUS );
        mLister.setConnectionStatus( status );
        break;
      case LISTER_UPDATE:
        long blk_id = bundle.getLong( LISTER_DATA_BLOCK_ID );
        mLister.updateBlockList( blk_id );
        break;
      case LISTER_REF_AZIMUTH:
        float azimuth =  bundle.getFloat( LISTER_DATA_AZIMUTH );
        long fixed_extend = bundle.getLong( LISTER_DATA_FIXED_EXTEND );
        mLister.setRefAzimuth( azimuth, fixed_extend );
        break;
    }
  }

}

