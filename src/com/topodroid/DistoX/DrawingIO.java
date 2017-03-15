/* @file DrawingIO.java    
 *
 * @author marco corvi
 * @date nov 2015
 *
 * @brief TopoDroid drawing: drawing I/O
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;

import java.util.List;
import java.util.HashMap;
import java.util.Locale;

import android.graphics.RectF;

import android.util.Log;

class DrawingIO
{
  private static float toTherion = TDConst.TO_THERION;
  private static float oneMeter  = DrawingUtil.SCALE_FIX * toTherion;

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
      line.replaceAll(" *", " ");
      // line.replaceAll("\\s+", " ");
    }
    return line;
  } 

  public static boolean doLoadTherion( DrawingSurface surface,
                                String filename,
                                float dx, float dy,
                                SymbolsPalette missingSymbols,
                                SymbolsPalette localPalette )
  {
    float x, y, x1, y1, x2, y2;
    boolean is_not_section = true;

    // TDLog.Log( TDLog.LOG_PLOT, "Load Therion file " + filename + " delta " + dx + " " + dy );
    // BrushManager.makePaths( );
    BrushManager.resetPointOrientations();

    // TDLog.Log( TDLog.LOG_PLOT, "after reset 0: " + BrushManager.mOrientation[0]
    //                      + " 7: " + BrushManager.mOrientation[7] );

    synchronized( TDPath.mTherionLock ) {
      try {
        FileReader fr = new FileReader( filename );
        BufferedReader br = new BufferedReader( fr );
        String line = null;
        while ( (line = readLine(br)) != null ) {
          int comment = line.indexOf('#');
          if ( comment == 0 ) {
            if ( line.startsWith( "#P " ) ) { // POINT PALETTE
              if ( localPalette != null ) {
                localPalette.mPalettePoint.clear();
                localPalette.addPointFilename( "user" );
                String[] syms = line.split( " " );
                for ( int k=1; k<syms.length; ++k ) {
                  if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addPointFilename( syms[k] );
                }
                BrushManager.mPointLib.makeEnabledListFromPalette( localPalette );
              }
            } else if ( line.startsWith( "#L " ) ) { // LINE PALETTE
              if ( localPalette != null ) {
                localPalette.mPaletteLine.clear();
                localPalette.addLineFilename("user");
                String[] syms = line.split( " " );
                for ( int k=1; k<syms.length; ++k ) {
                  if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addLineFilename( syms[k] );
                }
                BrushManager.mLineLib.makeEnabledListFromPalette( localPalette );
              }
            } else if ( line.startsWith( "#A " ) ) { // AREA PALETTE
              if ( localPalette != null ) {
                localPalette.mPaletteArea.clear();
                localPalette.addAreaFilename("user");
                String[] syms = line.split( " " );
                for ( int k=1; k<syms.length; ++k ) {
                  if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addAreaFilename( syms[k] );
                }
                BrushManager.mAreaLib.makeEnabledListFromPalette( localPalette );
              }
            }
            continue;
          } else if (comment > 0 ) {
            line = line.substring( 0, comment );
          }
          if ( line.length() == 0 /* || line.charAt(0) == '#' */ ) {
            continue;
          }

          // TDLog.Log( TDLog.LOG_PLOT, "  line: >>" + line + "<<");
          line = line.replaceAll("\\s+", " ");
          String[] vals = line.split( " " );
          // FIXME assert( vals.length > 0 );
          if ( vals[0].equals( "scrap" ) ) {
            if ( vals.length < 4 ) {
              TDLog.Error( "bad scrap cmd: " + line );
            } else {
              // String name = vals[1];
              // skip "-projection" vals[2]
              is_not_section = ! vals[3].equals("none");
            }
          } else if ( vals[0].equals( "point" ) ) {
            // ****** THERION POINT ********************************** point X Y type [options]
            if ( vals.length < 4 ) {
              TDLog.Error( "bad point cmd: " + line );
            } else {
              int ptType = BrushManager.mPointLib.mSymbolNr;
              boolean has_orientation = false;
              float orientation = 0.0f;
              int scale = DrawingPointPath.SCALE_M;
              String options = null;

              try {
                x = dx + Float.parseFloat( vals[1] ) / TDConst.TO_THERION;
                y = dy - Float.parseFloat( vals[2] ) / TDConst.TO_THERION;
              } catch ( NumberFormatException e ) {
                TDLog.Error( "Therion Point error (number fmt) <" + line + ">" );
                continue;
              }
              String type = vals[3];
              String label_text = null;
              int k = 4;
              if ( type.equals( "station" ) ) {
                if ( ! TDSetting.mAutoStations ) {
                  if ( vals.length > k+1 && vals[k].equals( "-name" ) ) {
                    String name = vals[k+1];
                    DrawingStationPath station_path = new DrawingStationPath( name, x, y, scale );
                    surface.addDrawingPath( station_path );
                  }
                }
                continue;
              }
              while ( vals.length > k ) { 
                if ( vals[k].equals( "-orientation" ) ) {
                  try {
                    orientation = Float.parseFloat( vals[k+1] );
                    has_orientation = true;
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "Therion Point orientation error : " + line );
                  }
                  k += 2;
                } else if ( vals[k].equals( "-scale" ) ) {
                  // FIXME assert (vals.length > k+1 );
                  if ( vals[k+1].equals("xs") ) {
                    scale = DrawingPointPath.SCALE_XS;
                  } else if ( vals[k+1].equals("s") ) {
                    scale = DrawingPointPath.SCALE_S;
                  } else if ( vals[k+1].equals("l") ) {
                    scale = DrawingPointPath.SCALE_L;
                  } else if ( vals[k+1].equals("xl") ) {
                    scale = DrawingPointPath.SCALE_XL;
                  } 
                  k += 2;
                } else if ( vals[k].equals( "-text" ) ) {
                  // FIXME assert (vals.length > k+1 );
                  label_text = vals[k+1];
                  k += 2;
                  if ( label_text.startsWith( "\"" ) ) {
                    while ( k < vals.length ) {
                      label_text = label_text + " " + vals[k];
                      if ( vals[k].endsWith( "\"" ) ) break;
                      ++ k;
                    }
                    label_text = label_text.replaceAll( "\"", "" );
                    ++ k;
                  }
                } else {
                  options = vals[k];
                  ++ k;
                  while ( vals.length > k ) {
                    options += " " + vals[k];
                    ++ k;
                  }
                }
              }

              BrushManager.mPointLib.tryLoadMissingPoint( type );
              // map pre 3.1.1 thnames to 3.1.1 names
              String thname = type;
              if ( thname.equals( "user" ) )        { thname = "u:user"; }
              else if ( thname.equals( "danger" ) ) { thname = "u:danger"; }
              else if ( thname.equals( "archeo" ) ) { thname = "archeo-material"; }
              ptType = BrushManager.mPointLib.getSymbolIndexByThName( thname );
              // Log.v("DistoX", "type " + type + " thname " + thname + " " + ptType );
              if ( ptType < 0 ) {
                if ( missingSymbols != null ) missingSymbols.addPointFilename( type ); // add "type" to the missing point-types
                ptType = 0; // SymbolPointLibrary.mPointUserIndex; // FIXME
                // continue;
              }

              if ( ptType == BrushManager.mPointLib.mPointLabelIndex ) {
                if ( label_text != null ) {
                  // "danger" is no longer mapped on a label 
                  // if ( label_text.equals( "!" ) ) {    // "danger" point
                  //   DrawingPointPath path = new DrawingPointPath( BrushManager.mPointLib.mPointDangerIndex, x, y, scale, options );
                  //   surface.addDrawingPath( path );
                  // } else {                             // regular label
                    DrawingLabelPath path = new DrawingLabelPath( label_text, x, y, scale, options );
                    if ( has_orientation ) {
                      path.setOrientation( orientation );
                    }
                    surface.addDrawingPath( path );
                  // }
                }
              } else if ( has_orientation && BrushManager.mPointLib.isSymbolOrientable(ptType) ) {
                // TDLog.Log( TDLog.LOG_PLOT, "[2] point " + ptType + " has orientation " + orientation );
                BrushManager.rotateGradPoint( ptType, orientation );
                DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, options );
                surface.addDrawingPath( path );
                BrushManager.rotateGradPoint( ptType, -orientation );
              } else {
                DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, options );
                surface.addDrawingPath( path );
              }
            }
          } else if ( vals[0].equals( "line" ) ) {
            // ********* THERION LINES ************************************************************
            if ( vals.length < 2 ) {
              TDLog.Error( "bad line cmd: " + line );
            } else {
              if ( vals.length >= 6 && vals[1].equals( "border" ) && vals[2].equals( "-id" ) ) { // THERION AREAS
                boolean visible = true;
                // TDLog.Log( TDLog.LOG_PLOT, "area id " + vals[3] );
                if ( vals.length >= 8 && vals[6].equals("-visibility") && vals[7].equals("off") ) {
                  visible = false;
                }
                int arType = BrushManager.mAreaLib.mSymbolNr;
                DrawingAreaPath path = new DrawingAreaPath( arType, vals[3], visible );

                // TODO insert new area-path
                line = readLine( br );
                if ( ! line.equals( "endline" ) ) { 
                  String[] pt = line.split( "\\s+" );
                  try {
                    x = dx + Float.parseFloat( pt[0] ) / TDConst.TO_THERION;
                    y = dy - Float.parseFloat( pt[1] ) / TDConst.TO_THERION;
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "Therion Line error (number fmt) <" + line + ">" );
                    continue;
                  }
                  path.addStartPoint( x, y );

                  while ( (line = readLine( br )) != null ) {
                    if ( line.equals( "endline" ) ) {
                      line = readLine( br ); // area statement
                      String[] vals2 = line.split( " " );
                      if ( vals2.length >= 2 ) {
                        BrushManager.mAreaLib.tryLoadMissingArea( vals2[1] );
                        String thname = vals2[1];
                        if ( thname.equals( "user" ) ) { thname = "u:user"; }
                        arType = BrushManager.mAreaLib.getSymbolIndexByThName( thname );
                        if ( arType < 0 ) {
                          if ( missingSymbols != null ) missingSymbols.addAreaFilename( vals2[1] );
                          arType = 0; // SymbolAreaLibrary.mAreaUserIndex; // FIXME
                          // continue;
                        }
                        // TDLog.Log(TDLog.LOG_PLOT, "set area type " + arType + " " + vals2[1]);
                        double orientation = 0;
                        if ( vals2.length >= 4 && vals2[2].equals("#orientation") ) {
                          try {
                            orientation = Double.parseDouble( vals2[3] );
                          } catch ( NumberFormatException e ) { 
                            TDLog.Error( "Therion Area orientation error <" + line + ">" );
                          }
                        }
                        path.setAreaType( arType );
                        path.setOrientation( orientation );
                        surface.addDrawingPath( path );
                      }
                      line = readLine( br ); // skip two lines
                      line = readLine( br );
                      break;
                    }
                    // TDLog.Log( TDLog.LOG_DEBUG, "  line point: >>" + line + "<<");
                    String[] pt2 = line.split( " " );
                    if ( pt2.length == 2 ) {
                      try {
                        x = dx + Float.parseFloat( pt2[0] ) / TDConst.TO_THERION;
                        y = dy - Float.parseFloat( pt2[1] ) / TDConst.TO_THERION;
                        path.addPoint( x, y );
                        // TDLog.Log( TDLog.LOG_DEBUG, "area pt " + x + " " + y);
                      } catch ( NumberFormatException e ) {
                        TDLog.Error( "Therion Line X-Y error (10) <" + line + ">" );
                        continue;
                      } catch ( ArrayIndexOutOfBoundsException e ) {
                        TDLog.Error( "Therion Line X-Y error (11) " + line );
                        continue;
                      }
                    } else if ( pt2.length == 6 ) {
                      try {
                        x1 = dx + Float.parseFloat( pt2[0] ) / TDConst.TO_THERION;
                        y1 = dy - Float.parseFloat( pt2[1] ) / TDConst.TO_THERION;
                        x2 = dx + Float.parseFloat( pt2[2] ) / TDConst.TO_THERION;
                        y2 = dy - Float.parseFloat( pt2[3] ) / TDConst.TO_THERION;
                        x  = dx + Float.parseFloat( pt2[4] ) / TDConst.TO_THERION;
                        y  = dy - Float.parseFloat( pt2[5] ) / TDConst.TO_THERION;
                        path.addPoint3( x1, y1, x2, y2, x, y );
                        // TDLog.Log( TDLog.LOG_DEBUG, "area pt " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + x + " " + y);
                      } catch ( NumberFormatException e ) {
                        TDLog.Error( "Therion Line X-Y error (12) <" + line + ">" );
                        continue;
                      } catch ( ArrayIndexOutOfBoundsException e ) {
                        TDLog.Error( "Therion Line X-Y error (13) " + line );
                        continue;
                      }
                    }
                  }
                }
              } else { // ********* regular lines
                // FIXME assert (vals.length > 1 );
                // TDLog.Log( TDLog.LOG_PLOT, "line type " + vals[1] );
                boolean closed = false;
                boolean reversed = false;
                int outline = DrawingLinePath.OUTLINE_UNDEF;
                String options = null;
               
                String type = vals[1];
                for (int index = 2; index < vals.length; ++index ) {
                  if ( vals[index] == null || vals[index].length() == 0 ) {
                    continue;
                  }
                  if ( vals[index].equals( "-close" ) ) {
                    ++ index;
                    if ( vals.length > index && vals[index].equals( "on" ) ) {
                      closed = true;
                    }
                  } else if ( vals[index].equals( "-reverse" ) ) {
                    ++ index;
                    if ( vals.length > index && vals[index].equals( "on" ) ) {
                      reversed = true;
                    }
                  } else if ( vals[index].equals( "-outline" ) ) {
                    ++ index;
                    if ( vals.length > index ) {
                      if ( vals[index].equals( "out" ) ) { outline = DrawingLinePath.OUTLINE_OUT; }
                      else if ( vals[index].equals( "in" ) ) { outline = DrawingLinePath.OUTLINE_IN; }
                      else if ( vals[index].equals( "none" ) ) { outline = DrawingLinePath.OUTLINE_NONE; }
                    }
                  } else {
                    if ( options == null ) {
                      options = vals[index];
                    } else {
                      options += " " + vals[index];
                    }
                  } 
                }
                
                int lnTypeMax = BrushManager.mLineLib.mSymbolNr;
                int lnType = lnTypeMax;
                DrawingLinePath path = null;
                BrushManager.mLineLib.tryLoadMissingLine( type );
                String thname = type;
                if ( thname.equals( "user" ) ) { thname = "u:user"; }
                lnType = BrushManager.mLineLib.getSymbolIndexByThName( thname );
                if ( lnType < 0 ) {
                  if ( missingSymbols != null ) missingSymbols.addLineFilename( type );
                  lnType = 0; // SymbolLineLibrary.mLineUserIndex; // FIXME
                  // continue;
                }
                // TODO insert new line-path
                line = readLine( br );
                if ( ! line.equals( "endline" ) ) { 
                  path = new DrawingLinePath( lnType );
                  path.setClosed( closed );
                  path.setReversed( reversed );
                  if ( outline != DrawingLinePath.OUTLINE_UNDEF ) path.mOutline = outline;
                  if ( options != null ) path.setOptions( options );

                  // TDLog.Log( TDLog.LOG_PLOT, "  line start point: <" + line + ">");
                  String[] pt0 = line.split( "\\s+" );
                  try {
                    x = dx + Float.parseFloat( pt0[0] ) / TDConst.TO_THERION;
                    y = dy - Float.parseFloat( pt0[1] ) / TDConst.TO_THERION;
                    path.addStartPoint( x, y );
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "Therion Line X-Y error (1) <" + line + ">" );
                    continue;
                  } catch ( ArrayIndexOutOfBoundsException e ) {
                    TDLog.Error( "Therion Line X-Y error (2) " + line );
                    continue;
                  }
                  // Log.v( "DistoX", "  line start point: <" + line + "> " + x + " " + y );
                  while ( (line = readLine( br )) != null ) {
                    if ( line.indexOf( "l-size" ) >= 0 ) continue;
                    if ( line.equals( "endline" ) ) {
                      if ( path != null ) {
                        if ( type.equals("section") ) { // section line only in non-section scraps
                          if ( is_not_section ) {
                            path.makeStraight( false ); // FIXME true = with arrow
                          }
                        } else {
                          path.computeUnitNormal(); // for section-line already done by makeStraight
                        } 
                        surface.addDrawingPath( path );
                      }
                      break;
                    }
                    if ( path != null ) {
                      // TDLog.Log( TDLog.LOG_PLOT, "  line point: >>" + line + "<<");
                      String[] pt = line.split( " " );
                      if ( pt.length == 2 ) {
                        try {
                          x = dx + Float.parseFloat( pt[0] ) / TDConst.TO_THERION;
                          y = dy - Float.parseFloat( pt[1] ) / TDConst.TO_THERION;
                          path.addPoint( x, y );
                        } catch ( NumberFormatException e ) {
                          TDLog.Error( "Therion Line X-Y error (3) <" + line + ">" );
                          continue;
                        } catch ( ArrayIndexOutOfBoundsException e ) {
                          TDLog.Error( "Therion Line X-Y error (4) " + line );
                          continue;
                        }
                      } else if ( pt.length == 6 ) {
                        try {
                          x1 = dx + Float.parseFloat( pt[0] ) / TDConst.TO_THERION;
                          y1 = dy - Float.parseFloat( pt[1] ) / TDConst.TO_THERION;
                          x2 = dx + Float.parseFloat( pt[2] ) / TDConst.TO_THERION;
                          y2 = dy - Float.parseFloat( pt[3] ) / TDConst.TO_THERION;
                          x  = dx + Float.parseFloat( pt[4] ) / TDConst.TO_THERION;
                          y  = dy - Float.parseFloat( pt[5] ) / TDConst.TO_THERION;
                          path.addPoint3( x1, y1, x2, y2, x, y );
                        } catch ( NumberFormatException e ) {
                          TDLog.Error( "Therion Line X-Y error (5) <" + line + ">" );
                          continue;
                        } catch ( ArrayIndexOutOfBoundsException e ) {
                          TDLog.Error( "Therion Line X-Y error (6) " + line );
                          continue;
                        }
                      }
                    }
                  } // end while ( line-points )
                }
              }
            }
          }
        }
      } catch ( FileNotFoundException e ) {
        // this is OK
      // } catch ( IOException e ) {
      //   e.printStackTrace();
      }
    }
    // remove repeated names

    return (missingSymbols != null )? missingSymbols.isOK() : true;
  }

  // =========================================================================
  // EXPORT 

  // exportTherion calls DrawingSurface' exportTherion, 
  // which calls DrawingCommandManager's exportTherion,
  // which calls the full method exportTherion with the list of sketch items
  //
  // FIXME DataHelper and SID are necessary to export splays by the station
  static public void exportTherion( // DataHelper dh, long sid, 
                       DrawingSurface surface, int type, File file, String fullname, String projname, int proj_dir )
  {
    // Log.v("DistoX", "Export Therion file " + file.getPath() );
    try {
      FileWriter fw = new FileWriter( file );
      BufferedWriter bw = new BufferedWriter( fw );
      surface.exportTherion( /* dh, sid, */ type, bw, fullname, projname, proj_dir );
      bw.flush();
      bw.close();
    } catch ( IOException e ) {
      TDLog.Error( "Export Therion i/o: " + e.getMessage() );
    }
  }

  static public void exportDataStream( DrawingSurface surface, int type, File file, String fullname, int proj_dir )
  {
    try {
      FileOutputStream fos = new FileOutputStream( file );

      // ByteArrayOutputStream bos = new ByteArrayOutputStream( 4096 );
      BufferedOutputStream bfos = new BufferedOutputStream( fos );
      DataOutputStream dos = new DataOutputStream( bfos );
      surface.exportDataStream( type, dos, fullname, proj_dir );
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

  static public int skipTdrHeader( DataInputStream dis )
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
        } else if ( what == 'S' ) {
          flag |= 0x02;
          String name = dis.readUTF();
          type = dis.readInt();
          if ( type == PlotInfo.PLOT_PROFILE ) dir = dis.readInt();
          String lib = dis.readUTF();
          lib = dis.readUTF();
          lib = dis.readUTF();
        } else if ( what == 'I' ) {
          flag |= 0x04;
          x = dis.readFloat();
          y = dis.readFloat();
          x = dis.readFloat();
          y = dis.readFloat();
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

  static public boolean doLoadDataStream( DrawingSurface surface,
                                   String filename,
                                   float dx, float dy,
                                   SymbolsPalette missingSymbols,
                                   SymbolsPalette localPalette,
                                   RectF bbox, boolean complete )
  {
    int version = 0;
    boolean in_scrap = false;
    // BrushManager.makePaths( );
    BrushManager.resetPointOrientations();
    DrawingPath path = null;
    int project_dir = 0;
    float north_x1, north_y1, north_x2, north_y2;

    File file = new File( filename );
    FileInputStream fis = null;
    DataInputStream dis = null;
    synchronized( TDPath.mTherionLock ) {
      try {
        // CACHE check if filename is in the cache: if so use the cache byte array
        // ByteArrayOutputStream bos = mTdrCache.get( file.getName() );
        // if ( bos == null ) {
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
                if ( type == PlotInfo.PLOT_PROFILE ) project_dir = dis.readInt();
                // read palettes
                String points = dis.readUTF();
                String[] vals = points.split(",");
                for ( String val : vals ) if ( val.length() > 0 ) localPalette.addPointFilename( val );
                String lines = dis.readUTF();
                vals = points.split(",");
                for ( String val : vals ) if ( val.length() > 0 ) localPalette.addLineFilename( val );
                String areas = dis.readUTF();
                vals = points.split(",");
                for ( String val : vals ) if ( val.length() > 0 ) localPalette.addAreaFilename( val );
                in_scrap = true;
                // Log.v("DistoX", "TDR type " + type );
              }
              break;
            case 'P':
              path = DrawingPointPath.loadDataStream( version, dis, dx, dy, missingSymbols );
              break;
            case 'T':
              path = DrawingLabelPath.loadDataStream( version, dis, dx, dy );
              break;
            case 'L':
              path = DrawingLinePath.loadDataStream( version, dis, dx, dy, missingSymbols );
              break;
            case 'A':
              path = DrawingAreaPath.loadDataStream( version, dis, dx, dy, missingSymbols );
              break;
            case 'U':
              path = DrawingStationPath.loadDataStream( version, dis ); // consume DrawingStationName data
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
              TDLog.Error( "ERROR bad input (1) " + (int)what );
              break;
          } 
          if ( path != null && in_scrap ) {
            if ( what == 'U' ) {
              surface.addDrawingStationPath( (DrawingStationPath)path );
            } else {
              surface.addDrawingPath( path );
            }
          }
        }
        dis.close();
        if ( fis != null ) fis.close();
      } catch ( FileNotFoundException e ) {
        // this is OK
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      // Log.v("DistoX", "read: " + sb.toString() );
    }
    return (missingSymbols != null )? missingSymbols.isOK() : true;
  }

  static public void exportDataStream( int type, DataOutputStream dos, String scrap_name, int proj_dir, RectF bbox,
      DrawingPath north,
      List<ICanvasCommand> cstack,
      List<DrawingStationPath> userstations,
      List<DrawingStationName> stations )
  {
    try { 
      dos.write( 'V' ); // version
      dos.writeInt( TopoDroidApp.VERSION_CODE );
      dos.write( 'S' );
      dos.writeUTF( scrap_name );
      dos.writeInt( type );
      if ( type == PlotInfo.PLOT_PROFILE ) dos.writeInt( proj_dir );
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
            if ( st.mStation != null && st.mStation.barriered() ) continue;
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

  static private void exportTherionHeader1( BufferedWriter out, int type, RectF bbox ) throws IOException
  {
    out.write("encoding utf-8");
    out.newLine();
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("##XTHERION## xth_me_area_adjust %.1f %.1f %.1f %.1f\n", 
       bbox.left*6, 400-bbox.bottom*6, bbox.right*6, 400-bbox.top*6 );
    pw.format("##XTHERION## xth_me_area_zoom_to 25\n\n");
    pw.format("# %s created by TopoDroid v. %s\n\n", TopoDroidUtil.currentDate(), TopoDroidApp.VERSION );
    out.write( sw.getBuffer().toString() );
  }

  static private void exportTherionHeader2( BufferedWriter out ) throws IOException
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("#P ");
    BrushManager.mPointLib.writePalette( pw );
    pw.format("\n#L ");
    BrushManager.mLineLib.writePalette( pw );
    pw.format("\n#A ");
    BrushManager.mAreaLib.writePalette( pw );
    pw.format("\n");
    out.write( sw.getBuffer().toString() );
  }

  static private void exportTherionHeader2( BufferedWriter out, String points, String lines, String areas ) throws IOException
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("#P");
    String[] vals = points.split(",");
    for ( int k=0; k<vals.length; ++k ) if ( vals[k].length() > 0 ) pw.format(" %s", vals[k] );
    pw.format("\n#L");
    vals = lines.split(",");
    for ( int k=0; k<vals.length; ++k ) if ( vals[k].length() > 0 ) pw.format(" %s", vals[k] );
    pw.format("\n#A");
    vals = lines.split(",");
    for ( int k=0; k<vals.length; ++k ) if ( vals[k].length() > 0 ) pw.format(" %s", vals[k] );
    pw.format("\n");
    out.write( sw.getBuffer().toString() );
  }
  
  static private void exportTherionHeader3( BufferedWriter out,
         int type, String scrap_name, String proj_name, int project_dir,
         boolean do_north, float x1, float y1, float x2, float y2 ) throws IOException
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if (    type == PlotInfo.PLOT_SECTION
         || type == PlotInfo.PLOT_H_SECTION 
         || type == PlotInfo.PLOT_X_SECTION ) {
      if ( do_north ) { // H_SECTION (horizontal section) : north line is 5 m long
        pw.format("scrap %s -projection %s -scale [%.0f %.0f %.0f %.0f 0 5 0 0 m]", scrap_name, proj_name, 
          x1*toTherion, -y1*toTherion, x2*toTherion, -y2*toTherion );
      } else {
        pw.format("scrap %s -projection %s -scale [0 0 %.0f 0 0 0 1 0 m]", scrap_name, proj_name, oneMeter );
      }
    } else if ( type == PlotInfo.PLOT_PROFILE ) {
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
  static public void exportTherion( // DataHelper dh, long sid,
                      int type, BufferedWriter out, String scrap_name, String proj_name, int project_dir,
        RectF bbox,
        DrawingPath north,
        List<ICanvasCommand> cstack,
        List<DrawingStationPath> userstations,
        List<DrawingStationName> stations,
        List<DrawingPath> splays )
  {
    try { 
      exportTherionHeader1( out, type, bbox );
      exportTherionHeader2( out );
      if ( north != null ) { 
        exportTherionHeader3( out, type, scrap_name, proj_name, 0, true, north.x1, north.y1, north.x2, north.y2 );
      } else {
        exportTherionHeader3( out, type, scrap_name, proj_name, project_dir, false, 0, 0, 0, 0 );
      }
        
      synchronized( cstack ) {
        for ( ICanvasCommand cmd : cstack ) {
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath p = (DrawingPath) cmd;
          if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
            DrawingPointPath pp = (DrawingPointPath)p;
            out.write( pp.toTherion() );
            out.newLine();
          } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) { // should never happen
            // if ( ! TDSetting.mAutoStations ) {
            //   DrawingStationPath st = (DrawingStationPath)p;
            //   out.write( st.toTherion() );
            //   out.newLine();
            // }
          } else if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath lp = (DrawingLinePath)p;
            if ( lp.size() > 1 ) {
              out.write( lp.toTherion() );
              out.newLine();
            }
          } else if ( p.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath ap = (DrawingAreaPath)p;
            out.write( ap.toTherion() );
            out.newLine();
          }
        }
      }
      out.newLine();

      if ( TDSetting.mTherionSplays ) {
        float th = TDConst.TO_THERION;
        StringWriter sw = new StringWriter();
        PrintWriter pw  = new PrintWriter(sw);
        for ( DrawingPath splay : splays ) {
          pw.format("line u:splay -visibility off\n");
          pw.format( Locale.US, "  %.2f %.2f\n  %.2f %.2f\n", splay.x1*th, -splay.y1*th, splay.x2*th, -splay.y2*th );
          pw.format("endline\n");
        }
        out.write( sw.toString() );
        out.newLine();
      }

      if ( TDSetting.mAutoStations ) {
        for ( DrawingStationName st : stations ) {
          if ( st.mStation != null && st.mStation.barriered() ) continue;
          // FIXME if station is in the convex hull (bbox) of the lines
          if ( bbox.left > st.cx || bbox.right  < st.cx ) continue;
          if ( bbox.top  > st.cy || bbox.bottom < st.cy ) continue;
          out.write( st.toTherion() );
          out.newLine();
/*
 * this was to export splays by the station instead of all of them
 *
          if ( TDSetting.mTherionSplays ) {
            float th = TDConst.TO_THERION;
            float x = st.cx * th;
            float y = - st.cy * th;
            th *= DrawingUtil.SCALE_FIX;
            List< DBlock > blks = dh.selectSplaysAt( sid, st.mName, false );
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
                float h = blk.mLength * TDMath.cosd( blk.mClino ) * th * blk.mExtend;
                out.write( "line splay\n" );
                out.write( String.format(Locale.US, "  %.2f %.2f\n  %.2f %.2f\n", x, y, x+h, y+v ) );
                out.write( "endline\n" );
              }
            } else if ( PlotInfo.isSection( type ) ) {
              for ( DBlock blk : blks ) {
                float d = blk.mLength;
                float b = blk.mBearing;
                float c = blk.mClino;
                long  e = blk.mExtend;
              }
            }
          }
*/
        }
      } else {
        synchronized( userstations ) {
          for ( DrawingStationPath sp : userstations ) {
            out.write( sp.toTherion() );
            out.newLine();
          }
        }
      }
      exportTherionClose( out );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  static public void dataStream2Therion( File file, BufferedWriter out, RectF bbox, boolean endscrap )
  {
    int version = 0;
    boolean in_scrap = false;

    boolean do_north = false;
    float north_x1=0, north_y1=0, north_x2=0, north_y2=0;

    String name = "";
    int type = 0;
    boolean project = false;
    int project_dir = 0;
    String points = "";
    String lines  = "";
    String areas  = "";

    // synchronized( TDPath.mTherionLock ) 
    {
      try {
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
                bbox.left   = dis.readFloat();
                bbox.top    = dis.readFloat();
                bbox.right  = dis.readFloat();
                bbox.bottom = dis.readFloat();
                if ( dis.readInt() == 1 ) {
                  do_north = true;
                  north_x1 = dis.readFloat();
                  north_y1 = dis.readFloat();
                  north_x2 = dis.readFloat();
                  north_y2 = dis.readFloat();
                }
                exportTherionHeader1( out, type, bbox );
                exportTherionHeader2( out, points, lines, areas );
                String proj = PlotInfo.projName[ type ];
                if ( do_north ) { 
                  exportTherionHeader3( out, type, name, proj, 0, true, north_x1, north_y1, north_x2, north_y2 );
                } else {
                  exportTherionHeader3( out, type, name, proj, project_dir, false, 0, 0, 0, 0 );
                }
                in_scrap = true;
              }
              break;
            case 'S':
              {
                name = dis.readUTF();
                type = dis.readInt();
                if ( type == PlotInfo.PLOT_PROFILE ) project_dir = dis.readInt();
                // read palettes
                points = dis.readUTF();
                lines = dis.readUTF();
                areas = dis.readUTF();
              }
              break;
            case 'P':
              out.write( DrawingPointPath.loadDataStream( version, dis, 0, 0, null ).toTherion() );
              break;
            case 'T':
              out.write( DrawingLabelPath.loadDataStream( version, dis, 0, 0 ).toTherion() );
              break;
            case 'L':
              out.write( DrawingLinePath.loadDataStream( version, dis, 0, 0, null ).toTherion() );
              break;
            case 'A':
              out.write( DrawingAreaPath.loadDataStream( version, dis, 0, 0, null ).toTherion() );
              break;
            case 'U':
              out.write( DrawingStationPath.loadDataStream( version, dis ).toTherion() );
              break;
            case 'X':
              // NOTE need to check XSection ??? STATION_XSECTION
              out.write( DrawingStationName.loadDataStream( version, dis ).toTherion() );
              break;
            case 'F':
              break; // continue parsing stations
            case 'E':
              todo = false;
              break;
            default:
              todo = false;
              TDLog.Error( "ERROR bad input (2) " + (int)what );
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

}
