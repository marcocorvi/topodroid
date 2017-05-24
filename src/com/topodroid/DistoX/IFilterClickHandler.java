/** @file IFilterClickHandler.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid filter-click handler interfare
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

public interface IFilterClickHandler
{
  void setButtonFilterMode( int filter_mode, int code );
  boolean dismissPopupFilter();
}
