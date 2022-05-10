/* @file DrawingStationName.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing station name from survey data reduction (reference station names)
 *        type: DRAWING_PATH_NAME
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
// import com.topodroid.utils.TDLog;
import com.topodroid.num.NumStation;
// import com.topodroid.math.Point2D;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;
import com.topodroid.common.PointScale;

import java.util.Locale;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Matrix;

public class DrawingStationName extends DrawingPointPath
                                implements IDrawingLink
{
  private String mName; // station name
  private NumStation mStation;
  // float mX;     // scene coordinates (cx, cy)
  // float mY;

  private boolean mDuplicate;  // whether this is a duplicated station

  long  mXSectionType; // whether this station has a X-section
  private float mAzimuth, mClino;
  private float mDX, mDY;     // X-section direction

  // get coords for a "section" point
  float getXSectionX( float r ) { return cx - ((mXSectionType == PlotType.PLOT_NULL)? 0 : r * mDY); }
  float getXSectionY( float r ) { return cy + ((mXSectionType == PlotType.PLOT_NULL)? 0 : r * mDX); }

  /** cstr
   * @param name   station name
   * @param x      X position (scene coord)
   * @param y      Y position (scene coord)
   * @param scrap  scrap index
   */
  public DrawingStationName( String name, float x, float y, int scrap )
  {
    super( BrushManager.getPointLabelIndex(),
           x, // scene coordinate
           y, 
           PointScale.SCALE_M, null, null, scrap ); // no text no options
    mType = DRAWING_PATH_NAME; // override DrawingPath.mType
    mStation = null;
    mName = ( name == null )? "" : name;
    mXSectionType = PlotType.PLOT_NULL;

    // TDLog.Log( TDLog.LOG_PLOT, "DrawingStationName cstr " + mName + " " + x + " " + y );

    setCenter( x, y ); // scene coords
    mDuplicate = false;
    makeStraightPath( 0, 0, 2*TDSetting.mStationSize*mName.length(), 0, cx, cy );
    mPaint = BrushManager.fixedStationPaint;
  }

  /** cstr
   * @param num_st data-reduction station
   * @param x      X position (scene coord)
   * @param y      Y position (scene coord)
   * @param scrap  scrap index
   */
  public DrawingStationName( NumStation num_st, float x, float y, int scrap )
  {
    super( BrushManager.getPointLabelIndex(),
           x, // scene coordinate
           y, 
           PointScale.SCALE_M, null, null, scrap );
    mType = DRAWING_PATH_NAME; // override DrawingPath.mType
    mStation = num_st;
    mName = (num_st.name == null)? "" : num_st.name;
    mXSectionType = PlotType.PLOT_NULL;

    // TDLog.Log( TDLog.LOG_PLOT, "DrawingStationName cstr " + mName + " " + x + " " + y );
    mPaint = ( num_st.mDuplicate )? BrushManager.duplicateStationPaint
                                  : BrushManager.fixedStationPaint;
    setCenter( x, y ); // scene coords
    mDuplicate = num_st.mDuplicate;
    
    makeStraightPath( 0, 0, 2*TDSetting.mStationSize*mName.length(), 0, cx, cy );
  }

  /** @return the station name
   */
  public String getName() { return mName; }

  /** @return the data-reduction station (or null)
   */
  public NumStation getNumStation() { return mStation; }

  /** @return X scene coord of the linked item
   * @implements IDrawingLink
   */
  public float getLinkX() { return cx; }

  /** @return Y scene coord of the linked item
   * @implements IDrawingLink
   */
  public float getLinkY() { return cy; }
  // public Point2D getLink() { return new Point2D(cx,cy); }

  /** set the XSection - FIXME OK PROFILE
   * @param azimuth    XSection azimuth
   * @param clino      XSection clino
   * @param type       XSection type
   */
  void setXSection( float azimuth, float clino, long type )
  {
    mXSectionType = type;
    mAzimuth      = azimuth;
    mClino        = clino;
    if ( type == PlotType.PLOT_PLAN ) {
      mDX =   TDMath.sind( azimuth );
      mDY = - TDMath.cosd( azimuth );
    } else if ( PlotType.isProfile( type ) ) {
      if ( clino > 89 ) {
        mDX = 0;
        mDY = -1;
      } else if ( clino < -89 ) {
        mDX = 0;
        mDY = 1;
      } else {
        mDX = 1; // FIXME
        mDY = 0;
      }
    }
  }

  /** clear the XXection
   */
  void resetXSection( ) { mXSectionType = PlotType.PLOT_NULL; }

  // defined in DrawingPointPath
  // float distance( float x, float y )
  // { 
  //   double dx = x - cx;
  //   double dy = y - cy;
  //   return Math.sqrt( dx*dx + dy*dy );
  // }

  /** draw the station on the screen
   * @param canvas   canvas
   * @param bbox     clipping box
   */
  @Override
  public void draw( Canvas canvas, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      // TDLog.Log( TDLog.LOG_PATH, "DrawingStationName::draw LABEL " + mName );
      canvas.drawTextOnPath( mName, mPath, 0f, 0f, mPaint );
      if ( mXSectionType != PlotType.PLOT_NULL ) {
        Path path = new Path();
        path.moveTo( cx, cy );
        path.lineTo( cx+mDX*TDSetting.mArrowLength, cy+mDY*TDSetting.mArrowLength );
        canvas.drawPath( path, BrushManager.mSectionPaint );
      }
    }
  }

  /** draw the station on the screen
   * @param canvas   canvas
   * @param matrix   transform matrix
   * @param bbox     clipping box
   */
  @Override
  public void draw( Canvas canvas, Matrix matrix, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      // TDLog.Log( TDLog.LOG_PATH, "DrawingStationName::draw[matrix] LABEL " + mName );
      if ( mName != null && mPaint != null ) {
        mTransformedPath = new Path( mPath );
        mTransformedPath.transform( matrix );
        canvas.drawTextOnPath( mName, mTransformedPath, 0f, 0f, mPaint );
      }
      if ( mXSectionType != PlotType.PLOT_NULL ) {
        Path path = new Path();
        path.moveTo( cx, cy );
        path.lineTo( cx+mDX*TDSetting.mArrowLength, cy+mDY*TDSetting.mArrowLength );
        path.transform( matrix );
        canvas.drawPath( path, BrushManager.mSectionPaint );
      }
    }
  }
  
  /** @return the coordinates (E,N,Z) as a string
   */
  String getCoordsString()
  {
    if ( mStation == null ) return null;
    // east north Z-vertical (upward)
    return String.format(Locale.US, "E %.2f N %.2f Z %.2f", mStation.e, -mStation.s, -mStation.v );
  }

  /** @return Therion representation
   */
  @Override
  public String toTherion( )
  {
    if ( mStation == null ) return null; // FIXME
    // TDLog.v("XVI " + mName + " " + cx + " " + cy );
    return String.format(Locale.US, "point %.2f %.2f station -name \"%s\"", cx*TDSetting.mToTherion, -cy*TDSetting.mToTherion, mName );
  }

  /** serialize 
   * @param dos   output stream
   * @param scrap scrap index
   */
  @Override
  public void toDataStream( DataOutputStream dos, int scrap )
  {
    try {
      dos.write('X');
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      dos.writeUTF( mName );
      // if ( version >= 401090 )
        dos.writeInt( mLevel );
      // if ( version >= 401160 )
        dos.writeInt( (scrap >= 0)? scrap : mScrap );
      dos.writeInt( (int)mXSectionType );
      if ( mXSectionType != PlotType.PLOT_NULL ) {
        dos.writeFloat( mAzimuth );
        dos.writeFloat( mClino );
      }
    } catch ( IOException e ) { }
  }

  /** deserialize - used to make therion file from binary file
   * @param version   file version
   * @param dis       input stream
   * @return deserialized station-name
   */
  public static DrawingStationName loadDataStream( int version, DataInputStream dis )
  {
    float ccx, ccy;
    String name;
    int type;
    try {
      ccx = dis.readFloat();
      ccy = dis.readFloat();
      name = dis.readUTF();
      int level = ( version >= 401090 )? dis.readInt() : DrawingLevel.LEVEL_DEFAULT;
      int scrap = ( version >= 401160 )? dis.readInt() : 0;
      // TDLog.Log( TDLog.LOG_PATH, "SN " + ccx + " " + ccy + " " + name );
      DrawingStationName ret = new DrawingStationName( name, ccx, ccy, scrap );
      ret.mLevel = level;
      if ( version >= 207038 ) {
        type = dis.readInt();
        if ( type != (int)PlotType.PLOT_NULL ) {
          ccx = dis.readFloat();
          ccy = dis.readFloat();
          ret.setXSection( ccx, ccy, type );
        }
      }
      return ret;
    } catch ( IOException e ) { }
    return null;
  }

  // static void globDataStream( int version, DataInputStream dis )
  // {
  //   try {
  //     dis.readFloat();
  //     dis.readFloat();
  //     dis.readUTF();
  //     if ( version >= 401090 ) dis.readInt();
  //     if ( version >= 401160 ) dis.readInt();
  //     if ( version >= 207038 ) {
  //       int type = dis.readInt();
  //       if ( type != (int)PlotType.PLOT_NULL ) {
  //         dis.readFloat();
  //         dis.readFloat();
  //       }
  //     }
  //   } catch ( IOException e ) { }
  // }

  /** flip horizontally
   * @param z   ???
   * @note from ICanvasCommand
   */
  @Override
  public void flipXAxis( float z )
  {
    super.flipXAxis(z);
    // mPath.offset( -2 * cx, 0 );

    if ( mXSectionType != PlotType.PLOT_NULL ) {
      mDX = - mDX; // FLIP flip direction
    }
  }

}
