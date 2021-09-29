/* @file TdmHelpDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager help dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.R;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;

import android.content.Intent;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class TdmHelpDialog extends Dialog // Activity
{
  private Context  mContext;
  private TextView mTVtext;

  TdmHelpDialog( Context context )
  {
    super( context );
    mContext = context;
  }

  private void load( )
  {
    // TDLog.v( "reading from file " + mFilename );
    try {
      InputStreamReader fr = new InputStreamReader( mContext.getAssets().open( "tdm_help.txt" ) );
      BufferedReader br = new BufferedReader( fr );
      String line = br.readLine();
      while ( line != null ) {
        mTVtext.append( line + "\n" );
        line = br.readLine();
      }
      fr.close();
    } catch ( IOException e ) {
      TDLog.Error("Tdm Help: error " + e.toString() );
    }
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView( R.layout.tdmanager_help_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( String.format( mContext.getResources().getString(R.string.title_help), TDVersion.string() ) );

    mTVtext = (TextView) findViewById(R.id.help );

    load();

  }

}


