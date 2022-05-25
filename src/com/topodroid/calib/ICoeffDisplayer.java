/* @file ICoeffDisplayer.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid DistoX coefficient displayer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;

public interface ICoeffDisplayer
{
  void displayCoeff( TDVector bg, TDMatrix ag, TDVector bm, TDMatrix am, TDVector nL );
  void enableButtons( boolean b );
  boolean isActivityFinishing();
}
