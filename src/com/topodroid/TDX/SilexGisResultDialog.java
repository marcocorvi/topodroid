/* @file SilexGisResultDialog.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload to a SilexGIS server: what the upload came to
 *
 * The results of the processing can be downloaded into the survey "out" folder, and the
 * 3D models opened in Cave3D. The upload is followed in the browser: its public link if
 * there is one, else its status page on the server (which asks to sign in).
 * The link can be withdrawn, or a new one made for anyone or for the signed-in members;
 * for a cave whose position is protected a link is made only after a confirmation.
 * When sketches had no current Therion export, the dialog offers to switch the sketch
 * auto-export to Therion, and it offers to turn it off again where the settings do not show it.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefHelper;

import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ActivityNotFoundException;
import android.content.res.Resources;
import android.net.Uri;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import android.view.View;

import java.io.File;
import java.util.List;
import java.util.Locale;

class SilexGisResultDialog extends MyDialog
                           implements View.OnClickListener
                           , AdapterView.OnItemClickListener
{
  private final SilexGisClient mClient;
  private final SilexGisUploader.Outcome mOutcome;
  private final String mNotes;
  private List< SilexGisStatus.Artefact > mArtefacts = null;
  private SilexGisStatus.Artefact mSelected = null;
  private boolean mDownloading = false; // a download is running: no other is started
  private final boolean mSketchesBehind;  // a sketch had no current Therion export
  private final String mAudience;         // link audience asked for the upload, null if the account decided
  private SilexGisLink mLink;             // replaced, on the UI thread, by the one a link request worked on
  private final boolean mLinkEditable;    // the server has the whole upload: its link can be withdrawn or made again
  private boolean mLinkBusy = false;      // a link request is running
  private int mAutoExportBefore = -2;     // sketch auto-export format before it was switched here (-2: not switched)

  private TextView mTVpick;
  private Button mBtnDownload;
  private Button mBtnCave3d;
  private Button mBtnBrowser;
  private Button mBtnCopy;
  private Button mBtnWithdraw;
  private Button mBtnMake;
  private Spinner mSpinAudience;

  /** cstr
   * @param context   context
   * @param client    client of the server
   * @param outcome   outcome of the upload
   * @param notes     what the user should know about the files of the upload (can be empty)
   * @param sketches_behind  whether a sketch had no current Therion export
   * @param audience  link audience asked for the upload (null if the account decided)
   */
  SilexGisResultDialog( Context context, SilexGisClient client, SilexGisUploader.Outcome outcome, String notes,
                        boolean sketches_behind, String audience )
  {
    super( context, null, 0 ); // 0: no help resource
    mClient  = client;
    mOutcome = outcome;
    mNotes   = notes;
    mSketchesBehind = sketches_behind;
    mAudience = SilexGisUpload.isAudience( audience )? audience : null;
    mLink    = SilexGisLink.of( outcome.status );
    mLinkEditable = outcome.completed && outcome.uploadId != null && outcome.kind != SilexGisUploader.KEY_REFUSED;
  }

  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout( R.layout.silexgis_result_dialog, R.string.silexgis_upload_title );
    Resources res = mContext.getResources();

    ( (TextView) findViewById( R.id.silexgis_result ) ).setText( SilexGisExportTask.outcomeText( res, mOutcome ) );
    SilexGisStatus status = mOutcome.status;
    String text_notes = ( mNotes == null )? "" : mNotes;
    if ( status != null ) { // the server's own words about an output it did not produce or could not read
      for ( SilexGisStatus.Artefact a : status.withError() ) {
        if ( ! text_notes.isEmpty() ) text_notes += "\n";
        text_notes += a.kind + ": " + ( ( a.errorMessage != null )? a.errorMessage : a.errorCode );
      }
    }
    if ( ! text_notes.isEmpty() ) {
      TextView notes = (TextView) findViewById( R.id.silexgis_notes );
      notes.setText( text_notes );
      notes.setVisibility( View.VISIBLE );
    }

    ( (Button) findViewById( R.id.button_auto_th2 ) ).setOnClickListener( this );
    showAutoExport();

    mTVpick = (TextView) findViewById( R.id.silexgis_pick );
    if ( status != null ) {
      mArtefacts = status.downloadable();
      if ( ! mArtefacts.isEmpty() ) {
        ArrayAdapter< String > adapter = new ArrayAdapter<>( mContext, R.layout.message );
        for ( SilexGisStatus.Artefact a : mArtefacts ) adapter.add( artefactLine( a ) );
        ListView list = (ListView) findViewById( R.id.silexgis_artefacts );
        list.setAdapter( adapter );
        list.setOnItemClickListener( this );
        list.setVisibility( View.VISIBLE );
        mTVpick.setVisibility( View.VISIBLE );
      }
    }

    mBtnBrowser = (Button) findViewById( R.id.button_browser );
    mBtnBrowser.setOnClickListener( this );
    mBtnCopy = (Button) findViewById( R.id.button_copy );
    mBtnCopy.setOnClickListener( this );
    mBtnWithdraw = (Button) findViewById( R.id.button_link_withdraw );
    mBtnWithdraw.setOnClickListener( this );
    mBtnMake = (Button) findViewById( R.id.button_link_make );
    mBtnMake.setOnClickListener( this );
    mSpinAudience = (Spinner) findViewById( R.id.silexgis_link_audience );
    mSpinAudience.setAdapter( new ArrayAdapter<>( mContext, R.layout.menu, res.getStringArray( R.array.silexgisLinkAudience ) ) );
    String audience = ( mLink.hasLink() && SilexGisUpload.isAudience( mLink.audience ) )? mLink.audience : mAudience;
    mSpinAudience.setSelection( SilexGisUpload.AUDIENCE_ORGANIZATION.equals( audience )? 2 : SilexGisUpload.AUDIENCE_PUBLIC.equals( audience )? 1 : 0 );
    showLink();
    mBtnDownload = (Button) findViewById( R.id.button_download );
    mBtnDownload.setOnClickListener( this );
    mBtnCave3d = (Button) findViewById( R.id.button_cave3d );
    mBtnCave3d.setOnClickListener( this );
    ( (Button) findViewById( R.id.button_close ) ).setOnClickListener( this );
    setSelected( null );
  }

  /** @return the line of an artefact in the list
   * @param a   artefact
   */
  private static String artefactLine( SilexGisStatus.Artefact a )
  {
    if ( a.size < 0 ) return String.format( Locale.US, "%-12s %s", a.kind, a.name );
    return String.format( Locale.US, "%-12s %s (%d kB)", a.kind, a.name, ( a.size + 1023 ) / 1024 );
  }

  /** @return the full URL of the public link, null if none
   */
  private String publicLink()
  {
    if ( mLink.url == null ) return null;
    return mClient.resolveUrl( mLink.url );
  }

  /** show the link, or why there is none, and what can be done with it
   */
  private void showLink()
  {
    Resources res = mContext.getResources();
    String link = publicLink();
    TextView tv_link = (TextView) findViewById( R.id.silexgis_link );
    if ( link != null ) {
      tv_link.setText( String.format( res.getString( R.string.silexgis_public_link ), link ) );
      tv_link.setVisibility( View.VISIBLE );
    } else if ( SilexGisLink.REVOKED.equals( mLink.refusal ) ) {
      tv_link.setText( R.string.silexgis_link_withdrawn );
      tv_link.setVisibility( View.VISIBLE );
    } else if ( mLink.refusal != null && ! mLink.isProtected() && ! mLink.isNotWanted() ) { // no link asked for: nothing to say
      tv_link.setText( String.format( res.getString( R.string.silexgis_no_public_link ), mLink.refusal ) );
      tv_link.setVisibility( View.VISIBLE );
    } else {
      tv_link.setVisibility( View.GONE );
    }
    TextView tv_note = (TextView) findViewById( R.id.silexgis_link_note );
    if ( mLink.isProtected() ) {
      tv_note.setText( R.string.silexgis_link_protected );
      tv_note.setVisibility( View.VISIBLE );
    } else if ( mLink.isOrganization() ) {
      tv_note.setText( R.string.silexgis_link_organization_note );
      tv_note.setVisibility( View.VISIBLE );
    } else {
      tv_note.setVisibility( View.GONE );
    }
    mBtnBrowser.setEnabled( ! mLinkBusy && browserUrl() != null ); // not while the link changes
    mBtnCopy.setEnabled( ! mLinkBusy && link != null );
    mBtnWithdraw.setVisibility( ( mLinkEditable && link != null )? View.VISIBLE : View.GONE );
    mBtnWithdraw.setEnabled( ! mLinkBusy );
    findViewById( R.id.silexgis_link_edit ).setVisibility( mLinkEditable? View.VISIBLE : View.GONE );
    mBtnMake.setText( mLink.isProtected()? R.string.silexgis_link_make_anyway : R.string.silexgis_link_make );
    mBtnMake.setEnabled( ! mLinkBusy );
  }

  /** @return the audience chosen for a new link, null to leave it to the server (the audience of the link it
   *          replaces, else the one the upload asked for, else the account's default)
   */
  private String chosenAudience()
  {
    switch ( mSpinAudience.getSelectedItemPosition() ) {
      case 1: return SilexGisUpload.AUDIENCE_PUBLIC;
      case 2: return SilexGisUpload.AUDIENCE_ORGANIZATION;
    }
    return null;
  }

  /** show the offer to export the sketches to Therion automatically, or the way to turn it off:
   *  off where the settings do not show the sketch auto-export format (basic level), or once switched here
   */
  private void showAutoExport()
  {
    Resources res = mContext.getResources();
    boolean th2 = ( TDSetting.mAutoExportPlotFormat == TDConst.SURVEY_FORMAT_TH2 );
    View block = findViewById( R.id.silexgis_auto_th2 );
    if ( ! th2 && mSketchesBehind ) {
      ( (TextView) findViewById( R.id.silexgis_auto_th2_text ) ).setText( String.format( res.getString( R.string.silexgis_auto_th2_offer ), autoExportName( res ) ) );
      ( (Button) findViewById( R.id.button_auto_th2 ) ).setText( R.string.silexgis_auto_th2_button );
      block.setVisibility( View.VISIBLE );
    } else if ( th2 && ( ! TDLevel.overBasic || mAutoExportBefore != -2 ) ) {
      ( (TextView) findViewById( R.id.silexgis_auto_th2_text ) ).setText( R.string.silexgis_auto_th2_on );
      ( (Button) findViewById( R.id.button_auto_th2 ) ).setText( R.string.silexgis_auto_th2_off );
      block.setVisibility( View.VISIBLE );
    } else {
      block.setVisibility( View.GONE );
    }
  }

  /** set the sketch auto-export format
   * @param format   format (-1 none)
   */
  private void setAutoExport( int format )
  {
    TDSetting.mAutoExportPlotFormat = format;
    TDPrefHelper.update( "DISTOX_AUTO_PLOT_EXPORT", Integer.toString( format ) );
    TDToast.make( String.format( mContext.getResources().getString( R.string.silexgis_auto_th2_done ), autoExportName( mContext.getResources() ) ) );
    showAutoExport();
  }

  /** @return the name of the current sketch auto-export format
   * @param res   resources
   */
  private static String autoExportName( Resources res )
  {
    String[] names  = res.getStringArray( R.array.exportPlotAuto );
    String[] values = res.getStringArray( R.array.exportPlotAutoValue );
    String value = Integer.toString( TDSetting.mAutoExportPlotFormat );
    for ( int k = 0; k < values.length && k < names.length; ++k ) if ( values[k].equals( value ) ) return names[k];
    return value;
  }

  /** a link request ends
   * @param outcome   outcome of the request
   */
  private void linkDone( int outcome )
  {
    mLinkBusy = false;
    Resources res = mContext.getResources();
    switch ( outcome ) {
      case SilexGisLink.WITHDRAWN:
        TDToast.make( R.string.silexgis_link_withdrawn );
        break;
      case SilexGisLink.KEY_REFUSED:
        SilexGisSettingsDialog.showKeyRefused( mContext, null );
        break;
      case SilexGisLink.REFUSED:
        String why = ( mLink.result == null )? "" : SilexGisSettingsDialog.errorMessage( res, mLink.result );
        TDToast.makeBad( String.format( res.getString( R.string.silexgis_link_failed ), why ) );
        break;
      // MADE: the new link is shown; PROTECTED: the explanation is shown, and the button asks for the link anyway
    }
    showLink();
  }

  /** start a link request
   * @param make             whether to make a link (else withdraw it)
   * @param allow_protected  whether the link is asked for although the position of the cave is protected
   */
  private void startLink( boolean make, boolean allow_protected )
  {
    if ( mLinkBusy || ! mLinkEditable ) return;
    mLinkBusy = true;
    mBtnWithdraw.setEnabled( false );
    mBtnMake.setEnabled( false );
    mBtnCopy.setEnabled( false );
    mBtnBrowser.setEnabled( false );
    (new LinkTask( this, make, chosenAudience(), allow_protected )).execute();
  }

  /** @return the URL to open in the browser: the public link, else the status page (null if none)
   */
  private String browserUrl()
  {
    String link = publicLink();
    if ( link != null ) return link;
    String page = ( mOutcome.status != null && mOutcome.status.statusPageUrl != null )? mOutcome.status.statusPageUrl : mOutcome.statusPageUrl;
    return ( page == null )? null : mClient.resolveUrl( page );
  }

  /** select an artefact
   * @param a   artefact (null for none)
   */
  private void setSelected( SilexGisStatus.Artefact a )
  {
    mSelected = a;
    mBtnDownload.setEnabled( a != null && ! mDownloading );
    mBtnCave3d.setEnabled( a != null && a.isModel() && ! mDownloading );
    if ( a != null && ! mDownloading ) mTVpick.setText( String.format( resString( R.string.silexgis_artefact_selected ), a.name ) );
  }

  /** a download starts or ends
   * @param on   whether a download is running
   */
  private void setDownloading( boolean on )
  {
    mDownloading = on;
    setSelected( mSelected );
  }

  /** show the progress of a download
   * @param name   artefact name
   * @param done   bytes received
   */
  private void showDownload( String name, long done )
  {
    mTVpick.setText( String.format( resString( R.string.silexgis_downloading ), name, (int)( done / 1024 ) ) );
  }

  @Override
  public void onItemClick( AdapterView<?> parent, View view, int pos, long id )
  {
    if ( mArtefacts != null && pos >= 0 && pos < mArtefacts.size() ) setSelected( mArtefacts.get( pos ) );
  }

  @Override
  public void onClick( View v )
  {
    int id = v.getId();
    if ( id == R.id.button_browser ) {
      String url = browserUrl();
      if ( url == null ) return;
      try {
        mContext.startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( url ) ) );
      } catch ( ActivityNotFoundException e ) {
        TDToast.makeBad( url );
      }
      return;
    } else if ( id == R.id.button_copy ) {
      String link = publicLink();
      ClipboardManager cm = (ClipboardManager)mContext.getSystemService( Context.CLIPBOARD_SERVICE );
      if ( link != null && cm != null ) {
        cm.setPrimaryClip( ClipData.newPlainText( "SilexGIS", link ) );
        TDToast.make( String.format( resString( R.string.copied ), link ) );
      }
      return;
    } else if ( id == R.id.button_link_withdraw ) {
      startLink( false, false );
      return;
    } else if ( id == R.id.button_link_make ) {
      if ( mLink.isProtected() ) { // the results show where the passages are: never without a confirmation
        TopoDroidAlertDialog.makeAlert( mContext, mContext.getResources(), R.string.silexgis_link_confirm_protected,
          new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { startLink( true, true ); }
          } );
      } else {
        startLink( true, false );
      }
      return;
    } else if ( id == R.id.button_auto_th2 ) {
      if ( TDSetting.mAutoExportPlotFormat != TDConst.SURVEY_FORMAT_TH2 ) {
        mAutoExportBefore = TDSetting.mAutoExportPlotFormat;
        setAutoExport( TDConst.SURVEY_FORMAT_TH2 );
      } else { // back to the format it had before it was switched here, else none
        setAutoExport( ( mAutoExportBefore >= 0 && mAutoExportBefore != TDConst.SURVEY_FORMAT_TH2 )? mAutoExportBefore : -1 );
      }
      return;
    } else if ( id == R.id.button_download || id == R.id.button_cave3d ) {
      if ( mSelected == null || mDownloading ) return;
      TDPath.checkOutdir();
      File dest = new File( TDPath.getOutFile( SilexGisFiles.safeName( mSelected.name, mSelected.kind ) ) );
      (new DownloadTask( this, mClient, mOutcome.uploadId, mSelected, dest, id == R.id.button_cave3d )).execute();
      return;
    }
    dismiss(); // button_close
  }

  /** open a model file in Cave3D
   * @param context   context
   * @param path      full pathname of the model file
   */
  static void openInCave3D( Context context, String path )
  {
    try {
      Intent intent = new Intent( Intent.ACTION_VIEW ).setClass( context, com.topodroid.TDX.TopoGL.class );
      intent.putExtra( "INPUT_FILE", path );
      context.startActivity( intent );
    } catch ( ActivityNotFoundException e ) {
      TDToast.makeBad( R.string.no_cave3d );
    }
  }

  /** make or withdraw the link off the UI thread: the request works on a copy of the link, which takes the
   *  place of the one of the dialog when it ends, so that the dialog never reads a link that is changing
   */
  private static class LinkTask extends AsyncTask< Void, Void, Integer >
  {
    private final SilexGisResultDialog mDialog;
    private final SilexGisLink mLink;
    private final boolean mMake;
    private final String mAudience;
    private final boolean mAllowProtected;

    LinkTask( SilexGisResultDialog dialog, boolean make, String audience, boolean allow_protected )
    {
      mDialog   = dialog;
      mLink     = dialog.mLink.copy();
      mMake     = make;
      mAudience = audience;
      mAllowProtected = allow_protected;
    }

    @Override
    protected Integer doInBackground( Void... args )
    {
      String id = mDialog.mOutcome.uploadId;
      return mMake ? mLink.make( mDialog.mClient, id, mAudience, mAllowProtected ) : mLink.withdraw( mDialog.mClient, id );
    }

    @Override
    protected void onPostExecute( Integer outcome )
    {
      mDialog.mLink = mLink;
      mDialog.linkDone( outcome );
    }
  }

  /** download an artefact off the UI thread
   */
  private static class DownloadTask extends AsyncTask< Void, Long, SilexGisClient.Result >
  {
    private final SilexGisResultDialog mDialog;
    private final Context mContext;
    private final SilexGisClient mClient;
    private final String mUploadId;
    private final SilexGisStatus.Artefact mArtefact;
    private final File mDest;
    private final boolean mOpen;

    private long mShown = -1; // kB shown

    DownloadTask( SilexGisResultDialog dialog, SilexGisClient client, String upload_id, SilexGisStatus.Artefact artefact, File dest, boolean open )
    {
      mDialog   = dialog;
      mContext  = dialog.mContext;
      mClient   = client;
      mUploadId = upload_id;
      mArtefact = artefact;
      mDest     = dest;
      mOpen     = open;
    }

    @Override
    protected void onPreExecute() { mDialog.setDownloading( true ); }

    @Override
    protected SilexGisClient.Result doInBackground( Void... args )
    {
      return ( new SilexGisUploader( mClient, null ) ).fetchArtefact( mUploadId, mArtefact, mDest, new SilexGisClient.Progress() {
        @Override public boolean onProgress( long done, long total ) {
          if ( done / 1024 != mShown ) { // one update per kB
            mShown = done / 1024;
            publishProgress( done );
          }
          return true;
        }
      } );
    }

    @Override
    protected void onProgressUpdate( Long... done ) { mDialog.showDownload( mArtefact.name, done[ done.length - 1 ] ); }

    @Override
    protected void onPostExecute( SilexGisClient.Result r )
    {
      mDialog.setDownloading( false );
      Resources res = mContext.getResources();
      if ( ! r.isOk() ) {
        TDLog.e( "SilexGIS download failed " + r.kind + " " + r.status );
        TDToast.makeBad( String.format( res.getString( R.string.silexgis_download_failed ), mArtefact.name,
          ( r.kind == SilexGisClient.FILE_ERROR )? res.getString( R.string.saving_file_failed ) : SilexGisSettingsDialog.errorMessage( res, r ) ) );
        return;
      }
      if ( mOpen ) {
        openInCave3D( mContext, mDest.getPath() );
      } else {
        TDToast.make( String.format( res.getString( R.string.saved_file_1 ), mDest.getName() ) );
      }
    }
  }

}
