/* @file FixedAdapter.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid adapter for fixed station info
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.List;

class FixedAdapter extends ArrayAdapter< FixedInfo >
{
  private List< FixedInfo > items;
  private final Context context;

  FixedAdapter( Context ctx, int id, List< FixedInfo > items )
  {
    super( ctx, id, items );
    this.context = ctx;
    this.items = items;
  }

  public FixedInfo get( int pos ) { return items.get(pos); }
 
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    View v = convertView;
    if ( v == null ) {
      LayoutInflater li = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      try {
        v = li.inflate( R.layout.row, parent, false ); // may produce NullPointerException
      } catch ( NullPointerException e ) {
        TDLog.Error("Fixed adapter inflate view: null pointer");
	return null;
      }
    }

    FixedInfo b = items.get( pos );
    if ( b != null ) {
      TextView tw = (TextView) v.findViewById( R.id.row_text );
      tw.setText( b.toString() );
      // TDLog.v(  "Fixed Info " + b.toString() );
      // tw.setTextColor( b.color() );
    }
    return v;
  }

}

