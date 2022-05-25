/* @file DialogSketch.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief file opener dialog: get TopoDroid sketch-export filename
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.MyFileItem;

import java.io.File; // FIXME-FILE
import java.io.FilenameFilter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;

import android.view.View.OnClickListener;
// import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Button;

import android.view.View;
// import android.view.View.OnClickListener;


class DialogSketch extends MyDialog
                   implements OnItemClickListener
                   , OnClickListener
{
  private TopoGL  mApp;
  private String  mBaseDir;

  private ArrayList< MyFileItem > mItems;
  private MyFileAdapter mArrayAdapter;
  private ListView mList;

  class MyFilenameFilter implements FilenameFilter
  {
    public boolean accept( File dir, String name ) {
      return ( name.toLowerCase().endsWith( ".c3d" ) );  // Cave3D sketch file
    }
  }

  class MyDirnameFilter implements FilenameFilter
  {
    public boolean accept( File dir, String name ) {
      File file = new File( dir, name );
      return ( file.isDirectory() && ! name.startsWith(".") );
    }
  }

  DialogSketch( Context context, TopoGL app )
  {
    super( context, R.string.DialogSketch );
    mApp  = app;
    mBaseDir = TDPath.getC3dPath(); // Cave3DFile.C3D_PATH;
    // TDLog.v("TopoGL sketch base dir " + mBaseDir );
  } 

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.cave3d_openfile, R.string.select_sketch_file );

    mList = (ListView) findViewById( R.id.sketch_list );
    // mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );
 
    mItems = new ArrayList< MyFileItem >();
    mArrayAdapter = new MyFileAdapter( mContext, this, mList, R.layout.message, mItems );
    updateList( mBaseDir );
    mList.setAdapter( mArrayAdapter );

    ((Button)findViewById( R.id.button_cancel )).setOnClickListener( this );
  }

  private void updateList( String basedir )
  {
    if ( basedir == null ) return;
    File dir = new File( basedir );
    if ( dir.exists() ) {
      String[] dirs  = dir.list( new MyDirnameFilter() );
      String[] files = dir.list( new MyFilenameFilter() );
      mArrayAdapter.clear();
      mArrayAdapter.add( "..", true );
      if ( dirs != null ) {
        for ( String item : dirs ) {
          mArrayAdapter.add( item, true );
        }
      }
      if ( files != null ) {
        for ( String item : files ) {
          mArrayAdapter.add( item, false );
        }
      }
    } else {
      // should never comes here
      TDToast.makeWarn( R.string.warning_no_cwd );
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String name = item.toString();
    if ( name.startsWith("+ ") ) {
      name = name.substring( 2 );
    }
    if ( name.equals("..") ) {
      File dir = new File( mBaseDir );
      String parent_dir = dir.getParent();
      if ( parent_dir != null ) {
        mBaseDir = parent_dir;
        updateList( mBaseDir );
      } else {
        TDToast.makeWarn( R.string.warning_no_parent );
      }
      return;
    }
    File file = new File( mBaseDir, name );
    if ( file.isDirectory() ) {
      mBaseDir += "/" + name;
      updateList( mBaseDir );
      return;
    }
    mApp.openSketch( mBaseDir + "/" + name, name );
    dismiss();
  }

  @Override
  public void onClick( View v ) 
  {
    // only for button_cancel
    dismiss();
  }
}
