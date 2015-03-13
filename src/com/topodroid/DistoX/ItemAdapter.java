/** @file ItemAdapter.java
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

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
// import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
// import android.view.LayoutInflater;
// import android.widget.RadioGroup;

import android.view.View.OnClickListener;

// import android.widget.LinearLayout;

import android.util.Log;

class ItemAdapter extends ArrayAdapter< ItemSymbol >
                  implements OnClickListener
{
  private ArrayList< ItemSymbol > mItems;
  // private Context mContext;
  private IItemPicker mParent;
  private int mPos;    

  /** get the item at a certain position in the list of symbols 
   */
  ItemSymbol get( int k )
  { 
    // Log.v("DistoX", "ItemAdapter get item at " + k + " of " + mItems.size() );
    return ( k < mItems.size() ) ? mItems.get(k) : null ;
  }

  public ItemAdapter( Context ctx, IItemPicker parent, int id, ArrayList< ItemSymbol > items )
  {
    super( ctx, id, items );
    mParent = parent;
    mPos    = -1;

    if ( items != null ) {
      mItems = items;
      for ( ItemSymbol item : items ) {
        item.setOnClickListener( this );
      }
    } else {
      mItems = new ArrayList< ItemSymbol >();
    }
  }

  void rotatePoint( int index, int angle )
  {
    for ( ItemSymbol b : mItems ) {
      if ( b.mIndex == index ) {
        b.rotate( angle );
        return;
      }
    }
  }

  // ItemSymbol get( int pos ) { return mItems.get(pos); }
  int getSelectedPos() { return mPos; }
  // public int size() { return mItems.size(); }

  // set selected position from the item index
  void setSelectedItem( int index )
  {
    mPos = -1;
    for ( int k=0; k<mItems.size(); ++k ) {
      ItemSymbol item = mItems.get(k);
      if ( index == item.mIndex ) {
        mPos = k;
        item.setChecked( true );
      } else {
        item.setChecked( false );
      }
    }
    if ( mPos == -1 ) {
      mPos = ( mItems.size() > 1 )? 1 : 0;
      mItems.get( mPos ).setChecked( true );
    }
  }

  // public ItemSymbol get( String name ) 
  // {
  //   for ( ItemSymbol sym : mItems ) {
  //     if ( sym.mName.equals( name ) ) return sym;
  //   }
  //   return null;
  // }

  public void add( ItemSymbol item ) 
  {
    mItems.add( item );
    item.setOnClickListener( this );
  }

  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    ItemSymbol b = mItems.get( pos );
    if ( b == null ) return convertView;
    return b.mView;
  }

  @Override
  public void onClick( View v )
  {
    // Log.v("DistoX", "ItemAdapter onClick()");
    doClick( v );
  }

  public void doClick( View v )
  {
    // Log.v("DistoX", "--> ItemAdapter doClick()");
    try {
      CheckBox cb = (CheckBox)v;
      if ( cb != null ) {
        int pos = 0;
        for ( ItemSymbol item : mItems ) {
          if ( cb == item.mCheckBox ) {
            mPos = pos; // item.mIndex;
            mParent.setTypeAndItem( mPos );
            item.setChecked( true );
          } else {
            item.setChecked( false );
          }
          ++ pos;
        }
      }
    } catch ( ClassCastException e ) {
      int pos = 0;
      ItemButton ib = (ItemButton)v;
      for ( ItemSymbol item : mItems ) {
        if ( ib == item.mButton ) {
          mPos = pos; // item.mIndex;
          mParent.setTypeAndItem( mPos );
          item.setChecked( true );
        } else {
          item.setChecked( false );
        }
        ++ pos;
      }
    }
  }

}

