/** @file SymbolInterface.java
 *
 */
package com.topodroid.DistoX;

import android.graphics.Paint;
import android.graphics.Path;

public interface SymbolInterface
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
  void setAngle( float angle );
  int getAngle();
}

