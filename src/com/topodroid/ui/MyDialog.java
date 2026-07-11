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
import com.topodroid.util.TDUtil;
import com.topodroid.util.TDLog;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.R;

// import android.app.Activity;
import android.app.Dialog;
// import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.View.OnClickListener;
// import android.view.View.OnLongClickListener;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;


public class MyDialog extends Dialog
                      // implements View.OnClickListener
{
  protected final Context mContext;
  protected final TopoDroidApp mApp;
  private String mHelpPage = null;

  /** cstr
   * @param context       context
   * @param help_resource help resource id
   */
  public MyDialog( Context context, TopoDroidApp app, int help_resource )
  {
    super( context );
    mContext = context;
    mApp     = app;
    if ( help_resource != 0 ) {
      mHelpPage = resString( help_resource );
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
        // TDLog.v( "set help page " + mHelpPage );
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
        // TDLog.v( "no help page");
        btn_help.setVisibility( View.GONE );
      }
    // } else {
    //   // TDLog.v( "null button help");
    }

    if ( getWindow() != null ) getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
  }

  /** initialize the layout - utility method for derived classes
   * @param layout_resource layout resource id
   * @param title_resource  title resource id
   */
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

  /** initialize the layout - utility method for derived classes
   * @param layout_resource layout resource id
   * @param title           title string
   */
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

  /** initialize the layout - utility method for derived classes
   * @param v               view of the content
   * @param title_resource  title resource id
   */
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
    TDLog.v( "My Dialog key down: code " + code );
    if ( code == KeyEvent.KEYCODE_MENU || code == KeyEvent.KEYCODE_VOLUME_UP ) {
      if ( mHelpPage != null ) {
        UserManualActivity.showHelpPage( mContext, mHelpPage );
      }
      return true;
    } else if ( code == KeyEvent.KEYCODE_BACK ) {
      onBackPressed(); // issue 167
      // ev.startTracking();
      return true;
    }
    return false;
  }

  @Override // issue 167
  public boolean onKeyUp( int code, KeyEvent ev )
  {
    TDLog.v("My Dialog key up: code " + code );
    if ( code == KeyEvent.KEYCODE_BACK ) {
      // if ( ev.isTracking() && ! ev.isCanceled() ) {
        return true;
      // }
    }
    return false;
  }
  

  // override in subclases
  @Override
  public void onBackPressed()
  {
    dismiss();
  }

  // public void onWindowAttributesChanged(WindowManager.LayoutParams params)
  // {
  //   TDLog.v("DIALOG onWindowAttributesChanged");
  // }

  // public Bundle onSaveInstanceState()
  // {
  //   TDLog.v("DIALOG onSaveInstanceState");
  //   return new Bundle();
  // }

  // public void onRestoreInstanceState(Bundle savedInstanceState)
  // {
  //   TDLog.v("DIALOG onRestoreInstanceState");
  // }

  // public void onContentChanged()
  // { 
  //   TDLog.v("DIALOG onContentChanged");
  // }

  public void onAttachedToWindow()
  {
    // TDLog.v("DIALOG onAttachedToWindow");
    if ( mApp != null ) mApp.pushDialog( this );
  }

  public void onDetachedFromWindow()
  {
    // TDLog.v("DIALOG onDetachedFromWindow");
    if ( mApp != null ) mApp.popDialog( );
  }

  /** (re)do the initialization
   * @param landscape   whether screen is landscape
   * @note if doInit select the content view by the orientation, it must invalidate the top-view
   */
  public void doInit( boolean landscape ) { /* nothing */ }

  /** @return a resource string
   * @param r   resource id
   */
  protected String resString( int r ) { return mContext.getResources().getString( r ); }

  /** @return true if two edittetxt contains equal strings
   * @param e1  first edittext (FROM)
   * @param e2  second edittext (TO)
   */
  protected static boolean sameString( EditText e1, EditText e2 )  // protected should be enough
  {
    if ( e1.getText() == null ) return false;
    String s1 = TDUtil.toStationFromName( e1.getText().toString() );
    if ( s1.length()== 0 ) return false;
    if ( e2.getText() == null ) return false;
    return s1.equals( TDUtil.toStationToName( e2.getText().toString() ) );
  }

  @Override
  public void setTitle( int res )
  {
    TextView t = (TextView)findViewById( R.id.title );
    if ( t == null ) {
      super.setTitle( res );
    } else {
      t.setText( mContext.getResources().getString( res ) );
    }
  }

  @Override
  public void setTitle( CharSequence tit )
  {
    TextView t = (TextView)findViewById( R.id.title );
    if ( t == null ) {
      super.setTitle( tit );
    } else {
      t.setText( tit );
    }
  }

  /** set the state of a checkbox
   * @param cb    checkbox
   * @param state new checkbox state
   */
  protected void setState( MyCheckBox cb, boolean state )
  {
    if ( cb != null ) cb.setState( state );
  }

  /** @return true if s checkbox is not null and checked
   * @param cb    checkbox
   */
  protected boolean isChecked(  MyCheckBox cb ) { return cb != null && cb.isChecked(); }

}
