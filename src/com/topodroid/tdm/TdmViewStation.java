/** @file TdmViewStation.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager survey station display object
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

import android.widget.CheckBox;

class TdmViewStation
{
  TdmStation mStation;
  TdmViewCommand mCommand;
  float x;  // canvas coords
  float y;
  Path mPath;
  boolean mEquated;
  double d;  // distance on selection
  boolean mChecked;
  CheckBox mCB;
  private float xoff; // equate: command offsets
  private float yoff;

  TdmViewStation( TdmStation st, TdmViewCommand command, float x0, float y0, boolean equated )
  {
    mStation = st;
    mCommand = command;
    x = x0;
    y = y0;
    mEquated = equated;
    d = 0;
    mChecked = false;
    mCB = null;
    mPath = new Path();
    mPath.moveTo( x, y );
    mPath.lineTo( x + 10 * st.mName.length(), y );
    xoff = 0;
    yoff = 0;
  }

  void shift( float dx, float dy ) 
  {
    xoff += dx;
    yoff += dy;
  }

  String name() { return mStation.mName; }

  void setChecked( boolean checked ) 
  { 
    mChecked = checked;
    if ( mCB != null ) {
      // mCB.setChecked( false );
      mCB.setChecked( checked );
      mCB.invalidate();
    }
  }

  boolean isChecked() { return mCB != null && mCB.isChecked(); }

  boolean resetChecked()
  {
    boolean ret = mChecked;
    mCB = null;
    mChecked = false;
    return ret;
  }

  void setCheckBox( CheckBox cb ) 
  { 
    mCB = cb;
    if ( mCB != null ) mCB.setChecked( mChecked );
  }

  void shiftBy( float dx, float dy )
  {
    x += dx;
    y += dy;
  }

  void setEquated()
  {
    mEquated = true;
  }

  float fullX() { return x + xoff; }
  float fullY() { return y + yoff; }

  void draw( Canvas canvas, Matrix matrix, Paint paint, Paint fill, float zoom )
  {
    Path path;
    if ( mEquated ) {
      path = new Path();
      path.addCircle( x, y, 0.5f/zoom, Path.Direction.CCW );
      path.transform( matrix );
      canvas.drawPath( path, fill );
    }
    path = new Path( mPath );
    path.transform( matrix );
    canvas.drawTextOnPath( mStation.mName, path, 0,0, paint );
  }
  
  void drawCircle( Canvas canvas, Matrix matrix, Paint paint, float zoom )
  {
    Path path = new Path( );
    // path.moveTo( x, y );
    path.addCircle( x, y, 1.0f/zoom, Path.Direction.CCW );
    path.transform( matrix );
    canvas.drawPath( path, paint );
  }
  
}
