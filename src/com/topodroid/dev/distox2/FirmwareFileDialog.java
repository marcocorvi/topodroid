/* @file FirmwareFileDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid firmware file list dialog
 *        
 * used by the FirmwareDialog to get a firmware filename
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox2;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyDialog;
import com.topodroid.DistoX.TDPath;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.R;

import java.io.File;
// import java.util.Set;
import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.Collections;

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

// import android.content.IntentFilter;
import android.content.Context;


public class FirmwareFileDialog extends MyDialog
                         implements OnItemClickListener
{ 
  private final FirmwareDialog mParent;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  // private TextView mTVfile;

  public FirmwareFileDialog( Context context, FirmwareDialog parent )
  {
    super( context, R.string.FirmwareFileDialog );
    mParent = parent;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.firmware_file_dialog, R.string.firmware_file_title );

    mList = (ListView) findViewById( R.id.list );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // mTVfile = (TextView) findViewById( R.id.file );
    // setTitleColor( TDColor.TITLE_NORMAL );

    File[] files = TDPath.getBinFiles();
    ArrayList< String > names = new ArrayList<>();
    if ( files != null ) {
      for ( File f : files ) { 
        names.add( f.getName() );
      }
    }
    if ( names.size() > 0 ) {
      // sort files by name (alphabetical order)
      // Comparator<String> cmp = new Comparator<String>() 
      // {
      //     @Override
      //     public int compare( String s1, String s2 ) { return s1.compareToIgnoreCase( s2 ); }
      // };
      // Collections.sort( names, cmp );
      TDUtil.sortStringList( names );

      ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>( mContext, R.layout.message );
      for ( int k=0; k<names.size(); ++k ) {
        mArrayAdapter.add( names.get(k) );
      }
      mList.setAdapter( mArrayAdapter );
    } else {
      TDToast.makeBad( R.string.firmware_none );
      dismiss();
    }

  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    if ( ! ( view instanceof TextView ) ) {
      TDLog.Error("firmware file view instance of " + view.toString() );
      return;
    }
    String item = ((TextView) view).getText().toString();

    // hide();
    mList.setOnItemClickListener( null );
    dismiss();

    mParent.setFile( item );
  }

}

