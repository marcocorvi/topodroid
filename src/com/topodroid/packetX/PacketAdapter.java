/* @file PacketAdapter.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid adapter for DistoX packets
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.packetX;

import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.R;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

// import android.support.annotation.NonNull;

import java.util.List;

class PacketAdapter extends ArrayAdapter< PacketData >
{
  private List< PacketData > items;  // list if calibration data
  private final Context context;                 // context

  PacketAdapter( Context ctx, int id, List< PacketData > items )
  {
    super( ctx, id, items );
    this.context = ctx;
    this.items = items;
  }

  PacketData get( int pos ) { return items.get(pos); }
 
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

    PacketData b = items.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      // tw.setTextSize( TDSetting.mTextSize );
      tw.setTextColor( b.color() );
      tw.setBackgroundColor( b.background() );
    }
    return v;
  }

}

