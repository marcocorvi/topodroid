/** @file MyMenuAdapter.java
 *
 * @author marco corvi
 * @date
 *
 * @brief TopoDroid 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
// import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
// import android.view.LayoutInflater;

import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import android.util.Log;

class MyMenuAdapter extends ArrayAdapter< MyMenuItem >
                    implements OnClickListener
{
  private ArrayList< MyMenuItem > mItems;
  private Context mContext;
  private OnItemClickListener mListener;
  private ListView mParent;

  /** get the item at a certain position in the list of symbols 
   */
  MyMenuItem get( int k )
  { 
    // Log.v("DistoX", "MyMenuAdapter get item at " + k + " of " + mItems.size() );
    return ( k < mItems.size() ) ? mItems.get(k) : null ;
  }

  public MyMenuAdapter( Context context, OnItemClickListener click_listener, ListView parent, 
                        int id, ArrayList< MyMenuItem > items )
  {
    super( context, id, items );
    mContext  = context;
    mParent   = parent;
    mListener = click_listener;
    if ( items != null ) {
      mItems = items;
      for ( MyMenuItem item : items ) item.setListener( this );
    } else {
      mItems = new ArrayList< MyMenuItem >();
    }
  }

  public void add( String text )
  {
    mItems.add( new MyMenuItem( mContext, this, text ) );
  }

  public void resetBgColor()
  {
    for ( MyMenuItem item : mItems ) item.resetBgColor();
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    MyMenuItem b = mItems.get( pos );
    if ( b == null ) return convertView;
    return b; // mView;
  }

  @Override
  public void onClick( View v ) 
  {
    for ( int pos = 0; pos < mItems.size(); ++ pos ) {
      if ( v == mItems.get( pos ) ) {
        mListener.onItemClick( mParent, v, pos, 0 );
        break;
      }
    }
  }

}

