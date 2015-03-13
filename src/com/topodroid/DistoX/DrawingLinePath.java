/* @file DrawingLinePath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: line-path (lines)
 *
 * The line path id DrawingPath.mPath
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120621 attribute "outline" and "options"
 * 20130829 line point(s) shift
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import android.util.FloatMath;
// import android.util.Log;

/**
 */
public class DrawingLinePath extends DrawingPointLinePath
{
  static final int OUTLINE_OUT = 1;
  static final int OUTLINE_IN = -1;
  static final int OUTLINE_NONE = 0;
  static final int OUTLINE_UNDEF = -2;

  // static int mCount = 0;
  // int mCnt;
  int mLineType;
  boolean mReversed;
  int mOutline; 
  private String mOptions;

  public DrawingLinePath( int line_type )
  {
    // visible = true,  closed = false
    super( DrawingPath.DRAWING_PATH_LINE, true, false );
    // DrawingBrushPaths.makePaths( );
    // mCnt = ++ mCount;
    // TopoDroidLog.Log( TopoDroidLog.LOG_PATH, "DrawingLinePath " + mCnt + " cstr type " + line_type );

    mLineType = line_type;
    mReversed = false;
    mOutline  = ( mLineType == DrawingBrushPaths.mLineLib.mLineWallIndex )? OUTLINE_OUT : OUTLINE_NONE;
    mOptions  = null;
    setPaint( DrawingBrushPaths.getLinePaint( line_type, mReversed ) );
  }

  void addOption( String option ) 
  {
    if ( mOptions == null ) {
      mOptions = option;
    } else {
      mOptions = mOptions + " " + option;
    }
  }

  String[] getOptions() 
  {
    if ( mOptions == null ) return new String[0];
    return mOptions.split(" ");
  }

  String getOptionString()
  {
    if ( mOptions == null ) return "";
    return mOptions;
  }

  void setOptions( String options ) 
  {
    mOptions = options;
  }

  /** 
   * @param exclude whether to exclude splitting-point
   */
  boolean splitAt( LinePoint lp0, DrawingLinePath line1, DrawingLinePath line2, boolean exclude ) // x,y scene point
  {
    line1.mOutline  = mOutline;
    line1.mOptions  = mOptions;
    line1.mReversed = mReversed;
    line2.mOutline  = mOutline;
    line2.mOptions  = mOptions;
    line2.mReversed = mReversed;

    // int k0 = mPoints.indexOf( lp0 );
    // int kmax = mPoints.size() - 1;
    // if ( k0 <= 0 || k0 >= kmax ) return false;
    if ( lp0 == mFirst || lp0 == mLast ) return false;
    if ( exclude ) {
      // if ( k0 <= 1 || k0 >= kmax-1 ) return false;
      if ( lp0 == mFirst.mNext || lp0 == mLast.mPrev ) return false;
    } 

    // LinePoint lp = mPoints.get( 0 );
    LinePoint lp = mFirst;
    line1.addStartPoint( lp.mX, lp.mY );
    // int k;
    // for ( k=1; k<k0; ++ k ) 
    for ( lp=lp.mNext; lp != lp0 && lp != null; lp = lp.mNext ) 
    {
      // lp = mPoints.get(k);
      if ( lp.has_cp ) {
        line1.addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
      } else {
        line1.addPoint( lp.mX, lp.mY );
      }
    }
    if ( ! exclude ) {
      // lp = mPoints.get(k); // k == k0
      if ( lp.has_cp ) {
        line1.addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
      } else {
        line1.addPoint( lp.mX, lp.mY );
      }
    } else {
      // ++ k;
      // lp = mPoints.get(k); // k == k0+1
      if ( lp != null ) {
        lp = lp.mNext;
      }
    }
    if ( lp != null ) {
      line2.addStartPoint( lp.mX, lp.mY );

      // for ( ++k; k < mPoints.size(); ++k ) 
      for ( lp=lp.mNext; lp != null; lp = lp.mNext ) 
      {
        // lp = mPoints.get(k);
        if ( lp.has_cp ) {
          line2.addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
        } else {
          line2.addPoint( lp.mX, lp.mY );
        }
      }
    }
    // Log.v( TopoDroidApp.TAG, "line " + mCnt + " split: " + size() + " --> " + line1.size() + " + " + line2.size() );
    return true;
  }

  void setReversed( boolean reversed )
  {
    if ( reversed != mReversed ) {
      mReversed = reversed;
      // retracePath();
      setPaint( DrawingBrushPaths.getLinePaint( mLineType, mReversed ) );
    }
  }

  public int lineType() { return mLineType; }

  @Override
  public void toCsurvey( PrintWriter pw )
  {
    int layer  = DrawingBrushPaths.getLineCsxLayer( mLineType );
    int type   = DrawingBrushPaths.getLineCsxType( mLineType );
    int cat    = DrawingBrushPaths.getLineCsxCategory( mLineType );
    int pen    = DrawingBrushPaths.getLineCsxPen( mLineType );
    // linetype: 0 line, 1 spline, 2 bezier
    pw.format("          <item layer=\"%d\" name=\"\" type=\"%d\" category=\"%d\" linetype=\"0\" mergemode=\"0\">\n",
      layer, type, cat );
    pw.format("            <pen type=\"%d\" />\n", pen);
    pw.format("            <points data=\"");
    boolean b = true;
    // for ( LinePoint pt : mPoints ) 
    LinePoint pt = mFirst; 
    // NOTE do not skip tick-point if want to save section with tick
    // if ( mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex && size() > 2 ) pt = pt.mNext; // skip first point (tick)
    for ( ; pt != null; pt = pt.mNext ) 
    {
      float x = DrawingActivity.sceneToWorldX( pt.mX );
      float y = DrawingActivity.sceneToWorldY( pt.mY );
      pw.format(Locale.ENGLISH, "%.2f %.2f ", x, y );
      if ( b ) { pw.format("B "); b = false; }
    }
    pw.format("\" />\n");
    pw.format("          </item>\n");
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line %s", DrawingBrushPaths.getLineThName(mLineType) );
    if ( mClosed ) {
      pw.format(" -close on");
    }
    if ( mLineType == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
      if ( mOutline == OUTLINE_IN ) {
        pw.format(" -outline in");
      } else if ( mOutline == OUTLINE_NONE ) {
        pw.format(" -outline none");
      }
    } else {
      if ( mOutline == OUTLINE_IN ) {
        pw.format(" -outline in");
      } else if ( mOutline == OUTLINE_OUT ) {
        pw.format(" -outline out");
      }
    }
    if ( mReversed ) {
      pw.format(" -reversed on");
    }
    if ( mOptions != null && mOptions.length() > 0 ) {
      pw.format(" %s", mOptions );
    }
    pw.format("\n");

    // for ( LinePoint pt : mPoints ) 
    LinePoint pt = mFirst; 
    if ( mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex && size() > 2 ) pt = pt.mNext; // skip first point (tick)
    for ( ; pt != null; pt = pt.mNext ) 
    {
      pt.toTherion( pw );
    }
    if ( mLineType == DrawingBrushPaths.mLineLib.mLineSlopeIndex ) {
      pw.format("  l-size 40\n");
    }
    pw.format("endline\n");
    return sw.getBuffer().toString();
  }


}

