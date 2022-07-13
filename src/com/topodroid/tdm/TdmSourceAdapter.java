/** @file TdmSourceAdapter.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager adapter for source files
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

class TdmSourceAdapter extends ArrayAdapter< TdmSource >
{
  private ArrayList< TdmSource > mItems;
  private Context mContext;
  private LayoutInflater mLayoutInflater;

  /** cstr
   * @param ctx    context
   * @param id     resource id
   * @param items  items in the adapter
   */
  public TdmSourceAdapter( Context ctx, int id, ArrayList< TdmSource > items )
  {
    super( ctx, id, items );
    mContext = ctx;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    if ( items != null ) {
      mItems = items;
    } else {
      mItems = new ArrayList< TdmSource >();
    }
  }

  /** @return the source at a given position (null if position out of bounds)
   * @param pos   position (in the source array)
   */
  public TdmSource get( int pos )
  {
    return (pos < 0 || pos >- mItems.size())? null : mItems.get(pos);
  }

  /** @return the source with a given name (or null if not found)
   * @param name  source name
   */
  public TdmSource get( String name ) 
  {
    for ( TdmSource source : mItems ) {
      if ( source.getSurveyName().equals( name ) ) return source;
    }
    return null;
  }

  /** add a source to the list
   * @param source  source to add
   */
  void addTdmSource( TdmSource source ) { mItems.add( source ); }

  /** @return the number of sources
   */
  public int size() { return mItems.size(); }

  /** private view holder class
   */
  private class ViewHolder
  { 
    CheckBox checkBox;
    TextView textView;
  }

  /**
   * @return list of filename of checked sources
   */
  ArrayList< String > getCheckedSources()
  {
    ArrayList< String > ret = new ArrayList<>();
    for ( TdmSource source : mItems ) {
      if ( source.isChecked() ) {
        ret.add( source.getSurveyName() );
      }
    }
    return ret;
  }


  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    // TDLog.v( "Sources: get source pos " + pos + "/" + mItems.size() );
    TdmSource b = mItems.get( pos );
    if ( b == null ) return convertView;

    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.tdsource_adapter, null );
      holder = new ViewHolder();
      holder.checkBox = (CheckBox) convertView.findViewById( R.id.tdsource_checked );
      holder.textView = (TextView) convertView.findViewById( R.id.tdsource_name );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.checkBox.setOnClickListener( null );
    holder.checkBox.setChecked( b.isChecked() );
    holder.checkBox.setOnClickListener( b );
    holder.textView.setText( b.getSurveyName() );
    return convertView;
  }

}

