/* @file DrawingAudioPath.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid drawing: audio point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.prefs.TDSetting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

// import android.util.Log;
import android.util.Base64;

public class DrawingAudioPath extends DrawingPointPath
{
  long mId;
  // private Paint paint;
  
  // FIXME-COPYPATH
  // @Override
  // DrawingPath copyPath()
  // {
  //   DrawingAudioPath ret = new DrawingAudioPath( cx, cy, mScale, mOptions, mId );
  //   copyTo( ret );
  //   return ret;
  // }

  DrawingAudioPath( float off_x, float off_y, int scale, String options, long id, int scrap )
  {
    super( BrushManager.getPointAudioIndex(), off_x, off_y, scale, null, options, scrap );
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

  public static DrawingAudioPath loadDataStream( int version, DataInputStream dis, float x, float y )
  {
    // int type;
    try {
      float orientation = 0;
      float ccx = x + dis.readFloat( );
      float ccy = y + dis.readFloat( );
      // String th_name = dis.readUTF( );
      // type = BrushManager.getPointLabelIndex();
      // String group = ( version >= 401147 )? dis.readUTF() : ""; // audio has no group
      if ( version > 207043 ) orientation = dis.readFloat( ); // audio-point have no orientation
      int scale = dis.readInt( );
      int lvl = ( version >= 401090 )? dis.readInt() : DrawingLevel.LEVEL_DEFAULT;
      int scrap = ( version > 401160 )? dis.readInt() : 0; 
      String text = dis.readUTF(); // audio-point does not have text (not used)
      String options = dis.readUTF();
      int id = dis.readInt();

      // TDLog.Log( TDLog.LOG_PLOT, "Label <" + text + " " + ccx + " " + ccy + " scale " + scale + " (" + options + ")" );
      DrawingAudioPath ret = new DrawingAudioPath( ccx, ccy, scale, options, id, scrap );
      ret.mLevel = lvl;
      ret.setOrientation( orientation );
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
    pw.format(Locale.US, "point %.2f %.2f audio -text \"%s\" -audio \"%d.wav\" ",
         cx*TDSetting.mToTherion, -cy*TDSetting.mToTherion, ((mPointText==null)?"":mPointText), (int)mId );
    toTherionOrientation( pw );
    toTherionOptions( pw );
    pw.format("\n");
    return sw.getBuffer().toString();
  }

  @Override
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
  { 
    String subdir = survey + "/audio"; // "audio/" + survey;
    String name   = Long.toString( mId ) + ".wav";
    if ( TDFile.hasMSfile( subdir, name ) ) {
      byte[] buf = TDExporter.readFileBytes( subdir, name );
      if ( buf != null ) {
        pw.format("<item type=\"point\" name=\"audio\" cave=\"%s\" branch=\"%s\" text=\"%s\" ", cave, branch, ((mPointText == null)? "" : mPointText) );
        if ( bind != null ) pw.format(" bind=\"%s\" ", bind );
        pw.format(Locale.US, "scale=\"%d\" orientation=\"0.0\" options=\"%s\" >\n", mScale, ((mOptions   == null)? "" : mOptions) );
        pw.format(" <attachment dataformat\"0\" data=\"%s\" name=\"\" note=\"\" type=\"audio/x-wav\" />\n", 
          Base64.encodeToString( buf, Base64.NO_WRAP ) );
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
      dos.write( 'Z' );
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      // dos.writeUTF( BrushManager.getPointThName(mPointType) );
      // if ( version >= 401147 ) dos.writeUTF( (group != null)? group : "" ); // audio has no group
      dos.writeFloat( (float)mOrientation ); // from version 2.7.4e
      dos.writeInt( mScale );
      // if ( version >= 401090 )
        dos.writeInt( mLevel );
      // if ( version >= 401160 )
        dos.writeInt( (scrap >= 0)? scrap : mScrap );
      dos.writeUTF( ( mPointText != null )? mPointText : "" ); // audio-point does not have text
      dos.writeUTF( ( mOptions != null )? mOptions : "" );
      dos.writeInt( ((int)mId) );
      // TDLog.Log( TDLog.LOG_PLOT, "T " + " " + cx + " " + cy );
    } catch ( IOException e ) {
      TDLog.Error( "AUDIO out error " + e.toString() );
    }
  }
}


