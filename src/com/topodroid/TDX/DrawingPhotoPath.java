/* @file DrawingPhotoPath.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid drawing: photo point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.prefs.TDSetting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.util.Base64;

public class DrawingPhotoPath extends DrawingPointPath
                              implements IDrawingLink
{
  long mId; // id of the photo 
  // float mPhotoSize; // photo size (horizontal width) [m]
  DrawingPicturePath mPicture;
  String mGeoCode; // geomorphology code
  // private Paint paint;

  // FIXME-COPYPATH
  // @Override
  // DrawingPath copyPath()
  // {
  //   DrawingPhotoPath ret = new DrawingPhotoPath( mPointText, cx, cy, mScale, mOptions, mId );
  //   copyTo( ret );
  //   return ret;
  // }

  DrawingPhotoPath( String text, float off_x, float off_y, int scale, String options, long id, int scrap )
  {
    super( BrushManager.getPointPhotoIndex(), off_x, off_y, scale, text, options, scrap );
    mId = id;
    // mPhotoSize = size;
    mPicture = null;
    mGeoCode    = "";

    // mPointText = text;
    // setPaint( BrushManager.pointPaint[ BrushManager.POINT_LABEL ] );
    // mPaint = BrushManager.pointPaint[ BrushManager.POINT_LABEL ];
    // paint = new Paint();
    // paint.setDither(true);
    // paint.setColor( 0xffffffff );
    // paint.setStyle(Paint.Style.STROKE);
    // paint.setStrokeJoin(Paint.Join.ROUND);
    // paint.setStrokeCap(Paint.Cap.ROUND);
    // paint.setStrokeWidth( WIDTH_CURRENT );
  }

  /** set the geomorphology code
   * @param geocode  new geocode
   */
  void setCode( String geocode ) { mGeoCode = geocode; }

  /** @return the geomorphology code
   */
  String getCode() { return mGeoCode; }

  /** set the photo size
   * @param size   new photo size (horizontal width) [m]
   */
  void setPhotoSize( float size ) 
  { 
    // mPhotoSize = size;
    if ( mPicture != null ) mPicture.mPhotoSize = size;
  }

  /** @return the size of the picture of this photo
   */
  float getPhotoSize() 
  {
    return ( mPicture != null )? mPicture.mPhotoSize : 0;
  }

  /** link this photo to a picture
   * @param picture   picture path
   */
  void setPicture( DrawingPicturePath picture ) 
  { 
    if ( mPicture == picture ) return;
    if ( mPicture != null ) {
      mPicture.setLink( null );
      mPicture.setId( 0 );
      // TODO remove picture ?
    }
    if ( picture  != null ) {
      DrawingPhotoPath path = (DrawingPhotoPath)(picture.getLink());
      if ( path != null ) path.mPicture = null;
      picture.setLink( this );
      picture.setId( this.mId );
    }
    mPicture = picture;
  }

  /** @return the picture of this photo
   */
  DrawingPicturePath getPicture() { return mPicture; }

  
  /** @return the photo-ID  of this photo
   */
  public long getId() { return mId; }


  public static DrawingPhotoPath loadDataStream( int version, DataInputStream dis, float x, float y )
  {
    // TDLog.v( "Drawing Photo Path load data stream");
    float ccx, ccy;
    int scale;
    float orientation = 0;
    // int type;
    String text, options;
    int id;
    int with_picture = 0;
    try {
      ccx = x + dis.readFloat( );
      ccy = y + dis.readFloat( );
      // String th_name = dis.readUTF( );
      // type = BrushManager.getPointLabelIndex();
      // String group = ( version >= 401147 )? dis.readUTF() : ""; // photo has no group
      if ( version > 207043 ) orientation = dis.readFloat( );
      scale = dis.readInt( );
      int lvl = ( version >= 401090 )? dis.readInt() : DrawingLevel.LEVEL_DEFAULT;
      int scrap = ( version >= 401160 )? dis.readInt() : 0;
      text = dis.readUTF();
      options = dis.readUTF();
      id = dis.readInt();
      // float size = ( version >= 602066 )? dis.readFloat() : 1;

      // TDLog.Log( TDLog.LOG_PLOT, "Label <" + text + " " + ccx + " " + ccy + " scale " + scale + " (" + options + ")" );
      DrawingPhotoPath ret = new DrawingPhotoPath( text, ccx, ccy, scale, options, id, scrap );
      ret.setOrientation( orientation );
      ret.mLevel = lvl;
      if ( version >= 602067 ) {
        ret.setCode( dis.readUTF() );
        with_picture = dis.readInt();
        // TDLog.v("Photo with picture " + with_picture );
        if ( with_picture == 1 ) {
          ccx = x + dis.readFloat( );
          ccy = y + dis.readFloat( );
          float size = dis.readFloat( );
          DrawingPicturePath picture = new DrawingPicturePath( size, ccx, ccy, id, scrap );
          picture.mLevel = lvl;
          ret.setPicture( picture );
        }
      }
      return ret;
    } catch ( IOException e ) {
      TDLog.Error( "LABEL in error " + e.getMessage() );
      // TDLog.v( "LABEL in error " + e.getMessage() );
    }
    return null;
  }

  @Override
  public String toTherion( )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.US, "point %.2f %.2f u:photo -text \"%s\" -photo %d.jpg ",
         cx*TDSetting.mToTherion, -cy*TDSetting.mToTherion, ((mPointText==null)?"":mPointText), (int)mId );
    if ( mPicture != null ) {
      pw.format(Locale.US, "[%.1f %.1f %.1f] ", mPicture.cx*TDSetting.mToTherion, -mPicture.cy*TDSetting.mToTherion, mPicture.mPhotoSize*TDSetting.mToTherion );
    }
    toTherionOrientation( pw );
    toTherionOptions( pw );
    pw.format("\n");
    return sw.getBuffer().toString();
  }

  // FIXME PHOTO PICTURE
  @Override
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
  { 
    String subdir = survey + "/photo"; // "photo/" + survey;
    String name   = mId + ".jpg"; // Long.toString(mId) + ".jpg";
    if ( TDFile.hasMSfile( subdir, name ) ) {
      byte[] buf = TDExporter.readFileBytes( subdir, name );
      if ( buf != null ) {
        pw.format("<item type=\"point\" name=\"photo\" cave=\"%s\" branch=\"%s\" text=\"%s\" ", cave, branch, ((mPointText == null)? "" : mPointText) );
        if ( bind != null ) pw.format(" bind=\"%s\" ", bind );
        pw.format(Locale.US, "scale=\"%d\" orientation=\"%.2f\" options=\"%s\" >\n", mScale, mOrientation, ((mOptions   == null)? "" : mOptions) );
        pw.format(" <attachment dataformat=\"0\" data=\"%s\" name=\"\" note=\"%s\" type=\"image/jpeg\" />\n", 
          Base64.encodeToString( buf, Base64.NO_WRAP ),
          ((mPointText==null)?"":mPointText)
        );
        float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
        float y = DrawingUtil.sceneToWorldY( cx, cy );
        pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
        pw.format("</item>\n");
        // TDLog.v( "toCSurvey() Point " + mPointType + " (" + x + " " + y + ") orientation " + mOrientation );
      }
    }
  }

  @Override
  void toDataStream( DataOutputStream dos, int scrap )
  {
    try {
      dos.write( 'Y' );
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      // dos.writeUTF( BrushManager.getPointThName(mPointType) );
      // if ( version >= 401147 ) dos.writeUTF( "" ); // photo has no group
      dos.writeFloat( (float)mOrientation ); // from version 2.7.4e
      dos.writeInt( mScale );
      // if ( version >= 401090 ) 
        dos.writeInt( mLevel );
      // if ( version >= 401160 ) 
        dos.writeInt( (scrap >= 0)? scrap : mScrap );
      dos.writeUTF( ( mPointText != null )? mPointText : "" );
      dos.writeUTF( ( mOptions != null )? mOptions : "" );
      dos.writeInt( ((int)mId) );
      dos.writeUTF( (mGeoCode != null)? mGeoCode : "" );
      // TDLog.v( "PHOTO id " + mId + " (" + cx + " " + cy + ") geocode <" + mGeoCode + ">");
      if ( mPicture != null ) {
        TDLog.v( "PHOTO id " + mId + " picture size " + mPicture.mPhotoSize );
        dos.writeInt( 1 );
        dos.writeFloat( mPicture.cx );
        dos.writeFloat( mPicture.cy );
        dos.writeFloat( mPicture.mPhotoSize );
      } else {
        dos.writeInt( 0 );
      }
    } catch ( IOException e ) {
      TDLog.Error( "PHOTO out error " + e.toString() );
    }
  }

  // IDrawingLink
  public float getLinkX() { return cx; }
  public float getLinkY() { return cy; }
}

