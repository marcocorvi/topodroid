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
  private AutoCalibDialog mDialog = null; // AUTO-CALIB

  /** default cstr
   */
  public ListerHandler( ) { }

  /** cstr with a list displayer
   * @param lister  list displayer
   */
  public ListerHandler( ILister lister )
  { 
    mLister = lister;
  }

  /** cstr with a dialog
   * @param dialog  dialog
   */
  public ListerHandler( AutoCalibDialog dialog ) // AUTO-CALIB
  {
    mDialog = dialog;
  }

  /** @return true if this lister handler is associated to a dialog
   */
  public boolean hasDialog() { return mDialog != null; }

  /** set the dialog to null
   */
  public void closeDialog() { mDialog = null; } // AUTO-CALIB

  /** @return the handler name: either the lister name, or the string 'dialog', or 'no-name'
   */
  public String name() 
  { 
    if ( mLister != null ) return mLister.name();
    if ( mDialog != null ) return "dialog";
    return "no-name";
  }

  /** refresh display
   * @param nr    number of data
   * @param toast whether to toast
   */
  public void refreshDisplay( int nr, boolean toast )
  {
    if ( mLister != null ) mLister.refreshDisplay( nr, toast );
  }

  /** set the connection status
   * @param status status
   */
  public void setConnectionStatus( int status ) 
  {
    if ( mLister != null ) mLister.setConnectionStatus( status );
  }

  /** handle a message
   * @param msg  message
   */
  @Override
  public void handleMessage( Message msg )
  {
    if ( mLister != null ) {
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
    } else if (  mDialog != null ) { // AUTO-CALIB
      if ( msg.what ==  Lister.LIST_AUTO_CALIB ) {
        Bundle bundle = msg.getData();
        long gx = bundle.getLong( "GX" );
        long gy = bundle.getLong( "GY" );
        long gz = bundle.getLong( "GZ" );
        long mx = bundle.getLong( "MX" );
        long my = bundle.getLong( "MY" );
        long mz = bundle.getLong( "MZ" );
        mDialog.update( gx, gy, gz, mx, my, mz );
      }
    }
  }

}

