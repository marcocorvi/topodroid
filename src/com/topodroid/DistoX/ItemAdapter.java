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

import com.topodroid.utils.TDLog;
import com.topodroid.ui.ItemButton;
import com.topodroid.common.SymbolType;

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

// import androidx.annotation.RecentlyNonNull;

// import android.widget.LinearLayout;

class ItemAdapter extends ArrayAdapter< ItemSymbol >
                  implements OnClickListener
{
  private ArrayList< ItemSymbol > mItems;
  // private Context mContext;
  private IItemPicker mParent;
  private int mPos;    // position of selected item
  private int mType;
  private boolean mShowSelected; // whether to show selected item
  // private int mNonClick = -1;
  // private boolean mOnClickReact = true;

  /** cstr
   * @param ctx
   * @param parent  parent dialog
   * @param type    symbol type (POINT, LINE, AREA)
   * @param id      layout resource id
   * @param items
   */
  ItemAdapter( Context ctx, IItemPicker parent, int type, int id, ArrayList< ItemSymbol > items )
  {
    super( ctx, id, items );
    mParent = parent;
    mPos    = -1;
    mType   = type;
    mShowSelected = true;
    // mNonClick = -1;

    if ( items != null ) { // always true
      mItems = items;
      for ( int k=0; k<items.size(); ++k ) {
        ItemSymbol item = items.get( k );
        // if ( mType == SymbolType.POINT && BrushManager.isPointSection( item.mIndex ) ) {
        //   mNonClick = k;
        //   continue;
        // }
        item.setOnClickListener( this );
      }
    } else {
      mItems = new ArrayList<>();
    }
  }

  // /** set whether to react to a checkbox click
  //  * @param react  whether to react to a checkbox click
  //  */
  // void setOnClickReact( boolean react ) { mOnClickReact = react; }

  /** @return the number of items on this adapter
   */
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
  
  /** @return true if the index is valid, ie, between 0 and the number of items
   * @param p   index
   */
  private boolean isValid( int p ) { return p >= 0 && p < mItems.size() /* && p != mNonClick */ ; }

  /** set anitem orientation
   * @param pos    item position (index)
   * @param angle  orientation angle [degrees]
   */
  void setItemOrientation( int pos, int angle ) { if ( isValid(pos) ) { mItems.get( pos ).setAngle( angle ); } }

  /** set whether to show selected item
   * @param s   whether to show selected item or not
   */
  void setShowSelected( boolean s ) 
  { 
    TDLog.v("Adapter " + mType + " show selected " + s + " pos " + mPos );
    mShowSelected = s;
    if ( isValid(mPos) ) {
      // setOnClickReact( false );
      mItems.get( mPos ).setItemChecked( mShowSelected );
      // setOnClickReact( true );
    }
  }

  /** @return the item at a certain position in the list of symbols 
   * @param k  itemindex
   */
  ItemSymbol get( int k ) 
  { 
    TDLog.v("Adapter " + mType + " get item at " + k + " of " + mItems.size() );
    return ( k < mItems.size() ) ? mItems.get(k) : null ; 
  }

  // ItemSymbol get( int pos ) { return mItems.get(pos); }
  
  /** @return the index of the selected item
   */
  int getSelectedPos() 
  {
    TDLog.v("Adapter " + mType + " get selected position " + mPos );
    return mPos;
  }

  /** @return the selected item
   */
  ItemSymbol getSelectedItem() { return ( isValid(mPos) )? mItems.get(mPos) : null; }

  // /** clear selected position from the item index
  //  * @param index   index of item to clear
  //  */
  // void clearSelectedItem( int index )
  // {
  //   TDLog.v("Adapter " + mType + " clear " + index );
  //   for ( int k=0; k<mItems.size(); ++k ) {
  //     // if ( k == mNonClick ) continue;
  //     ItemSymbol item = mItems.get(k);
  //     if ( index == item.mIndex ) {
  //       item.clearChecked( );
  //       break;
  //     }
  //   }
  // }

  /** set selected position from the item index
   * @param index   index of item to select
   */
  void setSelectedItem( int index )
  {
    TDLog.v("Adapter " + mType + " select " + index );
    // setOnClickReact( false );
    mPos = -1;
    for ( int k=0; k<mItems.size(); ++k ) {
      // if ( k == mNonClick ) continue;
      ItemSymbol item = mItems.get(k);
      if ( index == item.mIndex ) {
        mPos = k;
        item.setItemChecked( true );
      } else {
        item.setItemChecked( false );
      }
    }
    if ( mPos == -1 ) {
      mPos = ( mItems.size() > 1 )? 1 : 0; // user symbols cannot be disabled: are always present
      mItems.get( mPos ).setItemChecked( true );
    }
    // TDLog.v("set selected at index " + index + " pos " + mPos );
    // setOnClickReact( true );
    TDLog.v("Adapter " + mType + " select done " + mPos );
  }

  /** set selected position from the item symbol
   * @param symbol   selected symbol
   * @return index of selected symbol ie item.mIndex
   */
  int setSelectedItem( Symbol symbol )
  {
    TDLog.v("Adapter " + mType + " set selected symbol ");
    // setOnClickReact( false );
    mPos = -1;
    for ( int k=0; k<mItems.size(); ++k ) {
      // if ( k == mNonClick ) continue;
      ItemSymbol item = mItems.get(k);
      if ( symbol == item.mSymbol ) {
        mPos = k;
        item.setItemChecked( true );
      } else {
        item.setItemChecked( false );
      }
    }
    if ( mPos == -1 ) {
      mPos = ( mItems.size() > 1 )? 1 : 0;
      mItems.get( mPos ).setItemChecked( true );
    }
    // setOnClickReact( true );
    TDLog.v("Adapter " + mType + " set selected symbol " + mPos + " return " + mItems.get( mPos ).mIndex);
    return mItems.get( mPos ).mIndex;
  }

  // public ItemSymbol get( String name ) 
  // {
  //   for ( ItemSymbol sym : mItems ) {
  //     if ( sym.mName.equals( name ) ) return sym;
  //   }
  //   return null;
  // }

  /** add an item
   * @param item   item to add
   */
  public void add( ItemSymbol item ) 
  {
    // if ( mType == SymbolType.POINT && BrushManager.isPointSection( item.mIndex ) ) mNonClick = mItems.size();
    mItems.add( item );
    item.setOnClickListener( this );
  }

  /** @return the view for an item
   * @param pos         item position
   * @param convertView convertible view or null
   * @param parent      parent group for the view
   */
  // @RecentlyNonNull
  @Override
  public View getView( int pos, View convertView, ViewGroup parent )
  {
    ItemSymbol b = mItems.get( pos );
    return ( b == null )? convertView : b.mView;
  }

  /** react to a user tap: forward to doClick()
   * @param v  tapped  view
   */
  @Override
  public void onClick( View v )
  {
    TDLog.v( "Adapter " + mType + " onClick()");
    doClick( v );
  }

  private long mClickMillis = 0;
  static final private int DOUBLE_CLICK_TIME = 400;

  /** react to a user tap
   * @param v  tapped  view
   */
  private void doClick( View v )
  {
    // TDLog.v( "Adapter " + mType + " do click - react " + mOnClickReact );
    TDLog.v( "Adapter " + mType + " do click " );
    long millis = System.currentTimeMillis();
    boolean doubleclick = false;
    if ( v instanceof CheckBox ) {
      CheckBox cb = (CheckBox)v;
      if ( /* mOnClickReact && */ cb != null ) {
        int pos = 0;
        for ( ItemSymbol item : mItems ) {
          if ( cb == item.mCheckBox ) {
            if ( mPos == pos && Math.abs(millis - mClickMillis) < DOUBLE_CLICK_TIME ) doubleclick = true;
            mPos = pos; // item.mIndex;
            // TDLog.v("set type and item [1]: pos " + mPos + " index " + item.mIndex );
            mParent.setTypeAndItem( mType, mPos );
            item.setItemChecked( true );
          } else {
            item.setItemChecked( false );
          }
          ++ pos;
        }
      }
    } else if ( v instanceof ItemButton ) {
      ItemButton ib = (ItemButton)v;
      int pos = 0;
      for (ItemSymbol item : mItems) {
        if (ib == item.mButton) {
          if (mPos == pos && Math.abs( millis - mClickMillis ) < DOUBLE_CLICK_TIME)
            doubleclick = true;
          mPos = pos; // item.mIndex;
          // TDLog.v("set type and item [2]: pos " + mPos + " index " + item.mIndex );
          mParent.setTypeAndItem( mType, mPos );
          item.setItemChecked( true );
        } else {
          item.setItemChecked( false );
        }
        ++pos;
      }
    } else {
      TDLog.Error("View is neither CheckBox nor ItemButton");
    }
    if ( doubleclick ) mParent.closeDialog();
    mClickMillis = millis;
  }

}

