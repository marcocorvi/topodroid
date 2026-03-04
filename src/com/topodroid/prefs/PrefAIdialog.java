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

// import android.widget.Button;
import android.widget.TextView;
// import android.widget.EditText;
// import android.widget.Spinner;
// import android.widget.ArrayAdapter;
// import android.widget.AdapterView;

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
  /** cstr
   */
  public PrefAIdialog( Context context, IHelpViewer parent, String user_key, String page )
  {
    super( context, parent, user_key, page, R.string.ai_model_settings );
    mPattern = Pattern.compile( "\\[([A-Z_]+)=([a-zA-Z0-9.]+)]" );
    mRtitle = R.string.title_ai_dialog_pref;

    TDLog.v("Pref AI dialog page " + page );

    if ( user_key != null ) {
      if ( mSystemInstruction == null ) {
        mSystemInstruction = getSettingText( context );
        // TDLog.v("PrefAI System instr. length " + mSystemInstruction.length() );
      }
    // } else { // GEMMA3
    //   if ( mLLMsystemInstruction == null ) {
    //     loadLLMsettingText( context );
    //   }
    }
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
  }

  /* GEMMA3
  private void loadLLMsettingText( Context ctx )
  {
    mLLMsystemInstruction = new String[ mLLMindex.length ];
    int idx = 0;
    StringBuilder sb = new StringBuilder();
    try {
      InputStream is = ctx.getAssets().open("ai/llm-settings.txt");
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String line;
      int cnt = 1;
      while ( ( line = br.readLine() ) != null ) {
        if ( ! line.startsWith("#") ) {
          int pos = line.indexOf(": ");
          if ( pos < 0 ) { TDLog.e("BAD end-key " + line ); continue; }
          String key = line.substring(0,pos);
          String rem = line.substring( pos+2 );
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
          sb.append( String.format("%d, KEY: \"%s\" | TYPE: %s | DESC: \"%s. %s\n", cnt, key, type, name, desc ) );
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
  */

  private String getSettingText( Context ctx )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( ctx.getResources().getString( R.string.ai_settings ) )
      .append( ctx.getResources().getString( R.string.ai_begin_settings ) );
    try {
      InputStream is = ctx.getAssets().open("ai/settings.txt");
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
    TextView tv = mAnswer;
    if ( tv == null ) return;
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
    tv.setText( ssb );
    tv.setMovementMethod( LinkMovementMethod.getInstance() );

  }

}

