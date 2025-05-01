/* @file DrawingPointPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: points
 *        type DRAWING_PATH_POINT
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */

package com.topodroid.TDX;

import com.topodroid.math.TDVector;
import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDUtil;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;
import com.topodroid.common.PointScale;

import com.topodroid.num.TDNum;

import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.util.Base64;

public class DrawingPointPath extends DrawingPath
{
  // float mXpos;        // scene coords
  // float mYpos;
  public int mPointType;      // symbol point type (index in symbol-point lib)
  protected int mScale;       // symbol scale
  public double mOrientation; // orientation [degrees]
  public String mPointText;   // point text value (if any)
  public IDrawingLink mLink;  // linked drawing item

  // FIXME-COPYPATH
  // @Override
  // DrawingPath copyPath()
  // {
  //   DrawingPointPath ret = new DrawingPointPath( mPointType, cx, cy, mScale, mPointText, mOptions );
  //   copyTo( ret );
  //   return ret;
  // }

  // FIXME SECTION_RENAME
  /** fix the scrap name in the option string replacing the survey-prefix
   * @param survey_name   name of the new survey
   * @return this point path
   * @note only for section point
   */
  public DrawingPointPath fixScrap( String survey_name )
  {
    if ( survey_name != null && BrushManager.isPointSection( mPointType ) ) {
      String scrapname = mOptions.replace("-scrap ", "");
      if ( scrapname != null ) scrapname = TDUtil.replacePrefix( TDInstance.survey, scrapname );
      if ( scrapname != null ) {
        if ( ! scrapname.startsWith(survey_name) ) {
          int pos = scrapname.lastIndexOf('-');
          scrapname = survey_name + "-" + scrapname.substring(pos+1);
        }
        mOptions = "-scrap " + scrapname;
      } else {
        TDLog.e("section point without scrap-name");
        return null;
      }
    }
    return this;
  }

  // String getTextFromOptions( String options )
  // {
  //   if ( options != null ) {
  //     int len = options.length();
  //     int pos = options.indexOf("-text");
  //     if ( pos > 0 ) {
  //       int start = pos + 5;
  //       while ( start < len && options.charAt( start ) == ' ' ) ++ start;
  //       if ( start < len ) {
  //         int end = start + 1;
  //         while ( end < len && options.charAt( end ) != ' ' ) ++ end;
  //         if ( end < len ) {
  //           mOptions = options.substring(0, start) + options.substring(end);
  //         } else {
  //           mOptions = options.substring(0, start);
  //         }
  //         if ( options.charAt( start ) == '"' ) start ++;
  //         if ( options.charAt( end ) == '"' ) end --;
  //         return options.substring( start, end );
  //       }
  //     }
  //   }
  //   return null;
  // }

  /** cstr
   * @param type    point type
   * @param x       X coord
   * @param y       Y coord
   * @param scale   symbol scale
   * @param scrap   scrap index
   */
  public DrawingPointPath( int type, float x, float y, int scale, int scrap )
  {
    super( DrawingPath.DRAWING_PATH_POINT, null, scrap );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    mPointType = type;
    setCenter( x, y );
    // mScale   = PointScale.SCALE_NONE;
    mOrientation = 0.0;
    mOptions   = BrushManager.getPointDefaultOptions( mPointType );
    mPointText = null; // getTextFromOptions( options ); // this can also reset mOptions
    mLevel     = BrushManager.getPointLevel( mPointType );

    if ( BrushManager.isPointOrientable( mPointType ) ) {
      mOrientation = BrushManager.getPointOrientation( mPointType );
    }
    setPathPaint( BrushManager.getPointPaint( mPointType ) );
    mScale = scale;
    resetPath( 1.0f );
    mLink = null;
  }

  /** cstr
   * @param type    point type
   * @param x       X coord
   * @param y       Y coord
   * @param scale   symbol scale
   * @param text    text string
   * @param options options string
   * @param scrap   scrap index
   */
  public DrawingPointPath( int type, float x, float y, int scale, String text, String options, int scrap )
  {
    super( DrawingPath.DRAWING_PATH_POINT, null, scrap );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    mPointType = type;
    setCenter( x, y );
    // mScale   = PointScale.SCALE_NONE;
    mOrientation = 0.0;
    mOptions   = options;
    mPointText = text; // getTextFromOptions( options ); // this can also reset mOptions
    mLevel     = BrushManager.getPointLevel( type );

    if ( BrushManager.isPointOrientable( type ) ) {
      mOrientation = BrushManager.getPointOrientation(type);
    }
    setPathPaint( BrushManager.getPointPaint( mPointType ) );
    mScale = scale;
    resetPath( 1.0f );
    mLink = null;
    // TDLog.v( "Point cstr " + type + " orientation " + mOrientation );
  }

  /** factory: create a point path from the data stream
   * @param version serialize version
   * @param dis     input data stream
   * @param x       offset X coord [scene ?]
   * @param y       offset Y coord
   * @return the deserialized point path
   */
  public static DrawingPointPath loadDataStream( int version, DataInputStream dis, float x, float y /* , SymbolsPalette missingSymbols */ ) 
  {
    float ccx, ccy, orientation;
    int   type;
    int   scale;
    int   level = DrawingLevel.LEVEL_DEFAULT;
    int   scrap = 0;
    String name;  // th-name
    String group = null;
    String options; // = null;
    String text = null;
    try {
      ccx = x + dis.readFloat();
      ccy = y + dis.readFloat();
      name = dis.readUTF( );
      if ( version >= 401147 ) group = dis.readUTF();
      orientation = dis.readFloat();
      scale   = dis.readInt();
      if ( version >= 401090 ) level = dis.readInt();
      if ( version >= 401160 ) scrap = dis.readInt();
      if ( version >= 303066 ) text  = dis.readUTF();
      options = dis.readUTF();

      // BrushManager.tryLoadMissingPoint( name ); // LOAD_MISSING

      type = BrushManager.getPointIndexByThNameOrGroup( name, group );
      // TDLog.Log( TDLog.LOG_PLOT, "P " + name + " " + type + " " + ccx + " " + ccy + " " + orientation + " " + scale + " options (" + options + ")" );
      // TDLog.v( "PLOT " + name + " " + type + " " + ccx + " " + ccy + " " + orientation + " " + scale + " options (" + options + ")" );
      if ( type < 0 ) {
        // FIXME-MISSING if ( missingSymbols != null ) missingSymbols.addPointFilename( name ); 
        type = 0;
        options = (options != null)? options + " -symbol " + name : "-symbol " + name;
      }
      // FIXME SECTION_RENAME
      // if ( BrushManager.isPointSection( type ) ) {
      //   String scrapname = TDUtil.replacePrefix( TDInstance.survey, options.replace("-scrap ", "") );
      //   scrapname = scrapname.replace( mApp.mSurvey + "-", "" ); // remove survey name from options
      //   option = "-scrap " + scrapname;
      // }
      DrawingPointPath ret = new DrawingPointPath( type, ccx, ccy, scale, text, options, scrap );
      ret.mLevel = level;
      ret.setOrientation( orientation );
      return ret;

      // // TODO parse option for "-text"
      // setPathPaint( BrushManager.getPointPaint( mPointType ) );
      // if ( BrushManager.isPointOrientable( mPointType ) ) {
      //   BrushManager.rotateGradPoint( mPointType, mOrientation );
      //   resetPath( 1.0f );
      //   BrushManager.rotateGradPoint( mPointType, -mOrientation );
      // }
    } catch ( IOException e ) {
      TDLog.e( "POINT in error " + e.getMessage() );
      // TDLog.v( "POINT in error " + e.getMessage() );
    }
    return null;
  }

  // static void globDataStream( int version, DataInputStream dis )
  // {
  //   try {
  //     dis.readFloat();
  //     dis.readFloat();
  //     dis.readUTF( );
  //     if ( version >= 401147 ) dis.readUTF();
  //     dis.readFloat();
  //     dis.readInt();
  //     if ( version >= 401090 ) dis.readInt();
  //     if ( version >= 401160 ) dis.readInt();
  //     if ( version >= 303066 ) dis.readUTF();
  //     dis.readUTF();
  //   } catch ( IOException e ) {
  //     TDLog.e( "POINT in error " + e.getMessage() );
  //     // TDLog.v( "POINT in error " + e.getMessage() );
  //   }
  // }

  /** set the point center - move the point to a new center
   * @param x       X coord
   * @param y       Y coord
   */
  void setCenter( float x, float y )
  {
    cx = x;
    cy = y;
    left   = x; 
    right  = x+1;
    top    = y;
    bottom = y+1;
  }

  /** rotate the point orientation
   * @param dt   rotation angle [degrees]
   * @return true if the point can be rotated
   */
  @Override
  boolean rotateBy( float dt )
  {
    if ( ! BrushManager.isPointOrientable( mPointType ) ) return false;
    setOrientation ( mOrientation + dt );
    return true;
  }

  /** shift the point position
   * @param dx       X coord shift
   * @param dy       Y coord shift
   */
  @Override
  void shiftBy( float dx, float dy )
  {
    cx += dx;
    cy += dy;
    mPath.offset( dx, dy );
    left   += dx;
    right  += dx;
    top    += dy;
    bottom += dy;
  }

  /** scale the point symbol - with respect to the scene
   * @param z   scale factor
   * @param m   scaling matrix
   */
  @Override
  void scaleBy( float z, Matrix m )
  {
    cx *= z;
    cy *= z;
    mPath.transform( m );
    left   *= z;
    right  *= z;
    top    *= z;
    bottom *= z;
  }

  /** affine transform the point symbol - with respect to the scene
   * @param mm   affine matrix (to transform the center)
   * @param m    transform matrix
   */
  @Override
  void affineTransformBy( float[] mm, Matrix m )
  {
    float x = mm[0] * cx + mm[1] * cy + mm[2];
         cy = mm[3] * cx + mm[4] * cy + mm[5];
         cx = x;
    mPath.transform( m );
    left   = cx;   // simplified
    right  = cx+1;
    top    = cy;
    bottom = cy+1;
  }

  // from ICanvasCommand
  /** shift the symbol path
   * @param dx       X shift
   * @param dy       Y shift
   */
  @Override
  public void shiftPathBy( float dx, float dy ) 
  {
    // x1 += dx;
    // y1 += dy;
    // x2 += dx;
    // y2 += dy;
    // cx += dx;
    // cy += dy;
    // mPath.offset( dx, dy );
    // left   += dx;
    // right  += dx;
    // top    += dy;
    // bottom += dy;
  }

  // from ICanvasCommand
  // FIXME SCALE
  /** scale the symbol path
   * @param z   scale factor
   * @param m   scaling matrix
   */
  @Override
  public void scalePathBy( float z, Matrix m )
  {
    // x1 *= z;
    // y1 *= z;
    // x2 *= z;
    // y2 *= z;
    // cx *= z;
    // cy *= z;
    // mPath.transform( m );
    // left   *= z;
    // right  *= z;
    // top    *= z;
    // bottom *= z;
  }

  /** Affine transform the symbol path (empty method)
   * @param mm   affine matrix 
   * @param m    transform matrix
   */
  @Override // empty
  public void affineTransformPathBy( float[] mm, Matrix m )
  {
  }

  /** set the link
   * @param link   new link
   */
  void setLink( IDrawingLink link ) { mLink = link; }

  /** @return the linked item (or null)
   */
  IDrawingLink getLink() { return mLink; }

  /** @return the point color
   */
  int pointColor( ) { return BrushManager.getPointColor( mPointType ); }

  /** get the therion name 
   * @return the point type
   */
  public String getThName() { return  BrushManager.getPointThName( mPointType ); }

  /** get the full therion name 
   * @return the point Therion type (possibly incuding the prefix)
   */
  public String getFullThName() { return  BrushManager.getPointFullThName( mPointType ); }

  /** @return the point Therion type (possibly incuding the prefix), with ':' replaced by '_'
   */
  public String getFullThNameEscapedColon() { return  BrushManager.getPointFullThNameEscapedColon( mPointType ); }

  /** draw the point on the canvas
   * @param canvas    canvas
   * @param matrix    transform matrix
   * @param scale     scale factor
   * @param bbox      clipping rectangle
   * @note canvas is guaranteed not null
   */
  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      if ( TDSetting.mUnscaledPoints ) {
        resetPath( 4 * scale );
      }
      mTransformedPath = new Path( mPath );
      if ( mLandscape && ! BrushManager.isPointOrientable( mPointType ) ) {
	Matrix rot = new Matrix();
	rot.postRotate( 90, cx, cy );
	mTransformedPath.transform( rot );
      }
      mTransformedPath.transform( matrix );
      drawPath( mTransformedPath, canvas );
      if ( mLink != null ) {
        Path link = new Path();
        link.moveTo( cx, cy );
	link.lineTo( mLink.getLinkX(), mLink.getLinkY() );
        if ( mLandscape ) {
	  Matrix rot = new Matrix();
	  rot.postRotate( 90, cx, cy );
	  link.transform( rot );
	}
        link.transform( matrix );
        canvas.drawPath( link, BrushManager.fixedOrangePaint );
      }
    }
  }

  /** draw the point on the canvas
   * @param canvas    canvas
   * @param matrix    transform matrix
   * @param scale     scale factor
   * @param bbox      clipping rectangle
   * @param xor_color xoring color
   * @note canvas is guaranteed not null
   */
  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox, int xor_color )
  {
    if ( intersects( bbox ) ) {
      if ( TDSetting.mUnscaledPoints ) {
        resetPath( 4 * scale );
      }
      mTransformedPath = new Path( mPath );
      if ( mLandscape && ! BrushManager.isPointOrientable( mPointType ) ) {
	Matrix rot = new Matrix();
	rot.postRotate( 90, cx, cy );
	mTransformedPath.transform( rot );
      }
      mTransformedPath.transform( matrix );
      drawPath( mTransformedPath, canvas, xor_color );
      if ( mLink != null ) {
        Path link = new Path();
        link.moveTo( cx, cy );
	link.lineTo( mLink.getLinkX(), mLink.getLinkY() );
        if ( mLandscape ) {
	  Matrix rot = new Matrix();
	  rot.postRotate( 90, cx, cy );
	  link.transform( rot );
	}
        link.transform( matrix );
        canvas.drawPath( link, BrushManager.fixedOrangePaint );
      }
    }
  }

  /** set the scale index
   * @param scale   new scale index
   */
  void setScale( int scale )
  {
    if ( scale != mScale ) {
      mScale = scale;
      resetPath( 1.0f );
    }
  }

  /** get the scale index
   * @return the scale index
   */
  public int getScale() { return mScale; }

  /** get the scale value
   * @return the scale value
   */
  public float getScaleValue() // FIX Asenov
  {
    switch ( mScale ) {
      case PointScale.SCALE_XS: return 0.50f;
      case PointScale.SCALE_S:  return 0.72f;
      case PointScale.SCALE_L:  return 1.41f;
      case PointScale.SCALE_XL: return 2.00f;
    }
    return 1;
  }
      
  /** recreate the symbol path
   * @param f   post-scale factor
   */
  private void resetPath( float f )
  {
    // TDLog.v( "Reset path " + mOrientation + " scale " + mScale );
    Matrix m = new Matrix();
    if ( ! BrushManager.isPointLabel( mPointType ) ) {
      if ( BrushManager.isPointOrientable( mPointType ) ) {
        m.postRotate( (float)mOrientation );
      }
      switch ( mScale ) {
        case PointScale.SCALE_XS: f *= 0.50f; break;
        case PointScale.SCALE_S:  f *= 0.72f; break;
        case PointScale.SCALE_L:  f *= 1.41f; break;
        case PointScale.SCALE_XL: f *= 2.00f; break;
      }
      m.postScale(f,f);
      makePath( BrushManager.getPointOrigPath( mPointType ), m, cx, cy );
    }
  }

  // void setPos( float x, float y ) 
  // {
  //   setCenter( x, y );
  // }

  // void setPointType( int t ) { mPointType = t; }

  /** get the type of the point
   * @return the point type
   */
  public int pointType() { return mPointType; }

  // double xpos() { return cx; }
  // double ypos() { return cy; }

  // double orientation() { return mOrientation; }

  /** set the point orientation
   * @param angle  orientation angle [degrees]
   */
  @Override
  void setOrientation( double angle ) 
  { 
    // TDLog.Log( TDLog.LOG_PATH, "Point " + mPointType + " set Orientation " + angle );
    // TDLog.v( "Point::set Orientation " + angle );
    mOrientation = TDMath.in360( angle ); 
    resetPath( 1.0f );
  }

  /** get the text of the point
   * @return the point text
   */
  public String getPointText() { return mPointText; }

  /** set the text of the point
   * @param text    the point text
   */
  void setPointText( String text )
  {
    mPointText = text;
  }

  // /** move the point to a new position - UNUSED
  //  * @param x   X coord (in the scene)
  //  * @param y   Y coord (in the scene)
  //  */
  // void shiftTo( float x, float y ) // x,y scene coords
  // {
  //   mPath.offset( x-cx, y-cy );
  //   setCenter( x, y );
  // }

//   @Override
//   void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
//   { 
//     int size = mScale - PointScale.SCALE_XS;
//     int layer  = BrushManager.getPointCsxLayer( mPointType );
//     int type   = BrushManager.getPointCsxType( mPointType );
//     int cat    = BrushManager.getPointCsxCategory( mPointType );
//     String csx = BrushManager.getPointCsx( mPointType );
//     pw.format("<item layer=\"%d\" cave=\"%s\" branch=\"%s\" type=\"%d\" category=\"%d\" transparency=\"0.00\" data=\"",
//       layer, cave, branch, type, cat );
//     pw.format("&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;!DOCTYPE svg PUBLIC &quot;-//W3C//DTD SVG 1.1//EN&quot; &quot;http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd&quot;[]&gt;&lt;svg xmlns=&quot;http://www.w3.org/2000/svg&quot; xml:space=&quot;preserve&quot; style=&quot;shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd&quot; xmlns:xlink=&quot;http://www.w3.org/1999/xlink&quot;&gt;&lt;defs&gt;&lt;style type=&quot;text/css&quot;&gt;&lt;![CDATA[ .str0 {stroke:#1F1A17;stroke-width:0.2} .fil0 {fill:none} ]]&gt;&lt;/style&gt;&lt;/defs&gt;&lt;g id=&quot;Livello_%d&quot;&gt;", layer );
//     pw.format("%s", csx );
//     pw.format("&lt;/g&gt;&lt;/svg&gt;\" ");
//     if ( bind != null ) pw.format(" bind=\"%s\" ", bind );
//     pw.format(Locale.US, "dataformat=\"0\" signsize=\"%d\" angle=\"%.2f\" >\n", size, mOrientation );
//     pw.format("  <pen type=\"10\" />\n");
//     pw.format("  <brush type=\"7\" />\n");
//     float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
//     float y = DrawingUtil.sceneToWorldY( cx, cy );
//     pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
//     pw.format("  <datarow>\n");
//     pw.format("  </datarow>\n");
//     pw.format("</item>\n");
// 
//     // TDLog.v( "toCSurvey() Point " + mPointType + " (" + x + " " + y + ") orientation " + mOrientation );
//   }

  /** export the point path in cSurvey format
   * @param pw      export writer
   * @param survey  survey name
   * @param cave    cave name
   * @param branch  branch name
   * @param bind    cSurvey binding
   */
  @Override
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind )
  { 
    String name = getThName( );
    pw.format("<item type=\"point\" name=\"%s\" cave=\"%s\" branch=\"%s\" text=\"%s\" ", name, cave, branch, ((mPointText == null)? "" : mPointText) );
    if ( bind != null ) pw.format(" bind=\"%s\" ", bind );
    pw.format(Locale.US, "scale=\"%d\" orientation=\"%.2f\" options=\"%s\" >\n", mScale, mOrientation, ((mOptions   == null)? "" : mOptions) );
    float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
    float y = DrawingUtil.sceneToWorldY( cx, cy );
    pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
    pw.format("</item>\n");
    // TDLog.v( "toCSurvey() Point " + mPointType + " (" + x + " " + y + ") orientation " + mOrientation );
  }

  /** export the point path in cSurvey format
   * @param pw      export writer
   * @param survey  survey name
   * @param cave    cave name
   * @param branch  branch name
   * @param bind    cSurvey binding
   * @param extra   ...
   * @param section X-Section info
   */
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind, String extra, PlotInfo section )
  { 
    String name = getThName( );
    pw.format("<item type=\"point\" name=\"%s\" cave=\"%s\" branch=\"%s\" text=\"%s\" ", name, cave, branch, ((mPointText == null)? "" : mPointText) );
    if ( bind != null ) pw.format("bind=\"%s\" ", bind );
    if ( extra != null ) pw.format("%s ", extra );
    pw.format(Locale.US, "scale=\"%d\" orientation=\"%.2f\" options=\"%s\" >\n", mScale, mOrientation, ((mOptions   == null)? "" : mOptions) );
    float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
    float y = DrawingUtil.sceneToWorldY( cx, cy );
    pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
    if ( section != null ) {
      // include cross-section
      // pw.format("    <crosssection id=\"%s\" design=\"0\" crosssection=\"%d\">\n", section.name, section.csxIndex );
      pw.format("    <crosssection>\n" );
      exportTCsxXSection( pw, section, survey, cave, branch );
      pw.format("    </crosssection>\n" );
      String subdir = TDInstance.survey + "/photo"; // "photo/" + TDInstance.survey;
      String filename = section.name + ".jpg";
      if ( TDFile.hasMSfile( subdir, filename ) ) { // if ( TDFile.hasMSpath( imagefilename ) )
        byte[] buf = TDExporter.readFileBytes( subdir, filename );
        if ( buf != null ) {
          pw.format("    <crosssectionfile>\n" );
          pw.format(" <attachment dataformat=\"0\" data=\"%s\" name=\"\" note=\"%s\" type=\"image/jpeg\" />\n", 
            Base64.encodeToString( buf, Base64.NO_WRAP ),
            ((mPointText==null)?"":mPointText)
          );
          pw.format("    </crosssectionfile>\n" );
        }
      }
    }
    pw.format("</item>\n");
    // TDLog.v( "toCSurvey() Point " + mPointType + " (" + x + " " + y + ") orientation " + mOrientation );
  }

  /** export the X-Section in cSurvey format
   * @param pw      export writer
   * @param section X-Section info
   * @param survey  survey name
   * @param cave    cave name
   * @param branch  branch name
   */
  private static void exportTCsxXSection( PrintWriter pw, PlotInfo section, String survey, String cave, String branch /* , String session */ )
  {
    if ( section == null ) return;
    // open xsection file
    // String filename = TDPath.getSurveyPlotTdrFile( survey, section.name );
    String filename = survey + "-" + section.name + ".tdr";
    DrawingIO.doExportTCsxXSection( pw, filename, survey, cave, branch, /* session, */ section.name /* , drawingUtil */ ); // bind=section.name
  }

  /** serialize point path to therion format
   * @return therion representation of the point
   */ 
  @Override
  String toTherion( )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);

    String th_name = getFullThName();
    pw.format(Locale.US, "point %.2f %.2f %s", cx*TDSetting.mToTherion, -cy*TDSetting.mToTherion, th_name );
    toTherionOrientation( pw );
    // FIXME SECTION_RENAME
    // if ( ! BrushManager.isPointSection( mPointType ) )
    toTherionTextOrValue( pw );
    toTherionOptions( pw );
    pw.format("\n");

    return sw.getBuffer().toString();
  }

  /** write orientation in therion format
   * @param pw   output writer
   */
  void toTherionOrientation( PrintWriter pw )
  {
    if ( mOrientation != 0.0 ) {
      pw.format(Locale.US, " -orientation %.2f", mOrientation);
    }
  }

  /** write text/value in therion format
   * @param pw   output writer
   */
  private void toTherionTextOrValue( PrintWriter pw )
  {
    if ( mPointText != null && mPointText.length() > 0 ) {
      if ( BrushManager.pointHasText(mPointType) ) { // label, remark
        pw.format(" -text \"%s\"", mPointText );
      } else if ( BrushManager.pointHasValue(mPointType) ) { // passage-height
        pw.format(" -value %s", mPointText );
      }
    }
  }

  /** write options in therion format
   * @param pw   output writer
   */
  void toTherionOptions( PrintWriter pw )
  {
    if ( mScale != PointScale.SCALE_M ) {
      pw.format( " -scale %s", PointScale.scaleToString( mScale ) );
      // switch ( mScale ) {
      //   case PointScale.SCALE_XS: pw.format( " -scale xs" ); break;
      //   case PointScale.SCALE_S:  pw.format( " -scale s" ); break;
      //   case PointScale.SCALE_L:  pw.format( " -scale l" ); break;
      //   case PointScale.SCALE_XL: pw.format( " -scale xl" ); break;
      // }
    }
    // FIXME SECTION_RENAME
    // if ( BrushManager.isPointSection( type ) ) {
    //   String scrapname = TDUtil.replacePrefix( TDInstance.survey, mOptions.replace("-scrap ", "") );
    //   pw.format(" -scrap %s-%s", mApp.mSurvey, scrapname );
    // } else {
      if ( mOptions != null && mOptions.length() > 0 ) {
        pw.format(" %s", mOptions );
      }
    // }
  }

  // override mScrap with scrap
  /** export point to data stream
   * @param dos    output stream
   * @param scrap  scrap index
   */
  @Override
  void toDataStream( DataOutputStream dos, int scrap )
  {
    String name  = getThName();
    if ( name == null ) {
      name = SymbolLibrary.USER;
      TDLog.e( "null point name" );
    }
    String group = BrushManager.getPointGroup(mPointType);
    try {
      dos.write( 'P' );
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      dos.writeUTF( name );
      // if ( version >= 401147 )
        dos.writeUTF( (group != null)? group : "" );
      dos.writeFloat( (float)mOrientation );
      dos.writeInt( mScale );
      // if ( version >= 401090 )
        dos.writeInt( mLevel );
      // if ( version >= 401160 )
        dos.writeInt( (scrap >= 0)? scrap : mScrap ); 
      // if ( version >= 303066 ) 
        dos.writeUTF( (mPointText != null)? mPointText : "" );
      dos.writeUTF( (mOptions != null)? mOptions : "" );
      // TDLog.Log( TDLog.LOG_PLOT, "P " + name + " " + cx + " " + cy );
    } catch ( IOException e ) {
      TDLog.e( "POINT out error " + e.toString() );
    }
  }

  /** write point in Cave3D format
   * @param pw    writer
   * @param type  ...
   * @param cmd   command manager
   * @param num   data reduction
   */
  @Override
  void toCave3D( PrintWriter pw, int type, DrawingCommandManager cmd, TDNum num )
  {
    // cx,cy are in pixels divide by 20 to write coords in meters
    String name  = getThName();
    float x = DrawingUtil.sceneToWorldX( cx, cy );
    float y = DrawingUtil.sceneToWorldY( cx, cy );
    float v = 0;
    TDVector vv = cmd.getCave3Dv( cx, cy, num );
    if ( type == PlotType.PLOT_PLAN ) {
      v = vv.z;
    } else {
      v = y;
      x = vv.x;
      y = vv.y;
    }
    pw.format( Locale.US, "POINT %s %.1f %f %f %f\n", name, mOrientation, x, -y, -v );
  }

  /** write point in Cave3D format
   * @param pw    writer
   * @param type  ...
   * @param V1    ...
   * @param V2    ...
   */
  @Override
  void toCave3D( PrintWriter pw, int type, TDVector V1, TDVector V2 )
  {
    // cx,cy are in pixels divide by 20 to write coords in meters
    String name  = getThName();
    TDVector vv = DrawingPath.getCave3D( cx, cy, V1, V2 );
    pw.format( Locale.US, "POINT %s %.1f %f %f %f\n", name, mOrientation, vv.x,  vv.y, -vv.z );
  }

}

