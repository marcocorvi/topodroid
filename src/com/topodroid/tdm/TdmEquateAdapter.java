/** @file TdmEquateAdapter.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TopoDroid Manager adapter of TdmEquate items
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 */
package com.topodroid.tdm;

import com.topodroid.Cave3X.R;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;

class TdmEquateAdapter extends ArrayAdapter< TdmEquate >
{
  private ArrayList< TdmEquate > mItems;
  private Context mContext;
  private LayoutInflater mLayoutInflater;

  public TdmEquateAdapter( Context ctx, int id, ArrayList< TdmEquate > items )
  {
    super( ctx, id, items );
    mContext = ctx;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    if ( items != null ) {
      mItems = items;
    } else {
      mItems = new ArrayList< TdmEquate >();
    }
  }

  public TdmEquate get( int pos ) { return mItems.get(pos); }

  // void addTdmEquate( TdmEquate equate ) { mItems.add( equate ); }

  public int size() { return mItems.size(); }


  @Override
  public View getView( int pos, View view, ViewGroup parent )
  {
    TdmEquate b = mItems.get( pos );
    if ( b == null ) return view;

    TdmEquateViewHolder holder = null; 
    if ( view == null ) {
      view = mLayoutInflater.inflate( R.layout.tdequate_adapter, null );
      holder = new TdmEquateViewHolder();
      holder.textView = (TextView) view.findViewById( R.id.tdequate );
      view.setTag( holder );
    } else {
      holder = (TdmEquateViewHolder) view.getTag();
    }
    holder.equate = b;
    holder.textView.setText( b.stationsString() );
    return view;
  }

  

}

