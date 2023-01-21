/* @file DrawingTh.java    
 *
 * @author marco corvi
 * @date nov 2015
 *
 * @brief TopoDroid drawing: reading Therion th2 file
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.th;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDUtil;
// import com.topodroid.utils.TDMath;
// import com.topodroid.num.NumStation;
// import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
// import com.topodroid.common.PlotType;
import com.topodroid.common.PointScale;
// import com.topodroid.math.TDVector;
import com.topodroid.TDX.BrushManager;
import com.topodroid.TDX.DrawingSurface;
import com.topodroid.TDX.DrawingStationUser;
// import com.topodroid.TDX.DrawingStationName;
import com.topodroid.TDX.DrawingPointPath;
import com.topodroid.TDX.DrawingLabelPath;
// import com.topodroid.TDX.DrawingPointLinePath;
import com.topodroid.TDX.DrawingLinePath;
import com.topodroid.TDX.DrawingAreaPath;
// import com.topodroid.TDX.Scrap;
import com.topodroid.TDX.SymbolLibrary;
import com.topodroid.TDX.TDToast;


// import java.io.File;
// import java.io.FileWriter;
// import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
// import java.io.BufferedInputStream;
// import java.io.BufferedOutputStream;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
// import java.io.StringWriter;
// import java.io.PrintWriter;
// import java.nio.channels.FileChannel;

// import java.io.FileNotFoundException;
import java.io.IOException;
// import java.io.EOFException;

// import java.util.List;
import java.util.ArrayList;
// import android.util.ArraySet; // REQUIRES API-23
// import java.util.Locale;

// import android.graphics.RectF;

public class DrawingTh
{

  /** read a line or a multi-line
   * @param br   buffered reader
   * @return read line(s) or null
   */
  private static String readLine( BufferedReader br )
  {
    StringBuilder sb = new StringBuilder();
    try {
      while (true) {
        String line = br.readLine();
        if ( line == null ) return null;
        sb.append( line.trim() );
        if ( line.endsWith( "\\" ) ) { // continuation
          sb.append( " " );
        } else {
          break;
        }
      }
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return sb.toString();
  } 

  /* NOTE therion th2 files can no longer be supported because therion scale is not fixed
   * @param surface  drawing surface
   * @param fr       input th2 file reader
   * @param dx       X offset
   * @param dy       Y offset
   * @return scrap options
   */
  public static boolean doLoadTherion( DrawingSurface surface,
                                FileReader fr,
                                float dx, float dy )
  {
    // boolean ret = false;
    StringBuilder missingSymbols = new StringBuilder();
    missingSymbols.append("Missing");
    float x, y, x1, y1, x2, y2;
    float toTherion = TDSetting.mToTherion;
    int scrap_idx = -1;
    String thname;

    // TDLog.v( "Load Therion file " + " delta " + dx + " " + dy );
    BrushManager.resetPointOrientations();
    ArrayList< DrawingAreaPath > areas = new ArrayList<>();
    int area_cnt = 0;

    // TDLog.v( "after reset 0: " + BrushManager.mOrientation[0]
    //                      + " 7: " + BrushManager.mOrientation[7] );

    // TDLog.v( "drawing I/O load therion " );
    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    TDLog.v( "TH load plot from Therion file " );
    BufferedReader br = new BufferedReader( fr );
    String line = null;
    while ( (line = readLine(br)) != null ) {
      int comment = line.indexOf('#');
      if ( comment == 0 ) continue;
      if ( comment > 0 ) line = line.substring( 0, comment ).trim();
      if ( line.length() == 0 ) continue;
      // TDLog.v( "TH line: >>" + line + "<<");
      String[] vals = TDString.splitOnSpaces( line );
      if ( vals.length == 0 ) continue;
      if ( vals[0].equals( "scrap" ) ) {
        if ( vals.length < 4 ) {
          TDLog.Error( "bad scrap cmd: " + line );
        } else {
          // String name = vals[1];
          // skip "-projection" vals[2]
          scrap_idx = surface.newScrapIndex( true ); // true = force 
          TDLog.v("DRAW scrap index " + scrap_idx );
          int pos = line.indexOf( "scrap" );
          surface.setScrapOptions( scrap_idx, line.substring( pos+6 ) );
        }
      } else if ( vals[0].equals( "endscrap" ) ) {
         TDLog.v("DRAW endscrap");
      } else if ( vals[0].equals( "point" ) ) { // THERION POINT: point X Y type [options]
        if ( vals.length < 4 ) {
          TDLog.Error( "TH bad point cmd: " + line );
        } else {
          int ptType = BrushManager.getPointLibSize();
          boolean has_orientation = false;
          float orientation = 0.0f;
          int scale = PointScale.SCALE_M;
          String options = null;

          try {
            x = dx + Float.parseFloat( vals[1] ) / toTherion;
            y = dy - Float.parseFloat( vals[2] ) / toTherion;
          } catch ( NumberFormatException e ) {
            TDLog.Error( "TH Point error (number fmt) <" + line + ">" );
            continue;
          }
          thname = vals[3];
          String label_text = null;
          int k = 4;
          if ( thname.equals( "station" ) ) {
            if ( vals.length > k+1 && vals[k].equals( "-name" ) ) {
              String name = vals[k+1].replaceAll( "\"", "" );
              DrawingStationUser station_path = new DrawingStationUser( name, x, y, scale, scrap_idx );
              surface.addDrawingStationUser( station_path );
            }
            continue;
          }
          while ( vals.length > k ) { 
            if ( vals[k].equals( "-orientation" ) ) {
              try {
                orientation = Float.parseFloat( vals[k+1] );
                has_orientation = true;
              } catch ( NumberFormatException e ) {
                TDLog.Error( "TH Point orientation error : " + line );
              }
              k += 2;
            } else if ( vals[k].equals( "-scale" ) ) {
              // FIXME assert (vals.length > k+1 );
              if ( vals[k+1].equals("xs") ) {
                scale = PointScale.SCALE_XS;
              } else if ( vals[k+1].equals("s") ) {
                scale = PointScale.SCALE_S;
              } else if ( vals[k+1].equals("l") ) {
                scale = PointScale.SCALE_L;
              } else if ( vals[k+1].equals("xl") ) {
                scale = PointScale.SCALE_XL;
              } 
              k += 2;
            } else if ( vals[k].equals( "-text" ) ) {
              // FIXME assert (vals.length > k+1 );
              label_text = vals[k+1];
              k += 2;
              if ( label_text.startsWith( "\"" ) ) {
                StringBuilder sb = new StringBuilder();
                sb.append(label_text);
                while ( k < vals.length ) {
                  sb.append(" ").append(vals[k]);
                  // label_text = label_text + " " + vals[k];
                  if ( vals[k].endsWith( "\"" ) ) break;
                  ++ k;
                }
                label_text = sb.toString().replaceAll( "\"", "" );
                ++ k;
              }
            } else {
              options = TDUtil.concat( vals, k );
              k = vals.length;
            }
          }

          // BrushManager.tryLoadMissingPoint( type );
          // map pre 3.1.1 therion names to 3.1.1 names
          if ( thname.equals( "archeo-material" ) ) { thname = "archeo"; }
          ptType = BrushManager.getPointIndexByThName( thname );
          if ( ptType < 0 ) {
            missingSymbols.append(" P").append( thname );
            ptType = 0; // SymbolPointLibrary.mPointUserIndex; // FIXME
          }

          if ( ptType == BrushManager.getPointLabelIndex() ) {
            if ( label_text != null ) {
              // "danger" is no longer mapped on a label 
              // if ( label_text.equals( "!" ) ) {    // "danger" point
              //   DrawingPointPath path = new DrawingPointPath( BrushManager.getPointDangerIndex(), x, y, scale, text, options, scrap_idx );
              //   surface.addDrawingPath( path );
              // } else {                             // regular label
                DrawingLabelPath path = new DrawingLabelPath( label_text, x, y, scale, options, scrap_idx );
                if ( has_orientation ) {
                  path.setOrientation( orientation );
                }
                surface.addDrawingPath( path );
              // }
            }
          } else if ( has_orientation && BrushManager.isPointOrientable(ptType) ) {
            // TDLog.v( "TH point " + ptType + " has orientation " + orientation );
            BrushManager.rotateGradPoint( ptType, orientation );
            DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, label_text, options, scrap_idx );
            surface.addDrawingPath( path );
            BrushManager.rotateGradPoint( ptType, -orientation );
          } else {
            DrawingPointPath path = new DrawingPointPath( ptType, x, y, scale, label_text, options, scrap_idx );
            surface.addDrawingPath( path );
          }
        }
      } else if ( vals[0].equals( "line" ) ) { // THERION LINES 
        if ( vals.length < 2 ) {
          TDLog.Error( "TH bad line cmd: " + line );
        } else {
          thname = vals[1];
          if ( vals.length >= 6 && thname.equals( "border" ) && vals[2].equals( "-id" ) ) { // THERION AREAS
            boolean visible = true;
            String area_id = vals[3];
            TDLog.v( "TH area border id " + area_id );
            if ( vals.length >= 8 && vals[6].equals("-visibility") && vals[7].equals("off") ) {
              visible = false;
            }
            DrawingAreaPath path = new DrawingAreaPath( 0, area_cnt++, area_id, visible, scrap_idx, true ); // true = th2_edit
            areas.add( path );

            // TODO insert new area-path
            line = readLine( br );
            if ( ! line.equals( "endline" ) ) {  // FIXME may null pointer
              String[] pt = line.split( "\\s+" );
              try {
                x = dx + Float.parseFloat( pt[0] ) / toTherion;
                y = dy - Float.parseFloat( pt[1] ) / toTherion;
              } catch ( NumberFormatException e ) {
                TDLog.Error( "TH Line error (number fmt) <" + line + ">" );
                continue;
              }
              path.addStartPoint( x, y );
              while ( (line = readLine( br )) != null ) {
                if ( line.equals( "endline" ) ) break;
                String[] pt2 = line.split( " " );
                if ( pt2.length == 2 ) {
                  try {
                    x = dx + Float.parseFloat( pt2[0] ) / toTherion;
                    y = dy - Float.parseFloat( pt2[1] ) / toTherion;
                    path.addPoint( x, y );
                    // TDLog.v( "TH area pt " + x + " " + y);
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "TH area line X-Y error (10) <" + line + ">" );
                    continue;
                  } catch ( ArrayIndexOutOfBoundsException e ) {
                    TDLog.Error( "TH area line X-Y error (11) " + line );
                    continue;
                  }
                } else if ( pt2.length == 6 ) {
                  try {
                    x1 = dx + Float.parseFloat( pt2[0] ) / toTherion;
                    y1 = dy - Float.parseFloat( pt2[1] ) / toTherion;
                    x2 = dx + Float.parseFloat( pt2[2] ) / toTherion;
                    y2 = dy - Float.parseFloat( pt2[3] ) / toTherion;
                    x  = dx + Float.parseFloat( pt2[4] ) / toTherion;
                    y  = dy - Float.parseFloat( pt2[5] ) / toTherion;
                    path.addPoint3( x1, y1, x2, y2, x, y );
                    // TDLog.v( "TH area pt " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + x + " " + y);
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "TH area line X-Y error (12) <" + line + ">" );
                    continue;
                  } catch ( ArrayIndexOutOfBoundsException e ) {
                    TDLog.Error( "TH area line X-Y error (13) " + line );
                    continue;
                  }
                }
              }
              path.setClosed( true );
            }
          } else { // ********* regular lines
            // FIXME assert (vals.length > 1 );
            // TDLog.v( "line thname " + thname );
            boolean closed = false;
            boolean reversed = false;
            int outline = DrawingLinePath.OUTLINE_UNDEF;
            String options = null;
           
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
            
            int lnType = BrushManager.mLineLib.size();
            DrawingLinePath path = null;
            // BrushManager.tryLoadMissingLine( thname );
            lnType = BrushManager.getLineIndexByThName( thname );
            if ( lnType < 0 ) {
              missingSymbols.append(" L").append( thname );
              lnType = 0; // SymbolLineLibrary.mLineUserIndex; // FIXME
              // continue;
            }
            // TODO insert new line-path
            line = readLine( br );
            if ( ! line.equals( "endline" ) ) {  // FIXME may null pointer
              path = new DrawingLinePath( lnType, scrap_idx );
              path.setClosed( closed );
              path.setReversed( reversed );
              if ( outline != DrawingLinePath.OUTLINE_UNDEF ) path.mOutline = outline;
              if ( options != null ) path.setOptions( options );

              // TDLog.v( "  line start point: <" + line + ">");
              String[] pt0 = line.split( "\\s+" );
              try {
                x = dx + Float.parseFloat( pt0[0] ) / toTherion;
                y = dy - Float.parseFloat( pt0[1] ) / toTherion;
                path.addStartPoint( x, y );
              } catch ( NumberFormatException e ) {
                TDLog.Error( "THERION line X-Y error (1) <" + line + ">" );
                continue;
              } catch ( ArrayIndexOutOfBoundsException e ) {
                TDLog.Error( "THERION line X-Y error (2) " + line );
                continue;
              }
              // TDLog.v( "  line start point: <" + line + "> " + x + " " + y );
              while ( (line = readLine( br )) != null ) {
                if ( line.contains( "l-size" ) ) continue;
                if ( line.equals( "endline" ) ) {
                  if ( path != null ) { // always true
                    if ( thname.equals( SymbolLibrary.SECTION) ) { // section line only in non-section scraps
                      path.makeStraight( );
                    } else {
                      path.computeUnitNormal(); // for section-line already done by makeStraight
                    } 
                    surface.addDrawingPath( path );
                  }
                  break;
                }
                if ( path != null ) {
                  // TDLog.v( "  line point: >>" + line + "<<");
                  String[] pt = line.split( " " );
                  if ( pt.length == 2 ) {
                    try {
                      x = dx + Float.parseFloat( pt[0] ) / toTherion;
                      y = dy - Float.parseFloat( pt[1] ) / toTherion;
                      path.addPoint( x, y );
                    } catch ( NumberFormatException e ) {
                      TDLog.Error( "THERION line X-Y error (3) <" + line + ">" );
                      continue;
                    } catch ( ArrayIndexOutOfBoundsException e ) {
                      TDLog.Error( "THERION line X-Y error (4) " + line );
                      continue;
                    }
                  } else if ( pt.length == 6 ) {
                    try {
                      x1 = dx + Float.parseFloat( pt[0] ) / toTherion;
                      y1 = dy - Float.parseFloat( pt[1] ) / toTherion;
                      x2 = dx + Float.parseFloat( pt[2] ) / toTherion;
                      y2 = dy - Float.parseFloat( pt[3] ) / toTherion;
                      x  = dx + Float.parseFloat( pt[4] ) / toTherion;
                      y  = dy - Float.parseFloat( pt[5] ) / toTherion;
                      path.addPoint3( x1, y1, x2, y2, x, y );
                    } catch ( NumberFormatException e ) {
                      TDLog.Error( "THERION line X-Y error (5) <" + line + ">" );
                      continue;
                    } catch ( ArrayIndexOutOfBoundsException e ) {
                      TDLog.Error( "THERION line X-Y error (6) " + line );
                      continue;
                    }
                  }
                }
              } // end while ( line-points )
            }
          }
        }
      } else if ( vals[0].equals( "area" ) ) { // THERION AREAS
        thname = vals[1]; // area type
        // TDLog.v( "area thname " + thname );
        int area_type = BrushManager.getAreaIndexByThName( thname ); // 20230118 local var "area_type"
        if ( area_type < 0 ) {
          missingSymbols.append(" A").append( thname );
          area_type = 0; // SymbolPointLibrary.mPointUserIndex; // FIXME
        }
        line = readLine( br );
        if ( ! line.equals( "endarea" ) ) {  // FIXME may null pointer
          String border_id = line;
          for ( DrawingAreaPath path : areas ) {
            if ( path.mPrefix.equals( border_id ) ) {
              path.setAreaType( area_type );
              surface.addDrawingPath( path );
              break;
            }
          }
          line = readLine( br );
        }
      }
    }
    // ret = true;
    if ( missingSymbols.length() > 8 ) {
      TDToast.makeWarn( missingSymbols.toString() );
    }
    return true;
  }
}
