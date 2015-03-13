/** @file SymbolAdapter.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.app.Activity;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.LinearLayout;

// import android.util.Log;

class SymbolAdapter extends ArrayAdapter< EnableSymbol >
{
  private ArrayList< EnableSymbol > mItems;
  // private Context mContext;
  private Activity mActivity;
  private LayoutInflater mLayoutInflater;

  SymbolAdapter( Activity ctx, int id, ArrayList< EnableSymbol > items )
  {
    super( ctx, id, items );
    // mContext = ctx;
    mActivity = ctx;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

    if ( items != null ) {
      mItems = items;
    } else {
      mItems = new ArrayList< EnableSymbol >();
    }
  }

  public EnableSymbol get( int pos ) { return mItems.get(pos); }

  public EnableSymbol get( String name ) 
  {
    for ( EnableSymbol sym : mItems ) {
      if ( sym.getName().equals( name ) ) return sym;
    }
    return null;
  }

  public void add( EnableSymbol sym ) 
  {
    mItems.add( sym );
  }

  private class ViewHolder
  { 
    CheckBox checkBox;
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    EnableSymbol b = mItems.get( pos );
    if ( b == null ) return convertView;

    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.enable_symbol, null );
      holder = new ViewHolder();
      holder.checkBox = (CheckBox) convertView.findViewById( R.id.enable_symbol_cb );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.checkBox.setChecked( b.mEnabled );
    holder.checkBox.setText( b.getName() );
    holder.checkBox.setOnClickListener( b );
    return convertView;
  }

  public int size() { return mItems.size(); }

  void updateSymbols( String prefix )
  {
    for ( EnableSymbol symbol : mItems ) {
      if ( symbol.MustSave() ) {
        symbol.mSymbol.setEnabled( symbol.mEnabled );
        TopoDroidApp.mData.setSymbolEnabled( prefix + symbol.mSymbol.getThName(), symbol.mSymbol.isEnabled() );
      }
    }
  }

}

