/** @file Archiver.java
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

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.util.List;
// import java.util.Locale;
// import java.util.ArrayList;

// import android.content.Context;
// import android.content.Intent;

class Archiver
{
  // private final TopoDroidApp mApp;
  private static final int BUF_SIZE = 4096;
  private byte[] data; // = new byte[ BUF_SIZE ];

  String zipname;

  Archiver( ) // TopoDroidApp app
  {
    // mApp = app;
    data = new byte[ BUF_SIZE ];
  }

  private boolean addEntry( ZipOutputStream zos, File name )
  {
    if ( name == null || ! name.exists() ) return false;
    Log.v( "DistoX-ZIP", "zip add file " + name.getPath() );
    boolean ret = false;
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "zip add file " + name.getPath() );
      bis = new BufferedInputStream( new FileInputStream( name ), BUF_SIZE );
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
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { ret = false; }
    }
    return ret;
  }

  private void addOptionalEntry( ZipOutputStream zos, File name )
  {
    if ( name == null || ! name.exists() ) return;
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "zip add file " + name.getPath() );
      bis = new BufferedInputStream( new FileInputStream( name ), BUF_SIZE );
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

  private void addDirectory( ZipOutputStream zos, File dir )
  {
    if ( ! dir.exists() ) return;
    // Log.v( "DistoX-ZIP", "zip add dir " + dir.getPath() );
    File[] files = dir.listFiles();
    if ( files != null ) {
      for ( File file : files ) { // listFiles MAY NullPointerException
        if (file.isFile()) addOptionalEntry(zos, file); 
      }
    }
  }

  boolean compressFiles( String zipname, List<File> files )
  {
    ZipOutputStream zos = null;
    boolean ret = true;
    try {
      zos = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream( zipname ) ) );
      for ( File file : files ) ret &= addEntry( zos, file );
      // for ( File file : files ) TDUtil.deleteFile( file );
    } catch ( FileNotFoundException e ) {
    // } catch ( IOException e ) {
    //   // FIXME
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { 
        ret = false;
        // TDLog.Error("ZIP compress close error");
      }
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
    TDLog.Log( TDLog.LOG_IO, "zip export file: " + zipname );

    ZipOutputStream zos = null;
    try {
      String pathname;
      zos = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream( zipname ) ) );

/* FIXME_SKETCH_3D *
      List< Sketch3dInfo > sketches  = app_data.selectAllSketches( TDInstance.sid, TDStatus.NORMAL );
      for ( Sketch3dInfo skt : sketches ) {
        ret &= addEntry( zos, new File( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
      sketches  = app_data.selectAllSketches( TDInstance.sid, TDStatus.DELETED );
      for ( Sketch3dInfo skt : sketches ) {
        ret &= addEntry( zos, new File( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
/* END_SKETCH_3D */

      List< PlotInfo > plots  = app_data.selectAllPlots( TDInstance.sid, TDStatus.NORMAL );
      for ( PlotInfo plt : plots ) {
        ret &= addEntry( zos, new File( TDPath.getSurveyPlotTdrFile( survey, plt.name ) ) );
        addOptionalEntry( zos, new File( TDPath.getSurveyPlotTh2File( survey, plt.name ) ) );
        addOptionalEntry( zos, new File( TDPath.getSurveyPlotDxfFile( survey, plt.name ) ) );
        addOptionalEntry( zos, new File( TDPath.getSurveyPlotSvgFile( survey, plt.name ) ) );
        addOptionalEntry( zos, new File( TDPath.getSurveyPlotXviFile( survey, plt.name ) ) );
        addOptionalEntry( zos, new File( TDPath.getSurveyPlotPngFile( survey, plt.name ) ) );
        if ( plt.type == PlotInfo.PLOT_PLAN ) {
          addOptionalEntry( zos, new File( TDPath.getSurveyCsxFile( survey, plt.name ) ) );
        }
	addOptionalEntry( zos, new File( TDPath.getSurveyPlotShzFile( survey, plt.name ) ) );
      }

      // try overview exports
      addOptionalEntry( zos, new File( TDPath.getSurveyPlotTh2File( survey, "p" ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyPlotDxfFile( survey, "p" ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyPlotSvgFile( survey, "p" ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyPlotXviFile( survey, "p" ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyPlotShzFile( survey, "p" ) ) ); // zipped shp

      addOptionalEntry( zos, new File( TDPath.getSurveyPlotTh2File( survey, "s" ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyPlotDxfFile( survey, "s" ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyPlotSvgFile( survey, "s" ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyPlotXviFile( survey, "s" ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyPlotShzFile( survey, "s" ) ) ); // zipped shp

      plots  = app_data.selectAllPlots( TDInstance.sid, TDStatus.DELETED );
      for ( PlotInfo plt : plots ) {
        ret &= addEntry( zos, new File( TDPath.getSurveyPlotTdrFile( survey, plt.name ) ) );
      }

      addDirectory( zos, new File( TDPath.getSurveyPhotoDir( survey ) ) );

      addDirectory( zos, new File( TDPath.getSurveyAudioDir( survey ) ) );

      addOptionalEntry( zos, new File( TDPath.getSurveyThFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyCsvFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyCsxFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyCaveFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyCavFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyDatFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyDxfFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyGrtFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyGtxFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyKmlFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyJsonFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyPltFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyShzFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveySrvFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveySurFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveySvxFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyTroFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyTrbFile( survey ) ) );
      addOptionalEntry( zos, new File( TDPath.getSurveyTopFile( survey ) ) );

      addOptionalEntry( zos, new File( TDPath.getSurveyNoteFile( survey ) ) );
 
      pathname = TDPath.getSqlFile( );
      app_data.dumpToFile( pathname, TDInstance.sid );
      ret &= addEntry( zos, new File(pathname) );

      pathname = TDPath.getManifestFile();
      mApp.writeManifestFile();
      ret &= addEntry( zos, new File(pathname) );

      // ret = true;
    } catch ( FileNotFoundException e ) {
      Log.v("DistoX-ZIP", e.getMessage() );
      ret = false;
    // } catch ( IOException e ) {
    //   // FIXME
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { TDLog.Error("ZIP close error"); }
      TDUtil.deleteFile( TDPath.getSqlFile() );
    }
    return ret;
  }

  int unArchive( TopoDroidApp mApp, String filename, String surveyname )
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
        FileOutputStream fout = new FileOutputStream( pathname );
        InputStream is = zip.getInputStream( ze );
        int c;
        while ( ( c = is.read( buffer ) ) != -1 ) {
          fout.write(buffer, 0, c);
        }
        fout.close();
        ok_manifest = mApp.checkManifestFile( pathname, surveyname  ); // this sets surveyname
        TDUtil.deleteFile( pathname );
      }
      zip.close();
      TDLog.Log( TDLog.LOG_ZIP, "un-archive manifest " + ok_manifest );
      if ( ok_manifest < 0 ) return ok_manifest;
      TDLog.Log( TDLog.LOG_ZIP, "un-archive survey " + surveyname );

      // TDLog.Log( TDLog.LOG_IO, "unzip file " + filename );
      FileInputStream fis = new FileInputStream( filename );
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        if ( ze.isDirectory() ) {
          TDUtil.makeDir( TDPath.getDirFile( ze.getName() ) );
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
            TDUtil.makeDir( pathname );
            pathname = TDPath.getAudioFile( surveyname, ze.getName() );
          } else if ( ze.getName().endsWith( ".jpg" ) ) { // PHOTOS
            // FIXME need survey dir
            pathname = TDPath.getJpgDir( surveyname );
            TDUtil.makeDir( pathname );
            pathname = TDPath.getJpgFile( surveyname, ze.getName() );
          // } else {
            // unexpected file type
            // pathname = null; // already null
          }
          TDLog.Log( TDLog.LOG_ZIP, "Zip filename \"" + pathname + "\"" );
          if ( pathname != null ) {
            TDPath.checkPath( pathname );
            FileOutputStream fout = new FileOutputStream( pathname );
            int c;
            while ( ( c = zin.read( buffer ) ) != -1 ) {
              fout.write(buffer, 0, c);
            }
            fout.close();
            if ( sql ) {
              TDLog.Log( TDLog.LOG_ZIP, "Zip sqlfile \"" + pathname + "\" DB version " + mApp.mManifestDbVersion );
              sql_success = ( app_data.loadFromFile( pathname, mApp.mManifestDbVersion ) >= 0 );
              TDUtil.deleteFile( pathname );
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

    return ok_manifest; // return 0
  }
}

