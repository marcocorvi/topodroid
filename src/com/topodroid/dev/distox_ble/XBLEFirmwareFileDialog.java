/* @file XBLEFirmwareFileDialog.java
 *
 * @author siwei tian
 * @date aug 2022
 *
 * @brief TopoDroid firmware file list dialog
 *        
 * used by the XBLEFirmwareDialog to get a firmware filename
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox_ble;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.topodroid.TDX.R;
import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.TDToast;
import com.topodroid.dev.distox2.FirmwareDialog;
import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;

import java.io.File;
import java.util.ArrayList;


public class XBLEFirmwareFileDialog extends MyDialog
                         implements OnItemClickListener
{
  private final XBLEFirmwareDialog mParent;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  // private TextView mTVfile;

  public XBLEFirmwareFileDialog(Context context, XBLEFirmwareDialog parent )
  {
    super( context, null, R.string.FirmwareFileDialog ); // null app
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

