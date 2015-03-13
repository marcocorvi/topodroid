/** @file Archiver.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey archiver
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120611 created
 * 20120619 added therion export to the zip
 * 20120720 added manifest
 * 20120725 TopoDroidApp log
 * 20130324 zip export of 3D sketches
 * 201311   commented 3d sketches
 * 201401   added files CSV
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
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

// import android.util.Log;


public class Archiver
{
  private TopoDroidApp app;
  private static final int BUF_SIZE = 2048;
  private byte[] data = new byte[ BUF_SIZE ];

  public String zipname;

  public Archiver( TopoDroidApp _app )
  {
    app = _app;
    data = new byte[ BUF_SIZE ];
  }

  private boolean addEntry( ZipOutputStream zos, File name )
  {
    try {
      FileInputStream fis = new FileInputStream( name );
      BufferedInputStream bis = new BufferedInputStream( fis, BUF_SIZE );
      ZipEntry entry = new ZipEntry( name.getName() );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
      zos.write( data, 0, cnt );
      }
      bis.close();
      zos.closeEntry( );
    } catch (FileNotFoundException e ) {
      // FIXME
      return false;
    } catch ( IOException e ) {
      // FIXME
    }
    return true;
  }

  public boolean archive( )
  {
    if ( app.mSID < 0 ) return false;
    
    File temp = null;
    String survey = app.mySurvey;

    zipname = TopoDroidPath.getSurveyZipFile( survey );
    TopoDroidPath.checkPath( zipname );

    try {
      String pathname;
      FileOutputStream fos = new FileOutputStream( zipname );
      ZipOutputStream zos = new ZipOutputStream( new BufferedOutputStream( fos ) );

      // FIXME_SKETCH_3D
      List< Sketch3dInfo > sketches  = app.mData.selectAllSketches( app.mSID, TopoDroidApp.STATUS_NORMAL );
      for ( Sketch3dInfo skt : sketches ) {
        pathname = TopoDroidPath.getSurveySketchFile( survey, skt.name );
        addEntry( zos, new File( pathname ) );
      }
      sketches  = app.mData.selectAllSketches( app.mSID, TopoDroidApp.STATUS_DELETED );
      for ( Sketch3dInfo skt : sketches ) {
        pathname = TopoDroidPath.getSurveySketchFile( survey, skt.name );
        addEntry( zos, new File( pathname ) );
      }
      // END_SKETCH_3D

      List< PlotInfo > plots  = app.mData.selectAllPlots( app.mSID, TopoDroidApp.STATUS_NORMAL );
      for ( PlotInfo plt : plots ) {
        pathname = TopoDroidPath.getSurveyPlotTh2File( survey, plt.name );
        addEntry( zos, new File(pathname) );
        File dxf2 = new File( TopoDroidPath.getSurveyPlotDxfFile( survey, plt.name ) );
        if ( dxf2 != null && dxf2.exists() ) {
          addEntry( zos, dxf2 );
        }
        if ( plt.type == PlotInfo.PLOT_PLAN ) {
          File csx2 = new File( TopoDroidPath.getSurveyCsxFile( survey, plt.name ) );
          if ( csx2 != null && csx2.exists() ) {
            addEntry( zos, csx2 );
          }
        }
      }
      plots  = app.mData.selectAllPlots( app.mSID, TopoDroidApp.STATUS_DELETED );
      for ( PlotInfo plt : plots ) {
        pathname = TopoDroidPath.getSurveyPlotTh2File( survey, plt.name );
        addEntry( zos, new File(pathname) );
        File dxf2 = new File( TopoDroidPath.getSurveyPlotDxfFile( survey, plt.name ) );
        if ( dxf2 != null && dxf2.exists() ) {
          addEntry( zos, dxf2 );
        }
      }

      List< PhotoInfo > photos = app.mData.selectAllPhotos( app.mSID, TopoDroidApp.STATUS_NORMAL );
      for ( PhotoInfo pht : photos ) {
        pathname = TopoDroidPath.getSurveyJpgFile( survey, Long.toString(pht.id) );
        addEntry( zos, new File(pathname) );
      }

      photos = app.mData.selectAllPhotos( app.mSID, TopoDroidApp.STATUS_DELETED );
      for ( PhotoInfo pht : photos ) {
        pathname = TopoDroidPath.getSurveyJpgFile( survey, Long.toString(pht.id) );
        addEntry( zos, new File(pathname) );
      }


      File therion = new File( TopoDroidPath.getSurveyThFile( survey ) );
      if ( therion != null && therion.exists() ) {
        addEntry( zos, therion );
      }

      File vtopo = new File( TopoDroidPath.getSurveyTroFile( survey ) );
      if ( vtopo != null && vtopo.exists() ) {
        addEntry( zos, vtopo );
      }

      File ptopo = new File( TopoDroidPath.getSurveyTopFile( survey ) );
      if ( ptopo != null && ptopo.exists() ) {
        addEntry( zos, ptopo );
      }

      File survex = new File( TopoDroidPath.getSurveySvxFile( survey ) );
      if ( survex != null && survex.exists() ) {
        addEntry( zos, survex );
      }

      File csv = new File( TopoDroidPath.getSurveyCsvFile( survey ) );
      if ( csv != null && csv.exists() ) {
        addEntry( zos, csv );
      }

      File csurvex = new File( TopoDroidPath.getSurveyCsxFile( survey ) );
      if ( csurvex != null && csurvex.exists() ) {
        addEntry( zos, csurvex );
      }

      File compass = new File( TopoDroidPath.getSurveyDatFile( survey ) );
      if ( compass != null && compass.exists() ) {
        addEntry( zos, compass );
      }

      File dxf = new File( TopoDroidPath.getSurveyDxfFile( survey ) );
      if ( dxf != null && dxf.exists() ) {
        addEntry( zos, dxf );
      }

      File note = new File( TopoDroidPath.getSurveyNoteFile( survey ) );
      if ( note != null && note.exists() ) {
        addEntry( zos, note );
      }
 
      pathname = TopoDroidPath.getSqlFile( );
      app.mData.dumpToFile( pathname, app.mSID );
      addEntry( zos, new File(pathname) );

      pathname = TopoDroidPath.getManifestFile();
      app.writeManifestFile();
      addEntry( zos, new File(pathname) );

      zos.close();
    } catch ( FileNotFoundException e ) {
      // FIXME
      return false;
    } catch ( IOException e ) {
      // FIXME
      return false;
    } finally {
      File fp = new File( TopoDroidPath.getSqlFile() );
      if ( fp.exists() ) {
        // fp.delete();
      }
    }
    return true;
  }

  public int unArchive( String filename, String surveyname )
  {
    int ok_manifest = -2;
    String pathname;
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];

      TopoDroidLog.Log( TopoDroidLog.LOG_ZIP, "unzip " + filename );
      ZipFile zip = new ZipFile( filename );
      ZipEntry ze = zip.getEntry( "manifest" );
      if ( ze != null ) {
        pathname = TopoDroidPath.getManifestFile();
        // TopoDroidApp.checkPath( pathname );
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
      TopoDroidLog.Log( TopoDroidLog.LOG_ZIP, "unArchive manifest " + ok_manifest );
      if ( ok_manifest == 0 ) {
        FileInputStream fis = new FileInputStream( filename );
        ZipInputStream zin = new ZipInputStream( fis );
        while ( ( ze = zin.getNextEntry() ) != null ) {
          if ( ze.isDirectory() ) {
            File dir = new File( TopoDroidPath.getDirFile( ze.getName() ) );
            if ( ! dir.isDirectory() ) {
              dir.mkdirs();
            }
          } else {
            TopoDroidLog.Log( TopoDroidLog.LOG_ZIP, "Zip entry \"" + ze.getName() + "\"" );
            boolean sql = false;
            pathname = null;
            if ( ze.getName().equals( "manifest" ) ) {
              // skip
            } else if ( ze.getName().equals( "survey.sql" ) ) {
              pathname = TopoDroidPath.getSqlFile();
              sql = true;
            } else if ( ze.getName().endsWith( ".csv" ) ) {
              pathname = TopoDroidPath.getCsvFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".csx" ) ) {
              pathname = TopoDroidPath.getCsxFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".dat" ) ) {
              pathname = TopoDroidPath.getDatFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".dxf" ) ) {
              pathname = TopoDroidPath.getDxfFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".png" ) ) {
              pathname = TopoDroidPath.getPngFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".svg" ) ) {
              pathname = TopoDroidPath.getSvgFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".svx" ) ) {
              pathname = TopoDroidPath.getSvxFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".srv" ) ) {
              pathname = TopoDroidPath.getSrvFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".tro" ) ) {
              pathname = TopoDroidPath.getTroFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".top" ) ) {
              pathname = TopoDroidPath.getTopFile( ze.getName() );

            } else if ( ze.getName().endsWith( ".th" ) ) {
              pathname = TopoDroidPath.getThFile( ze.getName() );
            } else if ( ze.getName().endsWith( ".th2" ) ) {
              pathname = TopoDroidPath.getTh2File( ze.getName() );
            } else if ( ze.getName().endsWith( ".th3" ) ) {
              pathname = TopoDroidPath.getTh3File( ze.getName() );

            } else if ( ze.getName().endsWith( ".jpg" ) ) { // PHOTOS
              // FIXME need survey dir
              pathname = TopoDroidPath.getJpgDir( surveyname );
              File file = new File( pathname );
              file.mkdirs();
              pathname = TopoDroidPath.getJpgFile( surveyname, ze.getName() );
            } else {
              pathname = TopoDroidPath.getNoteFile( ze.getName() );
            }
            TopoDroidLog.Log( TopoDroidLog.LOG_ZIP, "Zip filename \"" + pathname + "\"" );
            if ( pathname != null ) {
              TopoDroidApp.checkPath( pathname );
              FileOutputStream fout = new FileOutputStream( pathname );
              int c;
              while ( ( c = zin.read( buffer ) ) != -1 ) {
                fout.write(buffer, 0, c);
              }
              fout.close();
              if ( sql ) {
                TopoDroidLog.Log( TopoDroidLog.LOG_ZIP, "Zip sqlfile \"" + pathname + "\"" );
                app.mData.loadFromFile( pathname );
                File f = new File( pathname );
                f.delete();
              }
            }
            zin.closeEntry();
          }
        }
        zin.close();
      }
    } catch ( FileNotFoundException e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "ERROR File: " + e.toString() );
    } catch ( IOException e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "ERROR IO: " + e.toString() );
    }
    return ok_manifest;
  }
}

