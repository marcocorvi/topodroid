/** @file ExportCGAL.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Walls KML exporter
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.Cave3X.TglParser;
import com.topodroid.Cave3X.Cave3DStation;
import com.topodroid.Cave3X.Cave3DShot;
import com.topodroid.Cave3X.Vector3D;

import com.topodroid.utils.TDLog;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
// import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ExportCGAL
{
  // ArrayList<CWFacet> mFacets;
  // double lat, lng, asl;
  double s_radius = 1;
  double e_radius = 1;
  Cave3DStation zero = new Cave3DStation( "", 0, 0, 0 );

  // ExportCGAL() { }

  public boolean exportASCII( OutputStreamWriter osw, TglParser data, boolean do_splays, boolean do_walls, boolean do_station )
  {
    // String name = "Cave3D";
    // Log.v( "Cave3D-CGAL, "export as CGAL " + filename );
    if ( data == null ) return false;

    List< Cave3DStation> stations = data.getStations();
    List< Cave3DShot>    shots    = data.getShots();
    List< Cave3DShot>    splays   = data.getSplays();

    // now write the KML
    try {
      PrintWriter pw = new PrintWriter( osw );

      int nst = stations.size();
      int nsp = splays.size();
      pw.format(Locale.US, "OFF\n");
      pw.format(Locale.US, "%d 0 0\n", (nst+nsp) );
      pw.format(Locale.US, "\n");

      for ( Cave3DStation st : stations ) {
        double e = (st.x - zero.x) * e_radius;
        double n = (st.y - zero.y) * s_radius;
        double z = (st.z - zero.z);
        int cnt = 0;
        for ( Cave3DShot sp : splays ) {
          if ( st == sp.from_station ) ++cnt;
        }
        pw.format(Locale.US, "# %s %d\n", st.name, cnt );
        pw.format(Locale.US, "%.2f %.2f %.2f\n", e, n, z );
        for ( Cave3DShot sp : splays ) {
          if ( st == sp.from_station ) {
            Vector3D v = sp.toVector3D();
            e = (st.x + v.x - zero.x) * e_radius;
            n = (st.y + v.y - zero.y) * s_radius;
            z = (st.z + v.z - zero.z);
            pw.format(Locale.US, "%.2f %.2f %.2f\n", e, n, z );
          }
        }  
      }
      osw.flush();
      osw.close();
      return true;
    } catch ( IOException e ) {
      // Log.e( "Cave3D-CGAL, "Failed export: " + e.getMessage() );
      return false;
    }
  }

}


