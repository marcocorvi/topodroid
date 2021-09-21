/* @file PhotoAdapter.java
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
package com.topodroid.Cave3X;

// import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.ArrayList;

class PhotoAdapter extends ArrayAdapter< PhotoInfo >
{
  private ArrayList< PhotoInfo > items;
  private Context context;

  PhotoAdapter( Context ctx, int id, ArrayList< PhotoInfo > items )
  {
    super( ctx, id, items );
    this.context = ctx;
    this.items = items;
  }

  // items are guaranteed non-null
  // throws if pos < 0 or pos >= items.size()
  public PhotoInfo get( int pos ) { return items.get(pos); }
 
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      v = li.inflate( R.layout.row, parent, false ); // NullPointerException
    }

    PhotoInfo b = get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      tw.setTextSize( TDSetting.mTextSize );
      // tw.setTextColor( b.color() );
    }
    return v;
  }

}

