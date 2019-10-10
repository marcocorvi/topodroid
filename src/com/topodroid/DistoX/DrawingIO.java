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
package com.topodroid.DistoX;

// import android.util.Log;

import java.io.File;
import java.io.FileWriter;
// import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
// import java.io.EOFException;

import java.util.List;
import java.util.ArrayList;
// import android.util.ArraySet; // API 23
// import java.util.HashMap;
import java.util.Locale;

import android.graphics.RectF;

class DrawingIO
{

  private static String readLine( BufferedReader br )
  {
    String line = null;
    try {
      line = br.readLine();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    if ( line != null ) {
      line = line.trim();
      line = line.replaceAll(" *", " ");
      // line.replaceAll("\\s+", " ");
    }
    return line;
  } 

  /* NOTE therion th2 files can no longer be supported because therionscale is not fixed
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
  //   // BrushManager.makePaths( );
  //   BrushManager.resetPointOrientations();

  //   // TDLog.Log( TDLog.LOG_PLOT, "after reset 0: " + BrushManager.mOrientation[0]
  //   //                      + " 7: " + BrushManager.mOrientation[7] );

  //   // Log.v("DistoX", "drawing I/O load therion " + filename );
  //   // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
  //   {
  //     try {
  //       TDLog.Log( TDLog.LOG_IO, "load plot from Therion file " + filename );
  //       FileReader fr = new FileReader( filename );
  //       BufferedReader br = new BufferedReader( fr );
  //       String line = null;
  //       while ( (line = readLine(br)) != null ) {
  //         int comment = line.indexOf('#');
  //         if ( comment == 0 ) {
  //           if ( line.startsWith( "#P " ) ) { // POINT PALETTE
  //             if ( localPalette != null ) {
  //               localPalette.mPalettePoint.clear();
  //               localPalette.addPointFilename( "user" );
  //               String[] syms = line.split( " " );
  //               for ( int k=1; k<syms.length; ++k ) {
  //                 if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addPointFilename( syms[k] );
  //               }
  //               BrushManager.mPointLib.makeEnabledListFromPalette( localPalette );
  //             }
  //           } else if ( line.startsWith( "#L " ) ) { // LINE PALETTE
  //             if ( localPalette != null ) {
  //               localPalette.mPaletteLine.clear();
  //               localPalette.addLineFilename("user");
  //               String[] syms = line.split( " " );
  //               for ( int k=1; k<syms.length; ++k ) {
  //                 if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addLineFilename( syms[k] );
  //               }
  //               BrushManager.mLineLib.makeEnabledListFromPalette( localPalette );
  //             }
  //           } else if ( line.startsWith( "#A " ) ) { // AREA PALETTE
  //             if ( localPalette != null ) {
  //               localPalette.mPaletteArea.clear();
  //               localPalette.addAreaFilename("user");
  //               String[] syms = line.split( " " );
  //               for ( int k=1; k<syms.length; ++k ) {
  //                 if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addAreaFilename( syms[k] );
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
  //         line = line.replaceAll("\\s+", " ");
  //         String[] vals = line.split( " " );
  //         if ( vals.length == 0 ) continue;
  //         if ( vals[0].equals( "scrap" ) ) {
  //           if ( vals.length < 4 ) {
  //             TDLog.Error( "bad scrap cmd: " + line );
  //           } else {
  //             // String name = vals[1];
  //             // skip "-projection" vals[2]
  //             is_not_section = ! vals[3].equals("none");
  //           }
  //         } else if ( vals[0].equals( "point" ) ) {
  //           // ****** THERION POINT ********************************** point X Y type [options]
  //           if ( vals.length < 4 ) {
  //             TDLog.Error( "bad point cmd: " + line );
  //           } else {
  //             int ptType = BrushManager.mPointLib.size();
  //             boolean has_orientation = false;
  //             float orientation = 0.0f;
  //             int scale = DrawingPointPath.SCALE_M;
  //             String options = null;

  //             try {
  //               x = dx + Float.parseFloat( vals[1] ) / toTherion;
  //               y = dy - Float.parseFloat( vals[2] ) / toTherion;
  //             } catch ( NumberFormatException e ) {
  //               TDLog.Error( "Therion Point error (number fmt) <" + line + ">" );
  //               continue;
  //             }
  //             String type = vals[3];
  //             String label_text = null;
  //             int k = 4;
  //             if ( type.equals( "station" ) ) {
  //               if ( ! TDSetting.mAutoStations ) {
  //                 if ( vals.length > k+1 && vals[k].equals( "-name" ) ) {
  //                   String name = vals[k+1];
  //                   DrawingStationPath station_path = new DrawingStationPath( name, x, y, scale );
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
  //                   TDLog.Error( "Therion Point orientation error : " + line );
  //                 }
  //                 k += 2;
  //               } else if ( vals[k].equals( "-scale" ) ) {
  //                 // FIXME assert (vals.length > k+1 );
  //                 if ( vals[k+1].equals("xs") ) {
  //                   scale = DrawingPointPath.SCALE_XS;
  //                 } else if ( vals[k+1].equals("s") ) {
  //                   scale = DrawingPointPath.SCALE_S;
  //                 } else if ( vals[k+1].equals("l") ) {
  //                   scale = DrawingPointPath.SCALE_L;
  //                 } else if ( vals[k+1].equals("xl") ) {
  //                   scale = DrawingPointPath.SCALE_XL;
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

  //             BrushManager.mPointLib.tryLoadMissingPoint( type );
  //             // map pre 3.1.1 thnames to 3.1.1 names
  //             String thname = type;
  //             if ( thname.equals( "user" ) )        { thname = "u:user"; }
  //             else if ( thname.equals( "danger" ) ) { thname = "u:danger"; }
  //             else if ( thname.equals( "archeo" ) ) { thname = "archeo-material"; }
  //             ptType = BrushManager.mPointLib.getSymbolIndexByThName( thname );
  //             // Log.v("DistoX", "type " + type + " thname " + thname + " " + ptType );
  //             if ( ptType < 0 ) {
  //               if ( missingSymbols != null ) missingSymbols.addPointFilename( type ); // add "type" to the missing point-types
  //               ptType = 0; // SymbolPointLibrary.mPointUserIndex; // FIXME
  //               // continue;
  //             }

  //             if ( ptType == BrushManager.mPointLib.mPointLabelIndex ) {
  //               if ( label_text != null ) {
  //                 // "danger" is no longer mapped on a label 
  //                 // if ( label_text.equals( "!" ) ) {    // "danger" point
  //                 //   DrawingPointPath path = new DrawingPointPath( BrushManager.mPointLib.mPointDangerIndex, x, y, scale, text, options );
  //                 //   surface.addDrawingPath( path );
  //                 // } else {                             // regular label
  //                   DrawingLabelPath path = new DrawingLabelPath( label_text, x, y, scale, options );
  //                   if ( has_orientation ) {
  //                     path.setOrientation( orientation );
  //                   }
  //                   surface.addDrawingPath( path );
  //                 // }
  //               }
  //             } else if ( has_orientation && BrushManager.mPointLib.isSymbolOrientable(ptType) ) {
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
  //         } else if ( vals[0].equals( "line" ) ) {
  //           // ********* THERION LINES ************************************************************
  //           if ( vals.length < 2 ) {
  //             TDLog.Error( "bad line cmd: " + line );
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
  //                   TDLog.Error( "Therion Line error (number fmt) <" + line + ">" );
  //                   continue;
  //                 }
  //                 path.addStartPoint( x, y );

  //                 while ( (line = readLine( br )) != null ) {
  //                   if ( line.equals( "endline" ) ) {
  //                     line = readLine( br ); // area statement
  //                     String[] vals2 = line.split( " " );
  //                     if ( vals2.length >= 2 ) {
  //                       BrushManager.mAreaLib.tryLoadMissingArea( vals2[1] );
  //                       String thname = vals2[1];
  //                       if ( thname.equals( "user" ) ) { thname = "u:user"; }
  //                       arType = BrushManager.mAreaLib.getSymbolIndexByThName( thname );
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
  //                           TDLog.Error( "Therion Area orientation error <" + line + ">" );
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
  //                       TDLog.Error( "Therion Line X-Y error (10) <" + line + ">" );
  //                       continue;
  //                     } catch ( ArrayIndexOutOfBoundsException e ) {
  //                       TDLog.Error( "Therion Line X-Y error (11) " + line );
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
  //                       TDLog.Error( "Therion Line X-Y error (12) <" + line + ">" );
  //                       continue;
  //                     } catch ( ArrayIndexOutOfBoundsException e ) {
  //                       TDLog.Error( "Therion Line X-Y error (13) " + line );
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
  //               BrushManager.mLineLib.tryLoadMissingLine( type );
  //               String thname = type;
  //               if ( thname.equals( "user" ) ) { thname = "u:user"; }
  //               lnType = BrushManager.mLineLib.getSymbolIndexByThName( thname );
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
  //                   TDLog.Error( "Therion Line X-Y error (1) <" + line + ">" );
  //                   continue;
  //                 } catch ( ArrayIndexOutOfBoundsException e ) {
  //                   TDLog.Error( "Therion Line X-Y error (2) " + line );
  //                   continue;
  //                 }
  //                 // Log.v( "DistoX", "  line start point: <" + line + "> " + x + " " + y );
  //                 while ( (line = readLine( br )) != null ) {
  //                   if ( line.contains( "l-size" ) ) continue;
  //                   if ( line.equals( "endline" ) ) {
  //                     if ( path != null ) {
  //                       if ( type.equals("section") ) { // section line only in non-section scraps
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
  //                         TDLog.Error( "Therion Line X-Y error (3) <" + line + ">" );
  //                         continue;
  //                       } catch ( ArrayIndexOutOfBoundsException e ) {
  //                         TDLog.Error( "Therion Line X-Y error (4) " + line );
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
  //                         TDLog.Error( "Therion Line X-Y error (5) <" + line + ">" );
  //                         continue;
  //                       } catch ( ArrayIndexOutOfBoundsException e ) {
  //                         TDLog.Error( "Therion Line X-Y error (6) " + line );
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
  //
  // FIXME DataHelper and SID are necessary to export splays by the station
  // @param fullname  full scrap name ( filename without extension )
  static void exportTherion( DrawingCommandManager manager, int type, File file, String fullname, String projname, int proj_dir, boolean multiscrap )
  {
    TDLog.Log( TDLog.LOG_IO, "export Therion " + fullname + " file " + file.getPath() );
    try {
      FileWriter fw = new FileWriter( file );
      BufferedWriter bw = new BufferedWriter( fw );
      manager.exportTherion( type, bw, fullname, projname, proj_dir, multiscrap );
      bw.flush();
      bw.close();
    } catch ( IOException e ) {
      TDLog.Error( "Export Therion i/o error: " + e.getMessage() );
    }
  }

  static void exportDataStream( DrawingCommandManager manager, int type, File file, String fullname, int proj_dir )
  {
    try {
      FileOutputStream fos = new FileOutputStream( file );

      // ByteArrayOutputStream bos = new ByteArrayOutputStream( 4096 );
      BufferedOutputStream bfos = new BufferedOutputStream( fos );
      DataOutputStream dos = new DataOutputStream( bfos );
      manager.exportDataStream( type, dos, fullname, proj_dir );
      dos.close();

      // CACHE add filename/bos.toByteArray to cache
      // mTdrCache.put( fullname + ".tdr", bos );
      // byte[] bytes = bos.toByteArray();
      // fos.write( bytes, 0, bos.size() );

      fos.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "Export Data file: " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "Export Data i/o: " + e.getMessage() );
    }
  }

  static void exportDataStream( List<DrawingPath> paths, int type, File file, String fullname, int proj_dir )
  {
    try {
      FileOutputStream fos = new FileOutputStream( file );

      // ByteArrayOutputStream bos = new ByteArrayOutputStream( 4096 );
      BufferedOutputStream bfos = new BufferedOutputStream( fos );
      DataOutputStream dos = new DataOutputStream( bfos );

      float xmin=1000000f, xmax=-1000000f, 
            ymin=1000000f, ymax=-1000000f;
      for ( DrawingPath p : paths ) {
        if ( p.left   < xmin ) xmin = p.left;
        if ( p.right  > xmax ) xmax = p.right;
        if ( p.top    < ymin ) ymin = p.top;
        if ( p.bottom > ymax ) ymax = p.bottom;
      }
      RectF bbox = new RectF( xmin, ymin, xmax, ymax );

      exportDataStream( type, dos, fullname, proj_dir, bbox, paths );

      dos.close();

      // CACHE add filename/bos.toByteArray to cache
      // mTdrCache.put( fullname + ".tdr", bos );
      // byte[] bytes = bos.toByteArray();
      // fos.write( bytes, 0, bos.size() );

      fos.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "Export Data file: " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "Export Data i/o: " + e.getMessage() );
    }
  }

  // tdr files CACHE
  // no need to use a cache: buffering streams is as fast as reading from file
  // and requires less management overhead
  // static HashMap< String, ByteArrayOutputStream > mTdrCache = new HashMap< String, ByteArrayOutputStream >();
  // static void clearTdrCache() { mTdrCache.clear(); }

  // =========================================================================
  // EXPORT details
  // V ... S ... I ...
  // paths: P. T, L. A. U. X.
  // F
  // stations: U
  // E

  static int skipTdrHeader( DataInputStream dis )
  {
    int what, type, dir;
    int version = 0;
    int flag = 0;
    float x, y;
    try {
      for ( int k=0; k<3; ++k ) {
        what = dis.read(); // 'V'
        if ( what == 'V' ) {
          flag |= 0x01;
          version = dis.readInt();
	  // Log.v("DistoXs", "TDR header version: " + version );
        } else if ( what == 'S' ) {
          flag |= 0x02;
          String name = dis.readUTF();
          type = dis.readInt();
          if ( type == PlotInfo.PLOT_PROJECTED ) dir = dis.readInt();
          String lib = dis.readUTF();
          lib = dis.readUTF();
          lib = dis.readUTF();
	  // Log.v("DistoXs", "TDR header scrap: " + name + " type " + type );
        } else if ( what == 'I' ) {
          flag |= 0x04;
          x = dis.readFloat();
          y = dis.readFloat();
	  // Log.v("DistoXs", "TDR header bbox from: " + x + " " + y );
          x = dis.readFloat();
          y = dis.readFloat();
	  // Log.v("DistoXs", "TDR header bbox to:   " + x + " " + y );
          if ( dis.readInt() == 1 ) {
            x = dis.readFloat();
            y = dis.readFloat();
            x = dis.readFloat();
            y = dis.readFloat();
          }
        } else {
          break;
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    } 
    return (flag == 0x07)? version : 0;
  }

  static boolean doLoadDataStream( DrawingSurface surface,
                                   String filename,
                                   float dx, float dy,
                                   // FIXME-MISISNG SymbolsPalette missingSymbols,
                                   SymbolsPalette localPalette,
                                   RectF bbox,
				   boolean complete,
				   String plotName )
  {
    // Log.v("DistoXX", "load data stream file " + filename );
    // if ( plotName != null ) Log.v("DistoXX", "load data stream plot-name " + plotName );

    int version = 0;
    boolean in_scrap = false;
    // BrushManager.makePaths( );
    BrushManager.resetPointOrientations();
    DrawingPath path = null;
    int project_dir = 0;
    float north_x1, north_y1, north_x2, north_y2;

    File file = new File( filename );
    if ( ! file.exists() ) return false;

    FileInputStream fis = null;
    DataInputStream dis = null;

    // FIXME SECTION_RENAME
    int pos = filename.lastIndexOf('/');
    String survey_name = null;
    if ( pos >= 0 ) {
      survey_name = filename.substring(pos+1);
    } else {
      survey_name = filename;
    }
    if ( survey_name != null ) {
      pos = survey_name.indexOf('-');
      if ( pos > 0 ) survey_name = survey_name.substring(0, pos);
    }

    // Log.v("DistoX", "drawing I/O load stream " + filename );
    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    {
      try {
        // CACHE check if filename is in the cache: if so use the cache byte array
        // ByteArrayOutputStream bos = mTdrCache.get( file.getName() );
        // if ( bos == null ) {
          TDLog.Log( TDLog.LOG_IO, "load tdr file " + filename );
          fis = new FileInputStream( filename );
          BufferedInputStream bfis = new BufferedInputStream( fis );
          dis = new DataInputStream( bfis );
        // } else {
        //   Log.v("DistoX", "cache hit " + filename );
        //   ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
        //   dis = new DataInputStream( bis );
        // }
        boolean todo = true;
        while ( todo ) {
          int what = dis.read();
          // Log.v("DistoX", "Read " + what );
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
                  north_x1 = dis.readFloat();
                  north_y1 = dis.readFloat();
                  north_x2 = dis.readFloat();
                  north_y2 = dis.readFloat();
                }
                // TDLog.Log(TDLog.LOG_PLOT, "TDR bbox " + xmin + "-" + xmax + " " + ymin + "-" + ymax );
              }
              break;
            case 'S':
              {
                String name = dis.readUTF();
                int type = dis.readInt();
                if ( type == PlotInfo.PLOT_PROJECTED ) project_dir = dis.readInt();
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
                // Log.v("DistoX", "TDR type " + type );
              }
              break;
            case 'P':
	      // FIXME SECTION_RENAME
              path = DrawingPointPath.loadDataStream( version, dis, dx, dy /*, missingSymbols */ ).fixScrap( survey_name );
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
            case 'L':
              path = DrawingLinePath.loadDataStream( version, dis, dx, dy /*, missingSymbols */ );
              // Log.v("DistoX0", "add path ... " + ((DrawingLinePath)path).mFirst.mX + " " + ((DrawingLinePath)path).mFirst.mY );
              break;
            case 'A':
              path = DrawingAreaPath.loadDataStream( version, dis, dx, dy /*, missingSymbols */ );
              break;
            case 'J':
              path = DrawingSpecialPath.loadDataStream( version, dis, dx, dy );
              break;
            case 'U':
              path = DrawingStationPath.loadDataStream( version, dis ); // consume DrawingStationPath data
              break;
            case 'X':
              path = DrawingStationName.loadDataStream( version, dis ); // consume DrawingStationName data
              break;
            case 'F':
              // TDLog.Log( TDLog.LOG_PLOT, "<F>" );
              if ( complete ) break; // continue parsing stations
            case 'E':
              todo = false;
              break;
            default:
              todo = false;
              TDLog.Error( "ERROR " + filename + " bad input (1) " + what );
              break;
          } 
          if ( path != null && in_scrap ) {
            // if ( plotName != null ) 
	      path.mPlotName = plotName;
            if ( what == 'U' ) {
              surface.addDrawingStationPath( (DrawingStationPath)path );
            } else {
              surface.addDrawingPath( path );
            }
          }
        }
        dis.close();
        /* if ( fis != null ) */ fis.close();
      } catch ( FileNotFoundException e ) {
        // this is OK
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      // Log.v("DistoX", "read: " + sb.toString() );
    }
    // FIXME-MISSING return (missingSymbols == null ) || missingSymbols.isOK();
    return true;
  }

  static void doLoadOutlineDataStream( DrawingSurface surface,
                                   String filename,
                                   float dx, float dy, String name )
  {
    int version = 0;
    boolean in_scrap = false;

    File file = new File( filename );
    FileInputStream fis = null;
    DataInputStream dis = null;

    // Log.v("DistoXX", "drawing I/O load outline stream " + filename + " name " + ((name == null)? "null" : name) );
    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    {
      try {
        // CACHE check if filename is in the cache: if so use the cache byte array
        // ByteArrayOutputStream bos = mTdrCache.get( file.getName() );
        // if ( bos == null ) {
          TDLog.Log( TDLog.LOG_IO, "load outline tdr file " + filename );
          fis = new FileInputStream( filename );
          BufferedInputStream bfis = new BufferedInputStream( fis );
          dis = new DataInputStream( bfis );
        // } else {
        //   Log.v("DistoX", "cache hit " + filename );
        //   ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
        //   dis = new DataInputStream( bis );
        // }
        boolean todo = true;
        while ( todo ) {
          DrawingLinePath path = null;
          int what = dis.read();
          // Log.v("DistoXX", "Read " + what );
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
            case 'S':
              {
                dis.readUTF();
                int type = dis.readInt();
                if ( type == PlotInfo.PLOT_PROJECTED ) dis.readInt();
                // read palettes
                dis.readUTF();
                dis.readUTF();
                dis.readUTF();
                in_scrap = true;
              }
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
              DrawingStationPath.loadDataStream( version, dis ); // consume DrawingStationName data
              break;
            case 'X':
              DrawingStationName.loadDataStream( version, dis ); // consume DrawingStationName data
              break;
            case 'F':
            case 'E':
            default:
              todo = false;
              // TDLog.Error( "ERROR " + filename + " bad input (3) " + (int)what );
              break;
          } 
          if (    in_scrap && path != null 
               && ( BrushManager.mLineLib.isWall( path.mLineType ) || path.hasOutline() ) ) {
            // Log.v("DistoXX", "outline add path ... " + path.mFirst.x + " " + path.mFirst.y + " path size " + path.size()  );
            path.setPathPaint( BrushManager.fixedGrid100Paint );
            if ( name != null ) { // xsection outline
              surface.addXSectionOutlinePath( new DrawingOutlinePath( name, path ) );
            } else {
              surface.addScrapOutlinePath( path );
            }
          }
        }
        dis.close();
        /* if ( fis != null ) */ fis.close();
      } catch ( FileNotFoundException e ) {
        // this is OK
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      // Log.v("DistoX", "read: " + sb.toString() );
    }
  }

  // used by ParserPocketTopo
  static void exportDataStream( int type, DataOutputStream dos, String scrap_name, int proj_dir,
                                        RectF bbox, List<DrawingPath> paths )
  {
    try { 
      dos.write( 'V' ); // version
      dos.writeInt( TopoDroidApp.VERSION_CODE );
      dos.write( 'S' );
      dos.writeUTF( scrap_name );
      dos.writeInt( type );
      if ( type == PlotInfo.PLOT_PROJECTED ) dos.writeInt( proj_dir );
      BrushManager.mPointLib.toDataStream( dos );
      BrushManager.mLineLib.toDataStream( dos );
      BrushManager.mAreaLib.toDataStream( dos );

      dos.write('I');
      dos.writeFloat( bbox.left );
      dos.writeFloat( bbox.top );
      dos.writeFloat( bbox.right );
      dos.writeFloat( bbox.bottom );
      dos.writeInt( 0 ); // null north

      for ( DrawingPath p : paths ) {
        if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) continue; // safety check: should not happen
        p.toDataStream( dos );
      }
      // synchronized( userstations ) { // user stations are always exported to data stream
      //   for ( DrawingStationPath sp : userstations ) {
      //     sp.toDataStream( dos );
      //   }
      // }
      dos.write('F'); // final: bbox and autostations (reading can skip all that follows)

      // if ( TDSetting.mAutoStations ) {
      //   synchronized( stations ) {
      //     for ( DrawingStationName st : stations ) {
      //       NumStation station = st.getNumStation();
      //       if ( station != null && station.barriered() ) continue;
      //       if ( bbox.left > st.cx || bbox.right  < st.cx ) continue;
      //       if ( bbox.top  > st.cy || bbox.bottom < st.cy ) continue;
      //       st.toDataStream( dos );
      //     }
      //   }
      // }

      dos.write('E'); // end
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  // this is called by DrawingCommandManager
  static void exportDataStream(
      int type,
      DataOutputStream dos,
      String scrap_name,
      int proj_dir,
      RectF bbox,
      DrawingPath north,
      final List<ICanvasCommand> cstack,
      final List<DrawingStationPath> userstations,
      final List<DrawingStationName> stations )
  {
    // Log.v("DistoX", "cstack size " + cstack.size() );
    try { 
      dos.write( 'V' ); // version
      dos.writeInt( TopoDroidApp.VERSION_CODE );
      dos.write( 'S' );
      dos.writeUTF( scrap_name );
      dos.writeInt( type );
      if ( type == PlotInfo.PLOT_PROJECTED ) dos.writeInt( proj_dir );
      BrushManager.mPointLib.toDataStream( dos );
      BrushManager.mLineLib.toDataStream( dos );
      BrushManager.mAreaLib.toDataStream( dos );

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

      synchronized( cstack ) {
        for ( ICanvasCommand cmd : cstack ) {
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath p = (DrawingPath) cmd;
          if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) continue; // safety check: should not happen
          p.toDataStream( dos );
        }
      }
      synchronized( userstations ) { // user stations are always exported to data stream
        for ( DrawingStationPath sp : userstations ) {
          sp.toDataStream( dos );
        }
      }
      dos.write('F'); // final: bbox and autostations (reading can skip all that follows)

      if ( TDSetting.mAutoStations ) {
        synchronized( stations ) {
          for ( DrawingStationName st : stations ) {
            NumStation station = st.getNumStation();
            if ( station != null && station.barriered() ) continue;
            if ( bbox.left > st.cx || bbox.right  < st.cx ) continue;
            if ( bbox.top  > st.cy || bbox.bottom < st.cy ) continue;
            st.toDataStream( dos );
          }
        }
      }
      dos.write('E'); // end
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  // @param name filename without extension .th2
  static private void exportTherionHeader1( BufferedWriter out, int type, RectF bbox, String name ) throws IOException
  {
    out.write("encoding utf-8");
    out.newLine();
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("##XTHERION## xth_me_area_adjust %.1f %.1f %.1f %.1f\n", bbox.left*6, 400-bbox.bottom*6, bbox.right*6, 400-bbox.top*6 );
    pw.format("##XTHERION## xth_me_area_zoom_to 25\n");
    if ( TDSetting.mTherionXvi ) {
      // xx vsb gamma - yy XVIroot
      pw.format("##XTHERION## xth_me_image_insert {%.2f 1 1.0} {%.2f 0} %s.xvi 0 {}\n",
		      TDSetting.mToTherion*DrawingUtil.CENTER_X, -TDSetting.mToTherion*DrawingUtil.CENTER_Y, name );
      // Log.v("DistoXX", "bbox " + bbox.left + " " + bbox.top + " - " + bbox.right + " " + bbox.bottom );
    }
    pw.format("\n");
    pw.format("# %s created by TopoDroid v. %s\n\n", TDUtil.currentDate(), TopoDroidApp.VERSION );
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
  
  static private void exportTherionHeader3( BufferedWriter out,
         int type, String scrap_name, String proj_name, int project_dir,
         boolean do_north, float x1, float y1, float x2, float y2 ) throws IOException
  {
    float oneMeter  = DrawingUtil.SCALE_FIX * TDSetting.mToTherion;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if (    type == PlotInfo.PLOT_SECTION
         || type == PlotInfo.PLOT_H_SECTION 
         || type == PlotInfo.PLOT_X_SECTION ) {
      if ( do_north ) { // H_SECTION (horizontal section) : north line is 5 m long
        pw.format("scrap %s -projection %s -scale [%.0f %.0f %.0f %.0f 0 5 0 0 m]", scrap_name, proj_name, 
          x1*TDSetting.mToTherion, -y1*TDSetting.mToTherion, x2*TDSetting.mToTherion, -y2*TDSetting.mToTherion );
      } else {
        pw.format("scrap %s -projection %s -scale [0 0 %.0f 0 0 0 1 0 m]", scrap_name, proj_name, oneMeter );
      }
    } else if ( type == PlotInfo.PLOT_PROJECTED ) {
      pw.format("scrap %s -projection [%s %d] -scale [0 0 %.0f 0 0 0 1 0 m]", scrap_name, proj_name, project_dir, oneMeter );
    } else {
      pw.format("scrap %s -projection %s -scale [0 0 %.0f 0 0 0 1 0 m]", scrap_name, proj_name, oneMeter );
    }
    out.write( sw.getBuffer().toString() );
    out.newLine();
    out.newLine();
  }

  static private void exportTherionClose( BufferedWriter out ) throws IOException
  {
    out.newLine();
    out.newLine();
    out.write("endscrap");
    out.newLine();
  }

  // FIXME DataHelper and SID are necessary to export splays by the station
  //
  // @param full_name   name of the scrap (= file_name without extension)
  // @param proj_name   name of the projection
  //
  static void exportTherion( int type, BufferedWriter out, String full_name, String proj_name, int project_dir,
        RectF bbox,
        DrawingPath north,
        final List<ICanvasCommand> cstack,
        final List<DrawingStationPath> userstations,
        final List<DrawingStationName> stations,
        final List<DrawingPath> splays )
  {
    try { 
      exportTherionHeader1( out, type, bbox, full_name );
      // exportTherionHeader2( out );
      if ( north != null ) { 
        exportTherionHeader3( out, type, full_name, proj_name, 0, true, north.x1, north.y1, north.x2, north.y2 );
      } else {
        exportTherionHeader3( out, type, full_name, proj_name, project_dir, false, 0, 0, 0, 0 );
      }
        
      synchronized( cstack ) {
        for ( ICanvasCommand cmd : cstack ) {
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath p = (DrawingPath) cmd;
          if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
            DrawingPointPath pp = (DrawingPointPath)p;
            String pp_str = pp.toTherion();
	    if ( pp_str != null ) {
              out.write( pp_str );
              out.newLine();
	    }
          } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) { // should never happen
            // if ( ! TDSetting.mAutoStations ) {
            //   DrawingStationPath st = (DrawingStationPath)p;
            //   String st_str = st.toTherion();
	    //   if ( st_str != null ) {
            //     out.write( st_str );
            //     out.newLine();
            //   }
	    // }
          } else if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath lp = (DrawingLinePath)p;
            if ( lp.size() > 1 ) {
              String lp_str = lp.toTherion();
	      if ( lp_str != null ) {
                out.write( lp_str );
                out.newLine();
              }
	    }
          } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath ap = (DrawingAreaPath)p;
            if ( ap.size() > 2 ) {
              String ap_str = ap.toTherion();
	      if ( ap_str != null ) {
                out.write( ap_str );
                out.newLine();
              }
	    }
	  }
        }
      }
      out.newLine();

      if ( TDSetting.mTherionSplays ) {
        float toTherion = TDSetting.mToTherion;
        StringWriter sw = new StringWriter();
        PrintWriter pw  = new PrintWriter(sw);
	       synchronized( splays ) {
          for ( DrawingPath splay : splays ) {
            // if ( bbox.left > splay.right  || bbox.right  < splay.left ) continue;
            // if ( bbox.top  > splay.bottom || bbox.bottom < splay.top  ) continue;
            if ( splay.intersects( bbox ) ) {
              pw.format("line u:splay -visibility off\n");
              pw.format( Locale.US, "  %.2f %.2f\n  %.2f %.2f\n", splay.x1*toTherion, -splay.y1*toTherion, splay.x2*toTherion, -splay.y2*toTherion );
              pw.format("endline\n");
            }
          }
	}
        out.write( sw.toString() );
        out.newLine();
      }

      if ( TDSetting.mAutoStations ) {
        synchronized( stations ) {
          for ( DrawingStationName st : stations ) {
            NumStation station = st.getNumStation();
            if ( station != null && station.barriered() ) continue;
            // FIXME if station is in the convex hull (bbox) of the lines
            if ( bbox.left > st.cx || bbox.right  < st.cx ) continue;
            if ( bbox.top  > st.cy || bbox.bottom < st.cy ) continue;
	    String st_str = st.toTherion();
	    if ( st_str != null ) {
              out.write( st_str );
              out.newLine();
	    }
/*
 * this was to export splays by the station instead of all of them
 *
            if ( TDSetting.mTherionSplays ) {
              float th = TDSetting.mToTherion;
              float x = st.cx * th;
              float y = - st.cy * th;
              th *= DrawingUtil.SCALE_FIX;
              List< DBlock > blks = dh.selectSplaysAt( sid, st.getName(), false );
              if ( type == PlotInfo.PLOT_PLAN ) {
                for ( DBlock blk : blks ) {
                  float h = blk.mLength * TDMath.cosd( blk.mClino ) * th;
                  float e = h * TDMath.sind( blk.mBearing );
                  float n = h * TDMath.cosd( blk.mBearing );
                  out.write( "line splay\n" );
                  out.write( String.format(Locale.US, "  %.2f %.2f\n  %.2f %.2f\n", x, y, x+e, y+n ) );
                  out.write( "endline\n" );
                }
              } else if ( PlotInfo.isProfile( type ) ) {
                for ( DBlock blk : blks ) {
                  float v = blk.mLength * TDMath.sind( blk.mClino ) * th;
                  float h = blk.mLength * TDMath.cosd( blk.mClino ) * th * blk.getReducedExtend();
                  out.write( "line splay\n" );
                  out.write( String.format(Locale.US, "  %.2f %.2f\n  %.2f %.2f\n", x, y, x+h, y+v ) );
                  out.write( "endline\n" );
                }
              } else if ( PlotInfo.isSection( type ) ) {
                for ( DBlock blk : blks ) {
                  float d = blk.mLength;
                  float b = blk.mBearing;
                  float c = blk.mClino;
                  float e = blk.getReducedExtend();
                }
              }
            }
*/
          }
	}
      } else {
        synchronized( userstations ) {
          for ( DrawingStationPath sp : userstations ) {
            String sp_str = sp.toTherion();
	    if ( sp_str != null ) {
              out.write( sp_str );
              out.newLine();
            }
	  }
        }
      }
      exportTherionClose( out );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  // FIXME DataHelper and SID are necessary to export splays by the station
  // @param full_name    filename without extension
  static void exportTherionMultiScrap( int type, BufferedWriter out, String full_name, String proj_name, int project_dir,
        // RectF bbox,
        // DrawingPath north, // no x-section
        final List<ICanvasCommand> cstack,
        final List<DrawingStationPath> userstations,
        final List<DrawingStationName> stations,
        final List<DrawingPath> splays )
  {
    // Log.v("DistoXX", "export multiscrap type " + type + " proj " + proj_name );
    class XSectionScrap
    {
      String name; // scrap name
      float x, y;  // offset
  
      XSectionScrap( String nn, float xx, float yy )
      {
        name = nn;
        x = xx;
        y = yy;
      }
    }

    // ArraySet<String> plots = new ArraySet<String>(); // need API-23
    int NPLOTS = 8;
    int nplots = 0;
    String[] plots = new String[NPLOTS];

    ArrayList< XSectionScrap> xsections = new ArrayList<XSectionScrap>();

    float xmin=1000000f, xmax=-1000000f, 
          ymin=1000000f, ymax=-1000000f;
    synchronized( cstack ) {
      for ( ICanvasCommand cmd : cstack ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath p = (DrawingPath) cmd;
        // RectF bbox = p.mBBox;
        if ( p.left   < xmin ) xmin = p.left;
        if ( p.right  > xmax ) xmax = p.right;
        if ( p.top    < ymin ) ymin = p.top;
        if ( p.bottom > ymax ) ymax = p.bottom;

	if ( p.mPlotName != null ) {
	  // Log.v("DistoXX", "path with plot-name " + p.mPlotName + " type " + p.mType + " nr plots " + nplots );
	  int k=0;
	  for ( ; k<nplots; ++k ) if ( plots[k].equals( p.mPlotName ) ) break;
	  if ( k == nplots ) {
	    if ( nplots == NPLOTS ) {
              NPLOTS += 8;
              String[] tmp = new String[NPLOTS];
	      for ( int j=0; j<nplots; ++j ) tmp[j] = plots[j];
              plots = tmp;
	    }
	    plots[k] = p.mPlotName;
	    nplots ++;
	  }
	} else {
	  TDLog.Error("path with no plot-name, type " + p.mType );
	}
      }
      RectF bbox = new RectF( xmin, ymin, xmax, ymax ); // left top right bottom

      // Log.v("DistoXX", "export th2 multiscrap nr. " + nplots + "/" + plots.length + " cstack " + cstack.size() );
      // Log.v("DistoXX", "export th2 multiscrap bbox X " + xmin + " " + xmax  + " Y " + ymin + " " + ymax );

      try { 
        exportTherionHeader1( out, type, bbox, full_name );
        // exportTherionHeader2( out );
        for ( int k=0; k<nplots; ++k ) {
          String plot = plots[k]; 
          // if ( north != null ) { 
          //   exportTherionHeader3( out, type, full_name, proj_name, 0, true, north.x1, north.y1, north.x2, north.y2 );
          // } else {
            exportTherionHeader3( out, type, plot, proj_name, project_dir, false, 0, 0, 0, 0 );
          // }

          synchronized( cstack ) {
            for ( ICanvasCommand cmd : cstack ) {
              if ( cmd.commandType() != 0 ) continue;
              DrawingPath p = (DrawingPath) cmd;
              if ( ! plot.equals( p.mPlotName ) ) continue;
              if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
                DrawingPointPath pp = (DrawingPointPath)p;
                String pp_str = pp.toTherion();
                if ( pp_str != null ) {
                  out.write( pp_str );
                  out.newLine();
                }
		if ( BrushManager.isPointSection( pp.mPointType ) ) {
		  if ( TDSetting.mAutoXSections ) {
                    String name = pp.getOption("-scrap");  // xsection name
		    // Log.v("DistoXX", "multiscrap add x-section " + name );
		    if ( name != null && name.length() > 0 ) xsections.add( new XSectionScrap( name, pp.cx, pp.cy ) );
		  }
		}
              } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) { // should never happen
                // if ( ! TDSetting.mAutoStations ) {
                //   DrawingStationPath st = (DrawingStationPath)p;
                //   String st_str = st.toTherion();
                //   if ( st_str != null ) {
                //     out.write( st_str );
                //     out.newLine();
                //   }
                // }
              } else if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
                DrawingLinePath lp = (DrawingLinePath)p;
                if ( lp.size() > 1 ) {
                  String lp_str = lp.toTherion();
                  if ( lp_str != null ) {
                    out.write( lp_str );
                    out.newLine();
                  }
                }
              } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
                DrawingAreaPath ap = (DrawingAreaPath)p;
                if ( ap.size() > 2 ) {
                  String ap_str = ap.toTherion();
                  if ( ap_str != null ) {
                    out.write( ap_str );
                    out.newLine();
                  }
                }
              }
            }
            out.newLine();
          }

          // if ( TDSetting.mTherionSplays ) {
          //   float toTherion = TDSetting.mToTherion;
          //   StringWriter sw = new StringWriter();
          //   PrintWriter pw  = new PrintWriter(sw);
          //          synchronized( splays ) {
          //     for ( DrawingPath splay : splays ) {
          //       // if ( bbox.left > splay.right  || bbox.right  < splay.left ) continue;
          //       // if ( bbox.top  > splay.bottom || bbox.bottom < splay.top  ) continue;
          //       if ( splay.intersects( bbox ) ) {
          //         pw.format("line u:splay -visibility off\n");
          //         pw.format( Locale.US, "  %.2f %.2f\n  %.2f %.2f\n", splay.x1*toTherion, -splay.y1*toTherion, splay.x2*toTherion, -splay.y2*toTherion );
          //         pw.format("endline\n");
          //       }
          //     }
          //   }
          //   out.write( sw.toString() );
          //   out.newLine();
          // }

          // if ( TDSetting.mAutoStations ) {
          //   synchronized( stations ) {
          //     for ( DrawingStationName st : stations ) {
          //       NumStation station = st.getNumStation();
          //       if ( station != null && station.barriered() ) continue;
          //       // FIXME if station is in the convex hull (bbox) of the lines
          //       if ( bbox.left > st.cx || bbox.right  < st.cx ) continue;
          //       if ( bbox.top  > st.cy || bbox.bottom < st.cy ) continue;
          //       String st_str = st.toTherion();
          //       if ( st_str != null ) {
          //         out.write( st_str );
          //         out.newLine();
          //       }
          //     }
          //   }
          // } else {
            synchronized( userstations ) {
              for ( DrawingStationPath sp : userstations ) {
                String sp_str = sp.toTherion();
                if ( sp_str != null ) {
                  out.write( sp_str );
                  out.newLine();
                }
              }
            }
          // }
          exportTherionClose( out );
        }
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }

    // Log.v("DistoXX", "multiscrap sections " + xsections.size() );
    for ( XSectionScrap xsection : xsections ) { // write xsection scraps
      File file = new File( TDPath.getTdrFileWithExt( xsection.name ) );
      dataStreamToTherion( file, out, null, null, false, true, xsection.x, xsection.y );
    }
  }

  // @param fullname  file name without extension (= scrap name)
  // static public void dataStreamToTherion( File file, BufferedWriter out, String fullname, RectF bbox, boolean endscrap )
  // {
  //   dataStreamToTherion( file, out, fullname, bbox, true, endscrap, 0, 0 ); // true = beginheader
  // }

  // bbox != null  <==>  begeinheader true
  // @param file_name  filename without extension
  static private void dataStreamToTherion( File file, BufferedWriter out, String file_name, RectF bbox, 
                                          boolean beginheader, boolean endscrap,
                                          float xoff, float yoff )
  {
    int version = 0;
    boolean in_scrap = false;

    boolean do_north = false;
    float north_x1=xoff, north_y1=yoff, north_x2=xoff, north_y2=yoff;

    String name = "";
    int type = 0;
    boolean project = false;
    int project_dir = 0;
    String points = "";
    String lines  = "";
    String areas  = "";
    String th_str;

    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    {
      try {
        TDLog.Log( TDLog.LOG_IO, "tdr to Therion. file " + file.getPath() );
        FileInputStream fis = new FileInputStream( file );
        DataInputStream dis = new DataInputStream( fis );
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
                if ( bbox != null ) exportTherionHeader1( out, type, bbox, file_name );
                // exportTherionHeader2( out, points, lines, areas );
                String proj = PlotInfo.projName[ type ];
                exportTherionHeader3( out, type, name, proj, project_dir, do_north, north_x1, north_y1, north_x2, north_y2 );
                // if ( do_north ) { 
                //   exportTherionHeader3( out, type, name, proj, 0, true, north_x1, north_y1, north_x2, north_y2 );
                // } else {
                //   exportTherionHeader3( out, type, name, proj, project_dir, false, 0, 0, 0, 0 );
                // }
                in_scrap = true;
              }
              break;
            case 'S':
              {
                name = dis.readUTF();
                type = dis.readInt();
                if ( type == PlotInfo.PLOT_PROJECTED ) project_dir = dis.readInt();
                // read palettes
                points = dis.readUTF();
                lines = dis.readUTF();
                areas = dis.readUTF();
              }
              break;
            case 'P':
              th_str = DrawingPointPath.loadDataStream( version, dis, xoff, yoff /*, null */ ).toTherion();
	      if ( th_str != null ) out.write( th_str );
              break;
            case 'T':
              th_str = DrawingLabelPath.loadDataStream( version, dis, xoff, yoff ).toTherion();
	      if ( th_str != null ) out.write( th_str );
              break;
            case 'L':
              th_str = DrawingLinePath.loadDataStream( version, dis, xoff, yoff /*, null */ ).toTherion();
	      if ( th_str != null ) out.write( th_str );
              break;
            case 'A':
              th_str = DrawingAreaPath.loadDataStream( version, dis, xoff, yoff /*, null */ ).toTherion();
	      if ( th_str != null ) out.write( th_str );
              break;
            case 'J':
              th_str = DrawingSpecialPath.loadDataStream( version, dis, xoff, yoff ).toTherion(); // empty string anyways
	      if ( th_str != null ) out.write( th_str );
              break;
            case 'U':
              th_str = DrawingStationPath.loadDataStream( version, dis ).toTherion();
	      if ( th_str != null ) out.write( th_str );
              break;
            case 'X':
              // NOTE need to check XSection ??? STATION_XSECTION
              th_str = DrawingStationName.loadDataStream( version, dis ).toTherion();
	      if ( th_str != null ) out.write( th_str );
              break;
            case 'F':
              break; // continue parsing stations
            case 'E':
              todo = false;
              break;
            default:
              todo = false;
              TDLog.Error( "ERROR " + file.getName() + " bad input (2) " + what );
              break;
          } 
        }
        if (endscrap ) exportTherionClose( out );
        dis.close();
        fis.close();
      } catch ( FileNotFoundException e ) {
        // this is OK
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
  }
  
  // -----------------------------------------------------------------------------
  // CSURVEY

  static void doExportCsxXSection( PrintWriter pw, String filename,
                                   String survey, String cave, String branch, /* String session, */ String bind /* , DrawingUtil drawingUtil */ )
  {
    File file = new File( filename );
    if ( ! file.exists() ) return;
    int version = 0;
    boolean in_scrap = false;
    String name = "";
    int type = 0;
    String points = "";
    String lines  = "";
    String areas  = "";

    ArrayList<DrawingPath> paths = new ArrayList<>();

    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    {
      try {
        TDLog.Log( TDLog.LOG_IO, "export cSurvey. X-section file " + filename );
        FileInputStream fis = new FileInputStream( file );
        DataInputStream dis = new DataInputStream( fis );
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
                in_scrap = true;
              }
              break;
            case 'S':
              {
                name = dis.readUTF();
                type = dis.readInt();
                if ( ! PlotInfo.isAnySection( type ) ) {
                  dis.close();
                  fis.close();
                  return;
                }
                // project_dir = dis.readInt();
                // read palettes
                dis.readUTF();
                dis.readUTF();
                dis.readUTF();
              }
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
              DrawingStationPath.loadDataStream( version, dis );
              break;
            case 'X':
              // NOTE need to check XSection ??? STATION_XSECTION
              DrawingStationName.loadDataStream( version, dis );
              break;
            case 'F':
            case 'E':
              todo = false;
              break;
            default:
              todo = false;
              TDLog.Error( "ERROR " + filename + " bad input (4) " + what );
              break;
          } 
        }
        dis.close();
        fis.close();
      } catch ( FileNotFoundException e ) {
        // this is OK
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
    doExportAsCsx( pw, survey, cave, branch, /* session, */ bind, paths, null, null /* , drawingUtil */ ); // all_sections=null, sections=null
  }

  static void doExportAsCsx( PrintWriter pw, String survey, String cave, String branch, /* String session, */ String bind,
                             List<DrawingPath> paths, List< PlotInfo > all_sections, List< PlotInfo > sections
			     /* , DrawingUtil mDrawingUtil */ )
  {
    int csxIndex = 0;
    pw.format("    <layers>\n");

    // LAYER 0: images and sketches
    pw.format("      <layer name=\"Base\" type=\"0\">\n");
    pw.format("         <items>\n");
    pw.format("         </items>\n");
    pw.format("      </layer>\n");

    // LAYER 1: soil areas
    pw.format("      <layer name=\"Soil\" type=\"1\">\n");
    pw.format("        <items>\n");
    for ( DrawingPath p : paths ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
        DrawingAreaPath ap = (DrawingAreaPath)p;
        if ( BrushManager.getAreaCsxLayer( ap.mAreaType ) != 1 ) continue;
        ap.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ ); 
      }
    }
    pw.format("        </items>\n");
    pw.format("      </layer>\n");

    // LAYER 2: 
    pw.format("      <layer name=\"Water and floor morphologies\" type=\"2\">\n");
    pw.format("        <items>\n");
    for ( DrawingPath p : paths ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
        DrawingLinePath lp = (DrawingLinePath)p;
        if ( BrushManager.getLineCsxLayer( lp.mLineType ) != 2 ) continue;
        lp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
      } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
        DrawingAreaPath ap = (DrawingAreaPath)p;
        if ( BrushManager.getAreaCsxLayer( ap.mAreaType ) != 2 ) continue;
        ap.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ ); 
      } 
    }
    pw.format("        </items>\n");
    pw.format("      </layer>\n");

    // LAYER 3
    pw.format("      <layer name=\"Rocks and concretions\" type=\"3\">\n");
    pw.format("        <items>\n");
    for ( DrawingPath p : paths ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
        DrawingLinePath lp = (DrawingLinePath)p;
        if ( BrushManager.getLineCsxLayer( lp.mLineType ) != 3 ) continue;
        lp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
      }
    }
    pw.format("        </items>\n");
    pw.format("      </layer>\n");

    // LAYER 4
    pw.format("      <layer name=\"Ceiling morphologies\" type=\"4\">\n");
    pw.format("        <items>\n");
    for ( DrawingPath p : paths ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
        DrawingLinePath lp = (DrawingLinePath)p;
        if ( BrushManager.getLineCsxLayer( lp.mLineType ) != 4 ) continue;
        lp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
      }
    }
    pw.format("        </items>\n");
    pw.format("      </layer>\n");

    // LAYER 5:
    pw.format("      <layer name=\"Borders\" type=\"5\">\n");
    pw.format("        <items>\n");
    for ( DrawingPath p : paths ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
        DrawingLinePath lp = (DrawingLinePath)p;
        if ( BrushManager.getLineCsxLayer( lp.mLineType ) != 5 ) continue;
        lp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
      }
      // if ( lp.lineType() == BrushManager.mLineLib.mLineWallIndex ) {
      //   // linetype: 0 line, 1 spline, 2 bezier
      //   pw.format("          <item layer=\"5\" name=\"\" type=\"4\" category=\"1\" linetype=\"0\" mergemode=\"0\">\n");
      //   pw.format("            <pen type=\"1\" />\n");
      //   pw.format("            <points data=\"");
      //   ArrayList< LinePoint > pts = lp.mPoints;
      //   boolean b = true;
      //   for ( LinePoint pt : pts ) {
      //     float x = DrawingWindow.sceneToWorldX( pt.x );
      //     float y = DrawingWindow.sceneToWorldY( pt.y );
      //     pw.format(Locale.US, "%.2f %.2f ", x, y );
      //     if ( b ) { pw.format("B "); b = false; }
      //   }
      //   pw.format("\" />\n");
      //   pw.format("          </item>\n");
      // }
    }
    pw.format("        </items>\n");
    pw.format("      </layer>\n");

    // LAYER 6: signs and texts
    pw.format("      <layer name=\"Signs\" type=\"6\">\n");
    pw.format("        <items>\n");
    for ( DrawingPath p : paths ) {
      if ( p.mType != DrawingPath.DRAWING_PATH_POINT ) continue;
      DrawingPointPath pp = (DrawingPointPath)p;
      if ( BrushManager.getPointCsxLayer( pp.mPointType ) != 6 ) continue;

      // section points are special
      if ( all_sections != null && BrushManager.isPointSection( pp.mPointType ) ) {
	if ( TDSetting.mAutoXSections ) {
          // Log.v("DistoX", "Section point <" + pp.mOptions + ">");
          // option: -scrap survey-xx#
          // FIXME GET_OPTION
          PlotInfo section = null;
          String scrap_name = pp.getOption( "-scrap" );
          if ( scrap_name != null ) {
            for ( PlotInfo s : all_sections ) {
              if ( scrap_name.endsWith( s.name ) ) {
                // String name = survey + "-" + s.name; // scrap filename
                section = s;
                section.csxIndex = csxIndex;
                if ( sections != null ) sections.add( section );
                break;
              }
            }
          }
          // String[] vals = pp.mOptions.split(" ");
          // int k0 = vals.length;
          // for ( int k = 0; k < k0; ++k ) {
          //   if ( vals[k].equals("-scrap") ) {
          //     for ( ++k; k < k0; ++k ) {
          //       if ( vals[k].length() > 0 ) break;
          //     }
          //     if ( k < k0 ) {
          //       for ( PlotInfo s : all_sections ) {
          //         if ( vals[k].endsWith( s.name ) ) {
          //           // String name = survey + "-" + s.name; // scrap filename
          //           section = s;
          //           section.csxIndex = csxIndex;
          //           if ( sections != null ) sections.add( section );
          //           break;
          //         }
          //       }
          //     }
          //   }
          // }

          if ( section != null ) {
            // Log.v("DistoX", "section " + section.name + " " + section.nick );
            // special toCsurvey for cross-section points
            float x = DrawingUtil.sceneToWorldX( pp.cx, pp.cy ); // convert to world coords.
            float y = DrawingUtil.sceneToWorldY( pp.cx, pp.cy );
            String text = ( section.nick == null || section.nick.length() == 0 )? section.name : section.nick;
            pw.format("  <item layer=\"6\" cave=\"%s\" branch=\"%s\" type=\"9\" category=\"96\" direction=\"0\" ", cave, branch );
            pw.format("text=\"%s\" textdistance=\"2\" crosswidth=\"4\" crossheight=\"4\" name=\"%s\" ", text, section.name );
            // pw.format("crosssection=\"%d\" ", section.csxIndex );
            if ( section.name.startsWith("xs-") || section.name.startsWith("xh-") ) {
              pw.format("station=\"%s\" ", section.name.substring(3) ); // == section.start
            } else {
              pw.format("stationfrom=\"%s\" stationto=\"%s\" ", section.start, section.view );
            }
            // pw.format(" segment=\"%s\"", "undefined" );
            pw.format(Locale.US, "splayborderprojectionangle=\"%.2f\" splayborderprojectionvangle=\"%.2f\" id=\"%d\">\n",
              section.azimuth, section.clino, section.csxIndex );
            pw.format(Locale.US, "<points data=\"%.2f %.2f \" />\n", x, y );
            pw.format("    <font type=\"4\" />\n");
            pw.format("  </item>\n");
          } else {
            TDLog.Error("xsection not found. Name: " + ((scrap_name == null)? "null" : scrap_name) );
          }
	}
      } else {
        pp.toCsurvey( pw, survey, cave, branch, bind /* , mDrawingUtil */ );
      }
      ++ csxIndex;
      
    }
    pw.format("        </items>\n");
    pw.format("      </layer>\n");
    pw.format("    </layers>\n");
  }

}
