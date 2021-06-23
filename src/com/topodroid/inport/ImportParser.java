/* @file ImportParser.java
 *
 * @author marco corvi
 * @date mar 2015
 *
 * @brief TopoDroid import parser
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.DistoX.SurveyInfo;
import com.topodroid.DistoX.TDUtil;
import com.topodroid.DistoX.DataHelper;

// import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
// import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

// import android.util.Log;

class ImportParser
{
  // final static String EMPTY = "";
  String mName = null;  // survey name
  String mDate = null;  // survey date
  String mTeam = TDString.EMPTY;
  String mTitle = TDString.EMPTY;
  String mDescr = TDString.EMPTY;
  float  mDeclination = SurveyInfo.DECLINATION_UNSET; // 0.0f; // one-survey declination
  protected boolean mApplyDeclination = false;
  protected boolean mValid = false;  // whether the parser is valid

  protected ArrayList< ParserShot > shots;   // centerline shots
  protected ArrayList< ParserShot > splays;  // splay shots

  public int getShotNumber()    { return shots.size(); }
  public int getSplayNumber()   { return splays.size(); }

  ArrayList< ParserShot > getShots() { return shots; }
  ArrayList< ParserShot > getSplays() { return splays; }

  protected void checkValid() 
  {
    mValid =  ( mName != null && mName.length() > 0 ) && ( mDate != null ) && ( shots.size() > 0 );
  }

  public boolean isValid() { return mValid; }

  String initStation()
  {
    for ( ParserShot sh : shots ) {
      if ( sh.from != null && sh.from.length() > 0 ) return sh.from;
    }
    return TDString.ZERO;
  }

  int mLineCnt;  // line counter

  Pattern pattern = Pattern.compile( "\\s+" );

  // cstr
  // @param apply_declination  whether to apply the declination read from the file
  ImportParser( boolean apply_declination ) // throws ParserException
  {
    mDate  = TDUtil.currentDate();
    shots  = new ArrayList<>();
    splays = new ArrayList<>();
    mApplyDeclination = apply_declination;
  }

  // update survey metadata in the DB: data, title, declination, init_station
  // @param sid           survey ID
  // @param data_helper   database helper class
  boolean updateSurveyMetadata( long sid, DataHelper data_helper ) 
  {
    boolean b1 = data_helper.updateSurveyDayAndComment( sid, mDate, mTitle );
    boolean b2 = data_helper.updateSurveyDeclination( sid, mDeclination );
    boolean b3 = data_helper.updateSurveyInitStation( sid, initStation() );
    return b1 && b2 && b3; // try all the three actions
  }
  
  String nextLine( BufferedReader br ) throws IOException
  {
    String line = br.readLine();
    if ( line == null ) return null; // EOF
    line = line.trim();
    ++mLineCnt;
    StringBuilder ret = new StringBuilder();
    while ( line != null && line.endsWith( "\\" ) ) {
      ret.append( line.replace( '\\', ' ' ) ); // FIXME
      line = br.readLine();
      ++mLineCnt;
    }
    if ( line != null ) ret.append( line );
    return ret.toString();
  }

  String[] splitLine( String line )
  {
     return pattern.split(line); // line.split( "\\s+" );
  }
   
  /** read input file
   * @param filename name of the file to parse
   *
  void readFile( String filename ) throws ParserException
  {
    // TDLog.Log( TDLog.LOG_IO, "import parse file <" + filename + ">" );
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      readFile( br );
      fr.close();
    } catch ( IOException e ) {
      TDLog.Error( "ERROR I/O " + e.toString() );
      throw new ParserException();
    }
    TDLog.Log( TDLog.LOG_THERION, "ImportParser shots "+ shots.size() +" splays "+ splays.size()  );
  }

  void readFile( FileReader fr ) throws ParserException
  {
    try {
      BufferedReader br = new BufferedReader( fr );
      readFile( br );
      fr.close();
    } catch ( IOException e ) {
      TDLog.Error( "ERROR I/O " + e.toString() );
      throw new ParserException();
    }
  }

  void readFile( BufferedReader br ) throws ParserException
  {
  }
   */

  BufferedReader getBufferedReader( InputStreamReader isr, String filename )
  {
    try {
      if ( isr == null ) {
        isr = new InputStreamReader( new FileInputStream( filename ) );
      }
      return new BufferedReader( isr );
    } catch ( FileNotFoundException e ) {
    }
    return null;
  }

  protected String extractName( String filename )
  {
    int pos = filename.lastIndexOf( '/' );
    if ( pos < 0 ) { pos = 0; } else { ++pos; }
    int ext = filename.lastIndexOf( '.' ); if ( ext < 0 ) ext = filename.length();
    return filename.substring( pos, ext );
  }

}
