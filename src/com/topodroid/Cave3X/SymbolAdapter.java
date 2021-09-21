/* @file SymbolAdapter.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.ItemButton;

import java.util.ArrayList;

// import android.app.Activity;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;


class SymbolAdapter extends ArrayAdapter< EnableSymbol >
{
  private ArrayList< EnableSymbol > mItems;
  // private Context mContext;
  // private Activity mActivity;
  private LayoutInflater mLayoutInflater;

  SymbolAdapter( Context ctx, int id, ArrayList< EnableSymbol > items )
  {
    super( ctx, id, items );
    // mContext = ctx;
    // mActivity = ctx;
    mLayoutInflater = (LayoutInflater)ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

    if ( items != null ) { // ALWAYS true
      mItems = items;
    } else {
      mItems = new ArrayList<>();
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
    CheckBox     mCheckBox;
    ItemButton   mButton;
    TextView     mTextView;
    TextView     mGroupView;
    EnableSymbol mSymbol;
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    EnableSymbol b = mItems.get( pos );
    if ( b == null ) return convertView;

    ViewHolder holder = null; 
    if ( convertView == null ) {
      convertView = mLayoutInflater.inflate( R.layout.enable_symbol_x, parent, false );
      holder = new ViewHolder();
      holder.mCheckBox = (CheckBox) convertView.findViewById( R.id.enable_symbol_cb );
      holder.mButton   = (ItemButton) convertView.findViewById( R.id.enable_symbol_bt );
      holder.mTextView = (TextView) convertView.findViewById( R.id.enable_symbol_tv );
      holder.mGroupView = (TextView) convertView.findViewById( R.id.enable_symbol_grp );
      convertView.setTag( holder );
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    holder.mSymbol = b;
    holder.mCheckBox.setChecked( b.mEnabled );
    holder.mCheckBox.setOnClickListener( b );
    // holder.mCheckBox.setText( b.getName() );
    holder.mTextView.setText( b.getName() );
    holder.mGroupView.setText( b.getGroupName() );
    holder.mButton.resetPaintPath( b.mSymbol.getPaint(), b.mSymbol.getPath(), b.sx, b.sy );
    convertView.invalidate();
    return convertView;
  }

  public int size() { return mItems.size(); }

  void updateSymbols( String prefix )
  {
    if ( mItems.size() > 0 ) mItems.get(0).setEnabled( true ); // user symbols are always enabled

    for ( EnableSymbol symbol : mItems ) {
      if ( symbol.MustSave() ) {
        symbol.mSymbol.setEnabled( symbol.mEnabled );
        TopoDroidApp.mData.setSymbolEnabled( prefix + symbol.mSymbol.getThName(), symbol.mSymbol.isEnabled() );
      }
    }
  }

}

