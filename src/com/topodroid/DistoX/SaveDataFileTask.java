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

import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDStatus;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;

// import java.lang.ref.WeakReference;

import java.util.List;

import android.os.AsyncTask;

import android.util.Log;

class SaveDataFileTask extends AsyncTask<Void, Void, String >
{
  private String mFormat;   // for the toast
  private long  mSid;
  private DataHelper mData;
  private SurveyInfo mInfo;
  private String mSurvey;
  private Device mDevice;   // only for SVX
  private int mType;        // export type
  private boolean mToast;

  SaveDataFileTask( String format, long sid, SurveyInfo info, DataHelper data, String survey, Device device, int type, boolean toast )
  {
    mFormat  = format;
    mSid     = sid;
    mInfo    = info.copy();
    mData    = data;
    mSurvey  = survey;
    mDevice  = device;
    mType    = type;
    mToast   = toast;
    // Log.v("DistoX", "save data file task - type " + mType);
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
    // Log.v("DistoX", "save data file task - execute");
    int ret = 0;
    String pathname = null;
    synchronized ( TDFile.mFilesLock ) { // too-big synch
      switch ( mType ) {
        // case TDConst.DISTOX_EXPORT_TLX:
        //   filename = exportSurveyAsTlx();
        //   break;
        case TDConst.DISTOX_EXPORT_CSX: // cSurvey (only mInfo, no plot-data)
          pathname = mSurvey + ".csx";
          ret = TDExporter.exportSurveyAsCsx( mSid, mData, mInfo, null, null, null, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_CSV:
          pathname = mSurvey + ".csv";
          if ( TDSetting.mCsvRaw ) {
            ret = TDExporter.exportSurveyAsRawCsv( mSid, mData, mInfo, mSurvey );
          } else {
            ret = TDExporter.exportSurveyAsCsv( mSid, mData, mInfo, mSurvey );
          }
          break;
        case TDConst.DISTOX_EXPORT_CAV: // Topo
          pathname = mSurvey + ".cav";
          ret = TDExporter.exportSurveyAsCav( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_DAT: // Compass
          pathname = mSurvey + ".dat";
          ret = TDExporter.exportSurveyAsDat( mSid, mData, mInfo, mSurvey );
          // Log.v("DistoX", "save DAT " + ret );
          break;
        case TDConst.DISTOX_EXPORT_DXF:
          pathname = mSurvey + ".dxf";
          List< DBlock > list = mData.selectAllShots( mSid, TDStatus.NORMAL );
          if ( list.size() > 0 ) {
            DBlock blk = list.get( 0 );
            // Log.v( TopoDroidApp.TAG, "DISTOX_EXPORT_DXF from " + blk.mFrom );
            // float decl = mData.getSurveyDeclination( mSid );
            // if ( decl >= SurveyInfo.DECLINATION_MAX ) decl = 0;
            float decl = mInfo.getDeclination();
            TDNum num = new TDNum( list, blk.mFrom, null, null, decl, null ); // null formatClosure
            ret = TDExporter.exportSurveyAsDxf( mSid, mData, mInfo, num, mSurvey );
          }
          break;
        case TDConst.DISTOX_EXPORT_GRT: // Grottolf
          pathname = mSurvey + ".grt";
          // TDToast.make( "WARNING Grottolf export is untested" );
          ret = TDExporter.exportSurveyAsGrt( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_GTX: // GHTopo
          pathname = mSurvey + ".gtx";
          // TDToast.make( "WARNING GHTopo export is untested" );
          ret = TDExporter.exportSurveyAsGtx( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_KML: // KML
          pathname = mSurvey + ".kml";
          ret = TDExporter.exportSurveyAsKml( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_JSON: // GeoJSON
          pathname = mSurvey + ".json";
          ret = TDExporter.exportSurveyAsJson( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_SHP: // Shapefile
          pathname = mSurvey + ".shz";
          ret = TDExporter.exportSurveyAsShp( mSid, mData, mInfo, mSurvey );
          // ret = TDExporter.exportSurveyAsShp( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_PLT: // Track file
          pathname = mSurvey + ".plt";
          ret = TDExporter.exportSurveyAsPlt( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_PLG: // Polygon CAVE
          pathname = mSurvey + ".cave";
          ret = TDExporter.exportSurveyAsPlg( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_SRV: // Walls
          pathname = mSurvey + ".srv";
          ret = TDExporter.exportSurveyAsSrv( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_SUR: // WinKarst
          pathname = mSurvey + ".sur";
          // TDToast.make( "WARNING WinKarst export is untested" );
          ret = TDExporter.exportSurveyAsSur( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_SVX: // Survex
          pathname = mSurvey + ".svx";
          ret = TDExporter.exportSurveyAsSvx( mSid, mData, mInfo, mDevice, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_TRO: // VisualTopo
          pathname = mSurvey + ".tro";
          ret = TDExporter.exportSurveyAsTro( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_TRB: // TopoRobot
          pathname = mSurvey + ".trb";
          // TDToast.make( "WARNING TopoRobot export is untested" );
          ret = TDExporter.exportSurveyAsTrb( mSid, mData, mInfo, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_TOP: // PocketTopo
          pathname = mSurvey + ".top";
          ret = TDExporter.exportSurveyAsTop( mSid, mData, mInfo, null, null, mSurvey );
          break;
        case TDConst.DISTOX_EXPORT_TH:
        default:
          pathname = mSurvey + ".th";
          ret = TDExporter.exportSurveyAsTh( mSid, mData, mInfo, mSurvey );
          break;
      }
    }
    return ( ret == 1 )? pathname : "";
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
        TDToast.make( String.format(mFormat, filename) ); 
      }
    }
  }

}

