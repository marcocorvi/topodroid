/* @file DrawingPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: paths (points, lines, and areas)
 * 
 * FixedPath path is a straight line between the two endpoints
 * GridPath paths are also straight lines
 * PreviewPath path is a line with "many" points
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
// import com.topodroid.num.TDNum;
// import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;
import com.topodroid.ui.TDGreenDot;

// import java.io.PrintWriter;
// import java.io.DataOutputStream;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * direct/indirect subclasses:
 *   - DrawingPointLinePath
 *      - DrawingLinePath
 *      - DrawingAreaPath
 *   - DrawingPointPath
 *   - DrawingStationPath
 */

public class DrawingSplayPath extends DrawingPath
{
  static final int SPLAY_MODE_LINE  = 1;
  static final int SPLAY_MODE_POINT = 2;
  static int mSplayMode = SPLAY_MODE_LINE; // splay display mode

  public float xEnd, yEnd; // drawing circle center = endpoint

  /** toggle the display mode of splays , between LINE and POINT
   */
  static int toggleSplayMode()
  {
    if ( mSplayMode == SPLAY_MODE_LINE ) {
      mSplayMode = SPLAY_MODE_POINT;
    } else {
      mSplayMode = SPLAY_MODE_LINE;
    }
    // TDLog.v("Splay mode: " + mSplayMode );
    return mSplayMode;
  }

  /** test whether to display splays as dots
   * @return true if splays as dots
   */
  static public boolean splaysAsDots() { return mSplayMode == SPLAY_MODE_POINT; }

  // Path mPathB = null;

  /** cstr
   * @param blk     splay data-block
   * @param scrap   scrap index
   */
  DrawingSplayPath( DBlock blk, int scrap )
  {
    super( DrawingPath.DRAWING_PATH_SPLAY, blk, scrap );
    xEnd= x2;
    yEnd = y2;
  }

  /** make the path copying from another path
   * @param path   the path to copy or null for an empty path
   * @param m      transform matrix
   * @param off_x  offset X
   * @param off_y  offset Y
   */
  void makePath( Path path, Matrix m, float off_x, float off_y )
  {
    super.makePath( path, m, off_x, off_y );
    // mPathB = new Path();
    // mPathB.addCircle( x2, y2, TDSetting.mDotRadius*1.5f, Path.Direction.CCW );
    // mPathB.offset( off_x, off_y ); // FIXME-PATH this was only for path != null
    // TDLog.v("splay make path with offset " + x1 + " " + y1 + " - " + x2 + " " + y2);
    xEnd = x2 + off_x;
    yEnd = y2 + off_y;
  }

  /** make the path a straight line between the two endpoints
   * @param x1   first endpoint X coord
   * @param y1   first endpoint Y coord
   * @param x2   second endpoint X coord
   * @param y2   second endpoint Y coord
   */
  void makePath( float x1, float y1, float x2, float y2 )
  {
    super.makePath( x1, y1, x2, y2 );
    // mPathB = new Path();
    // mPathB.addCircle( x2, y2, TDSetting.mDotRadius*1.5f, Path.Direction.CCW );
    // TDLog.v("splay make path with endpoints " + x1 + " " + y1 + " - " + x2 + " " + y2);
    xEnd = x2;
    yEnd = y2;
  }

  // from ICanvasCommand
  // public void shiftPathBy( float dx, float dy ) 
  // {
  //   super.shiftPathBy( dx, dy );
  //   mPathB.offset( dx, dy );
  // }

  // from ICanvasCommand
  // public void scalePathBy( float z, Matrix m )
  // {
  //   super.scalePathBy( z, m );
  //   mPathB.transform( m );
  // }

  // x' = a x + b y + c
  // y' = d x + e y + f
  // public void affineTransformPathBy( float[] mm, Matrix m )
  // {
  //   super.affineTransformPathBy( mm, m );
  //   mPathB.transform( m );
  // }

  /** draw the splay on the canvas
   * @param canvas   canvas
   * @note the circle radius increases with the zoom
   */
  @Override
  public void draw( Canvas canvas )
  {
    // if ( not_edit && mSplayMode == SPLAY_MODE_POINT ) {
    //   TDGreenDot.draw( canvas, 1.0f, xEnd, yEnd, TDSetting.mDotRadius*1.5f, mPaint );
    // } else {
      drawPath( mPath, canvas );
    // }
  }

  /** draw the splay on the canvas
   * @param canvas   canvas
   * @param bbox     clipping bounding box
   */
  @Override
  public void draw( Canvas canvas, RectF bbox )
  {
    if ( intersects( bbox ) ) draw( canvas );
  }

  /** draw the splay on the canvas
   * @param canvas   canvas
   * @param matrix   transform matrix
   * @param scale    transform scale
   * @param bbox     clipping bounding box
   * 
   * @note the circle radius is fixed and does not increase with the zoom
   * @note canvas is guaranteed ! null
   */
  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    draw( canvas, matrix, scale, bbox, true );
  }

  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox, boolean not_edit )
  {
    if ( intersects( bbox ) ) {
      if ( not_edit && mSplayMode == SPLAY_MODE_POINT ) {
        TDGreenDot.draw( canvas, matrix, scale, xEnd, yEnd, TDSetting.mDotRadius*1.5f, mPaint );
      } else {
        mTransformedPath = new Path( mPath );
        mTransformedPath.transform( matrix );
        drawPath( mTransformedPath, canvas );
      }
    }
  }

  // setSplayExtend is used for the plan view
  // cosine = cos(angle_splay-leg)
  // called by DrawingCommandManager
  @Override
  void setSplayPaintPlan( DBlock blk, float cosine, Paint h_paint, Paint v_paint )
  {
    if ( blk == null ) {
      mPaint = BrushManager.paintSplayXB;
      return;
    }
    // if ( blk.isHighlighted() ) {
    //   mPaint = BrushManager.highlightPaint;
    //   return;
    // }
    if ( TDLevel.overAdvanced ) {
      if ( blk.isCommented() ) { // FIXME_COMMENTED
        mPaint = BrushManager.paintSplayComment;
        return;
      } 
      if ( TDLevel.overAdvanced && blk.isXSplay() ) {
        mPaint = BrushManager.paintSplayLRUD;
        return;
      } 
      if ( blk.isHSplay() ) {
        mPaint = h_paint;
        return;
      } 
      if ( blk.isVSplay() ) {
        mPaint = v_paint;
        return;
      } 
    }
    if ( TDSetting.mDashSplay == TDSetting.DASHING_NONE ) {
      mPaint = BrushManager.paintSplayXB;
    } else {
      if (cosine >= 0 && cosine < TDSetting.mCosHorizSplay) {
        mPaint = BrushManager.paintSplayXBdot;
      } else if (cosine < 0 && cosine > -TDSetting.mCosHorizSplay) {
        mPaint = BrushManager.paintSplayXBdash;
      } else {
        mPaint = BrushManager.paintSplayXB;
      }
    }
  }
  
  // setSplayClino is used for the profile view
  @Override
  void setSplayPaintProfile( DBlock blk, Paint h_paint, Paint v_paint )
  {
    if ( blk == null ) {
      mPaint= BrushManager.paintSplayXB;
      return;
    } 
    // if ( blk.isHighlighted() ) {
    //   mPaint = BrushManager.highlightPaint;
    //   return;
    // } 
    if ( TDLevel.overAdvanced ) {
      if ( blk.isCommented() ) { // FIXME_COMMENTED
        mPaint= BrushManager.paintSplayComment;
        return;
      }
      if ( blk.isXSplay() ) {
        mPaint= BrushManager.paintSplayLRUD;
        return;
      }
      if ( blk.isHSplay() ) {
        mPaint = h_paint;
        return;
      }
      if ( blk.isVSplay() ) {
        mPaint = v_paint;
	return;
      } 
    }
    if ( TDSetting.mDashSplay == TDSetting.DASHING_NONE ) {
      mPaint = BrushManager.paintSplayXB;
    } else {
      if (blk.mClino > TDSetting.mVertSplay) {
        mPaint= BrushManager.paintSplayXBdot;
      } else if (blk.mClino < -TDSetting.mVertSplay) {
        mPaint= BrushManager.paintSplayXBdash;
      } else {
        mPaint= BrushManager.paintSplayXB;
      }
    }
  }

  /* FIXME apparently this can be called when mPaint is still null
   *        and when fixedBluePaint is null
   *
   * NOTE DrawingAreaPath overrides this
   */
  @Override
  void drawPath( Path path, Canvas canvas )
  {
    if ( mBlock != null ) {
      if ( TDSetting.mSplayColor ) {
        if ( mBlock.isRecent( ) ) { 
          canvas.drawPath( path, BrushManager.lightBluePaint );
          return;
        }
        if ( TDLevel.overExpert ) { // splay user-color only at tester level
          Paint paint = mBlock.getPaint();
          if ( paint != null ) {
            canvas.drawPath( path, paint );
            return;
          }
        }
      }
    } 
    if ( mPaint != null ) canvas.drawPath( path, mPaint );
  }


}
