/** @file TdmEquateNewDialog.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid Manager new equate dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.ui.MyDialog;
import com.topodroid.DistoX.R;

import java.util.List;
import java.util.ArrayList;

// import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;
import android.view.Window;

import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
// import android.widget.Spinner;
// import android.widget.ArrayAdapter;


class TdmEquateNewDialog extends MyDialog
                         implements OnClickListener
{
  TdmViewActivity mParent;
  ArrayList< TdmViewCommand > mCommands;
  // String[] mStation;
  // Spinner[] mSpinner;
  EditText[] mEdit;
  int size;

  private Button mBTok;
  private Button mBTback;

  TdmEquateNewDialog( Context context, TdmViewActivity parent, ArrayList< TdmViewCommand > commands )
  {
    super( context, R.string.TdmEquateNewDialog );
    mParent   = parent;
    mCommands = commands;
    size = mCommands.size();
    // mStation = new String[size];
    mEdit = new EditText[size];
    // mSpinner = new Spinner[size];
    for ( int k=0; k<size; ++k ) {
      // mStation[k] = null;
      mEdit[k] = null;
      // mSpinner[k] = null;
    }
  }

  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    initLayout( R.layout.tdequate_new_dialog, R.string.title_equate_new );

    mBTok = (Button) findViewById( R.id.button_ok );
    mBTok.setOnClickListener( this );
    mBTback = (Button) findViewById( R.id.button_back );
    mBTback.setOnClickListener( this );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp1.setMargins( 0, 10, 20, 10 );
    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp2.setMargins( 0, 10, 20, 10 );

    for ( int k=0; k<size; ++k ) {
      TdmViewCommand vc = mCommands.get( k );
      // List< TdmViewStation > vs = vc.mStations;
      
      LinearLayout layout = new LinearLayout( mContext );
      TextView text = new TextView( mContext );
      text.setText( vc.name() );
      mEdit[k] = new EditText( mContext );
      mEdit[k].setHint( "..." );
      
      // mSpinner[k] = new Spinner( mContext );
      // ArrayAdapter adapter = new ArrayAdapter<String>( mContext, R.layout.menu, mTypes );
      // spinner[k].setAdapter( adapter );
      layout.addView( text, lp1 );
      layout.addView( mEdit[k], lp2 );
      // layout.addView( spinner[k], lp );
      layout4.addView( layout, lp2 );
    }
    layout4.invalidate();
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    if ( b == mBTok ) {
      ArrayList<String> sts = new ArrayList<String>();
      for ( int k=0; k<size; ++k ) {
        TdmViewCommand vc = mCommands.get( k );
        String survey = vc.name();
        int len = survey.length();
        while ( len > 0 && survey.charAt( len - 1 ) == '.' ) -- len;
        String station = mEdit[k].getText().toString();
        if ( station != null && station.length() > 0 ) {
          sts.add( station + "@" + survey.substring(0,len) );
        }
      }
      mParent.makeEquate( sts ); // does nothing if sts.size() <= 1
    }
    dismiss();
  }
}
