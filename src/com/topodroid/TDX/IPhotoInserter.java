/* @file IPhotoInserter.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid label adder interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import android.graphics.Bitmap;

interface IPhotoInserter
{
  /** insert the photo
   */
  boolean insertPhoto( );

  void insertPhotoBitmap( Bitmap bitmap );
}
