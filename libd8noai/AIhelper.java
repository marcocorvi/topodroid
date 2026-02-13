/** @file AIhelper.java
 */
package com.topodroid.help;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDColor;
// import com.topodroid.TDX.TDToast;
// import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.R;

// import android.os.Handler;
// import android.os.Looper;
import android.content.Context;
// import android.content.Content.Builder;
import android.widget.TextView;
import android.view.View;

// import android.text.TextPaint;
// import android.text.Spanned;
// import android.text.SpannableString;
// import android.text.SpannableStringBuilder;
// import android.text.style.ClickableSpan;
// import android.text.method.LinkMovementMethod;

// for VertexAI replace ai.client.generativeai with firebase.vertexai
// import com.google.ai.client.generativeai.GenerativeModel;
// import com.google.ai.client.generativeai.java.GenerativeModelFutures;
// import com.google.ai.client.generativeai.java.ChatFutures;
// import com.google.ai.client.generativeai.type.Content;
// import com.google.ai.client.generativeai.type.GenerationConfig;
// import com.google.ai.client.generativeai.type.RequestOptions;
// import com.google.ai.client.generativeai.type.GenerateContentResponse;
// import com.google.ai.client.generativeai.type.CountTokensResponse;
// import com.google.common.util.concurrent.FutureCallback;
// import com.google.common.util.concurrent.Futures;
// import com.google.common.util.concurrent.ListenableFuture;
// */

// import java.lang.ref.WeakReference;
// import java.util.concurrent.Executor;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import java.util.List;
// import java.util.ArrayList;
// import java.util.HashMap;

// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.BufferedReader;
// import java.io.IOException;

public class AIhelper // extends AsyncTask< String, Void, String >
{
  AIdialog mDialog;
  private Context mContext;
  final String mUserKey;

  final String mRefPage; // refrence section of the user-manual
  // Pattern mPattern;

  public AIhelper( Context ctx, AIdialog dialog, String user_key, String page )
  {
    mContext = ctx;
    mDialog  = dialog;
    mUserKey = user_key;
    // mPattern = Pattern.compile( "\\[([^]]+\\.htm)\\]" );
    mRefPage = page;
  }

  /** set the Gemini model
   * @param model_name   name of the Gemini model
   * @note this method must be called everytime a question is asked
   * N.B. chat and model can be saved between instanatiations if the model does not change
   */
  void setModel( String model_name ) { }

  /** reset the chat so that the next time a question is posed the chat is rebuiilt
   */
  void resetChat() { }

  /** ask a question
   * @param user_prompt   user question
   * @param tv            textview for the response
   * @param local_context whether to use local_context
   */
  public void ask( String user_prompt, TextView tv, boolean local_context )
  {
    tv.setText( "AI support not enabled" );
  }

  /** API key validation callback
   */
  public interface ValidationCallback
  {
    public void onResult( boolean valid, String response );
  }

  /** validate an API key
   * @param api_key   API key
   * @param callback  validation callback
   */
  public static void validateApiKey( final String api_key, final ValidationCallback callback )
  {
    callback.onResult( false, "AI support not enable" );
  }

}
  



