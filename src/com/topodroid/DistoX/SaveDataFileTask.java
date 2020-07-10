/* @file SaveDataFileTask.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid drawing: save drawing in therion format
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDStatus;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;

// import java.lang.ref.WeakReference;

import java.util.List;

import java.io.File;
import java.io.IOException;

import android.os.AsyncTask;

// import android.util.Log;

class SaveDataFileTask extends AsyncTask<Void, Void, String >
{
  private String mSaving;   // for the toast
  private long  mSid;
  private DataHelper mData;
  private SurveyInfo mInfo;
  private String mSurvey;
  private Device mDevice;   // only for SVX
  private int mType;        // export type
  private boolean mToast;

  SaveDataFileTask( String saving, long sid, SurveyInfo info, DataHelper data, String survey, Device device, int type, boolean toast )
  {
     mSaving  = saving;
     mSid     = sid;
     mInfo    = info.copy();
     mData    = data;
     mSurvey  = survey;
     mDevice  = device;
     mType    = type;
     mToast   = toast;
  }

  // async exec
  @Override
  protected String doInBackground(Void... arg0)
  {
    if ( mInfo == null ) return null;
    // boolean do_binary = (TDSetting.mBinaryTh2 && mSuffix != DataSave.EXPORT ); // TDR BINARY
    return immed_exec();
  }

  // sync exec (immediate)
  String immed_exec() 
  {
    int ret = 0;
    String filename = null;
    String pathname = null;
    String dirname  = null;
    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    if ( mType == TDConst.DISTOX_EXPORT_SHP ) {
      pathname = TDPath.getShpPath( mSurvey );
      // FIXME too-big synch
      synchronized ( TDPath.mFilesLock ) {
        filename = TDExporter.exportSurveyAsShp( mSid, mData, mInfo, pathname );
      }
    } else { 
      switch ( mType ) {
        // case TDConst.DISTOX_EXPORT_TLX:
        //   filename = exportSurveyAsTlx();
        //   break;
        case TDConst.DISTOX_EXPORT_CSX: // cSurvey (only mInfo, no plot-data)
          pathname = TDPath.getSurveyCsxFile( mSurvey );
          dirname  = TDPath.getCsxFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_CSV:
          pathname = TDPath.getSurveyCsvFile( mSurvey );
          dirname  = TDPath.getCsvFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_CAV: // Topo
          pathname = TDPath.getSurveyCavFile( mSurvey );
          dirname  = TDPath.getCavFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_DAT: // Compass
          pathname = TDPath.getSurveyDatFile( mSurvey );
          dirname  = TDPath.getDatFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_DXF:
          pathname = TDPath.getSurveyDxfFile( mSurvey );
          dirname  = TDPath.getDxfFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_GRT: // Grottolf
          pathname = TDPath.getSurveyGrtFile( mSurvey );
          dirname  = TDPath.getGrtFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_GTX: // GHTopo
          pathname = TDPath.getSurveyGtxFile( mSurvey );
          dirname  = TDPath.getGtxFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_KML: // KML
          pathname = TDPath.getSurveyKmlFile( mSurvey );
          dirname  = TDPath.getKmlFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_JSON: // GeoJSON
          pathname = TDPath.getSurveyJsonFile( mSurvey );
          dirname  = TDPath.getJsonFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_SHP: // Shapefile
          // pathname = TDPath.getShpPath( mSurvey );
          // dirname  = TDPath.getShzFile( "" );
          return null; // cannot happen
          // break;
        case TDConst.DISTOX_EXPORT_PLT: // Track file
          pathname = TDPath.getSurveyPltFile( mSurvey );
          dirname  = TDPath.getPltFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_PLG: // Polygon CAVE
          pathname = TDPath.getSurveyCaveFile( mSurvey );
          dirname  = TDPath.getCaveFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_SRV: // Walls
          pathname = TDPath.getSurveySrvFile( mSurvey );
          dirname  = TDPath.getSrvFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_SUR: // WinKarst
          pathname = TDPath.getSurveySurFile( mSurvey );
          dirname  = TDPath.getSurFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_SVX: // Survex
          pathname = TDPath.getSurveySvxFile( mSurvey );
          dirname  = TDPath.getSvxFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_TRO: // VisualTopo
          pathname = TDPath.getSurveyTroFile( mSurvey );
          dirname  = TDPath.getTroFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_TRB: // TopoRobot
          pathname = TDPath.getSurveyTrbFile( mSurvey );
          dirname  = TDPath.getTrbFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_TOP: // PocketTopo
          pathname = TDPath.getSurveyTopFile( mSurvey );
          dirname  = TDPath.getTopFile( "" );
          break;
        case TDConst.DISTOX_EXPORT_TH:
          pathname = TDPath.getSurveyThFile( mSurvey );
          dirname  = TDPath.getThFile( "" );
          break;
        default:
          return null;
      }
      File temp = null;
      try {
        temp = File.createTempFile( "tmp", null, new File( dirname ) );
      } catch ( IOException e ) { return null; }

      switch ( mType ) {
        // case TDConst.DISTOX_EXPORT_TLX:
        //   filename = exportSurveyAsTlx();
        //   break;
        case TDConst.DISTOX_EXPORT_CSX: // cSurvey (only mInfo, no plot-data)
          ret = TDExporter.exportSurveyAsCsx( mSid, mData, mInfo, null, null, null, temp );
          break;
        case TDConst.DISTOX_EXPORT_CSV:
	  if ( TDSetting.mCsvRaw ) {
            ret = TDExporter.exportSurveyAsRawCsv( mSid, mData, mInfo, temp );
          } else {
            ret = TDExporter.exportSurveyAsCsv( mSid, mData, mInfo, temp );
	  }
          break;
        case TDConst.DISTOX_EXPORT_CAV: // Topo
          ret = TDExporter.exportSurveyAsCav( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_DAT: // Compass
          ret = TDExporter.exportSurveyAsDat( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_DXF:
          List< DBlock > list = mData.selectAllShots( mSid, TDStatus.NORMAL );
          if ( list.size() > 0 ) {
            DBlock blk = list.get( 0 );
            // Log.v( TopoDroidApp.TAG, "DISTOX_EXPORT_DXF from " + blk.mFrom );
            // float decl = mData.getSurveyDeclination( mSid );
            // if ( decl >= SurveyInfo.DECLINATION_MAX ) decl = 0;
            float decl = mInfo.getDeclination();
            TDNum num = new TDNum( list, blk.mFrom, null, null, decl, null ); // null formatClosure
            ret = TDExporter.exportSurveyAsDxf( mSid, mData, mInfo, num, temp );
          }
          break;
        case TDConst.DISTOX_EXPORT_GRT: // Grottolf
          // TDToast.make( "WARNING Grottolf export is untested" );
          ret = TDExporter.exportSurveyAsGrt( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_GTX: // GHTopo
          // TDToast.make( "WARNING GHTopo export is untested" );
          ret = TDExporter.exportSurveyAsGtx( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_KML: // KML
          ret = TDExporter.exportSurveyAsKml( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_JSON: // GeoJSON
          ret = TDExporter.exportSurveyAsJson( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_SHP: // Shapefile
          // filename = TDExporter.exportSurveyAsShp( mSid, mData, mInfo, TDPath.getShpPath( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_PLT: // Track file
          ret = TDExporter.exportSurveyAsPlt( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_PLG: // Polygon CAVE
          ret = TDExporter.exportSurveyAsPlg( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_SRV: // Walls
          ret = TDExporter.exportSurveyAsSrv( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_SUR: // WinKarst
          // TDToast.make( "WARNING WinKarst export is untested" );
          ret = TDExporter.exportSurveyAsSur( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_SVX: // Survex
          ret = TDExporter.exportSurveyAsSvx( mSid, mData, mInfo, mDevice, temp );
          break;
        case TDConst.DISTOX_EXPORT_TRO: // VisualTopo
          ret = TDExporter.exportSurveyAsTro( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_TRB: // TopoRobot
          // TDToast.make( "WARNING TopoRobot export is untested" );
          ret = TDExporter.exportSurveyAsTrb( mSid, mData, mInfo, temp );
          break;
        case TDConst.DISTOX_EXPORT_TOP: // PocketTopo
          ret = TDExporter.exportSurveyAsTop( mSid, mData, mInfo, null, null, temp );
          break;

        case TDConst.DISTOX_EXPORT_TH:
        default:
          ret = TDExporter.exportSurveyAsTh( mSid, mData, mInfo, temp );
          break;
      }
      if ( ret == 1 ) {
        filename = pathname;
      } else if ( ret == 2 ) {
        filename = "";
      }
      synchronized( TDPath.mFilesLock ) {
        File file = new File( pathname );
        temp.renameTo( file );
      }
    }
    return filename;
  }

  @Override
  protected void onPostExecute( String filename )
  {
    if ( mToast ) { 
      if ( filename == null ) {
        TDToast.makeBad( R.string.saving_file_failed );
      } else if ( filename.length() == 0 ) {
        TDToast.makeBad( R.string.no_geo_station );
      } else {
        TDToast.make( mSaving + filename ); // FIXME_FORMAT
      }
    }
  }

}

