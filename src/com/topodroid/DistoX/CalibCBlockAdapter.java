/* @file CalibCBlockAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for calibration data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

// import android.support.annotation.NonNull;

import java.util.ArrayList;

class CalibCBlockAdapter extends ArrayAdapter< CalibCBlock >
{
  private ArrayList< CalibCBlock > items;  // list if calibration data
  private final Context context;                 // context


  CalibCBlockAdapter( Context ctx, int id, ArrayList< CalibCBlock > items )
  {
    super( ctx, id, items );
    this.context = ctx;
    this.items = items;
  }

  CalibCBlock get( int pos ) { return items.get(pos); }
 
  @Override
  // @NonNull
  public View getView( int pos, View convertView, /* @NonNull */ ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      try { 
        v = li.inflate( R.layout.row, parent, false ); // FIXME inflate may produce NullPointerException
      } catch ( NullPointerException e ) {
        TDLog.Error("CBlock adapter inflate view: null pointer");
        return null;
      }
    }

    CalibCBlock b = items.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      tw.setTextSize( TDSetting.mTextSize );
      tw.setTextColor( b.color() );
      if ( b.isSaturated() ) {      // saturated data
        tw.setBackgroundColor( TDColor.DARK_BROWN );
      } else if ( b.isGZero() ) {   // G=0 data
        tw.setBackgroundColor( TDColor.DARK_VIOLET );
      } else if ( b.mStatus != 0 ) {
        tw.setBackgroundColor( TDColor.DARK_GRAY );
      } else if ( b.mError > TDMath.DEG2RAD ) { // 1 degree
        tw.setBackgroundColor( TDColor.MID_RED );
      } else if ( b.isFar() || b.isOffGroup() ) {
        tw.setBackgroundColor( TDColor.DARK_GREEN );
      } else {
        tw.setBackgroundColor( TDColor.BLACK );
      }
    }
    return v;
  }

}

