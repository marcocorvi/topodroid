/* @file HelpEntry
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid help dialog item entry
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.help;

import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyButton;
import com.topodroid.TDX.TDandroid;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

class HelpEntry
{
  // private Button   mButton   = null;
  // private TextView mTextView = null;
  LinearLayout mView;

  /** cstr
   * @param context     display context
   * @param text        button resource int, either text or icon
   * @param decs        description resource int
   * @param is_text     whether the adapter is for a MENU item (or an ICON item)
   */
  HelpEntry( Context context, int text, int decs, boolean is_text )
  {
    Button mButton   = new Button( context );
    TextView mTextView = new TextView( context );

    if ( is_text ) {
      mButton.setText( text );
      mButton.setBackgroundColor( TDColor.TRANSPARENT );
      mButton.setTextColor( 0xff66a8dd ); // FIXME color/menu_foreground
    } else {
      // int size = TopoDroidApp.getDefaultSize( context );
      TDandroid.setButtonBackground( mButton, MyButton.getButtonBackground( context, context.getResources(), text ) );
    }
    mTextView.setText( decs );

    LinearLayout ll = new LinearLayout( context );
    // ll.setOrientation( LinearLayout.HORIZONTAL );
    int lw = LinearLayout.LayoutParams.WRAP_CONTENT;
    int lh = LinearLayout.LayoutParams.WRAP_CONTENT;

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(lh,lw);
    lp.setMarginStart( 10 );
    lp.setMarginEnd( 20 );

    ll.addView( mButton,   lp );
    ll.addView( mTextView, new LinearLayout.LayoutParams(lh,lw) );
    mView = ll;
  }

}

