/* @file MyKeyboard.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid numerical keyboard dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.CutNPaste;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;

// import android.app.Dialog;

import android.widget.EditText;

import android.content.Context;
// import android.os.Bundle;

import android.view.View;
import android.view.View.OnKeyListener;
// import android.view.View.OnClickListener; 
import android.view.View.OnFocusChangeListener; 
// import android.view.View.OnTouchListener; 
// import android.view.Gravity; 
// import android.view.Window; 
// import android.view.WindowManager; 
import android.view.inputmethod.InputMethodManager; 
import android.view.KeyEvent;
// import android.view.ViewGroup;

import android.text.InputType;
import android.text.Editable;
// import android.text.Layout;
import android.text.method.KeyListener; 

/* you need to override EditText::onTouchListener()
 */
public class MyKeyboard // FIXME DIALOG extends Dialog
                        implements OnKeyListener
                                 , OnKeyboardActionListener 
{
  public static final int FLAG_SIGN   = 0x01;
  public static final int FLAG_POINT  = 0x02;
  public static final int FLAG_DEGREE = 0x04;
  public static final int FLAG_LCASE  = 0x08;
  public static final int FLAG_NOEDIT = 0x10;
  public static final int FLAG_2ND    = 0x20;
  public static final int FLAG_POINT_SIGN        = 0x03; // FLAG_POINT | FLAG_SIGN
  public static final int FLAG_POINT_DEGREE      = 0x06; // FLAG_POINT | FLAG_DEGREE
  public static final int FLAG_POINT_SIGN_DEGREE = 0x07; // FLAG_POINT | FLAG_SIGN | FLAG_DEGREE
  public static final int FLAG_POINT_LCASE       = 0x0a; // FLAG_POINT | FLAG_LCASE
  public static final int FLAG_POINT_LCASE_2ND   = 0x2a; // FLAG_POINT | FLAG_LCASE | FLAG_2ND

  private boolean hasDegree;
  private boolean hasPoint;
  private boolean hasSign;
  private boolean hasLcase;

  private boolean inLcase;

  private Map< EditText, Integer > mFlags;

  private Context  mContext;
  private EditText mEdit; 
  private KeyboardView mKeyboardView;
  private Keyboard mKeyboard;
  private Keyboard mKeyboard1;
  private Keyboard mKeyboard2; // secondary kbd

  public EditText getEditText() { return mEdit; }
  // Context  getContext() { return mContext; }

  // Keyboard.Key mKeySign;
  // Keyboard.Key mKeyDegree;
  // Keyboard.Key mKeyMinute;
  // Keyboard.Key mKeyPoint;

  private int setFlags( EditText e )
  { 
    Integer j = mFlags.get( e );
    int flag = ( j == null )? 0 : j.intValue();
    hasSign   = ( ( flag & FLAG_SIGN ) == FLAG_SIGN );
    hasPoint  = ( ( flag & FLAG_POINT ) == FLAG_POINT );
    hasDegree = ( ( flag & FLAG_DEGREE ) == FLAG_DEGREE );
    hasLcase  = ( ( flag & FLAG_LCASE ) == FLAG_LCASE );
    return flag;
  }
    
  private Integer addFlag( EditText e, int f )
  {
    return mFlags.put( e, Integer.valueOf(f) );
  }

  public static void registerEditText( final MyKeyboard kbd, final EditText e, int flag )
  { 
    if ( kbd == null ) return;
    if ( kbd.addFlag( e, flag ) == null ) {
      // TDLog.v( "set listeners for " + e.getText().toString() + " flag " + flag );

      if ( ( flag & FLAG_NOEDIT ) == FLAG_NOEDIT ) {
        // e.setBackgroundColor( TDColor.MID_GRAY );
        e.setTextColor( TDColor.BLACK );
	e.setFocusable( false );
        // e.setBackgroundResource( R.drawable.edit_text );
      // } else {
        // e.setBackgroundResource( android.R.drawable.edit_text );
      }

      e.setOnFocusChangeListener( new OnFocusChangeListener() {
        @Override
        public void onFocusChange( View v, boolean hasFocus ) {
          CutNPaste.dismissPopup();
          // TDLog.v("onFocusChange() " + hasFocus + " " + e.getText().toString() );
          if ( hasFocus ) {
            InputMethodManager imm = (InputMethodManager)kbd.mContext.getSystemService( Context.INPUT_METHOD_SERVICE );
            imm.hideSoftInputFromWindow( e.getWindowToken(), 0 ); // NullPointerException
            if ( kbd.setEditText( e ) ) {
              // e.setBackgroundResource( R.drawable.textfield_selected );
              kbd.show( e );
              // kbd.getWindow().makeActive();
            } else {
              // e.setBackgroundColor( TDColor.MID_GRAY );
              kbd.setEditText( null );
            }
          } else {
	    EditText et = (EditText)v;
	    if ( et != null ) clearCursor( et );
            // e.setBackgroundResource( android.R.drawable.edit_text );
            kbd.setEditText( null );
          }
        }
      } );

      e.setOnClickListener( new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
          CutNPaste.dismissPopup();
          // TDLog.v("on click " + e.getText().toString() );
          // EditText et = (EditText) v; 
          // EditText e0 = kbd.getEditText();
          // if ( e0 != null ) {
          //   e0.setBackgroundResource( android.R.drawable.edit_text );
          // }
          e.setInputType( InputType.TYPE_NULL );
          if ( kbd.setEditText( e ) ) {
            // e.setBackgroundResource( android.R.drawable.edit_text );
            // e.setBackgroundResource( R.drawable.textfield_selected );
            kbd.show( e );
          } else {
            // e.setBackgroundColor( TDColor.MID_GRAY );
            kbd.setEditText( null );
          }
        }
      } );

      e.setInputType( InputType.TYPE_NULL );
      e.setCursorVisible( true );
    }

    // if you need the cursor in the EditText use this 
    // e.setOnTouchListener( new OnTouchListener() {
    //   @Override
    //   public boolean onTouch( View v, MotionEvent ev ) {
    //     EditText et = (EditText)v;
    //     int t = et.getInputType();
    //     et.setInputType( InputType.TYPE_NULL );
    //     et.onTouchEvent( ev );
    //     et.setInputType( t );
    //     return true;
    //   }
    // }

    // FIXME getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
    // FIXME
    // @Override public void onBackPressed() {
    //   if ( kbd != null ) {
    //     kbd.hide();
    //     kbd.dismiss();
    //     kbd = null;
    //   }
    // }    
  }

  public void hide()
  {
    mKeyboardView.setVisibility( View.GONE );
    mKeyboardView.setEnabled( false );
  }

  private void show( View v )
  {
    mKeyboardView.setVisibility( View.VISIBLE );
    mKeyboardView.setEnabled( true );
    // if ( v != null ) {
    // }
  }

  private void switchKeyboard( int flag )
  {
    if ( mKeyboard2 == null ) return;
    Keyboard next = ( (flag & FLAG_2ND) == FLAG_2ND )? mKeyboard2 : mKeyboard1;
    if ( next == mKeyboard ) return;
    boolean visible = mKeyboardView.isShown();
    if ( visible ) hide();
    mKeyboard = next;
    mKeyboardView.setKeyboard(mKeyboard);
    if ( ( flag & FLAG_LCASE ) == FLAG_LCASE ) {
      setKeyLabels( false );
    }
    if ( visible ) show( null );
  }

  private boolean isVisible()
  {
    return mKeyboardView.isShown();
  }

  public MyKeyboard( Context context, KeyboardView view, int kbdid1, int kbdid2 )
  {
    // FIXME DIALOG super( context );
    mContext = context;
    mEdit = null;
    mFlags = new HashMap< EditText, Integer >();

    // TDLog.v( "id1 " + kbdid1 + " " + kbdid2 );
    mKeyboardView = view;
    mKeyboard1 = new Keyboard( context, kbdid1 );
    mKeyboard2 = ( kbdid2 == -1 )? null : new Keyboard( context, kbdid2 );
    mKeyboard  = mKeyboard1;
    mKeyboardView.setKeyboard(mKeyboard);
    mKeyboardView.setEnabled(true);
    mKeyboardView.setOnKeyListener(this);
    mKeyboardView.setOnKeyboardActionListener(this);
    mKeyboardView.setPreviewEnabled( false );
  }
 
  private boolean setEditText( EditText e )
  {
    if ( mEdit == e ) return true;
    mEdit = e;
    if ( mEdit != null ) {
      int flag = setFlags( mEdit );
      if ( ( flag & FLAG_NOEDIT ) == FLAG_NOEDIT ) {
        mEdit = null;
        return false;
      }
      switchKeyboard( flag );
      setCursor( e );
      return true;
    } else {
      hide();
    }
    return false;
  }

  private static void setCursor( EditText e )
  {
    if ( e == null || TDSetting.mNoCursor ) return;
    Editable cs = e.getText();
    int len = cs.length();
    if ( len == 0 || cs.charAt(len-1) != CHAR_CURSOR ) {
      cs.append( CHAR_CURSOR );
    }
  }

  private void clearCursor( ) { clearCursor( mEdit ); }

  private static void clearCursor( EditText e )
  {
    if ( e == null || TDSetting.mNoCursor ) return;
    Editable cs = e.getText();
    int len = cs.length();
    if ( len > 0 && cs.charAt(len-1) == CHAR_CURSOR ) {
      cs.delete( len-1, len );
    }
  }


  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event)
  {
    // TDLog.v( "[*] Keycode " + keyCode );
    return false;
  }

  public final static char CHAR_MINUS      = (char)'-';
  public final static char CHAR_PLUS_MINUS = (char)177;
  public final static char CHAR_DEGREE     = (char)176;
  public final static char CHAR_MINUTE     = (char)39;
  public final static char CHAR_POINT      = (char)46;
  public final static char CHAR_CURSOR     = (char)95; // 95 underscore, 124 vert bar, 63 question mark 166 broken vert bar
  public final static String STR_DEGREE  = Character.toString( CHAR_DEGREE );
  public final static String STR_MINUTE  = Character.toString( CHAR_MINUTE );
  public final static String STR_POINT   = Character.toString( CHAR_POINT  );


  @Override
  public void onKey(int keyCode, int[] keyCodes) 
  {
    if ( keyCode == -4 /* Keyboard.KEYCODE_CANCEL */ 
      || keyCode == 4 /* hw KEYCODE_BACK */ ) {
      // TDLog.v( "Keycode " + keyCode + " CANCEL");
      hide();
      return;
    } else if ( keyCode == 256 ) {
      hide();
      if ( mEdit != null ) {
        // cannot use FOCUS_FORWARD
        View next = mEdit.focusSearch( View.FOCUS_RIGHT );
        if ( next != null ) next.requestFocus();
      }
    } else if ( mEdit != null ) {
      Editable editable = mEdit.getText() ; 
      int len = editable.length();
      if ( len > 0 && editable.charAt(len-1) == CHAR_CURSOR ) --len;
      if ( keyCode == -5 /* Keyboard.KEYCODE_DELETE */ ) {
        // TDLog.v( "Keycode " + keyCode + " DELETE len " + len + " text " + editable.toString());
        if ( len > 0 ) {
          editable.delete( len-1, len );
        }
      } else if ( keyCode == 177 ) { // +/-
        if ( hasSign ) {
          if ( len > 0 && editable.charAt(0) == CHAR_MINUS ) {
            editable.delete(0,1);
          } else {
            editable.insert(0, "-" );
          }
        }
      } else if ( keyCode == 257 ) { // Aa
        if ( hasLcase ) {
          setKeyLabels( ! inLcase );
        }  
      } else if ( keyCode == 176 ) { // degree
        if ( hasDegree && len > 0 ) {
          boolean ok = true;
          for ( int k=0; k<len; ++k) {
            char ch = editable.charAt(k);
            if ( ch == CHAR_MINUTE || ch == CHAR_POINT || ch == CHAR_DEGREE ) { ok = false; break; }
          }
          // if ( ok ) editable.append( CHAR_DEGREE );
          if ( ok ) editable.insert( len, STR_DEGREE );
        }
      } else if ( keyCode == 39 ) { // minute
        if ( hasDegree && len > 0 ) {
          boolean ok = true;
          for ( int k=0; k<len; ++k) {
            char ch = editable.charAt(k);
            if ( ch == CHAR_MINUTE || ch == CHAR_POINT ) { ok = false; break; }
          }
          // if ( ok ) editable.append( CHAR_MINUTE );
          if ( ok ) editable.insert( len, STR_MINUTE );
        }
      } else if ( keyCode == 46 ) { // point
        if ( hasPoint ) {
          boolean ok = true;
          for ( int k=0; k<len; ++k) {
            char ch = editable.charAt(k);
            if ( ch == CHAR_POINT ) { ok = false; break; }
          }
          // if ( ok ) editable.append( CHAR_POINT );
          if ( ok ) editable.insert( len, STR_POINT );
        }
      } else {
        // TDLog.v( "Keycode " + keyCode + " APPEND text " + editable.toString());
        char ch = (char) keyCode;
        if ( inLcase ) {
          if ( ch >= 65 && ch <= 90 ) {
            ch += 32;
          }
        }
        // editable.append( Character.toString(ch) ); 
        editable.insert( len, Character.toString(ch) ); 
      }
      // editable.cursorAt( editable.length() );
      // setTitle( editable.toString() );
    // } else {
    //   // TDLog.Debug( "Keycode " + keyCode );
    }
  }

  private void setKeyLabels( boolean lcase )
  {
   inLcase = lcase;
    List< Keyboard.Key > keys = mKeyboard.getKeys();
    for ( Keyboard.Key key : keys ) {
      int c = key.codes[0];
      int off = inLcase ? 32 : 0;
      if ( c >= 65 && c <= 90 ) {
        key.label = Character.toString( (char) (c+off) );
      }
    }
    mKeyboardView.invalidateAllKeys();
  }

  // -----------------------------------------------------------
  // KeyListener methods

  // @Override
  // public int getInputType() 
  // {
  //   return InputType.TYPE_NULL;
  // }

  // @Override
  // public boolean onKeyDown( View view, Editable text, int keyCode, KeyEvent event)
  // {
  //   TDLog.Debug( "onKeyDown keyCode=" + keyCode);
  //   return true; // key down handled
  // }

  // @Override
  // public boolean onKeyOther(View view, Editable text, KeyEvent event)
  // {
  //   TDLog.Debug( "onKeyOther keyCode=" + keyCode);
  //   return true; // key other handled
  // }

  // @Override
  // public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event)
  // {
  //   TDLog.Debug("onKeyUp keyCode=" + keyCode);
  //   return true; // key up handled
  // }

  // @Override
  // public void clearMetaKeyState(View arg0, Editable arg1, int arg2) { 
  // }
    

  // OnKeyboardActionListener methods

  @Override
  public void swipeUp()
  {
    // TDLog.Debug("swipeUp");
  }

  @Override
  public void swipeRight() 
  {
    // TDLog.Debug("swipeRight");
  }

  @Override
  public void swipeLeft() 
  {
    // TDLog.Debug("swipeLeft");
  }

  @Override
  public void swipeDown() 
  {
    // TDLog.Debug("swipeDown");
  }

  @Override
  public void onText(CharSequence text) 
  {
    // TDLog.Debug("onText? \"" + text + "\"");
  }

  @Override
  public void onRelease(int primaryCode) 
  {
    // TDLog.Debug("onRelease? primaryCode=" + primaryCode);
  }

  @Override
  public void onPress(int primaryCode) 
  {
    // TDLog.Debug("onPress? primaryCode=" + primaryCode);
  }

  public static void setEditable( EditText et, MyKeyboard kbd, KeyListener kl, boolean editable, int flag )
  {
    if ( TDSetting.mKeyboard ) {
      et.setKeyListener( null );
      et.setClickable( true );
      et.setFocusable( editable );
      if ( editable ) {
        registerEditText( kbd, et, flag );
        // et.setKeyListener( mKeyboard );
        // et.setBackgroundResource( android.R.drawable.edit_text );
        // et.setBackgroundResource( R.color.bg );
	// et.setTextColor( R.color.text );
      } else {
        registerEditText( kbd, et, flag | FLAG_NOEDIT );
        et.setBackgroundColor( TDColor.MID_GRAY );
      }
    } else {
      if ( editable ) {
        et.setKeyListener( kl );
        // et.setBackgroundResource( android.R.drawable.edit_text );
        et.setClickable( true );
        et.setFocusable( true );
      } else {
        // et.setFocusable( false );
        // et.setClickable( false );
        et.setKeyListener( null );
        et.setBackgroundColor( TDColor.MID_GRAY );
      }
    }
  }

  public static boolean close( MyKeyboard kbd )
  {
    if ( kbd == null ) return false;
    kbd.clearCursor();
    if ( kbd.isVisible() ) {
      kbd.hide();
      return true;
    }
    return false;
  }

}

