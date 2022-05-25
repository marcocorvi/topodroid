/* @file EnableSymbol.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid enabled symbol(s)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;


import android.content.Context;

import android.widget.CheckBox;

import android.view.View;

class EnableSymbol implements View.OnClickListener
{
  private int mType;   // symbol type POINT (0) LINE (1) AREA (2)
  // int mIndex;  // symbol index
  boolean mEnabled;
  SymbolInterface mSymbol;
  float sx;
  float sy;

  /** @return true is the enabled value must be saved
   */
  boolean MustSave()
  {
    return mEnabled != mSymbol.isEnabled();
  }

  /** set the value of the enabled
   * @param enabled   new enabled value
   */
  void setEnabled( boolean enabled ) { mEnabled = enabled; }

  /** react to user taps
   * @param v   tapped view
   */
  @Override
  public void onClick( View v ) 
  {
    if ( mSymbol.getThName().equals(SymbolLibrary.USER) ) {
      mEnabled = true;
      ((CheckBox)v).setChecked( true ); // true = mEnabled
    } else {
      mEnabled = ! mEnabled;
    }
  }

  /** @return the symbol name
   */
  String getName()  { return mSymbol.getName(); }

  /** @return the symbol group
   */
  String getGroupName()  { return mSymbol.getGroupName(); }

  // boolean getEnabled() { return mSymbol.isEnabled(); }
  // void setEnabled( boolean enabled ) { mSymbol.mEnabled = enabled; }

  /** cstr
   * @param context  context
   * @param type     symbol type
   * @param index    symbol index in the library (unused)
   * @param symbol   symbol
   */
  EnableSymbol( Context context, int type, int index, SymbolInterface symbol )
  {  
    mType  = type;
    // mIndex = index;
    // mMustSave = false;
    mSymbol = symbol;
    mEnabled = mSymbol.isEnabled();
    sx = Symbol.sizeX( mType );
    sy = Symbol.sizeY( mType );
  }

}


