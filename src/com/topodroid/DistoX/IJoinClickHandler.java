/* @file IJoinClickHandler.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid join-click handler interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

interface IJoinClickHandler
{
  void setButtonJoinMode( int join_mode, int code );
  boolean dismissPopupJoin();
}

