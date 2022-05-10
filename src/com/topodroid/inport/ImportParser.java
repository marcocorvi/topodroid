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
import com.topodroid.utils.TDUtil;
import com.topodroid.TDX.SurveyInfo;
import com.topodroid.TDX.DataHelper;

// import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
// import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

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

  /** @return the number of legs
   */
  public int getShotNumber()    { return shots.size(); }

  /** @return the number of splays
   */
  public int getSplayNumber()   { return splays.size(); }

  /** @return the arrys of legs
   */
  ArrayList< ParserShot > getShots() { return shots; }

  /** @return the arrys of splays
   */
  ArrayList< ParserShot > getSplays() { return splays; }

  /** check if the parser is in a valid state
   */
  protected void checkValid() 
  {
    if ( mName == null ) {
      TDLog.v("invalid: null name"); 
      mValid = false;
    } else if ( mName.length() == 0 ) {
      TDLog.v("invalid: empty name"); 
      mValid = false;
    } else if ( mDate == null ) {
      TDLog.v("invalid: null date"); 
      mValid = false;
    } else if ( shots.size() == 0 ) {
      TDLog.v("invalid: no shots" );
      mValid = false;
    } else {
      mValid = true;
    }
  }

  /** @return true if the parser is valid
   */
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

  /** cstr
   * @param apply_declination  whether to apply the declination read from the file
   */
  ImportParser( boolean apply_declination ) // throws ParserException
  {
    mDate  = TDUtil.currentDate();
    shots  = new ArrayList<>();
    splays = new ArrayList<>();
    mApplyDeclination = apply_declination;
  }

  /** update survey metadata in the DB: data, title, declination, init_station
   * @param sid           survey ID
   * @param data_helper   database helper class
   */
  boolean updateSurveyMetadata( long sid, DataHelper data_helper ) 
  {
    boolean b1 = data_helper.updateSurveyDayAndComment( sid, mDate, mTitle );
    boolean b2 = data_helper.updateSurveyDeclination( sid, mDeclination );
    boolean b3 = data_helper.updateSurveyInitStation( sid, initStation() );
    return b1 && b2 && b3; // try all the three actions
  }
  
  /** @return the next line of the input
   * @param br    input reader
   */
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

  /** split a line (using a pattern)
   * @param line  line
   * @return the line tokens
   */
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

  /** @return the input reader
   * @param isr  input stream
   * @param filename file full pathname (userd if the stream is null)
   */
  static BufferedReader getBufferedReader( InputStreamReader isr, String filename )
  {
    try {
      if ( isr == null ) {
        isr = new InputStreamReader( new FileInputStream( filename ) );
      }
      return new BufferedReader( isr );
    } catch ( FileNotFoundException e ) {
      TDLog.Error("File not found");
    }
    return null;
  }

  /** extract the survey name from the filename
   * @param filename  file name
   * @return survey name 
   */
  static protected String extractName( String filename )
  {
    int pos = filename.lastIndexOf( '/' );
    if ( pos < 0 ) { pos = 0; } else { ++pos; }
    int ext = filename.lastIndexOf( '.' ); if ( ext < 0 ) ext = filename.length();
    return filename.substring( pos, ext );
  }

}
