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
package com.topodroid.DistoX;

import android.view.View;
import android.view.View.OnClickListener;

class JoinClickListener implements OnClickListener
{
  private int mIndex;
  private int mCode;
  private DrawingWindow mParent; // IJoinClickHandler mParent;

  /** cstr
   * @param parent   parent click handler
   * @param i        click index
   * @param c        click code
   */
  JoinClickListener( DrawingWindow parent, int i, int c ) 
  {
    mParent = parent;
    mIndex = i;
    mCode  = c;
  }

  /** react to a user tap - invoke the parent handling methods
   * @param v  tapped view
   */
  @Override
  public void onClick(View v) {
    mParent.setButtonJoinMode( mIndex, mCode );
    // mParent.dismissPopupJoin(); // already in setButtonJoinMode()
  }
}

