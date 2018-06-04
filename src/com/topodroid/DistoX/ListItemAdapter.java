/* @file ListItemAdapter.java
 *
 * @author marco corvi
 * @date nov 2014
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

// import java.util.ArrayList;

class ListItemAdapter extends ArrayAdapter< String >
{
  private final Context context;            // context


  ListItemAdapter( Context ctx, int id )
  {
    super( ctx, id );
    this.context = ctx;
  }


  String get( int pos ) { return getItem(pos); }
 
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      v = li.inflate( R.layout.row, parent, false );
    }

    String b = getItem( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b );
      tw.setTextSize( TDSetting.mTextSize );
    }
    return v;
  }

}

