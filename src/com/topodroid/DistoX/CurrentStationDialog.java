/** @file CurrentStationDialog.java
 *
 * @author marco corvi
 * @date jan 2015
 *
 * @brief TopoDroid current station dialog
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

import android.widget.TextView;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

import android.util.Log;

public class CurrentStationDialog extends Dialog
                        implements View.OnClickListener
                        , OnItemClickListener
{
  private Context mContext;
  private TopoDroidApp mApp;
  private ShotActivity mParent;
  private EditText mName;
  private EditText mComment;

  private Button mBtnPush;
  private Button mBtnPop;
  private Button mBtnOK;
  private Button mBtnClear;
  // private Button mBtnCancel;

  private ListView mList;

  public CurrentStationDialog( Context context, ShotActivity parent, TopoDroidApp app )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mApp = app;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.current_station_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mList = (ListView) findViewById(R.id.list);
    mList.setDividerHeight( 2 );
    mList.setOnItemClickListener( this );

    mName = (EditText) findViewById( R.id.name );
    mComment = (EditText) findViewById( R.id.comment );
    mName.setText( mApp.getCurrentOrLastStation() );

    mBtnPush    = (Button) findViewById(R.id.button_push);
    mBtnPop     = (Button) findViewById(R.id.button_pop );
    mBtnOK      = (Button) findViewById(R.id.button_ok );
    mBtnClear   = (Button) findViewById(R.id.button_clear );
    // mBtnCancel = (Button) findViewById(R.id.button_cancel);

    mBtnPush.setOnClickListener( this ); // STORE
    mBtnPop.setOnClickListener( this );  // DELETE
    mBtnOK.setOnClickListener( this );   // OK-SAVE
    mBtnClear.setOnClickListener( this );   // CLEAR
    // mBtnCancel.setOnClickListener( this );

    setTitle( R.string.title_current_station );
    updateList();
  }

  private void updateList()
  {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>( mContext, R.layout.message );
    // mApp.fillCurrentStationAdapter( adapter );
    ArrayList< CurrentStation > stations = mApp.mData.getStations( mApp.mSID );
    for ( CurrentStation st : stations ) {
      adapter.add( st.toString() );
    }
    mList.setAdapter( adapter );
  }


  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String name = item.toString();
    String[] token = name.split(" ");
    if ( token.length == 1 ) {
      name = name.trim();
    } else {
      name = token[0];
    }
    // Log.v("DistoX", "get station <" + name + ">" );
    CurrentStation cs = mApp.mData.getStation( mApp.mSID, name );
    if ( cs == null ) {
      mName.setText( "" );
      mComment.setText( null );
    } else {
      mName.setText( cs.mName );
      mComment.setText( cs.mComment );
    }
  }
 
  @Override
  public void onClick(View v) 
  {
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "CurrentStationDialog onClick() " );
    Button b = (Button) v;
    String name = mName.getText().toString().trim();
    String error = mContext.getResources().getString( R.string.error_name_required );
    if ( b == mBtnPush ) { // STORE
      if ( name.length() == 0 ) {
        mName.setError( error );
        return;
      }
      
      error = mContext.getResources().getString( R.string.error_comment_required );
      if ( mComment.getText() == null ) {
        mComment.setError( error );
        return;
      } 
      String comment = mComment.getText().toString().trim();
      if ( comment.length() == 0 ) {
        mComment.setError( error );
        return;
      }
      // mApp.pushCurrentStation( name, comment );
      mApp.mData.insertStation( mApp.mSID, name, comment );
      updateList();
      return;

    } else if ( b == mBtnPop ) { // DELETE
      if ( name.length() == 0 ) {
        mName.setError( error );
        return;
      }
      mApp.mData.deleteStation( mApp.mSID, name );
      updateList();
      mName.setText("");
      mComment.setText("");

      // CurrentStation cs = mApp.popCurrentStation();
      // if ( cs == null ) {
      //   mName.setText("-");
      //   mComment.setText( "" );
      // } else {
      //   mName.setText( cs.mName );
      //   mComment.setText( cs.mComment );
      // }
      return;
    } else if ( b == mBtnClear ) {
      mName.setText("");
      mComment.setText("");
      return;
    } else if ( b == mBtnOK ) {
      if ( name.length() > 0 ) {
        mApp.setCurrentStationName( name );
      } else {
        mApp.setCurrentStationName( null );
      }
      mParent.updateDisplay();

    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}
