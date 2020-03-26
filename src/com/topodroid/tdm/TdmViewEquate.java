/** @file TdmViewEquate.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager equate display object
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Canvas;
import android.graphics.Matrix;

import android.widget.CheckBox;

import android.util.Log;

class TdmViewEquate
{
  TdmEquate mEquate;
  ArrayList< TdmViewStation > mStations;
   
  Path mPath;

  TdmViewEquate( TdmEquate equate )
  {
    mEquate = equate;
    mStations = new ArrayList< TdmViewStation >();
    mPath = null;
  }

  void addViewStation( TdmViewStation st )
  {
    mStations.add( st );
    makePath();
  }

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
  //   Log.v("TdManager", "equate (size " + mStations.size() + ")" );
  //   for ( TdmViewStation vst : mStations )
  //     Log.v("TdManager", "  station: " + vst.mStation.mName + " " + vst.mCommand.name() );
  // }

  void draw( Canvas canvas, Matrix matrix, Paint paint )
  {
    if ( mPath != null ) {
      Path path = new Path( mPath );
      path.transform( matrix );
      canvas.drawPath( path, paint );
    }
  }
  
}
