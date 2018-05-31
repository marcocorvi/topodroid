/** @file FilterClickListener.java
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
package com.topodroid.DistoX;

import android.view.View;
import android.view.View.OnClickListener;

class FilterClickListener implements OnClickListener
{
  private int mIndex;
  private int mCode;
  private IFilterClickHandler mParent;

  FilterClickListener( IFilterClickHandler parent, int i, int c ) 
  {
    mParent = parent;
    mIndex = i;
    mCode  = c;
  }

  @Override
  public void onClick(View v) {
    mParent.setButtonFilterMode( mIndex, mCode );
    mParent.dismissPopupFilter();
  }
}

