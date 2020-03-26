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
package com.topodroid.DistoX;

import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;

interface ICoeffDisplayer
{
    void displayCoeff( TDVector bg, TDMatrix ag, TDVector bm, TDMatrix am, TDVector nL );
    void enableButtons( boolean b );
    boolean isActivityFinishing();
}
