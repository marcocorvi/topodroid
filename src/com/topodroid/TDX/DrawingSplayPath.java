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
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
// import com.topodroid.num.TDNum;
// import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;
import com.topodroid.ui.TDGreenDot;
import com.topodroid.common.PlotType;
import com.topodroid.dev.cavway.CavwayConst;

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
 *   - DrawingStationUser
 */

public class DrawingSplayPath extends DrawingPath
{
  static final int SPLAY_MODE_LINE  = 1;
  static final int SPLAY_MODE_POINT = 2;
  static int mSplayMode = SPLAY_MODE_LINE; // splay display mode

  public float xEnd, yEnd; // drawing circle center = endpoint (scene coords)

  private static final Paint H_SPLAY_PAINT = BrushManager.darkBluePaint;
  private static final Paint V_SPLAY_PAINT = BrushManager.deepBluePaint;

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

  // /** test whether to display splays as dots - UNUSED
  //  * @return true if splays as dots
  //  */
  // static public boolean splaysAsDots() { return mSplayMode == SPLAY_MODE_POINT; }

  // Path mPathB = null;

  /** cstr
   * @param blk     splay data-block
   * @param scrap   scrap index
   */
  DrawingSplayPath( DBlock blk, int scrap )
  {
    super( DrawingPath.DRAWING_PATH_SPLAY, blk, scrap );
    xEnd = x2;
    yEnd = y2;
  }

  /** make the path copying from another path
   * @param path   the path to copy or null for an empty path
   * @param m      transform matrix
   * @param off_x  offset X (scene ?)
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
   * @param x1   first endpoint X coord [scene]
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
  // @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    draw( canvas, matrix, scale, bbox, true );
  }

  /** draw the splay on the canvas
   * @param canvas   canvas
   * @param matrix   transform matrix
   * @param scale    transform scale
   * @param bbox     clipping bounding box
   * @param xor_color xoring color (not used)
   * 
   * @note the circle radius is fixed and does not increase with the zoom
   * @note canvas is guaranteed ! null
   */
  // @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox, int xor_color )
  {
    draw( canvas, matrix, scale, bbox, xor_color );
  }

  /** draw the splay on the canvas
   * @param canvas   canvas - @note canvas is guaranteed ! null
   * @param matrix   transform matrix
   * @param scale    transform scale: to keep the circle radius fixed
   * @param bbox     clipping bounding box
   * @param not_edit whether the splay is drawn not editable (only for splay mode POINT)
   * 
   * @note the circle radius is fixed and does not increase with the zoom
   */
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox, boolean not_edit )
  {
    if ( intersects( bbox ) ) {
      if ( not_edit && mSplayMode == SPLAY_MODE_POINT ) {
        TDGreenDot.draw( canvas, matrix, xEnd, yEnd, TDSetting.mDotRadius*1.5f*scale, mPaint );
      } else {
        mTransformedPath = new Path( mPath );
        mTransformedPath.transform( matrix );
        drawPath( mTransformedPath, canvas );
      }
    }
  }

  /** draw the splay on the canvas
   * @param canvas   canvas - @note canvas is guaranteed ! null
   * @param matrix   transform matrix
   * @param scale    transform scale: to keep the circle radius fixed
   * @param bbox     clipping bounding box
   * @param not_edit whether the splay is drawn not editable (only for splay mode POINT)
   * @param xor_color xoring color
   * 
   * @note the circle radius is fixed and does not increase with the zoom
   */
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox, boolean not_edit, int xor_color )
  {
    if ( intersects( bbox ) ) {
      if ( not_edit && mSplayMode == SPLAY_MODE_POINT ) {
        TDGreenDot.draw( canvas, matrix, xEnd, yEnd, TDSetting.mDotRadius*1.5f*scale, mPaint );
      } else {
        mTransformedPath = new Path( mPath );
        mTransformedPath.transform( matrix );
        drawPath( mTransformedPath, canvas, xor_color );
      }
    }
  }

  // -------------------------------------------------------------------------------------------------

  /** set splay paint - default behaviour
   * @param h_paint  paint used for H-splay
   * @param v_paint  paint used for V-splay
   * @return true if a special paint has been set
   *
   * @note called by DrawingCommandManager when TDSetting.mDashSplay == DASHING_NONE
   * 
   * over advanced level splay can have classes (X-H-V) or be commented
   */
  private boolean setSplayPaintDefault( DBlock blk, Paint h_paint, Paint v_paint )
  {
    if ( blk == null ) {
      mPaint = BrushManager.paintSplayXB; // BLUE
      return true;
    }
    int cavway_flag = blk.cavwayFlag();
    if ( cavway_flag > 0 ) {
      switch ( cavway_flag ) {
        case CavwayConst.FLAG_FEATURE: 
          mPaint = BrushManager.paintSplayFeature;
          break;
        case CavwayConst.FLAG_RIDGE:
          mPaint = BrushManager.paintSplayRidge;
          break;
        case CavwayConst.FLAG_BACKSIGHT:
          mPaint = BrushManager.paintSplayBacksight;
          break;
        case CavwayConst.FLAG_GENERIC:
          mPaint = BrushManager.paintSplayGeneric;
          break;
        default:
          mPaint = BrushManager.fixedOrangePaint;
          break;
      }
      return true;
    }
      
    // if ( blk.isHighlighted() ) {
    //   mPaint = BrushManager.highlightPaint;
    //   return;
    // }
    if ( TDLevel.overAdvanced ) {
      if ( blk.isCommented() ) { // FIXME_COMMENTED
        mPaint = BrushManager.paintSplayComment; // VERYDARK_GRAY
        return true;
      } 
      if ( blk.isXSplay() ) { 
        mPaint = BrushManager.paintSplayLRUD; // GREEN
        return true;
      } 
      if ( blk.isHSplay() ) {
        mPaint = h_paint;
        return true;
      } 
      if ( blk.isVSplay() ) {
        mPaint = v_paint;
        return true;
      } 
    }
    mPaint = BrushManager.paintSplayXB; // BLUE
    // TDLog.v("paint: none is false");
    return false;
  }

  /** set splay paint according to the azimuth (plan)
   * @param cosine   cos(angle_splay-leg) used for plan-dashing
   * @param h_paint  paint used for H-splay
   * @param v_paint  paint used for V-splay
   * @note called by DrawingCommandManager when TDSetting.mDashSplay == DASHING_AZIMUTH, or DASHING_VIEW for profile
   */
  private void setSplayPaintAzimuth( DBlock blk, float cosine, Paint h_paint, Paint v_paint )
  {
    if ( setSplayPaintDefault( blk, h_paint, v_paint ) ) return;
    if (cosine >= 0 ) {
      if ( cosine < TDSetting.mCosHorizSplay ) {
        mPaint = BrushManager.paintSplayXBdot;
        // TDLog.v("paint DOT cosine " + cosine+ " " + TDSetting.mCosHorizSplay );
      }
    } else if (cosine < 0 ) {
      if ( cosine > -TDSetting.mCosHorizSplay ) {
        mPaint = BrushManager.paintSplayXBdash;
        // TDLog.v("paint DASH cosine " + cosine+ " " + TDSetting.mCosHorizSplay );
      }
    // } else { // nothing: paint is already SplayXB
    //   mPaint = BrushManager.paintSplayXB;
    }
  }
  
  /** set splay paint according to the clino (profile)
   * @param h_paint  paint used for H-splay
   * @param v_paint  paint used for V-splay
   * @note called by DrawingCommandManager when TDSetting.mDashSplay == DASHING_CLINO, or DASHING_VIEW for plan
   */
  private void setSplayPaintClino( DBlock blk, Paint h_paint, Paint v_paint )
  {
    if ( setSplayPaintDefault( blk, h_paint, v_paint ) ) return;
    if (blk.mClino > TDSetting.mVertSplay ) {
      // TDLog.v("paint DOT clino " + blk.mClino + " " + TDSetting.mVertSplay );
      mPaint= BrushManager.paintSplayXBdot;
    } else if (blk.mClino < -TDSetting.mVertSplay) {
      // TDLog.v("paint DASH clino " + blk.mClino + " " + TDSetting.mVertSplay );
      mPaint= BrushManager.paintSplayXBdash;
    // } else { // nothing: paint is already SplayXB
    //   mPaint= BrushManager.paintSplayXB;
    }
  }

  /** set splay paint according to the azimuth (projected profile)
   * @param cosine   cos(angle_splay-proj_direction) used for projected dashing
   * @param h_paint  H-splay paint
   * @param v_paint  V-splay paint
   * @note called by DrawingCommandManager when TDSetting.mDashSplay == DASHING_AZIMUTH, or DASHING_VIEW for profile
   */
  private void setSplayPaintProjected( DBlock blk, float cosine, Paint h_paint, Paint v_paint )
  {
    if ( setSplayPaintDefault( blk, h_paint, v_paint ) ) return;
    if (cosine >= 0 ) {
      if ( cosine > TDSetting.mCosHorizSplay ) {
        mPaint = BrushManager.paintSplayXBdot;
        // TDLog.v("paint DOT cosine " + cosine+ " " + TDSetting.mCosHorizSplay );
      }
    } else if (cosine < 0 ) {
      if ( cosine < -TDSetting.mCosHorizSplay ) {
        mPaint = BrushManager.paintSplayXBdash;
        // TDLog.v("paint DASH cosine " + cosine+ " " + TDSetting.mCosHorizSplay );
      }
    // } else { // nothing: paint is already SplayXB
    //   mPaint = BrushManager.paintSplayXB;
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
          canvas.drawPath( path, BrushManager.paintSplayLatest );
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

  /** set the paint of the splay path according to the splay-dash setting
   * @param type    plot type
   * @param blk     splay data-block
   */
  void setSplayPathPaint( long type, DBlock blk )
  {
    // TDLog.v("splay paint " + TDSetting.mDashSplay + " cos " + this.getCosine() );
    switch ( TDSetting.mDashSplay ) {
      case TDSetting.DASHING_AZIMUTH:
        this.setSplayPaintAzimuth( blk, this.getCosine(), H_SPLAY_PAINT, V_SPLAY_PAINT );
        break;
      case TDSetting.DASHING_CLINO:
        this.setSplayPaintClino( blk, H_SPLAY_PAINT, V_SPLAY_PAINT );
        break;
      case TDSetting.DASHING_VIEW:
        if ( PlotType.isPlan( type ) ) {
          this.setSplayPaintClino( blk, H_SPLAY_PAINT, V_SPLAY_PAINT );
        } else if ( PlotType.isExtended( type ) ) {
          this.setSplayPaintAzimuth( blk, this.getCosine(), H_SPLAY_PAINT, V_SPLAY_PAINT );
        } else if ( PlotType.isProjected( type ) ) {
          this.setSplayPaintProjected( blk, this.getCosine(), H_SPLAY_PAINT, V_SPLAY_PAINT );
        }
        break;
      // case TDSetting.DASHING_NONE:
      default:
        this.setSplayPaintDefault( blk, H_SPLAY_PAINT, V_SPLAY_PAINT ); // H_splay DARK_BLUE, V_splay DEEP_BLUE
        break;
    }
  }


}
