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
package com.topodroid.inport;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.DistoX.TDUtil;
import com.topodroid.DistoX.R;
import com.topodroid.DistoX.MainWindow;


import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import android.content.Context;


public class ImportDialog extends MyDialog
                          implements OnItemClickListener
                          , OnClickListener
{ 
  // private final TopoDroidApp mApp;
  private final MainWindow mParent;
  // private File[] mFiles = null;
  private File[] mZips = null;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListView mList;
  private Button mBtnCancel;

  // public ImportDialog( Context context, MainWindow parent, File[] files, File[] zips )
  public ImportDialog( Context context, MainWindow parent, File[] zips )
  {
    super( context, R.string.ImportDialog );
    mParent  = parent;
    // mFiles = files;
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

    /*
    ContentResolver resolver = context.getContentResolver();
    Uri uri = MediaStore.Files.getContentUri("external");
    String[] projection = null;
    String selection = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
    String mimeZip = MimeTypeMap.getSingleton().getMimeTypeFromExtension("zip");
    String[] selectionArgs = new String[]{ mimeZip };
    String sortOrder = null;
    Cursor allFiles = cr.query(uri, projection, selection, selectionArgs, sortOrder);
    */

    ArrayList< String > names = new ArrayList<>();
    // if ( mFiles != null ) {
    //   for ( File f : mFiles ) { 
    //     names.add( f.getName() );
    //   }
    // }
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
    if ( ! ( view instanceof TextView ) ) {
      TDLog.Error("import view instance of " + view.toString() );
      return;
    }
    String item = ((TextView) view).getText().toString();
    // TDLog.Log(  TDLog.LOG_INPUT, "Import dialog item <" + item + ">" );

    // hide();
    mList.setOnItemClickListener( null );
    // setTitle(" W A I T ");
    dismiss();

    // mParent.importFile( item );
    mParent.importZipFile( item );
  }

}

