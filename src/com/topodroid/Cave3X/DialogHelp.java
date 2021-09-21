/* @file DialogHelp.java
 *
 * @author marco corvi
 * @date jul 2018
 *
 * @brief help dialog (from TopoDroid)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.Cave3X;

// import com.topodroid.utils.TDLog;
// import com.topodroid.Cave3X.R;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;

import android.widget.Button;
import android.widget.TextView;
import android.webkit.WebView;

import android.view.View;
import android.view.View.OnClickListener;

class DialogHelp extends Dialog
                 // implements OnClickListener
{
  private Context mContext;

  DialogHelp( Context context )
  {
    super( context ); 
    mContext = context;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.cave3d_help_dialog );
    setTitle( String.format( mContext.getResources().getString( R.string.chelp_title ), TopoGL.VERSION ) );
    WebView web_view = ( WebView ) findViewById( R.id.text_view );
    web_view.getSettings().setJavaScriptEnabled( false ); // no JS
    web_view.getSettings().setSupportZoom( true ); 
    web_view.loadUrl( "file:///android_asset/help.htm" );
  }

  // @Override 
  // public void onClick( View v ) 
  // {
  //   dismiss();
  // }
}

