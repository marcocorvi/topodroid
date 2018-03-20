/** @file FirmwareFileDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid firmware file list dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
// import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.Toast;

// import android.content.IntentFilter;
import android.content.Context;


class FirmwareFileDialog extends MyDialog
                          implements OnItemClickListener
{ 
  // private TopoDroidApp mApp; // UNUSED
  private FirmwareDialog mParent;

  private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  // private TextView mTVfile;

  FirmwareFileDialog( Context context, FirmwareDialog parent, TopoDroidApp app )
  {
    super( context, R.string.FirmwareFileDialog );
    mParent  = parent;
    // mApp = app;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.firmware_file_dialog, R.string.firmware_file_title );

    mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );

    mList = (ListView) findViewById( R.id.list );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // mTVfile = (TextView) findViewById( R.id.file );

    // setTitleColor( TDColor.TITLE_NORMAL );

    File[] files = TDPath.getBinFiles();
    ArrayList<String> names = new ArrayList<>();
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
      TDToast.make( mContext, R.string.firmware_none );
      dismiss();
    }

  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    String item = ((TextView) view).getText().toString();
    // TDLog.Log(  TDLog.LOG_INPUT, "FirmwareFileDialog onItemClick() " + item.toString() );

    // hide();
    mList.setOnItemClickListener( null );
    dismiss();

    mParent.setFile( item );
  }

}

