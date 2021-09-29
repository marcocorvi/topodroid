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

import com.topodroid.DistoX.R;
// import com.topodroid.DistoX.R;

import com.topodroid.DistoX.ModelType;
import com.topodroid.DistoX.TglParser;
import com.topodroid.DistoX.TopoGL;
import com.topodroid.DistoX.Cave3DFile;

import com.topodroid.utils.TDLog;

import android.os.AsyncTask;
import android.content.Context;

import android.net.Uri;

import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExportTask extends AsyncTask< Void, Void, Boolean >
{
  private TopoGL mApp;
  private TglParser mParser;
  private Uri mUri;
  private ExportData mExport;

  public ExportTask( TopoGL app, TglParser parser, Uri uri, String surveyname, ExportData export )
  {
    mApp    = app;
    mParser = parser;
    mUri    = uri;
    mExport = new ExportData( surveyname, export );
  }

  @Override
  public Boolean doInBackground( Void ... args )
  {
    String pathname = Cave3DFile.getFilepath( mExport.mName );
    if ( mExport.mType == ModelType.GLTF ) { 
      // TDLog.v( "export model GLTF " + pathname );
      return mApp.exportGltfModel( mExport.mType, pathname + ".gltf", mExport );
    } else if ( mExport.mType == ModelType.SHP_ASCII ) { // SHP export is only with its file and folder
      // TDLog.v( "export model SHP " + pathname );
      return mParser.exportModelAscii( mExport.mType, null, mExport );
    }
    
    OutputStreamWriter osw = null;
    DataOutputStream dos = null;
    try {
      // TDLog.v( "try export model type " + mExport.mType + " " + pathname );
      switch ( mExport.mType ) {
        case ModelType.STL_BINARY:
          dos = new DataOutputStream( (mUri != null)? mApp.getContentResolver().openOutputStream( mUri ) : new FileOutputStream( pathname + ".stl" ) );
          break;
        case ModelType.STL_ASCII:
          osw = new OutputStreamWriter( (mUri != null)? mApp.getContentResolver().openOutputStream( mUri ) : new FileOutputStream( pathname + ".stl") );
          break;
        case ModelType.KML_ASCII:
          osw = new OutputStreamWriter( (mUri != null)? mApp.getContentResolver().openOutputStream( mUri ) : new FileOutputStream( pathname + ".kml") );
          break;
        case ModelType.CGAL_ASCII:
          osw = new OutputStreamWriter( (mUri != null)? mApp.getContentResolver().openOutputStream( mUri ) : new FileOutputStream( pathname + ".cgal") );
          break;
        case ModelType.LAS_BINARY:
          dos = new DataOutputStream( (mUri != null)? mApp.getContentResolver().openOutputStream( mUri ) : new FileOutputStream( pathname + ".las" ) );
          break;
        case ModelType.DXF_ASCII:
          osw = new OutputStreamWriter( (mUri != null)? mApp.getContentResolver().openOutputStream( mUri ) : new FileOutputStream( pathname + ".dxf") );
          break;
        case ModelType.SERIAL:
          osw = new OutputStreamWriter( (mUri != null)? mApp.getContentResolver().openOutputStream( mUri ) : new FileOutputStream( pathname + ".kml") );
          break;
        default:
          break;
      }
      if ( dos != null ) {
        return mParser.exportModelBinary( mExport.mType, dos, mExport );
      } else if ( osw != null ) {
        return mParser.exportModelAscii( mExport.mType, osw, mExport );
      }
    } catch ( OutOfMemoryError e ) {
      TDLog.Error("Export task: Out of memory error" );
    } catch ( IOException e ) {
     TDLog.Error("IO error " + e.getMessage() );
    }
    return false;
  }

   @Override
  protected void onPostExecute( Boolean res )
  {
    if ( mApp != null ) { // CRASH here - this should not be necessary
      if ( res ) {
        mApp.toast(R.string.ok_export, mExport.mExt, false);
      } else {
        mApp.toast(R.string.error_export_failed, mExport.mExt, true );
      }
    }
  }

}
