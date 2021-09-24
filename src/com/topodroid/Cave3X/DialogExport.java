/* @file DialogExport.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D drawing infos dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

import com.topodroid.utils.TDLog;
// import com.topodroid.c3out.ExportTask;
import com.topodroid.c3out.ExportData;
import com.topodroid.ui.MyDialog;

import java.io.File;
import java.io.FileFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;

public class DialogExport extends MyDialog 
                          implements View.OnClickListener
                          // , AdapterView.OnItemClickListener
{
  private Button mBtnOk;

  private TopoGL  mApp;
  private TglParser mParser;

  // private EditText mETfilename;
  private Button   mButtonOK;
  private CheckBox mStlBinary;
  private CheckBox mStlAscii;
  private CheckBox mKmlAscii;
  private CheckBox mCgalAscii;
  private CheckBox mLasBinary;
  private CheckBox mDxfAscii;
  private CheckBox mShpAscii;
  private CheckBox mGltf;

  private CheckBox mSplay;
  private CheckBox mWalls;
  private CheckBox mSurface;
  private CheckBox mStation;
  // private CheckBox mOverwrite;

  // private RadioButton mDebug;
  // private String   mDirname;
  // private TextView mTVdir;

  // private ListView mList;
  // private ArrayAdapter<String> mArrayAdapter;

  public DialogExport( Context context, TopoGL app, TglParser parser )
  {
    super( context, R.string.DialogExport );
    mApp      = app;
    mParser   = parser;
    // mDirname  = Cave3DFile.mAppBasePath;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.cave3d_export_dialog, R.string.EXPORT );

    // mTVdir      = (TextView) findViewById( R.id.dirname );
    // mETfilename = (EditText) findViewById( R.id.filename );
    // String name = mParser.getName();
    // if ( name != null ) mETfilename.setText( name );

    // mList       = (ListView) findViewById( R.id.list );
    // mList.setOnItemClickListener( this );
    // mList.setDividerHeight( 2 );
    // mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );
    // mList.setAdapter( mArrayAdapter );

    mButtonOK = (Button) findViewById( R.id.button_ok );
    // mTVdir.setOnClickListener( this );
    mButtonOK.setOnClickListener( this );

    mStlBinary = (CheckBox) findViewById( R.id.stl_binary );
    mStlAscii  = (CheckBox) findViewById( R.id.stl_ascii );
    mKmlAscii  = (CheckBox) findViewById( R.id.kml_ascii );
    mCgalAscii = (CheckBox) findViewById( R.id.cgal_ascii );
    mLasBinary = (CheckBox) findViewById( R.id.las_binary );
    mDxfAscii  = (CheckBox) findViewById( R.id.dxf_ascii );
    mShpAscii  = (CheckBox) findViewById( R.id.shp_ascii );
    mGltf      = (CheckBox) findViewById( R.id.gltf );
    // mDebug  = (RadioButton) findViewById( R.id.debug );

    mStlBinary.setOnClickListener( this );
    mStlAscii.setOnClickListener( this );
    mKmlAscii.setOnClickListener( this );
    mCgalAscii.setOnClickListener( this );
    mLasBinary.setOnClickListener( this );
    mDxfAscii.setOnClickListener( this );
    mShpAscii.setOnClickListener( this );
    mGltf.setOnClickListener( this );
    // mDebug.setOnClickListener( this );

    mGltf.setChecked( true );

    mSplay   = (CheckBox) findViewById( R.id.splay );
    mWalls   = (CheckBox) findViewById( R.id.walls );
    mSurface = (CheckBox) findViewById( R.id.surface );
    mStation = (CheckBox) findViewById( R.id.station );
    // mOverwrite = (CheckBox) findViewById( R.id.overwrite );

    mSplay.setChecked( true );
    mStation.setChecked( true );

    // mSplay.setVisibility( View.GONE );
    // mWalls.setVisibility( View.GONE );
    // mSurface.setVisibility( View.GONE );

    // updateList( mDirname );
  }

  // private void updateList( String dirname )
  // {
  //   int len = dirname.length();
  //   while ( len > 1 && dirname.charAt(len-1) == '/' ) {
  //     dirname = dirname.substring( 0, len-1 );
  //     -- len;
  //   }
  //   // TDLog.v( "TopoGL DIR <" + dirname + ">" );
  //   mDirname = dirname;
  //   len = dirname.length();
  //   if ( len > 30 ) {
  //     dirname = "..." + dirname.substring( len-30 );
  //   }
  //   mTVdir.setText( dirname );
  //   mArrayAdapter.clear();
  //   File dir = new File( mDirname );
  //   File[] files = dir.listFiles( new FileFilter() { 
  //     public boolean accept( File pathname ) {
  //       if ( pathname.getName().startsWith(".") ) return false;
  //       return true;
  //     } } );
  //   if ( files == null || files.length == 0 ) {
  //     if ( mApp != null ) mApp.uiToast( R.string.no_files, false );
  //     return;
  //   }
  //   ArrayList<String> dirs  = new ArrayList<String>();
  //   ArrayList<String> names = new ArrayList<String>();
  //   for ( File f : files ) {
  //     if ( f.isDirectory() ) {
  //       // if ( f.getName().equals( "." ) continue;
  //       dirs.add( new String( f.getName() ) );
  //     }
  //   }
  //   for ( File f : files ) {
  //     if ( ! f.isDirectory() ) {
  //       names.add( new String( f.getName() ) );
  //     }
  //   }
  // 
  //   if ( dirs.size() > 0 ) { // sort files by name (alphabetical order)
  //     dirs.sort( String.CASE_INSENSITIVE_ORDER );
  //     for ( int k=0; k<dirs.size(); ++k ) mArrayAdapter.add( dirs.get(k) + " /" );
  //   }
  //   if ( names.size() > 0 ) { // sort files by name (alphabetical order)
  //     names.sort( String.CASE_INSENSITIVE_ORDER );
  //     for ( int k=0; k<names.size(); ++k ) mArrayAdapter.add( names.get(k) );
  //   }
  //   mList.setAdapter( mArrayAdapter );
  //   mList.invalidate();
  // }

  // @Override
  // public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  // {
  //   String item = ((TextView) view).getText().toString().trim();
  //   String[] vals = item.split(" ");
  //   if ( vals.length == 1 ) {
  //     mETfilename.setText( vals[0] );
  //   } else {
  //     updateList( mDirname + "/" + vals[0] );
  //   }
  // }

  @Override
  public void onClick(View v)
  {
    // TDLog.v( "TopoGL onClick()" );
    if ( v.getId() == R.id.button_ok ) {
      // String filename = mETfilename.getText().toString();
      // if ( filename == null || filename.length() == 0 ) {
      //   mETfilename.setError( "no filename" );
      //   return;
      // }
      // String pathname = mDirname + "/" + filename;
      ExportData export = new ExportData( "todo", // surveyname
        mSplay.isChecked(),
        mWalls.isChecked(),
        mSurface.isChecked(),
        mStation.isChecked(),
        true // mOverwrite.isChecked()
      );

      export.mType = ModelType.NONE;
      export.mMime = "text/plain";
      export.mExt  = "txt";
     
      if ( mStlBinary.isChecked() ) {
        export.mType = ModelType.STL_BINARY;
        export.mMime = "application/octet-stream";
        export.mExt  = "stl";
      } else if ( mStlAscii.isChecked() ) {
        export.mType = ModelType.STL_ASCII;
        export.mMime = "application/octet-stream";
        export.mExt  = "stl";
      } else if ( mKmlAscii.isChecked() ) {
        export.mType = ModelType.KML_ASCII;
        export.mMime = "application/octet-stream";
        export.mExt  = "kml";
      } else if ( mCgalAscii.isChecked() ) {
        export.mType = ModelType.CGAL_ASCII;
        export.mMime = "application/octet-stream";
        export.mExt  = "cgal";
      } else if ( mLasBinary.isChecked() ) {
        export.mType = ModelType.LAS_BINARY;
        export.mMime = "application/octet-stream";
        export.mExt  = "las";
      } else if ( mDxfAscii.isChecked() ) {
        export.mType = ModelType.DXF_ASCII;
        export.mMime = "application/octet-stream";
      } else if ( mShpAscii.isChecked() ) {
        export.mType = ModelType.SHP_ASCII;
        export.mMime = "application/octet-stream";
        export.mExt  = "shp";
      } else if ( mGltf.isChecked() ) {
        export.mType = ModelType.GLTF;
        export.mMime = "application/octet-stream";
        export.mExt  = "gltf";
      } else {
        export.mType = ModelType.SERIAL;
        // export.mMime = "text/plain"
        export.mExt  = "txt";
      }
      mApp.selectExportFile( export );
      // (new ExportTask( mApp, mParser, export.mType, pathname, splays, station, surface, walls, overwrite )).execute();

    // } else if ( v.getId() == R.id.dirname ) {
    //   int pos = mDirname.lastIndexOf('/');
    //   if ( pos > 1 ) {
    //     updateList( mDirname.substring(0, pos ) );
    //   }
    //   return;
    } else if ( v.getId() == R.id.stl_binary ) {
      if ( mStlBinary.isChecked() ) {
        mStlAscii.setChecked( false );
        mKmlAscii.setChecked( false );
        mCgalAscii.setChecked( false );
        mLasBinary.setChecked( false );
        mDxfAscii.setChecked( false );
        mShpAscii.setChecked( false );
        mGltf.setChecked( false );
      }
      return;
    } else if ( v.getId() == R.id.stl_ascii ) {
      if ( mStlAscii.isChecked() ) {
        mStlBinary.setChecked( false );
        mKmlAscii.setChecked( false );
        mCgalAscii.setChecked( false );
        mLasBinary.setChecked( false );
        mDxfAscii.setChecked( false );
        mShpAscii.setChecked( false );
        mGltf.setChecked( false );
      }
      return;
    } else if ( v.getId() == R.id.kml_ascii ) {
      if ( mKmlAscii.isChecked() ) {
        mStlBinary.setChecked( false );
        mStlAscii.setChecked( false );
        mCgalAscii.setChecked( false );
        mLasBinary.setChecked( false );
        mDxfAscii.setChecked( false );
        mShpAscii.setChecked( false );
        mGltf.setChecked( false );
      }
      return;
    } else if ( v.getId() == R.id.cgal_ascii ) {
      if ( mCgalAscii.isChecked() ) {
        mStlBinary.setChecked( false );
        mStlAscii.setChecked( false );
        mKmlAscii.setChecked( false );
        mLasBinary.setChecked( false );
        mDxfAscii.setChecked( false );
        mShpAscii.setChecked( false );
        mGltf.setChecked( false );
      }
      return;
    } else if ( v.getId() == R.id.las_binary ) {
      if ( mLasBinary.isChecked() ) {
        mStlBinary.setChecked( false );
        mStlAscii.setChecked( false );
        mKmlAscii.setChecked( false );
        mCgalAscii.setChecked( false );
        mDxfAscii.setChecked( false );
        mShpAscii.setChecked( false );
        mGltf.setChecked( false );
      }
      return;
    } else if ( v.getId() == R.id.dxf_ascii ) {
      if ( mDxfAscii.isChecked() ) {
        mStlBinary.setChecked( false );
        mStlAscii.setChecked( false );
        mKmlAscii.setChecked( false );
        mCgalAscii.setChecked( false );
        mLasBinary.setChecked( false );
        mShpAscii.setChecked( false );
        mGltf.setChecked( false );
      }
      return;
    } else if ( v.getId() == R.id.shp_ascii ) {
      if ( mShpAscii.isChecked() ) {
        mStlBinary.setChecked( false );
        mStlAscii.setChecked( false );
        mKmlAscii.setChecked( false );
        mCgalAscii.setChecked( false );
        mLasBinary.setChecked( false );
        mDxfAscii.setChecked( false );
        mGltf.setChecked( false );
      }
      return;
    } else if ( v.getId() == R.id.gltf ) {
      if ( mGltf.isChecked() ) {
        mStlBinary.setChecked( false );
        mStlAscii.setChecked( false );
        mKmlAscii.setChecked( false );
        mCgalAscii.setChecked( false );
        mLasBinary.setChecked( false );
        mDxfAscii.setChecked( false );
        mShpAscii.setChecked( false );
      }
      return;
    }
    dismiss();
  }
}

