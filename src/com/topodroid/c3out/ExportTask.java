/** @file ExportTask.java
 *
 * @author marco corvi
 * @date may 2021
 *
 * @brief export model task
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.TDX.R;

import com.topodroid.TDX.ModelType;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.TopoGL;
// import com.topodroid.TDX.Cave3DFile;
import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDToast;

import com.topodroid.prefs.TDSetting;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;

import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
// import android.content.Context;

import android.net.Uri;

import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class ExportTask extends AsyncTask< Void, Void, Boolean >
{
  private TopoGL mApp; // FIXME use TDFile ParcelFileDescriptor
  private TglParser mParser;
  private Uri mUri = null;
  private ExportData mExport;

  /** cstr
   * @param app     3D viewer
   * @param parser  data parser
   * @param uri     output uri
   * @param export  export flags
   */
  public ExportTask( TopoGL app, TglParser parser, Uri uri, ExportData export )
  {
    mApp    = app;
    mParser = parser;
    /* if ( TDSetting.mExportUri ) */ mUri = uri; // FIXME_URI
    mExport = new ExportData( export.mName, export ); // save a copy
  }

  @Override
  public Boolean doInBackground( Void ... args )
  {
    BufferedWriter bw = null;
    DataOutputStream dos = null;

    String pathname = TDPath.getC3exportPath( mExport.mName ); // .../TDX/TopoDroid/c3export/name
    TDFile.makeTopoDroidDir( pathname );

    TDLog.v("export task. name " + mExport.mName + " type " + mExport.mType + " pathname " + pathname );

    ParcelFileDescriptor pfd = TDsafUri.docWriteFileDescriptor( mUri );
    if ( pfd == null ) return false;
    boolean ret = false;
    try {
      switch ( mExport.mType ) {
        case ModelType.GLTF:
          // dos = new DataOutputStream( (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : new FileOutputStream( pathname + ".glz" ) );
          dos = new DataOutputStream( TDsafUri.docFileOutputStream( pfd ) );
          ret = mApp.exportGltfModel( mExport.mType, dos, pathname, mExport );
          break;
        case ModelType.SHP_ASCII: // SHP export is only with its file and folder
          // dos = new DataOutputStream( (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : new FileOutputStream( pathname + ".shz" ) );
          dos = new DataOutputStream( TDsafUri.docFileOutputStream( pfd ) );
          ret = mApp.exportShpModel( mExport.mType, dos, pathname, mExport );
          break;
        case ModelType.STL_BINARY:
          // dos = new DataOutputStream( (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : new FileOutputStream( pathname + ".stl" ) );
          dos = new DataOutputStream( TDsafUri.docFileOutputStream( pfd ) );
          ret = mParser.exportModelBinary( mExport.mType, dos, mExport );
          break;
        case ModelType.STL_ASCII:
          // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( pathname + ".stl" ) );
          bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        case ModelType.KML_ASCII:
          // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( pathname + ".kml" ) );
          bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        case ModelType.CGAL_ASCII:
          // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( pathname + ".cgal" ) );
          bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        case ModelType.LAS_BINARY:
          // dos = new DataOutputStream( (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : new FileOutputStream( pathname + ".las" ) );
          dos = new DataOutputStream( TDsafUri.docFileOutputStream( pfd ) );
          ret = mParser.exportModelBinary( mExport.mType, dos, mExport );
          break;
        case ModelType.DXF_ASCII:
          // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( pathname + ".dxf" ) );
          bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        case ModelType.SERIAL:
          // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( pathname + ".txt" ) );
          bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        default:
          break;
      }
      if ( bw != null ) bw.close();
      if ( dos != null ) dos.close();
    } catch ( OutOfMemoryError e ) {
      TDLog.Error("Export task: Out of memory error" );
    } catch ( IOException e ) {
      TDLog.Error("IO error " + e.getMessage() );
    } finally {
      TDsafUri.closeFileDescriptor( pfd );
    }
    return ret;
  }

   @Override
  protected void onPostExecute( Boolean res )
  {
    if ( res ) {
      TDToast.make( TDInstance.formatString( R.string.ok_export, mExport.mExt ) );
    } else {
      TDToast.makeBad( TDInstance.formatString( R.string.error_export_failed, mExport.mExt ) );
    }
  }

}
