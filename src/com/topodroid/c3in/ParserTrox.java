/** @file ParserTrox.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief VisualTopo trox file parser
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import com.topodroid.utils.TDLog;

import com.topodroid.TDX.TopoGL;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.Cave3DCS;
import com.topodroid.TDX.Cave3DSurvey;
import com.topodroid.TDX.Cave3DFix;
import com.topodroid.TDX.Cave3DShot;
import com.topodroid.TDX.Cave3DStation;

// import java.io.File;
import java.io.IOException;
// import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
// import java.io.StringWriter;
// import java.io.PrintWriter;
// import java.util.ArrayList;

public class ParserTrox extends TglParser
{
  public static final int FLIP_NONE       = 0;
  public static final int FLIP_HORIZONTAL = 1;
  public static final int FLIP_VERTICAL   = 2;

  public static final int DATA_NONE      = 0;
  public static final int DATA_NORMAL    = 1;
  public static final int DATA_DIMENSION = 2;

  double mDeclination = 0.0;
  boolean mApplyDeclination = false;
  boolean dmb = false; // whether bearing is DD.MM
  boolean dmc = false;
  float ul = 1;  // units factor [m]
  float ub = 1;  // dec.deg
  float uc = 1;  // dec.deg
  int dir_w = 1;  // width direction
  int dir_b = 1;  // bearing direction
  int dir_c = 1;  // clino direction


  public ParserTrox( TopoGL app, InputStreamReader isr, String name ) throws ParserException
  {
    super( app, name );

    readFile( isr );
    processShots();
    setShotSurveys();
    setSplaySurveys();
    setStationDepths();
  }

  private static boolean isDuplicate( String flag )
  {
    if ( flag == null ) return false;
    return ( flag.indexOf('L') >= 0 );
  }

  private static boolean isSurface( String flag )
  {
    if ( flag == null ) return false;
    return ( flag.indexOf('X') >= 0 );
  }

  private static float angle( float value, float unit, boolean dm )
  {
    if ( dm ) {
      int sign = 1;
      if ( value < 0 ) { sign = -1; value = -value; }
      int iv = (int)value;
      return sign * ( iv + (value-iv)*0.6f ); // 0.6 = 60/100
    }
    return value * unit;
  }

  static private String getValue( String key, String line ) { return getValue( key, "\"", line ); }

  static private String getValue( String key, String end, String line )
  {
    int pos = line.indexOf( key );
    if ( pos < 0 ) return null;
    pos += key.length();
    int qos = line.indexOf( end, pos );
    if ( qos > pos ) return line.substring( pos, qos );
    return null;
  }

  static private float getFloatValue( String key, String line, float val)
  {
    int pos = line.indexOf( key );
    if ( pos < 0 ) return val;
    pos += key.length();
    int qos = line.indexOf( "\"", pos );
    if ( qos <= pos ) return val;
    try {
      return Float.parseFloat( line.substring( pos, qos ) );
    } catch ( NumberFormatException e ) {
      TDLog.Error("Non-number value");
    }
    return val;
  }

  // @param cs   VTopo Coordinate System
  private void setTroxFix( String entrance, double x, double y, double z, String cs )
  {
    fixes.add( new Cave3DFix( entrance, x, y, z, new Cave3DCS( cs ) ) ); // FIXME
  }

  /** read input TRO file
   */
  private boolean readFile( InputStreamReader isr ) throws ParserException
  {
    int linenr = 0;
    String mEntrance = null;
    String mCS = null;
    double mLat = 0;
    double mLng = 0;
    double mAlt = 0;
    // Cave3DCS cs = null;
    // int in_data = 0; // 0 none, 1 normal, 2 dimension

    // String survey = null; // UNUSED

    String last_to = "";
    String mFrom = null;
    String mTo   = null;
    float mLength  = 0;
    float mBearing = 0;
    float mClino   = 0;
    float mLeft    = 0;
    float mRight   = 0;
    float mUp      = 0;
    float mDown    = 0;
    boolean splayAtFrom = true;
    String comment = "";
    long millis = 0;
    boolean duplicate = false;
    final boolean surface   = false; // TODO ...

    String line = null;
    try {
      BufferedReader br = new BufferedReader( isr );
      ++linenr;
      line = br.readLine();
      if ( line == null ) return false;
      line = line.trim().replaceAll("\\s+", " ");
      if ( ! line.startsWith( "<?xml " ) ) { // NOT XML
        TDLog.v( "VTopo trox not an xml file");
        return false;
      }

      boolean inCavite  = false;
      boolean inMesures = false;
      boolean inVisee   = false;
      String value;
      int cnt_splay = 0;
      int cnt_shot  = 0;
      boolean isSplay = false;

      for ( ; ; ) {
        ++linenr;
        line = br.readLine();
        if ( line == null ) break;
        line = line.trim().replaceAll("\\s+", " ");
        // TDLog.v( "LINE: " + line );

        if ( line.startsWith("<Cavite>") )          { inCavite = true;
        } else if ( line.startsWith("</Cavite>") )  { inCavite = false;
        } else if ( line.startsWith("<Mesures>") )  { inMesures = true;
        } else if ( line.startsWith("</Mesures>") ) { inMesures = false; 
        } else if ( line.startsWith("<Visee") && inMesures ) {
          if ( ! line.endsWith( "/>" ) ) inVisee = true;
          comment = "";
          mFrom = getValue( "Dep=\"", line );
          mTo   = getValue( "Arr=\"", line );
          isSplay = (mTo == null);
          if ( mFrom == null ) mFrom = last_to;
          if ( mTo != null ) last_to = mTo;
          // station = ( (splayAtFrom || isSplay )? mFrom : mTo );
          mLength  = getFloatValue("Long=\"", line, 0) * ul; 
          mBearing = angle( getFloatValue("Az=\"", line, 0), ub, dmb); 
          mClino   = angle( getFloatValue("Pte=\"", line, 0), uc, dmc);
          if ( mApplyDeclination ) mBearing += mDeclination;
          while ( mBearing < 0 ) mBearing += 360;
          while ( mBearing >= 360 ) mBearing -= 360;
          if ( ! isSplay ) {
            mLeft  = getFloatValue( "G=\"", line, -1 ) * ul;
            mRight = getFloatValue( "D=\"", line, -1 ) * ul;
            mUp    = getFloatValue( "H=\"", line, -1 ) * ul;
            mDown  = getFloatValue( "B=\"", line, -1 ) * ul;
          }
          // shot_extend = ExtendType.EXTEND_RIGHT;
          if ( ( value = getValue( "Inv=\"", line ) ) != null ) {
            // if ( value.equals("I") ) shot_extend = ExtendType.EXTEND_LEFT;
          }
          duplicate = false;
          if ( ( value = getValue( "Exc=\"", line ) ) != null ) {
            if ( value.equals("E") ) duplicate = true;
          }
          if ( ! inVisee ) {
            if ( isSplay ) {
              if ( mSplayUse > SPLAY_USE_SKIP ) {
                splays.add( new Cave3DShot( mFrom, mFrom + cnt_splay, mLength, mBearing, mClino, 0, millis ) );
                ++ cnt_splay;
              }
            } else {
              shots.add( new Cave3DShot( mFrom, mTo, mLength, mBearing, mClino, 0, millis ) );
              ++ cnt_shot;
              if ( mSplayUse > SPLAY_USE_SKIP ) {
                String station = (splayAtFrom || isSplay)? mFrom : mTo;
	        if ( mLeft  > 0 ) splays.add( new Cave3DShot( station, station+"-L", mLeft,  mBearing-90, 0, 0, millis ) );
	        if ( mRight > 0 ) splays.add( new Cave3DShot( station, station+"-R", mRight, mBearing+90, 0, 0, millis ) );
	        if ( mUp    > 0 ) splays.add( new Cave3DShot( station, station+"-U", mUp,    mBearing,   90, 0, millis ) );
	        if ( mDown  > 0 ) splays.add( new Cave3DShot( station, station+"-D", mDown,  mBearing,  -90, 0, millis ) );
              }
            }
          }
        } else if ( line.startsWith("</Visee>") && inMesures ) { 
          if ( inVisee ) {
            if ( isSplay ) {
              if ( mSplayUse > SPLAY_USE_SKIP ) {
                splays.add( new Cave3DShot( mFrom, mFrom + cnt_splay, mLength, mBearing, mClino, 0, millis ) );
                ++ cnt_splay;
              }
            } else {
              shots.add( new Cave3DShot( mFrom, mTo, mLength, mBearing, mClino, 0, millis ) );
              ++ cnt_shot;
              if ( mSplayUse > SPLAY_USE_SKIP ) {
                String station = (splayAtFrom || isSplay)? mFrom : mTo;
	        if ( mLeft  > 0 ) splays.add( new Cave3DShot( station, station+"-L", mLeft,  mBearing-90, 0, 0, millis ) );
	        if ( mRight > 0 ) splays.add( new Cave3DShot( station, station+"-R", mRight, mBearing+90, 0, 0, millis ) );
	        if ( mUp    > 0 ) splays.add( new Cave3DShot( station, station+"-U", mUp,    mBearing,   90, 0, millis ) );
	        if ( mDown  > 0 ) splays.add( new Cave3DShot( station, station+"-D", mDown,  mBearing,  -90, 0, millis ) );
              }
            }
          }
          inVisee = false;
        } else if ( line.startsWith("<Param>") && inMesures ) {
          if ( "Deca".equals( getValue( "InstrDist=\"", line ) ) ) {
            ub = 1; dmb = false;
            uc = 1; dmc = false;
            if ( ( value = getValue( "UnitDir=\"", line ) ) != null ) {
              if ( "Deg".equals( value ) ) { 
                dmb = true;
              } else if ( "Gra".equals( value ) ) {
                ub = 0.9f; // 360/400
              } else if ( "Degd".equals( value ) ) {
                // nothing: ub = 1, dmb = false
              }
            }
          }
          if ( "Clino".equals( getValue( "InstrPte=\"", line ) ) ) {
            if ( ( value = getValue( "UnitPte=\"", line ) ) != null ) {
              if ( "Deg".equals( value ) ) { 
                dmc = true;
              } else if ( "Gra".equals( value ) ) {
                uc = 0.9f; // 360/400
              } else if ( "Degd".equals( value ) ) {
                // nothing: uc = 1, dmc = false
              }
            }
          }
          mDeclination = getFloatValue( "Declin=\"", line, 999 ); // SurveyInfo.DECLINATION_UNSET );
          if ( ( value = getValue( "DeclinAuto=\"", line ) ) != null ) {
            mApplyDeclination = value.equals("M");
          }
          if ( "Inv".equals( getValue( "SensDir=\"", line ) ) ) dir_b = -1;
          if ( "Inv".equals( getValue( "SensPte=\"", line ) ) ) dir_c = -1;
          if ( "Inv".equals( getValue( "SensLar=\"", line ) ) ) dir_w = -1;
          if ( ( value = getValue( "DimPt=\"", line ) ) != null ) {
            if ( value.equals("Inc") ) {
              // FIXME isSplay at next station: Which ???
              splayAtFrom = false;
            } else if ( value.equals("Dep") ) {
              splayAtFrom = true;
            } else if ( value.equals("Arr") ) {
              splayAtFrom = false;
            }
          }
          // TODO if ( ( value = getValue( "Date\"", line ) ) != null ) mDate = TDUtil.fromVTopoDate( value );

        } else if ( line.startsWith("<Commentaire>") ) { // TODO
          // String comm = getValue("<Commentaire>", "</Commentaire>", line );
          // if ( comm != null ) {
          //   if ( inCavite ) {
          //     mDescr = comm;
          //   } else if ( inVisee ) {
          //     comment = comm;
          //   }
          // }
        } else if ( line.startsWith("<Nom>") && inCavite ) { 
          String name = getValue("<Nom>", "</Nom>", line );
          if ( name != null ) mName = name;
        } else if ( line.startsWith("<Coordonnees" ) && inCavite ) {
          mLng = getFloatValue( "X=\"", line, 0 );
          mLat = getFloatValue( "Y=\"", line, 0 );
          mAlt = getFloatValue( "Z=\"", line, 0 );
          mCS  = getValue( "Projection=\"", line, "WGS84" ); 
          if ( mEntrance != null ) {
            setTroxFix( mEntrance, mLng, mLat, mAlt, mCS );
            mCS = null;
            mEntrance  = null;
          }
        } else if ( line.startsWith("<Club>" ) && inCavite ) {
          // String club = getValue("<Club>", "</Club>", line );
          // if ( club != null ) mTeam = club;
        } else if ( line.startsWith("<Entree>" ) && inCavite ) {
          mEntrance = getValue( "<Entree>", "</Entree>", line );
          if ( mCS != null ) {
            setTroxFix( mEntrance, mLng, mLat, mAlt, mCS );
            mCS = null;
            mEntrance  = null;
          }
        } else if ( line.startsWith("<Configuration>") ) {
          break;
        }
        // ignore if ( line.startsWith("<VisualTopo>") ) continue;
        // ignore if ( line.startsWith("<Version>") ) continue;
        // ignore if ( line.startsWith("<Lignes>") ) continue;
        // ignore if ( line.startsWith("<Toporobot>") ) continue;
        // ignore if ( line.startsWith("<Couleur>") ) continue;
      }
    } catch ( IOException e ) {
      TDLog.v( "ERROR " + linenr + ": " + line );
      throw new ParserException( getName(), linenr );
    }
    return true;
  }

  // ------------------------------------------------------------

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
            srv.addShot( sh );
            break;
          }
        }
        if ( sh.mSurvey == null ) {
          Cave3DSurvey survey = new Cave3DSurvey(sv);
          // sh.mSurvey = survey;
          // sh.mSurveyNr = survey.number;
          // survey.addShotInfo( sh );
          survey.addShot( sh );
          surveys.add( survey );
        } 
      }
    }
  }

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
            // sh.mSurvey = srv;
            // sh.mSurveyNr = srv.number;
            // srv.addSplayInfo( sh );
            srv.addSplay( sh );
            break;
          }
        }
      }
    }
  }

  private void processShots()
  {
    if ( shots.size() == 0 ) return;
    if ( fixes.size() == 0 ) {
      Cave3DShot sh = shots.get( 0 );
      fixes.add( new Cave3DFix( sh.from, 0.0f, 0.0f, 0.0f, null ) );
      // TDLog.v( "TRO shots " + shots.size() + " no fixes. starts at " + sh.from );
    }
 
    int mLoopCnt = 0;
    Cave3DFix f0 = fixes.get( 0 );
    // TDLog.v( "TRO Process Shots. Fix " + f0.name + " " + f0.x + " " + f0.y + " " + f0.z );

    mCaveLength = 0.0f;

    for ( Cave3DFix f : fixes ) {
      boolean found = false;
      // TDLog.v( "TRO checking fix " + f.name );
      for ( Cave3DStation s1 : stations ) {
        if ( s1.hasName( f.getFullName() ) ) { found = true; break; }
      }
      if ( found ) { // skip fixed stations that are already included in the model
        // TDLog.v( "TRO found fix " + f.name );
        continue;
      }
      // TDLog.v( "TRO start station " + f.name + " N " + f.y + " E " + f.x + " Z " + f.z );
      stations.add( new Cave3DStation( f.getFullName(), f.x, f.y, f.z ) );
      // sh.from_station = s0;
    
      boolean repeat = true;
      while ( repeat ) {
        // TDLog.v( "TRO scanning the shots");
        repeat = false;
        for ( Cave3DShot sh : shots ) {
          if ( sh.isUsed() ) continue;
          // TDLog.v( "TRO check shot " + sh.from + " " + sh.to );
          // Cave3DStation sf = sh.from_station;
          // Cave3DStation st = sh.to_station;
          Cave3DStation sf = null;
          Cave3DStation st = null;
          for ( Cave3DStation s : stations ) {
            if ( s.hasName( sh.from ) ) {
              sf = s;
              if (  sh.from_station == null ) sh.from_station = s;
              else if ( sh.from_station != s ) TDLog.Error( "TRO shot " + sh.from + " " + sh.to + " from-station mismatch ");
            } 
            if ( s.hasName( sh.to ) )   {
              st = s;
              if (  sh.to_station == null ) sh.to_station = s;
              else if ( sh.to_station != s ) TDLog.Error( "TRO shot " + sh.from + " " + sh.to + " to-station mismatch ");
            }
            if ( sf != null && st != null ) break;
          }
          if ( sf != null && st != null ) {
            // TDLog.v( "TRO unused shot " + sh.from + " " + sh.to + " : " + sf.name + " " + st.name );
            sh.setUsed( ); // LOOP
            mCaveLength += sh.length();
            // make a fake station
            Cave3DStation s = sh.getStationFromStation( sf );
            stations.add( s );
            s.addToName( mLoopCnt ); // s.name = s.name + "-" + mLoopCnt;
            ++ mLoopCnt;
            sh.to_station = s;
          } else if ( sf != null && st == null ) {
            // TDLog.v( "TRO unused shot " + sh.from + " " + sh.to + " : " + sf.name + " null" );
            Cave3DStation s = sh.getStationFromStation( sf );
            stations.add( s );
            sh.to_station = s;
            // TDLog.v("TRO add station " + sh.to_station.name + " N " + sh.to_station.n + " E " + sh.to_station.e + " Z " + sh.to_station.z );
            sh.setUsed( );
            mCaveLength += sh.length();
            repeat = true;
          } else if ( sf == null && st != null ) {
            // TDLog.v( "TRO unused shot " + sh.from + " " + sh.to + " : null " + st.name );
            Cave3DStation s = sh.getStationFromStation( st );
            stations.add( s );
            sh.from_station = s;
            // TDLog.v("TRO add station " + sh.from_station.name + " N " + sh.from_station.n + " E " + sh.from_station.e + " Z " + sh.from_station.z );
            sh.setUsed( );
            mCaveLength += sh.length();
            repeat = true;
          } else {
            // TDLog.v( "TRO unused shot " + sh.from + " " + sh.to + " : null null" );
          }
        }
      }
    } // for ( Cave3DFix f : fixes )

    // 3D splay shots
    if ( mSplayUse > SPLAY_USE_SKIP ) {
      for ( Cave3DShot sh : splays ) {
        if ( sh.isUsed() ) continue;
        if (  sh.from_station != null ) continue;
        // TDLog.v("TRO check shot " + sh.from + " " + sh.to );
        for ( Cave3DStation s : stations ) {
          if ( s.hasName( sh.from ) ) {
            sh.from_station = s;
            sh.setUsed( );
            sh.to_station = sh.getStationFromStation( s );
            break;
          }
        }
      }
    }

    computeBoundingBox();
    // // bounding box
    // emin = emax = stations.get(0).e;
    // nmin = nmax = stations.get(0).n;
    // zmin = zmax = stations.get(0).z;
    // for ( Cave3DStation s : stations ) {
    //   if ( nmin > s.n )      nmin = s.n;
    //   else if ( nmax < s.n ) nmax = s.n;
    //   if ( emin > s.e )      emin = s.e;
    //   else if ( emax < s.e ) emax = s.e;
    //   if ( zmin > s.z )      zmin = s.z;
    //   else if ( zmax < s.z ) zmax = s.z;
    // }
  }

  static int nextIndex( String[] vals, int idx )
  {
    ++idx;
    while ( idx < vals.length && vals[idx].length() == 0 ) ++idx;
    return idx;
  }

  static int prevIndex( String[] vals, int idx )
  {
    --idx;
    while ( idx >= 0 && vals[idx].length() == 0 ) --idx;
    return idx;
  }

}
