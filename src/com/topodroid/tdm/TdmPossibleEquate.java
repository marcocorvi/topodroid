/* @file TdmPossibleEquate.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager possible equate between two surveys
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Canvas;
import android.graphics.Matrix;

/** struct for a possible equate, holding two stations of different surveys
 */
class TdmPossibleEquate
{
  TdmViewStation mStation1;
  TdmViewStation mStation2;

  Path mPath;

  /** cstr
   * @param st1  first station
   * @param st2  second station
   * @note the two stations must belong to different surveys
   */
  TdmPossibleEquate( TdmViewStation st1, TdmViewStation st2 )
  {
    assert( st1.survey() != st2.survey() );
    mStation1 = st1;
    mStation2 = st2;
    makePath();
  }

  boolean contains( TdmViewStation st ) 
  {
    return st == mStation1 || st == mStation2;
  }

  String getStationFullname( int k )
  {
    return ( k == 1 )? mStation1.fullname() : mStation2.fullname();
  }

  private void makePath()
  {
    mPath = new Path();
    mPath.moveTo( mStation1.fullX(), mStation1.fullY() );
    mPath.lineTo( mStation2.fullX(), mStation2.fullY() );
  }

  /** draw the possible equate on the display
   * @param canvas   display canvas
   * @param matrix   transform matrix
   * @param paint    drawing paint
   * @note this is the same as TdmViewEquate draw()
   */
  void draw( Canvas canvas, Matrix matrix, Paint paint )
  {
    if ( mPath != null ) {
      Path path = new Path( mPath );
      path.transform( matrix );
      canvas.drawPath( path, paint );
    }
  }

  /** shift the possible equate
   * @param dx   X shift [canvas ?]
   * @param dy   Y shift
   * @param command  survey command-view
   */
  void shift( float dx, float dy, TdmViewCommand command )
  {
    if ( command == mStation1.mCommand || command == mStation2.mCommand ) {
      // st.xoff += dx;
      // st.yoff += dy;
      makePath();
    }
  }

}

