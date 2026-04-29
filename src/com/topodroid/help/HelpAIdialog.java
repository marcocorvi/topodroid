/** @file HelpAIdialog.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid help dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.help;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDString;
import com.topodroid.util.TDColor;
import com.topodroid.util.TDAnalytics;
// import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.R;

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
import java.util.HashMap;

public class HelpAIdialog extends AIdialog
{

  // TODO list of help entries
  /** cstr
   */
  public HelpAIdialog( Context context, IHelpViewer parent, String user_key, String page )
  {
    super( context, parent, user_key, page, R.string.ai_model_manual );
    TopoDroidApp.updateAnalytic( TDAnalytics.AI_DIALOG );
    mPattern = Pattern.compile( "\\[([^]]+\\.htm)\\]" );
    mRtitle = R.string.title_ai_dialog;

    // TDLog.v("Help AI dialog: cstr man page " + page + " user key " + user_key );
    if ( mSystemInstruction == null ) {
      mSystemInstruction = getOrderedUserManual( context );
      // TDLog.v("HelpAI System instr. length " + mSystemInstruction.length() );
    }

    String lang = TDSetting.mLocale;
    // TDLog.v("HelpAI Jargon lang: <" + lang + ">" );
    if ( TDString.isNullOrEmpty( lang ) || lang.equals("en") ) {
      if ( mLang != null ) {
        mLang   = null;
        mJargon = null;
      }
    } else if ( ! lang.equals( mLang ) ) {
      mLang = lang;
      mJargon = getJargon( context, mLang );
      // TDLog.v("HelpAI jargon length " + mJargon.length() );
    }
    if ( mNames == null ) {
      mNames = getNames( context );
      // TDLog.v("HelpAI names length " + mNames.length() );
    }
    mLocalContext = true;

    readManualIndex();
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
  }

  /** get the man pages in the order according to list.txt
   * @param ctx  context
   */
  private String getOrderedUserManual( Context ctx )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( ctx.getResources().getString( R.string.ai_user ) )
      .append( ctx.getResources().getString( R.string.ai_begin_manual ) );
    try {
      InputStream is = ctx.getAssets().open("man/list.txt");
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String filename;
      while ( ( filename = br.readLine() ) != null ) {
        filename = filename.trim();
        if ( filename.isEmpty() || ! filename.endsWith(".htm") ) continue;
        try {
          InputStream fis = ctx.getAssets().open("man/" + filename );
          int size = fis.available();
          byte[] buffer = new byte[ size ];
          fis.read( buffer );
          String content = new String( buffer, "UTF-8" );
          sb.append("\r\n-- SOURCE_FILE ").append( filename ).append(" --\n");
          sb.append( content );
        } catch (IOException e ) {
          TDLog.e("Could not find man page " + filename );
        }
        // TDLog.v("Read man page " + filename );
      }
      br.close();
    } catch (IOException e ) {
      TDLog.e("Error reading list.txt " + e.getMessage() );
    }
    sb.append( ctx.getResources().getString( R.string.ai_end_manual ) );
    // TDLog.v("HelpAI User manual length " + sb.length() );
    return sb.toString()
           .replaceAll( "<!--(.*)-->", "" )
           .replaceAll( "(?i)<i>(.*?)</i>", "*$1*" )
           .replaceAll( "(?i)<b>(.*?)</b>", "**$1**" )
           .replaceAll( "<p>", "\n" )
           .replaceAll( "</p>", "" )
           .replaceAll( "<br/?>", "\n" )
           .replaceAll( "<(?!a|/a|\\*)[^>]+>", "");
  }

  /** get the jargon disctionary
   * @param ctx  context
   * @param lang language (2-char ISO code lowercase)
   */
  private String getJargon( Context ctx, String lang )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( ctx.getResources().getString( R.string.ai_jargon ), lang ) ).append("\n");
    try {
      InputStream is = ctx.getAssets().open("ai/dict.txt");
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String line;
      while ( ( line = br.readLine() ) != null ) {
        line = line.trim();
        if ( line.startsWith("{") ) continue;
        int pos = line.indexOf(":{");
        String term = line.substring(0,pos);
        String translation = null;
        while ( ( line = br.readLine() ) != null ) {  
          line = line.trim();
          if ( line.startsWith("}") ) {
            if ( translation != null ) { // add term-translation
              sb.append("- ").append( term ).append(" -> ").append( translation ).append("\n");
              // TDLog.v("- " + term + " -> " + translation );
            }
            break;
          } 
          if ( translation == null && line.startsWith( lang ) ) {
            pos = line.indexOf(": ");
            if ( pos + 2 < line.length() ) translation = line.substring( pos + 2 );
          }
        }
      }
    } catch (IOException e ) {
      TDLog.e("Error reading list.txt " + e.getMessage() );
    }
    // TDLog.v("HelpAI Jargon length " + sb.length() );
    return sb.toString();

  }

  /** get the disctionary of proper names
   * @param ctx  context
   */
  private String getNames( Context ctx )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( ctx.getResources().getString( R.string.ai_names ) ).append("\n");
    try {
      InputStream is = ctx.getAssets().open("ai/names.txt");
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String line;
      while ( ( line = br.readLine() ) != null ) {
        line = line.trim();
        if ( line.length() > 0 ) {
          sb.append("- ").append( line );
        }
      }
    } catch (IOException e ) {
      TDLog.e("Error reading list.txt " + e.getMessage() );
    }
    // TDLog.v("HelpAI Names length " + sb.length() );
    return sb.toString();
  }

  @Override
  public void openOnParent( String page )
  {
    dismiss();
    mParent.showManPage( page );
  }

  /** read the map filenames to titles
   */
  private void readManualIndex() // this is not sttaic because it needs Context ...
  {
    if ( mManualIndex != null ) return;
    Pattern pattern = Pattern.compile( "<a\\s+href=\"([^\"]+\\.htm)\">([^<]+)<\\/a>" );
    mManualIndex = new HashMap<>();
    try {
      InputStream is = mContext.getAssets().open("man/manual16.htm");
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String line;
      while ( ( line = br.readLine() ) != null ) {
        line = line.trim();
        if ( ! line.startsWith("<a href") ) continue;
        Matcher matcher = pattern.matcher( line );
        if ( matcher.find() ) {
          mManualIndex.put( matcher.group(1), matcher.group(2) );
        }
      }
      is.close();
    } catch ( IOException e ) {
      TDLog.e("Error reading manual16: " + e.getMessage() );
    }
  }

  /** @return the totle for a filename - or filename if there is no title
   * @param filename  filename
   */
  private String getTitle( String filename )
  {
    if ( mManualIndex == null ) return filename;
    String title = mManualIndex.get( filename );
    return ( title == null )? filename : title;
  }

  @Override
  public void showResponse( String message )
  {
      TDLog.v("Help AI dialog: response " + message );
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
      // int cnt = matcher.groupCount();
      // if ( cnt == 1 ) {
        // TDLog.v("Found " + matcher.start() + "-" + matcher.end() + ": " + matcher.group( 1 ) );
        pages.add( new PageLink( matcher.start(), matcher.end(), matcher.group( 1 ) ) );
        // TDLog.v("Found " + matcher.start() + "-" + matcher.end() + ": " + matcher.group( 1 ) + " " + matcher.group( 2 ) );
        //  pages.add( new PageLink( matcher.start(), matcher.end(), matcher.group( 1 ) + ":" + matcher.group( 2 ) ) );
      // }
      offset = matcher.end() + 1;
    }
    for ( PageLink page : pages ) {
      TDLog.v("Help AI dialog: page " + page.mFilename );
      page.mLinkText = getTitle( page.mFilename );
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

