/** @file PrefAIdialog.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid preferences AI dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.prefs;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDFile;
// import com.topodroid.utils.TDString;
// import com.topodroid.ui.MyDialog;
import com.topodroid.help.AIdialog;
import com.topodroid.help.IHelpViewer;
import com.topodroid.help.PageLink;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.R;

import java.util.ArrayList;

import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;

import android.widget.TextView;
// import android.widget.Spinner;
// import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;;

import android.view.View;
// import android.view.View.OnClickListener;

import android.text.TextPaint;
import android.text.Spanned;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.method.LinkMovementMethod;
import android.text.SpannableStringBuilder;

import  java.io.InputStream;
import  java.io.InputStreamReader;
import  java.io.BufferedReader;
import  java.io.IOException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefAIdialog extends AIdialog
{
  private boolean mWithGemini = true;

  /* GEMMA3 
  int mInputType = 0;
  EditText     mET = null;
  CheckBox     mCB = null;
  Spinner      mSP = null;
  Button mBtOk = null;
  // END GEMMA3 */

  /** cstr
   */
  public PrefAIdialog( Context context, IHelpViewer parent, String user_key, String page )
  {
    super( context, parent, user_key, page, R.string.ai_model_settings );
    mPattern = Pattern.compile( "\\[([A-Z_]+)=([a-zA-Z0-9.]+)]" );
    mRtitle = R.string.title_ai_dialog_pref;

    TDLog.v("Pref AI dialog page " + page );

    if ( user_key != null ) {
      mWithGemini = true;
      if ( mSystemInstruction == null ) {
        mSystemInstruction = getSettingText( context );
        // TDLog.v("PrefAI System instr. length " + mSystemInstruction.length() );
      }
    /* IF GEMMA3
    } else { 
      mWithGemini = false;
      if ( mLLMsystemInstruction == null ) {
        loadLLMsettingText( context );
      }
    // ELSE GEMMA3 */
    } else {
      mWithGemini = false;
    // END GEMMA3 */
    }
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    /* GEMMA3
    mLayout = (LinearLayout)findViewById( R.id.pref_layout );
    mET = (EditText)findViewById( R.id.pref_string );
    mCB = (CheckBox)findViewById( R.id.pref_bool );
    mSP = (Spinner)findViewById( R.id.pref_array );
    mBtOk = (Button)findViewById( R.id.pref_ok );
    mLayout.setVisibility( View.GONE );
    // END GEMMA3 */
  }

  /* GEMMA3 
  private void loadLLMsettingText( Context ctx )
  {
    mLLMsystemInstruction = new String[ mLLMindex.length ];
    int idx = 0;
    StringBuilder sb = new StringBuilder();
    try {
      InputStream is = null;
      if ( TDFile.existPrivateFile( null, "llm-settings.txt" ) ) {
        is = TDFile.getPrivateFileInputStream( null, "llm-settings.txt" );
      } else {
        is = ctx.getAssets().open("ai/llm-settings.txt");
      }
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String line;
      int cnt = 1;
      while ( ( line = br.readLine() ) != null ) {
        if ( ! line.startsWith("#") ) {
          int pos = line.indexOf(": ");
          if ( pos < 0 ) { TDLog.e("BAD end-key " + line ); continue; }
          String key = line.substring(0,pos);
          String rem = line.substring( pos+5 ); // skip activity level
          if ( rem.startsWith("Int") ) {
            pos = rem.indexOf(",");
            if ( rem.startsWith("Int[") ) pos = rem.indexOf("],");
          } else if ( rem.startsWith("Float") ) {
            pos = rem.indexOf(",");
            if ( rem.startsWith("Float[") ) pos = rem.indexOf("],");
          } else if ( rem.startsWith("Enum{") ) {
            pos = rem.indexOf("},");
          } else {
            pos = rem.indexOf(",");
          }
          if ( pos < 0 ) { TDLog.e("BAD end-type " + line ); continue; }
          String type = rem.substring(0, pos);
          rem = rem.substring( pos+3 ); // glob double quotes
          pos = rem.indexOf("\", ");
          if ( pos < 0 ) { TDLog.e("BAD end-name " + line ); continue; }
          String name = rem.substring(0, pos);
          rem = rem.substring( pos+4 ); // glob double quotes
          pos = rem.indexOf("\"");
          if ( pos < 0 ) { TDLog.e("BAD end-desc " + line ); continue; }
          String desc = rem.substring(0, pos);
          sb.append( String.format("%d, KEY: \"%s\" | TYPE: %s | LABEL: \"%s | DESC: %s\n", cnt, key, type, name, desc ) );
          ++ cnt;
        } else {
          mLLMsystemInstruction[ idx ] = sb.toString();
          sb = new StringBuilder();
          idx ++;
        }
      }
      br.close();
      mLLMsystemInstruction[ idx ] = sb.toString();
    } catch (IOException e ) {
      TDLog.e("Error reading settings.txt " + e.getMessage() );
    }
  }
  // END GEMMA3 */

  private String getSettingText( Context ctx )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( ctx.getResources().getString( R.string.ai_settings ) )
      .append( ctx.getResources().getString( R.string.ai_begin_settings ) );
    try {
      InputStream is = null;
      if ( TDFile.existPrivateFile( null, "settings.txt" ) ) {
        is = TDFile.getPrivateFileInputStream( null, "settings.txt" );
      } else {
        is = ctx.getAssets().open("ai/settings.txt");
      }
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String line;
      while ( ( line = br.readLine() ) != null ) {
        line = line.trim();
        if ( line.isEmpty() ) continue;
        if ( line.startsWith( "END_SETTING" ) ) break;
        sb.append( line ).append("\n");
      }
      br.close();
    } catch (IOException e ) {
      TDLog.e("Error reading settings.txt " + e.getMessage() );
    }
    sb.append( ctx.getResources().getString( R.string.ai_end_settings ) );
    TDLog.v("PrefAI Settings length " + sb.length() );
    return sb.toString();
  }

  @Override
  public void openOnParent( String page )
  {
    dismiss();
    TDLog.v("open <" + page + ">" );
    mParent.showManPage( page );
  }

  /** @return the totle for a filename - or filename if there is no title
   * @param filename  filename
   */
  private String getTitle( String filename )
  {
    return filename;
  }

  @Override
  public void showResponse( String message )
  {
    if ( mAnswer == null ) return;
    if ( message == null || message.isEmpty() ) return;
    if ( mWithGemini ) {
      showGeminiResponse( message );
    /* GEMMA3
    } else {
      showGemmaResponse( message );
    // END GEMMA3 */
    }
  }

  /* GEMMA3
  // handling of GEMMA3 response has not been finished: it should start a dialog to show the setting
  private void showGemmaResponse( String message )
  {
    boolean done = false;
    int pos = message.indexOf("{");
    if ( pos >= 0 ) {
      int qos = message.indexOf( "}", pos );
      String msg = message.substring( pos+1, qos );
      // TODO process msg
      // label=... key=... N.B. label can contain spaces
      String[] vals = msg.split( " " );
      for ( int i = 0; i < vals.length; ++i ) TDLog.v( "Item " + i + " <" + vals[i] + ">" );
      if ( vals.length == 2 ) {
        pos = vals[0].indexOf("=");
        String key = vals[0].substring(pos+1).replaceAll("[^A-Z_]", "");
        pos = vals[1].indexOf("=");
        String value = vals[1].substring(pos+1).replaceAll("\"", "");
        TDLog.v("KEY <" + key + "> VALUE <" + value + ">" );

        TDPrefKey setting = TDPrefKey.getPrefKey( key );
        if ( setting != null ) {
          mLayout.setVisibility( View.VISIBLE );
          mET.setVisibility( View.GONE );
          mCB.setVisibility( View.GONE );
          mSP.setVisibility( View.GONE );
          // TDPrefKey[] keyset = TDPrefKey.getPrefKeySet( key );
          int category = setting.cat;
          String title = mContext.getResources().getString( setting.title );
          String summary = mContext.getResources().getString( setting.summary );
          TDLog.v("key " + key + " category " + category );
          // get the value and update 
          TDPrefHelper hlp = new TDPrefHelper( mContext );
          SharedPreferences prefs = hlp.getSharedPrefs();
          ((TextView)findViewById( R.id.pref_title )).setText( title );
          ((TextView)findViewById( R.id.pref_summary )).setText( summary );
          mBtOk.setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
              String str_val = null;
              if ( mInputType == TDPrefKey.STR ) { // EditText
                str_val = mET.getText().toString();
                TDLog.v("Input STRING " + str_val );
              } else if ( mInputType == TDPrefKey.BOOL ) { // CheckBox
                boolean bool_val = mCB.isChecked();
                str_val = bool_val ? "TRUE" : "FALSE";
                TDLog.v("Input BOOL " + str_val );
              } else if ( mInputType == TDPrefKey.ARR ) { // ARRAY
                int array_pos = mSP.getSelectedItemPosition();
                String[] options = mContext.getResources().getStringArray( setting.label );
                String[] values = mContext.getResources().getStringArray( setting.value );
                str_val = values[array_pos];
                TDLog.v("Input ARRAY " + str_val + " (" + options[array_pos] + ")" );
              } else if ( mInputType == TDPrefKey.COL ) { // COLOR
              } else if ( mInputType == TDPrefKey.BTN ) { // BUTTON
              } else if ( mInputType == TDPrefKey.XTR ) { // EXTRA
              }
              mLayout.setVisibility( View.GONE );
              // if ( str_val != null ) TDSetting.updatePreference( hlp, category, key, str_val );
            }
          } );

          ((Button)findViewById( R.id.pref_no )).setOnClickListener( new View.OnClickListener() {
            public void onClick( View v ) {
              mLayout.setVisibility( View.GONE );
            }
          } );
          mInputType = -1;
          boolean implemented = false;
          switch (setting.type ) {
            case TDPrefKey.LONG:
            case TDPrefKey.FLT:
            case TDPrefKey.STR:
              mInputType = TDPrefKey.STR;
              mET.setVisibility( View.VISIBLE );
              if ( value != null ) mET.setText( value );
              implemented = true;
              break;
            case TDPrefKey.BOOL:
              mInputType = TDPrefKey.BOOL;
              mCB.setVisibility( View.VISIBLE );
              mCB.setChecked( (value != null && value.toUpperCase().charAt(0) == 'T' ) );
              implemented = true;
              break;
            case TDPrefKey.ARR:
              mInputType = TDPrefKey.ARR;
              mSP.setVisibility( View.VISIBLE );
              String[] options = mContext.getResources().getStringArray( setting.label );
              String[] values = mContext.getResources().getStringArray( setting.value );
              mSP.setAdapter( new ArrayAdapter<>( mContext, R.layout.menu, options ) );
              if ( values.length == options.length ) {
                for ( int k = 0; k < values.length; ++ k ) {
                  if ( value.equalsIgnoreCase( values[k] ) ) {
                    mSP.setSelection( k );
                    break;
                  }
                }
              }
              implemented = true;
              break;
            case TDPrefKey.COL:
              mInputType = TDPrefKey.COL;
              break;
            case TDPrefKey.BTN:
              mInputType = TDPrefKey.BTN;
              break;
            case TDPrefKey.XTR:
              mInputType = TDPrefKey.XTR;
              break;
            default:
          //   /// case FWRD not considered
          }
          if ( implemented ) {
            mAnswer.setText( "Setting \"" + title + "\" - " + summary + ": " + key + " = " + value + "\nPress \"Apply\" to apply the suggested value" );
          } else {
            mAnswer.setText( "Setting \"" + title + "\" - " + summary + ": " + key + " = " + value + "\nNot implemented" );
            mBtOk.setVisibility( View.GONE );
            // mBtNo.setVisibility( View.GONE );
          }
        }
      }
    }
    if ( ! done ) mAnswer.setText( message );
  }
  // END GEMMA3 */
      

  private void showGeminiResponse( String message )
  {
    ArrayList< PageLink > pages = new ArrayList<>();
    // SpannableString ssb = new SpannableString( message ); // immutable text
    SpannableStringBuilder ssb = new SpannableStringBuilder( message ); // mutable text
    Matcher matcher = mPattern.matcher( message );
    // TDLog.v("Message: " + message );
    int len = message.length();
    int offset = 0;
    while ( offset < len && matcher.find( offset ) ) {
      int cnt = matcher.groupCount();
      if ( cnt == 2 ) {
        TDLog.v("Found 2 " + matcher.start() + "-" + matcher.end() + ": " + matcher.group( 1 ) + " " + matcher.group( 2 ) );
        PageLink page = new PageLink( matcher.start(), matcher.end(), matcher.group( 1 ) + ":" + matcher.group( 2 ) );
        page.mLinkText = matcher.group( 2 );
        pages.add( page );
      }
      offset = matcher.end() + 1;
    }
    offset = 0;
    for ( PageLink page : pages ) {
      int linkStart = offset + page.mStart;
      int linkEnd   = offset + page.mStart + page.mLinkText.length();
      ssb.delete( linkStart, offset + page.mEnd );
      ssb.insert( linkStart, page.mLinkText );
      offset += page.mLinkText.length() - ( page.mEnd - page.mStart );
      ClickableSpan cs = new ClickableSpan() {
        @Override public void onClick( View v ) { openOnParent( page.mFilename ); }
        @Override public void updateDrawState( TextPaint ds ) {
          super.updateDrawState( ds );
          ds.setUnderlineText( true );
          ds.setColor( TDColor.FIXED_BLUE );
        }
      };
      ssb.setSpan( cs, linkStart, linkEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
    }
    mAnswer.setText( ssb );
    mAnswer.setMovementMethod( LinkMovementMethod.getInstance() );

  }

}

