/* @file DrawingIO.java    
 *
 * @author marco corvi
 * @date nov 2015
 *
 * @brief TopoDroid drawing: drawing I/O
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDUtil;
import com.topodroid.num.NumStation;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;
import com.topodroid.utils.TDMath;
import com.topodroid.math.TDVector;
// import com.topodroid.common.PointScale;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
// import java.io.BufferedReader;
// import java.io.BufferedInputStream;
// import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
// import java.nio.channels.FileChannel;

import java.io.FileNotFoundException;
import java.io.IOException;
// import java.io.EOFException;

import java.util.List;
import java.util.ArrayList;
// import android.util.ArraySet; // REQUIRES API-23
import java.util.Locale;

import android.graphics.RectF;

public class DrawingIO
{
  // UNUSED 
  // this should go in utils
  // private static String readLine( BufferedReader br )
  // {
  //   String line = null;
  //   try {
  //     line = br.readLine();
  //   } catch ( IOException e ) {
  //     e.printStackTrace();
  //   }
  //   if ( line != null ) {
  //     line = TDString.spacesToSpace( line.trim() );
  //     // TDString.splitOnSpaces( line ); // FIXME
  //   }
  //   return line;
  // } 

  /* NOTE therion th2 files can no longer be supported because therion scale is not fixed
   */
  // static boolean doLoadTherion( DrawingSurface surface,
  //                               String filename,
  //                               float dx, float dy,
  //                               SymbolsPalette missingSymbols,
  //                               SymbolsPalette localPalette )
  // {
  //   float x, y, x1, y1, x2, y2;
  //   boolean is_not_section = true;
  //   float toTherion = TDSetting.mToTherion;

  //   // TDLog.Log( TDLog.LOG_PLOT, "Load Therion file " + filename + " delta " + dx + " " + dy );
  //   BrushManager.resetPointOrientations();

  //   // TDLog.Log( TDLog.LOG_PLOT, "after reset 0: " + BrushManager.mOrientation[0]
  //   //                      + " 7: " + BrushManager.mOrientation[7] );

  //   // TDLog.v( "drawing I/O load therion " + filename );
  //   synchronized( TDPath.mTdrLock ) // FIXME-THREAD_SAFE
  //   {
  //     try {
  //       TDLog.Log( TDLog.LOG_IO, "load plot from Therion file " + filename );
  //       FileReader fr = TDFile.getFileReader( filename );
  //       BufferedReader br = new BufferedReader( fr );
  //       String line = null;
  //       while ( (line = readLine(br)) != null ) {
  //         int comment = line.indexOf('#');
  //         if ( comment == 0 ) {
  //           if ( line.startsWith( "#P " ) ) { // POINT PALETTE
  //             if ( localPalette != null ) {
  //               localPalette.mPalettePoint.clear();
  //               localPalette.addPointFilename( SymbolLibrary.USER );
  //               String[] syms = line.split( " " );
  //               for ( int k=1; k<syms.length; ++k ) {
  //                 if ( syms[k].length() > 0 && ! syms[k].equals(SymbolLibrary.USER) ) localPalette.addPointFilename( syms[k] );
  //               }
  //               BrushManager.mPointLib.makeEnabledListFromPalette( localPalette );
  //             }
  //           } else if ( line.startsWith( "#L " ) ) { // LINE PALETTE
  //             if ( localPalette != null ) {
  //               localPalette.mPaletteLine.clear();
  //               localPalette.addLineFilename(SymbolLibrary.USER);
  //               String[] syms = line.split( " " );
  //               for ( int k=1; k<syms.length; ++k ) {
  //                 if ( syms[k].length() > 0 && ! syms[k].equals(SymbolLibrary.USER) ) localPalette.addLineFilename( syms[k] );
  //               }
  //               BrushManager.mLineLib.makeEnabledListFromPalette( localPalette );
  //             }
  //           } else if ( line.startsWith( "#A " ) ) { // AREA PALETTE
  //             if ( localPalette != null ) {
  //               localPalette.mPaletteArea.clear();
  //               localPalette.addAreaFilename(SymbolLibrary.USER);
  //               String[] syms = line.split( " " );
  //               for ( int k=1; k<syms.length; ++k ) {
  //                 if ( syms[k].length() > 0 && ! syms[k].equals(SymbolLibrary.USER) ) localPalette.addAreaFilename( syms[k] );
  //               }
  //               BrushManager.mAreaLib.makeEnabledListFromPalette( localPalette );
  //             }
  //           }
  //           continue;
  //         } else if (comment > 0 ) {
  //           line = line.substring( 0, comment );
  //         }
  //         if ( line.length() == 0 /* || line.charAt(0) == '#' */ ) {
  //           continue;
  //         }

  //         // TDLog.Log( TDLog.LOG_PLOT, "  line: >>" + line + "<<");
  //         line = TDString.splitOnSpaces( line );
  //         String[] vals = line.split( " " );
  //         if ( vals.length == 0 ) continue;
  //         if ( vals[0].equals( "scrap" ) ) {
  //           if ( vals.length < 4 ) {
  //             TDLog.e( "bad scrap cmd: " + line );
  //           } else {
  //             // String name = vals[1];
  //             // skip "-projection" vals[2]
  //             is_not_section = ! vals[3].equals("none");
  //           }
  //         } else if ( vals[0].equals( TDPath.DIR_POINT ) ) {
  //           // ****** THERION POINT ********************************** point X Y type [options]
  //           if ( vals.length < 4 ) {
  //             TDLog.e( "bad point cmd: " + line );
  //           } else {
  //             int ptType = BrushManager.getPointLibSize();
  //             boolean has_orientation = false;
  //             float orientation = 0.0f;
  //             int scale = PointScale.SCALE_M;
  //             String options = null;

  //             try {
  //               x = dx + Float.parseFloat( vals[1] ) / toTherion;
  //               y = dy - Float.parseFloat( vals[2] ) / toTherion;
  //             } catch ( NumberFormatException e ) {
  //               TDLog.e( "Therion Point error (number fmt) <" + line + ">" );
  //               continue;
  //             }
  //             String type = vals[3];
  //             String label_text = null;
  //             int k = 4;
  //             if ( type.equals( "station" ) ) {
  //               if ( ! TDSetting.mAutoStations ) {
  //                 if ( vals.length > k+1 && vals[k].equals( "-name" ) ) {
  //                   String name = vals[k+1];
  //                   DrawingStationUser station_path = new DrawingStationUser( name, x, y, scale );
  //                   surface.addDrawingPath( station_path );
  //                 }
  //               }
  //               continue;
  //             }
  //             while ( vals.length > k ) { 
  //               if ( vals[k].equals( "-orientation" ) ) {
  //                 try {
  //                   orientation = Float.parseFloat( vals[k+1] );
  //                   has_orientation = true;
  //                 } catch ( NumberFormatException e ) {
  //                   TDLog.e( "Therion Point orientation error : " + line );
  //                 }
  //                 k += 2;
  //               } else if ( vals[k].equals( "-scale" ) ) {
  //                 // FIXME assert (vals.length > k+1 );
  //                 if ( vals[k+1].equals("xs") ) {
  //                   scale = PointScale.SCALE_XS;
  //                 } else if ( vals[k+1].equals("s") ) {
  //                   scale = PointScale.SCALE_S;
  //                 } else if ( vals[k+1].equals("l") ) {
  //                   scale = PointScale.SCALE_L;
  //                 } else if ( vals[k+1].equals("xl") ) {
  //                   scale = PointScale.SCALE_XL;
  //                 } 
  //                 k += 2;
  //               } else if ( vals[k].equals( "-text" ) ) {
  //                 // FIXME assert (vals.length > k+1 );
  //                 label_text = vals[k+1];
  //                 k += 2;
  //                 if ( label_text.startsWith( "\"" ) ) {
  //                   StringBuilder sb = new StringBuilder();
  //                   sb.append(label_text);
  //                   while ( k < vals.length ) {
  //                     sb.append(" ").append(vals[k]);
  //                     // label_text = label_text + " " + vals[k];
  //                     if ( vals[k].endsWith( "\"" ) ) break;
  //                     ++ k;
  //                   }
  //                   label_text = sb.toString().replaceAll( "\"", "" );
  //                   ++ k;
  //                 }
  //               } else {
  //                 options = TDUtil.concat( vals, k );
  //                 k = vals.length;
  //               }
  //             }

  //             BrushManager.tryLoadMissingPoint( type );
  //             // map pre 3.1.1 therion names to 3.1.1 names
  //             String thname = type;
  //             else if ( thname.equals( "archeo" ) ) { thname = "archeo-material"; }
  //             ptType = BrushManager.getPointIndexByThName( thname );
  //             // TDLog.v( "type " + type + " thname " + thname + " " + ptType );
  //             if ( ptType < 0 ) {
  //               if ( missingSymbols != null ) missingSymbols.addPointFilename( type ); // add "type" to the missing point-types
  //               ptType = 0; // SymbolPointLibrary.mPointUserIndex; // FIXME
  //               // continue;
  //             }

  //             if ( ptType == BrushManager.getPointLabelIndex() ) {
  //               if ( label_text != null ) {
  //                 // "danger" is no longer mapped on a label 
  //                 // if ( label_text.equals( "!" ) ) {    // "danger" point
  //                 //   DrawingPointPath path = new DrawingPointPath( BrushManager.getPointDangerIndex(), x, y, scale, text, options );
  //                 //   surface.addDrawingPath( path );
  //                 // } else {                             // regular label
  //                   DrawingLabelPath path = new DrawingLabelPath( label_text, x, y, scale, options );
  //                   if ( has_orientation ) {
  //                     path.setOrientation( orientation );
  //                   }
  //                   surface.addDrawingPath( path );
  //                 // }
  //               }
  //             } else if ( has_orientation && BrushManager.isPointOrientable(ptType) ) {
  //               // TDLog.Log( TDLog.LOG_PLOT, "[2] point " + ptType + " has orientation " + orientation );
  //               BrushManager.rotateGradPoint( ptType, orientation );
  //               DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, label_text, options );
  //               surface.addDrawingPath( path );
  //               BrushManager.rotateGradPoint( ptType, -orientation );
  //             } else {
  //               DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, label_text, options );
  //               surface.addDrawingPath( path );
  //             }
  //           }
  //         } else if ( vals[0].equals( TDPath.DIR_LINE ) ) {
  //           // ********* THERION LINES ************************************************************
  //           if ( vals.length < 2 ) {
  //             TDLog.e( "bad line cmd: " + line );
  //           } else {
  //             if ( vals.length >= 6 && vals[1].equals( "border" ) && vals[2].equals( "-id" ) ) { // THERION AREAS
  //               boolean visible = true;
  //               // TDLog.Log( TDLog.LOG_PLOT, "area id " + vals[3] );
  //               if ( vals.length >= 8 && vals[6].equals("-visibility") && vals[7].equals("off") ) {
  //                 visible = false;
  //               }
  //               int arType = BrushManager.mAreaLib.size();
  //               DrawingAreaPath path = new DrawingAreaPath( arType, vals[3], visible );

  //               // TODO insert new area-path
  //               line = readLine( br );
  //               if ( ! line.equals( "endline" ) ) { 
  //                 String[] pt = line.split( "\\s+" );
  //                 try {
  //                   x = dx + Float.parseFloat( pt[0] ) / toTherion;
  //                   y = dy - Float.parseFloat( pt[1] ) / toTherion;
  //                 } catch ( NumberFormatException e ) {
  //                   TDLog.e( "Therion Line error (number fmt) <" + line + ">" );
  //                   continue;
  //                 }
  //                 path.addStartPoint( x, y );

  //                 while ( (line = readLine( br )) != null ) {
  //                   if ( line.equals( "endline" ) ) {
  //                     line = readLine( br ); // area statement
  //                     String[] vals2 = line.split( " " );
  //                     if ( vals2.length >= 2 ) {
  //                       BrushManager.tryLoadMissingArea( vals2[1] );
  //                       String thname = vals2[1];
  //                       arType = BrushManager.getAreaIndexByThName( thname );
  //                       if ( arType < 0 ) {
  //                         if ( missingSymbols != null ) missingSymbols.addAreaFilename( vals2[1] );
  //                         arType = 0; // SymbolAreaLibrary.mAreaUserIndex; // FIXME
  //                         // continue;
  //                       }
  //                       // TDLog.Log(TDLog.LOG_PLOT, "set area type " + arType + " " + vals2[1]);
  //                       double orientation = 0;
  //                       if ( vals2.length >= 4 && vals2[2].equals("#orientation") ) {
  //                         try {
  //                           orientation = Double.parseDouble( vals2[3] );
  //                         } catch ( NumberFormatException e ) { 
  //                           TDLog.e( "THERION area orientation error <" + line + ">" );
  //                         }
  //                       }
  //                       path.setAreaType( arType );
  //                       path.setOrientation( orientation );
  //                       surface.addDrawingPath( path );
  //                     }
  //                     line = readLine( br ); // skip two lines
  //                     line = readLine( br );
  //                     break;
  //                   }
  //                   // TDLog.Log( TDLog.LOG_DEBUG, "  line point: >>" + line + "<<");
  //                   String[] pt2 = line.split( " " );
  //                   if ( pt2.length == 2 ) {
  //                     try {
  //                       x = dx + Float.parseFloat( pt2[0] ) / toTherion;
  //                       y = dy - Float.parseFloat( pt2[1] ) / toTherion;
  //                       path.addPoint( x, y );
  //                       // TDLog.Log( TDLog.LOG_DEBUG, "area pt " + x + " " + y);
  //                     } catch ( NumberFormatException e ) {
  //                       TDLog.e( "THERION line X-Y error (10) <" + line + ">" );
  //                       continue;
  //                     } catch ( ArrayIndexOutOfBoundsException e ) {
  //                       TDLog.e( "THERION line X-Y error (11) " + line );
  //                       continue;
  //                     }
  //                   } else if ( pt2.length == 6 ) {
  //                     try {
  //                       x1 = dx + Float.parseFloat( pt2[0] ) / toTherion;
  //                       y1 = dy - Float.parseFloat( pt2[1] ) / toTherion;
  //                       x2 = dx + Float.parseFloat( pt2[2] ) / toTherion;
  //                       y2 = dy - Float.parseFloat( pt2[3] ) / toTherion;
  //                       x  = dx + Float.parseFloat( pt2[4] ) / toTherion;
  //                       y  = dy - Float.parseFloat( pt2[5] ) / toTherion;
  //                       path.addPoint3( x1, y1, x2, y2, x, y );
  //                       // TDLog.Log( TDLog.LOG_DEBUG, "area pt " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + x + " " + y);
  //                     } catch ( NumberFormatException e ) {
  //                       TDLog.e( "THERION line X-Y error (12) <" + line + ">" );
  //                       continue;
  //                     } catch ( ArrayIndexOutOfBoundsException e ) {
  //                       TDLog.e( "THERION line X-Y error (13) " + line );
  //                       continue;
  //                     }
  //                   }
  //                 }
  //               }
  //             } else { // ********* regular lines
  //               // FIXME assert (vals.length > 1 );
  //               // TDLog.Log( TDLog.LOG_PLOT, "line type " + vals[1] );
  //               boolean closed = false;
  //               boolean reversed = false;
  //               int outline = DrawingLinePath.OUTLINE_UNDEF;
  //               String options = null;
  //              
  //               String type = vals[1];
  //               for (int index = 2; index < vals.length; ++index ) {
  //                 if ( vals[index] == null || vals[index].length() == 0 ) {
  //                   continue;
  //                 }
  //                 if ( vals[index].equals( "-close" ) ) {
  //                   ++ index;
  //                   if ( vals.length > index && vals[index].equals( "on" ) ) {
  //                     closed = true;
  //                   }
  //                 } else if ( vals[index].equals( "-reverse" ) ) {
  //                   ++ index;
  //                   if ( vals.length > index && vals[index].equals( "on" ) ) {
  //                     reversed = true;
  //                   }
  //                 } else if ( vals[index].equals( "-outline" ) ) {
  //                   ++ index;
  //                   if ( vals.length > index ) {
  //                     if ( vals[index].equals( "out" ) ) { outline = DrawingLinePath.OUTLINE_OUT; }
  //                     else if ( vals[index].equals( "in" ) ) { outline = DrawingLinePath.OUTLINE_IN; }
  //                     else if ( vals[index].equals( "none" ) ) { outline = DrawingLinePath.OUTLINE_NONE; }
  //                   }
  //                 } else {
  //                   if ( options == null ) {
  //                     options = vals[index];
  //                   } else {
  //                     options += " " + vals[index];
  //                   }
  //                 } 
  //               }
  //               
  //               int lnType = BrushManager.mLineLib.size();
  //               DrawingLinePath path = null;
  //               BrushManager.tryLoadMissingLine( type );
  //               String thname = type;
  //               lnType = BrushManager.getLineIndexByThName( thname );
  //               if ( lnType < 0 ) {
  //                 if ( missingSymbols != null ) missingSymbols.addLineFilename( type );
  //                 lnType = 0; // SymbolLineLibrary.mLineUserIndex; // FIXME
  //                 // continue;
  //               }
  //               // TODO insert new line-path
  //               line = readLine( br );
  //               if ( ! line.equals( "endline" ) ) { 
  //                 path = new DrawingLinePath( lnType );
  //                 path.setClosed( closed );
  //                 path.setReversed( reversed );
  //                 if ( outline != DrawingLinePath.OUTLINE_UNDEF ) path.mOutline = outline;
  //                 if ( options != null ) path.setOptions( options );

  //                 // TDLog.Log( TDLog.LOG_PLOT, "  line start point: <" + line + ">");
  //                 String[] pt0 = line.split( "\\s+" );
  //                 try {
  //                   x = dx + Float.parseFloat( pt0[0] ) / toTherion;
  //                   y = dy - Float.parseFloat( pt0[1] ) / toTherion;
  //                   path.addStartPoint( x, y );
  //                 } catch ( NumberFormatException e ) {
  //                   TDLog.e( "THERION line X-Y error (1) <" + line + ">" );
  //                   continue;
  //                 } catch ( ArrayIndexOutOfBoundsException e ) {
  //                   TDLog.e( "THERION line X-Y error (2) " + line );
  //                   continue;
  //                 }
  //                 // TDLog.v( "  line start point: <" + line + "> " + x + " " + y );
  //                 while ( (line = readLine( br )) != null ) {
  //                   if ( line.contains( "l-size" ) ) continue;
  //                   if ( line.equals( "endline" ) ) {
  //                     if ( path != null ) {
  //                       if ( type.equals(SymbolLibrary.SECTION) ) { // section line only in non-section scraps
  //                         if ( is_not_section ) {
  //                           path.makeStraight( );
  //                         }
  //                       } else {
  //                         path.computeUnitNormal(); // for section-line already done by makeStraight
  //                       } 
  //                       surface.addDrawingPath( path );
  //                     }
  //                     break;
  //                   }
  //                   if ( path != null ) {
  //                     // TDLog.Log( TDLog.LOG_PLOT, "  line point: >>" + line + "<<");
  //                     String[] pt = line.split( " " );
  //                     if ( pt.length == 2 ) {
  //                       try {
  //                         x = dx + Float.parseFloat( pt[0] ) / toTherion;
  //                         y = dy - Float.parseFloat( pt[1] ) / toTherion;
  //                         path.addPoint( x, y );
  //                       } catch ( NumberFormatException e ) {
  //                         TDLog.e( "THERION line X-Y error (3) <" + line + ">" );
  //                         continue;
  //                       } catch ( ArrayIndexOutOfBoundsException e ) {
  //                         TDLog.e( "THERION line X-Y error (4) " + line );
  //                         continue;
  //                       }
  //                     } else if ( pt.length == 6 ) {
  //                       try {
  //                         x1 = dx + Float.parseFloat( pt[0] ) / toTherion;
  //                         y1 = dy - Float.parseFloat( pt[1] ) / toTherion;
  //                         x2 = dx + Float.parseFloat( pt[2] ) / toTherion;
  //                         y2 = dy - Float.parseFloat( pt[3] ) / toTherion;
  //                         x  = dx + Float.parseFloat( pt[4] ) / toTherion;
  //                         y  = dy - Float.parseFloat( pt[5] ) / toTherion;
  //                         path.addPoint3( x1, y1, x2, y2, x, y );
  //                       } catch ( NumberFormatException e ) {
  //                         TDLog.e( "THERION line X-Y error (5) <" + line + ">" );
  //                         continue;
  //                       } catch ( ArrayIndexOutOfBoundsException e ) {
  //                         TDLog.e( "THERION line X-Y error (6) " + line );
  //                         continue;
  //                       }
  //                     }
  //                   }
  //                 } // end while ( line-points )
  //               }
  //             }
  //           }
  //         }
  //       }
  //     } catch ( FileNotFoundException e ) {
  //       // this is OK
  //     // } catch ( IOException e ) {
  //     //   e.printStackTrace();
  //     }
  //   }
  //   // remove repeated names

  //   return (missingSymbols == null ) || missingSymbols.isOK();
  // }

  // =========================================================================
  // EXPORT 

  // exportTherion calls DrawingSurface' exportTherion, 
  // which calls DrawingCommandManager's exportTherion,
  // which calls the full method exportTherion with the list of sketch items

  /* UNUSED ( was exportTherion )
   * @note DataHelper and SID are necessary to export splays by the station
   * @param manager   drawing items
   * @param type      plot type
   * @param file      output file
   * @param fullname  full scrap name ( filename without extension )
   * @param projname  ...
   * @param proj_dir  projection direction (for projected profile)
   * @param oblique   oblique projection angle (not used)
   * @param multisketch multisketch ...
   * @param th2_edit    therion th2 editor TH2EDIT
   */
  static void exportTherionExport( DrawingCommandManager manager, int type, File file, String fullname, String projname, int proj_dir, int oblique, boolean multisketch, boolean th2_edit )
  {
    // TDLog.Log( TDLog.LOG_IO, "export therion " + fullname + " file " + file.getPath() );
    // TDLog.v( "EXPORT THERION " + fullname + " file " + file.getPath() );
    try {
      FileWriter fw = TDFile.getFileWriter( file );
      BufferedWriter bw = new BufferedWriter( fw );
      exportTherion( manager, type, bw, fullname, projname, proj_dir, oblique, multisketch, th2_edit ); // TH2EDIT
    } catch ( IOException e ) {
      TDLog.e( "THERION export i/o error [1] " + e.getMessage() );
    }
  }

  /**
   * @param oblique   oblique projection angle (not used)
   */
  static void exportTherion( DrawingCommandManager manager, int type, BufferedWriter bw, String fullname, String projname, int proj_dir, int oblique, boolean multisketch, boolean th2_edit )  // TH2EDIT
  {
    try {
      manager.exportTherion( type, bw, fullname, projname, proj_dir, oblique, multisketch, th2_edit );  // TH2EDIT
      bw.flush();
      bw.close();
    } catch ( IOException e ) {
      TDLog.e( "THERION export i/o error [2] " + e.getMessage() );
    }
  }

  /** entry point to export data-stream
   * @param manager   drawing commands manager
   * @param type      plot type
   * @param info      plot info
   * @param dos       output data stream
   * @param fullname  output full name
   * @param proj_dir  direction, for projection profile 
   * @param oblique   oblique angle [degrees] for oblique projection profile
   */ 
  public static void exportDataStreamFile( DrawingCommandManager manager, int type, PlotInfo info, DataOutputStream dos, String fullname, int proj_dir, int oblique )
    throws IOException
  {
    // try {
      // FileOutputStream fos = TDFile.getFileOutputStream( file );
      // // ByteArrayOutputStream bos = new ByteArrayOutputStream( 4096 );
      // BufferedOutputStream bfos = new BufferedOutputStream( fos );
      // DataOutputStream dos = new DataOutputStream( bfos );
      manager.exportDataStream( type, dos, info, fullname, proj_dir, oblique );
      // dos.close();

      // CACHE add filename/bos.toByteArray to cache
      // mTdrCache.put( fullname + ".tdr", bos );
      // byte[] bytes = bos.toByteArray();
      // fos.write( bytes, 0, bos.size() );

      // fos.close();
    // } catch ( FileNotFoundException e ) {
    //   TDLog.e( "Export Data file [1]: " + e.getMessage() );
    // } catch ( IOException e ) {
    //   TDLog.e( "Export Data i/o [1]: " + e.getMessage() );
    // }
  }

  // entry point to export a set of paths to data-stream
  public static void exportDataStreamFile( List< DrawingPath > paths, int type, PlotInfo info, DataOutputStream dos, String fullname, int proj_dir, int oblique, int scrap )
    throws IOException
  {
    // try {
      // FileOutputStream fos = TDFile.getFileOutputStream( file );
      // // ByteArrayOutputStream bos = new ByteArrayOutputStream( 4096 );
      // BufferedOutputStream bfos = new BufferedOutputStream( fos );
      // DataOutputStream dos = new DataOutputStream( bfos );

      float xmin=1000000f, xmax=-1000000f, 
            ymin=1000000f, ymax=-1000000f;
      for ( DrawingPath p : paths ) {
        if ( p.left   < xmin ) xmin = p.left;
        if ( p.right  > xmax ) xmax = p.right;
        if ( p.top    < ymin ) ymin = p.top;
        if ( p.bottom > ymax ) ymax = p.bottom;
      }
      RectF bbox = new RectF( xmin, ymin, xmax, ymax );

      // TDLog.v("export data stream: paths " + paths.size() );
      exportDataStream( type, dos, info, fullname, proj_dir, oblique, bbox, paths, scrap );

      // dos.close();
      // fos.close();
    // } catch ( FileNotFoundException e ) {
    //   TDLog.e( "Export Data file [2]: " + e.getMessage() );
    // } catch ( IOException e ) {
    //   TDLog.e( "Export Data i/o [2]: " + e.getMessage() );
    // }
  }

  // tdr files CACHE
  // no need to use a cache: buffering streams is as fast as reading from file
  // and requires less management overhead
  // static HashMap< String, ByteArrayOutputStream > mTdrCache = new HashMap< String, ByteArrayOutputStream >();
  // static void clearTdrCache() { mTdrCache.clear(); }

  // =========================================================================
  // EXPORT details
  // V ... S ... I ... N nr_scraps
  // paths: P. T, L. A. U. X.
  // F
  // stations: U
  // E

  public static int skipTdrHeader( DataInputStream dis )
  {
    int what, type; // , dir;
    int version = 0;
    int flag = 0;
    int kmax = 3; // number of header entries 
    try {
      for ( int k=0; k<kmax; ++k ) {
        what = dis.read(); // 'V'
        if ( what == 'V' ) {
          flag |= 0x01;
          version = dis.readInt();
	  // TDLog.v("TDR header version: " + version );
        } else if ( what == 'S' ) { // scrap
          flag |= 0x02;
          String name = dis.readUTF();
          type = dis.readInt();
          if ( type == PlotType.PLOT_PROJECTED ) {
            /* dir = */ dis.readInt();
            // OBLIQUE if ( version > 602029 ) dis.readInt();
          }
          /* String lib = */ dis.readUTF();
          /* lib = */ dis.readUTF();
          /* lib = */ dis.readUTF();
	  // TDLog.v("TDR header scrap: " + name + " type " + type );
        } else if ( what == 'I' ) {
          flag |= 0x04;
          /* float x1 = */ dis.readFloat();
          /* float y1 = */ dis.readFloat();
          /* float x2 = */ dis.readFloat();
          /* float y2 = */ dis.readFloat();
          if ( dis.readInt() == 1 ) {
	    // TDLog.v("TDR header bbox from " + x1 + " " + y1 + " to " + x2 + " " + y2 + " with north");
            /* x1 = */ dis.readFloat();
            /* y1 = */ dis.readFloat();
            /* x2 = */ dis.readFloat();
            /* y2 = */ dis.readFloat();
          // } else {
	  //   TDLog.v("TDR header bbox from " + x1 + " " + y1 + " to " + x2 + " " + y2 + " no north");
          }
        } else if ( what == 'N' ) { // scrap index
          // int scrap_index = 
            dis.readInt();
          // TDLog.v("TDR scrap index " + scrap_index );
        } else {
          break;
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    } 
    // TDLog.v("TDR header flag " + flag );
    return (flag == 0x07)? version : 0;
  }

  static boolean doLoadDataStream( DrawingSurface surface,
                                   String filename,
                                   float dx, float dy,
                                   // FIXME-MISSING SymbolsPalette missingSymbols,
                                   SymbolsPalette localPalette,
                                   RectF bbox,
				   boolean complete,
				   String plotName )
  {
    // TDLog.v( "load data stream file " + filename + " plot name " + ( (plotName == null)? "null" : plotName ) );
    BrushManager.resetPointOrientations();
    // int project_dir = 0;
    // float north_x1, north_y1, north_x2, north_y2;

    // File file = TDFile.getTopoDroidFile( filename );
    // if ( ! file.exists() ) {
    //   TDLog.e("no file " + filename );
    //   return false;
    // }
    // if ( ! file.canRead() ) {
    //   TDLog.e("cannot read file " + filename );
    //   return false;
    // }

    // FIXME SECTION_RENAME
    int pos = filename.lastIndexOf('/');
    String survey_name; // = null;
    if ( pos >= 0 ) {
      survey_name = filename.substring(pos+1);
    } else {
      survey_name = filename;
    }
    // if ( survey_name != null ) { // always true
      pos = survey_name.indexOf('-');
      if ( pos > 0 ) survey_name = survey_name.substring(0, pos);
    // }
    // TDLog.v( "drawing I/O load stream " + filename );
    try {
      DataInputStream dis = TDFile.getTopoDroidFileInputStream( filename );
      if ( dis == null ) return false;
      return doLoadDataInputStream( surface, dis, dx, dy, survey_name, filename, localPalette, bbox, complete, plotName );
    } catch (IOException e ) {
      // TODO
    }
    return false;
  }

  
  static boolean doLoadDataInputStream( DrawingSurface surface,
                                   DataInputStream dis, 
                                   float dx, float dy,
                                   String survey_name,
                                   String filename,
                                   SymbolsPalette localPalette,
                                   RectF bbox,
				   boolean complete,
				   String plotName )
  {
    synchronized ( TDPath.mTdrLock ) { // FIXME-THREAD_SAFE
      try {
        int version = 0;
        boolean in_scrap = false;
        int scrap_index = 0;
        DrawingPath path; // = null;
        // FileInputStream fis = TDFile.getFileInputStream( filename );
        // BufferedInputStream bfis = new BufferedInputStream( fis );
        // DataInputStream dis = new DataInputStream( bfis );

        boolean todo = true;
        while ( todo ) {
          int what = dis.read();
          // TDLog.v( " read " + what );
          path = null;
          switch ( what ) {
            case 'V':
              version = dis.readInt();
              // TDLog.Log( TDLog.LOG_PLOT, "TDR version " + version );
              break;
            case 'I': // plot info: bounding box
              {
                float xmin = dis.readFloat();
                float ymin = dis.readFloat();
                float xmax = dis.readFloat();
                float ymax = dis.readFloat();
                if ( bbox != null ) {
                  bbox.left   = xmin;
                  bbox.top    = ymin;
                  bbox.right  = xmax;
                  bbox.bottom = ymax;
                }
                if ( dis.readInt() == 1 ) {
                  /* north_x1 = */ dis.readFloat();
                  /* north_y1 = */ dis.readFloat();
                  /* north_x2 = */ dis.readFloat();
                  /* north_y2 = */ dis.readFloat();
                }
                // TDLog.Log(TDLog.LOG_PLOT, "TDR bbox " + xmin + "-" + xmax + " " + ymin + "-" + ymax );
              }
              break;
            case 'S': // SCRAP
              {
                String name = dis.readUTF();
                int type = dis.readInt();
                if ( type == PlotType.PLOT_PROJECTED ) {
                  /* project_dir = */ dis.readInt();
                  // OBLIQUE if ( version > 602029 ) /* oblique */ dis.readInt();
                }
                // read palettes
                String points = dis.readUTF();
                String[] vals = points.split(",");
                for ( String val : vals ) if ( val.length() > 0 ) localPalette.addPointFilename( val );
                String lines = dis.readUTF();
                vals = lines.split(",");
                for ( String val : vals ) if ( val.length() > 0 ) localPalette.addLineFilename( val );
                String areas = dis.readUTF();
                vals = areas.split(",");
                for ( String val : vals ) if ( val.length() > 0 ) localPalette.addAreaFilename( val );
                in_scrap = true;
                // TDLog.v( "TDR type " + type );
              }
              break;
            case 'N':
              scrap_index = dis.readInt();
              break;
            case 'P':
	      // FIXME SECTION_RENAME
              path = DrawingPointPath.loadDataStream( version, dis, dx, dy /*, missingSymbols */ );
              if ( path != null ) path = ((DrawingPointPath)path).fixScrap( survey_name );
              break;
            case 'T':
              path = DrawingLabelPath.loadDataStream( version, dis, dx, dy );
              break;
            case 'Y':
              path = DrawingPhotoPath.loadDataStream( version, dis, dx, dy );
              break;
            case 'Z':
              path = DrawingAudioPath.loadDataStream( version, dis, dx, dy );
              break;
            // case 'W':
            //   path = DrawingSensorPath.loadDataStream( version, dis, dx, dy );
            //   break;
            case 'L':
              path = DrawingLinePath.loadDataStream( version, dis, dx, dy /*, missingSymbols */ );
              // TDLog.v("add path ... " + ((DrawingLinePath)path).mFirst.mX + " " + ((DrawingLinePath)path).mFirst.mY );
              break;
            case 'A':
              path = DrawingAreaPath.loadDataStream( version, dis, dx, dy /*, missingSymbols */ );
              break;
            case 'J':
              path = DrawingSpecialPath.loadDataStream( version, dis, dx, dy );
              break;
            case 'U':
              path = DrawingStationUser.loadDataStream( version, dis ); // consume DrawingStationUser data
              break;
            case 'X':
              path = DrawingStationName.loadDataStream( version, dis ); // consume DrawingStationName data
              break;
            // case 'G':
            //   path = DrawingFixedName.loadDataStream( version, dis ); // consume DrawingFixedName data
            //   break;
            case 'F':
              // TDLog.Log( TDLog.LOG_PLOT, "<F>" );
              if ( complete ) break; // continue parsing stations
            case 'E':
              todo = false;
              break;
            default:
              todo = false;
              if ( filename != null ) {
                TDLog.e( "ERROR " + filename + " bad input (1) " + what );
              } else {
                TDLog.e( "ERROR bad input (1) " + what );
              }
              break;
          } 
          if ( path != null && in_scrap ) {
            if ( path.mScrap == scrap_index ) {
              // if ( plotName != null ) 
	        path.mPlotName = plotName;
              if ( path instanceof DrawingStationUser ) { // what == 'U' - USER
                surface.addDrawingStationUser( (DrawingStationUser)path );
              } else {
                surface.addDrawingPath( path );
                if ( path instanceof DrawingPhotoPath ) { // what == 'Y' - PHOTO
                  DrawingPhotoPath photo = (DrawingPhotoPath)path;
                  DrawingPicturePath picture = photo.getPicture();
                  if ( picture != null ) {
                    picture.mPlotName = plotName;
                    surface.addDrawingPath( picture );
                  }
                }
              }
            } else {
              TDLog.e("Scrap/Path index mismatch " + scrap_index + " " + path.mScrap );
              // TDLog.v("Scrap/Path index mismatch " + scrap_index + " " + path.mScrap );
            }
          }
        }
        dis.close();
        // if ( fis != null ) fis.close();
      } catch ( FileNotFoundException e ) {
        // this is OK
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      // TDLog.v( "read: " + sb.toString() );
    }
    // FIXME-MISSING return (missingSymbols == null ) || missingSymbols.isOK();
    return true;
  }

  /** load the outline of a xection scrap
   * @param surface    drawing surface
   * @param filename   xsection filename
   * @param dx
   * @param dy
   * @param name       xsection name
   * @param scrap_id   index of the scrap of the section point
   */
  static void doLoadOutlineDataStream( DrawingSurface surface,
                                   String filename,
                                   float dx, float dy, String name, int scrap_id )
  {
    int version = 0;
    boolean in_scrap = false;
    // int scrap_index = 0;

    // TDLog.Log( TDLog.LOG_IO, "load outline tdr file " + filename );
    // TLog.v("drawing I/O load outline stream " + filename + " name " + ((name == null)? "null" : name) );
    synchronized( TDPath.mTdrLock ) { // FIXME-THREAD_SAFE
      try {
        // File file = TDFile.getTopoDroidFile( filename );
        // FileInputStream fis = new FileInputStream( file );
        // BufferedInputStream bfis = new BufferedInputStream( fis );
        // DataInputStream dis = new DataInputStream( bfis );
        DataInputStream dis = TDFile.getTopoDroidFileInputStream( filename );
        if ( dis == null ) return;
        boolean todo = true;
        while ( todo ) {
          DrawingLinePath path = null;
          int what = dis.read();
          // TDLog.v( "Read " + what );
          switch ( what ) {
            case 'V':
              version = dis.readInt();
              // TDLog.Log( TDLog.LOG_PLOT, "TDR version " + version );
              break;
            case 'I': // plot info: bounding box
              {
                dis.readFloat();
                dis.readFloat();
                dis.readFloat();
                dis.readFloat();
                if ( dis.readInt() == 1 ) {
                  dis.readFloat();
                  dis.readFloat();
                  dis.readFloat();
                  dis.readFloat();
                }
                // TDLog.Log(TDLog.LOG_PLOT, "TDR bbox " + xmin + "-" + xmax + " " + ymin + "-" + ymax );
              }
              break;
            case 'S': // SCRAP
              {
                dis.readUTF();
                int type = dis.readInt();
                if ( type == PlotType.PLOT_PROJECTED ) {
                  dis.readInt();
                  // OBLIQUE if ( version > 602029 ) dis.readInt();
                }
                // read palettes
                dis.readUTF();
                dis.readUTF();
                dis.readUTF();
                in_scrap = true;
              }
              break;
            case 'N':
              /* scrap_index = */ dis.readInt();
              break;
            case 'P':
              DrawingPointPath.loadDataStream( version, dis, dx, dy /*, null */ );
              break;
            case 'T':
              DrawingLabelPath.loadDataStream( version, dis, dx, dy );
              break;
            case 'L':
              path = DrawingLinePath.loadDataStream( version, dis, dx, dy /*, null */ );
              break;
            case 'A':
              DrawingAreaPath.loadDataStream( version, dis, dx, dy /*, null */ );
              break;
            case 'J':
              DrawingSpecialPath.loadDataStream( version, dis, dx, dy );
              break;
            case 'U':
              DrawingStationUser.loadDataStream( version, dis ); // consume DrawingStationName data
              break;
            case 'X':
              DrawingStationName.loadDataStream( version, dis ); // consume DrawingStationName data
              break;
            case 'Y':
              DrawingPhotoPath.loadDataStream( version, dis, dx, dy );
              break;
            case 'Z':
              DrawingAudioPath.loadDataStream( version, dis, dx, dy );
              break;
            // case 'W':
            //   path = DrawingSensorPath.loadDataStream( version, dis, dx, dy );
            //   break;

            // case 'G':
            //   DrawingFixedName.loadDataStream( version, dis ); // consume DrawingFixedName data
            //   break;
            case 'F':
            case 'E':
            default:
              todo = false;
              // TDLog.e( "ERROR " + filename + " bad input (3) " + (int)what );
              break;
          } 
          if (    in_scrap && path != null 
               && ( BrushManager.isLineWallGroup( path.mLineType ) || path.hasOutline() ) ) {
            // TDLog.v("outline add path ... " + path.mFirst.x + " " + path.mFirst.y + " path size " + path.size()  );
            path.setPathPaint( BrushManager.fixedGrid100Paint );
            if ( name != null ) { // xsection outline
              surface.addXSectionOutlinePath( new DrawingOutlinePath( name, path, scrap_id ) );
            } else {
              surface.addScrapOutlinePath( path );
            }
          }
        }
        dis.close();
        // if ( fis != null ) fis.close();
      } catch ( FileNotFoundException e ) {
        // this is OK
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      // TDLog.v( "read: " + sb.toString() );
    }
  }

  /** change the scrap name into a tdr file
   * @param old_name   existing tdr file (deleted at the end) - full pathname
   * @param new_name   new tdr file (created) - full pathname
   * @param scrap_name new scrap name
   * @return true if successful
   */
  static boolean changeTdrFile( String old_name, String new_name, String scrap_name )
  {
    File old_file = TDFile.getTopoDroidFile( old_name );
    if ( ! old_file.exists() ) return false;
    File new_file = TDFile.getTopoDroidFile( new_name );
    if ( new_file.exists() ) { 
      TDLog.e("tdr file change: " + old_name + " to existing " + new_name );
      return false;
    }
    byte[] buffer = new byte[4096];
    int n;
    try {
      FileInputStream  fis = new FileInputStream(  old_file );
      FileOutputStream fos = new FileOutputStream( new_file );
      DataInputStream  dis = new DataInputStream(  fis );
      DataOutputStream dos = new DataOutputStream( fos );
      int what = dis.read(); // 'V'
      if ( what != 'V' ) return false;
      int version = dis.readInt();
      dos.write( 'V' );
      dos.writeInt( version );
      what = dis.read(); // 'S'
      if ( what != 'S' ) return false;
      dis.readUTF();
      dos.write( 'S' );
      dos.writeUTF( scrap_name );
      while ( ( n = dis.read( buffer ) ) > 0 ) {
        dos.write( buffer, 0, n );
      }
      dos.flush();
      // dos.close(); // does nothing
      // dis.close(); // does nothing
      fis.close();
      fos.close();
      old_file.delete();
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      return false;
    } catch ( IOException e ) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /** change a tdr file: drop a media point
   * @param old_name   existing tdr file (deleted at the end) - full pathname
   * @param media_type media type
   * @param media_id   media ID
   * @return true if successful
   */
  static boolean changeTdrFileDropMedia( String old_name, int media_type, long media_id )
  {
    File old_file = TDFile.getTopoDroidFile( old_name );
    if ( ! old_file.exists() ) return false;
    File new_file = TDFile.getTopoDroidFile( old_name + "_x" ); // FIXME use tmpfile
    if ( new_file.exists() ) { 
      TDLog.e("tdr file " + old_name + " - drop media " + media_type + " id " + media_id );
      return false;
    }
    byte[] buffer = new byte[4096];
    int n, i, j;
    float f, dx=0, dy=0;
    String s;
    DrawingPath path;
    try {
      FileInputStream  fis = new FileInputStream(  old_file );
      DataInputStream  dis = new DataInputStream(  fis );
      int what = dis.read();
      if ( what != 'V' ) {
        TDLog.e("ERROR missing version code ");
        fis.close();
        return false;
      }
      FileOutputStream fos = new FileOutputStream( new_file );
      DataOutputStream dos = new DataOutputStream( fos );
      dos.write( 'V' );
      int version = dis.readInt(); dos.writeInt( version );
      boolean todo = true;
      while ( todo ) {
        what = dis.read();
        // TDLog.v( "Read " + what );
        switch ( what ) {
          case 'I': // plot info: bounding box
          {
            dos.write( 'I' );
            f = dis.readFloat(); dos.writeFloat( f );
            f = dis.readFloat(); dos.writeFloat( f );
            f = dis.readFloat(); dos.writeFloat( f );
            f = dis.readFloat(); dos.writeFloat( f );
            i = dis.readInt();   dos.writeInt( i );
            if ( i == 1 ) {
              f = dis.readFloat(); dos.writeFloat( f );
              f = dis.readFloat(); dos.writeFloat( f );
              f = dis.readFloat(); dos.writeFloat( f );
              f = dis.readFloat(); dos.writeFloat( f );
            }
                // TDLog.Log(TDLog.LOG_PLOT, "TDR bbox " + xmin + "-" + xmax + " " + ymin + "-" + ymax );
          }
          break;
          case 'S': // SCRAP
            {
              dos.write( 'S' );
              s = dis.readUTF(); dos.writeUTF( s );
              i = dis.readInt(); dos.writeInt( i );
              if ( i == PlotType.PLOT_PROJECTED ) {
                j = dis.readInt(); dos.writeInt( j );
                // OBLIQUE if ( version > 602029 ) dis.readInt();
              }
              // read palettes
              s = dis.readUTF(); dos.writeUTF( s );
              s = dis.readUTF(); dos.writeUTF( s );
              s = dis.readUTF(); dos.writeUTF( s );
              // in_scrap = true; // not used
            }
            break;
          case 'N':
            dos.write( 'N' );
            /* scrap_index = */ i = dis.readInt(); dos.writeInt( i );
            break;
          case 'P':
            DrawingPointPath point = DrawingPointPath.loadDataStream( version, dis, dx, dy /*, null */ );
            point.toDataStream( dos, -1 ); // -1: use point scrap
            break;
          case 'T':
            DrawingLabelPath label = DrawingLabelPath.loadDataStream( version, dis, dx, dy );
            label.toDataStream( dos, -1 ); // -1: use label scrap
            break;
          case 'L':
            DrawingLinePath line = DrawingLinePath.loadDataStream( version, dis, dx, dy /*, null */ );
            line.toDataStream( dos, -1 ); // -1:  use line scrap
            break;
          case 'A':
            DrawingAreaPath area = DrawingAreaPath.loadDataStream( version, dis, dx, dy /*, null */ );
            area.toDataStream( dos, -1 ); // -1: use area scrap
            break;
          case 'J':
            DrawingSpecialPath special = DrawingSpecialPath.loadDataStream( version, dis, dx, dy );
            special.toDataStream( dos, -1 ); // -1: use special mScrap
            break;
          case 'U':
            DrawingStationUser user = DrawingStationUser.loadDataStream( version, dis ); // consume DrawingStationName data
            user.toDataStream( dos, -1 ); // -1L use station-user scrap
            break;
          case 'X':
            DrawingStationName name = DrawingStationName.loadDataStream( version, dis ); // consume DrawingStationName data
            name.toDataStream( dos, -1 ); // -1: use station-name scrap
            break;
          case 'Y':
            DrawingPhotoPath photo = DrawingPhotoPath.loadDataStream( version, dis, dx, dy );
            if ( media_type == MediaInfo.MEDIA_PHOTO ) {
              if ( media_id == photo.mId ) {
                todo = false;
              } else {
                photo.toDataStream( dos, -1 ); // -1: use photo mScra
              }
            }
            break;
          case 'Z':
            DrawingAudioPath audio = DrawingAudioPath.loadDataStream( version, dis, dx, dy );
            if ( media_type == MediaInfo.MEDIA_AUDIO ) {
              if ( media_id == audio.mId ) {
                todo = false;
              } else {
                audio.toDataStream( dos, -1 ); // -1: use audio mScrap
              }
            }
            break;
          // case 'W':
          //   DrawingSensorPath sensor = DrawingSensorPath.loadDataStream( version, dis, dx, dy );
          //   if ( media_type == MediaInfo.MEDIA_SENSOR ) {
          //     if ( media_id == sensor.mId ) {
          //       todo = false;
          //     } else {
          //       sensor.toDataStream( dos, -1 ); -1: use sensor mScrap
          //     }
          //   }
          //   break;

          // case 'G':
          //   DrawingFixedName.loadDataStream( version, dis ); // consume DrawingFixedName data
          //   break;
          case 'F':
          case 'E':
          default:
            todo = false;
            // TDLog.e( "ERROR " + filename + " bad input (3) " + (int)what );
            break;
        } 
      }
      while ( ( n = dis.read( buffer ) ) > 0 ) {
        dos.write( buffer, 0, n );
      }
      dos.flush();
      // dos.close(); // does nothing
      // dis.close(); // does nothing
      fis.close();
      fos.close();
      if ( ! new_file.renameTo( old_file ) ) {
        TDLog.e("ERROR file rename " + old_name );
        return false;
      }
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
      return false;
    } catch ( IOException e ) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /** used by ParserPocketTopo
   * @param type    plot type
   * @param dos     output data stream
   * @param info    plot info
   * @param scrap_name output fullname
   * @param proj_dir ...
   * @param oblique  ...
   * @param scrap   scrap index
   */
  public static void exportDataStream( int type, DataOutputStream dos, PlotInfo info, String scrap_name, int proj_dir, int oblique,
                                RectF bbox, List< DrawingPath > paths, int scrap )
  {
    try { 
      dos.write( 'V' ); // version
      dos.writeInt( TDVersion.code() );
      dos.write( 'S' ); // scrap
      dos.writeUTF( scrap_name );
      dos.writeInt( type );
      if ( type == PlotType.PLOT_PROJECTED ) {
        dos.writeInt( proj_dir );
        // OBLIQUE dos.writeInt( oblique );
      }
      BrushManager.toDataStream( dos );

      dos.write('I');
      dos.writeFloat( bbox.left );
      dos.writeFloat( bbox.top );
      dos.writeFloat( bbox.right );
      dos.writeFloat( bbox.bottom );
      dos.writeInt( 0 ); // null north

      dos.write('N'); // scrap index
      dos.writeInt( 0 );

      for ( DrawingPath p : paths ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) continue; // safety check: should not happen
        p.toDataStream( dos, scrap );
      }
      // synchronized( TDPath.mStationsLock ) { // user stations are always exported to data stream
      //   for ( DrawingStationUser sp : userstations ) {
      //     sp.toDataStream( dos, scrap );
      //   }
      // }
      dos.write('F'); // final: bbox and autostations (reading can skip all that follows)

      // if ( info != null ) {
      //   dos.write('D');
      //   dos.writeFloat( info.xoffset );
      //   dos.writeFloat( info.yoffset );
      //   dos.writeFloat( info.azimuth );
      //   dos.writeFloat( info.clino );
      //   dos.writeFloat( info.intercept );
      //   dos.writeUTF( info.start );
      //   if ( PlotType.isSection( type ) ) {
      //     dos.writeUTF( (info.view != null)? info.view : "" );
      //     dos.writeUTF( (info.hide != null)? info.hide : "" );
      //     dos.writeUTF( (info.nick != null)? info.nick : "" );
      //   } else {
      //     dos.writeUTF( "" ); // view = barrier
      //     dos.writeUTF( "" ); // hide = hiding
      //     dos.writeUTF( "" ); // nick unset
      //   }
      // }
      // if ( TDSetting.mAutoStations && stations != null ) {
      //   synchronized( TDPath.mStationsLock ) {
      //     for ( DrawingStationName st : stations ) {
      //       NumStation station = st.getNumStation();
      //       if ( station != null && station.barriered() ) continue;
      //       if ( bbox.left > st.cx || bbox.right  < st.cx ) continue;
      //       if ( bbox.top  > st.cy || bbox.bottom < st.cy ) continue;
      //       st.toDataStream( dos, -1 );
      //     }
      //   }
      // }
      // if ( fixeds != null ) {
      //   synchronized( TDPath.mFixedsLock ) {
      //     for ( DrawingFixedName fx : fixeds ) {
      //       if ( bbox.left > fx.cx || bbox.right  < fx.cx ) continue;
      //       if ( bbox.top  > fx.cy || bbox.bottom < fx.cy ) continue;
      //       fx.toDataStream( dos, -1 );
      //     }
      //   }
      // }

      dos.write('E'); // end
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  // this is called by DrawingCommandManager
  public static void exportDataStream(
      int type,
      DataOutputStream dos,
      PlotInfo info,
      String scrap_name,
      int proj_dir,
      int oblique,
      RectF bbox,
      DrawingPath north,
      // final List< ICanvasCommand > cstack,
      // final List< DrawingStationUser > userstations,
      final List< Scrap > scraps,
      final List< DrawingStationName > stations // , final List< DrawingFixedName > fixeds
  )
  {
    // TDLog.v( "cstack size " + cstack.size() );
    try { 
      dos.write( 'V' ); // version
      dos.writeInt( TDVersion.code() );
      dos.write( 'S' ); // scrap
      dos.writeUTF( scrap_name );
      dos.writeInt( type );
      if ( type == PlotType.PLOT_PROJECTED ) {
        dos.writeInt( proj_dir );
        // OBLIQUE dos.writeInt( oblique );
      }
      BrushManager.toDataStream( dos );

      dos.write('I');
      dos.writeFloat( bbox.left );
      dos.writeFloat( bbox.top );
      dos.writeFloat( bbox.right );
      dos.writeFloat( bbox.bottom );
      if ( north != null ) {
        dos.writeInt( 1 );
        dos.writeFloat( north.x1 );
        dos.writeFloat( north.y1 );
        dos.writeFloat( north.x2 );
        dos.writeFloat( north.y2 );
      } else {
        dos.writeInt( 0 );
      }

      synchronized( scraps ) {
        for ( Scrap scrap : scraps ) {
          dos.write('N');  // scrap index
          dos.writeInt( scrap.mScrapIdx );

          List< ICanvasCommand > cstack = scrap.mCurrentStack;
          // TDLog.v("export scrap " + scrap.mScrapIdx + " items " + cstack.size() );
          synchronized( TDPath.mCommandsLock ) {
            for ( ICanvasCommand cmd : cstack ) {
              if ( cmd.commandType() != 0 ) continue;
              DrawingPath p = (DrawingPath) cmd;
              if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) continue; // safety check: should not happen
              p.toDataStream( dos, -1 ); // -1: use path mScrap
            }
          }
          List< DrawingStationUser > userstations = scrap.mUserStations;
          synchronized( TDPath.mStationsLock ) { // user stations are always exported to data stream
            for ( DrawingStationUser sp : userstations ) {
              sp.toDataStream( dos, -1 );
            }
          }
        }
      }
      dos.write('F'); // final: bbox and autostations (reading can skip all that follows)

      if ( info != null ) {
        dos.write('D');
        dos.writeFloat( info.xoffset );
        dos.writeFloat( info.yoffset );
        dos.writeFloat( info.azimuth );
        dos.writeFloat( info.clino );
        dos.writeFloat( info.intercept );
        dos.writeUTF( (info.start != null)? info.start : "" );
        dos.writeUTF( (info.view != null)? info.view : "" );
        dos.writeUTF( (info.hide != null)? info.hide : "" );
        dos.writeUTF( (info.nick != null)? info.nick : "" );
      }

      if ( TDSetting.mAutoStations && stations != null ) {
        synchronized( TDPath.mStationsLock ) {
          for ( DrawingStationName st : stations ) {
            NumStation station = st.getNumStation();
            if ( station != null && station.barriered() ) continue;
            if ( bbox.left > st.cx || bbox.right  < st.cx ) continue;
            if ( bbox.top  > st.cy || bbox.bottom < st.cy ) continue;
            st.toDataStream( dos, -1 );
          }
        }
      }
      // if ( fixeds != null ) {
      //   synchronized( TDPath.mFixedsLock ) {
      //     for ( DrawingFixedName fx : fixeds ) {
      //       if ( bbox.left > fx.cx || bbox.right  < fx.cx ) continue;
      //       if ( bbox.top  > fx.cy || bbox.bottom < fx.cy ) continue;
      //       fx.toDataStream( dos, -1 );
      //     }
      //   }
      // }
      dos.write('E'); // end
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  /** write therion file global header
   * @param out      output writer
   * @param type     [unused]
   * @param bbox     clipping rectangle
   * @param name     filename without extension .th2
   */
  static private void exportTherionGlobalHeader( BufferedWriter out, int type, RectF bbox, String name ) throws IOException
  {
    // TDLog.v("TH2 bbox " + bbox.left + " " + bbox.top + " - " + bbox.right + " " + bbox.bottom + " ToTherion " + TDSetting.mToTherion );
    float scale = TDSetting.mToTherion;
    out.write("encoding utf-8");
    out.newLine();
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.US, "##XTHERION## xth_me_area_adjust %.1f %.1f %.1f %.1f\n",
      bbox.left*scale-100, -bbox.bottom*scale-100, bbox.right*scale+100, -bbox.top*scale+100 );
    pw.format("##XTHERION## xth_me_area_zoom_to 25\n");
    if ( TDSetting.mTherionXvi ) {
      float xoff = 0.0f;
      float yoff = 0.0f;
      XviBBox bb = new XviBBox( bbox );
      // xx vsb gamma - yy XVIroot -- filename -- index -- data
      pw.format(Locale.US, "##XTHERION## xth_me_image_insert {%.2f 1 1.0} {%.2f 0} %s.xvi 0 {}\n",
        scale*(xoff+bb.xmin), scale*(yoff-bb.ymax), name ); // this is fine is sketch origin is first station
    }
    pw.format("\n");
    pw.format("# %s created by TopoDroid v. %s\n\n", TDUtil.currentDate(), TDVersion.string() );
    out.write( sw.getBuffer().toString() );
  }

  // static private void exportTherionHeader2( BufferedWriter out ) throws IOException
  // {
  //   StringWriter sw = new StringWriter();
  //   PrintWriter pw  = new PrintWriter(sw);
  //   pw.format("#P ");
  //   BrushManager.mPointLib.writePalette( pw );
  //   pw.format("\n#L ");
  //   BrushManager.mLineLib.writePalette( pw );
  //   pw.format("\n#A ");
  //   BrushManager.mAreaLib.writePalette( pw );
  //   pw.format("\n");
  //   out.write( sw.getBuffer().toString() );
  // }

  // static private void exportTherionHeader2( BufferedWriter out, String points, String lines, String areas ) throws IOException
  // {
  //   StringWriter sw = new StringWriter();
  //   PrintWriter pw  = new PrintWriter(sw);
  //   pw.format("#P");
  //   String[] vals = points.split(",");
  //   for ( String v : vals ) if ( v.length() > 0 ) pw.format(" %s", v );
  //   pw.format("\n#L");
  //   vals = lines.split(",");
  //   for ( String v : vals ) if ( v.length() > 0 ) pw.format(" %s", v );
  //   pw.format("\n#A");
  //   vals = lines.split(",");
  //   for ( String v : vals ) if ( v.length() > 0 ) pw.format(" %s", v );
  //   pw.format("\n");
  //   out.write( sw.getBuffer().toString() );
  // }
  
  /** start scrap export (write header
   * @param out          output writer
   * @param type         plot type
   * @param scrap_name   scrap name
   * @param proj_name    therion projection type
   * @param project_dir  projected profile azimuth (if applicable) - x-section ?
   * @param do_north     ???
   * @param x1           x-section first scale-point: X
   * @param y1           x-section first scale-point: Y
   * @param x2           x-section second scale-point: X
   * @param y2           x-section second scale-point: Y
   * @param scrap_options scrap therion options
   */
  static private void exportTherionScrapHeader( BufferedWriter out,
         int type, String scrap_name, String proj_name, int project_dir, int oblique,
         boolean do_north, float x1, float y1, float x2, float y2, String scrap_options ) throws IOException // TH2EDIT no scrap_options
  {
    float oneMeter  = DrawingUtil.SCALE_FIX * TDSetting.mToTherion;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( scrap_options != null ) { // TH2EDIT
        pw.format(Locale.US, "scrap %s", scrap_options );
    } else {
      if (    type == PlotType.PLOT_SECTION
           || type == PlotType.PLOT_H_SECTION 
           || type == PlotType.PLOT_X_SECTION ) {
        if ( do_north ) { // H_SECTION (horizontal section) : north line is 5 m long
          pw.format(Locale.US, "scrap %s -projection %s -scale [%.4f %.4f %.4f %.4f 0 5 0 0 m]", scrap_name, proj_name, 
            x1*TDSetting.mToTherion, -y1*TDSetting.mToTherion, x2*TDSetting.mToTherion, -y2*TDSetting.mToTherion );
        } else {
          pw.format(Locale.US, "scrap %s -projection %s -scale [0 0 %.4f 0 0 0 1 0 m]", scrap_name, proj_name, oneMeter );
        }
      } else if ( type == PlotType.PLOT_PROJECTED ) {
        pw.format(Locale.US, "scrap %s -projection [%s %d] -scale [0 0 %.4f 0 0 0 1 0 m]", scrap_name, proj_name, project_dir, oneMeter );
      } else {
        pw.format(Locale.US, "scrap %s -projection %s -scale [0 0 %.4f 0 0 0 1 0 m]", scrap_name, proj_name, oneMeter );
      }
    }
    out.write( sw.getBuffer().toString() );
    out.newLine();
    out.newLine();
  }

  /** finish scrap export
   * @param out     output writer
   */
  static private void exportTherionScrapEnd( BufferedWriter out ) throws IOException
  {
    // TDLog.v("THERION export endscrap");
    out.newLine();
    out.newLine();
    out.write("endscrap");
    out.newLine();
  }

  /** write splays to the export output
   * @param out     output writer
   * @param splays  list of splays
   * @param bbox    clipping rectangle
   */
  static private void exportTherionSplays( BufferedWriter out, List< DrawingSplayPath > splays, RectF bbox ) throws IOException
  {
    if ( splays == null ) return;
    float toTherion = TDSetting.mToTherion;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    synchronized( TDPath.mShotsLock ) {
      for ( DrawingSplayPath splay : splays ) {
        // if ( bbox.left > splay.right  || bbox.right  < splay.left ) continue;
        // if ( bbox.top  > splay.bottom || bbox.bottom < splay.top  ) continue;
        if ( bbox == null || splay.intersects( bbox ) ) {
          pw.format("line u:splay -visibility off\n"); // THERION-U
          pw.format( Locale.US, "  %.2f %.2f\n  %.2f %.2f\n", splay.x1*toTherion, -splay.y1*toTherion, splay.x2*toTherion, -splay.y2*toTherion );
          pw.format("endline\n");
        }
      }
    }
    out.write( sw.toString() );
    out.newLine();
  }

  /** export an area item to therion file
   * @param out   output writer
   * @param ap    area item
   */
  static private void exportTherionArea( BufferedWriter out, DrawingAreaPath ap ) throws IOException
  {
    if ( ap == null ) return;
    if ( ap.size() > 2 ) {
      String ap_str = ap.toTherion();
      if ( ap_str != null ) {
        out.write( ap_str );
        out.newLine();
      }
    }
  }

  /** export a line item to therion file
   * @param out   output writer
   * @param lp    line item
   */
  static private void exportTherionLine( BufferedWriter out, DrawingLinePath lp ) throws IOException
  {
    if ( lp == null ) return;
    if ( lp.size() > 1 ) {
      String lp_str = lp.toTherion();
      if ( lp_str != null ) {
        out.write( lp_str );
        out.newLine();
      }
    }
  }

  /** export a point item to therion file
   * @param out   output writer
   * @param pp     point item
   */
  static private void exportTherionPoint( BufferedWriter out, DrawingPointPath pp ) throws IOException
  {
    if ( pp == null ) return;
    String pp_str = pp.toTherion();
    if ( pp_str != null ) {
      out.write( pp_str );
      out.newLine();
    }
  }

  /** export a set of station names to therion file
   * @param out       output writer
   * @param stations  set of station names
   * @param bbox      clipping rectangle
   */
  static private void exportTherionStations( BufferedWriter out, List< DrawingStationName > stations, RectF bbox ) throws IOException
  {
    if ( stations == null ) return;
    synchronized( TDPath.mStationsLock ) {
      for ( DrawingStationName st : stations ) {
        NumStation station = st.getNumStation();
        if ( station != null && station.barriered() ) continue;
        // FIXME if station is in the convex hull (bbox) of the lines
        if ( bbox != null ) {
          if ( bbox.left > st.cx || bbox.right  < st.cx ) continue;
          if ( bbox.top  > st.cy || bbox.bottom < st.cy ) continue;
        }
        String st_str = st.toTherion();
        if ( st_str != null ) {
          out.write( st_str );
          out.newLine();
        }
      }
    }
  }

  /** export a set of user-station names to therion file
   * @param out       output writer
   * @param userstations  set of user-station names
   */
  static private void exportTherionUserStations( BufferedWriter out, List< DrawingStationUser > userstations ) throws IOException
  {
    if ( userstations == null ) return;
    synchronized( TDPath.mStationsLock ) {
      for ( DrawingStationUser sp : userstations ) {
        String sp_str = sp.toTherion();
        if ( sp_str != null ) {
          out.write( sp_str );
          out.newLine();
        }
      }
    }
  }

  /** export drawing in Therion th2 format
   * @note DataHelper and SID are necessary to export splays by the station
   *
   * @param type        sketch type
   * @param out         output writer
   * @param full_name   name of the scrap (= file_name without extension)
   * @param proj_name   name of the projection
   * @param project_dir projected profile direction
   * @param bbox        bounding box
   * @param north       north-arrow
   * @param scraps      number of scraps
   * @param stations    auto stations
   * @param splays      splays lines
   */
  static void exportTherion( int type, BufferedWriter out, String full_name, String proj_name, int project_dir, int oblique,
        RectF bbox,
        DrawingPath north,
        List< Scrap > scraps,
        List< DrawingStationName > stations,
        List< DrawingSplayPath > splays,
        boolean th2_edit ) // TH2EDIT
  {
    ArrayList< XSectionScrap> xsections = new ArrayList<>();
    // int scraps,

    try { 
      exportTherionGlobalHeader( out, type, bbox, full_name ); 
      // exportTherionHeader2( out );
      int scrap_nr = 0;
      synchronized( scraps ) {
        for ( Scrap scrap : scraps ) {
          String name = (scrap_nr == 0)? full_name : full_name + scrap_nr;
          if ( north != null ) { 
            exportTherionScrapHeader( out, type, name, proj_name, 0, 0, true, north.x1, north.y1, north.x2, north.y2, scrap.mScrapOptions ); // TH2EDIT
          } else {
            exportTherionScrapHeader( out, type, name, proj_name, project_dir, oblique, false, 0, 0, 0, 0, scrap.mScrapOptions ); // TH2EDIT
          }

          RectF scrap_bbox = scrap.getBBox(); // IMPORTANT BBox must have been properly computed before

          List< ICanvasCommand > cstack = scrap.mCurrentStack;
          synchronized( TDPath.mCommandsLock ) {
            for ( ICanvasCommand cmd : cstack ) {
              if ( cmd.commandType() != 0 ) continue;
              DrawingPath p = (DrawingPath) cmd;

              if ( p instanceof DrawingPointPath ) { // ( p.mType == DrawingPath.DRAWING_PATH_POINT )
                DrawingPointPath pp = (DrawingPointPath)p;
                exportTherionPoint( out, pp );
                // APP_OUT_DIR this if was commented !??
                if ( TDSetting.mAutoExportPlotFormat != TDConst.SURVEY_FORMAT_TH2 ) { // if auto-export is not Therion, ie xsections are not already exported therion
                  if ( BrushManager.isPointSection( pp.mPointType ) ) {
                    if ( TDSetting.mAutoXSections ) {
                      String scrapname = TDUtil.replacePrefix( TDInstance.survey, pp.getOption( TDString.OPTION_SCRAP ) ); // x-section name
                      // TDLog.v( "multisketch add x-section " + scrapname );
                      if ( scrapname != null && scrapname.length() > 0 ) xsections.add( new XSectionScrap( scrapname, pp.cx, pp.cy ) );
                    }
                  }
                }
              } else if ( p instanceof DrawingLinePath ) { // ( p.mType == DrawingPath.DRAWING_PATH_LINE ) 
                exportTherionLine( out, (DrawingLinePath)p );
              } else if ( p instanceof DrawingAreaPath ) { // ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
                exportTherionArea( out, (DrawingAreaPath)p );
              } else if ( p instanceof DrawingStationUser ) { // ( p.mType == DrawingPath.DRAWING_PATH_STATION ) // should never happen
                // if ( ! TDSetting.mAutoStations ) {
                //   DrawingStationUser st = (DrawingStationUser)p;
                //   String st_str = st.toTherion();
                //   if ( st_str != null ) {
                //     out.write( st_str );
                //     out.newLine();
                //   }
                // }
              }
            }
          }
          out.newLine();
          out.flush();

          if ( ! th2_edit ) {
            if ( TDSetting.mTherionSplays ) { // TH2EDIT - FIXME this should be conditioned on mAutoStations
              if ( scrap_nr == 0 || TDSetting.mTherionSplaysAll ) { // (splays always in the first scrap)
                exportTherionSplays( out, splays, scrap_bbox );
              }
            }
            if ( TDSetting.mAutoStations ) { // TH2EDIT 
              exportTherionStations( out, stations, scrap_bbox );
            }
          } else {
            List< DrawingStationUser > userstations = scrap.mUserStations;
            if ( userstations.size() > 0 ) {
              exportTherionUserStations( out, userstations );
            }
          }

          exportTherionScrapEnd( out );
          out.flush();
          ++ scrap_nr;
          out.newLine();
          out.flush();
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    // TH2EDIT skip this section
    if ( ! th2_edit ) {
      // APP_OUT_DIR this if was commented !?
      if ( TDSetting.mAutoExportPlotFormat != TDConst.SURVEY_FORMAT_TH2 ) { // xsections if not already auto exported
        for ( XSectionScrap xsection : xsections ) { // write xsection scraps
          // File file = TDFile.getTopoDroidFile( TDPath.getTdrFileWithExt( xsection.name ) );
          try { // 20230213
            DataInputStream dis = TDFile.getTopoDroidFileInputStream( TDPath.getTdrFileWithExt( xsection.name ) );
            if ( dis != null ) {
              dataStreamToTherion(dis, out, null, null, false, true, xsection.x, xsection.y, xsection.name );
              dis.close();
            }
          } catch ( IOException e ) { 
            e.printStackTrace();
          }
        }
      }
    }
  }

  // FIXME DataHelper and SID are necessary to export splays by the station
  // This is therion export from overview window
  // @param type         sketch type
  // @param out          output writer
  // @param full_name    filename without extension
  // @param proj_name    
  // @param project_dir  projected profile direction
  static void exportTherionMultiPlots( int type, BufferedWriter out, String full_name, String proj_name, int project_dir, int oblique,
        // RectF bbox,
        // DrawingPath north, // no x-section
        // List< ICanvasCommand > cstack,
        // List< DrawingStationUser > userstations,
        List< Scrap > scraps,
        List< DrawingStationName > stations,
        List< DrawingSplayPath > splays )
  {
    // TDLog.v( "export multisketch type " + type + " proj " + proj_name );
    class PlotExport
    {
      final String name;
      final RectF  bbox;

      PlotExport( String n, RectF r ) { name = n; bbox = r; }
    }


    // ArraySet<String> plots = new ArraySet<String>(); // REQUIRES API-23
    int NPLOTS = 8;
    int nplots = 0;
    PlotExport[] plots = new PlotExport[NPLOTS];

    ArrayList< XSectionScrap> xsections = new ArrayList<>();

    float xmin=1000000f, xmax=-1000000f, 
          ymin=1000000f, ymax=-1000000f;
    synchronized( scraps ) {
      for ( Scrap scrap : scraps ) {
        List< ICanvasCommand > cstack = scrap.mCurrentStack;
        synchronized( TDPath.mCommandsLock ) {
          for ( ICanvasCommand cmd : cstack ) {
            if ( cmd.commandType() != 0 ) continue;
            DrawingPath p = (DrawingPath) cmd;
            // RectF bbox = p.mBBox;
            if ( p.left   < xmin ) xmin = p.left;
            if ( p.right  > xmax ) xmax = p.right;
            if ( p.top    < ymin ) ymin = p.top;
            if ( p.bottom > ymax ) ymax = p.bottom;


            if ( /* scrap.mPlotName == null && */ p.mPlotName != null ) { // plot_name is the fullname of the plot
              String plot_name = p.mPlotName + p.mScrap;
              // TDLog.v( "path with plot-name " + p.mPlotName + " scrap " + p.mScrap + " type " + p.mType + " nr plots " + nplots );
              int k=0;
              for ( ; k<nplots; ++k ) if ( plots[k].name.equals( plot_name ) ) {
                RectF bb = plots[k].bbox;
                if ( p.left   < bb.left   ) bb.left   = p.left;
                if ( p.right  > bb.right  ) bb.right  = p.right;
                if ( p.top    < bb.top    ) bb.top    = p.top;
                if ( p.bottom > bb.bottom ) bb.bottom = p.bottom;
                break;
              }
              if ( k == nplots ) {
                if ( nplots == NPLOTS ) {
                  NPLOTS += 8;
                  PlotExport[] tmp = new PlotExport[NPLOTS];
                  // for ( int j=0; j<nplots; ++j ) tmp[j] = plots[j];
                  System.arraycopy( plots, 0, tmp, 0, nplots );
                  plots = tmp;
                }
                plots[k] = new PlotExport( plot_name, new RectF( p.left, p.top, p.right, p.bottom ) );
                nplots ++;
              }
            } else {
              TDLog.e("path with no plot-name, type " + p.mType );
            }
          }
        }
      }
    }
    // RectF bbox = new RectF( xmin, ymin, xmax, ymax ); // left top right bottom

    // TDLog.v( "export th2 multisketch nr. " + nplots + "/" + plots.length + " scraps " + scraps.size() );
    // TDLog.v( "export th2 multisketch bbox X " + xmin + " " + xmax  + " Y " + ymin + " " + ymax );

    try { 
      exportTherionGlobalHeader( out, type, new RectF( xmin, ymin, xmax, ymax ), full_name );

      // exportTherionHeader2( out );
      for ( int k=0; k<nplots; ++k ) {
        String plot_name = plots[k].name;
        RectF bbox = plots[k].bbox;
        // TDLog.v( "exporting plot " + plot_name );
        // if ( north != null ) { 
        //   exportTherionScrapHeader( out, type, full_name, proj_name, 0, true, north.x1, north.y1, north.x2, north.y2, null ); // TH2EDIT no null last param
        // } else {
        //   exportTherionScrapHeader( out, type, plot, proj_name, project_dir, oblique, false, 0, 0, 0, 0, null ); // TH2EDIT no null last param
        // }

        exportTherionScrapHeader( out, type, plot_name, proj_name, project_dir, oblique, false, 0, 0, 0, 0, null ); // TH2EDIT no null last param
        // all the scraps of the plot together
        // RectF scraps_bbox = null;
        synchronized( scraps ) {
          for ( Scrap scrap : scraps ) {
            // if ( scraps_bbox == null ) {
            //   scraps_bbox = scrap.getBBox();
            // } else {
            //   Scrap.union( scraps_bbox, scrap.getBBox() );
            // }

            List< ICanvasCommand > cstack = scrap.mCurrentStack;
            synchronized( TDPath.mCommandsLock ) {
              for ( ICanvasCommand cmd : cstack ) {
                if ( cmd.commandType() != 0 ) continue;
                DrawingPath p = (DrawingPath) cmd;
                if ( ! plot_name.equals( p.mPlotName + p.mScrap ) ) continue;
                if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
                  DrawingPointPath pp = (DrawingPointPath)p;
                  exportTherionPoint( out, pp );
                  if ( BrushManager.isPointSection( pp.mPointType ) ) {
                    if ( TDSetting.mAutoXSections ) {
                      String scrapname = TDUtil.replacePrefix( TDInstance.survey, pp.getOption( TDString.OPTION_SCRAP ) ); // x-section name
                      // TDLog.v( "multisketch add x-section " + scrapname );
                      if ( scrapname != null && scrapname.length() > 0 ) xsections.add( new XSectionScrap( scrapname, pp.cx, pp.cy ) );
                    }
                  }
                } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) { // should never happen
                  // if ( ! TDSetting.mAutoStations ) {
                  //   DrawingStationUser st = (DrawingStationUser)p;
                  //   String st_str = st.toTherion();
                  //   if ( st_str != null ) {
                  //     out.write( st_str );
                  //     out.newLine();
                  //   }
                  // }
                } else if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
                  exportTherionLine( out, (DrawingLinePath)p );
                } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
                  exportTherionArea( out, (DrawingAreaPath)p );
                }
              }
              out.newLine();
            }
            if ( ! TDSetting.mAutoStations ) {
              List< DrawingStationUser > userstations = scrap.mUserStations;
              if ( userstations.size() > 0 ) {
                exportTherionUserStations( out, userstations );
              }
            }
          }
        }
        if ( TDSetting.mTherionSplays ) { // FIXME this should be conditioned on mAutoStations
          exportTherionSplays( out, splays, bbox );
        }
        if ( TDSetting.mAutoStations ) {
          exportTherionStations( out, stations, bbox );
        }
        exportTherionScrapEnd( out );
        out.flush();
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    // TDLog.v( "multisketch sections " + xsections.size() );
    for ( XSectionScrap xsection : xsections ) { // write xsection scraps
      // File file = TDFile.getTopoDroidFile( TDPath.getTdrFileWithExt( xsection.name ) );
      try { // 20230213
        DataInputStream dis = TDFile.getTopoDroidFileInputStream( TDPath.getTdrFileWithExt( xsection.name ) );
        if ( dis != null ) {
          dataStreamToTherion( dis, out, null, null, false, true, xsection.x, xsection.y, xsection.name );
        }
        dis.close();
      } catch ( IOException e ) { 
        e.printStackTrace();
      }
    }
  }

  // return maximum scrap index, or 0
  // static int dataStreamScrapNumber( File file )
  // { 
  //   int scrap_index = 0;
  //   try {
  //     FileInputStream fis = TDFile.getFileInputStream( file );
  //     DataInputStream dis = new DataInputStream( fis );
  //     boolean todo = true;
  //     int version = 0;
  //     while ( todo ) {
  //       int what = dis.read();
  //       switch ( what ) {
  //         case 'V':
  //           version = dis.readInt();
  //           break;
  //         case 'I': // plot info: bounding box
  //           {
  //             for ( int k=0; k<4; ++k)  dis.readFloat();
  //             if ( dis.readInt() == 1 ) {
  //               for ( int k=0; k<4; ++k)  dis.readFloat();
  //             }
  //           }
  //           break;
  //         case 'S': // scrap
  //           {
  //             String name = dis.readUTF();
  //             int type = dis.readInt();
  //             if ( type == PlotType.PLOT_PROJECTED ) {
  //               dis.readInt();
  //               // OBLIQUE if ( version > 602029 ) dis.readInt();
  //             }
  //             // read palettes
  //             dis.readUTF();
  //             dis.readUTF();
  //             dis.readUTF();
  //           }
  //           break;
  //         case 'N':
  //           int idx = dis.readInt();
  //           // TDLog.v("EXP scrap index " + idx );
  //           if ( idx > scrap_index ) scrap_index = idx;
  //           break;
  //         case 'P':
  //           DrawingPointPath.globDataStream( version, dis );
  //           break;
  //         case 'T':
  //           DrawingLabelPath.globDataStream( version, dis );
  //           break;
  //         case 'L':
  //           DrawingLinePath.globDataStream( version, dis );
  //           break;
  //         case 'A':
  //           DrawingAreaPath.globDataStream( version, dis );
  //           break;
  //         case 'J':
  //           DrawingSpecialPath.globDataStream( version, dis );
  //           break;
  //         case 'U':
  //           DrawingStationUser.globDataStream( version, dis );
  //           break;
  //         case 'X':
  //           // NOTE need to check XSection ??? STATION_XSECTION
  //           DrawingStationName.globDataStream( version, dis );
  //           break;
  //         case 'F':
  //           break; // continue parsing stations
  //         case 'E':
  //           todo = false;
  //           break;
  //         default:
  //           todo = false;
  //           break;
  //       }
  //     }
  //     dis.close();
  //     fis.close();
  //   } catch ( FileNotFoundException e ) { // this is ok
  //     // TDLog.v("EXP file not found");
  //     scrap_index = -1;
  //   } catch ( IOException e ) { 
  //     // TDLog.v("EXP IO exception " + e.getMessage() );
  //   }
  //   return scrap_index;
  // }

  /** write a sketch directly from a tdr file to the therion output 
   * @param dis         tdr data input
   * @param out         therion output writer
   * @param file_name   filename without extension
   * @param bbox        clipping rectangle (read in if not null)
   * @param beginheader whether to write the initial header
   * @param endscrap    whether to write the scrap termination
   * @param xoff        X offset
   * @param yoff        Y offset
   * @param xsection_name optional xsection name // 20230213
   *
   * @note used only to write XSections to the output file
   * @note bbox != null  <==>  beginheader true
   */
  static private void dataStreamToTherion( DataInputStream dis, BufferedWriter out, String file_name, RectF bbox, 
                                          boolean beginheader, boolean endscrap,
                                          float xoff, float yoff, String xsection_name ) throws IOException 
  {
    int version = 0;
    // boolean in_scrap = false;
    // int scrap_index = 0;

    boolean do_north = false;
    float north_x1=xoff, north_y1=yoff, north_x2=xoff, north_y2=yoff;

    int type = 0;
    boolean project = false;
    int project_dir = 0;
    int oblique = 0;
    // String points = "";
    // String lines  = "";
    // String areas  = "";
    String th_str;

    // synchronized( TDPath.mTdrLock ) // FIXME-THREAD_SAFE
    {
      // try {
        // TDLog.Log( TDLog.LOG_IO, "tdr to Therion. file " + file.getPath() );
        // FileInputStream fis = TDFile.getFileInputStream( file.getPath() );
        // DataInputStream dis = new DataInputStream( fis );
        boolean todo = true;
        while ( todo ) {
          int what = dis.read();
          switch ( what ) {
            case 'V':
              version = dis.readInt();
              break;
            case 'I': // plot info: bounding box
              {
	        if ( bbox != null ) {
                  bbox.left   = dis.readFloat();
                  bbox.top    = dis.readFloat();
                  bbox.right  = dis.readFloat();
                  bbox.bottom = dis.readFloat();
		} else {
                  for ( int k=0; k<4; ++k)  dis.readFloat();
		}
                if ( dis.readInt() == 1 ) {
                  do_north = true;
                  north_x1 = xoff + dis.readFloat();
                  north_y1 = yoff + dis.readFloat();
                  north_x2 = xoff + dis.readFloat();
                  north_y2 = yoff + dis.readFloat();
                }
                if ( bbox != null ) exportTherionGlobalHeader( out, type, bbox, file_name );
                // exportTherionHeader2( out, points, lines, areas );
                String proj = PlotType.projName( type );
                exportTherionScrapHeader( out, type, xsection_name, proj, project_dir, oblique, do_north, north_x1, north_y1, north_x2, north_y2, null ); // TH2EDIT no null last param
                // if ( do_north ) { 
                //   exportTherionScrapHeader( out, type, scrap_name, proj, 0, 0, true, north_x1, north_y1, north_x2, north_y2, null ); // TH2EDIT no null last param
                // } else {
                //   exportTherionScrapHeader( out, type, scrap_name, proj, project_dir, oblique, false, 0, 0, 0, 0, null ); // TH2EDIT no null last param
                // }
                // in_scrap = true; // UNUSED HERE
              }
              break;
            case 'S': // scrap
              {
                // String name = // 20230213 discard name read from the file - use arg
                  dis.readUTF();
                type = dis.readInt();
                if ( type == PlotType.PLOT_PROJECTED ) {
                  project_dir = dis.readInt();
                  // OBLIQUE if ( version > 602029 ) oblique = dis.readInt();
                }
                // read palettes
                /* points = */ dis.readUTF();
                /* lines  = */ dis.readUTF();
                /* areas  = */ dis.readUTF();
              }
              break;
            case 'N':
              /* scrap_index = */ dis.readInt();
              break;
            case 'P':
              DrawingPointPath pointpath = DrawingPointPath.loadDataStream( version, dis, xoff, yoff /*, null */ );
              if ( pointpath != null ) {
                th_str = pointpath.toTherion();
	        if ( th_str != null ) out.write( th_str );
              }
              break;
            case 'T':
              DrawingLabelPath labelpath = DrawingLabelPath.loadDataStream( version, dis, xoff, yoff );
              if ( labelpath != null ) {
                th_str = labelpath.toTherion();
	        if ( th_str != null ) out.write( th_str );
              }
              break;
            case 'L':
              DrawingLinePath linepath = DrawingLinePath.loadDataStream( version, dis, xoff, yoff /*, null */ );
              if ( linepath != null ) {
                th_str = linepath.toTherion();
	        if ( th_str != null ) out.write( th_str );
              }
              break;
            case 'A':
              DrawingAreaPath areapath = DrawingAreaPath.loadDataStream( version, dis, xoff, yoff /*, null */ );
              if ( areapath != null ) {
                th_str = areapath.toTherion();
	        if ( th_str != null ) out.write( th_str );
              }
              break;
            case 'J':
              DrawingSpecialPath specialpath = DrawingSpecialPath.loadDataStream( version, dis, xoff, yoff );
              if ( specialpath != null ) {
                th_str = specialpath.toTherion(); // empty string anyways
	        if ( th_str != null ) out.write( th_str );
              }
              break;
            case 'U':
              DrawingStationUser stationpath = DrawingStationUser.loadDataStream( version, dis );
              if ( stationpath != null ) {
                th_str = stationpath.toTherion();
	        if ( th_str != null ) out.write( th_str );
              }
              break;
            case 'X':
              // NOTE need to check XSection ??? STATION_XSECTION
              DrawingStationName namepath = DrawingStationName.loadDataStream( version, dis );
              if ( namepath != null ) {
                th_str = namepath.toTherion();
	        if ( th_str != null ) out.write( th_str );
              }
              break;
            case 'Y':
              DrawingPhotoPath photopath = DrawingPhotoPath.loadDataStream( version, dis, xoff, yoff );
              if ( photopath != null ) {
              }
              break;
            case 'Z':
              DrawingAudioPath audiopath = DrawingAudioPath.loadDataStream( version, dis, xoff, yoff );
              if ( audiopath != null ) {
              }
              break;
            // case 'W':
            //   DrawingSensorPath sensorpath = DrawingSensorPath.loadDataStream( version, dis, dx, dy );
            //   if ( sensorpath != null ) {
            //   }
            //   break;

            // case 'G':
            //   DrawingFixedName.loadDataStream( version, dis ); // consume DrawingFixedName data
            //   break;
            case 'F':
              break; // continue parsing stations
            case 'E':
              todo = false;
              break;
            default:
              todo = false;
              TDLog.e( "ERROR bad input (2) " + what );
              break;
          } 
        }
        if (endscrap ) {
          exportTherionScrapEnd( out );
          out.flush();
        }
        // fis.close();
      // } catch ( FileNotFoundException e ) {
      //   // this is OK
      // } catch ( IOException e ) {
      //   e.printStackTrace();
      // }
    }
  }
  
  // -----------------------------------------------------------------------------
  // CSURVEY
  // static void doExportCsxXSection( PrintWriter pw, String filename, String survey, String cave, String branch, /* String session, */ String bind )
  // {
  //   doExportAnyCsxXSection( pw, filename, survey, cave, branch, /* session, */ bind, 0 );
  // }

  static void doExportTCsxXSection( PrintWriter pw, String filename, String survey, String cave, String branch, /* String session, */ String bind )
  {
    // TDLog.v("export CSX x-section, survey " + survey + " cave " + cave + " branch " + branch );
    doExportAnyCsxXSection( pw, filename, survey, cave, branch, /* session, */ bind /*, 1 */ );
  }

  static private void doExportAnyCsxXSection( PrintWriter pw, String filename, String survey, String cave, String branch, /* String session, */ String bind /*, int format */ )
  {
    String subdir = survey + "/tdr";
    if ( ! TDFile.hasMSfile( subdir, filename ) ) return;
    int version = 0;
    // boolean in_scrap = false;
    // int scrap_index = 0;
    // String name = "";
    String points = ""; // initializer redundant
    String lines  = "";
    String areas  = "";

    ArrayList< DrawingPath > paths = new ArrayList<>();

    // synchronized( TDPath.mTdrLock ) // FIXME-THREAD_SAFE
    {
      try {
        // TDLog.v( "export cSurvey. survey " + survey + " X-section file " + filename );
        DataInputStream dis = new DataInputStream( TDFile.getMSinput( subdir, filename, "application/octet-stream" ) );
        boolean todo = true;
        while ( todo ) {
          int what = dis.read();
          switch ( what ) {
            case 'V':
              version = dis.readInt();
              break;
            case 'I': // plot info: bounding box
              {
                dis.readFloat();
                dis.readFloat();
                dis.readFloat();
                dis.readFloat();
                if ( dis.readInt() == 1 ) {
                  dis.readFloat();
                  dis.readFloat();
                  dis.readFloat();
                  dis.readFloat();
                }
                // in_scrap = true;
              }
              break;
            case 'S':
              {
                /* name = */ dis.readUTF();
                int type = dis.readInt();
                if ( ! PlotType.isAnySection( type ) ) {
                  dis.close();
                  return;
                }
                // project_dir = dis.readInt();
                // read palettes
                dis.readUTF();
                dis.readUTF();
                dis.readUTF();
              }
              break;
            case 'N':
              /* scrap_index = */ dis.readInt();
              break;
            case 'P':
              paths.add( DrawingPointPath.loadDataStream( version, dis, 0, 0 /*, null */ ) );
              break;
            case 'T':
              paths.add( DrawingLabelPath.loadDataStream( version, dis, 0, 0 ) );
              break;
            case 'L':
              paths.add( DrawingLinePath.loadDataStream( version, dis, 0, 0 /*, null */ ) );
              break;
            case 'A':
              paths.add( DrawingAreaPath.loadDataStream( version, dis, 0, 0 /*, null */ ) );
              break;
	    case 'J':
              paths.add( DrawingSpecialPath.loadDataStream( version, dis, 0, 0 ) );
              break;
            case 'U':
              DrawingStationUser.loadDataStream( version, dis );
              break;
            case 'X':
              // NOTE need to check XSection ??? STATION_XSECTION
              DrawingStationName.loadDataStream( version, dis );
              break;
            case 'Y':
              DrawingPhotoPath.loadDataStream( version, dis, 0, 0 );
              break;
            case 'Z':
              DrawingAudioPath.loadDataStream( version, dis, 0, 0 );
              break;
            // case 'W':
            //   DrawingSensorPath.loadDataStream( version, dis, dx, dy );
            //   break;

            // case 'G':
            //   DrawingFixedName.loadDataStream( version, dis ); // consume DrawingFixedName data
            //   break;
            case 'F':
            case 'E':
              todo = false;
              break;
            default:
              todo = false;
              TDLog.e( "ERROR " + filename + " bad input (4) " + what );
              break;
          } 
        }
        dis.close();
      } catch ( FileNotFoundException e ) {
        // this is OK
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
    // if ( format == 0 ) {
    //   doExportAsCsx( pw, survey, cave, branch, /* session, */ bind, paths, null, null ); // all_sections=null, sections=null
    // } else {
      doExportAsTCsx( pw, survey, cave, branch, /* session, */ bind, paths, null, null ); // all_sections=null, sections=null
    // }
  }

  static void doExportAsTCsx( PrintWriter pw, String survey, String cave, String branch, /* String session, */ String bind,
                             List< DrawingPath > paths, List< PlotInfo > all_sections, List< PlotInfo > sections )
  {
    int csxIndex = 0;
    for ( DrawingPath p : paths ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
        DrawingAreaPath ap = (DrawingAreaPath)p;
  // system and special points
        ap.toTCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ ); 
      } else if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
        DrawingLinePath lp = (DrawingLinePath)p;
        lp.toTCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
      } else if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
        DrawingPointPath pp = (DrawingPointPath)p;
        String section_info = null;
        if ( TDSetting.mAutoXSections && all_sections != null && BrushManager.isPointSection( pp.mPointType ) ) {
          // option: -scrap survey-xx# 
          PlotInfo section = null;
          String scrapname = TDUtil.replacePrefix( TDInstance.survey, pp.getOption( TDString.OPTION_SCRAP ) );
          if ( scrapname != null ) {
            for ( PlotInfo s : all_sections ) {
              if ( scrapname.endsWith( s.name ) ) {
                // String name = survey + "-" + s.name; // scrap filename
                section = s;
                section.csxIndex = csxIndex;
                ++ csxIndex;
                if ( sections != null ) sections.add( section );
                break;
              }
            }
          }
          if ( section != null ) {
            StringWriter sb = new StringWriter();
            PrintWriter  pb = new PrintWriter( sb );
            // special toCsurvey for cross-section points
            pb.format("sectiontext=\"%s\" sectionname=\"%s\" ", ( ( section.nick == null || section.nick.length() == 0 )? section.name : section.nick ), section.name );
            if ( section.name.startsWith("xs-") || section.name.startsWith("xh-") ) {
              pb.format("station=\"%s\" ", section.name.substring(3) ); // == section.start
            } else {
              pb.format("stationfrom=\"%s\" stationto=\"%s\" ", section.start, section.view );
            }
            // pw.format(" segment=\"%s\"", "undefined" );
            pb.format(Locale.US, "sectionazimuth=\"%.2f\" sectionclino=\"%.2f\" sectionid=\"%d\"\n", section.azimuth, section.clino, section.csxIndex );
            section_info = sb.toString();
          }
          pp.toTCsurvey( pw, survey, cave, branch, bind, section_info, section );
        } else {
          if ( TDSetting.mExportMedia || ! BrushManager.isPointMedia( pp.mPointType ) ) {
            pp.toTCsurvey( pw, survey, cave, branch, bind );
          }
        }
      }
    }
  }

  // static void doExportAsCsx( PrintWriter pw, String survey, String cave, String branch, /* String session, */ String bind,
  //                            List< DrawingPath > paths, List< PlotInfo > all_sections, List< PlotInfo > sections )
  // {
  //   int csxIndex = 0;
  //   pw.format("    <layers>\n");

  //   // LAYER 0: images and sketches
  //   pw.format("      <layer name=\"Base\" type=\"0\">\n");
  //   pw.format("         <items>\n");
  //   pw.format("         </items>\n");
  //   pw.format("      </layer>\n");

  //   // LAYER 1: soil areas
  //   pw.format("      <layer name=\"Soil\" type=\"1\">\n");
  //   pw.format("        <items>\n");
  //   for ( DrawingPath p : paths ) {
  //     if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
  //       DrawingAreaPath ap = (DrawingAreaPath)p;
  //       if ( BrushManager.getAreaCsxLayer( ap.mAreaType ) != 1 ) continue;
  //       ap.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ ); 
  //     }
  //   }
  //   pw.format("        </items>\n");
  //   pw.format("      </layer>\n");

  //   // LAYER 2: 
  //   pw.format("      <layer name=\"Water and floor morphologies\" type=\"2\">\n");
  //   pw.format("        <items>\n");
  //   for ( DrawingPath p : paths ) {
  //     if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
  //       DrawingLinePath lp = (DrawingLinePath)p;
  //       if ( BrushManager.getLineCsxLayer( lp.mLineType ) != 2 ) continue;
  //       lp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
  //     } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
  //       DrawingAreaPath ap = (DrawingAreaPath)p;
  //       if ( BrushManager.getAreaCsxLayer( ap.mAreaType ) != 2 ) continue;
  //       ap.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ ); 
  //     } 
  //   }
  //   pw.format("        </items>\n");
  //   pw.format("      </layer>\n");

  //   // LAYER 3
  //   pw.format("      <layer name=\"Rocks and concretions\" type=\"3\">\n");
  //   pw.format("        <items>\n");
  //   for ( DrawingPath p : paths ) {
  //     if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
  //       DrawingLinePath lp = (DrawingLinePath)p;
  //       if ( BrushManager.getLineCsxLayer( lp.mLineType ) != 3 ) continue;
  //       lp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
  //     }
  //   }
  //   pw.format("        </items>\n");
  //   pw.format("      </layer>\n");

  //   // LAYER 4
  //   pw.format("      <layer name=\"Ceiling morphologies\" type=\"4\">\n");
  //   pw.format("        <items>\n");
  //   for ( DrawingPath p : paths ) {
  //     if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
  //       DrawingLinePath lp = (DrawingLinePath)p;
  //       if ( BrushManager.getLineCsxLayer( lp.mLineType ) != 4 ) continue;
  //       lp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
  //     }
  //   }
  //   pw.format("        </items>\n");
  //   pw.format("      </layer>\n");

  //   // LAYER 5:
  //   pw.format("      <layer name=\"Borders\" type=\"5\">\n");
  //   pw.format("        <items>\n");
  //   for ( DrawingPath p : paths ) {
  //     if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
  //       DrawingLinePath lp = (DrawingLinePath)p;
  //       if ( BrushManager.getLineCsxLayer( lp.mLineType ) != 5 ) continue;
  //       lp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
  //     }
  //     // if ( lp.lineType() == BrushManager.mLineLib.mLineWallIndex ) {
  //     //   // linetype: 0 line, 1 spline, 2 bezier
  //     //   pw.format("          <item layer=\"5\" name=\"\" type=\"4\" category=\"1\" linetype=\"0\" mergemode=\"0\">\n");
  //     //   pw.format("            <pen type=\"1\" />\n");
  //     //   pw.format("            <points data=\"");
  //     //   ArrayList< LinePoint > pts = lp.mPoints;
  //     //   boolean b = true;
  //     //   for ( LinePoint pt : pts ) {
  //     //     float x = DrawingWindow.sceneToWorldX( pt.x );
  //     //     float y = DrawingWindow.sceneToWorldY( pt.y );
  //     //     pw.format(Locale.US, "%.2f %.2f ", x, y );
  //     //     if ( b ) { pw.format("B "); b = false; }
  //     //   }
  //     //   pw.format("\" />\n");
  //     //   pw.format("          </item>\n");
  //     // }
  //   }
  //   pw.format("        </items>\n");
  //   pw.format("      </layer>\n");

  //   // LAYER 6: signs and texts
  //   pw.format("      <layer name=\"Signs\" type=\"6\">\n");
  //   pw.format("        <items>\n");
  //   for ( DrawingPath p : paths ) {
  //     if ( p.mType != DrawingPath.DRAWING_PATH_POINT ) continue;
  //     DrawingPointPath pp = (DrawingPointPath)p;
  //     if ( BrushManager.getPointCsxLayer( pp.mPointType ) != 6 ) continue;

  //     // section points are special
  //     if ( all_sections != null && BrushManager.isPointSection( pp.mPointType ) ) {
  //       if ( TDSetting.mAutoXSections ) {
  //         // TDLog.v( "Section point <" + pp.mOptions + ">");
  //         // option: -scrap survey-xx#
  //         // FIXME GET_OPTION
  //         PlotInfo section = null;
  //         String scrapname = TDUtil.replacePrefix( TDInstance.survey, pp.getOption( TDString.OPTION_SCRAP ) );
  //         if ( scrapname != null ) {
  //           for ( PlotInfo s : all_sections ) {
  //             if ( scrapname.endsWith( s.name ) ) {
  //               // String name = survey + "-" + s.name; // scrap filename
  //               section = s;
  //               section.csxIndex = csxIndex;
  //               if ( sections != null ) sections.add( section );
  //               break;
  //             }
  //           }
  //         }
  //         if ( section != null ) {
  //           // TDLog.v( "section " + section.name + " " + section.nick );
  //           // special toCsurvey for cross-section points
  //           float x = DrawingUtil.sceneToWorldX( pp.cx, pp.cy ); // convert to world coords.
  //           float y = DrawingUtil.sceneToWorldY( pp.cx, pp.cy );
  //           String text = TDString.isNullOrEmpty( section.nick )? section.name : section.nick;
  //           pw.format("  <item layer=\"6\" cave=\"%s\" branch=\"%s\" type=\"9\" category=\"96\" direction=\"0\" ", cave, branch );
  //           pw.format("text=\"%s\" textdistance=\"2\" crosswidth=\"4\" crossheight=\"4\" name=\"%s\" ", text, section.name );
  //           // pw.format("crosssection=\"%d\" ", section.csxIndex );
  //           if ( section.name.startsWith("xs-") || section.name.startsWith("xh-") ) {
  //             pw.format("station=\"%s\" ", section.name.substring(3) ); // == section.start
  //           } else {
  //             pw.format("stationfrom=\"%s\" stationto=\"%s\" ", section.start, section.view );
  //           }
  //           // pw.format(" segment=\"%s\"", "undefined" );
  //           pw.format(Locale.US, "splayborderprojectionangle=\"%.2f\" splayborderprojectionvangle=\"%.2f\" id=\"%d\">\n",
  //             section.azimuth, section.clino, section.csxIndex );
  //           pw.format(Locale.US, "<points data=\"%.2f %.2f \" />\n", x, y );
  //           pw.format("    <font type=\"4\" />\n");
  //           pw.format("  </item>\n");
  //         } else {
  //           TDLog.e("xsection not found. Name: " + ((scrapname == null)? "null" : scrapname) );
  //         }
  //       }
  //     } else {
  //       pp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
  //     }
  //     ++ csxIndex;
  //     
  //   }
  //   pw.format("        </items>\n");
  //   pw.format("      </layer>\n");
  //   pw.format("    </layers>\n");
  // }

  // this is called by DrawingCommandManager
  static void exportCave3D(
      int type,
      PrintWriter pw,
      DrawingCommandManager manager,
      TDNum num,
      String scrap_name,
      int proj_dir,
      int oblique, // TODO OBLIQUE not used
      final List< Scrap > scraps,
      float xoff, float yoff, float zoff
  )
  {
    // XYZefoffsets are on the header-line
    pw.format(Locale.US, "SCRAP %s %d %d %f %f %f\n", scrap_name, type, proj_dir, xoff, yoff, zoff );
    // TODO export points library
    synchronized( scraps ) {
      for ( Scrap scrap : scraps ) {
        // pw.format("N %d\n", scrap.mScrapIdx );
        List< ICanvasCommand > cstack = scrap.mCurrentStack;
        synchronized( TDPath.mCommandsLock ) {
          for ( ICanvasCommand cmd : cstack ) {
            if ( cmd.commandType() != 0 ) continue;
            DrawingPath p = (DrawingPath) cmd;
            if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) continue; // safety check: should not happen
            p.toCave3D( pw, type, manager, num );
          }
        }
      }
    }
  }

  static void exportCave3DXSection (
          int type,
          PrintWriter pw,
          // DrawingCommandManager manager,
          String scrap_name,
          @SuppressWarnings("ParameterCanBeLocal") int azimuth, int clino,
          Scrap scrap,
          TDVector center, TDVector V1, TDVector V2
  )
  {
    // XYZefoffsets are on the header-line
    azimuth = 0; // FIXME
    pw.format(Locale.US, "SCRAP %s %d %d %f %f %f\n", scrap_name, type, azimuth, /* clino, */ center.x, center.y, center.z );
    // synchronized( scraps ) { // FIXME
      // pw.format("N %d\n", scrap.mScrapIdx );
      List< ICanvasCommand > cstack = scrap.mCurrentStack;
      synchronized( TDPath.mCommandsLock ) {
        for ( ICanvasCommand cmd : cstack ) {
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath p = (DrawingPath) cmd;
          if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) continue; // safety check: should not happen
          p.toCave3D( pw, type, V1, V2 );
        }
      }
    // }
  }

  // interface for ExportPlotToFile
  // bw is flushed and closed by the caller
  static boolean exportCave3D( BufferedWriter bw, DrawingCommandManager manager, TDNum num, PlotInfo plot, FixedInfo fix, String fullname )
  {
    float xoff = 0;
    float yoff = 0;
    float zoff = 0;
    String start_station = plot.start; // start station has "num" coords (0,0,0)
    String fixed_station = fix.name;
    // TDLog.v( "start " + start_station + " fix " + fixed_station );

    NumStation fixed = num.getStation( fix.name );
    if ( fixed == null ) { // cannot export Cave3D
      TDLog.e("IO cannot export: fixed null");
      return false;
    }
    // TDLog.v( "Fixed " + fixed.name + ": " + fixed.e + " " + fixed.s + " " + fixed.v );
    if ( fix.cs != null ) {
      // TDLog.v( "CS " + fix.cs_lng + " " + fix.cs_lat + " " + fix.cs_h_geo );
      xoff = (float)(fix.cs_lng - fixed.e);
      yoff = (float)(fix.cs_lat + fixed.s);
      zoff = (float)(fix.cs_h_geo + fixed.v);
    } else {
      // TDLog.v( "WGS84 " + fix.lng + " " + fix.lat + " " + fix.h_ell );
      xoff = (float)(fix.lng - fixed.e);
      yoff = (float)(fix.lat + fixed.s);
      zoff = (float)(fix.h_ell + fixed.v);
    }
    int azimuth = (int)(plot.azimuth);
    int clino   = (int)(plot.clino);

    if ( PlotType.isAnySection( plot.type ) ) {
      if ( TDString.isNullOrEmpty( plot.start ) ) return false;
      NumStation start = num.getStation( plot.start );
      if ( start == null ) return false;

      // TDLog.v( "azimuth " + azimuth + " clino " + clino );
      float cc = TDMath.cosd( clino ); 
      float sc = TDMath.sind( clino ); 
      float ca = TDMath.cosd( azimuth );
      float sa = TDMath.sind( azimuth );
      TDVector V0 = new TDVector( cc * sa, cc * ca, sc );
      TDVector V1 = new TDVector( ca, -sa, 0 );
      TDVector V2 = V1.cross( V0 ); // new TDVector( -sc * sa, -sc * ca, cc );

      // TDLog.v( "fixed at " + fixed.name + " " + fixed.e + " " + fixed.s + " " + fixed.v );
      // TDLog.v( "start at " + start.name + " " + start.e + " " + start.s + " " + start.v );
      // offset of xsection origin (xoff, yoff, zoff)
      float ratio = 0;
      NumStation view  = num.getStation( plot.view  );
      TDVector viewed = null;
      if ( view != null ) {
        xoff += view.e-fixed.e;
        yoff -= view.s-fixed.s;
        zoff -= view.v-fixed.v;
        viewed = new TDVector( (float)(start.e - view.e), -(float)(start.s - view.s), -(float)(start.v - view.v) );
        float d3 = viewed.lengthSquared();
        float dd = V0.dot( viewed );
        ratio = TDMath.sqrt( (d3 - dd*dd) ); // world coordinates
        // TDLog.v( "view  at " + view.name + " " + view.e + " " + view.s + " " + view.v + " length " + viewed.length() );
        // TDLog.v( "viewed  " + viewed.x + " " + viewed.y + " " + viewed.z + " length " + viewed.length() );
      } else {
        xoff += start.e-fixed.e;
        yoff -= start.s-fixed.s;
        zoff -= start.v-fixed.v;
      }
      TDVector center = new TDVector( xoff, yoff, zoff );
      // TDLog.v( "center  " + center.x + " " + center.y + " " + center.z );
      PrintWriter pw = new PrintWriter( bw );
      manager.exportCave3DXSection( plot.type, pw, fullname, azimuth, clino, center, V1, V2, viewed, ratio );
    } else {
      PrintWriter pw = new PrintWriter( bw );
      manager.exportCave3D( plot.type, pw, num, fullname, azimuth, clino, xoff, yoff, zoff );
    }
    return true;
  }

}
