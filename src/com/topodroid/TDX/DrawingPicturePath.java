/* @file DrawingPicturePath.java
 *
 * @author marco corvi
 * @date june 2024
 *
 * @brief TopoDroid drawing: photo picture point
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

// import android.util.Base64;
import android.graphics.Path;

public class DrawingPicturePath extends DrawingPointPath
{
  long mId; // id of the photo 
  float mPhotoSize; // photo size (horizontal width) [m]
  // private Paint paint;


  DrawingPicturePath( float size, float off_x, float off_y, long id, int scrap )
  {
    super( BrushManager.getPointPictureIndex(), off_x, off_y, 0, "", "", scrap );
    mId = id;
    mPhotoSize = size;
    makePath();
    TDLog.v("PICTURE size " + size );
  }

  /** @return the photo ID
   */
  public long getId() { return mId; }

  void setId( long id ) { mId = id; }

  /** set the size of the picture
   * @param size   new photo size (horizontal width) [m]
   */
  void setPhotoSize( float size ) { mPhotoSize = size; }

  /** @return the size of the picture
   */
  float getPhotoSize() { return mPhotoSize; }

  /** scale the size of the picture
   * @param scale  scale factor
   */
  void scalePhotoSize( float scale )
  { 
    mPhotoSize *= scale;
    if ( mPhotoSize < TDSetting.mPictureMin ) mPhotoSize = TDSetting.mPictureMin; // min max sizes in meters
    else if ( mPhotoSize > TDSetting.mPictureMax ) mPhotoSize = TDSetting.mPictureMax;
    // TDLog.v("PICTURE new size " + mPhotoSize + " scale " + scale );
    makePath();
  }

  private void makePath()
  {
    mPath = new Path();
    mPath.moveTo( -mPhotoSize, -mPhotoSize );
    mPath.lineTo(  mPhotoSize, -mPhotoSize );
    mPath.lineTo(  mPhotoSize,  mPhotoSize );
    mPath.lineTo( -mPhotoSize,  mPhotoSize );
    mPath.lineTo( -mPhotoSize, -mPhotoSize );
    mPath.lineTo(  mPhotoSize,  mPhotoSize );
    mPath.offset( cx, cy );
  }

  /** this is called by the DrawingPhotoPath.loadDataStream()
   */
  public static DrawingPicturePath loadDataStream( int version, DataInputStream dis, float x, float y )
  {
    TDLog.v( "Drawing Picture Path load data stream");
    // float ccx, ccy;
    // // float orientation = 0;
    // int id;
    // float size;
    // try {
    //   id   = dis.readInt();
    //   ccx  = x + dis.readFloat( );
    //   ccy  = y + dis.readFloat( );
    //   size =  dis.readFloat();
    //   int lvl   = dis.readInt();
    //   int scrap = dis.readInt();

    //   // TDLog.Log( TDLog.LOG_PLOT, "Label <" + text + " " + ccx + " " + ccy + " scale " + scale + " (" + options + ")" );
    //   DrawingPicturePath ret = new DrawingPicturePath( size, ccx, ccy, id, scrap );
    //   // ret.setOrientation( orientation );
    //   ret.mLevel = lvl;
    //   return ret;
    // } catch ( IOException e ) {
    //   TDLog.Error( "PICTURE in error " + e.getMessage() );
    // }
    return null;
  }

  /** picture is already exported with thw photo
   */
  @Override
  public String toTherion( )
  {
    // StringWriter sw = new StringWriter();
    // PrintWriter pw  = new PrintWriter(sw);
    // pw.format(Locale.US, "point %.2f %.2f u:picture -size %.2f -photo %d.jpg ",
    //      cx*TDSetting.mToTherion, -cy*TDSetting.mToTherion, mPhotoSize, (int)mId );
    // // toTherionOrientation( pw );
    // // toTherionOptions( pw );
    // pw.format("\n");
    // return sw.getBuffer().toString();
    return "";
  }

  // FIXME  TODO cSurvey export is not implemented
  @Override
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
  { 
    // String subdir = survey + "/photo"; // "photo/" + survey;
    // String name   = mId + ".jpg"; // Long.toString(mId) + ".jpg";
    // if ( TDFile.hasMSfile( subdir, name ) ) {
    //   byte[] buf = TDExporter.readFileBytes( subdir, name );
    //   if ( buf != null ) {
    //     pw.format("<item type=\"point\" name=\"picture\" cave=\"%s\" branch=\"%s\" text=\"\" ", cave, branch, );
    //     if ( bind != null ) pw.format(" bind=\"%s\" ", bind );
    //     // pw.format(Locale.US, "scale=\"%d\" orientation=\"%.2f\" options=\"%s\" >\n", mScale, mOrientation, ((mOptions   == null)? "" : mOptions) );
    //     pw.format(" <attachment dataformat=\"0\" data=\"%s\" name=\"\" type=\"image/jpeg\" />\n", 
    //       Base64.encodeToString( buf, Base64.NO_WRAP ),
    //     );
    //     float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
    //     float y = DrawingUtil.sceneToWorldY( cx, cy );
    //     pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
    //     pw.format("</item>\n");
    //     // TDLog.v( "toCSurvey() Point " + mPointType + " (" + x + " " + y + ") orientation " + mOrientation );
    //   }
    // }
  }

  /** picture is saved by the photo path
   */
  @Override
  void toDataStream( DataOutputStream dos, int scrap )
  {
    TDLog.v( "Drawing Picture Path to data stream");
    // try {
    //   dos.write( 'J' );
    //   dos.writeInt( ((int)mId) );
    //   dos.writeFloat( cx );
    //   dos.writeFloat( cy );
    //   dos.writeFloat( (float)mPhotoSize );
    //   // dos.writeUTF( BrushManager.getPointThName(mPointType) );
    //   // if ( version >= 401147 ) dos.writeUTF( "" ); // photo has no group
    //   // dos.writeFloat( (float)mOrientation ); // from version 2.7.4e
    //   // dos.writeInt( mScale );
    //   dos.writeInt( mLevel );
    //   dos.writeInt( (scrap >= 0)? scrap : mScrap );
    //   // dos.writeUTF( ( mPointText != null )? mPointText : "" );
    //   // dos.writeUTF( ( mOptions != null )? mOptions : "" );
    //   // TDLog.Log( TDLog.LOG_PLOT, "T " + " " + cx + " " + cy );
    // } catch ( IOException e ) {
    //   TDLog.Error( "PHOTO out error " + e.toString() );
    // }
  }
}

