/* @file ImportDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid import file list dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
// import java.util.Set;
import java.util.ArrayList;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

// import android.content.IntentFilter;
import android.content.Context;


class ImportDialog extends MyDialog
                          implements OnItemClickListener
                          , OnClickListener
{ 
  // private final TopoDroidApp mApp;
  private final MainWindow mParent;
  private File[] mFiles = null;
  private File[] mZips = null;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  private Button mBtnCancel;

  ImportDialog( Context context, MainWindow parent, File[] files, File[] zips )
  {
    super( context, R.string.ImportDialog );
    mParent  = parent;
    mFiles = files;
    mZips  = zips;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    initLayout( R.layout.import_dialog, R.string.import_title );

    ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // mBtnCancel = (Button)findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );
    ( (Button)findViewById( R.id.button_cancel ) ).setOnClickListener( this );

    ArrayList<String> names = new ArrayList<>();
    if ( mFiles != null ) {
      for ( File f : mFiles ) { 
        names.add( f.getName() );
      }
    }
    if ( mZips != null ) {
      for ( File f : mZips ) {
        names.add( f.getName() );
      }
    }
    if ( names.size() > 0 ) { // this is guaranteed
      TDUtil.sortStringList( names );
      for ( int k=0; k<names.size(); ++k ) {
        mArrayAdapter.add( names.get(k) );
      }
      mList.setAdapter( mArrayAdapter );
    }
  }

  @Override
  public void onClick( View v ) 
  {
    dismiss();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    String item = ((TextView) view).getText().toString();
    TDLog.Log(  TDLog.LOG_INPUT, "ImportDialog onItemClick() <" + item + ">" );

    // hide();
    mList.setOnItemClickListener( null );
    // setTitle(" W A I T ");
    dismiss();

    mParent.importFile( item );
  }

}

