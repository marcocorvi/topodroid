/* @file StationInfoAdapter.java
 *
 * @author marco corvi
 * @date may 2024
 *
 * @brief TopoDroid adapter for survey saved stations
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.ArrayList;

class StationInfoAdapter extends ArrayAdapter< StationInfo >
{
  private ArrayList< StationInfo > items;
  private Context context;

  StationInfoAdapter( Context ctx, int id, ArrayList< StationInfo > items )
  {
    super( ctx, id, items );
    this.context = ctx;
    this.items = items;
  }

  // items are guaranteed non-null
  // throws if pos < 0 or pos >= items.size()
  public StationInfo get( int pos ) { return items.get(pos); }
 
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      v = li.inflate( R.layout.row, parent, false ); // NullPointerException
    }

    StationInfo b = get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      tw.setTextSize( TDSetting.mTextSize );
      // tw.setTextColor( b.color() );
    }
    return v;
  }

}

