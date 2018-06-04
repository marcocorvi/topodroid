/* @file ILabelAdder.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid label adder interfare
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

interface ILabelAdder
{
  void addLabel( String text, float x, float y );

  void addPhotoPoint( String text, float x, float y );
}
