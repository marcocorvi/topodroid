/* @file CBDdialog.java
 *
 * @author marco corvi
 * @date sept 2015
 *
 * @brief TopoDroid current base directory dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;
// import java.util.Set;
// import java.util.ArrayList;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

// import android.content.IntentFilter;
import android.content.Context;


class CBDdialog extends MyDialog
                implements OnItemClickListener
                , OnClickListener
{ 
  private CWDActivity mParent;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  private TextView mTVcbd;
  private EditText mETsubdir;
  private String mBasename;
  private Button mBtnOk;
  private Button mBtnCreate;


  CBDdialog( Context context, CWDActivity parent, String basename )
  {
    super( context, R.string.CBDdialog );
    mParent  = parent;
    mBasename = basename;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.cbd_dialog, R.string.cbd_title );

    mTVcbd = (TextView) findViewById( R.id.cbd );
    mETsubdir = (EditText) findViewById( R.id.subdir );
    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    ((Button)findViewById( R.id.button_cancel )).setOnClickListener( this );
    mBtnCreate = (Button)findViewById( R.id.button_create );
    mBtnOk = (Button)findViewById( R.id.button_ok ); 
    mBtnCreate.setOnClickListener( this );
    mBtnOk.setOnClickListener( this );

    // setTitleColor( TDColor.TITLE_NORMAL );
    
    updateList();
  }

  private void updateList()
  {
    mTVcbd.setText( mBasename );

    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>( mContext, R.layout.menu );
    File base = new File( mBasename );
    File[] dirs = base.listFiles( new FileFilter() {
      public boolean accept( File pathname ) { 
        if ( pathname.getName().startsWith(".") ) return false;
        if ( pathname.getName().toUpperCase(Locale.US).startsWith("TOPODROID") ) return false;
        try {
	  // canExecute() is API-9
          if ( pathname.isDirectory() && pathname.canRead() ) return true;
	} catch ( SecurityException e ) { }
        return false;
      }
    } );
    String parent_name = base.getParent();
    if ( parent_name != null ) {
      // File parent_path = new File( parent_name );
      // try {
      //   if ( parent_path.canWrite() ) mArrayAdapter.add( ".." );
      // } catch ( SecurityException e ) { }
      arrayAdapter.add( ".." );
    }
    if ( dirs != null && dirs.length > 0 ) {
      for ( File item : dirs ) {
        arrayAdapter.add( item.getName() );
      }
    }
    mList.setAdapter( arrayAdapter );
    mBtnOk.setEnabled( base.canWrite() );
    mBtnCreate.setEnabled( base.canWrite() );
  }

  @Override
  public void onClick( View v ) 
  {
    if ( v.getId() == R.id.button_ok ) {
      mParent.setBasename( mBasename );
    } else if ( v.getId() == R.id.button_create ) {
      if ( mETsubdir.getText() == null ) {
        mETsubdir.setError( mContext.getResources().getString( R.string.error_name_required ) );
        return;
      }
      String subdir = mETsubdir.getText().toString().trim();
      if ( subdir.length() == 0 ) {
        mETsubdir.setError( mContext.getResources().getString( R.string.error_invalid_name ) );
	return;
      }
      if ( subdir.startsWith(".") || subdir.toUpperCase(Locale.US).startsWith("TOPODROID") ) {
        mETsubdir.setError( mContext.getResources().getString( R.string.error_invalid_name ) );
	return;
      }
      subdir = mBasename + "/" + subdir;
      File dir = new File( subdir );
      if ( dir.exists() ) {
        mETsubdir.setError( mContext.getResources().getString( R.string.error_dir_exists ) );
	return;
      }
      if ( ! dir.mkdir() ) TDLog.Error("mkdir error");
      mParent.setBasename( subdir );
    } // else if ( v.getId() == R.id.button_cancel ) 
    dismiss();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    String item = ((TextView) view).getText().toString();
    if ( item.equals("..") ) {
      File base = new File( mBasename );
      String parent_name = base.getParent();
      if ( parent_name != null ) mBasename = parent_name;
    } else {
      mBasename = mBasename + "/" + item;
    }
    updateList();
  }

}


