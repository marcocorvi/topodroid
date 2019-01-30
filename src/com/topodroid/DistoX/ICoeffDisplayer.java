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

interface ICoeffDisplayer
{
    void displayCoeff( Vector bg, Matrix ag, Vector bm, Matrix am, Vector nL );
    void enableButtons( boolean b );
    boolean isActivityFinishing();
}
