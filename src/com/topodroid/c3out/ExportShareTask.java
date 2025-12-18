/** @file ExportShareTask.java
 *
 * @author claude
 * @date dec 2024
 *
 * @brief export model and share task
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.TDX.R;

import com.topodroid.TDX.ModelType;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.TopoGL;
import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDToast;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.MyFileProvider;

import android.os.AsyncTask;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * AsyncTask to export 3D model to a file and share it
 */
public class ExportShareTask extends AsyncTask< Void, Void, Boolean >
{
  private TopoGL mApp;
  private TglParser mParser;
  private ExportData mExport;
  private String mFilePath = null;

  /** cstr
   * @param app     3D viewer
   * @param parser  data parser
   * @param export  export flags
   */
  public ExportShareTask( TopoGL app, TglParser parser, ExportData export )
  {
    mApp    = app;
    mParser = parser;
    mExport = new ExportData( export.mName, export );
    mExport.debug();
  }

  @Override
  public Boolean doInBackground( Void ... args )
  {
    BufferedWriter bw = null;
    DataOutputStream dos = null;

    String pathname = TDPath.getC3exportPath( mExport.mName );
    TDFile.makeTopoDroidDir( pathname );

    String ext = getFileExtension( mExport.mType );
    mFilePath = pathname + "/" + mExport.mName + ext;

    TDLog.v("SHARE export to file: " + mFilePath);

    boolean ret = false;
    try {
      switch ( mExport.mType ) {
        case ModelType.GLTF:
          dos = new DataOutputStream( new FileOutputStream( mFilePath ) );
          ret = mApp.exportGltfModel( mExport.mType, dos, pathname, mExport );
          break;
        case ModelType.SHP_ASCII:
          dos = new DataOutputStream( new FileOutputStream( mFilePath ) );
          ret = mApp.exportShpModel( mExport.mType, dos, pathname, mExport );
          break;
        case ModelType.STL_BINARY:
          dos = new DataOutputStream( new FileOutputStream( mFilePath ) );
          ret = mParser.exportModelBinary( mExport.mType, dos, mExport );
          break;
        case ModelType.STL_ASCII:
          bw = new BufferedWriter( new FileWriter( mFilePath ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        case ModelType.KML_ASCII:
          bw = new BufferedWriter( new FileWriter( mFilePath ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        case ModelType.CGAL_ASCII:
          bw = new BufferedWriter( new FileWriter( mFilePath ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        case ModelType.LAS_BINARY:
          dos = new DataOutputStream( new FileOutputStream( mFilePath ) );
          ret = mParser.exportModelBinary( mExport.mType, dos, mExport );
          break;
        case ModelType.DXF_ASCII:
          bw = new BufferedWriter( new FileWriter( mFilePath ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        case ModelType.GPX_ASCII:
          bw = new BufferedWriter( new FileWriter( mFilePath ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        case ModelType.SERIAL:
          bw = new BufferedWriter( new FileWriter( mFilePath ) );
          ret = mParser.exportModelAscii( mExport.mType, bw, mExport );
          break;
        default:
          break;
      }
      if ( bw != null ) bw.close();
      if ( dos != null ) dos.close();
    } catch ( OutOfMemoryError e ) {
      TDLog.e("Export share task: Out of memory error" );
    } catch ( IOException e ) {
      TDLog.e("IO error " + e.getMessage() );
    }
    return ret;
  }

  @Override
  protected void onPostExecute( Boolean res )
  {
    if ( res && mFilePath != null ) {
      shareFile();
    } else {
      TDToast.makeBad( TDInstance.formatString( R.string.error_export_failed, mExport.mExt ) );
    }
  }

  /**
   * Share the exported file using Android share intent
   */
  private void shareFile()
  {
    File file = new File( mFilePath );
    if ( !file.exists() ) {
      TDToast.makeBad( R.string.error_export_failed );
      return;
    }

    Uri uri = MyFileProvider.fileToUri( mApp, file );
    Intent intent = new Intent();
    intent.setAction( Intent.ACTION_SEND );
    intent.putExtra( Intent.EXTRA_STREAM, uri );
    intent.setType( getMimeType() );
    intent.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );

    try {
      Intent chooser = Intent.createChooser( intent,
          mApp.getResources().getString( R.string.export_model_title ) );
      chooser.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
      mApp.startActivity( chooser );
    } catch ( ActivityNotFoundException e ) {
      TDLog.e( "SHARE failed: " + e.getMessage() );
      TDToast.makeBad( R.string.file_share_no_app );
    }
  }

  /**
   * Get the MIME type for the export format
   */
  private String getMimeType()
  {
    switch ( mExport.mType ) {
      case ModelType.GLTF:       return "model/gltf-binary";
      case ModelType.STL_ASCII:
      case ModelType.STL_BINARY: return "application/sla";
      case ModelType.KML_ASCII:  return "application/vnd.google-earth.kml+xml";
      case ModelType.DXF_ASCII:  return "application/dxf";
      case ModelType.GPX_ASCII:  return "application/gpx+xml";
      case ModelType.SHP_ASCII:  return "application/zip";
      default:                   return "application/octet-stream";
    }
  }

  /**
   * Get file extension for the export format
   */
  private String getFileExtension( int type )
  {
    switch ( type ) {
      case ModelType.GLTF:       return ".gltf";
      case ModelType.CGAL_ASCII: return ".cgal";
      case ModelType.STL_ASCII:
      case ModelType.STL_BINARY: return ".stl";
      case ModelType.LAS_BINARY: return ".las";
      case ModelType.DXF_ASCII:  return ".dxf";
      case ModelType.KML_ASCII:  return ".kml";
      case ModelType.SHP_ASCII:  return ".shz";
      case ModelType.GPX_ASCII:  return ".gpx";
      case ModelType.SERIAL:     return ".txt";
      default:                   return ".bin";
    }
  }
}
