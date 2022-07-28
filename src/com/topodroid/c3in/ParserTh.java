/** @file ParserTh.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D therion file parser and model
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import com.topodroid.TDX.R;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.c3db.DataHelper;
import com.topodroid.c3db.SurveyFixed;
import com.topodroid.c3db.SurveyInfo;
import com.topodroid.c3db.DBlock;
import com.topodroid.TDX.TopoGL;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.Cave3DCS;
import com.topodroid.TDX.Cave3DShot;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Cave3DSurvey;
import com.topodroid.TDX.Cave3DFix;
// import com.topodroid.TDX.Cave3DXSection;
import com.topodroid.TDX.Geodetic;
import com.topodroid.TDX.DEMsurface;
import com.topodroid.TDX.Vector3D;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.TDInstance;

import java.io.File; // DB FILE
import java.io.IOException;
// import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ParserTh extends TglParser
{
  public static final int SUCCESS       =  0;
  public static final int ERR_NO_DB     = -1;
  public static final int ERR_NO_SURVEY = -2;
  public static final int ERR_NO_FILE   = -3;
  public static final int ERR_NO_SHOTS  = -4;

  public static final int FLIP_NONE       = 0;
  public static final int FLIP_HORIZONTAL = 1;
  public static final int FLIP_VERTICAL   = 2;

  public static final int DATA_NONE      = 0;
  public static final int DATA_NORMAL    = 1;
  public static final int DATA_DIMENSION = 2;

  public static final int TH       = 1;
  public static final int THCONFIG = 2;
  public static final int TDCONFIG = 3; // only difference with THCONFIG is doSketches = true

  public ArrayList< String > mMarks;

  DataHelper mData;

  private Cave3DCS cs1 = null;

  /** handle the command "flip"
   * @param flip   command argument
   * @return int-value of the flip
   */
  public static int parseFlip( String flip )
  {
    if ( flip.equals("horizontal") ) return FLIP_HORIZONTAL;
    if ( flip.equals("vertical") ) return FLIP_VERTICAL;
    return FLIP_NONE;
  }

  /** handle the command "mark"
   */
  private void processMarks()
  {
    for ( String mark : mMarks ) {
      String[] vals = mark.split(" ");
      int len = vals.length;
      len = prevIndex( vals, len );
      if ( len > 0 ) { // 0 must be "mark"
        int flag = parseFlag( vals[len] );
        int idx = nextIndex( vals, -1 );
        while ( idx < len && vals[idx].equals("mark") ) idx = nextIndex( vals, idx );
        while ( idx < len ) {
          Cave3DStation st = getStation( vals[idx] );
          if ( st != null ) st.setFlag( flag );
          idx = nextIndex( vals, idx );
        }
      }
    }
  }

  /** @return int-flag parsed from a string
   * @param str flag
   */
  private int parseFlag( String str ) // parse Therion flag name
  {
    if ( str.equals( "fixed" ) ) {
      return Cave3DStation.FLAG_PAINTED;
    } else if ( str.equals( "painted" ) ) {
      return Cave3DStation.FLAG_PAINTED;
    }
    return Cave3DStation.FLAG_NONE;
  }

  // FIXME isr not used
  /** cstr - parse a survey from the database
   * @param app        the 3D activity
   * @param isr        input stream (not used)
   * @param surveyname survey name
   * @param base       database folder base
   */
  public ParserTh( TopoGL app, InputStreamReader isr, String surveyname, String base ) throws ParserException
  {
    super( app, surveyname );
    mMarks = new ArrayList< String >();

    String path = base + "/distox14.sqlite";
    // TDLog.v( "Th parser DB " + path + " survey " + surveyname );
    mData = new DataHelper( app, path, TDVersion.DATABASE_VERSION );

    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.printf( String.format( mApp.getResources().getString( R.string.read_survey ), surveyname ) );

    int res = readSurvey( surveyname, "", false, 0, pw );
    if ( sw != null ) TDToast.make( sw.toString() );

    if ( res == SUCCESS ) {
      processShots();
      setShotSurveys();
      setSplaySurveys();
      setStationDepths();
      processMarks();
      // TDLog.v( "Th read survey " + surveyname );
    }
  }

  /** cstr - parse a Therion file
   * @param app      the 3D activity
   * @param isr      input stream 
   * @param filename input file path
   * @param type     not used
   */
  public ParserTh( TopoGL app, InputStreamReader isr, String filename, int type ) throws ParserException
  {
    super( app, filename );

    // TDLog.v( "Th parser, file: " + filename + " type " + type );
    mMarks = new ArrayList< String >();
    int pos = filename.indexOf("thconfig");
    if ( pos >= 0 ) {
      String path = filename.substring(0, pos) + "/distox14.sqlite";
      // TDLog.v( "Th DB " + path );
      mData = new DataHelper( app, path, TDVersion.DATABASE_VERSION );
    } else {
      mData = null;
    }

    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.printf("Read file " + filename + "\n");
    int res = readFile( isr, filename, "", false, 0.0f, 1.0, 1.0, 1.0, pw );
    // Toast.makeText( mApp, sw.toString(), Toast.LENGTH_LONG ).show();

    if ( res == SUCCESS ) {
      processShots();
      setShotSurveys();
      setSplaySurveys();
      setStationDepths();
      processMarks();

      // System.out.println("Shots    " + shots.size() );
      // System.out.println("Stations " + stations.size() );
      // System.out.println("Bounds N: " + nmin + " " + nmax );
      // System.out.println("       E: " + emin + " " + emax );
      // System.out.println("       Z: " + zmin + " " + zmax );
      // TDLog.v( "Th Shots    " + shots.size() );
      // TDLog.v( "Th Stations " + stations.size() );
      // TDLog.v( "Th Bounds N: " + nmin + " " + nmax );
      // TDLog.v( "       E: " + emin + " " + emax );
      // TDLog.v( "       Z: " + zmin + " " + zmax );
      // for ( Cave3DFix f : fixes ) {
      //   // TDLog.v( "fix " + f.name + " " + f.e + " " + f.n );
      // }
    } else {
      if ( sw != null ) TDToast.make( sw.toString() );
    }
  }

  /** compose the name of a station interposing a pathname
   * @param in     parent 
   * @param path   pathname
   */
  private String makeName( String in, String path )
  {
    int index = in.indexOf('@');
    if ( index > 0 ) {
      return in.substring(0,index) + "@" + path + "." + in.substring(index+1);
    } else {
      return in + "@" + path;
    }
  }

  /** read a survey
   * @param surveyname    name of the survey
   * @param basepath      base folder pathname
   * @param usd           whether to use the given declination
   * @param sd            specified declination
   * @param pw            error message writer
   * @return success (0) or error code
   */
  private int readSurvey( String surveyname, String basepath, boolean usd, double sd, PrintWriter pw ) // throws ParserException
  {
    if ( mData == null ) {
      if ( pw != null ) pw.printf( mApp.getResources().getString( R.string.no_database ) );
      return ERR_NO_DB; 
    }

    // TDLog.v("Th survey " + surveyname );

    // Toast.makeText( mApp, "Reading " + surveyname, Toast.LENGTH_SHORT ).show();

    SurveyInfo info = mData.getSurveyInfo( surveyname );
    if ( info == null ) {
      if ( pw != null ) pw.printf( String.format( mApp.getResources().getString( R.string.cno_survey ), surveyname ) );
      return ERR_NO_SURVEY;
    }
    long sid = info.id;

    List< DBlock > blks = mData.getSurveyShots( sid, 0 );
    if ( blks.size() == 0 ) {
      if ( pw != null ) pw.printf( String.format( mApp.getResources().getString( R.string.empty_survey ), surveyname ) );
      return ERR_NO_SHOTS;
    }

    boolean use_centerline_declination = false;
    double declination = 0.0f;
    if ( info.hasDeclination() ) {
      use_centerline_declination = true;
      declination = info.declination;
    } else if ( usd ) {
      declination = sd;
    }

    int[] survey_pos = new int[50]; // FIXME max 50 levels
    int ks = 0;
    String path = basepath;
    survey_pos[ks] = path.length();
    path = path + "." + surveyname;
    ++ks;

    for ( DBlock blk : blks ) {
      if ( blk.mFrom.length() > 0 ) {
        double ber = blk.mBearing + declination;
        if ( ber >= 360 ) ber -= 360; else if ( ber < 0 ) ber += 360;
        String from = makeName( blk.mFrom, path );
        if ( blk.mTo.length() > 0 ) {
          String to = makeName( blk.mTo, path );
          shots.add( new Cave3DShot( from, to, blk.mLength, ber, blk.mClino, blk.mFlag, blk.mMillis ) );
        } else {
          if ( mSplayUse > SPLAY_USE_SKIP ) {
            splays.add( new Cave3DShot( from, null, blk.mLength, ber, blk.mClino, blk.mFlag, blk.mMillis ) );
          }
        }
      } else if ( blk.mTo.length() > 0 ) {
        if ( mSplayUse > SPLAY_USE_SKIP ) {
          String to = makeName( blk.mTo, path );
          double ber = 180 + blk.mBearing + declination;
          if ( ber >= 360 ) ber -= 360;
          splays.add( new Cave3DShot( to, null, blk.mLength, ber, -blk.mClino, blk.mFlag, blk.mMillis ) );
        }
      }
    }

    List<SurveyFixed> fixeds = mData.getSurveyFixeds( sid );
    // TDLog.v("Th survey fixed points " + fixeds.size() + " shots " + shots.size() + " splays " + splays.size() );

    if ( fixeds != null && fixeds.size() > 0 ) {
      Cave3DCS cs0 = new Cave3DCS( );
      double PI_180 = (Math.PI / 180);
      for ( SurveyFixed fx : fixeds ) {
        // fx.log();
        String name = makeName( fx.station, path );
        double x0=0, y0=0, z0=0; // long-lat E,N,Z
        double x1=0, y1=0, z1=0; // CS1 E,N,Z

        // double a_lng = fx.mLongitude;
        double a_lat = fx.mLatitude;
        double a_alt = fx.mAltitude; // FIXME Therion altitude are geodetic not ellipsoidic
        // KML radius is already pre-multiplied by PI/180
        double s_radius = Geodetic.meridianRadiusExact( a_lat, a_alt );
        double e_radius = Geodetic.parallelRadiusExact( a_lat, a_alt );

        // TODO use a_lng a_lat a_alt

        x0 = fx.mLongitude * e_radius;
        y0 = fx.mLatitude  * s_radius;
        z0 = fx.mAltitude;
        // TDLog.v( "Th fix Long-Lat " + x0 + " " + y0 + " " + z0 + " cs1 <" + ((fx.mCsName!=null)?fx.mCsName:"null") + ">" );
        if ( mOrigin == null ) {
          // TDLog.v( "Th fix origin " + name + " " + x0 + " " + y0 + " " + z0 );
          if ( fx.hasCS() ) {
            cs1 = new Cave3DCS( fx.mCsName );
            x1 = fx.mCsLongitude;
            y1 = fx.mCsLatitude;
            z1 = fx.mCsAltitude;
            // TDLog.v( "Th fix " + name + " CS1 " + fx.mCsName + " " + x1 + " " + y1 + " " + z1 );
            mOrigin = new Cave3DFix( name, x1, y1, z1, cs1, fx.mLongitude, fx.mLatitude, fx.mAltitude );
	        fixes.add( mOrigin );
          } else {
            // TDLog.v( "Th CS0 " + x0 + " " + y0 + " " + z0 );
            mOrigin = new Cave3DFix( name, x0, y0, z0, cs0, fx.mLongitude, fx.mLatitude, fx.mAltitude );
	       fixes.add( mOrigin );
          }
        } else {
          // TDLog.v( "Th Fix relative " + name + " " + x0 + " " + y0 + " " + z0 + " cs1 " + ((fx.mCsName!=null)?fx.mCsName:"null") );
          if ( cs1 != null && cs1.equals( fx.mCsName ) ) {
            x1 = fx.mCsLongitude;
            y1 = fx.mCsLatitude;
            z1 = fx.mCsAltitude;
            // TDLog.v( "Th fix " + name + " using " + cs1.name + " " + x1 + " " + y1 + " " + z1 );
	    fixes.add( new Cave3DFix( name, x1, y1, z1, cs1, fx.mLongitude, fx.mLatitude, fx.mAltitude ) );
          } else {
            // TDLog.v( "Th use CS0 " + x0 + " " + y0 + " " + z0 );
            fixes.add( new Cave3DFix( name, x0, y0, z0, cs0, fx.mLongitude, fx.mLatitude, fx.mAltitude ) );
          }
        }
      }
    }

    // TDLog.v("Th fixes " + fixes.size() );
    return SUCCESS;
  }
  
  /** read input file
   * @param usd   whether to use given declination
   * @param sd    declination
   * @param ul units of length (as multiple of 1 meter)
   * @param ub units of bearing (as multiple of 1 degree)
   * @param uc units of clino
   * @return success (0) or error code
   */
  private int readFile( InputStreamReader isr, String filename, String basepath,
                        boolean usd, double sd,
                        double ul, double ub, double uc, PrintWriter pw )
                  throws ParserException
  {
    // if ( ! checkPath( filename ) ) {
    //   if ( pw != null ) pw.printf( String.format( mApp.getResources().getString( R.string.no_file ), filename ) );
    //   return ERR_NO_FILE;
    // }

    // Toast.makeText( mApp, "Reading " + filename, Toast.LENGTH_SHORT ).show();
    if ( isr == null ) {
      TDLog.Error("Parser Therion: null input stream reader");
      return ERR_NO_FILE;
    }

    String surveyname = "--";
    String path = basepath;
    int linenr = 0;
    // TDLog.v( "Th basepath <" + basepath + ">");
    // TDLog.v( "Th filename <" + filename + ">");
    Cave3DCS cs = null;
    int in_data = 0; // 0 none, 1 normal, 2 dimension

    int[] survey_pos = new int[50]; // FIXME max 50 levels
    int ks = 0;
    boolean in_surface = false;
    boolean in_centerline = false;
    boolean in_survey = false;
    boolean in_map = false;
    boolean use_centerline_declination = false;
    boolean use_survey_declination = usd;
    double centerline_declination = 0.0f;
    double survey_declination = sd;
    double units_len = ul;
    double units_ber = ub;
    double units_cln = uc;
    double units_grid = 1; // default units meter
    int grid_flip = FLIP_NONE;
    int flags = 0;
    long millis = 0;

    try {
      String dirname = "./";  // dirname has the trailing '/'
      int i = filename.lastIndexOf('/');
      if ( i > 0 ) dirname = filename.substring(0, i+1);
      // TDLog.v( "Th reading file " + filename + " dir " + dirname );

      // FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( isr );
      ++linenr;
      String line = br.readLine();
      // TDLog.v("Th linenr + ":" + line );
      while ( line != null ) {
        line = line.trim();
        int pos = line.indexOf( '#' );
        if ( pos >= 0 ) {
          line = line.substring( 0, pos );
        }
        if ( line.length() > 0 ) {
          String[] vals = splitLine( line );
          // TDLog.v( "[" + vals.length + "] >>" + line + "<<" );
          // for (int j=0; j<vals.length; ++j ) TDLog.v( "    " + vals[j] );

          if ( vals.length > 0 ) {
            int idx = nextIndex( vals, -1 );
            String cmd = vals[idx];
            if ( cmd.equals("survey") ) {
              idx = nextIndex( vals, idx );
              if ( idx < vals.length ) {
                surveyname = vals[idx];
                survey_pos[ks] = path.length();
                path = path + "." + vals[idx];
                // TDLog.v( "Th SURVEY " + path );
                ++ks;
                in_survey = true;
              }
            } else if ( in_map ) {
              if ( cmd.equals("endmap") ) {
                in_map = false;
              }
            } else if ( in_centerline ) {
              if ( cmd.equals("endcenterline") ) {
                in_centerline = false;
                use_centerline_declination = false;
                centerline_declination = 0.0f;
              } else if ( cmd.equals("date") ) {
                if ( (idx = nextIndex( vals, idx )) < vals.length ) {
                  String date = vals[idx];
                  if ( date.length() >= 10 ) {
                    int yy = Integer.parseInt( date.substring( 0, 4 ) );
                    String m = date.substring(5,7);
                    String d = date.substring(8,10);
                    int mm = (m.charAt(0)-'0')*10 + (m.charAt(1)-'0');
                    int dd = (d.charAt(0)-'0')*10 + (d.charAt(1)-'0');
                    Calendar cal = new GregorianCalendar( yy, mm, dd );
                    millis = cal.get( Calendar.MILLISECOND );
                  }
                }
              } else if ( cmd.equals("flags") ) { 
                if ( (idx = nextIndex( vals, idx )) < vals.length ) {
                  if ( vals[idx].equals("not") ) {
                    if ( (idx = nextIndex( vals, idx )) < vals.length ) {
                      if ( vals[idx].equals("duplicate") ) {
                        flags &= ~0x00000001;
                      } else if ( vals[idx].equals("surface") ) {
                        flags &= ~0x00000002;
                      }
                    }
                  } else {
                    if ( vals[idx].equals("duplicate") ) {
                      flags |= 0x00000001;
                    } else if ( vals[idx].equals("surface") ) {
                      flags |= 0x00000002;
                    }
                  }
                }
              } else if ( cmd.equals("team") ) { // skip
              } else if ( cmd.equals("extend") ) { // skip
              } else if ( cmd.equals("declination") ) { 
                idx = nextIndex( vals, idx );
                if ( idx < vals.length ) {
                  try {
                    double decl = Double.parseDouble( vals[idx] );
                    use_centerline_declination = true;
                    centerline_declination = decl;
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "Th Number error: centerline declination number format exception" );
                  }
                }
              } else if ( cmd.equals("station") ) {
                // TODO
              } else if ( cmd.equals("mark") ) {
                mMarks.add( line );
              } else if ( cmd.equals("data") ) {
                in_data = 0;
                // data normal from to length compass clino ...
                idx = nextIndex( vals, idx );
                if ( idx < vals.length ) {
                  if ( vals[idx].equals("normal") ) {
                    in_data = DATA_NORMAL;
                  } else if ( vals[idx].equals("dimension") ) {
                    in_data = DATA_DIMENSION;
                  } else {
                    // TODO
                  }
                }
              } else if ( cmd.equals("units") ) {
                idx = nextIndex( vals, idx );
                if ( idx < vals.length ) {
                  // parse "units" command
                  boolean isLength  = false;
                  boolean isBearing = false;
                  boolean isClino   = false;
                  double factor = 1;
                  for ( ; idx < vals.length; ++idx ) {
                    if ( vals[idx].equals("length") || vals[idx].equals("tape") ) { 
                      isLength = true;
                    } else if ( vals[idx].equals("compass") || vals[idx].equals("bearing") ) { 
                      isBearing = true;
                    } else if ( vals[idx].equals("clino") ) {
                      isClino = true;
                    } else if ( vals[idx].equals("m") || vals[idx].startsWith("meter") ) {
                      if ( isLength ) ul = factor;
                    } else if ( vals[idx].equals("cm") || vals[idx].startsWith("centimeter") ) {
                      if ( isLength ) ul = factor/100;
                    } else if ( vals[idx].startsWith("degree") ) {
                      if ( isBearing ) ub = factor;
                      if ( isClino )   uc = factor;
                    } else if ( vals[idx].startsWith("grad") ) {
                      if ( isBearing ) ub = (factor*360)/400.0f;
                      if ( isClino )   uc = (factor*360)/400.0f;
                    } else if ( vals[idx].length() > 0 ) {
                      try {
                        factor = Double.parseDouble( vals[idx] );
                      } catch ( NumberFormatException e ) { 
                        TDLog.Error( "Th Number error " + e.getMessage() );
                      }
                    }
                  } 
                }
              } else if ( cmd.equals("cs") ) { // ***** fix station east north Z
                idx = nextIndex( vals, idx );
                if ( idx < vals.length ) {
                  cs = new Cave3DCS( vals[idx] );
                }
              } else if ( cmd.equals("fix") ) { // ***** fix station east north Z
                // TDLog.v( "Th command fix");
                idx = nextIndex( vals, idx );
                if ( idx < vals.length ) {
                  String name = makeName( vals[idx], path );
                  // TDLog.v( "Th command fix " + name );
                  try { 
                    idx = nextIndex( vals, idx );
                    if ( idx < vals.length ) {
                      double x = Double.parseDouble( vals[idx] );
                      // TDLog.v( "Th fix x " + x );
                      idx = nextIndex( vals, idx );
                      if ( idx < vals.length ) {
                        double y = Double.parseDouble( vals[idx] );
                        // TDLog.v( "Th fix y " + y );
                        idx = nextIndex( vals, idx );
                        if ( idx < vals.length ) {
                          double z = Double.parseDouble( vals[idx] );
	                  fixes.add( new Cave3DFix( name, x, y, z, cs ) );
                          // TDLog.v( "Th adding fix " + x + " " + y + " " + z );
                        }
                      }
                    }
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "Th Fix station error: " + e.getMessage() );
                  }
                }
              } else if ( vals.length >= 5 ) {
                if ( in_data == DATA_NORMAL ) {
                  String from = vals[idx];
                  idx = nextIndex( vals, idx );
                  if ( idx < vals.length ) {
                    String to = vals[idx]; 
                    try {
                      idx = nextIndex( vals, idx );
                      if ( idx < vals.length ) {
                        double len  = Double.parseDouble( vals[idx] ) * units_len;
                        idx = nextIndex( vals, idx );
                        if ( idx < vals.length ) {
                          double ber  = Double.parseDouble( vals[idx] ) * units_len;
                          if ( use_centerline_declination ) {
                            ber += centerline_declination;
                          } else if ( use_survey_declination ) {
                            ber += survey_declination;
                          }
                          idx = nextIndex( vals, idx );
                          if ( idx < vals.length ) {
                            double cln  = Double.parseDouble( vals[idx] ) * units_len;
                            // TODO add shot
                            if ( to.equals("-") || to.equals(".") ) {
                              // TODO splay shot
                              if ( mSplayUse > SPLAY_USE_SKIP ) {
                                from = makeName( from, path );
                                to = null;
                                splays.add( new Cave3DShot( from, to, len, ber, cln, flags, millis ) );
                              }
                            } else {
                              from = makeName( from, path );
                              to   = makeName( to, path );
                              // StringWriter sw = new StringWriter();
                              // PrintWriter pw = new PrintWriter( sw );
                              // pw.format(Locale.US, "%s %s %.2f %.1f %.1f", from, to, len, ber, cln );
                              // TDLog.v( "Th " + sw.getBuffer().toString() );
                              shots.add( new Cave3DShot( from, to, len, ber, cln, flags, millis ) );
                            }
                          }
                        }
                      }
                    } catch ( NumberFormatException e ) {
                      TDLog.Error("Th Shot data error: " + e.getMessage() );
                    }
                  }
                } else if ( in_data == DATA_DIMENSION ) {
                  // TODO
                }
              }            
            } else if ( in_surface ) {
              if ( cmd.equals("endsurface") ) {
                in_surface = false;
              } else if ( cmd.equals("grid") ) {
                grid_flip = FLIP_NONE;
                units_grid = 1;
                mSurface = null;

                try {
                  double e1, n1, delta_e, delta_n;
                  int c1, c2;
                  // parse grid metadata
                  idx = nextIndex( vals, idx );
                  if ( idx < vals.length ) {
                    e1 = Double.parseDouble( vals[idx] );
                    idx = nextIndex( vals, idx );
                    if ( idx < vals.length ) {
                      n1 = Double.parseDouble( vals[idx] );
                      idx = nextIndex( vals, idx );
                      if ( idx < vals.length ) {
                        delta_e = Double.parseDouble( vals[idx] );
                        idx = nextIndex( vals, idx );
                        if ( idx < vals.length ) {
                          delta_n = Double.parseDouble( vals[idx] );
                          idx = nextIndex( vals, idx );
                          if ( idx < vals.length ) {
                            c1 = Integer.parseInt( vals[idx] );
                            idx = nextIndex( vals, idx );
                            if ( idx < vals.length ) {
                              c2 = Integer.parseInt( vals[idx] );
                              mSurface = new DEMsurface( e1, n1, delta_e, delta_n, c1, c2 );
                              // TDLog.v( "Th Surface " + e1 + "-" + n1 + " " + e2 + "-" + n2 + " " + c1 + "x" + c2);
                            }
                          }
                        }
                      }
                    }
                  }
                } catch ( NumberFormatException e ) {
                  TDLog.Error( "Th surface grid metadata " + e.getMessage() );
                }
                // and read grid data
                if ( mSurface != null ) {
                  mSurface.readGridData( units_grid, grid_flip, br, filename );
                }
              } else if ( cmd.equals("grid-flip") ) {
                // TDLog.v("Th parse the flip-value" );
                idx = nextIndex( vals, idx );
                if ( idx < vals.length ) {
                  grid_flip = parseFlip( vals[idx] );
                }
              } else if ( cmd.equals("grid-units") ) {
                // TDLog.v("Th parse the grid-units" );
                try {
                  idx = nextIndex( vals, idx );
                  if ( idx < vals.length ) {
                    double value = Double.parseDouble( vals[idx] );
                    idx = nextIndex( vals, idx );
                    if ( idx < vals.length ) {
                      // FIXME TODO
                      // units_grid = parseUnits( value, vals[idx] );
                    }
                  }
                } catch ( NumberFormatException e ) {
                  TDLog.Error( "Th surface grid units " + e.getMessage() );
                }
              }
            } else if ( cmd.equals("declination") ) {
              try {
                idx = nextIndex( vals, idx );
                if ( idx < vals.length ) {
                  use_survey_declination = true;
                  survey_declination = Double.parseDouble( vals[idx] );
                }
              } catch ( NumberFormatException e ) {
                TDLog.Error( "Th survey declination " + e.getMessage() );
              }
            } else if ( cmd.equals("input") ) {
              idx = nextIndex( vals, idx );
              if ( idx < vals.length ) {
                filename = vals[idx];
                // TDLog.v( "Th file " + filename );
                if ( filename.toLowerCase( Locale.getDefault() ).endsWith( ".th" ) ) {
                  String filepath = dirname + filename; // dirname + '/' + filename
                  InputStreamReader isr0 = new InputStreamReader( new FileInputStream( filepath ) );
                  int res = readFile( isr0, filepath, path,
                                   use_survey_declination, survey_declination,
                                   units_len, units_ber, units_cln, pw );
                  if ( res != SUCCESS ) {
                    TDLog.Error( "Th read file " + filename + " failed. Error code " + res );
                    TDToast.makeBad( TDInstance.formatString( R.string.error_file_read, filename ) );
                  }
                } else {
                  TDLog.Error( "Th Input file <" + filename + "> has no .th extension");
                }
              }
            } else if ( cmd.equals("load") ) {
              idx = nextIndex( vals, idx );
              if ( idx < vals.length ) {
                filename = vals[idx]; // survey name
                // TDLog.v( "Th survey " + filename );
                if ( mData == null ) {
                  String base = null;
                  if ( dirname.toLowerCase( Locale.getDefault() ).endsWith( "tdconfig/" ) ) {
                    // base = dirname.replace( "tdconfig/", "" );
                    base = dirname.substring( 0, dirname.length()-9 );
                    i = base.lastIndexOf('/');
                    if ( i > 0 && i < base.length() ) base = base.substring(0, i+1);
                  } else {
                    base = dirname;
                  }
                  String db_path = base + "/distox14.sqlite";
                  // TDLog.v( "Th DB " + db_path );
                  if ( (new File(db_path)).exists() ) { // DB FILE
                    mData = new DataHelper( mApp, db_path, TDVersion.DATABASE_VERSION );
                  }
                }
                int res = readSurvey( filename, path, use_survey_declination, survey_declination, pw );
                if ( res != SUCCESS ) {
                  TDLog.Error( "Th read survey " + filename + " failed. Error code " + res );
                  TDToast.makeBad( TDInstance.formatString( R.string.error_survey_read, filename ) );
                }
              }
            } else if ( cmd.equals("equate") ) {
              idx = nextIndex( vals, idx );
              if ( idx < vals.length ) {
                String from = makeName( vals[idx], path );
                while ( idx < vals.length ) {
                  idx = nextIndex( vals, idx );
                  if ( idx < vals.length ) {
		    String to = makeName( vals[idx], path );
                    // StringWriter sw = new StringWriter();
                    // PrintWriter pw = new PrintWriter( sw );
                    // pw.format(Locale.US, "EQUATE %s %s 0.00 0.0 0.0", from, to );
                    // TDLog.v( "Th " + sw.getBuffer().toString() );
                    // TDLog.v( "Th Equate " + from + " " + to );
                    shots.add( new Cave3DShot( from, to, 0.0f, 0.0f, 0.0f, 0, 0 ) );
                  }
                }
              }
            } else if ( cmd.equals("surface") ) {
              in_surface = true;
            } else if ( cmd.equals("centerline") ) {
              in_centerline = true;
            } else if ( cmd.equals("map") ) {
              in_map = true;
            } else if ( cmd.equals("endsurvey") ) {
              --ks;
              if ( ks < 0 ) {
                TDLog.Error( "Th " + filename + ":" + linenr + " negative survey level" );
              } else {
                path = path.substring(0, survey_pos[ks]); // return to previous survey_pos in path
                // TDLog.v( "Th endsurvey PATH " + path );
                in_survey = ( ks > 0 );
              }
            }
          }
        }
        ++linenr;
        line = br.readLine();
        // TDLog.v( "Th " + linenr + ":" + line );
      }
    } catch ( IOException e ) {
      TDLog.Error( "Th IO error " + e.getMessage() );
      throw new ParserException( filename, linenr );
    } catch ( NullPointerException e ) { // FIXME ANDROID-11
      TDLog.Error( "Th IO null ptr " + e.getMessage() ); 
      throw new ParserException( filename, linenr );
    }
    // TDLog.v( "Th Done readFile " + filename );

    if ( shots.size() <= 0 ) {
      if ( pw != null ) pw.printf( String.format( mApp.getResources().getString( R.string.empty_survey ), surveyname ) );
      return ERR_NO_SHOTS;
    }
    return SUCCESS;
  }

  /** assign the surveys to the leg shots
   */
  private void setShotSurveys()
  {
    for ( Cave3DShot sh : shots ) {
      Cave3DStation sf = sh.from_station;
      Cave3DStation st = sh.to_station;
      sh.mSurvey = null;
      if ( sf != null && st != null ) {
        String sv = sh.from;
        sv = sv.substring( 1 + sv.indexOf('@', 0) );
        for ( Cave3DSurvey srv : surveys ) {
          if ( srv.hasName( sv ) ) {
            // sh.mSurvey = srv;
            // sh.mSurveyNr = srv.number;
            // srv.addShotInfo( sh );
            // srv.addShotInfo( sh );
            srv.addShot( sh );
            break;
          }
        }
        if ( sh.mSurvey == null ) {
          Cave3DSurvey survey = new Cave3DSurvey(sv);
          // sh.mSurvey = survey;
          // sh.mSurveyNr = survey.number;
          // survey.addShotInfo( sh );
          surveys.add( survey );
          survey.addShot( sh );
        } 
      }
    }
  }

  /** assign the surveys to the splay shots
   */
  private void setSplaySurveys()
  {
    if ( mSplayUse == SPLAY_USE_SKIP ) return;
    for ( Cave3DShot sh : splays ) {
      String sv = null;
      Cave3DStation sf = sh.from_station;
      if ( sf == null ) {
        sf = sh.to_station;
        sv = sh.to;
      } else {
        sv = sh.from;
      }
      if ( sf != null ) {
        sv = sv.substring( 1 + sv.indexOf('@', 0) );
        for ( Cave3DSurvey srv : surveys ) {
          if ( srv.hasName( sv ) ) {
            // sh.mSurvey   = srv;
            // sh.mSurveyNr = srv.number;
            // srv.addSplayInfo( sh );
            srv.addSplay( sh );
            break;
          }
        }
      }
    }
  }

  /** process the shots
   */
  private void processShots()
  {
    if ( shots.size() == 0 ) return;
    // TDLog.v( "Th shots " + shots.size() + " fixes " + fixes.size() );
    ArrayList< Cave3DFix > ok_fixes = new ArrayList<>(); // array of fixed stations that are in the survey

    int bad_fixes = 0;
    for ( Cave3DFix f : fixes ) {
      boolean found = false; 
      // when fixes are checked stations may not have been created yet, therefore the check runs on the shots
      for ( Cave3DShot s1 : shots ) { // HB
          if ( f.hasName( s1.from ) ) { found = true; break ; }
          if ( f.hasName( s1.to ) ) { found = true; break ; }
      }
      if ( found ) {
        ok_fixes.add( f );
      } else {
        bad_fixes ++;
      }
    }
    if ( bad_fixes > 0 ) {
      TDToast.makeWarn( R.string.error_bad_fixes );
    }

    if ( ok_fixes.size() == 0 ) {
      Cave3DShot sh = shots.get( 0 );
      ok_fixes.add( new Cave3DFix( sh.from, 0.0f, 0.0f, 0.0f, null ) );
    }
 
    int mLoopCnt = 0;
    Cave3DFix f0 = ok_fixes.get( 0 );
    // TDLog.v( "Th Process Shots. Fix " + f0.getFullName() + " " + f0.x + " " + f0.y + " " + f0.z );

    mCaveLength = 0.0f;
    // TDLog.v( "Th shots " + shots.size() + " splays " + splays.size() + " ok_fixes " + ok_fixes.size() );

    // for ( Cave3DShot sh : shots ) {
    //   // TDLog.v( "Th shot " + sh.from + " " + sh.to );
    // }

    int used_cnt = 0; // number of used shots
    for ( Cave3DFix f : ok_fixes ) {
      // TDLog.v( "Th checking fix " + f.getFullName() );
      boolean found = false;
      for ( Cave3DStation s1 : stations ) {
        if ( f.hasName( s1.getFullName() ) ) { found = true; break; }
      }
      if ( found ) { // skip fixed stations that are already included in the model
        // TDLog.v( "Th fix " + f.getFullName() + " already used" );
        continue;
      }
      // TDLog.v( "Th add start station " + f.getFullName() + " N " + f.y + " E " + f.x + " Z " + f.z );
      stations.add( new Cave3DStation( f.getFullName(), f.x, f.y, f.z ) );
      // sh.from_station = s0;

      boolean repeat = true;
      while ( repeat ) {
        // TDLog.v( "Th scanning the shots");
        repeat = false;
        for ( Cave3DShot sh : shots ) {
          if ( sh.isUsed() ) continue;
          // TDLog.v( "Th check shot " + sh.from + " " + sh.to );
          // Cave3DStation sf = sh.from_station;
          // Cave3DStation st = sh.to_station;
          Cave3DStation sf = null;
          Cave3DStation st = null;
          for ( Cave3DStation s : stations ) {
            if ( s.hasName( sh.from ) ) {
              sf = s;
              if (  sh.from_station == null ) sh.from_station = s;
              else if ( sh.from_station != s ) TDLog.Error( "Th shot " + sh.from + " " + sh.to + " from-station mismatch ");
            } 
            if ( s.hasName( sh.to ) )   {
              st = s;
              if (  sh.to_station == null ) sh.to_station = s;
              else if ( sh.to_station != s ) TDLog.Error( "Th shot " + sh.from + " " + sh.to + " to-station mismatch ");
            }
            if ( sf != null && st != null ) break;
          }
          if ( sf != null && st != null ) {
            // TDLog.v( "Th using loop-closing shot " + sh.from + " " + sh.to + " : " + sf.name + " " + st.name );
            sh.setUsed(); // LOOP
	    ++ used_cnt;
            mCaveLength += sh.length();
            // make a fake station
            Cave3DStation s = sh.getStationFromStation( sf );
            stations.add( s );
            s.addToName( mLoopCnt ); // s.name = s.name + "-" + mLoopCnt;
            ++ mLoopCnt;
            sh.to_station = s;
            repeat = true; // unnecessary
          } else if ( sf != null && st == null ) {
            // TDLog.v( "Th using forward shot " + sh.from + " " + sh.to + " : " + sf.name + " null" );
            Cave3DStation s = sh.getStationFromStation( sf );
            stations.add( s );
            sh.to_station = s;
            // TDLog.v( "Th add station TO " + sh.from + " " + sh.to + " " + sh.to_station.name );
            sh.setUsed();
	    ++ used_cnt;
            mCaveLength += sh.length();
            repeat = true;
          } else if ( sf == null && st != null ) {
            // TDLog.v( "Th using backward shot " + sh.from + " " + sh.to + " : null " + st.name );
            Cave3DStation s = sh.getStationFromStation( st );
            stations.add( s );
            sh.from_station = s;
            // TDLog.v( "Th add station FR " + sh.from + " " + sh.to + " " + sh.from_station.name  );
            sh.setUsed();
	    ++ used_cnt;
            mCaveLength += sh.length();
            repeat = true;
          } else {
            // TDLog.v( "Th unused shot " + sh.from + " " + sh.to + " : null null" );
          }
        }
      }
      // TDLog.v( "Th after " + f.getFullName() + " used shot " + used_cnt + " loops " + mLoopCnt );
    } // for ( Cave3DFix f : ok_fixes )
    // TDLog.v( "Th used shot " + used_cnt + " loops " + mLoopCnt + " total shots " + shots.size() );
    // StringBuilder sb = new StringBuilder();
    // for ( Cave3DStation st : stations ) { sb.append(" "); sb.append( st.name ); }
    // TDLog.v( "Th " + sb.toString() );

    if ( mSplayUse > SPLAY_USE_SKIP ) {
      // 3D splay shots
      Vector3D vert = new Vector3D( 0,0,1 ); // vertical

      for ( Cave3DStation s : stations ) {
        ArrayList< Vector3D > station_splays = new ArrayList<>();
        for ( Cave3DShot sh : splays ) {
          if ( sh.isUsed() ) continue;
          if ( sh.from_station != null ) continue;
          if ( s.hasName( sh.from ) ) {
            sh.from_station = s;
            sh.setUsed();
            sh.to_station = sh.getStationFromStation( s );
            station_splays.add( sh.toPoint3D() );
          }
        }
        // FIXME XSECTIONS
        // if ( TglParser.mSplayUse == TglParser.SPLAY_USE_XSECTION && station_splays.size() > 3 ) {
        //   xsections.add( new Cave3DXSection( s, s, vert, station_splays ) );
        // }
      }
    }

    computeBoundingBox();
    // TDLog.v("Th stations " + stations.size() + " center " + x0 + " " + y0 + " " + z0 );
    // TDLog.v("Th bbox E " + emin + " " + emax + " N " + nmin + " " + nmax );
  }

  /** @return the next index of non-empty string in an array
   * @param vals   string array
   * @param idx    start from the index after this
   */
  public static int nextIndex( String[] vals, int idx )
  {
    ++idx;
    while ( idx < vals.length && vals[idx].length() == 0 ) ++idx;
    return idx;
  }

  /** @return the previous index of non-empty string in an array
   * @param vals   string array
   * @param idx    start from the index before this
   */
  public static int prevIndex( String[] vals, int idx )
  {
    --idx;
    while ( idx >= 0 && vals[idx].length() == 0 ) --idx;
    return idx;
  }

}
