/* @file MyDialog.java
 *
 * @author marco corvi
 * @date jan 2016
 *
 * @brief TopoDroid generic dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import com.topodroid.help.UserManualActivity;
import com.topodroid.DistoX.R;

import android.util.Log;

// import android.app.Activity;
import android.app.Dialog;
// import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import android.widget.LinearLayout;
import android.widget.Button;


public class MyDialog extends Dialog
                      // implements View.OnClickListener
{
  protected final Context mContext;
  private String mHelpPage = null;

  public MyDialog( Context context, int help_resource )
  {
    super( context );
    mContext = context;
    if ( help_resource != 0 ) {
      mHelpPage = mContext.getResources().getString( help_resource );
    }
  }

  public void anchorTop()
  {
    Window wnd = getWindow();
    WindowManager.LayoutParams wlp = wnd.getAttributes();
    wlp.gravity = Gravity.TOP;
    wnd.setAttributes( wlp );
  }


  private void setHelpLayout()
  {
    Button btn_help = (Button) findViewById( R.id.button_help );
    if ( btn_help != null ) {
      if ( mHelpPage != null ) {
        // Log.v("DistoX", "set help page " + mHelpPage );
        btn_help.setOnClickListener( new android.view.View.OnClickListener() {
          @Override
          public void onClick( View v ) {
            if ( mHelpPage != null ) UserManualActivity.showHelpPage( mContext, mHelpPage );
          }
        } );
        LinearLayout help = (LinearLayout) findViewById( R.id.help );
        if ( help != null ) {
          help.setBackgroundColor( 0xff333333 );
          // help.setOnLongClickListener( new OnLongClickListener() {
          //   @Override
          //   public boolean onLongClick( View v ) {
          //     if ( mHelpPage != null ) UserManualActivity.showHelpPage( mContext, mHelpPage );
          //     return true;
          //   }
          // } );
        }
      } else {
        // Log.v("DistoX", "no help page");
        btn_help.setVisibility( View.GONE );
      }
    // } else {
    //   // Log.v("DistoX", "null button help");
    }
  }

  // utility method for derived classes
  protected void initLayout( int layout_resource, int title_resource )
  {
    if ( title_resource == -1 ) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    setContentView( layout_resource );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ); // NullPointerException
    if ( title_resource != -1 ) {
      setTitle( title_resource );
    }
    setHelpLayout();
  }

  protected void initLayout( int layout_resource, String title )
  {
    if ( title == null ) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    setContentView( layout_resource );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ); // NullPointerException
    if ( title != null ) {
      setTitle( title );
    }
    setHelpLayout();
  }

  protected void initLayout( View v, int title_resource )
  {
    if ( title_resource == -1 ) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    setContentView( v );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT ); // NullPointerException
    if ( title_resource != -1 ) {
      setTitle( title_resource );
    }
    setHelpLayout();
  }

  @Override 
  // public boolean onKeyLongPress( int code, KeyEvent ev )
  public boolean onKeyDown( int code, KeyEvent ev )
  {
    // Log.v("DistoX-KEYCODE", "code " + code );
    if ( code == KeyEvent.KEYCODE_BACK ) {
      onBackPressed();
      return true;
    } else if ( code == KeyEvent.KEYCODE_MENU || code == KeyEvent.KEYCODE_VOLUME_UP ) {
      if ( mHelpPage != null ) {
        UserManualActivity.showHelpPage( mContext, mHelpPage );
      }
      return true;
    }
    return false;
  }

}

