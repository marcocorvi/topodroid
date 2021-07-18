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
import com.topodroid.utils.TDsafUri;
import com.topodroid.utils.TDStatus;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;

// import java.lang.ref.WeakReference;

import java.util.List;
import android.net.Uri;

import android.os.AsyncTask;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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
  private Uri mUri = null;

  SaveDataFileTask( Uri uri, String format, long sid, SurveyInfo info, DataHelper data, String survey, Device device, int type, boolean toast )
  {
    // mUri     = uri; // FIXME_URI
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
      try {
        switch ( mType ) {
          // case TDConst.SURVEY_FORMAT_TLX:
          //   filename = exportSurveyAsTlx();
          //   break;
          case TDConst.SURVEY_FORMAT_CSX: // cSurvey (only mInfo, no plot-data)
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getCsxFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".csx";
            ret = TDExporter.exportSurveyAsCsx( bw, mSid, mData, mInfo, null, null, null, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_CSV:
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getCsvFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".csv";
            if ( TDSetting.mCsvRaw ) {
              ret = TDExporter.exportSurveyAsRawCsv( bw, mSid, mData, mInfo, mSurvey );
            } else {
              ret = TDExporter.exportSurveyAsCsv( bw, mSid, mData, mInfo, mSurvey );
            }
            break;
          case TDConst.SURVEY_FORMAT_CAV: // Topo
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getCavFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".cav";
            ret = TDExporter.exportSurveyAsCav( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_DAT: // Compass
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getDatFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".dat";
            ret = TDExporter.exportSurveyAsDat( bw, mSid, mData, mInfo, mSurvey );
            // Log.v("DistoX", "save DAT " + ret );
            break;
          case TDConst.SURVEY_FORMAT_DXF:
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getDxfFileWithExt( mSurvey ) ) );
            List< DBlock > list = mData.selectAllShots( mSid, TDStatus.NORMAL );
            if ( list.size() > 0 ) {
              DBlock blk = list.get( 0 );
              // Log.v( TopoDroidApp.TAG, "SURVEY_FORMAT_DXF from " + blk.mFrom );
              // float decl = mData.getSurveyDeclination( mSid );
              // if ( decl >= SurveyInfo.DECLINATION_MAX ) decl = 0;
              float decl = mInfo.getDeclination();
              TDNum num = new TDNum( list, blk.mFrom, null, null, decl, null ); // null formatClosure
              pathname = mSurvey + ".dxf";
              ret = TDExporter.exportSurveyAsDxf( bw, mSid, mData, mInfo, num, mSurvey );
            }
            break;
          case TDConst.SURVEY_FORMAT_GRT: // Grottolf
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getGrtFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".grt";
            // TDToast.make( "WARNING Grottolf export is untested" );
            ret = TDExporter.exportSurveyAsGrt( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_GTX: // GHTopo
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getGtxFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".gtx";
            // TDToast.make( "WARNING GHTopo export is untested" );
            ret = TDExporter.exportSurveyAsGtx( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_KML: // KML
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getKmlFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".kml";
            ret = TDExporter.exportSurveyAsKml( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_JSON: // GeoJSON
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getJsonFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".json";
            ret = TDExporter.exportSurveyAsJson( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_SHP: // Shapefile
            fos = (mUri != null)? TDsafUri.docFileOutputStream( mUri ) : new FileOutputStream( TDPath.getShpFileWithExt( mSurvey ) );
            pathname = mSurvey + ".shz";
            ret = TDExporter.exportSurveyAsShp( fos, mSid, mData, mInfo, mSurvey, TDPath.getShpTempDir() );
            break;
          case TDConst.SURVEY_FORMAT_PLT: // Track file
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getPltFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".plt";
            ret = TDExporter.exportSurveyAsPlt( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_PLG: // Polygon CAVE
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getCaveFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".cave";
            ret = TDExporter.exportSurveyAsPlg( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_SRV: // Walls
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getSrvFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".srv";
            ret = TDExporter.exportSurveyAsSrv( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_SUR: // WinKarst
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getSurFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".sur";
            // TDToast.make( "WARNING WinKarst export is untested" );
            ret = TDExporter.exportSurveyAsSur( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_SVX: // Survex
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getSvxFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".svx";
            ret = TDExporter.exportSurveyAsSvx( bw, mSid, mData, mInfo, mDevice, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_TRO: // VisualTopo
            if ( TDSetting.mVTopoTrox ) {
              bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getTroxFileWithExt( mSurvey ) ) );
              pathname = mSurvey + ".trox";
              ret = TDExporter.exportSurveyAsTrox( bw, mSid, mData, mInfo, mSurvey );
            } else {
              bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getTroFileWithExt( mSurvey ) ) );
              pathname = mSurvey + ".tro";
              ret = TDExporter.exportSurveyAsTro( bw, mSid, mData, mInfo, mSurvey );
            }
            break;
          case TDConst.SURVEY_FORMAT_TRB: // TopoRobot
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getTrbFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".trb";
            // TDToast.make( "WARNING TopoRobot export is untested" );
            ret = TDExporter.exportSurveyAsTrb( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_TOP: // PocketTopo
            fos = (mUri != null)? TDsafUri.docFileOutputStream( mUri ) : new FileOutputStream( TDPath.getTopFileWithExt( mSurvey ) );
            pathname = mSurvey + ".top";
            ret = TDExporter.exportSurveyAsTop( fos, mSid, mData, mInfo, null, null, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_TH: // Therion
          default:
            bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getThFileWithExt( mSurvey ) ) );
            pathname = mSurvey + ".th";
            ret = TDExporter.exportSurveyAsTh( bw, mSid, mData, mInfo, mSurvey );
            break;
        }
      } catch ( IOException e ) { }
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

