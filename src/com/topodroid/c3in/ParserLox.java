/** @file ParserLox.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief loch file parser 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import com.topodroid.TDX.TopoGL;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Cave3DShot;
import com.topodroid.TDX.Cave3DSurvey;
import com.topodroid.TDX.DEMsurface;

// import com.topodroid.utils.TDLog;

// import java.io.File;
import java.io.DataInputStream;
// import java.io.IOException;

import java.util.ArrayList;

public class ParserLox extends TglParser
{
  private static double RAD2DEG = (180/Math.PI);

  // @param dis    input stream
  // @param name   name for error report
  public ParserLox( TopoGL app, DataInputStream dis, String name ) throws ParserException
  {
    super( app, name );
    readfile( dis );
  }

  private static LoxSurvey getLoxSurvey( int id, ArrayList< LoxSurvey > lox_surveys ) 
  { 
    if ( id < 0 ) return null;
    for ( LoxSurvey lox_survey : lox_surveys ) if ( lox_survey.id == id ) return lox_survey;
    return null;
  }

  private static String getLoxSurveyFullname( LoxSurvey lox_survey, ArrayList< LoxSurvey > lox_surveys )
  {
    if ( lox_survey.pid > 0 ) {
      LoxSurvey parent = getLoxSurvey( lox_survey.pid, lox_surveys );
      if ( parent != null ) {
        String parent_name = getLoxSurveyFullname( parent, lox_surveys );
        if ( parent_name.length() > 0 ) {
          return parent_name + "." + lox_survey.name;
        }
      }
    }
    // parent null or empty name
    return lox_survey.name; 
  }

  // @param dis    input stream
  // @param name   name for error report
  private void readfile( DataInputStream dis ) throws ParserException
  {
    LoxFile lox = new LoxFile( dis, getName() );
    // Log.v("TopoGL-LOX", "file " + name );

    ArrayList< LoxSurvey > lox_surveys = lox.GetSurveys();
    for ( LoxSurvey survey : lox_surveys ) {
      String name = getLoxSurveyFullname( survey, lox_surveys );
      // String name = survey.name;
      // Log.v("TopoGL-LOX", "survey " + name + " " + survey.id + " " + survey.pid );
      surveys.add( new Cave3DSurvey( name, survey.id, survey.pid ) ); // pid = parent_id
    }

    ArrayList< LoxStation > lox_stations = lox.GetStations();
    ArrayList< LoxShot > lox_shots = lox.GetShots();
    // Log.v( "TopoGL-LOX", "lox stations " + lox_stations.size() + " shots " + lox_shots.size() );

    int cnt = 0;
    for ( LoxStation st : lox_stations ) {
      Cave3DSurvey survey = getSurvey( st.sid );
      String name = (survey != null)? st.name + "@" + survey.getName() : st.name;
      // String name = null;
      // for ( LoxSurvey s : lox_surveys ) if ( s.id == st.sid ) { name = s.name; break; } // no need to get the survey name
      Cave3DStation station = new Cave3DStation( name, st.x, st.y, st.z, st.id, st.sid, st.flag, st.comment );
      // Log.v("TopoGL", "station " + st.name + " " + st.x + " " + st.y + " " + st.z );
      stations.add( station );
      // Cave3DSurvey survey = getSurvey( st.sid );
      // if ( survey != null ) survey.addStationInfo( station );
    }
    computeBoundingBox();
    // Log.v( "TopoGL-LOX", "E " + emin + " " + emax + " N " + nmin + " " + nmax + " Z " + zmin + " " + zmax );

    // equate shots
    int nst = stations.size();
    for ( int i = 0; i < nst; ++ i ) {
      Cave3DStation st1 = stations.get(i);
      for ( int j = i+1; j< nst; ++j ) {
        Cave3DStation st2 = stations.get(j);
        if ( Math.abs( st1.x - st2.x ) < 0.01 && Math.abs( st1.y - st2.y ) < 0.01 && Math.abs( st1.z - st2.z ) < 0.01 ) {
          Cave3DShot shot = new Cave3DShot( st1.getFullName(), st2.getFullName(), 0, 0, 0, 0, 0 );
          shot.from_station = st1;
          shot.to_station   = st2;
          shot.setUsed();
          shots.add( shot );
        }
      }
    }

    for ( LoxShot sh : lox_shots ) {
      Cave3DStation f = getStation( sh.from );
      Cave3DStation t = getStation( sh.to );
      if ( f != null && t != null ) {
        double de = t.x - f.x;
        double dn = t.y - f.y;
        double dz = t.z - f.z;
        double len = Math.sqrt( de*de + dn*dn + dz*dz );
        double ber = Math.atan2( de, dn ) * RAD2DEG;
        if ( ber < 0 ) ber += 360;
        double dh = Math.sqrt( de*de + dn*dn );
        double cln = Math.atan2( dz, dh ) * RAD2DEG;
        Cave3DShot shot = new Cave3DShot( f.getFullName(), t.getFullName(), len, ber, cln, sh.flag, 0 );
        // Log.v("TopoGL", "shot " + f.name + " " + t.name + " : " + len + " " + ber + " " + cln );
        shot.from_station = f;
        shot.to_station   = t;
        shot.setUsed();
        mCaveLength += len;

        Cave3DSurvey survey = getSurvey( sh.sid );
        if ( (sh.flag & LoxShot.FLAG_SPLAY) != 0 ) {
          if ( mSplayUse > SPLAY_USE_SKIP ) {
            splays.add( shot );
            if ( survey != null ) {
              // shot.mSurvey   = survey;
              // shot.mSurveyNr = survey.number;
              // survey.addSplayInfo( shot );
              survey.addSplay( shot );
            }
          }
        } else {
          shots.add( shot );
          if ( survey != null ) {
            // shot.mSurvey   = survey;
            // shot.mSurveyNr = survey.number;
            // survey.addShotInfo( shot );
            survey.addShot( shot );
          }
        }
      }
    }
    setStationDepths();

    // lox surfaces are stored row-wise (west to east) and from north to south
    // lox DEM has (East,North) at LL-corner of cells
    LoxSurface surface = lox.GetSurface();
    if ( surface != null ) {
      int xoff = 0;
      int yoff = 0;
      int step = 1;
      double e1 = surface.East1();  // west bound
      double n1 = surface.North1(); // north bound (lox data are north to south but the coordinate is south)
      int dim1 = surface.Width();
      int dim2 = surface.Height();
      // Log.v("TopoGL-LOX", "lox surface orig " + surface.East1() + " N " + surface.North1() );
      // Log.v("TopoGL-LOX", "lox surface orig " + dim1 + "x" + dim2 + " dim " + surface.DimEast() + "x" + surface.DimNorth() );
      if ( TopoGL.mDEMreduce == TopoGL.DEM_SHRINK ) {
        while ( dim1 > TopoGL.mDEMmaxsize || dim2 > TopoGL.mDEMmaxsize ) {
          dim1 /= 2;
          dim2 /= 2;
          step *= 2;
        }
      } else {
        if ( dim1 > TopoGL.mDEMmaxsize ) {
          xoff = (dim1 - TopoGL.mDEMmaxsize)/2;
          e1 += xoff * surface.DimEast();
          dim1 -= 2 * xoff;
        }
        if ( dim2 > TopoGL.mDEMmaxsize ) {
          yoff = (dim2 - TopoGL.mDEMmaxsize)/2;
          n1 += yoff * surface.DimNorth();
          dim2 -= 2 * yoff;
        }
      }
      double de = step * surface.DimEast();
      double dn = step * surface.DimNorth();
      // Log.v("TopoGL-LOX", "lox surface " + dim1 + "x" + dim2 + " (max " + TopoGL.mDEMmaxsize + ") W-E " + e1 + " " + de + " N-S " + n1 + " " + dn );
      mSurface = new DEMsurface( e1, n1, de, dn, dim1, dim2 );
      mSurface.setGridData( surface.Grid(), xoff, yoff, step, surface.Width(), surface.Height() );
    }

    mBitmap = lox.GetBitmap();
  }

}

