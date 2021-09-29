/** @file MyFileAdapter.java
 *
 * @author marco corvi
 * @date
 *
 * @brief Cave3D file adapter
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
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

class MyFileAdapter extends ArrayAdapter< MyFileItem >
                    implements OnClickListener
{
  private ArrayList< MyFileItem > mItems;
  private Context mContext;
  private OnItemClickListener mListener;
  private ListView mParent;

  /** get the item at a certain position in the list of symbols 
   */
  MyFileItem get( int k )
  { 
    // TDLog.v( "File Adapter get item at " + k + " of " + mItems.size() );
    return ( k < mItems.size() ) ? mItems.get(k) : null ;
  }

  public MyFileAdapter( Context context, OnItemClickListener click_listener, ListView parent, 
                        int id, ArrayList< MyFileItem > items )
  {
    super( context, id, items );
    mContext  = context;
    mParent   = parent;
    mListener = click_listener;
    if ( items != null ) {
      mItems = items;
      // for ( MyFileItem item : items ) item.setListener( this );
    } else {
      mItems = new ArrayList< MyFileItem >();
    }
  }

  public void add( String text, boolean is_dir )
  {
    mItems.add( new MyFileItem( mContext, this, text, is_dir ) );
  }

  // public void resetBgColor()
  // {
  //   for ( MyFileItem item : mItems ) item.resetBgColor();
  // }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    MyFileItem b = mItems.get( pos );
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

