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

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.ui.ItemButton;
// import com.topodroid.common.SymbolType;

// import java.util.ArrayList;

import android.content.Context;

import android.view.View.OnClickListener;

import android.widget.CheckBox;
import android.widget.TextView;

// import android.text.Layout;
import android.widget.LinearLayout;

class ItemSymbol
{
  // private OnClickListener mListener = null;
  int mType;   // symbol type POINT (1) LINE (2) AREA (3)
  int mIndex;  // symbol index
  CheckBox     mCheckBox = null;
  ItemButton   mButton   = null;
  // private TextView     mTextView = null;
  LinearLayout mView;
  SymbolInterface mSymbol;
  private float sx;
  private float sy;
  // private boolean mUseText;

  // private Context mContext;

  /** cstr
   * @param context   context
   * @param dialog    parent dialog
   * @param type      symbol class (POINT, LINE, AREA)
   * @param index     symbol index
   * @param symbol    symbol interface
   */
  ItemSymbol( Context context, IItemPicker dialog, int type, int index, SymbolInterface symbol )
  {  
    mType  = type;
    mIndex = index;
    mSymbol = symbol;
    int pad = 4;

    sx = Symbol.sizeX( mType );
    sy = Symbol.sizeY( mType );
    // TDLog.v( "Item " + mType + "/" + mIndex + " " + mSymbol.getName() );

    LinearLayout ll = new LinearLayout( context );
    // ll.setOrientation( LinearLayout.HORIZONTAL );
    int lw = LinearLayout.LayoutParams.WRAP_CONTENT;
    int lh = LinearLayout.LayoutParams.WRAP_CONTENT;
    LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(lh,lw);
    // if ( ! mUseText ) {
    //   lllp.gravity = 0x03; // left
    // } else {
      mCheckBox = new CheckBox( context );
      mCheckBox.setChecked( false );
      ll.addView( mCheckBox, lllp );
    // }
    lllp.setMargins(2,1,2,1);

    mButton   = new ItemButton( context, mSymbol.getPaint(), mSymbol.getPath(), sx, sy, pad );
    ll.addView( mButton, lllp );

    // if ( mUseText ) {
      TextView textView = new TextView( context );
      // textView.setBackgroundColor( TDColor.BLACK );
      textView.setText( mSymbol.getName() ); // getFullName()
      ll.addView( textView, new LinearLayout.LayoutParams(lh,lw) );
    // } else {
    //   mButton.setClickable( true );
    // }

    // FIXME
    // if ( mType == SymbolType.POINT ) {
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
  //   // TDLog.v( "Item " + mType + "/" + mIndex + " rotate " + dx );
  //   mSymbol.rotate( dx );
  //   mButton.resetPath( mSymbol.getPath(), sx, sy );
  //   mView.invalidate();
  // }

  /** set the symbol orientatiomn angle
   * @param angle   orientation angle [degree]
   */
  void setAngle( float angle )
  {
    // TDLog.v( "item " + mType + "/" + mIndex + " " + mSymbol.getName() + " set angle " + angle );
    if ( mSymbol.setAngle( angle ) ) {
      mButton.resetPath( mSymbol.getPath(), sx, sy );
      mButton.invalidate();
    }
  }

  /** set the click listener
   */
  void setOnClickListener( OnClickListener listener ) 
  {
    // mListener = listener;
    // if ( mUseText ) {
      mCheckBox.setOnClickListener( listener );
      // mButton.setOnClickListener( listener );
    // } else {
    //   mButton.setOnClickListener( listener );
    // }
  }

  // /** clear teh checkbox
  //  */
  // void clearChecked()
  // {
  //   TDLog.v( "Item " + mType + "/" + mIndex + " clear checked " );
  //   // if ( mUseText ) {
  //     mCheckBox.setChecked( false );
  //   // }
  // }

  /** set the item checked or not
   * @param checked     whether to set the item checked
   */
  void setItemChecked( boolean checked )
  {
    TDLog.v( "Item " + mType + "/" + mIndex + " set checked " + checked  );
    // if ( mUseText ) {
      mCheckBox.setChecked( checked );
    // } else {
    //   mButton.setBackgroundColor( checked? TDColor.DARK_GRAY : TDColor.BLACK ); 
    //   // if ( checked ) {
    //   //   mButton.setBackground( BrushManager.mSymbolHighlight );
    //   // } else {
    //   //   mButton.setBackgroundColor( TDColor.BLACK ); 
    //   // }
    // }
  }

}

