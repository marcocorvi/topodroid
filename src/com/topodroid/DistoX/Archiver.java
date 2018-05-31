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

// import android.util.Log;


class Archiver
{
  private final TopoDroidApp app;
  private final DataHelper app_data;
  private static final int BUF_SIZE = 2048;
  private byte[] data = new byte[ BUF_SIZE ];

  String zipname;

  Archiver( TopoDroidApp _app )
  {
    app = _app;
    app_data = TopoDroidApp.mData;
    data = new byte[ BUF_SIZE ];
  }

  private boolean addEntry( ZipOutputStream zos, File name )
  {
    if ( name == null || ! name.exists() ) return false;
    boolean ret = false;
    BufferedInputStream bis = null;
    try { 
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
      // FIXME
    } catch ( IOException e ) {
      // FIXME
    } finally {
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { }
    }
    return ret;
  }

  boolean archive( )
  {
    if ( app.mSID < 0 ) return false;
    
    // File temp = null;
    String survey = app.mySurvey;
    boolean ret = true;

    zipname = TDPath.getSurveyZipFile( survey );
    TDPath.checkPath( zipname );

    ZipOutputStream zos = null;
    try {
      String pathname;
      zos = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream( zipname ) ) );

/* FIXME BEGIN SKETCH_3D */
      List< Sketch3dInfo > sketches  = app_data.selectAllSketches( app.mSID, TDStatus.NORMAL );
      for ( Sketch3dInfo skt : sketches ) {
        addEntry( zos, new File( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
      sketches  = app_data.selectAllSketches( app.mSID, TDStatus.DELETED );
      for ( Sketch3dInfo skt : sketches ) {
        addEntry( zos, new File( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
/* END SKETCH_3D */

      List< PlotInfo > plots  = app_data.selectAllPlots( app.mSID, TDStatus.NORMAL );
      for ( PlotInfo plt : plots ) {
        addEntry( zos, new File( TDPath.getSurveyPlotTh2File( survey, plt.name ) ) );
        addEntry( zos, new File( TDPath.getSurveyPlotTdrFile( survey, plt.name ) ) );
        addEntry( zos, new File( TDPath.getSurveyPlotDxfFile( survey, plt.name ) ) );
        addEntry( zos, new File( TDPath.getSurveyPlotSvgFile( survey, plt.name ) ) );
        addEntry( zos, new File( TDPath.getSurveyPlotHtmFile( survey, plt.name ) ) ); // SVG in HTML
        addEntry( zos, new File( TDPath.getSurveyPlotPngFile( survey, plt.name ) ) );
        if ( plt.type == PlotInfo.PLOT_PLAN ) {
          addEntry( zos, new File( TDPath.getSurveyCsxFile( survey, plt.name ) ) );
        }
      }

      plots  = app_data.selectAllPlots( app.mSID, TDStatus.DELETED );
      for ( PlotInfo plt : plots ) {
        addEntry( zos, new File( TDPath.getSurveyPlotTdrFile( survey, plt.name ) ) );
      }

      List< PhotoInfo > photos = app_data.selectAllPhotos( app.mSID, TDStatus.NORMAL );
      for ( PhotoInfo pht : photos ) {
        addEntry( zos, new File( TDPath.getSurveyJpgFile( survey, Long.toString(pht.id) ) ) );
      }

      photos = app_data.selectAllPhotos( app.mSID, TDStatus.DELETED );
      for ( PhotoInfo pht : photos ) {
        addEntry( zos, new File( TDPath.getSurveyJpgFile( survey, Long.toString(pht.id) ) ) );
      }

      List< AudioInfo > audios = app_data.selectAllAudios( app.mSID );
      for ( AudioInfo audio : audios ) {
        addEntry( zos, new File( TDPath.getSurveyAudioFile( survey, Long.toString( audio.shotid ) ) ) );
      }

      addEntry( zos, new File( TDPath.getSurveyCsvFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyCsxFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyCaveFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyDatFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyDxfFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyGrtFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyGtxFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyKmlFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyPltFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveySrvFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveySvxFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyThFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyTroFile( survey ) ) );
      addEntry( zos, new File( TDPath.getSurveyTopFile( survey ) ) );

      addEntry( zos, new File( TDPath.getSurveyNoteFile( survey ) ) );
 
      pathname = TDPath.getSqlFile( );
      app_data.dumpToFile( pathname, app.mSID );
      addEntry( zos, new File(pathname) );

      pathname = TDPath.getManifestFile();
      app.writeManifestFile();
      addEntry( zos, new File(pathname) );

      ret = true;
    } catch ( FileNotFoundException e ) {
      // FIXME
    // } catch ( IOException e ) {
    //   // FIXME
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { }
      File fp = new File( TDPath.getSqlFile() );
      if ( fp.exists() ) {
        fp.delete();
      }
    }
    return ret;
  }

  int unArchive( String filename, String surveyname )
  {
    boolean sql_success = false;
    int ok_manifest = -2;
    String pathname;
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];

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
        ok_manifest = app.checkManifestFile( pathname, surveyname  );
        File f = new File( pathname );
        f.delete();
      }
      zip.close();
      TDLog.Log( TDLog.LOG_ZIP, "un-archive manifest " + ok_manifest );
      if ( ok_manifest < 0 ) return ok_manifest;

      FileInputStream fis = new FileInputStream( filename );
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        if ( ze.isDirectory() ) {
          File dir = new File( TDPath.getDirFile( ze.getName() ) );
          if ( ! dir.isDirectory() ) {
            dir.mkdirs();
          }
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
          } else if ( ze.getName().endsWith( TDPath.PLT ) ) {
            pathname = TDPath.getPltFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.PNG ) ) {
            pathname = TDPath.getPngFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.SRV ) ) {
            pathname = TDPath.getSrvFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.SVG ) ) {
            pathname = TDPath.getSvgFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.SVX ) ) {
            pathname = TDPath.getSvxFile( ze.getName() );

          } else if ( ze.getName().endsWith( TDPath.TH ) ) {
            pathname = TDPath.getThFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TH2 ) ) {
            pathname = TDPath.getTh2File( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TH3 ) ) {
            pathname = TDPath.getTh3File( ze.getName() );
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

          } else if ( ze.getName().endsWith( TDPath.TXT ) ) {
            pathname = TDPath.getNoteFile( ze.getName() );

          } else if ( ze.getName().endsWith( ".wav" ) ) { // AUDIOS
            pathname = TDPath.getAudioDir( surveyname );
            File file = new File( pathname );
            file.mkdirs();
            pathname = TDPath.getAudioFile( surveyname, ze.getName() );
          } else if ( ze.getName().endsWith( ".jpg" ) ) { // PHOTOS
            // FIXME need survey dir
            pathname = TDPath.getJpgDir( surveyname );
            File file = new File( pathname );
            file.mkdirs();
            pathname = TDPath.getJpgFile( surveyname, ze.getName() );
          } else {
            // unexpected file type
            pathname = null; //
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
              TDLog.Log( TDLog.LOG_ZIP, "Zip sqlfile \"" + pathname + "\"" );
              sql_success = ( app_data.loadFromFile( pathname, app.mManifestDbVersion ) >= 0 );
              File f = new File( pathname );
              f.delete();
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
      // TODO tell user that there was a problem
      return -5;
    }

    return ok_manifest; // return 0
  }
}

