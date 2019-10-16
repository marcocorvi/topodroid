/* @file ItemSymbol.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.ArrayList;

import android.content.Context;
// import android.app.Dialog;

// import android.graphics.Color;
import android.view.View.OnClickListener;

// import android.widget.RadioButton;
import android.widget.CheckBox;
// import android.widget.Button;
import android.widget.TextView;

// import android.view.LayoutInflater;
// import android.view.MotionEvent;
// import android.view.View;
// import android.view.View.OnTouchListener;
// import android.text.Layout;
import android.widget.LinearLayout;

// import android.util.Log;

class ItemSymbol
{
  int mType;   // symbol type POINT (0) LINE (1) AREA (2)
  int mIndex;  // symbol index
  CheckBox     mCheckBox = null;
  ItemButton   mButton   = null;
  // private TextView     mTextView = null;
  LinearLayout mView;
  SymbolInterface mSymbol;
  private float sx;
  private float sy;
  private boolean mUseText;

  // private Context mContext;

  ItemSymbol( Context context, IItemPicker dialog, int type, int index, SymbolInterface symbol, boolean use_text )
  {  
    mType  = type;
    mIndex = index;
    mSymbol = symbol;
    mUseText = use_text;
    int pad = 4;

    sx = Symbol.sizeX( mType );
    sy = Symbol.sizeY( mType );
    // Log.v( TopoDroidApp.TAG, "Item " + mType + "/" + mIndex + " " + mSymbol.getName() );

    LinearLayout ll = new LinearLayout( context );
    // ll.setOrientation( LinearLayout.HORIZONTAL );
    int lw = LinearLayout.LayoutParams.WRAP_CONTENT;
    int lh = LinearLayout.LayoutParams.WRAP_CONTENT;
    LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(lh,lw);
    if ( ! mUseText ) {
      lllp.gravity = 0x03; // left
    } else {
      mCheckBox = new CheckBox( context );
      mCheckBox.setChecked( false );
      ll.addView( mCheckBox, lllp );
    }
    lllp.setMargins(2,1,2,1);

    mButton   = new ItemButton( context, mSymbol.getPaint(), mSymbol.getPath(), sx, sy, pad );
    ll.addView( mButton, lllp );

    if ( mUseText ) {
      TextView textView = new TextView( context );
      // textView.setBackgroundColor( TDColor.BLACK );
      textView.setText( mSymbol.getName() ); // getFullName()
      ll.addView( textView, new LinearLayout.LayoutParams(lh,lw) );
    } else {
      mButton.setClickable( true );
    }

    // FIXME
    // if ( mType == Symbol.POINT ) {
    //   SymbolPoint point = (SymbolPoint) mSymbol;
    //   if ( point.isOrientable() ) {
    //     Button btn = new Button( context );
    //     btn.setBackgroundResource( R.drawable.ic_turn ); 
    //     ll.addView( btn, new LinearLayout.LayoutParams(lh,lw) );
    //   }
    // }

    mView = ll;

    // if ( mSymbol.isOrientable() ) {
    //   mView.setOnTouchListener( new OnTouchListener()
    //   {
    //     private float x_old=0f;

    //     @Override
    //     public boolean onTouch(View v, MotionEvent event) {
    //       int action = event.getAction();
    //       int actionCode = action & MotionEvent.ACTION_MASK;
    //       if ( actionCode == MotionEvent.ACTION_DOWN ) {
    //         x_old = event.getX(0);
    //       } else if ( actionCode == MotionEvent.ACTION_MOVE ) {
    //         float x = event.getX(0);
    //         float delta_x = x - x_old;
    //         x_old = x;
    //         notifyListener( delta_x );
    //       }
    //       return true;
    //     }
    //   });
    // }
  }

  // private void notifyListener( float dx ) 
  // {
  //   rotate( dx );
  // }

  // void rotate( float dx ) 
  // {
  //   // Log.v( TopoDroidApp.TAG, "rotate " + dx );
  //   mSymbol.rotate( dx );
  //   mButton.resetPath( mSymbol.getPath(), sx, sy );
  //   mView.invalidate();
  // }

  void setAngle( float angle )
  {
    // Log.v("DistoX", "item symbol " + mSymbol.getName() + " set angle " + angle );
    if ( mSymbol.setAngle( angle ) ) {
      mButton.resetPath( mSymbol.getPath(), sx, sy );
      mButton.invalidate();
    }
  }

  void setOnClickListener( OnClickListener listener ) 
  {
    if ( mUseText ) {
      mCheckBox.setOnClickListener( listener );
      // mButton.setOnClickListener( listener );
    } else {
      mButton.setOnClickListener( listener );
    }
  }
 
  void setChecked( boolean checked )
  {
    if ( mUseText ) {
      mCheckBox.setChecked( checked );
    } else {
      mButton.setBackgroundColor( checked? TDColor.DARK_GRAY : TDColor.BLACK ); 
      // if ( checked ) {
      //   mButton.setBackground( BrushManager.mSymbolHighlight );
      // } else {
      //   mButton.setBackgroundColor( TDColor.BLACK ); 
      // }
    }
  }

}

