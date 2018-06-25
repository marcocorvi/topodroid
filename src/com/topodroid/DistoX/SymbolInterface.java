/* @file SymbolInterface.java
 *
 * @author marco corvi
 * @date 
 *
 * @brief TopoDroid drawing symbol interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Paint;
import android.graphics.Path;

interface SymbolInterface
{
  String getFilename();
  String getName();
  String getThName();
  Paint  getPaint();
  Path   getPath();
  boolean isOrientable();
  boolean isEnabled();
  void setEnabled( boolean enabled );
  void toggleEnabled();
  // void rotate( float angle );
  boolean setAngle( float angle ); // return true if symbol has rotated
  int getAngle();
}

