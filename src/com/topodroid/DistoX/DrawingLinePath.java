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
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.graphics.Canvas;
// import android.graphics.Paint;
// import android.graphics.Path;
// import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
// import java.util.Iterator;
// import java.util.List;
// import java.util.ArrayList;
import java.util.Locale;

// import android.util.Log;

/**
 */
class DrawingLinePath extends DrawingPointLinePath
{
  static final int OUTLINE_OUT = 1;
  static final int OUTLINE_IN = -1;
  static final int OUTLINE_NONE = 0;
  static final int OUTLINE_UNDEF = -2;

  boolean hasOutline() { return mOutline == OUTLINE_OUT || mOutline == OUTLINE_IN; }

  // static int mCount = 0;
  // int mCnt;
  int mLineType;
  int mOutline; 
  private boolean mReversed;

  // FIXME-COPYPATH
  // @Override 
  // DrawingPath copyPath()
  // {
  //   DrawingLinePath ret = new DrawingLinePath( mLineType );
  //   copyTo( ret );
  //   ret.mOutline = mOutline;
  //   ret.mReversed = mReversed;
  //   return ret;
  // }

  DrawingLinePath( int line_type )
  {
    // visible = true,  closed = false
    super( DrawingPath.DRAWING_PATH_LINE, true, false );
    // BrushManager.makePaths( );
    // mCnt = ++ mCount;
    // TDLog.Log( TDLog.LOG_PATH, "DrawingLinePath " + mCnt + " cstr type " + line_type );

    mLineType = line_type;
    mReversed = false;
    mOutline  = ( mLineType == BrushManager.mLineLib.mLineWallIndex )? OUTLINE_OUT : OUTLINE_NONE;
    setPathPaint( BrushManager.mLineLib.getLinePaint( mLineType, mReversed ) );
    mLevel     = BrushManager.mLineLib.getSymbolLevel( mLineType );
  }

  static DrawingLinePath loadDataStream( int version, DataInputStream dis, float x, float y /*, SymbolsPalette missingSymbols */ )
  {
    int type;
    boolean closed, reversed;
    int outline;
    int level = DrawingLevel.LEVEL_DEFAULT;
    String fname, options;
    try {
      fname = dis.readUTF();
      closed = (dis.read() == 1);
      // visible= (dis.read() == 1);
      reversed = (dis.read() == 1);
      outline = dis.readInt();
      if ( version >= 401090 ) level = dis.readInt();
      options = dis.readUTF();

      // BrushManager.mLineLib.tryLoadMissingArea( fname );
      type = BrushManager.mLineLib.getSymbolIndexByFilename( fname ); 
      if ( type < 0 ) {
        // FIXME-MISSING if ( missingSymbols != null ) missingSymbols.addLineFilename( fname );
        type = 0;
      }

      DrawingLinePath ret = new DrawingLinePath( type );
      ret.mOutline  = outline;
      ret.mLevel    = level;
      ret.mOptions  = options;
      ret.setReversed( reversed );

      int npt = dis.readInt();
      // TDLog.Log( TDLog.LOG_PLOT, "L: " + fname + " T " + type + " R" + reversed + " C" + closed + " NP " + npt );

      int has_cp;
      float x1, y1, x2, y2, x0, y0;
      x0 = x + dis.readFloat();
      y0 = y + dis.readFloat();
      has_cp = dis.read(); // this is 0
      if ( has_cp == 1 ) { // consume 4 floats
        x1 = x + dis.readFloat();
        y1 = y + dis.readFloat();
        x2 = x + dis.readFloat();
        y2 = y + dis.readFloat();
      }
      // Log.v("DistoX", "line add start pt " + x0 + " " + y0 );
      ret.addStartPointNoPath( x0, y0 );
      for ( int k=1; k<npt; ++k ) {
        x0 = x + dis.readFloat();
        y0 = y + dis.readFloat();
        has_cp = dis.read();
        if ( has_cp == 1 ) {
          x1 = x + dis.readFloat();
          y1 = y + dis.readFloat();
          x2 = x + dis.readFloat();
          y2 = y + dis.readFloat();
          // Log.v("DistoX", "line add pt " + x0 + " " + y0 + " " + x1 + " " + y1 + " " + x2 + " " + y2 );
          ret.addPoint3NoPath( x1, y1, x2, y2, x0, y0 );
        } else {
          // Log.v("DistoX", "line add pt " + x0 + " " + y0 );
          ret.addPointNoPath( x0, y0 );
        }
      }
      ret.setClosed( closed );
      if ( closed ) {
        ret.closePath();
      }
      ret.retracePath();
      return ret;
    } catch ( IOException e ) {
      TDLog.Error( "LINE in error " + e.getMessage() );
      // Log.v("DistoX", "LINE in error " + e.getMessage() );
    }
    return null;
  }

  @Override
  void computeUnitNormal()
  {
    mDx = mDy = 0;
    if ( mFirst != null && mFirst.mNext != null ) {
      LinePoint second = mFirst.mNext;
      mDx =   second.y - mFirst.y;
      mDy = - second.x + mFirst.x;
      float d = ( mDx*mDx + mDy*mDy );
      if ( d > 0 ) {
        d = 1 / (float)Math.sqrt( d );
        if ( mReversed ) d = -d;
        mDx *= d;
        mDy *= d;
      }
    }
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
    line1.addStartPoint( lp.x, lp.y );
    // int k;
    // for ( k=1; k<k0; ++ k ) 
    for ( lp=lp.mNext; lp != lp0 && lp != null; lp = lp.mNext ) 
    {
      // lp = mPoints.get(k);
      if ( lp.has_cp ) {
        line1.addPoint3( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
      } else {
        line1.addPoint( lp.x, lp.y );
      }
    }
    if ( ! exclude ) {
      // lp = mPoints.get(k); // k == k0
      if ( lp != null ) {
        if ( lp.has_cp ) {
          line1.addPoint3( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
        } else {
          line1.addPoint( lp.x, lp.y );
        }
      }
    } else {
      // ++ k;
      // lp = mPoints.get(k); // k == k0+1
      if ( lp != null ) {
        lp = lp.mNext;
      }
    }
    if ( lp != null ) {
      line2.addStartPoint( lp.x, lp.y );

      // for ( ++k; k < mPoints.size(); ++k ) 
      for ( lp=lp.mNext; lp != null; lp = lp.mNext ) 
      {
        // lp = mPoints.get(k);
        if ( lp.has_cp ) {
          line2.addPoint3( lp.x1, lp.y1, lp.x2, lp.y2, lp.x, lp.y );
        } else {
          line2.addPoint( lp.x, lp.y );
        }
      }
    }
    // Log.v( TopoDroidApp.TAG, "line " + mCnt + " split: " + size() + " --> " + line1.size() + " + " + line2.size() );
    line1.computeUnitNormal();
    line2.computeUnitNormal();
    return true;
  }

  void setReversed( boolean reversed )
  {
    if ( reversed != mReversed ) {
      mReversed = reversed;
      // retracePath();
      setPathPaint( BrushManager.mLineLib.getLinePaint( mLineType, mReversed ) );
      computeUnitNormal();
    }
  }

  void flipReversed() 
  {
    mReversed = ! mReversed;
    // retracePath();
    setPathPaint( BrushManager.mLineLib.getLinePaint( mLineType, mReversed ) );
    computeUnitNormal();
  }

  boolean isReversed() { return mReversed; }

  int lineType() { return mLineType; }

  void setLineType( int type )
  {
    mLineType = type;
    setPathPaint( BrushManager.mLineLib.getLinePaint( mLineType, mReversed ) );
  }
  
  @Override
  void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
  {
    float bezier_step = TDSetting.getBezierStep();
    int layer  = BrushManager.getLineCsxLayer( mLineType );
    int type   = BrushManager.getLineCsxType( mLineType );
    int cat    = BrushManager.getLineCsxCategory( mLineType );
    int pen    = BrushManager.getLineCsxPen( mLineType );
    // linetype: 0 line, 1 spline, 2 bezier
    pw.format("          <item layer=\"%d\" cave=\"%s\" branch=\"%s\" name=\"\" type=\"%d\" category=\"%d\" linetype=\"0\"",
      layer, cave, branch, type, cat );
    if ( bind != null ) pw.format(" bind=\"%s\"", bind );
    // FIXME CLOSE 
    pw.format(" mergemode=\"0\">\n" );
    pw.format("            <pen type=\"%d\" />\n", pen);
    pw.format("            <points data=\"");
    // boolean b = true;
    float x3, y3;
    float x0, y0, x1, y1, x2, y2;
    // for ( LinePoint pt : mPoints ) 
    if ( ! mReversed ) {
      // NOTE do not skip tick-point if want to save section with tick
      // if ( mLineType == BrushManager.mLineLib.mLineSectionIndex && size() > 2 ) pt = pt.mNext; // skip first point (tick)
      LinePoint pt = mFirst; 
      x0 = DrawingUtil.sceneToWorldX( pt.x, pt.y );
      y0 = DrawingUtil.sceneToWorldY( pt.x, pt.y );
      pw.format(Locale.US, "%.2f %.2f ", x0, y0 );
      // Log.v("DistoX", "X " + x0 + " Y " + y0 );
      // if ( b ) {
        pw.format("B ");
        //  b = false;
      // }
      for ( pt = pt.mNext; pt != null; pt = pt.mNext ) 
      {
        x3 = DrawingUtil.sceneToWorldX( pt.x, pt.y );
        y3 = DrawingUtil.sceneToWorldY( pt.x, pt.y );
	if ( pt.has_cp ) {
          x1 = DrawingUtil.sceneToWorldX( pt.x1, pt.y1 );
          y1 = DrawingUtil.sceneToWorldY( pt.x1, pt.y1 );
          x2 = DrawingUtil.sceneToWorldX( pt.x2, pt.y2 );
          y2 = DrawingUtil.sceneToWorldY( pt.x2, pt.y2 );
	  float len = (x1-x0)*(x1-x0) + (x2-x1)*(x2-x1) + (x3-x2)*(x3-x2) + (x3-x0)*(x3-x0)
	            + (y1-y0)*(y1-y0) + (y2-y1)*(y2-y1) + (y3-y2)*(y3-y2) + (y3-y0)*(y3-y0);
	  int np = (int)( TDMath.sqrt( len ) * bezier_step + 0.5f );
	  if ( np > 1 ) {
	    BezierCurve bc = new BezierCurve( x0, y0, x1, y1, x2, y2, x3, y3 );
	    for ( int n=1; n < np; ++n ) {
	      Point2D p = bc.evaluate( (float)n / (float)np );
              pw.format(Locale.US, "%.2f %.2f ", p.x, p.y );
              // Log.v("DistoX", "N " + n + " X " + p.x + " Y " + p.y );
            }
	  }
	} 
        pw.format(Locale.US, "%.2f %.2f ", x3, y3 );
        // Log.v("DistoX", "X " + x3 + " Y " + y3 );
        // if ( b ) { pw.format("B "); b = false; }
	x0 = x3;
	y0 = y3;
      }
    } else {
      LinePoint pt = mLast;
      x0 = DrawingUtil.sceneToWorldX( pt.x, pt.y );
      y0 = DrawingUtil.sceneToWorldY( pt.x, pt.y );
      pw.format(Locale.US, "%.2f %.2f ", x0, y0 );
      // Log.v("DistoX", "X " + x0 + " Y " + y0 );
      // if ( b ) {
        pw.format("B ");
        // b = false;
      // }
      for ( pt = pt.mPrev; pt != null; pt = pt.mPrev ) 
      {
        x3 = DrawingUtil.sceneToWorldX( pt.x, pt.y );
        y3 = DrawingUtil.sceneToWorldY( pt.x, pt.y );
	if ( pt.has_cp ) {
          x1 = DrawingUtil.sceneToWorldX( pt.x2, pt.y2 );
          y1 = DrawingUtil.sceneToWorldY( pt.x2, pt.y2 );
          x2 = DrawingUtil.sceneToWorldX( pt.x1, pt.y1 );
          y2 = DrawingUtil.sceneToWorldY( pt.x1, pt.y1 );
	  float len = (x1-x0)*(x1-x0) + (x2-x1)*(x2-x1) + (x3-x2)*(x3-x2) + (x3-x0)*(x3-x0)
	            + (y1-y0)*(y1-y0) + (y2-y1)*(y2-y1) + (y3-y2)*(y3-y2) + (y3-y0)*(y3-y0);
	  int np = (int)( TDMath.sqrt( len ) * bezier_step + 0.5f );
	  if ( np > 1 ) {
	    BezierCurve bc = new BezierCurve( x0, y0, x1, y1, x2, y2, x3, y3 );
	    for ( int n=1; n < np; ++n ) {
	      Point2D p = bc.evaluate( (float)n / (float)np );
              pw.format(Locale.US, "%.2f %.2f ", p.x, p.y );
              // Log.v("DistoX", "N " + n + " X " + p.x + " Y " + p.y );
	    }
	  }
	}
        pw.format(Locale.US, "%.2f %.2f ", x3, y3 );
        // Log.v("DistoX", "X " + x3 + " Y " + y3 );
        // if ( b ) { pw.format("B "); b = false; }
	x0 = x3;
	y0 = y3;
      }
    }
    pw.format("\" />\n");
    pw.format("          </item>\n");
  }

  @Override
  String toTherion( )
  {
    if ( mFirst == null ) return null;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line %s", BrushManager.mLineLib.getSymbolThName(mLineType) );
    if ( isClosed() ) {
      pw.format(" -close on");
    }
    if ( mLineType == BrushManager.mLineLib.mLineWallIndex ) {
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
      pw.format(" -reverse on");
    }
    if ( mOptions != null && mOptions.length() > 0 ) {
      pw.format(" %s", mOptions );
    }
    pw.format("\n");

    // // for ( LinePoint pt : mPoints ) 
    // LinePoint pt = mFirst; 
    // // if ( mLineType == BrushManager.mLineLib.mLineSectionIndex && size() > 2 ) pt = pt.mNext; // skip first point (tick)
    // for ( ; pt != null; pt = pt.mNext ) {
    //   pt.toTherion( pw );
    // }
    // // if ( isClosed() ) { // insert start point again if closed
    // //   mFirst.toTherion( pw );
    // // }
    toTherionPoints( pw, isClosed() );

    if ( mLineType == BrushManager.mLineLib.mLineSlopeIndex ) {
      pw.format("  l-size 40\n");
    }
    pw.format("endline\n");
    return sw.getBuffer().toString();
  }

  @Override
  void toDataStream( DataOutputStream dos )
  {
    String name = BrushManager.mLineLib.getSymbolThName( mLineType );
    try {
      dos.write( 'L' );
      dos.writeUTF( name );
      dos.write( isClosed()? 1 : 0 );
      // dos.write( isVisible()? 1 : 0 );
      dos.write( mReversed? 1 : 0 );
      dos.writeInt( mOutline );
      // if ( version >= 401090 )
        dos.writeInt( mLevel );
      dos.writeUTF( ( mOptions != null )? mOptions : "" );
      
      int npt = size(); // number of line points
      dos.writeInt( npt );
      for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) {
        pt.toDataStream( dos );
      }
      // TDLog.Log( TDLog.LOG_PLOT, "L " + name + " " + npt );
    } catch ( IOException e ) {
      TDLog.Error( "LINE out error " + e.toString() );
    }
  }

}

