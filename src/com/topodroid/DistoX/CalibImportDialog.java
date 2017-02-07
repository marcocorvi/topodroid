/** @file CalibImportDialog.java
 *
 * @author marco corvi
 * @date sept 2015
 *
 * @brief TopoDroid import file list dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
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


public class CalibImportDialog extends MyDialog
                               implements OnItemClickListener
                               , OnClickListener
{ 
  private DeviceActivity mParent;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  private Button mBtnCancel;

  public CalibImportDialog( Context context, DeviceActivity parent )
  {
    super( context, R.string.CalibImportDialog );
    mParent  = parent;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.import_dialog, R.string.calib_import_title );

    mArrayAdapter = new ArrayAdapter<String>( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.list);
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnCancel = (Button)findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );

    // setTitleColor( 0x006d6df6 );

    File[] files = TDPath.getCalibFiles();
    ArrayList<String> names = new ArrayList<String>();
    if ( files != null ) {
      for ( File f : files ) { 
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
    // TDLog.Log(  TDLog.LOG_INPUT, "CalibImportDialog onItemClick() " + item );

    // hide();
    mList.setOnItemClickListener( null );
    // setTitle(" W A I T ");
    dismiss();

    mParent.importCalibFile( item );
  }

}


