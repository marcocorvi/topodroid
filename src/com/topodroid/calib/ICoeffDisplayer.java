/* @file ICoeffDisplayer.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid DistoX coefficient diaplayer
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
    public void displayCoeff( TDVector bg, TDMatrix ag, TDVector bm, TDMatrix am, TDVector nL );
    public void enableButtons( boolean b );
    public boolean isActivityFinishing();
}
