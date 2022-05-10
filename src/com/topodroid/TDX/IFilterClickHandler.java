/* @file IFilterClickHandler.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid filter-click handler interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

interface IFilterClickHandler
{
  void setButtonFilterMode( int filter_mode, int code );

  boolean dismissPopupFilter();
}
