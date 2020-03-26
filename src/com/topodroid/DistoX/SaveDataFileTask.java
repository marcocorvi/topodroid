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
    String filename = null;
    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    { 
      switch ( mType ) {
        // case TDConst.DISTOX_EXPORT_TLX:
        //   filename = exportSurveyAsTlx();
        //   break;
        case TDConst.DISTOX_EXPORT_CSX: // cSurvey (only mInfo, no plot-data)
          filename = TDExporter.exportSurveyAsCsx( mSid, mData, mInfo, null, null, null, TDPath.getSurveyCsxFile( mSurvey ));
          break;
        case TDConst.DISTOX_EXPORT_CSV:
	  if ( TDSetting.mCsvRaw ) {
            filename = TDExporter.exportSurveyAsRawCsv( mSid, mData, mInfo, TDPath.getSurveyCsvFile( mSurvey ) );
          } else {
            filename = TDExporter.exportSurveyAsCsv( mSid, mData, mInfo, TDPath.getSurveyCsvFile( mSurvey ) );
	  }
          break;
        case TDConst.DISTOX_EXPORT_CAV: // Topo
          filename = TDExporter.exportSurveyAsCav( mSid, mData, mInfo, TDPath.getSurveyCavFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_DAT: // Compass
          filename = TDExporter.exportSurveyAsDat( mSid, mData, mInfo, TDPath.getSurveyDatFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_DXF:
          List<DBlock> list = mData.selectAllShots( mSid, TDStatus.NORMAL );
          if ( list.size() > 0 ) {
            DBlock blk = list.get( 0 );
            // Log.v( TopoDroidApp.TAG, "DISTOX_EXPORT_DXF from " + blk.mFrom );
            // float decl = mData.getSurveyDeclination( mSid );
            // if ( decl >= SurveyInfo.DECLINATION_MAX ) decl = 0;
            float decl = mInfo.getDeclination();
            TDNum num = new TDNum( list, blk.mFrom, null, null, decl, null ); // null formatClosure
            filename = TDExporter.exportSurveyAsDxf( mSid, mData, mInfo, num, TDPath.getSurveyDxfFile( mSurvey ) );
          }
          break;
        case TDConst.DISTOX_EXPORT_GRT: // Grottolf
          // TDToast.make( "WARNING Grottolf export is untested" );
          filename = TDExporter.exportSurveyAsGrt( mSid, mData, mInfo, TDPath.getSurveyGrtFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_GTX: // GHTopo
          // TDToast.make( "WARNING GHTopo export is untested" );
          filename = TDExporter.exportSurveyAsGtx( mSid, mData, mInfo, TDPath.getSurveyGtxFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_KML: // KML
          filename = TDExporter.exportSurveyAsKml( mSid, mData, mInfo, TDPath.getSurveyKmlFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_JSON: // GeoJSON
          filename = TDExporter.exportSurveyAsJson( mSid, mData, mInfo, TDPath.getSurveyJsonFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_SHP: // Shapefile
          filename = TDExporter.exportSurveyAsShp( mSid, mData, mInfo, TDPath.getShpPath( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_PLT: // Track file
          filename = TDExporter.exportSurveyAsPlt( mSid, mData, mInfo, TDPath.getSurveyPltFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_PLG: // Polygon CAVE
          filename = TDExporter.exportSurveyAsPlg( mSid, mData, mInfo, TDPath.getSurveyCaveFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_SRV: // Walls
          filename = TDExporter.exportSurveyAsSrv( mSid, mData, mInfo, TDPath.getSurveySrvFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_SUR: // WinKarst
          // TDToast.make( "WARNING WinKarst export is untested" );
          filename = TDExporter.exportSurveyAsSur( mSid, mData, mInfo, TDPath.getSurveySurFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_SVX: // Survex
          filename = TDExporter.exportSurveyAsSvx( mSid, mData, mInfo, mDevice, TDPath.getSurveySvxFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_TRO: // VisualTopo
          filename = TDExporter.exportSurveyAsTro( mSid, mData, mInfo, TDPath.getSurveyTroFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_TRB: // TopoRobot
          // TDToast.make( "WARNING TopoRobot export is untested" );
          filename = TDExporter.exportSurveyAsTrb( mSid, mData, mInfo, TDPath.getSurveyTrbFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_TOP: // PocketTopo
          filename = TDExporter.exportSurveyAsTop( mSid, mData, mInfo, null, null, TDPath.getSurveyTopFile( mSurvey ) );
          break;

        case TDConst.DISTOX_EXPORT_TH:
        default:
          filename = TDExporter.exportSurveyAsTh( mSid, mData, mInfo, TDPath.getSurveyThFile( mSurvey ) );
          break;
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

