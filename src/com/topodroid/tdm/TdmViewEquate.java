/** @file TdmViewEquate.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager equate display object
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Canvas;
import android.graphics.Matrix;

// import android.widget.CheckBox;

class TdmViewEquate
{
  TdmEquate mEquate;
  ArrayList< TdmViewStation > mStations;
   
  Path mPath;

  /** cstr
   * @param equate   equate
   */
  TdmViewEquate( TdmEquate equate )
  {
    mEquate = equate;
    mStations = new ArrayList< TdmViewStation >();
    mPath = null;
  }

  /** add a station-view to this equate-view
   * @param st   station view
   */
  void addViewStation( TdmViewStation st )
  {
    mStations.add( st );
    makePath();
  }

  /** shift the equate-view
   * @param dx   X shift [canvas ?]
   * @param dy   Y shift
   * @param command  survey command-view
   */
  void shift( float dx, float dy, TdmViewCommand command )
  {
    for ( TdmViewStation st : mStations ) {
      if ( command == st.mCommand ) {
        // st.xoff += dx;
        // st.yoff += dy;
        makePath();
        break;
      }
    }
  }

  /** make the equate-view display path
   */
  void makePath()
  {
    if ( mStations.size() > 1 ) {
      mPath = null;
      for ( TdmViewStation vst : mStations ) {
        if ( mPath == null ) {
          mPath = new Path();
          mPath.moveTo( vst.fullX(), vst.fullY() );
        } else {
          mPath.lineTo( vst.fullX(), vst.fullY() );
        }
      }
    }
  }

  // void dump()
  // {
  //   TDLog.v("equate (size " + mStations.size() + ")" );
  //   for ( TdmViewStation vst : mStations )
  //     TDLog.v("  station: " + vst.mStation.mName + " " + vst.mCommand.name() );
  // }

  /** draw the equate-view on the display
   * @param canvas   display canvas
   * @param matrix   transform matrix
   * @param paint    drawing paint
   */
  void draw( Canvas canvas, Matrix matrix, Paint paint )
  {
    if ( mPath != null ) {
      Path path = new Path( mPath );
      path.transform( matrix );
      canvas.drawPath( path, paint );
    }
  }
  
}
