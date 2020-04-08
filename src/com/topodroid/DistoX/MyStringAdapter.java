/* @file MyStringAdapter.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid simple string adapter
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.prefs.TDSetting;

import android.content.Context;

import android.widget.TextView;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.ArrayList;

class MyStringAdapter extends BaseAdapter
{
  private final Context mContext;
  private int     mResId;
  private ArrayList< String > mStr;

  MyStringAdapter( Context context, int res_id )
  {
    mContext = context;
    mResId   = res_id;
    mStr     = new ArrayList<>();
  }

  @Override public int getCount() { return mStr.size(); }

  @Override public String getItem( int pos ) { return mStr.get(pos); }

  @Override  public long getItemId( int pos ) { return pos; }

  void add( String str ) { mStr.add( str ); }

  void remove( String str ) { mStr.remove( str ); }

  class Holder { TextView tv; }

  void clear() { mStr.clear(); }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    Holder holder;
    if  ( convertView == null ) {
      holder = new Holder();
      convertView = LayoutInflater.from( mContext ).inflate( mResId, parent, false );
      holder.tv = (TextView) convertView.findViewById( R.id.message_text );
      holder.tv.setTextSize( TDSetting.mTextSize );
      convertView.setTag( holder );
    } else {
      holder = (Holder) convertView.getTag();
    }
    holder.tv.setText( mStr.get(pos) );
    return convertView;
  }

}

