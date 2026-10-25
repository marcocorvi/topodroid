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
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.ui.MyDialog;

import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Context;
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

  private TextView mTVpick;
  private Button mBtnDownload;
  private Button mBtnCave3d;

  /** cstr
   * @param context   context
   * @param client    client of the server
   * @param outcome   outcome of the upload
   * @param notes     what the user should know about the files of the upload (can be empty)
   */
  SilexGisResultDialog( Context context, SilexGisClient client, SilexGisUploader.Outcome outcome, String notes )
  {
    super( context, null, 0 ); // 0: no help resource
    mClient  = client;
    mOutcome = outcome;
    mNotes   = notes;
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

    String link = publicLink();
    TextView tv_link = (TextView) findViewById( R.id.silexgis_link );
    if ( link != null ) {
      tv_link.setText( String.format( res.getString( R.string.silexgis_public_link ), link ) );
      tv_link.setVisibility( View.VISIBLE );
    } else if ( status != null && status.publicLinkRefusal != null ) {
      tv_link.setText( String.format( res.getString( R.string.silexgis_no_public_link ), status.publicLinkRefusal ) );
      tv_link.setVisibility( View.VISIBLE );
    }

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

    Button browser = (Button) findViewById( R.id.button_browser );
    browser.setOnClickListener( this );
    browser.setEnabled( browserUrl() != null );
    Button copy = (Button) findViewById( R.id.button_copy );
    copy.setOnClickListener( this );
    copy.setEnabled( link != null );
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
    if ( mOutcome.status == null || mOutcome.status.publicLinkUrl == null ) return null;
    return mClient.resolveUrl( mOutcome.status.publicLinkUrl );
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
