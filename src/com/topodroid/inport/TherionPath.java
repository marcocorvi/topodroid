/* @file TherionPath.java
 *
 * @author marco corvi
 * @date sept 2024
 *
 * @brief TopoDroid Therion survey path, for Therion parser
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.inport;

// import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDio;
// import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDUtil;
// import com.topodroid.prefs.TDSetting;


class TherionPath
{
  StringBuilder path;
  int[] survey_pos;
  int ks     = 0;
  int ks_max = 20;

  // StringBuilder qath;
  // int[] survey_qos;
  // char ch1 = 'A';
  // char ch2 = 'A';
  // java.util.HashMap<String,String> q2p;
  
  // private void nextChars()
  // {
  //   if ( ch2 == 'Z' ) {
  //     ch2 = 'A';
  //     ch1 ++;
  //   } else {
  //     ch2 ++;
  //   }
  // }

  // String path2qath( String p )
  // {
  //   return (String)q2p.get( p );
  // }


  TherionPath( String basepath )
  {
    if ( basepath == null ) {
      path = new StringBuilder( );
    } else {
      path = new StringBuilder( basepath );
    }
    survey_pos = new int[ ks_max ];
    survey_pos[ks] = 0;
    ks ++;

    // qath = new StringBuilder( "0" );
    // survey_qos = new int[ ks_max ];
    // q2p = new  java.util.HashMap<String,String>();
  }

  /** append a survey name to the path
   * @param name   survey name (can be null or empty)
   */
  void insertSurvey( String name )
  {
    TDLog.v("Parser TH survey (" + ks + " " + survey_pos[ks] + "): " + path.toString() + " <" + name + ">" );
    if ( name == null || name.isEmpty() ) name = "unnamed";
    survey_pos[ ks ] = name.length()+1;
    path.insert( 0, name + "." );

    // qath.append( "." ).append( ch1 ).append( ch2 );
    // nextChars();
    // q2p.put( path.toString(), qath.toString() );

    ++ks;
    if ( ks >= ks_max ) {
     	ks_max += 10;
     	int[] tmp = new int[ks_max];
     	System.arraycopy( survey_pos, 0, tmp, 0, ks );
     	survey_pos = tmp;
     	// int[] tmq = new int[ks_max];
     	// System.arraycopy( survey_qos, 0, tmq, 0, ks );
     	// survey_qos = tmq;
    }
  }

  /** @return true is the path is not empty
   */
  boolean dropSurvey()
  {
    if ( ks > 0 ) {
      --ks;
    } else {
      TDLog.e("Parser Therion: endsurvey out of survey");
    }
    int k_pos = survey_pos[ks];
    // TDLog.v("Parser TH: end survey (" + k_pos + " " + ks + "): " + path.toString() );
    // path = ( k_pos > 0 )? path.substring(k_pos) : ""; // return to previous survey_pos in path
    if ( k_pos > 0 ) path.replace( 0, k_pos, "" );

    // int k_qos = survey_qos[ks];
    // if ( k_qos > 0 ) qath.setLength( k_qos );

    return (ks > 0);
  }
   
  /** @return the fullname of a therion item
   * @param name  item short-name
   */
  String getName( String name ) { return name + "@" + path.toString(); }

  public String toString() { return path.toString(); }
}
