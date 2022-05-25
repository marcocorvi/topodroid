/* @file CBlockAdapter.java
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
package com.topodroid.calib;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.R;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

// import android.support.annotation.NonNull;

import java.util.ArrayList;

public class CBlockAdapter extends ArrayAdapter< CBlock >
{
  private ArrayList< CBlock > items;  // list if calibration data
  // private final Context context;
  private final LayoutInflater mLayoutInflater;

  // @param ctx    context
  // @param id     resource layout of the array entries
  // @param items  array items
  public CBlockAdapter( Context ctx, int id, ArrayList< CBlock > items )
  {
    super( ctx, id, items );
    // this.context = ctx;
    this.items = items;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  // @return the CBlock item at the given position in the array
  public CBlock get( int pos ) { return items.get(pos); }

  // @NonNull
  @Override
  public View getView( int pos, View convertView, /* @NonNull */ ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      try { 
        v = mLayoutInflater.inflate( R.layout.cblock_row, parent, false ); // FIXME inflate may produce NullPointerException
      } catch ( NullPointerException e ) {
        TDLog.Error("CBlock adapter inflate view: null pointer");
        return null;
      }
    }

    CBlock b = items.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      tw.setTextSize( TDSetting.mTextSize );
      tw.setTextColor( b.color() );
      tw.setWidth( (int)(TopoDroidApp.mDisplayWidth * 1.5) );
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

