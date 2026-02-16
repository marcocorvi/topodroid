/** @file AIhelper.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid AI help assistant
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.help;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.TDX.TDToast;
// import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.R;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;
// import android.content.Content.Builder;
import android.widget.TextView;
import android.view.View;

import android.text.TextPaint;
import android.text.Spanned;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.method.LinkMovementMethod;

// for VertexAI replace ai.client.generativeai with firebase.vertexai
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.CountTokensResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
// */

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class AIhelper // extends AsyncTask< String, Void, String >
{
  AIdialog mDialog;
  private Context mContext;
  final String mUserKey;

  final String mRefPage; // refrence section of the user-manual
  Pattern mPattern;

  static private HashMap<String, String> mManualIndex = null;

  /* */
  static private String  mModelName = null;
  static private GenerativeModelFutures model = null;
  static private ChatFutures chat = null;
  // */

  public AIhelper( Context ctx, AIdialog dialog, String user_key, String page )
  {
    mContext = ctx;
    mDialog  = dialog;
    mUserKey = user_key;
    mPattern = Pattern.compile( "\\[([^]]+\\.htm)\\]" );
    mRefPage = page;
    readManualIndex();
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
    
  /** set the Gemini model
   * @param model_name   name of the Gemini model
   * @note this method must be called everytime a question is asked
   * N.B. chat and model can be saved between instanatiations if the model does not change
   */
  void setModel( String model_name )
  {
    if ( ! model_name.equals( mModelName ) ) {
      mModelName = model_name;
      GenerationConfig.Builder gcb = new GenerationConfig.Builder();
      gcb.temperature = 0.2f; // 0.0 to 1.0 or 2.0
      gcb.topK = 2; // 1 to 40
      gcb.topP = 0.95f; // 0.0 to 1.0 allow some natural language
      gcb.maxOutputTokens = 1024; // 1 token = 4 chars
      GenerationConfig gc = gcb.build();
      GenerativeModel gm = new GenerativeModel( model_name, mUserKey, gc, null, new RequestOptions() );
      this.model = GenerativeModelFutures.from( gm );
      this.chat = null; // force chat rebuild
    } 
    if ( this.chat == null ) {
      Content.Builder cb1 = new Content.Builder();
      cb1.setRole("user");
      cb1.addText( mDialog.mSystemInstruction );
      if ( mDialog.mJargon != null ) cb1.addText( mDialog.mJargon ); 
      if ( mDialog.mNames  != null ) cb1.addText( mDialog.mNames  ); 
      Content mystemInstruction = cb1.build();

      Content.Builder cb2 = new Content.Builder();
      cb2.setRole("model");
      cb2.addText( mContext.getResources().getString( R.string.ai_model ) );
      Content modelInstruction  = cb2.build();

      List< Content > history = new ArrayList<>();
      history.add( mystemInstruction );
      history.add( modelInstruction );
      this.chat = this.model.startChat( history );
    }
  }

  /** reset the chat so that the next time a question is posed the chat is rebuiilt
   */
  void resetChat()
  {
    this.chat = null;
  }

  /** ask a question
   * @param user_prompt   user question
   * @param tv            textview for the response
   * @param local_context whether to use local_context
   */
  public void ask( String user_prompt, TextView tv, boolean local_context )
  {
    if ( chat != null ) {
      final String error_format = mContext.getResources().getString( R.string.ai_error );
      final WeakReference<TextView> textViewRef = new WeakReference<>(tv);

      StringBuilder sb = new StringBuilder();
      if ( local_context && mRefPage != null ) sb.append("CONTTEXT: The user is currently reading the manual page: \'").append( mRefPage ).append("\'\n");
      sb.append("QUESTION: ").append( user_prompt ).append("\n");
 
      Content content = new Content.Builder().addText( sb.toString() ).build();

      // single-shot query
      // ListenableFuture< GenerateContentResponse > response = model.generateContent( content );

      ListenableFuture< GenerateContentResponse > response = chat.sendMessage( content ); 

      Futures.addCallback(response,
        new FutureCallback<GenerateContentResponse>() {
          @Override
          public void onSuccess(GenerateContentResponse response) {
            updateUI( textViewRef, response.getText() );
          }

          @Override
          public void onFailure(Throwable t) {
            updateUI( textViewRef, String.format( error_format, t.getMessage() ) );
          }
        },
        new Executor() {  // context.getMainExecutor()
          @Override
          public void execute(Runnable command) {
              command.run();
          }
        }
      );
    } else {
      TDToast.makeBad( R.string.ai_null_chat );
    }
  }

  private void updateUI( WeakReference<TextView> tvRef, String message )
  {
    // Ensure we run on the Main UI Thread
    new Handler( Looper.getMainLooper() ).post( new Runnable() {
      @Override public void run() {
        mDialog.resetCanSubmit();
        TextView tv = tvRef.get();
        if ( tv != null ) {
          ArrayList< PageLink > pages = new ArrayList<>();

          // SpannableString ssb = new SpannableString( message ); // immutable text
          SpannableStringBuilder ssb = new SpannableStringBuilder( message ); // mutable text
          Matcher matcher = mPattern.matcher( message );
          // TDLog.v("Message: " + message );
          int len = message.length();
          int offset = 0;
          while ( offset < len && matcher.find( offset ) ) {
            // TDLog.v("Found " + matcher.start() + "-" + matcher.end() + ": " + matcher.group( 1 ) );
            pages.add( new PageLink( matcher.start(), matcher.end(), matcher.group( 1 ) ) );
            offset = matcher.end() + 1;
          }
          for ( PageLink page : pages ) {
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
              @Override public void onClick( View v ) { mDialog.openPageOnParent( page.mFilename ); }
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
    });
  }

  /** validate an API key
   * @param api_key   API key
   * @param callback  validation callback
   */
  public static void validateApiKey( final String api_key, final ValidationCallback callback )
  {
    TDLog.v("Validate API key " + api_key );
    GenerativeModel gm = new GenerativeModel( "gemini-2.5-flash", api_key );
    GenerativeModelFutures model = GenerativeModelFutures.from( gm );
    Content.Builder cb = new Content.Builder();
    cb.addText("Hello");
    Content dummy = cb.build();
    ListenableFuture< CountTokensResponse > future = model.countTokens( dummy );
    Futures.addCallback( future,
      new FutureCallback< CountTokensResponse >() {
        @Override public void onSuccess( CountTokensResponse result ) { callback.onResult( true, null ); }
        @Override public void onFailure( Throwable t ) { callback.onResult( false, t.getMessage() ); }
      }, 
      new Executor() {  // context.getMainExecutor()
        @Override
        public void execute(Runnable command) { command.run(); }
      }
    );
  }

}
  



