/* @file JoinClickListener.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid filter-click listener
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import android.view.View;
import android.view.View.OnClickListener;

class JoinClickListener implements OnClickListener
{
  private int mIndex;
  private int mCode;
  private IJoinClickHandler mParent;

  JoinClickListener( IJoinClickHandler parent, int i, int c ) 
  {
    mParent = parent;
    mIndex = i;
    mCode  = c;
  }

  @Override
  public void onClick(View v) {
    mParent.setButtonJoinMode( mIndex, mCode );
    mParent.dismissPopupJoin();
  }
}

