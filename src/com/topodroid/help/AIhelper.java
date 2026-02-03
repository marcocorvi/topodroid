/** @file AIhelper.java
 */
package com.topodroid.help;

import com.topodroid.utils.TDLog;

import android.os.Handler;
import android.os.Looper;
// import android.content.Content.Builder;
import android.widget.TextView;

/* */
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
// */

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

public class AIhelper // extends AsyncTask< String, Void, String >
{
  boolean mDoReport = false;
  AIdialog mDialog;
  final String mUserKey;

  /* */
  private GenerativeModelFutures model;
  // */

  public AIhelper( AIdialog dialog, String user_key )
  {
    mDialog  = dialog;
    mUserKey = user_key;
  }
    
  void setModel( String model_name )
  {
    /* //
    Content systemInstruction = new Content.Builder()
            .addText("You are the AI assistant for TopoDroid, a cave surveying app. Answer concisely.")
            .build();
    GenerativeModel gm = new GenerativeModel( "gemini-1.5-flash", user_key, null, null, new RequestOptions(), null, null, systemInstruction );
    // */

    GenerativeModel gm = new GenerativeModel( model_name, mUserKey );
    this.model = GenerativeModelFutures.from( gm );
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

  public void ask( String user_prompt, TextView tv )
  {
    if ( mDoReport ) {
      execute( user_prompt, tv );
    }
  }

  public void execute( String user_prompt, TextView tv )
  {
    mDoReport = true;
    final WeakReference<TextView> textViewRef = new WeakReference<>(tv);
    /* //
    StringBuilder sb = new StringBuilder();
    sb.append( tv.getText().toString() ).append("\n").append(user_prompt);
    updateUI( textViewRef, sb.toString() );
    // */

    /* */
    String system_ctx = "You are the AI assistant for TopoDroid, the cave surveying app. Answer the following question concisely: ";
    Content content = new Content.Builder().addText( system_ctx + user_prompt ).build();
    ListenableFuture< GenerateContentResponse > response = model.generateContent( content );
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
            }, new Executor() {
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
          tv.setText(message);
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
  



