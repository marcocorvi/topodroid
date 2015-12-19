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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;

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
      line.replaceAll(" *", " ");
      // line.replaceAll("\\s+", " ");
    }
    return line;
  } 

  public static boolean doLoadDataStream( DrawingSurface surface,
                                   String filename,
                                   float dx, float dy,
                                   SymbolsPalette missingSymbols,
                                   SymbolsPalette localPalette,
                                   RectF bbox, boolean complete )
  {
    int version = 0;
    boolean in_scrap = false;
    // DrawingBrushPaths.makePaths( );
    DrawingBrushPaths.resetPointOrientations();
    DrawingPath path;

    synchronized( TopoDroidPath.mTherionLock ) {
      try {
        FileInputStream fis = new FileInputStream( filename );
        DataInputStream dis = new DataInputStream( fis );
        boolean todo = true;
        while ( todo ) {
          int what = dis.read();
          path = null;
          switch ( what ) {
            case 'V':
              version = dis.readInt();
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
              }
            case 'S':
              {
                String name = dis.readUTF();
                int type = dis.readInt();
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
              if ( complete ) break; // continue parsing stations
            case 'E':
              todo = false;
            default:
              break;
          } 
          if ( path == null ) { // this is an unrecoverable error
            if ( what == 'V' || what == 'S' || what == 'E' || what == 'F' ) continue;
            TopoDroidLog.Error( "ERROR failed to create path " + (char)what );
            break;
          } else if ( in_scrap ) {
            if ( what == 'U' ) {
              surface.addDrawingStationPath( (DrawingStationPath)path );
            } else {
              surface.addDrawingPath( path );
            }
          }
        }
        dis.close();
        fis.close();
      } catch ( FileNotFoundException e ) {
        // this is OK
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      // Log.v("DistoX", "read: " + sb.toString() );
    }
    return (missingSymbols != null )? missingSymbols.isOK() : true;
  }

  public static boolean doLoadTherion( DrawingSurface surface,
                                String filename,
                                float dx, float dy,
                                SymbolsPalette missingSymbols,
                                SymbolsPalette localPalette )
  {
    float x, y, x1, y1, x2, y2;
    boolean is_not_section = true;

    TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "Load Therion file " + filename + " delta " + dx + " " + dy );
    // DrawingBrushPaths.makePaths( );
    DrawingBrushPaths.resetPointOrientations();

    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "after reset 0: " + DrawingBrushPaths.mOrientation[0]
    //                      + " 7: " + DrawingBrushPaths.mOrientation[7] );

    synchronized( TopoDroidPath.mTherionLock ) {
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
                DrawingBrushPaths.mPointLib.makeEnabledListFromPalette( localPalette );
              }
            } else if ( line.startsWith( "#L " ) ) { // LINE PALETTE
              if ( localPalette != null ) {
                localPalette.mPaletteLine.clear();
                localPalette.addLineFilename("user");
                String[] syms = line.split( " " );
                for ( int k=1; k<syms.length; ++k ) {
                  if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addLineFilename( syms[k] );
                }
                DrawingBrushPaths.mLineLib.makeEnabledListFromPalette( localPalette );
              }
            } else if ( line.startsWith( "#A " ) ) { // AREA PALETTE
              if ( localPalette != null ) {
                localPalette.mPaletteArea.clear();
                localPalette.addAreaFilename("user");
                String[] syms = line.split( " " );
                for ( int k=1; k<syms.length; ++k ) {
                  if ( syms[k].length() > 0 && ! syms[k].equals("user") ) localPalette.addAreaFilename( syms[k] );
                }
                DrawingBrushPaths.mAreaLib.makeEnabledListFromPalette( localPalette );
              }
            }
            continue;
          } else if (comment > 0 ) {
            line = line.substring( 0, comment );
          }
          if ( line.length() == 0 /* || line.charAt(0) == '#' */ ) {
            continue;
          }

          // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "  line: >>" + line + "<<");
          line = line.replaceAll("\\s+", " ");
          String[] vals = line.split( " " );
          // FIXME assert( vals.length > 0 );
          if ( vals[0].equals( "scrap" ) ) {
            if ( vals.length < 4 ) {
              TopoDroidLog.Error( "bad scrap cmd: " + line );
            } else {
              // String name = vals[1];
              // skip "-projection" vals[2]
              is_not_section = ! vals[3].equals("none");
            }
          } else if ( vals[0].equals( "point" ) ) {
            // ****** THERION POINT ********************************** point X Y type [options]
            if ( vals.length < 4 ) {
              TopoDroidLog.Error( "bad point cmd: " + line );
            } else {
              int ptType = DrawingBrushPaths.mPointLib.mSymbolNr;
              boolean has_orientation = false;
              float orientation = 0.0f;
              int scale = DrawingPointPath.SCALE_M;
              String options = null;

              try {
                x = dx + Float.parseFloat( vals[1] ) / TopoDroidConst.TO_THERION;
                y = dy - Float.parseFloat( vals[2] ) / TopoDroidConst.TO_THERION;
              } catch ( NumberFormatException e ) {
                TopoDroidLog.Error( "Therion Point error (number fmt) <" + line + ">" );
                continue;
              }
              String type = vals[3];
              String label_text = null;
              int k = 4;
              if ( type.equals( "station" ) ) {
                if ( ! TopoDroidSetting.mAutoStations ) {
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
                    TopoDroidLog.Error( "Therion Point orientation error : " + line );
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

              DrawingBrushPaths.mPointLib.tryLoadMissingPoint( type );
              ptType = DrawingBrushPaths.mPointLib.getSymbolIndexByThName( type );
              if ( ptType < 0 ) {
                if ( missingSymbols != null ) missingSymbols.addPointFilename( type ); // add "type" to the missing point-types
                ptType = 0; // SymbolPointLibrary.mPointUserIndex; // FIXME
                // continue;
              }

              if ( has_orientation && DrawingBrushPaths.mPointLib.isSymbolOrientable(ptType) ) {
                // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "[2] point " + ptType + " has orientation " + orientation );
                DrawingBrushPaths.rotateGradPoint( ptType, orientation );
                DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, options );
                surface.addDrawingPath( path );
                DrawingBrushPaths.rotateGradPoint( ptType, -orientation );
              } else {
                if ( ptType != DrawingBrushPaths.mPointLib.mPointLabelIndex ) {
                  DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, options );
                  surface.addDrawingPath( path );
                } else {
                  if ( label_text.equals( "!" ) ) {    // "danger" point
                    DrawingPointPath path = new DrawingPointPath( DrawingBrushPaths.mPointLib.mPointDangerIndex, x, y, scale, options );
                    surface.addDrawingPath( path );
                  } else {                             // regular label
                    DrawingLabelPath path = new DrawingLabelPath( label_text, x, y, scale, options );
                    surface.addDrawingPath( path );
                  }
                }
              }
            }
          } else if ( vals[0].equals( "line" ) ) {
            // ********* THERION LINES ************************************************************
            if ( vals.length < 2 ) {
              TopoDroidLog.Error( "bad line cmd: " + line );
            } else {
              if ( vals.length >= 6 && vals[1].equals( "border" ) && vals[2].equals( "-id" ) ) { // THERION AREAS
                boolean visible = true;
                // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "area id " + vals[3] );
                if ( vals.length >= 8 && vals[6].equals("-visibility") && vals[7].equals("off") ) {
                  visible = false;
                }
                int arType = DrawingBrushPaths.mAreaLib.mSymbolNr;
                DrawingAreaPath path = new DrawingAreaPath( arType, vals[3], visible );

                // TODO insert new area-path
                line = readLine( br );
                if ( ! line.equals( "endline" ) ) { 
                  String[] pt = line.split( "\\s+" );
                  try {
                    x = dx + Float.parseFloat( pt[0] ) / TopoDroidConst.TO_THERION;
                    y = dy - Float.parseFloat( pt[1] ) / TopoDroidConst.TO_THERION;
                  } catch ( NumberFormatException e ) {
                    TopoDroidLog.Error( "Therion Line error (number fmt) <" + line + ">" );
                    continue;
                  }
                  path.addStartPoint( x, y );

                  while ( (line = readLine( br )) != null ) {
                    if ( line.equals( "endline" ) ) {
                      line = readLine( br ); // area statement
                      String[] vals2 = line.split( " " );
                      if ( vals2.length >= 2 ) {
                        DrawingBrushPaths.mAreaLib.tryLoadMissingArea( vals2[1] );
                        arType = DrawingBrushPaths.mAreaLib.getSymbolIndexByThName( vals2[1] );
                        if ( arType < 0 ) {
                          if ( missingSymbols != null ) missingSymbols.addAreaFilename( vals2[1] );
                          arType = 0; // SymbolAreaLibrary.mAreaUserIndex; // FIXME
                          // continue;
                        }
                        // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, "set area type " + arType + " " + vals2[1]);
                        double orientation = 0;
                        if ( vals2.length >= 4 && vals2[2].equals("#orientation") ) {
                          try {
                            orientation = Double.parseDouble( vals2[3] );
                          } catch ( NumberFormatException e ) { 
                            TopoDroidLog.Error( "Therion Area orientation error <" + line + ">" );
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
                    // TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "  line point: >>" + line + "<<");
                    String[] pt2 = line.split( " " );
                    if ( pt2.length == 2 ) {
                      try {
                        x = dx + Float.parseFloat( pt2[0] ) / TopoDroidConst.TO_THERION;
                        y = dy - Float.parseFloat( pt2[1] ) / TopoDroidConst.TO_THERION;
                        path.addPoint( x, y );
                        // TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "area pt " + x + " " + y);
                      } catch ( NumberFormatException e ) {
                        TopoDroidLog.Error( "Therion Line X-Y error (10) <" + line + ">" );
                        continue;
                      } catch ( ArrayIndexOutOfBoundsException e ) {
                        TopoDroidLog.Error( "Therion Line X-Y error (11) " + line );
                        continue;
                      }
                    } else if ( pt2.length == 6 ) {
                      try {
                        x1 = dx + Float.parseFloat( pt2[0] ) / TopoDroidConst.TO_THERION;
                        y1 = dy - Float.parseFloat( pt2[1] ) / TopoDroidConst.TO_THERION;
                        x2 = dx + Float.parseFloat( pt2[2] ) / TopoDroidConst.TO_THERION;
                        y2 = dy - Float.parseFloat( pt2[3] ) / TopoDroidConst.TO_THERION;
                        x  = dx + Float.parseFloat( pt2[4] ) / TopoDroidConst.TO_THERION;
                        y  = dy - Float.parseFloat( pt2[5] ) / TopoDroidConst.TO_THERION;
                        path.addPoint3( x1, y1, x2, y2, x, y );
                        // TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "area pt " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + x + " " + y);
                      } catch ( NumberFormatException e ) {
                        TopoDroidLog.Error( "Therion Line X-Y error (12) <" + line + ">" );
                        continue;
                      } catch ( ArrayIndexOutOfBoundsException e ) {
                        TopoDroidLog.Error( "Therion Line X-Y error (13) " + line );
                        continue;
                      }
                    }
                  }
                }
              } else { // ********* regular lines
                // FIXME assert (vals.length > 1 );
                // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "line type " + vals[1] );
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
                
                int lnTypeMax = DrawingBrushPaths.mLineLib.mSymbolNr;
                int lnType = lnTypeMax;
                DrawingLinePath path = null;
                DrawingBrushPaths.mLineLib.tryLoadMissingLine( type );
                lnType = DrawingBrushPaths.mLineLib.getSymbolIndexByThName( type );
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

                  // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "  line start point: <" + line + ">");
                  String[] pt0 = line.split( "\\s+" );
                  try {
                    x = dx + Float.parseFloat( pt0[0] ) / TopoDroidConst.TO_THERION;
                    y = dy - Float.parseFloat( pt0[1] ) / TopoDroidConst.TO_THERION;
                    path.addStartPoint( x, y );
                  } catch ( NumberFormatException e ) {
                    TopoDroidLog.Error( "Therion Line X-Y error (1) <" + line + ">" );
                    continue;
                  } catch ( ArrayIndexOutOfBoundsException e ) {
                    TopoDroidLog.Error( "Therion Line X-Y error (2) " + line );
                    continue;
                  }
                  // Log.v( "DistoX", "  line start point: <" + line + "> " + x + " " + y );
                  while ( (line = readLine( br )) != null ) {
                    if ( line.indexOf( "l-size" ) >= 0 ) continue;
                    if ( line.equals( "endline" ) ) {
                      if ( path != null ) {
                        if ( type.equals("section") ) { // section line only in non-section scraps
                          if ( is_not_section ) {
                            path.makeStraight( true );
                          }
                        } else {
                          path.computeUnitNormal();
                        }
                        surface.addDrawingPath( path );
                      }
                      break;
                    }
                    if ( path != null ) {
                      // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "  line point: >>" + line + "<<");
                      String[] pt = line.split( " " );
                      if ( pt.length == 2 ) {
                        try {
                          x = dx + Float.parseFloat( pt[0] ) / TopoDroidConst.TO_THERION;
                          y = dy - Float.parseFloat( pt[1] ) / TopoDroidConst.TO_THERION;
                          path.addPoint( x, y );
                        } catch ( NumberFormatException e ) {
                          TopoDroidLog.Error( "Therion Line X-Y error (3) <" + line + ">" );
                          continue;
                        } catch ( ArrayIndexOutOfBoundsException e ) {
                          TopoDroidLog.Error( "Therion Line X-Y error (4) " + line );
                          continue;
                        }
                      } else if ( pt.length == 6 ) {
                        try {
                          x1 = dx + Float.parseFloat( pt[0] ) / TopoDroidConst.TO_THERION;
                          y1 = dy - Float.parseFloat( pt[1] ) / TopoDroidConst.TO_THERION;
                          x2 = dx + Float.parseFloat( pt[2] ) / TopoDroidConst.TO_THERION;
                          y2 = dy - Float.parseFloat( pt[3] ) / TopoDroidConst.TO_THERION;
                          x  = dx + Float.parseFloat( pt[4] ) / TopoDroidConst.TO_THERION;
                          y  = dy - Float.parseFloat( pt[5] ) / TopoDroidConst.TO_THERION;
                          path.addPoint3( x1, y1, x2, y2, x, y );
                        } catch ( NumberFormatException e ) {
                          TopoDroidLog.Error( "Therion Line X-Y error (5) <" + line + ">" );
                          continue;
                        } catch ( ArrayIndexOutOfBoundsException e ) {
                          TopoDroidLog.Error( "Therion Line X-Y error (6) " + line );
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
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
    // remove repeated names

    return (missingSymbols != null )? missingSymbols.isOK() : true;
  }

  static public void exportTherion( DrawingSurface surface, int type, File file, String fullname, String projname )
  {
    try {
      FileWriter fw = new FileWriter( file );
      BufferedWriter br = new BufferedWriter( fw );
      surface.exportTherion( type, br, fullname, projname );
      br.flush();
      br.close();
    } catch ( IOException e ) {
      TopoDroidLog.Error( "Export Therion i/o: " + e.getMessage() );
    }
  }

  static public void exportDataStream( DrawingSurface surface, int type, File file, String fullname )
  {
    try {
      FileOutputStream fos = new FileOutputStream( file );
      DataOutputStream dos = new DataOutputStream( fos );
      surface.exportDataStream( type, dos, fullname );
      dos.close();
      fos.close();
    } catch ( FileNotFoundException e ) {
      TopoDroidLog.Error( "Export Data file: " + e.getMessage() );
    } catch ( IOException e ) {
      TopoDroidLog.Error( "Export Data i/o: " + e.getMessage() );
    }
  }

}
