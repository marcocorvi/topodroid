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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
import com.topodroid.utils.TDStatus;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;
import com.topodroid.common.ExportInfo;

// import java.lang.ref.WeakReference;

import java.util.List;
import android.net.Uri;

import android.os.ParcelFileDescriptor;
import android.os.AsyncTask;

import java.io.FileOutputStream;
// import java.io.OutputStreamWriter;
// import java.io.FileWriter;
import java.io.BufferedWriter;
// import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.charset.Charset;

class SaveDataFileTask extends AsyncTask<Void, Void, String >
{
  private String mFormat;   // for the toast
  private long  mSid;
  private DataHelper mData;
  private SurveyInfo mInfo;
  private String mSurvey;
  private Device mDevice;   // only for SVX
  private ExportInfo mExportInfo;
  private String mSurveyName; // export survey name - TopoRobot
  private boolean mToast;
  private Uri mUri = null;

  /**
   * @param uri         output URI
   * @param format      format for the toast
   * @param sid         survey ID
   * @param info        survey info
   * @param data        DB helper class
   * @param survey      survey name
   * @param device      active device (A) - only for SVX
   * @param export_info export info (type, prefix, name ...)
   * @param toast       whether to toast
   */
  SaveDataFileTask( Uri uri, String format, long sid, SurveyInfo info, DataHelper data, String survey, Device device, ExportInfo export_info, boolean toast )
  {
    /* if ( TDSetting.mExportUri ) */ mUri = uri; // FIXME_URI
    mFormat  = format;
    mSid     = sid;
    mInfo    = info.copy();
    mData    = data;
    mSurvey  = survey;
    mDevice  = device;
    mExportInfo = export_info;
    mToast   = toast;
    setSurveyName();
    // TDLog.v( "save data file task - type " + export_info.index + " name " + export_info.name );
  }

  /** set the survey-name
   */
  private void setSurveyName()
  {
    if ( mExportInfo.name != null ) { // only for TopoRobot
      int pos = mExportInfo.name.lastIndexOf('.');
      mSurveyName = ( pos > 0 )? mExportInfo.name.substring( 0, pos ) : mExportInfo.name;
    } else {
      mSurveyName = mSurvey;
    }
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
    // TDLog.v( "save data file task - execute");
    int ret = 0;
    String pathname   = null;
    BufferedWriter bw = null;
    FileOutputStream fos  = null;
    ParcelFileDescriptor pfd = TDsafUri.docWriteFileDescriptor( mUri ); // mUri null handled by TDsafUri
    if ( pfd == null ) {
      TDLog.e( "save data file task - null fd");
      return null;
    }
    synchronized ( TDFile.mFilesLock ) { // too-big synch
      try {
        switch ( mExportInfo.index ) {
          // case TDConst.SURVEY_FORMAT_TLX:
          //   filename = exportSurveyAsTlx();
          //   break;
          case TDConst.SURVEY_FORMAT_CSX: // cSurvey (only mInfo, no plot-data)
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getCsxFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.CSX;
            ret = TDExporter.exportSurveyAsCsx( bw, mSid, mData, mInfo, null, null, null, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_CSV:
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getCsvFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.CSV;
            if ( TDSetting.mCsvRaw ) {
              ret = TDExporter.exportSurveyAsRawCsv( bw, mSid, mData, mInfo, mSurvey );
            } else {
              ret = TDExporter.exportSurveyAsCsv( bw, mSid, mData, mInfo, mSurvey );
            }
            break;
          case TDConst.SURVEY_FORMAT_CAV: // Topo
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getCavFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.CAV;
            ret = TDExporter.exportSurveyAsCav( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_DAT: // Compass
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getDatFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.DAT;
            ret = TDExporter.exportSurveyAsDat( bw, mSid, mData, mInfo, mSurvey, mExportInfo.prefix );
            // TDLog.v( "save DAT " + ret );
            break;
          case TDConst.SURVEY_FORMAT_DXF:
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getDxfFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            List< DBlock > list = mData.selectAllShots( mSid, TDStatus.NORMAL );
            if ( list.size() > 0 ) {
              DBlock blk = list.get( 0 );
              // TDLog.v( "SURVEY_FORMAT_DXF from " + blk.mFrom );
              // float decl = mData.getSurveyDeclination( mSid );
              // if ( decl >= SurveyInfo.DECLINATION_MAX ) decl = 0;
              float decl = mInfo.getDeclination();
              TDNum num = new TDNum( list, blk.mFrom, decl, null ); // null formatClosure
              pathname = mSurvey + TDPath.DXF;
              ret = TDExporter.exportSurveyAsDxf( bw, mSid, mData, mInfo, num, mSurvey );
            }
            break;
          // case TDConst.SURVEY_FORMAT_GRT: // Grottolf
          //   // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getGrtFileWithExt( mSurvey ) ) );
          //   bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
          //   pathname = mSurvey + ".grt";
          //   // TDToast.make( "WARNING Grottolf export is untested" );
          //   ret = TDExporter.exportSurveyAsGrt( bw, mSid, mData, mInfo, mSurvey );
          //   break;
          // case TDConst.SURVEY_FORMAT_GTX: // GHTopo
          //   // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getGtxFileWithExt( mSurvey ) ) );
          //   bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
          //   pathname = mSurvey + TDPath.GTX;
          //   // TDToast.make( "WARNING GHTopo export is untested" );
          //   ret = TDExporter.exportSurveyAsGtx( bw, mSid, mData, mInfo, mSurvey );
          //   break;
          case TDConst.SURVEY_FORMAT_KML: // KML
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getKmlFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.KML;
            ret = TDExporter.exportSurveyAsKml( bw, mSid, mData, mInfo, mSurvey );
            break;
          // case TDConst.SURVEY_FORMAT_JSON: // GeoJSON
          //   // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getJsonFileWithExt( mSurvey ) ) );
          //   bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
          //   pathname = mSurvey + ".json";
          //   ret = TDExporter.exportSurveyAsJson( bw, mSid, mData, mInfo, mSurvey );
          //   break;
          case TDConst.SURVEY_FORMAT_SHP: // Shapefile
            // fos = (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : new FileOutputStream( TDPath.getShpFileWithExt( mSurvey ) );
            fos = TDsafUri.docFileOutputStream( pfd );
            pathname = mSurvey + TDPath.SHZ;
            ret = TDExporter.exportSurveyAsShp( fos, mSid, mData, mInfo, mSurvey, TDPath.getShpTempRelativeDir() );
            break;
          // case TDConst.SURVEY_FORMAT_PLT: // Track file PLT
          //   // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getPltFileWithExt( mSurvey ) ) );
          //   bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
          //   pathname = mSurvey + ".plt";
          //   ret = TDExporter.exportSurveyAsPlt( bw, mSid, mData, mInfo, mSurvey );
          //   break;
          case TDConst.SURVEY_FORMAT_GPX: // Track file GPX
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getPltFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.GPX;
            ret = TDExporter.exportSurveyAsGpx( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_PLG: // Polygon CAVE
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getCaveFileWithExt( mSurvey ) ) );
            // bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            bw = new BufferedWriter( TDsafUri.docOutputStreamWriter( pfd, Charset.forName( "ISO-8859-2" ) ) );
            pathname = mSurvey + TDPath.CAVE;
            ret = TDExporter.exportSurveyAsPlg( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_SRV: // Walls
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getSrvFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.SRV;
            ret = TDExporter.exportSurveyAsSrv( bw, mSid, mData, mInfo, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_SUR: // WinKarst
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getSurFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.SUR;
            // TDToast.make( "WARNING WinKarst export is untested" );
            ret = TDExporter.exportSurveyAsSur( bw, mSid, mData, mInfo, mSurvey, mExportInfo.prefix );
            break;
          case TDConst.SURVEY_FORMAT_SVX: // Survex
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getSvxFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.SVX;
            ret = TDExporter.exportSurveyAsSvx( bw, mSid, mData, mInfo, mDevice, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_TRO: // VisualTopo
            if ( TDSetting.mVTopoTrox ) {
              // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getTroxFileWithExt( mSurvey ) ) );
              bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
              pathname = mSurvey + TDPath.TROX;
              ret = TDExporter.exportSurveyAsTrox( bw, mSid, mData, mInfo, mSurvey, mExportInfo.prefix );
            } else {
              // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getTroFileWithExt( mSurvey ) ) );
              bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
              pathname = mSurvey + TDPath.TRO;
              ret = TDExporter.exportSurveyAsTro( bw, mSid, mData, mInfo, mSurvey, mExportInfo.prefix );
            }
            break;
          case TDConst.SURVEY_FORMAT_TRB: // TopoRobot
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getTrbFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = (mExportInfo.name != null )? mExportInfo.name : mSurvey + TDPath.TRB;
            // TDToast.make( "WARNING TopoRobot export is untested" );
            ret = TDExporter.exportSurveyAsTrb( bw, mSid, mData, mInfo, mSurveyName, mExportInfo.first );
            break;
          case TDConst.SURVEY_FORMAT_TOP: // PocketTopo
            // fos = (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : new FileOutputStream( TDPath.getTopFileWithExt( mSurvey ) );
            fos = TDsafUri.docFileOutputStream( pfd );
            pathname = mSurvey + TDPath.TOP;
            ret = TDExporter.exportSurveyAsTop( fos, mSid, mData, mInfo, null, null, mSurvey );
            break;
          case TDConst.SURVEY_FORMAT_TH: // Therion
          default:
            // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getThFileWithExt( mSurvey ) ) );
            bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
            pathname = mSurvey + TDPath.TH;
            ret = TDExporter.exportSurveyAsTh( bw, mSid, mData, mInfo, mSurvey );
            break;
        }
        if ( bw != null ) bw.close();
        if ( fos != null ) fos.close();
      } catch ( IOException e ) { 
        TDLog.e("IO error " + e.getMessage() );
      } finally {
        TDsafUri.closeFileDescriptor( pfd );
      }
    }
    return ( ret == 1 )? pathname : "";
  }

  /**
   * @param filename   survey_name + extension
   */
  @Override
  protected void onPostExecute( String filename )
  {
    // TDLog.v( "save data file task post exec - filename " + filename );
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

