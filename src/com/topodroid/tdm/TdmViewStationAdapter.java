/** @file TdmViewStationAdapter.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager survey station display adapter
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.TDX.R;

import java.util.ArrayList;
// import java.util.Iterator;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.CheckBox;
// import android.widget.LinearLayout;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;

// import android.widget.ArrayAdapter;
// import android.widget.ListView;
// import android.widget.CheckBox;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

class TdmViewStationAdapter extends ArrayAdapter< TdmViewStation >
                           implements OnClickListener
{
  private ArrayList< TdmViewStation > mItems;
  private Context mContext;
  private LayoutInflater mLayoutInflater;
  private TextView mTextView;
  private TdmViewCommand mCommand;

  /** cstr
   * @param ctx     context
   * @param id      resource id ?
   * @param items   station-view items
   * @param text    text-view for the station name
   * @param command survey view command
   */
  public TdmViewStationAdapter( Context ctx, int id, ArrayList< TdmViewStation > items, TextView text, TdmViewCommand command )
  {
    super( ctx, id, items );
    mContext = ctx;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    mItems = items;
    mTextView = text;
    mCommand = command;
  }

  /** @return the station of the (first) checked station-view - or null
   */
  public TdmStation getCheckedStation( ) 
  { 
    for ( TdmViewStation tv : mItems ) {
      if ( tv.isChecked() ) return tv.mStation;
    }
    return null;
  }

  /** @return the station name
   */
  public String getStationName() 
  {
    if ( mTextView.getText() != null ) return mTextView.getText().toString();
    return null;
  }

  /** @return the station-view at a given position
   * @param pos   position (index)
   */
  public TdmViewStation get( int pos ) { return mItems.get(pos); }

  /** @return the number of station views
   */
  public int size() { return mItems.size(); }

  /** helper class view-holder
   */
  private class ViewHolder
  { 
    CheckBox cb;
    TdmViewStation st;
  }

  /** get a view 
   * @param pos   item position
   * @param convertView   convertible view
   * @param parent        view-group parent
   * @return either the converted view or a new view filled with the item data
   */
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    TdmViewStation b = mItems.get( pos );
    if ( b == null ) return convertView;

    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.tdviewstation_adapter, null );
      holder = new ViewHolder();
      holder.cb = (CheckBox) convertView.findViewById( R.id.station );
      holder.cb.setOnClickListener( this );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
      if ( holder.st != null ) holder.st.setCheckBox( null );
    }
    holder.st = b;
    b.setCheckBox( holder.cb );
    holder.cb.setText( b.name() );
    // holder.cb.setChecked( b.mChecked );
    return convertView;
  }

  // @Override 
  // public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  // {
  //   CharSequence item = ((TextView) view).getText();
  //   for ( TdmViewStation station : mItems ) {
  //     if ( station.name().equals( item ) ) {
  //       station.setChecked( true );
  //     } else {
  //       station.setChecked( false );
  //     }
  //   }
  // }

  /** react on a user click
   * @param v  tapped view
   */
  @Override
  public void onClick( View v )
  {
    CheckBox cb = (CheckBox)v;
    CharSequence item = cb.getText();
    for ( TdmViewStation station : mItems ) {
      if ( station.name().equals( item ) ) {
        station.setChecked( true );
        if ( mTextView != null ) mTextView.setText( station.name() + "@" + mCommand.name() );
      } else {
        station.setChecked( false );
      }
    }
  }

}

