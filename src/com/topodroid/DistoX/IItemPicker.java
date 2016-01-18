/** @file IItemPicker.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid item picker interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.view.View.OnClickListener;

interface IItemPicker 
          // extends OnClickListener
{
  public void setTypeAndItem( int pos );

  public void closeDialog();
}
