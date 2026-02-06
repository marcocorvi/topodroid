/** @file AIhelper.java
 */
package com.topodroid.help;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
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

public class AIhelper // extends AsyncTask< String, Void, String >
{
  boolean mDoReport = false;
  AIdialog mDialog;
  private Context mContext;
  final String mUserKey;

  final String mRefPage; // refrence section of the user-manual
  Pattern mPattern;

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
  }
    
  // N.B. chat and model can be saved between instanatiations if the model does not change
  void setModel( String model_name )
  {
    if ( ! model_name.equals( mModelName ) ) {
      mModelName = model_name;

      Content.Builder cb1 = new Content.Builder();
      cb1.setRole("user");
      cb1.addText( mDialog.mSystemInstruction );
      if ( mDialog.mJargon != null ) cb1.addText( mDialog.mJargon ); 
      Content mystemInstruction = cb1.build();

      Content.Builder cb2 = new Content.Builder();
      cb2.setRole("model");
      cb2.addText( mContext.getResources().getString( R.string.ai_model ) );
      Content modelInstruction  = cb2.build();

      List< Content > history = new ArrayList<>();
      history.add( mystemInstruction );
      history.add( modelInstruction );

      GenerationConfig.Builder gcb = new GenerationConfig.Builder();
      gcb.temperature = 0.2f; // 0.0 to 1.0 or 2.0
      gcb.topK = 2; // 1 to 40
      gcb.topP = 0.95f; // 0.0 to 1.0 allow some natural language
      gcb.maxOutputTokens = 800; // 1 token = 4 chars

      GenerationConfig gc = gcb.build();

      // GenerativeModel.Builder gmb = new GenerativeModel.Builder();
      // gmb.setModelname( model_name )
      //    .setApiKey( mUserKey )
      //    .setGenerationConfig( gc )
      //    .setSystemInstructions( mystemInstruction );
      // GenerativeModel gm = gmb.build();
      GenerativeModel gm = new GenerativeModel( model_name, mUserKey, gc, null, new RequestOptions() );

      this.model = GenerativeModelFutures.from( gm );
      this.chat = this.model.startChat( history );
    }
    // */

    // try {
    // } catch ( ClassNotFoundException e ) {
    //   TDLog.v("Class not found ");
    //   StackTraceElement[] stack = e.getStackTrace();
    //   for ( StackTraceElement s : stack ) TDLog.v( s.getClassName() + " " + s.getMethodName() );
    // }

    mDoReport = true;
  }


  public void stop() { mDoReport = false; }

  /**
   * @param user_prompt   user question
   * @param tv            textview for the response
   * @param local_context whether to use local_context
   */
  public void ask( String user_prompt, TextView tv, boolean local_context )
  {
    if ( mDoReport ) {
      execute( user_prompt, tv, local_context );
    }
  }

  public void execute( String user_prompt, TextView tv, boolean local_context )
  {
    mDoReport = true;
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
                    updateUI(textViewRef, response.getText());
                }

                @Override
                public void onFailure(Throwable t) {
                    updateUI(textViewRef, "AI error: " + t.getMessage());
                }
            }, new Executor() {  // context.getMainExecutor()
                @Override
                public void execute(Runnable command) {
                    command.run();
                }
            }
    );
    // */
  }

  private void updateUI( WeakReference<TextView> tvRef, String message )
  {
    // Ensure we run on the Main UI Thread
    new Handler( Looper.getMainLooper() ).post( new Runnable() {
      @Override public void run() {
        TextView tv = tvRef.get();
        if ( mDoReport && tv != null ) {
          // SpannableString ssb = new SpannableString( message );
          SpannableStringBuilder ssb = new SpannableStringBuilder( message );
          Matcher matcher = mPattern.matcher( message );
          TDLog.v("Message: " + message );
          int len = message.length();
          int offset = 0;
          while ( matcher.find( offset ) ) {
            final String filename = matcher.group( 1 );
            int start = matcher.start();
            int end   = matcher.end();
            TDLog.v("Start " + start + " End " + end + " Len " + len + " filename " + filename );
            ssb.delete( end - 1, end );
            ssb.delete( start, start + 1 );
            end -= 2;
            ClickableSpan cs = new ClickableSpan() {
              @Override public void onClick( View v ) { mDialog.openPageOnParent( filename ); }
              @Override public void updateDrawState( TextPaint ds ) {
                super.updateDrawState( ds );
                ds.setUnderlineText( true );
                ds.setColor( TDColor.FIXED_BLUE );
              }
            };
            ssb.setSpan( cs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
            offset  = end;
          }
          tv.setText( ssb );
          tv.setMovementMethod( LinkMovementMethod.getInstance() );

          // tv.setText(message);
        }
      }
    });
  }

/*
  @Override
  public String doInBackground( String ... user_prompts )
  {
      String responses = null;
      int cnt = user_prompts.length;
      return null;
  }
  // protected void onProgressUpdate( Void ... progress ) { }

  @Override
  public void onPostExecute( String ... responses )
  {
    if ( ! mDoReport ) return;
    if ( responses == null ) return;
    int cnt = responses.length;
    if ( cnt == 0 ) return;
    StringBuilder sb = new StringBuilder();
    for ( int i=0; i<cnt; ++i ) {
      sb.append( responses[i] );
      if ( i < cnt-1) sb.append("\n");
    }
    if ( mDialog != null ) mDialog.showResponse( sb.toString() );
  }
*/

}
  



