/* @file PopupClickListener.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid popup click listener
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

// import com.topodroid.ui.IColorClickHandler;

import android.view.View;
import android.view.View.OnClickListener;

public class PopupClickListener implements OnClickListener
{
  private final int mIndex; // entry index
  private final int mCode;  // popup code
  private final IPopupClickHandler mParent; // click handler

  public PopupClickListener( IPopupClickHandler parent, int i, int c ) 
  {
    mParent = parent;
    mIndex = i;
    mCode  = c;
  }

  @Override
  public void onClick(View v) {
    mParent.setButtonPopupMode( mIndex, mCode );
    mParent.dismissPopup( mCode );
  }
}

