/** @file PlotRecoverDialog.java
 *
 * @author marco corvi
 * @date jan 2015
 *
 * @brief TopoDroid plot recover dialog
 *
 * displays the stack of saved stations and allows to push 
 * a station on it or pop one from it
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

// import java.io.StringWriter;
// import java.io.PrintWriter;
import java.io.File;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.CheckBox;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

import android.util.Log;

public class PlotRecoverDialog extends Dialog
                        implements View.OnClickListener
                        , OnItemClickListener
{
  private Context mContext;
  private TopoDroidApp mApp;
  private DrawingActivity mParent;

  private TextView mTVfilename;
  private Button mBtnOK;

  private ListView mList;
  ArrayAdapter<String> mAdapter;
  String mFilename;
  int mType;

  // type is either 1 or 2
  public PlotRecoverDialog( Context context, DrawingActivity parent, String filename, int type )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mFilename = filename;
    mType = type;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plot_recover_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mList = (ListView) findViewById(R.id.th2_list);
    mList.setDividerHeight( 2 );
    mList.setOnItemClickListener( this );
 
    mTVfilename = (TextView) findViewById( R.id.filename );

    mBtnOK      = (Button) findViewById(R.id.btn_ok );
    mBtnOK.setOnClickListener( this );   // OK-SAVE
    // mBtnCancel.setOnClickListener( this );

    // setTitle( R.string.title_current_station );
    updateList();
  }

  private void updateList()
  {
    mAdapter = new ArrayAdapter<String>( mContext, R.layout.message );

    String filename = TopoDroidPath.getTh2FileWithExt( mFilename );
    File file = new File( filename );
    if ( file.exists() ) {
      mAdapter.add( Long.toString(file.length()) + " " +  mFilename + ".th2" );
    }
    String filename1 = filename + ".bck";
    file = new File( filename );
    if ( file.exists() ) {
      mAdapter.add( Long.toString(file.length()) + " " +  mFilename + ".th2.bck" );
    }
    for ( int i=0; i<SaveTh2File.NR_BACKUP; ++i ) {
      filename1 = filename + ".bck" + Integer.toString(i);
      file = new File( filename1 );
      if ( file.exists() ) {
        mAdapter.add( Long.toString(file.length()) + " " +  mFilename + ".th2.bck" + Integer.toString(i) );
      }
    }
    mList.setAdapter( mAdapter );
    mTVfilename.setText( mFilename + ".th2" );
  }


  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String[] name = item.toString().split(" ");
    mTVfilename.setText( name[1] );
  }
 
  @Override
  public void onClick(View v) 
  {
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "PlotRecoverDialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnOK ) { // OK
      mParent.doRecover( mTVfilename.getText().toString(), mType );
      dismiss();
    }
  }

}
