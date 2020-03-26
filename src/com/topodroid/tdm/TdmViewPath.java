/** @file TdmViewPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager path display object
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Canvas;
import android.graphics.Matrix;

class TdmViewPath
{
  TdmViewStation mSt1;
  TdmViewStation mSt2;
  Path mPath;

  TdmViewPath( TdmViewStation st1, TdmViewStation st2 )
  {
    mSt1 = st1;
    mSt2 = st2;
    mPath = new Path();
    mPath.moveTo( st1.x, st1.y );
    mPath.lineTo( st2.x, st2.y );
  }

  void draw( Canvas canvas, Matrix matrix, Paint paint )
  {
    Path path = new Path( mPath );
    path.transform( matrix );
    canvas.drawPath( path, paint );
  }
}
