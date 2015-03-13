/** @file EnableSymbol.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid enabled symbol(s)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.content.Context;
// import android.app.Dialog;

import android.widget.CheckBox;
// import android.widget.Button;
import android.widget.TextView;

import android.view.LayoutInflater;
// import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
// import android.view.View.OnTouchListener;
// import android.text.Layout;
import android.widget.LinearLayout;

// import android.util.Log;

class EnableSymbol implements View.OnClickListener
{
  int mType;   // symbol type POINT (0) LINE (1) AREA (2)
  // int mIndex;  // symbol index
  boolean mEnabled;
  SymbolInterface mSymbol;
  float sx = 1.0f;
  float sy = 1.0f;

  boolean MustSave()
  {
    return mEnabled != mSymbol.isEnabled();
  }

  // CheckBox     mCheckBox = null;
  // // ItemButton   mButton   = null;
  // TextView     mTextView = null;
  // LinearLayout mView = null;

  // private Context mContext;

  @Override
  public void onClick( View v ) 
  {
    if ( mSymbol.getThName().equals("user") ) {
      mEnabled = true;
      ((CheckBox)v).setChecked( mEnabled );
    } else {
      mEnabled = ! mEnabled;
    }
  }

  // String getName()  { return mSymbol.mThName; }
  String getName()  { return mSymbol.getName(); }

  // boolean getEnabled() { return mSymbol.isEnabled(); }
  // void setEnabled( boolean enabled ) { mSymbol.mEnabled = enabled; }

  public EnableSymbol( Context context, int type, int index, SymbolInterface symbol )
  {  
    mType  = type;
    // mIndex = index;
    // mMustSave = false;
    mSymbol = symbol;
    mEnabled = mSymbol.isEnabled();

    if ( mType == DrawingActivity.SYMBOL_POINT ) {
      sx = 2.0f;
      sy = 2.0f;
    } else if ( mType == DrawingActivity.SYMBOL_AREA ) {
      sx = 2.5f;
      sy = 1.7f;
    }
  }

}


