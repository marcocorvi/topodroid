/* @file UndeleteAdapter.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid adapter for undelete items
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;

import android.content.Context;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.AdapterView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
// import android.view.View.OnLongClickListener;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

class UndeleteAdapter extends ArrayAdapter< UndeleteItem >
                      // , OnClickListener
{
  // private Context mContext;
  private final UndeleteDialog mParent;
  ArrayList< UndeleteItem > mItems;
  ArrayList< DBlock > mSelect;
  private final LayoutInflater mLayoutInflater;

  // private ArrayList< View > mViews;

  /** cstr
   * @param ctx    context
   * @param perent parent window
   * @param id     ...
   * @param items  list of items that can be recovered
   */
  UndeleteAdapter( Context ctx, UndeleteDialog parent, int id, ArrayList< UndeleteItem > items )
  {
    super( ctx, id, items );
    mParent  = parent;
    mItems   = items;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  /** @return the item at a given position
   * @param pos   position
   */
  public UndeleteItem get( int pos ) 
  { 
    return ( pos < 0 || pos >= mItems.size() )? null : mItems.get(pos);
  }

  /** inner class holding the view for the items
   */
  private class ViewHolder implements OnClickListener
  { 
    int      pos;
    CheckBox mCB;
    TextView mTV;
    UndeleteItem   mItem;   // used to make sure blocks do not hold ref to a view, that does not belog to them REVISE_RECENT

    ViewHolder( CheckBox cb, TextView tv )
    {
      pos = 0;
      mTV = tv;
      mCB = cb;
      mItem = null; 
      mCB.setOnClickListener( this );
    }

    @Override
    public void onClick( View v ) 
    { 
      if ( (CheckBox)v != mCB ) return;
      if ( mItem != null ) mItem.flag = mCB.isChecked();
    }
  }

  /** @return the view for an item
   * @param pos         item position
   * @param convertView convertible view
   * @param parent      ...
   */
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    UndeleteItem b = mItems.get( pos );
    ViewHolder holder; // = null;
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.undelete_row, parent, false );
      holder = new ViewHolder( 
        (CheckBox)convertView.findViewById( R.id.flag ),
        (TextView)convertView.findViewById( R.id.text )
      );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.pos = pos;
    holder.mItem = b;
    // b.mView = convertView;
    holder.mTV.setText( b.text );
    holder.mCB.setChecked( b.flag );
    return convertView;
  }

  /** @return the number of items
   */
  @Override
  public int getCount() { return mItems.size(); }

  /** @return the number of items
   */
  public int size() { return mItems.size(); }

  /** @return the item view-type (always "ignore")
   */
  @Override
  public int getItemViewType(int pos) { return AdapterView.ITEM_VIEW_TYPE_IGNORE; }
 
}

