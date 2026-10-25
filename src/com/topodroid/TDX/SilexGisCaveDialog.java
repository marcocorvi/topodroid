/* @file SilexGisCaveDialog.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload to a SilexGIS server: the cave to upload to
 *
 * The survey goes to a cave found by its name, to a new cave, to the survey of the cave
 * it was uploaded to last time, or to no cave: then the server makes one named after
 * the survey title.
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
import android.os.Handler;
import android.os.Looper;
import android.content.Context;

import android.text.Editable;
import android.text.TextWatcher;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SilexGisCaveDialog extends MyDialog
                         implements View.OnClickListener
                         , AdapterView.OnItemClickListener
{
  static final long SEARCH_DELAY = 300; // [ms] after the last key
  static final int  SEARCH_SIZE  = 20;  // most caves listed

  /** receiver of the picked target
   */
  interface Listener
  {
    /** @param target   upload that carries the picked target
     */
    void onTarget( SilexGisUpload target );
  }

  private final Listener mListener;
  private final String mSurveyTitle;
  private final SilexGisUpload mCurrent;    // target in the export dialog
  private final SilexGisUpload mRemembered; // target remembered for the survey (null if none)

  private EditText mETsearch;
  private TextView mTVsearch;
  private ListView mList;
  private EditText mETname;
  private Spinner  mSPvisibility;
  private CheckBox mCBprotected;
  private Button   mBtnNew;

  private final ArrayList< String[] > mCaves = new ArrayList<>(); // { id, name } of the listed caves
  private ArrayAdapter< String > mAdapter;
  private final ArrayList< String > mVisibilities = new ArrayList<>( Arrays.asList( SilexGisUpload.VISIBILITIES ) ); // offered for a new cave

  private final Handler mHandler = new Handler( Looper.getMainLooper() );
  private int mSearchSeq = 0; // the answer of an older search is dropped
  private boolean mKeyAsked = false; // the access dialog has been opened after a refused key
  private final Runnable mSearch = new Runnable() {
    @Override public void run() { startSearch(); }
  };

  /** cstr
   * @param context      context
   * @param survey_title survey title, the name of a new cave
   * @param current      current target
   * @param remembered   target remembered for the survey (can be null)
   * @param listener     receiver of the picked target
   */
  SilexGisCaveDialog( Context context, String survey_title, SilexGisUpload current, SilexGisUpload remembered, Listener listener )
  {
    super( context, null, 0 ); // 0: no help resource
    mSurveyTitle = survey_title;
    mCurrent     = current;
    mRemembered  = remembered;
    mListener    = listener;
  }

  /** @return the client of the configured server
   */
  private static SilexGisClient client() { return new SilexGisClient( TDSetting.mSilexGisUrl, TDSetting.mSilexGisKey ); }

  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout( R.layout.silexgis_cave_dialog, R.string.silexgis_cave_title );

    ( (Button) findViewById( R.id.silexgis_auto ) ).setOnClickListener( this );
    Button last = (Button) findViewById( R.id.silexgis_last );
    if ( mRemembered != null && mRemembered.target != SilexGisUpload.TARGET_AUTO ) {
      last.setText( SilexGisExportTask.targetText( mContext.getResources(), mRemembered ) );
      last.setVisibility( View.VISIBLE );
      last.setOnClickListener( this );
    }

    mETsearch = (EditText) findViewById( R.id.silexgis_search );
    mTVsearch = (TextView) findViewById( R.id.silexgis_search_result );
    mList     = (ListView) findViewById( R.id.silexgis_caves );
    mAdapter  = new ArrayAdapter<>( mContext, R.layout.message );
    mList.setAdapter( mAdapter );
    mList.setOnItemClickListener( this );
    mETsearch.addTextChangedListener( new TextWatcher() {
      @Override public void beforeTextChanged( CharSequence s, int start, int count, int after ) { }
      @Override public void onTextChanged( CharSequence s, int start, int before, int count ) { }
      @Override public void afterTextChanged( Editable e ) {
        mHandler.removeCallbacks( mSearch );
        mHandler.postDelayed( mSearch, SEARCH_DELAY );
      }
    } );

    mETname = (EditText) findViewById( R.id.silexgis_new_name );
    mSPvisibility = (Spinner) findViewById( R.id.silexgis_new_visibility );
    mCBprotected  = (CheckBox) findViewById( R.id.silexgis_new_protected );
    String visibility = ( mCurrent != null )? mCurrent.visibility : TDSetting.mSilexGisVisibility; // the switches of the export
    if ( mCurrent != null && mCurrent.target == SilexGisUpload.TARGET_NEW_CAVE ) {
      mETname.setText( mCurrent.newCaveName );
      visibility = mCurrent.newCaveVisibility;
      mCBprotected.setChecked( mCurrent.newCaveProtected );
    } else {
      mETname.setText( mSurveyTitle );
    }
    setVisibilities( mVisibilities, visibility );

    mBtnNew = (Button) findViewById( R.id.button_ok );
    mBtnNew.setOnClickListener( this );
    ( (Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );

    (new InfoTask( this, client() )).execute();
  }

  @Override
  public void onClick( View v )
  {
    int id = v.getId();
    SilexGisUpload target = new SilexGisUpload();
    if ( id == R.id.silexgis_auto ) {
      target.setAuto();
    } else if ( id == R.id.silexgis_last ) {
      target = mRemembered.copy();
    } else if ( id == R.id.button_ok ) { // new cave
      String name = mETname.getText().toString().trim();
      if ( name.isEmpty() ) {
        mETname.setError( resString( R.string.error_name_required ) );
        return;
      }
      target.setNewCave( name, selectedVisibility(), mCBprotected.isChecked() );
    } else { // button_cancel
      mHandler.removeCallbacks( mSearch );
      dismiss();
      return;
    }
    mHandler.removeCallbacks( mSearch );
    dismiss();
    if ( mListener != null ) mListener.onTarget( target );
  }

  @Override
  public void onItemClick( AdapterView<?> parent, View view, int pos, long id )
  {
    if ( pos < 0 || pos >= mCaves.size() ) return;
    String[] cave = mCaves.get( pos );
    SilexGisUpload target = new SilexGisUpload();
    target.setCave( cave[0], cave[1] );
    mHandler.removeCallbacks( mSearch );
    dismiss();
    if ( mListener != null ) mListener.onTarget( target );
  }

  /** offer the visibilities of a new cave
   * @param values     visibilities, in the order of SilexGisUpload.VISIBILITIES
   * @param selected   visibility to select (the club, else the first, if it is not offered)
   */
  private void setVisibilities( List< String > values, String selected )
  {
    String[] labels = mContext.getResources().getStringArray( R.array.silexgisVisibility );
    ArrayList< String > names = new ArrayList<>();
    for ( String vis : values ) names.add( labels[ SilexGisExportTask.visibilityIndex( vis ) ] );
    if ( values != mVisibilities ) {
      mVisibilities.clear();
      mVisibilities.addAll( values );
    }
    mSPvisibility.setAdapter( new ArrayAdapter<>( mContext, R.layout.menu, names ) );
    int k = mVisibilities.indexOf( selected );
    if ( k < 0 ) k = mVisibilities.indexOf( SilexGisUpload.VISIBILITY_CLUB );
    mSPvisibility.setSelection( Math.max( 0, k ) );
  }

  /** @return the visibility selected for a new cave
   */
  private String selectedVisibility()
  {
    int k = mSPvisibility.getSelectedItemPosition();
    if ( k < 0 || k >= mVisibilities.size() ) return mVisibilities.isEmpty()? SilexGisUpload.VISIBILITY_CLUB : mVisibilities.get( 0 );
    return mVisibilities.get( k );
  }

  /** search the caves named as the text of the search field
   */
  private void startSearch()
  {
    String text = mETsearch.getText().toString().trim();
    ++ mSearchSeq;
    if ( text.isEmpty() ) {
      mCaves.clear();
      mAdapter.clear();
      mTVsearch.setText( "" );
      return;
    }
    mTVsearch.setText( R.string.silexgis_cave_searching );
    (new SearchTask( this, client(), text, mSearchSeq )).execute();
  }

  /** show the caves found
   * @param seq   number of the search
   * @param r     answer of the server
   */
  private void showCaves( int seq, SilexGisClient.Result r )
  {
    if ( seq != mSearchSeq ) return; // a newer search is on its way
    mCaves.clear();
    mAdapter.clear();
    if ( ! r.isOk() ) {
      mTVsearch.setText( SilexGisSettingsDialog.errorMessage( mContext.getResources(), r ) );
      if ( r.kind == SilexGisClient.KEY_REFUSED && ! mKeyAsked ) { // once: the searches typed meanwhile are refused too
        mKeyAsked = true;
        SilexGisSettingsDialog.showKeyRefused( mContext, new Runnable() {
          @Override public void run() {
            mKeyAsked = false;
            if ( isShowing() ) startSearch(); // with the new key
          }
        } );
      }
      return;
    }
    for ( String[] cave : SilexGisClient.parseCaves( r.json ) ) {
      mCaves.add( cave );
      mAdapter.add( cave[2] );
    }
    mTVsearch.setText( mCaves.isEmpty()? resString( R.string.silexgis_cave_none ) : "" );
  }

  /** show what the server allows: making a cave, and with which visibilities
   * @param caps   capabilities
   * @note the server binds a club-visible cave to the club of the account: the account's clubs are not listed to an access key
   */
  private void showInfo( SilexGisClient.Result caps )
  {
    if ( caps == null || ! caps.isOk() ) return;
    if ( SilexGisUploader.caveCreationAllowed( caps.json ) ) {
      setVisibilities( SilexGisUploader.caveVisibilities( caps.json ), selectedVisibility() );
    } else { // neither a new cave nor one named after the survey title
      findViewById( R.id.silexgis_auto ).setEnabled( false );
      mBtnNew.setEnabled( false );
      mETname.setEnabled( false );
      mSPvisibility.setEnabled( false );
      mCBprotected.setEnabled( false );
      findViewById( R.id.silexgis_no_creation ).setVisibility( View.VISIBLE );
    }
  }

  /** search off the UI thread
   */
  private static class SearchTask extends AsyncTask< Void, Void, SilexGisClient.Result >
  {
    private final SilexGisCaveDialog mDialog;
    private final SilexGisClient mClient;
    private final String mText;
    private final int mSeq;

    SearchTask( SilexGisCaveDialog dialog, SilexGisClient client, String text, int seq )
    {
      mDialog = dialog;
      mClient = client;
      mText   = text;
      mSeq    = seq;
    }

    @Override
    protected SilexGisClient.Result doInBackground( Void... args ) { return mClient.searchCaves( mText, SEARCH_SIZE ); }

    @Override
    protected void onPostExecute( SilexGisClient.Result r )
    {
      if ( mDialog.isShowing() ) mDialog.showCaves( mSeq, r );
    }
  }

  /** capabilities off the UI thread
   */
  private static class InfoTask extends AsyncTask< Void, Void, SilexGisClient.Result >
  {
    private final SilexGisCaveDialog mDialog;
    private final SilexGisClient mClient;

    InfoTask( SilexGisCaveDialog dialog, SilexGisClient client )
    {
      mDialog = dialog;
      mClient = client;
    }

    @Override
    protected SilexGisClient.Result doInBackground( Void... args ) { return mClient.capabilities(); }

    @Override
    protected void onPostExecute( SilexGisClient.Result r )
    {
      if ( mDialog.isShowing() ) mDialog.showInfo( r );
    }
  }

}
