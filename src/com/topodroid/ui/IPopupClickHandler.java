/* @file IPopupClickHandler.java
 *
 * @author marco corvi
 * @date oct 2026
 *
 * @brief TopoDroid popup-click handler interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

public interface IPopupClickHandler
{
  public void setButtonPopupMode( int popup_mode, int code );

  public boolean dismissPopup( int code );
}
