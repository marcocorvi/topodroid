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
package com.topodroid.DistoX;

// import android.util.Log;


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

  boolean MustSave()
  {
    return mEnabled != mSymbol.isEnabled();
  }

  void setEnabled( boolean enabled ) { mEnabled = enabled; }

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

  String getName()  { return mSymbol.getName(); }
  String getGroupName()  { return mSymbol.getGroupName(); }

  // boolean getEnabled() { return mSymbol.isEnabled(); }
  // void setEnabled( boolean enabled ) { mSymbol.mEnabled = enabled; }

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


