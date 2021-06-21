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
import com.topodroid.utils.TDsaf;
import com.topodroid.utils.TDStatus;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;

// import java.lang.ref.WeakReference;

import java.util.List;
import android.net.Uri;

import android.os.AsyncTask;

import android.util.Log;

import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.IOException;

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
  private Uri mUri;

  SaveDataFileTask( Uri uri, String format, long sid, SurveyInfo info, DataHelper data, String survey, Device device, int type, boolean toast )
  {
    mUri     = uri;
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
    BufferedWriter bw;
    OutputStream fos;
    synchronized ( TDFile.mFilesLock ) { // too-big synch
      // try {
        switch ( mType ) {
          // case TDConst.DISTOX_EXPORT_TLX:
          //   filename = exportSurveyAsTlx();
          //   break;
          case TDConst.DISTOX_EXPORT_CSX: // cSurvey (only mInfo, no plot-data)
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".csx";
            ret = TDExporter.exportSurveyAsCsx( bw, mSid, mData, mInfo, null, null, null, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_CSV:
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".csv";
            if ( TDSetting.mCsvRaw ) {
              ret = TDExporter.exportSurveyAsRawCsv( bw, mSid, mData, mInfo, mSurvey );
            } else {
              ret = TDExporter.exportSurveyAsCsv( bw, mSid, mData, mInfo, mSurvey );
            }
            break;
          case TDConst.DISTOX_EXPORT_CAV: // Topo
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".cav";
            ret = TDExporter.exportSurveyAsCav( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_DAT: // Compass
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".dat";
            ret = TDExporter.exportSurveyAsDat( bw, mSid, mData, mInfo, mSurvey );
            // Log.v("DistoX", "save DAT " + ret );
            break;
          case TDConst.DISTOX_EXPORT_DXF:
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            List< DBlock > list = mData.selectAllShots( mSid, TDStatus.NORMAL );
            if ( list.size() > 0 ) {
              DBlock blk = list.get( 0 );
              // Log.v( TopoDroidApp.TAG, "DISTOX_EXPORT_DXF from " + blk.mFrom );
              // float decl = mData.getSurveyDeclination( mSid );
              // if ( decl >= SurveyInfo.DECLINATION_MAX ) decl = 0;
              float decl = mInfo.getDeclination();
              TDNum num = new TDNum( list, blk.mFrom, null, null, decl, null ); // null formatClosure
              pathname = mSurvey + ".dxf";
              ret = TDExporter.exportSurveyAsDxf( bw, mSid, mData, mInfo, num, mSurvey );
            }
            break;
          case TDConst.DISTOX_EXPORT_GRT: // Grottolf
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".grt";
            // TDToast.make( "WARNING Grottolf export is untested" );
            ret = TDExporter.exportSurveyAsGrt( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_GTX: // GHTopo
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".gtx";
            // TDToast.make( "WARNING GHTopo export is untested" );
            ret = TDExporter.exportSurveyAsGtx( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_KML: // KML
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".kml";
            ret = TDExporter.exportSurveyAsKml( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_JSON: // GeoJSON
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".json";
            ret = TDExporter.exportSurveyAsJson( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_SHP: // Shapefile
            fos = TDsaf.docFileOutputStream( mUri );
            pathname = mSurvey + ".shz";
            ret = TDExporter.exportSurveyAsShp( fos, mSid, mData, mInfo, mSurvey );
            // ret = TDExporter.exportSurveyAsShp( mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_PLT: // Track file
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".plt";
            ret = TDExporter.exportSurveyAsPlt( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_PLG: // Polygon CAVE
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".cave";
            ret = TDExporter.exportSurveyAsPlg( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_SRV: // Walls
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".srv";
            ret = TDExporter.exportSurveyAsSrv( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_SUR: // WinKarst
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".sur";
            // TDToast.make( "WARNING WinKarst export is untested" );
            ret = TDExporter.exportSurveyAsSur( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_SVX: // Survex
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".svx";
            ret = TDExporter.exportSurveyAsSvx( bw, mSid, mData, mInfo, mDevice, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_TRO: // VisualTopo
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".tro";
            ret = TDExporter.exportSurveyAsTro( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_TRB: // TopoRobot
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".trb";
            // TDToast.make( "WARNING TopoRobot export is untested" );
            ret = TDExporter.exportSurveyAsTrb( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_TOP: // PocketTopo
            fos = TDsaf.docFileOutputStream( mUri );
            pathname = mSurvey + ".top";
            ret = TDExporter.exportSurveyAsTop( fos, mSid, mData, mInfo, null, null, mSurvey );
            break;
          case TDConst.DISTOX_EXPORT_TH:
          default:
            bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
            pathname = mSurvey + ".th";
            ret = TDExporter.exportSurveyAsTh( bw, mSid, mData, mInfo, mSurvey );
            break;
        }
      // } catch ( IOException e ) { }
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

