/** @file TdmConfig.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager cave-project object
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.util.Log;

class TdmConfig extends TdmFile
{
  String mParentDir;            // parent directory
  String mSurveyName;
  TdmSurvey mSurvey;             // inline survey in the tdconfig file
  ArrayList< TdmSurvey > mViewSurveys = null; // current view surveys
  ArrayList< TdmInput >  mInputs; // surveys: th files on input
  ArrayList< TdmEquate > mEquates;
  private boolean mRead;        // whether the TdmConfig has read the file

  public TdmConfig( String filepath )
  {
    super( filepath, null );

    // Log.v("TdManager", "TdmConfig cstr filepath " + filepath );
    mParentDir = (new File( filepath )).getParentFile().getName() + "/";
    mSurvey  = null;
    mInputs      = new ArrayList< TdmInput >();
    mEquates     = new ArrayList< TdmEquate >();
    mRead = false;
  }

  void populateViewSurveys( ArrayList< TdmSurvey > surveys )
  {
    mViewSurveys = new ArrayList< TdmSurvey >(); // current view surveys
    for ( TdmSurvey survey : surveys ) {
      survey.reduce();
      mViewSurveys.add( survey );
    }
  }

  void dropEquates( String survey )
  {
    // Log.v("TdManager", "drop equates with " + survey + " before " + mEquates.size() );
    if ( survey == null || survey.length() == 0 ) return;
    ArrayList< TdmEquate > equates = new ArrayList< TdmEquate >();
    for ( TdmEquate equate : mEquates ) {
      if ( equate.dropStations( survey ) > 1 ) {
        equates.add( equate );
      }
    }
    mEquates = equates;
    // Log.v("TdManager", "dropped equates with " + survey + " after " + mEquates.size() );
  }

  void addEquate( TdmEquate equate ) 
  {
    if ( equate == null ) return;
    mEquates.add( equate );
    // Log.v("TdManager", "nr. equates " + mEquates.size() );
  }

  // unconditionally remove an equate
  void removeEquate( TdmEquate equate ) { mEquates.remove( equate ); }

  void readTdmConfig()
  {
    if ( mRead ) return;
    // Log.v( TdManagerApp.TAG, "readTdmConfig() for file " + mName );
    readFile();
    // Log.v( TdManagerApp.TAG, "TdmConfig() inputs " + mInputs.size() + " equates " + mEquates.size() );
    mRead = true;
  }
    
  boolean hasInput( String name )
  {
    if ( name == null ) return false;
    // Log.v("TdManager", "TdmConfig check input name " + name );
    for ( TdmInput input : mInputs ) {
      // Log.v("TdManager", "TdmConfig check input " + input.mName );
      if ( name.equals( input.getSurveyName() ) ) return true;
    }
    return false;
  }

  private void addInput( String name )
  {
    if ( name == null ) return;
    // Log.v("TdManager", "add input name " + surveyname );
    mInputs.add( new TdmInput( name ) );
  }

  private void dropInput( String name )
  {
    if ( name == null ) return;
    for ( TdmInput input : mInputs ) {
      if ( name.equals( input.getSurveyName() ) ) {
        mInputs.remove( input );
        return;
      }
    }
  }


// ---------------------------------------------------------------
// READ and WRITE
  static String currentDate()
  {
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    return sdf.format( new Date() );
  }


  void writeTdmConfig( boolean force )
  {
    if ( mRead || force ) {
      writeTd( getFilepath() );
    }
  }

  void writeTd( String filepath )
  {
    try {
      FileWriter fw = new FileWriter( filepath );
      PrintWriter pw = new PrintWriter( fw );
      pw.format("# created by TopoDroid Manager %s - %s\n", TopoDroidApp.VERSION, currentDate() );
      pw.format("source\n");
      pw.format("  survey %s\n", mSurveyName );
      for ( TdmInput input : mInputs ) {
        // FIXME path
        String path = input.getSurveyName();
        // Log.v("TdManager", "config write add survey " + path );
        pw.format("    load %s\n", path );
      }
      for ( TdmEquate equate : mEquates ) {
        pw.format("    equate");
        for ( String st : equate.mStations ) pw.format(" %s", st );
        pw.format("\n");
      }
      pw.format("  endsurvey\n");
      pw.format("endsource\n");
      fw.flush();
      fw.close();
    } catch ( IOException e ) { 
      TDLog.Error("TdManager write file " + getFilepath() + " I/O error " + e.getMessage() );
    }
  }

  String exportTherion( boolean overwrite )
  {
    String filepath = getFilepath().replace(".tdconfig", ".th").replace("/tdconfig/", "/th/");
    File file = new File( filepath );
    if ( file.exists() ) {
      if ( ! overwrite ) return null;
    } else {
      File dir = file.getParentFile();
      if ( dir != null ) dir.mkdirs();
    }
    writeTherion( filepath );
    return filepath;
  }

  void writeTherion( String filepath )
  {
    try {
      FileWriter fw = new FileWriter( filepath );
      PrintWriter pw = new PrintWriter( fw );
      pw.format("# created by TopoDroid Manager %s - %s\n", TopoDroidApp.VERSION, currentDate() );
      pw.format("source\n");
      pw.format("  survey %s\n", mSurveyName );
      for ( TdmInput input : mInputs ) {
        // FIXME path
        String path = "../th/" + input.getSurveyName() + ".th";
        // Log.v("TdManager", "config write add survey " + path );
        pw.format("    input %s\n", path );
      }
      for ( TdmEquate equate : mEquates ) {
        pw.format("    equate");
        for ( String st : equate.mStations ) pw.format(" %s", st );
        pw.format("\n");
      }
      pw.format("  endsurvey\n");
      pw.format("endsource\n");
      fw.flush();
      fw.close();
    } catch ( IOException e ) { 
      TDLog.Error("TdManager write file " + getFilepath() + " I/O error " + e.getMessage() );
    }
  }

  String exportSurvex( boolean overwrite )
  {
    String filepath = getFilepath().replace(".tdconfig", ".svx").replace("/tdconfig/", "/svx/");
    File file = new File( filepath );
    if ( file.exists() ) {
      if ( ! overwrite ) return null;
    } else {
      File dir = file.getParentFile();
      if ( dir != null ) dir.mkdirs();
    }
    writeSurvex( filepath );
    return filepath;
  }

  private String toSvxStation( String st )
  {
    int pos = st.indexOf('@');
    return st.substring(pos+1) + "." + st.substring(0,pos);
  }

  void writeSurvex( String filepath )
  {
    try {
      FileWriter fw = new FileWriter( filepath );
      PrintWriter pw = new PrintWriter( fw );
      pw.format("; created by TopoDroid Manager %s - %s\n", TopoDroidApp.VERSION, currentDate() );
      // TODO EXPORT
      for ( TdmInput s : mInputs ) {
        String path = "../svx/" + s.getSurveyName() + ".svx";
        pw.format("*include %s\n", path );
      }
      for ( TdmEquate equate : mEquates ) {
        pw.format("*equate");
        for ( String st : equate.mStations ) pw.format(" %s", toSvxStation( st ) );
        pw.format("\n");
      }

      fw.flush();
      fw.close();
    } catch ( IOException e ) { 
      TDLog.Error("TdManager write file " + getFilepath() + " I/O error " + e.getMessage() );
    }
  }

  // private void loadFile()
  // {
  //   // Log.v("TdManager", "load file path " + getFilepath() );
  //   mSurvey = new TdmSurvey( "." );
  //   new TdParser( getFilepath(), mSurvey, new TdUnits() );
  // }

  private void readFile( )
  {
    // if the file does not exists creates it and write an empty tdconfig file
    // Log.v("TdManager", "read file path " + getFilepath() );
    File file = new File( getFilepath() );
    if ( ! file.exists() ) {
      // Log.v("TdManager", "file does not exist");
      writeTdmConfig( true );
      return;
    }

    try {
      FileReader fr = new FileReader( file );
      BufferedReader br = new BufferedReader( fr );
      String line = br.readLine();
      int cnt = 1;
      // Log.v( TdManagerApp.TAG, cnt + ":" + line );
      while ( line != null ) {
        line = line.trim();
        int pos = line.indexOf( '#' );
        if ( pos >= 0 ) line = line.substring( 0, pos );
        if ( line.length() > 0 ) {
          String[] vals = line.split( " " );
          if ( vals.length > 0 ) {
            if ( vals[0].equals( "source" ) ) {
            } else if ( vals[0].equals( "survey" ) ) {
              for (int k=1; k<vals.length; ++k ) {
                if ( vals[k].length() > 0 ) {
                  mSurveyName = vals[k];
                  break;
                }
              }
            } else if ( vals[0].equals( "load" ) ) {
              for (int k=1; k<vals.length; ++k ) {
                if ( vals[k].length() > 0 ) {
                  String surveyname = vals[k];
                  addInput( surveyname );
                  break;
                }
              }    
            } else if ( vals[0].equals( "equate" ) ) {
              TdmEquate equate = new TdmEquate();
              for (int k=1; k<vals.length; ++k ) {
                if ( vals[k].length() > 0 ) {
                  equate.addStation( vals[k] );
                }
              }
              mEquates.add( equate );
            }
          }
        }
        line = br.readLine();
        ++ cnt;
      }
      fr.close();
    } catch ( IOException e ) {
      // TODO
      TDLog.Error( "TdManager exception " + e.getMessage() );
    }
    // Log.v( "TdManager", "TdmConfig read file: nr. sources " + mInputs.size() );
  }

}
