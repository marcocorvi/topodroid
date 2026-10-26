/* @file SilexGisSettingsDialog.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid SilexGIS server access: URL, personal access key, test, forget
 *
 * The key is created by the user in SilexGIS (Settings, Access keys) and pasted here.
 * Forget removes it only from this device: the key is revoked in SilexGIS.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.content.res.Resources;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import android.view.View;

import org.json.JSONObject;

public class SilexGisSettingsDialog extends MyDialog
                                    implements View.OnClickListener
{
  private final String mReason;         // why the dialog has been opened (null if from the settings)
  private final Runnable mOnConfigured; // run after OK if the access is set (can be null)

  private EditText mETurl;
  private EditText mETkey;
  private TextView mTVresult;
  private Button   mBtnTest;

  private String mTestedUrl = null; // URL and key of the last successful test
  private String mTestedKey = null;
  private String mAccount   = null; // account found by the last successful test

  /** cstr
   * @param context       context
   * @param reason        message shown on top of the dialog (null for none)
   * @param on_configured run after OK when the server URL and the access key are set (can be null)
   */
  public SilexGisSettingsDialog( Context context, String reason, Runnable on_configured )
  {
    super( context, null, 0 ); // 0: no help resource
    mReason       = reason;
    mOnConfigured = on_configured;
  }

  // ---------------------------------------------------------------
  // static helpers for the callers

  /** @return true if the server URL and the access key are set, otherwise open the dialog and return false
   * @param context       context
   * @param on_configured run after the user sets the access in the dialog (can be null)
   */
  public static boolean ensureConfigured( Context context, Runnable on_configured )
  {
    SilexGisClient client = new SilexGisClient( TDSetting.mSilexGisUrl, TDSetting.mSilexGisKey );
    if ( client.isConfigured() && SilexGisClient.checkBaseUrl( client.getBaseUrl() ) == SilexGisClient.URL_OK && SilexGisClient.checkKey( TDSetting.mSilexGisKey ) ) return true;
    (new SilexGisSettingsDialog( context, context.getResources().getString( R.string.silexgis_not_configured ), on_configured )).show();
    return false;
  }

  /** open the dialog after the server refused the access key (401)
   * @param context       context
   * @param on_configured run after the user sets the access in the dialog (can be null)
   */
  public static void showKeyRefused( Context context, Runnable on_configured )
  {
    (new SilexGisSettingsDialog( context, context.getResources().getString( R.string.silexgis_key_refused ), on_configured )).show();
  }

  /** @return the user message of a failed call
   * @param res    resources
   * @param r      call result
   */
  public static String errorMessage( Resources res, SilexGisClient.Result r )
  {
    switch ( r.kind ) {
      case SilexGisClient.NOT_CONFIGURED:   return res.getString( R.string.silexgis_not_configured );
      case SilexGisClient.BAD_URL:
        return res.getString( ( r.url == SilexGisClient.URL_CLEARTEXT )? R.string.silexgis_url_cleartext : R.string.silexgis_url_invalid );
      case SilexGisClient.KEY_REFUSED:      return res.getString( R.string.silexgis_key_refused );
      case SilexGisClient.KEY_NOT_ACCEPTED: return res.getString( R.string.silexgis_key_not_accepted );
      case SilexGisClient.NETWORK:          return String.format( res.getString( R.string.silexgis_network ), r.message() );
      case SilexGisClient.BAD_RESPONSE:     return res.getString( R.string.silexgis_bad_response );
      case SilexGisClient.BAD_KEY:          return res.getString( R.string.silexgis_key_invalid );
    }
    return String.format( res.getString( R.string.silexgis_problem ), r.status, r.message() ); // PROBLEM
  }

  // ---------------------------------------------------------------

  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout( R.layout.silexgis_settings_dialog, R.string.silexgis_title );

    TextView reason = (TextView) findViewById( R.id.silexgis_reason );
    if ( mReason != null ) {
      reason.setText( mReason );
      reason.setVisibility( View.VISIBLE );
    }
    mETurl    = (EditText) findViewById( R.id.silexgis_url );
    mETkey    = (EditText) findViewById( R.id.silexgis_key );
    mTVresult = (TextView) findViewById( R.id.silexgis_result );
    mETurl.setText( TDSetting.mSilexGisUrl );
    setKeyHint();
    if ( ! TDSetting.mSilexGisAccount.isEmpty() ) {
      mTVresult.setText( String.format( resString( R.string.silexgis_account ), TDSetting.mSilexGisAccount ) );
    }

    mBtnTest = (Button) findViewById( R.id.button_test );
    mBtnTest.setOnClickListener( this );
    ( (Button) findViewById( R.id.button_forget ) ).setOnClickListener( this );
    ( (Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );
    ( (Button) findViewById( R.id.button_ok ) ).setOnClickListener( this );
  }

  /** show the stored key, masked, as hint of the key field
   */
  private void setKeyHint()
  {
    String masked = SilexGisClient.maskKey( TDSetting.mSilexGisKey );
    mETkey.setHint( masked.isEmpty()? resString( R.string.silexgis_key_hint ) : masked );
  }

  /** @return the URL in the dialog, normalized
   */
  private String getUrl() { return SilexGisClient.normalizeBaseUrl( mETurl.getText().toString() ); }

  /** @return the key in the dialog: the one typed, or the stored one if nothing has been typed
   */
  private String getKey()
  {
    String key = mETkey.getText().toString().trim();
    return key.isEmpty()? TDSetting.mSilexGisKey : key;
  }

  /** respond to a user tap
   * @param v  tapped view
   */
  @Override
  public void onClick( View v )
  {
    int id = v.getId();
    if ( id == R.id.button_test ) {
      String url = getUrl();
      String key = getKey();
      SilexGisClient client = new SilexGisClient( url, key );
      int check = SilexGisClient.checkBaseUrl( url );
      if ( ! client.isConfigured() ) {
        mTVresult.setText( R.string.silexgis_not_configured );
      } else if ( check != SilexGisClient.URL_OK ) {
        mTVresult.setText( ( check == SilexGisClient.URL_CLEARTEXT )? R.string.silexgis_url_cleartext : R.string.silexgis_url_invalid );
      } else if ( ! SilexGisClient.checkKey( key ) ) {
        mETkey.setError( resString( R.string.silexgis_key_invalid ) );
        mTVresult.setText( R.string.silexgis_key_invalid );
      } else {
        mTVresult.setText( R.string.silexgis_testing );
        mBtnTest.setEnabled( false );
        (new TestTask( this, client, url, key )).execute();
      }
      return;
    } else if ( id == R.id.button_forget ) {
      TDSetting.setSilexGisAccess( TDSetting.mSilexGisUrl, "" ); // only the key is forgotten
      mETkey.setText( "" );
      setKeyHint();
      mTestedKey = null;
      mAccount   = null;
      mTVresult.setText( R.string.silexgis_forgotten );
      return;
    } else if ( id == R.id.button_ok ) {
      String url = getUrl();
      String key = getKey();
      if ( ! url.isEmpty() ) {
        int check = SilexGisClient.checkBaseUrl( url );
        if ( check != SilexGisClient.URL_OK ) {
          mETurl.setError( resString( ( check == SilexGisClient.URL_CLEARTEXT )? R.string.silexgis_url_cleartext : R.string.silexgis_url_invalid ) );
          return;
        }
      }
      if ( ! key.isEmpty() && ! SilexGisClient.checkKey( key ) ) {
        mETkey.setError( resString( R.string.silexgis_key_invalid ) );
        return;
      }
      TDSetting.setSilexGisAccess( url, key );
      if ( mAccount != null && url.equals( mTestedUrl ) && key.equals( mTestedKey ) ) {
        TDSetting.setSilexGisAccount( mAccount );
      }
      dismiss();
      if ( mOnConfigured != null && ! url.isEmpty() && ! key.isEmpty() ) mOnConfigured.run();
      return;
    }
    dismiss(); // button_cancel
  }

  /** show the result of the test
   * @param url   tested URL
   * @param key   tested key
   * @param res   results of me, about, capabilities
   */
  private void showTest( String url, String key, SilexGisClient.Result[] res )
  {
    mBtnTest.setEnabled( true );
    Resources r = mContext.getResources();
    if ( ! res[0].isOk() ) {
      mTVresult.setText( errorMessage( r, res[0] ) );
      return;
    }
    // the key is good once me() answers: a failure of about or capabilities is reported below the account
    mTestedUrl = url;
    mTestedKey = key;
    mAccount   = SilexGisClient.accountLabel( res[0] );
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( r.getString( R.string.silexgis_account ), mAccount ) ).append( "\n" );
    for ( int k = 1; k < res.length; ++k ) {
      if ( ! res[k].isOk() ) {
        sb.append( errorMessage( r, res[k] ) );
        mTVresult.setText( sb.toString() );
        return;
      }
    }
    sb.append( String.format( r.getString( R.string.silexgis_server ), res[1].getString( "name", "" ), res[1].getString( "version", "" ) ) ).append( "\n" );
    SilexGisExportTask.getStore( mContext ).saveAccountAudience( SilexGisUploader.accountLinkAudience( res[2].json ) ); // the default link audience
    if ( SilexGisClient.compilerAvailable( res[2] ) ) {
      JSONObject compiler = res[2].json.optJSONObject( "compiler" );
      sb.append( String.format( r.getString( R.string.silexgis_compiles ), compiler.optString( "version", "" ) ) );
    } else {
      sb.append( r.getString( R.string.silexgis_no_compiler ) );
    }
    mTVresult.setText( sb.toString() );
  }

  /** test the access off the UI thread: me, about, capabilities
   */
  private static class TestTask extends AsyncTask< Void, Void, SilexGisClient.Result[] >
  {
    private final SilexGisSettingsDialog mDialog;
    private final SilexGisClient mClient;
    private final String mUrl;
    private final String mKey;

    TestTask( SilexGisSettingsDialog dialog, SilexGisClient client, String url, String key )
    {
      mDialog = dialog;
      mClient = client;
      mUrl    = url;
      mKey    = key;
    }

    @Override
    protected SilexGisClient.Result[] doInBackground( Void... args )
    {
      SilexGisClient.Result me = mClient.me();
      if ( ! me.isOk() ) return new SilexGisClient.Result[] { me };
      return new SilexGisClient.Result[] { me, mClient.about(), mClient.capabilities() };
    }

    @Override
    protected void onPostExecute( SilexGisClient.Result[] res )
    {
      if ( mDialog.isShowing() ) mDialog.showTest( mUrl, mKey, res );
    }
  }

}
