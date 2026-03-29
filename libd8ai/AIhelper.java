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
import android.app.ActivityManager;
import android.content.Context;
// import android.content.Content.Builder;
import android.widget.TextView;
import android.view.View;

import android.text.TextPaint;
import android.text.Spanned;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.method.LinkMovementMethod;

import android.view.View;

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

  /* */
  static private String  mModelName = null;
  static private GenerativeModelFutures model = null;
  static private ChatFutures chat = null;
  // */

  // private Runnable mUpdater = null;

  public AIhelper( Context ctx, AIdialog dialog, String user_key, String page )
  {
    TDLog.v("AI helper: cstr" );
    mContext = ctx;
    mDialog  = dialog;
    mUserKey = user_key;
    mRefPage = page;
    // mUpdater = updater;
    // readManualIndex();
  }

  /** set the Gemini model
   * @param model_name   name of the Gemini model
   * @note this method must be called everytime a question is asked
   * N.B. chat and model can be saved between instanatiations if the model does not change
   */
  void setModel( String model_name, int r_ai_model )
  {
    TDLog.v("AI helper: set model " + model_name );
    if ( ! model_name.equals( mModelName ) ) {
      mModelName = model_name;
      GenerationConfig.Builder gcb = new GenerationConfig.Builder();
      gcb.temperature = 0.2f; // 0.0 to 1.0 or 2.0
      gcb.topK = 2; // 1 to 40
      gcb.topP = 0.95f; // 0.0 to 1.0 allow some natural language
      gcb.maxOutputTokens = 1024; // 1 token = 4 chars
      GenerationConfig gc = gcb.build();
      GenerativeModel gm = new GenerativeModel( model_name, mUserKey, gc, null, new RequestOptions() );
      model = GenerativeModelFutures.from( gm );
      chat = null; // force chat rebuild
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
      cb2.addText( mContext.getResources().getString( r_ai_model ) );
      Content modelInstruction  = cb2.build();

      List< Content > history = new ArrayList<>();
      history.add( mystemInstruction );
      history.add( modelInstruction );
      chat = model.startChat( history );
    }
  }

  /** reset the chat so that the next time a question is posed the chat is rebuiilt
   */
  static void resetChat()
  {
    chat = null;
  }

  /** ask a question
   * @param user_prompt   user question
   * @param tv            textview for the response
   * @param local_context whether to use local_context
   */
  public void ask( String user_prompt, AIdialog dialog, boolean local_context )
  {
    TDLog.v("AI helper: ask " + user_prompt );
    if ( chat != null ) {
      final String error_format = mContext.getResources().getString( R.string.ai_error );
      final WeakReference<AIdialog> dialogRef = new WeakReference<>(dialog);

      StringBuilder sb = new StringBuilder();
      if ( local_context && mRefPage != null ) sb.append("CONTEXT: The user is currently reading the manual page: \'").append( mRefPage ).append("\'\n");
      sb.append("QUESTION: ").append( user_prompt ).append("\n");
 
      Content content = new Content.Builder().addText( sb.toString() ).build();

      // single-shot query
      // ListenableFuture< GenerateContentResponse > response = model.generateContent( content );

      ListenableFuture< GenerateContentResponse > response = chat.sendMessage( content ); 

      Futures.addCallback(response,
        new FutureCallback<GenerateContentResponse>() {
          @Override
          public void onSuccess(GenerateContentResponse response) {
            updateUI( dialogRef, response.getText() );
          }

          @Override
          public void onFailure(Throwable t) {
            updateUI( dialogRef, String.format( error_format, t.getMessage() ) );
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

  private void updateUI( WeakReference<AIdialog> ref, String message )
  {
    // Ensure we run on the Main UI Thread
    new Handler( Looper.getMainLooper() ).post( new Runnable() {
      @Override public void run() {
        if ( ref == null ) return;
        AIdialog dialog = ref.get();
        dialog.resetCanSubmit();
        dialog.showResponse( message );
      }
    } );
  }

  /** validate an API key
   * @param api_key   API key
   * @param callback  validation callback
   */
  public static void validateApiKey( final String api_key, final ValidationCallback callback )
  {
    TDLog.v("AI helper: validate API key " + api_key );
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
