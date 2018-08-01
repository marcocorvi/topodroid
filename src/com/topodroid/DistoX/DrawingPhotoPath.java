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

// import android.graphics.Canvas;
// import android.graphics.Paint;
// import android.graphics.Path;
// import android.graphics.RectF;
// import android.graphics.Matrix;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

// import android.util.Log;
import android.util.Base64;

/**
 */
class DrawingPhotoPath extends DrawingPointPath
{
  private static final float toTherion = TDConst.TO_THERION;

  long mId; // id of the photo 
  // private Paint paint;

  DrawingPhotoPath( String text, float off_x, float off_y, int scale, String options, long id )
  {
    super( BrushManager.mPointLib.mPointPhotoIndex, off_x, off_y, scale, text, options );
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

  static DrawingPhotoPath loadDataStream( int version, DataInputStream dis, float x, float y )
  {
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
      if ( version > 207043 ) orientation = dis.readFloat( );
      scale = dis.readInt( );
      text = dis.readUTF();
      options = dis.readUTF();
      id = dis.readInt();

      // TDLog.Log( TDLog.LOG_PLOT, "Label <" + text + " " + ccx + " " + ccy + " scale " + scale + " (" + options + ")" );
      DrawingPhotoPath ret = new DrawingPhotoPath( text, ccx, ccy, scale, options, id );
      ret.setOrientation( orientation );
      return ret;
    } catch ( IOException e ) {
      TDLog.Error( "LABEL in error " + e.getMessage() );
      // Log.v("DistoX", "LABEL in error " + e.getMessage() );
    }
    return null;
  }

  @Override
  String toTherion( )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.US, "point %.2f %.2f photo -text \"%s\" -photo %d.jpg ",
         cx*toTherion, -cy*toTherion, ((mPointText==null)?"":mPointText), (int)mId );
    toTherionOrientation( pw );
    toTherionOptions( pw );
    pw.format("\n");
    return sw.getBuffer().toString();
  }

  // FIXME_SYNC might be a problem with big photoes, but it is called on export, which runs on async task
  @Override
  void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind, DrawingUtil mDrawingUtil )
  { 
    File photofile = new File( TDPath.getSurveyJpgFile( survey, Long.toString(mId) ) );
    if ( photofile.exists() ) {
      byte[] buf = TDExporter.readFileBytes( photofile );
      if ( buf != null ) {
        pw.format("<item layer=\"6\" cave=\"%s\" branch=\"%s\" type=\"12\" category=\"80\" transparency=\"0.00\"",
          cave, branch );
        if ( bind != null ) pw.format(" bind=\"%s\"", bind );
        pw.format(" text=\"%s\" textrotatemode=\"1\" >\n", ((mPointText==null)?"":mPointText) );
        // pw.format("  <pen type=\"10\" />\n");
        // pw.format("  <brush type=\"7\" />\n");
        pw.format(" <attachment dataformat=\"0\" data=\"%s\" name=\"\" note=\"%s\" type=\"image/jpeg\" />\n", 
          Base64.encodeToString( buf, Base64.NO_WRAP ),
          ((mPointText==null)?"":mPointText)
        );
        float x = mDrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
        float y = mDrawingUtil.sceneToWorldY( cx, cy );
        pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
        // pw.format("  <font type=\"0\" />\n");
        pw.format("</item>\n");
      }
    }
  }

  @Override
  void toDataStream( DataOutputStream dos )
  {
    try {
      dos.write( 'Y' );
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      // dos.writeUTF( BrushManager.mPointLib.getSymbolThName(mPointType) );
      dos.writeFloat( (float)mOrientation ); // from version 2.7.4e
      dos.writeInt( mScale );
      dos.writeUTF( ( mPointText != null )? mPointText : "" );
      dos.writeUTF( ( mOptions != null )? mOptions : "" );
      dos.writeInt( ((int)mId) );
      // TDLog.Log( TDLog.LOG_PLOT, "T " + " " + cx + " " + cy );
    } catch ( IOException e ) {
      TDLog.Error( "PHOTO out error " + e.toString() );
    }
  }
}

