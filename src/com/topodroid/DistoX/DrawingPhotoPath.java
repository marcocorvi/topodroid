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
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.prefs.TDSetting;

import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.util.Base64;

public class DrawingPhotoPath extends DrawingPointPath
{
  long mId; // id of the photo 
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

  public static DrawingPhotoPath loadDataStream( int version, DataInputStream dis, float x, float y )
  {
    // Log.v("DistoX", "Drawing Photo Path load data stream");
    float ccx, ccy;
    int scale;
    float orientation = 0;
    // int type;
    String text, options;
    int id;
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

      // TDLog.Log( TDLog.LOG_PLOT, "Label <" + text + " " + ccx + " " + ccy + " scale " + scale + " (" + options + ")" );
      DrawingPhotoPath ret = new DrawingPhotoPath( text, ccx, ccy, scale, options, id, scrap );
      ret.setOrientation( orientation );
      ret.mLevel = lvl;
      return ret;
    } catch ( IOException e ) {
      TDLog.Error( "LABEL in error " + e.getMessage() );
      // Log.v("DistoX", "LABEL in error " + e.getMessage() );
    }
    return null;
  }

  public long getId() { return mId; }

  @Override
  public String toTherion( )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.US, "point %.2f %.2f photo -text \"%s\" -photo %d.jpg ",
         cx*TDSetting.mToTherion, -cy*TDSetting.mToTherion, ((mPointText==null)?"":mPointText), (int)mId );
    toTherionOrientation( pw );
    toTherionOptions( pw );
    pw.format("\n");
    return sw.getBuffer().toString();
  }

  // FIXME_SYNC might be a problem with big photoes, but it is called on export, which runs on async task
//   @Override
//   void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
//   { 
//     String photofilename = TDPath.getSurveyJpgFile( survey, Long.toString(mId) );
//     // File photofile = TDFile.getFile( photofilename );
//     if ( TDFile.hasFile( photofilename ) ) {
//       byte[] buf = TDExporter.readFileBytes( photofilename );
//       if ( buf != null ) {
//         pw.format("<item layer=\"6\" cave=\"%s\" branch=\"%s\" type=\"12\" category=\"80\" transparency=\"0.00\"",
//           cave, branch );
//         if ( bind != null ) pw.format(" bind=\"%s\"", bind );
//         pw.format(" text=\"%s\" textrotatemode=\"1\" >\n", ((mPointText==null)?"":mPointText) );
//         // pw.format("  <pen type=\"10\" />\n");
//         // pw.format("  <brush type=\"7\" />\n");
//         pw.format(" <attachment dataformat=\"0\" data=\"%s\" name=\"\" note=\"%s\" type=\"image/jpeg\" />\n", 
//           Base64.encodeToString( buf, Base64.NO_WRAP ),
//           ((mPointText==null)?"":mPointText)
//         );
//         float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
//         float y = DrawingUtil.sceneToWorldY( cx, cy );
//         pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
//         // pw.format("  <font type=\"0\" />\n");
//         pw.format("</item>\n");
//       }
//     }
//   }

  @Override
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
  { 
    String photofilename = TDPath.getSurveyJpgFile( survey, Long.toString(mId) );
    // File photofile = TDFile.getFile( photofilename );
    if ( TDFile.hasFile( photofilename ) ) {
      byte[] buf = TDExporter.readFileBytes( photofilename );
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
        // Log.v( TopoDroidApp.TAG, "toCSurevy() Point " + mPointType + " (" + x + " " + y + ") orientation " + mOrientation );
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
      // TDLog.Log( TDLog.LOG_PLOT, "T " + " " + cx + " " + cy );
    } catch ( IOException e ) {
      TDLog.Error( "PHOTO out error " + e.toString() );
    }
  }
}

