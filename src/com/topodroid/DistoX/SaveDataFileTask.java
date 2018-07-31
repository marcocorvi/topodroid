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

import java.io.File;

import java.util.List;

import android.content.Intent;
import android.content.Context;

import android.os.AsyncTask;
// import android.os.Bundle;
import android.os.Handler;

import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;

// import android.util.Log;

class SaveDataFileTask extends AsyncTask<Void, Void, String >
{
  private final Context mContext; // FIXME LEAK
  // private TopoDroidApp mApp;
  private long  mSid;
  private DataHelper mData;
  private SurveyInfo mInfo;
  private String mSurvey;
  private Device mDevice;
  private int mType;    // export type
  private boolean mToast;

  SaveDataFileTask( Context context, long sid, SurveyInfo info, DataHelper data, String survey, Device device, int type, boolean toast )
  {
     mContext = context;
     mSid     = sid;
     mInfo    = info;
     mData    = data;
     mSurvey  = survey;
     mDevice  = device;
     mType    = (int)type;
     mToast   = toast;
  }

  @Override
  protected String doInBackground(Void... arg0)
  {
    if ( mInfo == null ) return null;
    // boolean do_binary = (TDSetting.mBinaryTh2 && mSuffix != DataSave.EXPORT ); // TDR BINARY
    String filename = null;

    synchronized( TDPath.mTherionLock ) {
      switch ( mType ) {
        // case TDConst.DISTOX_EXPORT_TLX:
        //   filename = exportSurveyAsTlx();
        //   break;
        case TDConst.DISTOX_EXPORT_DAT:
          filename = TDExporter.exportSurveyAsDat( mSid, mData, mInfo, TDPath.getSurveyDatFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_SVX:
          filename = TDExporter.exportSurveyAsSvx( mSid, mData, mInfo, mDevice, TDPath.getSurveySvxFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_TRO:
          filename = TDExporter.exportSurveyAsTro( mSid, mData, mInfo, TDPath.getSurveyTroFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_CSV:
          filename = TDExporter.exportSurveyAsCsv( mSid, mData, mInfo, TDPath.getSurveyCsvFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_DXF:
          List<DBlock> list = mData.selectAllShots( mSid, TDStatus.NORMAL );
          if ( list.size() > 0 ) {
            DBlock blk = list.get( 0 );
            // Log.v( TopoDroidApp.TAG, "DISTOX_EXPORT_DXF from " + blk.mFrom );
            float decl = mData.getSurveyDeclination( mSid );
            DistoXNum num = new DistoXNum( list, blk.mFrom, null, null, decl );
            filename = TDExporter.exportSurveyAsDxf( mSid, mData, mInfo, num, TDPath.getSurveyDxfFile( mSurvey ) );
          }
          break;
        case TDConst.DISTOX_EXPORT_KML: // KML
          filename = TDExporter.exportSurveyAsKml( mSid, mData, mInfo, TDPath.getSurveyKmlFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_JSON: // GeoJSON
          filename = TDExporter.exportSurveyAsJson( mSid, mData, mInfo, TDPath.getSurveyJsonFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_PLT: // Track file
          filename = TDExporter.exportSurveyAsPlt( mSid, mData, mInfo, TDPath.getSurveyPltFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_CSX: // cSurvey (only mInfo, no plot-data)
          filename = TDExporter.exportSurveyAsCsx( mSid, mData, mInfo, null, null, null, TDPath.getSurveyCsxFile( mSurvey ));
          break;
        case TDConst.DISTOX_EXPORT_TOP: // PocketTopo
          filename = TDExporter.exportSurveyAsTop( mSid, mData, mInfo, null, null, TDPath.getSurveyTopFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_SRV: // Walls
          filename = TDExporter.exportSurveyAsSrv( mSid, mData, mInfo, TDPath.getSurveySrvFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_PLG: // Polygon
          filename = TDExporter.exportSurveyAsPlg( mSid, mData, mInfo, TDPath.getSurveyCaveFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_CAV: // Topo
          filename = TDExporter.exportSurveyAsCav( mSid, mData, mInfo, TDPath.getSurveyCavFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_GRT: // Grottolf
          // TDToast.make( this, "WARNING Grottolf export is untested" );
          filename = TDExporter.exportSurveyAsGrt( mSid, mData, mInfo, TDPath.getSurveyGrtFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_GTX: // GHTopo
          // TDToast.make( this, "WARNING GHTopo export is untested" );
          filename = TDExporter.exportSurveyAsGtx( mSid, mData, mInfo, TDPath.getSurveyGtxFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_SUR: // WinKarst
          // TDToast.make( this, "WARNING WinKarst export is untested" );
          filename = TDExporter.exportSurveyAsSur( mSid, mData, mInfo, TDPath.getSurveySurFile( mSurvey ) );
          break;
        case TDConst.DISTOX_EXPORT_TRB: // TopoRobot
          // TDToast.make( this, "WARNING TopoRobot export is untested" );
          filename = TDExporter.exportSurveyAsTrb( mSid, mData, mInfo, TDPath.getSurveyTrbFile( mSurvey ) );
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
        TDToast.make( mContext, R.string.saving_file_failed );
      } else if ( filename.length() == 0 ) {
        TDToast.make( mContext, R.string.no_geo_station );
      } else {
        TDToast.make( mContext, mContext.getResources().getString(R.string.saving_) + filename );
      }
    }
  }

}

