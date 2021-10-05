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

import com.topodroid.utils.TDLog;
import com.topodroid.num.TDNum;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;

import java.io.PrintWriter;
import java.io.DataOutputStream;

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
  static int mSplayMode = SPLAY_MODE_LINE;

  static int toggleSplayMode()
  {
    if ( mSplayMode == SPLAY_MODE_LINE ) {
      mSplayMode = SPLAY_MODE_POINT;
    } else {
      mSplayMode = SPLAY_MODE_LINE;
    }
    TDLog.v("Splay mode: " + mSplayMode );
    return mSplayMode;
  }

  Path mPathB = null;

  DrawingSplayPath( DBlock blk, int scrap )
  {
    super( DrawingPath.DRAWING_PATH_SPLAY, blk, scrap );
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
    mPathB = new Path();
    mPathB.addCircle( x2, y2, TDSetting.mLineThickness*2, Path.Direction.CCW );
    mPathB.offset( off_x, off_y ); // FIXME-PATH this was only for path != null
    // TDLog.v("splay make path with offset " + x1 + " " + y1 + " - " + x2 + " " + y2);
  }

  void makePath( float x1, float y1, float x2, float y2 )
  {
    super.makePath( x1, y1, x2, y2 );
    mPathB = new Path();
    mPathB.addCircle( x2, y2, TDSetting.mLineThickness*2, Path.Direction.CCW );
    // TDLog.v("splay make path with endpoints " + x1 + " " + y1 + " - " + x2 + " " + y2);
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

  @Override
  public void draw( Canvas canvas )
  {
    drawPath( (mSplayMode == SPLAY_MODE_LINE )? mPath : mPathB, canvas );
  }

  @Override
  public void draw( Canvas canvas, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      drawPath( (mSplayMode == SPLAY_MODE_LINE )? mPath : mPathB, canvas );
    }
  }

  // N.B. canvas is guaranteed ! null
  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      mTransformedPath = new Path( (mSplayMode == SPLAY_MODE_LINE )? mPath : mPathB );
      mTransformedPath.transform( matrix );
      drawPath( mTransformedPath, canvas );
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
