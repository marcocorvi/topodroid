/* @file TDLayout.java
 *
 * @author marco corvi
 * @date june 2018
 *
 * @grief layout utilities
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;

class TDLayout
{
  static LayoutParams getLayoutParamsFill( int l, int t, int r, int b )
  {
    LayoutParams lp = new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT );
    lp.setMargins( l, t, r, b );
    return lp;
  }

  static LayoutParams getLayoutParams( int l, int t, int r, int b )
  {
    LayoutParams lp = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
    lp.setMargins( l, t, r, b );
    return lp;
  }

  static void setMargins( Button btn, int l, int t, int r, int b )
  {
    LayoutParams lp = (LayoutParams) btn.getLayoutParams();
    lp.setMargins( l, t, r, b );
    btn.setLayoutParams( lp );
  }
}

