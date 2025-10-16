/* @file PhotoCommandManager.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid photo drawing: commands manager
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.TDGreenDot;
import com.topodroid.num.TDNum;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;
// import com.topodroid.math.Point2D; // intersection point

// import android.content.res.Configuration;
import android.app.Activity;

import android.graphics.Canvas;
import android.graphics.Matrix;
// import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.Display;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.DataOutputStream;

// import java.util.Locale;

public class PhotoCommandManager
{
  // FIXED_ZOOM 
  private int mFixedZoom = 0;

  // private static final int BORDER = 20; // for the bitmap

  final private List< DrawingLinePath >     mCurrentStack;
  final private List< DrawingLinePath >     mRedoStack;

  private Matrix  mMatrix = new Matrix();
  private float   mScale = 1.0f; // current zoom: value of 1 pl in scene space

  /** cstr
   */
  PhotoCommandManager( )
  {
    // TDLog.v(plot_name + " command manager mode " + mode );
    mCurrentStack = Collections.synchronizedList(new ArrayList< DrawingLinePath >());
    mRedoStack    = Collections.synchronizedList(new ArrayList< DrawingLinePath >());
    // mMatrix      = new Matrix(); // identity
    // mScale       = 1.0f;
  }

  // ----------------------------------------------------------------

  /** @return a list (copy) of the drawing objects
   * @note used by DrawingDxf and DrawingSvg, and exportAsCsx
   */
  public List< DrawingLinePath > getCommands() { return mCurrentStack; }


  /** clear the drawing: clear the references and the sketch items
   * @note called only for th2 drawing import 
   */
  void clearDrawing()
  {
    synchronized(this) {
      mCurrentStack.clear();
    }
    mRedoStack.clear();
    // mMatrix = new Matrix(); // identity
  }


  /** add a drawing item (and set the current scrap)
   * @param path    item
   */
  void addLine( DrawingLinePath path ) 
  { 
    synchronized(this) {
      mCurrentStack.add( path ); 
    }
  }

  void undo()
  {
    int sz = mCurrentStack.size();
    if ( sz > 0 ) {
      synchronized(this) {
        DrawingLinePath path = mCurrentStack.get( sz - 1 );
        mRedoStack.add( path );
      }
    }
  }

  void redo() 
  {
    int sz = mRedoStack.size();
    if ( sz > 0 ) {
      synchronized(this) {
        DrawingLinePath path = mRedoStack.get( sz - 1 );
        mCurrentStack.add( path );
      }
    }
  }

  boolean hasMoreRedo() { return mRedoStack.size() > 0; }

  boolean hasMoreUndo() { return mCurrentStack.size() > 0; }
      
  /** draw the sketch on the canvas (display)
   * N.B. doneHandler is not used
   * @param canvas where to draw
   */
  void executeAll( Canvas canvas )
  {
    if ( canvas == null ) {
      TDLog.e( "drawing execute all: null canvas");
      return;
    }

    // Matrix mm    = mMatrix; // mMatrix = Scale( 1/s, 1/s) * Translate( -Offx, -Offy)  (first translate then scale)
    // float  scale = mScale;

    synchronized(this) {
      for ( DrawingLinePath line : mCurrentStack ) {
        line.draw( canvas ); // , mMatrix, mScale );
      }
    }

  }


}
