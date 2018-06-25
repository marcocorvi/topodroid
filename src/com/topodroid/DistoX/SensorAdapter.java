/* @file SensorAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for survey data
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

import java.util.ArrayList;

class SensorAdapter extends ArrayAdapter< SensorInfo >
{
  private ArrayList< SensorInfo > items;
  private Context context;

  SensorAdapter( Context ctx, int id, ArrayList< SensorInfo > items )
  {
    super( ctx, id, items );
    this.context = ctx;
    this.items = items;
  }

  SensorInfo get( int pos ) { return items.get(pos); }
 
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      v = li.inflate( R.layout.row, parent, false );
    }

    SensorInfo b = items.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      tw.setTextSize( TDSetting.mTextSize );
      // tw.setTextColor( b.color() );
    }
    return v;
  }

}

