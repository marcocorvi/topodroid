/* @file Archiver.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey archiver
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDFile;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// import java.nio.charsets.StandardCharsets; // API-19

import java.util.List;
// import java.util.Locale;
// import java.util.ArrayList;

// import android.content.Context;
// import android.content.Intent;

public class Archiver
{
  // private final TopoDroidApp mApp;
  private static final int BUF_SIZE = 4096;
  private byte[] data; // = new byte[ BUF_SIZE ];

  String zipname;

  public Archiver( ) // TopoDroidApp app
  {
    // mApp = app;
    data = new byte[ BUF_SIZE ];
  }

  private boolean addEntry( ZipOutputStream zos, File name, String filepath )
  {
    if ( name == null || ! name.exists() ) return false;
    boolean ret = false;
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "zip add file " + name.getPath() );
      bis = new BufferedInputStream( TDFile.getFileInputStream( filepath ), BUF_SIZE );
      ZipEntry entry = new ZipEntry( name.getName() );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
        zos.write( data, 0, cnt );
      }
      zos.closeEntry( );
      ret = true;
    } catch (FileNotFoundException e ) {
      TDLog.Error( "File Not Found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "IO exception " + e.getMessage() );
    } finally {
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { /* ret = false; */ }
    }
    // Log.v( "DistoX-ZIP", "zip add file " + name.getPath() + " return " + ret );
    return ret;
  }

  private void addOptionalEntry( ZipOutputStream zos, File name, String filepath )
  {
    if ( name == null || ! name.exists() ) return;
    // Log.v( "DistoX-ZIP", "zip optional file " + name.getPath() );
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "zip add file " + name.getPath() );
      bis = new BufferedInputStream( TDFile.getFileInputStream( filepath ), BUF_SIZE );
      ZipEntry entry = new ZipEntry( name.getName() );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
        zos.write( data, 0, cnt );
      }
      zos.closeEntry( );
    } catch (FileNotFoundException e ) {
      TDLog.Error( "File Not Found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "IO exception " + e.getMessage() );
    } finally {
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { }
    }
  }

  private void addDirectory( ZipOutputStream zos, File dir, String dirname )
  {
    if ( ! dir.exists() ) return;
    // Log.v( "DistoX-ZIP", "zip add dir " + dir.getPath() );
    File[] files = dir.listFiles();
    if ( files != null ) {
      for ( File file : files ) { // listFiles MAY NullPointerException
        if (file.isFile()) addOptionalEntry(zos, file, file.getPath() ); 
      }
    }
  }

  public boolean compressFiles( String zipname, List< File > files )
  {
    ZipOutputStream zos = null;
    boolean ret = true;
    try {
      zos = new ZipOutputStream( new BufferedOutputStream( TDFile.getFileOutputStream( zipname ) ) );
      for ( File file : files ) ret &= addEntry( zos, file, file.getPath() );
      // for ( File file : files ) TDFile.deleteFile( file );
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
      // FIXME
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { 
        ret = false;
        // TDLog.Error("ZIP compress close error");
      }
    }
    return ret;
  }

  // @param zipname    name of compressed zip
  // @param lib        symbol library
  // @param dirpath    directory of symbol files (must end with '/') eg, TDPath.APP_POINT_PATH
  private boolean compressSymbols( String zipname, SymbolLibrary lib, String dirpath )
  {
    if ( lib == null ) return false;
    if ( ! (TDFile.getFile(dirpath)).exists() ) return false;
    List< Symbol > symbols = lib.getSymbols();
    ZipOutputStream zos = null;
    try { 
      zos = new ZipOutputStream( new BufferedOutputStream( TDFile.getFileOutputStream( zipname ) ) );
      for ( Symbol symbol : symbols ) {
        if ( symbol.mEnabled ) {
          String filename = symbol.getThName();
          // THERION-U: filename = Symbol.deprefix_u( filename );
          String filepath = dirpath + filename;
          addOptionalEntry( zos, TDFile.getFile( filepath ), filepath );
        }
      }
    } catch ( FileNotFoundException e ) {
      return false;
    } catch ( IOException e ) {
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { TDLog.Error("ZIP-symbol close error"); }
    }
    return true;
  }

  // return true is a symbol has been uncompressed
  private boolean uncompressSymbols( ZipInputStream zin, String dirpath, String prefix )
  {
    if ( ! (TDLevel.overExpert && TDSetting.mZipWithSymbols ) ) return false;
    boolean ret = false;
    String filename = TDPath.getSymbolFile( "tmp.zip" );
    FileOutputStream fout = null;
    int c;
    byte[] sbuffer = new byte[4096];
    try { 
      fout = TDFile.getFileOutputStream( filename );
      while ( ( c = zin.read( sbuffer ) ) != -1 ) {
        fout.write(sbuffer, 0, c);
      }
      fout.close();
      fout = null;
      // uncompress symbols zip
      ZipEntry sze;
      FileInputStream fis = TDFile.getFileInputStream( filename );
      ZipInputStream szin = new ZipInputStream( fis );
      while ( ( sze = szin.getNextEntry() ) != null ) {
        String symbolfilename = dirpath + sze.getName();
        // Log.v("DistoX-ZIP", "try to uncompress symbol " + symbolfilename );
        File symbolfile = TDFile.getFile( symbolfilename );
        if ( ! symbolfile.exists() ) { // don't overwrite
          // Log.v("DistoX-ZIP", "uncompress symbol " + symbolfilename );
          FileOutputStream sfout = TDFile.getFileOutputStream( symbolfilename ); // uncompress symbols zip
          while ( ( c = szin.read( sbuffer ) ) != -1 ) {
            sfout.write(sbuffer, 0, c);
          }
          sfout.close();
          ret = true;
          // add symbol to library and enable it
        }
        szin.closeEntry();
        // need to get the thname from the file
        FileInputStream sfis = TDFile.getFileInputStream( symbolfilename );
        BufferedReader br = new BufferedReader( new InputStreamReader( sfis, "UTF-8" /* StandardCharsets.UTF_8 */ ) ); // String iso = "UTF-8";
        String line;
        while ( (line = br.readLine()) != null ) {
          line = line.trim();
          if ( line.startsWith("th_name") ) {
            String th_name = line.substring(8).trim();
            // Log.v("DistoX-ZIP", "enable " + th_name );
            TopoDroidApp.mData.setSymbolEnabled( prefix + th_name, true );
            break;
          }
        }
        sfis.close();
      }
    } catch ( FileNotFoundException e1 ) { Log.v( "DistoX-ZIP", "File Not Found " + e1.getMessage() );
    } catch ( IOException e2 ) { Log.v( "DistoX-ZIP", "I/O exception " + e2.getMessage() );
    } finally {
      if ( fout != null ) { try { fout.close(); } catch ( IOException e ) { } }
    }
    return ret;
  }


  boolean archive( TopoDroidApp mApp )
  {
    if ( TDInstance.sid < 0 ) return false;
    DataHelper app_data = TopoDroidApp.mData;
    // if ( app_data == null ) return false;
    
    // File temp = null;
    String survey = TDInstance.survey;
    boolean ret = true;

    zipname = TDPath.getSurveyZipFile( survey );
    TDPath.checkPath( zipname );
    // TDLog.Log( TDLog.LOG_IO, "zip export file: " + zipname );
    // Log.v( "DistoX-ZIP", "zip export file: " + zipname + " pre " + ret );

    ZipOutputStream zos = null;
    try {
      String pathname;
      zos = new ZipOutputStream( new BufferedOutputStream( TDFile.getFileOutputStream( zipname ) ) );

      if ( TDLevel.overExpert && TDSetting.mZipWithSymbols ) {
        String filename = TDPath.getSymbolFile( "points.zip" );
        if ( compressSymbols( filename, BrushManager.getPointLib(), TDPath.getSymbolPointDir() ) )  {
          addOptionalEntry( zos, TDFile.getFile( filename ), filename );
        }
        filename = TDPath.getSymbolFile( "lines.zip" );
        if ( compressSymbols( filename, BrushManager.getLineLib(), TDPath.getSymbolLineDir() ) )  {
          addOptionalEntry( zos, TDFile.getFile( filename ), filename );
        }
        filename = TDPath.getSymbolFile( "areas.zip" );
        if ( compressSymbols( filename, BrushManager.getAreaLib(), TDPath.getSymbolAreaDir() ) )  {
          addOptionalEntry( zos, TDFile.getFile( filename ), filename );
        }
      }

/* FIXME_SKETCH_3D *
      List< Sketch3dInfo > sketches  = app_data.selectAllSketches( TDInstance.sid, TDStatus.NORMAL );
      for ( Sketch3dInfo skt : sketches ) {
        ret &= addEntry( zos, TDFile.getFile( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
      sketches  = app_data.selectAllSketches( TDInstance.sid, TDStatus.DELETED );
      for ( Sketch3dInfo skt : sketches ) {
        ret &= addEntry( zos, TDFile.getFile( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
/* END_SKETCH_3D */

      List< PlotInfo > plots  = app_data.selectAllPlots( TDInstance.sid, TDStatus.NORMAL );
      for ( PlotInfo plt : plots ) {
        pathname = TDPath.getSurveyPlotTdrFile( survey, plt.name ); // N.B. plot file CAN be missing
        addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotTh2File( survey, plt.name );
        addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotDxfFile( survey, plt.name );
        addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotSvgFile( survey, plt.name );
        addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotXviFile( survey, plt.name );
        addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotPngFile( survey, plt.name );
        addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotTnlFile( survey, plt.name );
        addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        if ( plt.type == PlotType.PLOT_PLAN ) {
          pathname = TDPath.getSurveyCsxFile( survey, plt.name );
          addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        }
        pathname = TDPath.getSurveyPlotShzFile( survey, plt.name );
	addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      }

      // try overview exports
        pathname = TDPath.getSurveyPlotTh2File( survey, "p" );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotDxfFile( survey, "p" );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotSvgFile( survey, "p" );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotXviFile( survey, "p" );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotShzFile( survey, "p" ); // zipped shp
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );

        pathname = TDPath.getSurveyPlotTh2File( survey, "s" );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotDxfFile( survey, "s" );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotSvgFile( survey, "s" );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotXviFile( survey, "s" );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
        pathname = TDPath.getSurveyPlotShzFile( survey, "s" ); // zipped shp
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );

      plots  = app_data.selectAllPlots( TDInstance.sid, TDStatus.DELETED );
      for ( PlotInfo plt : plots ) {
        pathname = TDPath.getSurveyPlotTdrFile( survey, plt.name );
        addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      }

      pathname = TDPath.getSurveyPhotoDir( survey );
      addDirectory( zos, TDFile.getFile( pathname ), pathname );

      pathname = TDPath.getSurveyAudioDir( survey );
      addDirectory( zos, TDFile.getFile( pathname ), pathname );

      pathname = TDPath.getSurveyThFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyCsvFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyCsxFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyCaveFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyCavFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyDatFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyDxfFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyGrtFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyGtxFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyKmlFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyJsonFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyPltFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyShzFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveySrvFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveySurFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveySvxFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyTroFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyTrbFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
      pathname = TDPath.getSurveyTopFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );

      pathname = TDPath.getSurveyNoteFile( survey );
      addOptionalEntry( zos, TDFile.getFile( pathname ), pathname );
 
      pathname = TDPath.getSqlFile( );
      app_data.dumpToFile( pathname, TDInstance.sid );
      ret &= addEntry( zos, TDFile.getFile(pathname), pathname );
      // Log.v("DistoX-ZIP", "archive post-sqlite returns " + ret );

      pathname = TDPath.getManifestFile();
      mApp.writeManifestFile();
      ret &= addEntry( zos, TDFile.getFile(pathname), pathname );
      // Log.v("DistoX-ZIP", "archive post-manifest returns " + ret );

      // ret = true;
    } catch ( FileNotFoundException e ) {
      // Log.v("DistoX-ZIP", e.getMessage() );
      ret = false;
    } catch ( IOException e ) {
      // FIXME
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { TDLog.Error("ZIP close error"); }
      TDFile.deleteFile( TDPath.getSqlFile() );
    }
    // Log.v("DistoX-ZIP", "archive returns " + ret );
    return ret;
  }

  public int unArchive( TopoDroidApp mApp, String filename, String surveyname, boolean force )
  {
    boolean sql_success = false;
    int ok_manifest = -2;
    String pathname;
    DataHelper app_data = TopoDroidApp.mData;
    try {
      // byte buffer[] = new byte[36768];
      byte[] buffer = new byte[4096];

      TDLog.Log( TDLog.LOG_ZIP, "unzip " + filename );
      ZipFile zip = new ZipFile( filename );
      ZipEntry ze = zip.getEntry( "manifest" );
      if ( ze != null ) {
        pathname = TDPath.getManifestFile();
        // TDPath.checkPath( pathname );
        FileOutputStream fout = TDFile.getFileOutputStream( pathname );
        InputStream is = zip.getInputStream( ze );
        int c;
        while ( ( c = is.read( buffer ) ) != -1 ) {
          fout.write(buffer, 0, c);
        }
        fout.close();
        ok_manifest = mApp.checkManifestFile( pathname, surveyname  ); // this sets surveyname
        TDFile.deleteFile( pathname );
      }
      zip.close();
      TDLog.Log( TDLog.LOG_ZIP, "un-archive manifest " + ok_manifest );
      if ( ok_manifest < 0 ) {
        if ( ! force ) return ok_manifest;
      }
      TDLog.Log( TDLog.LOG_ZIP, "un-archive survey " + surveyname );

      // TDLog.Log( TDLog.LOG_IO, "unzip file " + filename );
      FileInputStream fis = TDFile.getFileInputStream( filename );
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        if ( ze.isDirectory() ) {
          TDFile.makeDir( TDPath.getDirFile( ze.getName() ) );
        } else {
          TDLog.Log( TDLog.LOG_ZIP, "Zip entry \"" + ze.getName() + "\"" );
          boolean sql = false;
          pathname = null;
          if ( ze.getName().equals( "manifest" ) ) {
            // skip
          } else if ( ze.getName().equals( "survey.sql" ) ) {
            pathname = TDPath.getSqlFile();
            sql = true;
          } else if ( ze.getName().endsWith( TDPath.CSV ) ) {
            pathname = TDPath.getCsvFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.CSX ) ) {
            pathname = TDPath.getCsxFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.CAVE ) ) {
            pathname = TDPath.getCaveFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.CAV ) ) {
            pathname = TDPath.getCavFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.DAT ) ) {
            pathname = TDPath.getDatFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.GRT ) ) {
            pathname = TDPath.getGrtFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.GTX ) ) {
            pathname = TDPath.getGtxFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.DXF ) ) {
            pathname = TDPath.getDxfFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.KML ) ) {
            pathname = TDPath.getKmlFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.JSON ) ) {
            pathname = TDPath.getJsonFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.PLT ) ) {
            pathname = TDPath.getPltFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.PNG ) ) {
            pathname = TDPath.getPngFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.SRV ) ) {
            pathname = TDPath.getSrvFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.SVG ) ) {
            pathname = TDPath.getSvgFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.SUR ) ) {
            pathname = TDPath.getSurFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.SVX ) ) {
            pathname = TDPath.getSvxFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.SHZ ) ) {
            pathname = TDPath.getShzFile( ze.getName() );

          } else if ( ze.getName().endsWith( TDPath.TH ) ) {
            pathname = TDPath.getThFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TH2 ) ) {
            pathname = TDPath.getTh2File( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TNL ) ) {
            pathname = TDPath.getTnlFile( ze.getName() );
	  /* FIXME_SKETCH_3D *
          } else if ( ze.getName().endsWith( TDPath.TH3 ) ) {
            pathname = TDPath.getTh3File( ze.getName() );
	  * END_SKETCH_3D */
          } else if ( ze.getName().endsWith( TDPath.TDR ) ) {
            pathname = TDPath.getTdrFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TDR3 ) ) {
            pathname = TDPath.getTdr3File( ze.getName() );

          } else if ( ze.getName().endsWith( TDPath.TRB ) ) {
            pathname = TDPath.getTrbFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TRO ) ) {
            pathname = TDPath.getTroFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TOP ) ) {
            pathname = TDPath.getTopFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.XVI ) ) {
            pathname = TDPath.getXviFile( ze.getName() );

          } else if ( ze.getName().endsWith( TDPath.TXT ) ) {
            pathname = TDPath.getNoteFile( ze.getName() );

          } else if ( ze.getName().endsWith( ".wav" ) ) { // AUDIOS
            pathname = TDPath.getAudioDir( surveyname );
            TDFile.makeDir( pathname );
            pathname = TDPath.getAudioFile( surveyname, ze.getName() );
          } else if ( ze.getName().endsWith( ".jpg" ) ) { // PHOTOS
            // FIXME need survey dir
            pathname = TDPath.getJpgDir( surveyname );
            TDFile.makeDir( pathname );
            pathname = TDPath.getJpgFile( surveyname, ze.getName() );
          } else if ( ze.getName().equals( "points.zip" ) ) { // POINTS
            if ( uncompressSymbols( zin, TDPath.getSymbolPointDir(), "p_" ) ) {
              BrushManager.reloadPointLibrary( mApp, mApp.getResources() );
            }
          } else if ( ze.getName().equals( "lines.zip" ) ) { // LINES
            if ( uncompressSymbols( zin, TDPath.getSymbolLineDir(), "l_" ) ) {
              BrushManager.reloadLineLibrary( mApp.getResources() );
            }
          } else if ( ze.getName().equals( "areas.zip" ) ) { // AREAS
            if ( uncompressSymbols( zin, TDPath.getSymbolAreaDir(), "a_" ) ) {
              BrushManager.reloadAreaLibrary( mApp.getResources() );
            }
          } else {
            TDLog.Error("unexpected file type " + ze.getName() );
            // pathname = null; // already null
          }
          TDLog.Log( TDLog.LOG_ZIP, "Zip filename \"" + pathname + "\"" );
          if ( pathname != null ) {
            TDPath.checkPath( pathname );
            FileOutputStream fout = TDFile.getFileOutputStream( pathname );
            int c;
            while ( ( c = zin.read( buffer ) ) != -1 ) {
              fout.write(buffer, 0, c);
            }
            fout.close();
            if ( sql ) {
              TDLog.Log( TDLog.LOG_ZIP, "Zip sqlfile \"" + pathname + "\" DB version " + mApp.mManifestDbVersion );
              sql_success = ( app_data.loadFromFile( pathname, mApp.mManifestDbVersion ) >= 0 );
              TDFile.deleteFile( pathname );
            }
          }
          zin.closeEntry();
        }
      }
      zin.close();
      
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "ERROR File: " + e.toString() );
    } catch ( IOException e ) {
      TDLog.Error( "ERROR IO: " + e.toString() );
    }
    if ( ok_manifest == 0 && ! sql_success ) {
      TDLog.Error( "ERROR SQL" );
      // tell user that there was a problem
      return -5;
    }

    return ok_manifest; // return 0 or 1
  }
}

