/* @file IItemPicker.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid item picker interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import android.view.View.OnClickListener;

interface IItemPicker 
          // extends OnClickListener
{
  void setTypeAndItem( int type, int pos );

  void closeDialog();
}
