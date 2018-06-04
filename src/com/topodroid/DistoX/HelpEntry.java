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
package com.topodroid.DistoX;

import android.content.Context;
// import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.graphics.drawable.BitmapDrawable;

class HelpEntry
{
  private Button   mButton   = null;
  private TextView mTextView = null;
  LinearLayout mView;

  /**
   * @param context     display context
   * @param icon        button resource int
   * @param text        text resource int
   */
  HelpEntry( Context context, int icon, int text, boolean is_text )
  {
    mButton   = new Button( context );
    mTextView = new TextView( context );

    if ( is_text ) {
      mButton.setText( icon );
      mButton.setBackgroundColor( 0x00000000 );
      mButton.setTextColor( 0xff33ccff ); // FIXME color/menu_foreground
    } else {
      // int size = TopoDroidApp.getDefaultSize( context );
      mButton.setBackgroundDrawable( MyButton.getButtonBackground( context, context.getResources(), icon ) );
    }
    mTextView.setText( text );

    LinearLayout ll = new LinearLayout( context );
    // ll.setOrientation( LinearLayout.HORIZONTAL );
    int lw = LinearLayout.LayoutParams.WRAP_CONTENT;
    int lh = LinearLayout.LayoutParams.WRAP_CONTENT;

    ll.addView( mButton,   new LinearLayout.LayoutParams(lh,lw) );
    ll.addView( mTextView, new LinearLayout.LayoutParams(lh,lw) );
    mView = ll;
  }

}

