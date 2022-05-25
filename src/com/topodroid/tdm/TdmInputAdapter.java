/** @file TdmInputAdapter.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TopoDroid Manager adapter for the surveys input objects
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.R;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.CheckBox;
// import android.widget.LinearLayout;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

class TdmInputAdapter extends ArrayAdapter< TdmInput >
{
  private ArrayList< TdmInput > mItems;
  private Context mContext;
  private LayoutInflater mLayoutInflater;

  public TdmInputAdapter( Context ctx, int id, ArrayList< TdmInput > items )
  {
    super( ctx, id, items );
    mContext = ctx;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    if ( items != null ) {
      mItems = items;
    } else {
      mItems = new ArrayList< TdmInput >();
    }
  }

  public TdmInput get( int pos ) { return mItems.get(pos); }

  public TdmInput get( String name ) 
  {
    for ( TdmInput input : mItems ) {
      if ( input.getSurveyName().equals( name ) ) return input;
    }
    return null;
  }

  public void add( TdmInput input ) { mItems.add( input ); }

  public void drop( TdmInput input ) { mItems.remove( input ); }

  public void dropChecked( ) 
  {
    final Iterator it = mItems.iterator();
    while ( it.hasNext() ) {
      TdmInput input = (TdmInput) it.next();
      if ( input.isChecked() ) {
        mItems.remove( input );
      }
    }
  }

  public int size() { return mItems.size(); }


  private class ViewHolder
  { 
    CheckBox checkBox;
    TextView textView;
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    TdmInput b = mItems.get( pos );
    if ( b == null ) return convertView;

    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.tdinput_adapter, null );
      holder = new ViewHolder();
      holder.checkBox = (CheckBox) convertView.findViewById( R.id.tdinput_checked );
      holder.textView = (TextView) convertView.findViewById( R.id.tdinput_name );
      holder.textView.setTextSize( TDSetting.mTextSize );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.checkBox.setOnClickListener( b );
    holder.checkBox.setChecked( b.isChecked() );
    holder.textView.setText( b.getSurveyName() );
    return convertView;
  }

}

