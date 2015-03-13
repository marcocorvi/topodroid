/** @file ImportDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid import file list dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120605 created
 * 20121212 sorted names in alphabetical order
 */
package com.topodroid.DistoX;

import java.io.File;
import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

// import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import android.content.Intent;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;

import android.content.IntentFilter;
import android.content.Context;


public class ImportDialog extends Dialog
                          implements OnItemClickListener
                          , OnClickListener
{ 
  private Context mContext;
  private TopoDroidApp app;
  private TopoDroidActivity mParent;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  // private Button mBtnCancel;

  public ImportDialog( Context context, TopoDroidActivity parent, TopoDroidApp _app )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    app = _app;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.import_dialog);

    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // mBtnCancel = (Button)findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );

    // setTitleColor( 0x006d6df6 );
    setTitle( R.string.import_title );

    File[] files = TopoDroidPath.getImportFiles();
    ArrayList<String> names = new ArrayList<String>();
    if ( files != null ) {
      for ( File f : files ) { 
        names.add( f.getName() );
      }
    }
    File[] zips = TopoDroidPath.getZipFiles();
    if ( zips != null ) {
      for ( File f : zips ) {
        names.add( f.getName() );
      }
    }
    if ( names.size() > 0 ) {
      // sort files by name (alphabetical order)
      Comparator<String> cmp = new Comparator<String>() 
      {
          @Override
          public int compare( String s1, String s2 ) { return s1.compareToIgnoreCase( s2 ); }
      };
      Collections.sort( names, cmp );
      for ( int k=0; k<names.size(); ++k ) {
        mArrayAdapter.add( names.get(k) );
      }
      mList.setAdapter( mArrayAdapter );
    } else {
      Toast.makeText( mContext, R.string.import_none, Toast.LENGTH_SHORT ).show();
      dismiss();
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
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "ImportDialog onItemClick() " + item.toString() );

    // hide();
    mList.setOnItemClickListener( null );
    // setTitle(" W A I T ");
    dismiss();

    mParent.importFile( item );
  }

}

