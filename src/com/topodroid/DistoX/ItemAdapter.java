/* @file ItemAdapter.java
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

// import android.util.Log;

class ItemAdapter extends ArrayAdapter< ItemSymbol >
                  implements OnClickListener
{
  private ArrayList< ItemSymbol > mItems;
  // private Context mContext;
  private IItemPicker mParent;
  private int mPos;    
  private int mType;
  private boolean mShowSelected;

  ItemAdapter( Context ctx, IItemPicker parent, int type, int id, ArrayList< ItemSymbol > items )
  {
    super( ctx, id, items );
    mParent = parent;
    mPos    = -1;
    mType   = type;
    mShowSelected = true;

    if ( items != null ) { // always true
      mItems = items;
      for ( ItemSymbol item : items ) {
        item.setOnClickListener( this );
      }
    } else {
      mItems = new ArrayList<>();
    }
  }

  int size() { return mItems.size(); }

  // void rotateItem( int index, int angle )
  // {
  //   for ( ItemSymbol b : mItems ) {
  //     if ( b.mIndex == index ) {
  //       b.rotate( angle );
  //       return;
  //     }
  //   }
  // }
  
  private boolean isValid( int p ) { return p >= 0 && p < mItems.size(); }

  void setItemOrientation( int pos, int angle ) { if ( isValid(pos) ) { mItems.get( pos ).setAngle( angle ); } }

  void setShowSelected( boolean s ) 
  { 
    mShowSelected = s;
    if ( isValid(mPos) ) {
      mItems.get( mPos ).setChecked( mShowSelected );
    }
  }

  // get the item at a certain position in the list of symbols 
  ItemSymbol get( int k ) { return ( k < mItems.size() ) ? mItems.get(k) : null ; }

  // ItemSymbol get( int pos ) { return mItems.get(pos); }
  
  int getSelectedPos() { return mPos; }

  ItemSymbol getSelectedItem() { return ( isValid(mPos) )? mItems.get(mPos) : null; }

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
      mPos = ( mItems.size() > 1 )? 1 : 0; // user symbols cannot be disabled: are always present
      mItems.get( mPos ).setChecked( true );
    }
  }

  /** set selected position from the item symbol
   * @param symbol   selected symbol
   * @return index of selected symbol ie item.mIndex
   */
  int setSelectedItem( Symbol symbol )
  {
    mPos = -1;
    for ( int k=0; k<mItems.size(); ++k ) {
      ItemSymbol item = mItems.get(k);
      if ( symbol == item.mSymbol ) {
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
    return mItems.get( mPos ).mIndex;
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
    return ( b == null )? convertView : b.mView;
  }

  @Override
  public void onClick( View v )
  {
    // Log.v("DistoX", "ItemAdapter onClick()");
    doClick( v );
  }

  private long mClickMillis = 0;
  static final private int DOUBLE_CLICK_TIME = 400;

  private void doClick( View v )
  {
    // Log.v("DistoX", "--> ItemAdapter doClick()");
    long millis = System.currentTimeMillis();
    boolean doubleclick = false;
    try {
      CheckBox cb = (CheckBox)v;
      if ( cb != null ) {
        int pos = 0;
        for ( ItemSymbol item : mItems ) {
          if ( cb == item.mCheckBox ) {
            if ( mPos == pos && Math.abs(millis - mClickMillis) < DOUBLE_CLICK_TIME ) doubleclick = true;
            mPos = pos; // item.mIndex;
            mParent.setTypeAndItem( mType, mPos );
            item.setChecked( true );
          } else {
            item.setChecked( false );
          }
          ++ pos;
        }
      }
    } catch ( ClassCastException e ) {
      try {
        ItemButton ib = (ItemButton)v;
        int pos = 0;
        for (ItemSymbol item : mItems) {
          if (ib == item.mButton) {
            if (mPos == pos && Math.abs( millis - mClickMillis ) < DOUBLE_CLICK_TIME)
              doubleclick = true;
            mPos = pos; // item.mIndex;
            mParent.setTypeAndItem( mType, mPos );
            item.setChecked( true );
          } else {
            item.setChecked( false );
          }
          ++pos;
        }
      } catch ( ClassCastException ee ) {
        TDLog.Error("View is neither CheckBox nor ItemButton");
      }
    }
    if ( doubleclick ) mParent.closeDialog();
    mClickMillis = millis;
  }

}

