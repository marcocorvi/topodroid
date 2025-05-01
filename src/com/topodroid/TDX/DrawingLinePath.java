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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.num.TDNum;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.RectF;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Locale;

public class DrawingLinePath extends DrawingPointLinePath
{
  public static final int OUTLINE_OUT = 1; // TH2EDIT these were package
  public static final int OUTLINE_IN = -1;
  public static final int OUTLINE_NONE = 0;
  public static final int OUTLINE_UNDEF = -2;


  /** test whether the line has the outline
   * @return true if the line has either outline OUT or IN
   */
  boolean hasOutline() { return mOutline == OUTLINE_OUT || mOutline == OUTLINE_IN; }

  // static int mCount = 0;
  // int mCnt;
  int mLineType;
  public int mOutline; // TH2EDIT package
  private boolean mReversed;
  private int mLSide = -1;

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

  void setLSide( int lside ) 
  {
    mLSide = ( lside < 1 )? -1 : lside;
  }

  int getLSide() { return mLSide; }

  /** cstr
   * @param line_type   type of the line
   * @param scrap       index of the scrap of this line
   */
  public DrawingLinePath( int line_type, int scrap )
  {
    // visible = true,  closed = false
    super( DrawingPath.DRAWING_PATH_LINE, true, false, scrap );
    // mCnt = ++ mCount;
    // TDLog.Log( TDLog.LOG_PATH, "DrawingLinePath " + mCnt + " cstr type " + line_type );

    mLineType = line_type;
    mReversed = false;
    mOutline  = ( BrushManager.isLineWall(mLineType) )? OUTLINE_OUT : OUTLINE_NONE;
    setPathPaint( BrushManager.getLinePaint( mLineType, mReversed ) );
    mLevel     = BrushManager.getLineLevel( mLineType );
  }

  /** factory: deserialize a line from a data stream
   * @param version   serialized version
   * @param dis       data input stream
   * @param x         offset X coord [scene ?]
   * @param y         offset Y coord
   */
  public static DrawingLinePath loadDataStream( int version, DataInputStream dis, float x, float y /*, SymbolsPalette missingSymbols */ )
  {
    int type;
    boolean closed, reversed;
    int outline;
    int level = DrawingLevel.LEVEL_DEFAULT;
    int scrap = 0;
    int lside = -1;
    String thname, options;
    String group = null;
    try {
      thname = dis.readUTF();
      if ( version >= 401147 ) group = dis.readUTF();
      closed = (dis.read() == 1);
      // visible= (dis.read() == 1);
      reversed = (dis.read() == 1);
      outline = dis.readInt();
      if ( version >= 602055 ) lside = dis.readInt();
      if ( version >= 401090 ) level = dis.readInt();
      if ( version >= 401160 ) scrap = dis.readInt();
      options = dis.readUTF();

      // BrushManager.tryLoadMissingLine( thname ); // LOAD_MISSING

      type = BrushManager.getLineIndexByThNameOrGroup( thname, group ); 
      if ( type < 0 ) {
        // FIXME-MISSING if ( missingSymbols != null ) missingSymbols.addLineFilename( thname );
        type = 0;
        options = (options != null)? options + " -symbol " + thname : "-symbol " + thname;
      }

      DrawingLinePath ret = new DrawingLinePath( type, scrap );
      ret.mOutline  = outline;
      ret.mLevel    = level;
      ret.mOptions  = options;
      ret.setReversed( reversed );
      ret.setLSide( lside );

      int npt = dis.readInt();
      // TDLog.Log( TDLog.LOG_PLOT, "L: " + thname + " T " + type + " R" + reversed + " C" + closed + " NP " + npt );

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
      // TDLog.v( "line add start pt " + x0 + " " + y0 );
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
          // TDLog.v( "line add pt " + x0 + " " + y0 + " " + x1 + " " + y1 + " " + x2 + " " + y2 );
          ret.addPoint3NoPath( x1, y1, x2, y2, x0, y0 );
        } else {
          // TDLog.v( "line add pt " + x0 + " " + y0 );
          ret.addPointNoPath( x0, y0 );
        }
      }
      // TDLog.v("Line read: closed " + closed + " (" + ret.isClosed() + ")" );
      ret.setClosed( closed );
      if ( closed ) {
        ret.closePath();
      }
      ret.retracePath();
      return ret;
    } catch ( IOException e ) {
      TDLog.e( "LINE in error " + e.getMessage() );
      // TDLog.v( "LINE in error " + e.getMessage() );
    }
    return null;
  }

  // static void globDataStream( int version, DataInputStream dis )
  // {
  //   try {
  //     dis.readUTF();
  //     if ( version >= 401147 ) dis.readUTF();
  //     dis.read();
  //     // visible= (dis.read() == 1);
  //     dis.read();
  //     dis.readInt();
  //     if ( version >= 401090 ) dis.readInt();
  //     if ( version >= 401160 ) dis.readInt();
  //     dis.readUTF();
  //     int npt = dis.readInt();
  //     int has_cp;
  //     float x1, y1, x2, y2, x0, y0;
  //     dis.readFloat();
  //     dis.readFloat();
  //     has_cp = dis.read(); // this is 0
  //     if ( has_cp == 1 ) { // consume 4 floats
  //       dis.readFloat();
  //       dis.readFloat();
  //       dis.readFloat();
  //       dis.readFloat();
  //     }
  //     for ( int k=1; k<npt; ++k ) {
  //       dis.readFloat();
  //       dis.readFloat();
  //       has_cp = dis.read();
  //       if ( has_cp == 1 ) {
  //         dis.readFloat();
  //         dis.readFloat();
  //         dis.readFloat();
  //         dis.readFloat();
  //       }
  //     }
  //   } catch ( IOException e ) {
  //     TDLog.e( "LINE in error " + e.getMessage() );
  //   }
  // }

  /** compute the unit normal
   */
  @Override
  public void computeUnitNormal() // TH2EDIT package
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

  /** split the line at a point
   * @param lp0   splitting point
   * @param line1 first part 
   * @param line2 second part 
   * @param exclude whether to exclude the splitting-point
   * @return true if successful
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
    // TDLog.v( "line " + mCnt + " split: " + size() + " --> " + line1.size() + " + " + line2.size() );
    line1.computeUnitNormal();
    line2.computeUnitNormal();
    return true;
  }

  /** append the points of a line to this line
   * @param line   line to append
   */
  void appendLinePoints( DrawingLinePath line )
  {
    if ( line == null ) return;
    LinePoint pt = line.mFirst;
    while ( pt != null ) {
      if ( pt.has_cp ) {
        addPoint3( pt.x1, pt.y1, pt.x2, pt.y2, pt.x, pt.y );
      } else {
        addPoint( pt.x, pt.y );
      }
      pt = pt.mNext;
    }
  }

  /** append the points of a line to this line, in reversed order (from last to first)
   * @param line   line to append
   */
  void appendReversedLinePoints( DrawingLinePath line )
  {
    if ( line == null ) return;
    boolean with_cp = false;
    LinePoint pt = line.mLast;
    float x1 = pt.x, y1=pt.y, x2=pt.x, y2=pt.y;
    while ( pt != null ) {
      if ( with_cp ) {
         addPoint3( x2, y2, x1, y1, pt.x, pt.y );
      } else {
         addPoint( pt.x, pt.y );
      }
      with_cp = pt.has_cp;
      if ( with_cp ) {
        x1 = pt.x1; y1 = pt.y1; x2 = pt.x2; y2 = pt.y2;
      }
      pt = pt.mPrev;
    }
  }

  /** set the reversed attribute
   * @param reversed   new reversed attribute
   */
  public void setReversed( boolean reversed ) // TH2EDIT package
  {
    if ( reversed != mReversed ) {
      mReversed = reversed;
      // retracePath();
      setPathPaint( BrushManager.getLinePaint( mLineType, mReversed ) );
      computeUnitNormal();
    }
  }

  /** flip the reversed attribute
   */
  void flipReversed() 
  {
    mReversed = ! mReversed;
    // retracePath();
    setPathPaint( BrushManager.getLinePaint( mLineType, mReversed ) );
    computeUnitNormal();
  }

  /** check the reversed attribute
   * @return true if reversed
   */
  public boolean isReversed() { return mReversed; }

  /** get the line type
   * @return the type of the line
   */
  public int lineType() { return mLineType; }

  /** set the line type
   * @param type   type of the line
   */
  void setLineType( int type )
  {
    mLineType = type;
    setPathPaint( BrushManager.getLinePaint( mLineType, mReversed ) );
  }
  
  /** get the therion name
   * @return the therion name of this line type
   */
  public String getThName() { return BrushManager.getLineThName( mLineType ); }

  /** get the full therion name
   * @return the full *with possible prefix) therion name of this line type
   */
  public String getFullThName() { return BrushManager.getLineFullThName( mLineType ); }

  /** @return the line Therion type (possibly incuding the prefix), with ':' replaced by '_'
   */
  public String getFullThNameEscapedColon() { return  BrushManager.getLineFullThNameEscapedColon( mLineType ); }

  /** draw the line with the specified paint
   * @param canvas   canvas
   * @param matrix   transform matrix
   * @param bbox     clipping rectangle
   * @param paint    paint
   *
   * @note canvas is guaranteed ! null
   */
  public void drawWithPaint( Canvas canvas, Matrix matrix, RectF bbox, Paint paint )
  {
    if ( intersects( bbox ) ) 
    {
      mTransformedPath = new Path( mPath );
      mTransformedPath.transform( matrix );
      canvas.drawPath( mTransformedPath, paint );
    }
  }

//   @Override
//   void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
//   {
//     int layer  = BrushManager.getLineCsxLayer( mLineType );
//     int type   = BrushManager.getLineCsxType( mLineType );
//     int cat    = BrushManager.getLineCsxCategory( mLineType );
//     int pen    = BrushManager.getLineCsxPen( mLineType );
//     // linetype: 0 line, 1 spline, 2 bezier
//     pw.format("          <item layer=\"%d\" cave=\"%s\" branch=\"%s\" name=\"\" type=\"%d\" category=\"%d\" linetype=\"0\"",
//       layer, cave, branch, type, cat );
//     if ( bind != null ) pw.format(" bind=\"%s\"", bind );
//     // FIXME CLOSE 
//     pw.format(" mergemode=\"0\">\n" );
//     pw.format("            <pen type=\"%d\" />\n", pen);
//     toCsurveyPoints( pw, false, mReversed );
//     pw.format("          </item>\n");
//   }

  /** export in cSurvey format
   * @param pw     output writer
   * @param survey survey name
   * @param cave   cave name
   * @param branch branch name
   * @param bind   cSurvey binding
   */
  @Override
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
  {
    // linetype: 0 line, 1 spline, 2 bezier
    String name = getThName();
    pw.format("          <item type=\"line\" name=\"%s\" cave=\"%s\" branch=\"%s\" reversed=\"%d\" closed=\"%d\" outline=\"%d\" options=\"%s\" ",
      name, cave, branch, (mReversed ? 1 : 0), (isClosed() ? 1 : 0), mOutline, 
      ((mOptions   == null)? "" : mOptions)
    );
    if ( bind != null ) pw.format(" bind=\"%s\"", bind );
    pw.format(" >\n" );
    toCsurveyPoints( pw, false, mReversed );
    pw.format("          </item>\n");
  }

  /** get the therion representation of the line
   * @return the therion format of this line
   */
  @Override
  String toTherion( )
  {
    if ( mFirst == null ) return null;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line %s", getFullThName() );
    if ( isClosed() ) {
      pw.format(" -close on");
    }
    if ( BrushManager.isLineWall( mLineType ) ) {
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

    if ( BrushManager.isLineSlope( mLineType ) ) {
      pw.format("  l-size %d\n", ( ( mLSide < 0 )? TDSetting.mSlopeLSide : mLSide ) );
    }
    pw.format("endline\n");
    return sw.getBuffer().toString();
  }

  /** serialize to a data stream
   * @param dos    data output stream,
   * @param scrap  index of the scrap to output (neg. to use the line scrap)
   */
  @Override
  void toDataStream( DataOutputStream dos, int scrap )
  {
    String name  = getThName( );
    if ( name == null ) { // should not happen
      TDLog.e("null line name");
      name = SymbolLibrary.USER;
    }
    String group = BrushManager.getLineGroup( mLineType );
    try {
      dos.write( 'L' );
      dos.writeUTF( name );
      // if ( version >= 401147 )
        dos.writeUTF( (group != null)? group : "" );
      dos.write( isClosed()? 1 : 0 );
      // dos.write( isVisible()? 1 : 0 );
      dos.write( mReversed? 1 : 0 );
      dos.writeInt( mOutline );
      dos.writeInt( mLSide );
      // if ( version >= 401090 )
        dos.writeInt( mLevel );
      // if ( version >= 401160 )
        dos.writeInt( (scrap >= 0)? scrap : mScrap );
      dos.writeUTF( ( mOptions != null )? mOptions : "" );
      
      int npt = size(); // number of line points
      dos.writeInt( npt );
      for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) {
        pt.toDataStream( dos );
      }
      // TDLog.Log( TDLog.LOG_PLOT, "L " + name + " " + npt );
    } catch ( IOException e ) {
      TDLog.e( "LINE out error " + e.toString() );
    }
  }

  /** @return the line color
   */
  int lineColor( ) { return BrushManager.getLineColor( mLineType ); }

  /** export in Cav3D format
   * @param pw     output writer
   * @param type   line type
   * @param cmd    command manager
   * @param num    data reduction
   */
  @Override
  void toCave3D( PrintWriter pw, int type, DrawingCommandManager cmd, TDNum num )
  {
    if ( size() < 2 ) return;
    String name = getThName();
    int color   = BrushManager.getLineColor( mLineType );
    float red   = ((color >> 16)&0xff)/255.0f;
    float green = ((color >>  8)&0xff)/255.0f;
    float blue  = ((color      )&0xff)/255.0f;
    pw.format( Locale.US, "LINE %s %.2f %.2f %.2f\n", name, red, green, blue );
    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) {
      pt.toCave3D( pw, type, cmd, num );
    }
    if ( isClosed() ) {
      mFirst.toCave3D( pw, type, cmd, num );
    }
    pw.format( Locale.US, "ENDLINE\n" );
  }

  /** export in Cav3D format
   * @param pw     output writer
   * @param type   line type
   * @param V1     ...
   * @param V2     ...
   */
  @Override
  void toCave3D( PrintWriter pw, int type, TDVector V1, TDVector V2 )
  {
    if ( size() < 2 ) return;
    String name = getThName();
    int color   = BrushManager.getLineColor( mLineType );
    float red   = ((color >> 16)&0xff)/255.0f;
    float green = ((color >>  8)&0xff)/255.0f;
    float blue  = ((color      )&0xff)/255.0f;
    pw.format( Locale.US, "LINE %s %.2f %.2f %.2f\n", name, red, green, blue );
    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) {
      pt.toCave3D( pw, type, V1, V2 );
    }
    if ( isClosed() ) {
      mFirst.toCave3D( pw, type, V1, V2 );
    }
    pw.format( Locale.US, "ENDLINE\n" );
  }

}

