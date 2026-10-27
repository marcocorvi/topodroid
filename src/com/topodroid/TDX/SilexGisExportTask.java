/* @file SilexGisExportTask.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload of the current survey to a SilexGIS server
 *
 * The survey is archived (the ordinary ZIP), exported to Therion (the survey file with its
 * map commands live, and every sketch that has a Therion export) and to the other formats
 * asked for; then the files are uploaded and the server processing is watched.
 * The upload options are those of the export dialog, which remembers them.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDFile;
import com.topodroid.util.TDVersion;
import com.topodroid.util.TDString;
import com.topodroid.types.TDStatus;
import com.topodroid.types.ExportInfo;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefHelper;

import android.os.AsyncTask;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class SilexGisExportTask extends AsyncTask< Void, SilexGisExportTask.Line, SilexGisUploader.Outcome >
                         implements SilexGisUploader.Listener
{
  // the sketch window saves the drawing again when it is closed, a few seconds after a Therion export:
  // a Therion export is said to be older than its sketch only past this margin
  static final long SKETCH_MARGIN = 10 * 60 * 1000L; // [ms]

  /** a progress line
   */
  static class Line
  {
    final String step;
    final int percent;
    final String detail;
    Line( String s, int p, String d ) { step = s; percent = p; detail = d; }
  }

  private final Context mContext; // activity, for the dialogs
  private final TopoDroidApp mApp;
  private final Resources mRes;
  private final String mSurvey;
  private final long mSid;
  private final SurveyInfo mInfo;
  private final SilexGisUpload mUpload;
  private final SilexGisClient mClient;
  private final SilexGisProgressDialog mDialog;
  private final ArrayList< String > mNotes = new ArrayList<>();
  private boolean mSketchesBehind = false; // a sketch has no Therion export, or an old one
  private volatile boolean mCancelled = false;
  private String mStepText = null;
  private int mPercent = -1;

  // ---------------------------------------------------------------------
  // helpers of the export dialog and of the task

  /** @return the store of what is remembered between uploads
   * @param context   context
   */
  static SilexGisStore getStore( Context context )
  {
    final TDPrefHelper hlp = new TDPrefHelper( context );
    return new SilexGisStore( new SilexGisStore.Prefs() {
      @Override public String get( String key ) { return hlp.getString( key, null ); }
      @Override public void put( String key, String value ) { TDPrefHelper.update( key, value ); }
    } );
  }

  /** @return the upload switches of the settings: the defaults before the first upload
   */
  static SilexGisUpload getDefaults()
  {
    SilexGisUpload ret = new SilexGisUpload();
    ret.withTh     = TDSetting.mSilexGisWithTh;
    ret.withSvx    = TDSetting.mSilexGisWithSvx;
    ret.withDat    = TDSetting.mSilexGisWithDat;
    ret.withCsv    = TDSetting.mSilexGisWithCsv;
    ret.withKml    = TDSetting.mSilexGisWithKml;
    ret.withGpx    = TDSetting.mSilexGisWithGpx;
    ret.withOut    = TDSetting.mSilexGisWithOut;
    ret.compile    = TDSetting.mSilexGisCompile;
    ret.outputs    = TDSetting.mSilexGisOutputs;
    if ( ! TDSetting.mSilexGisPublicLink ) ret.setLinkChoice( SilexGisUpload.LINK_NONE ); // else the account decides
    if ( SilexGisUpload.isVisibility( TDSetting.mSilexGisVisibility ) ) ret.visibility = TDSetting.mSilexGisVisibility;
    return ret;
  }

  /** @return the upload to pre-set for a survey
   * @param context   context
   * @param survey    survey name
   */
  static SilexGisUpload loadUpload( Context context, String survey ) { return getStore( context ).load( survey, getDefaults() ); }

  /** @return the title of a survey: its name with spaces for the underscores, as the Therion export writes it
   * @param survey   survey name
   */
  static String surveyTitle( String survey ) { return TDString.underscoresToSpaces( survey ); }

  /** @return the index of a visibility in the visibility list (club if not known)
   * @param visibility   visibility
   */
  static int visibilityIndex( String visibility )
  {
    for ( int k = 0; k < SilexGisUpload.VISIBILITIES.length; ++k ) if ( SilexGisUpload.VISIBILITIES[k].equals( visibility ) ) return k;
    return 1;
  }

  /** @return the text of the target of an upload
   * @param res     resources
   * @param upload  upload
   */
  static String targetText( Resources res, SilexGisUpload upload )
  {
    switch ( upload.target ) {
      case SilexGisUpload.TARGET_CAVE:
        return String.format( res.getString( R.string.silexgis_target_cave ), upload.caveName );
      case SilexGisUpload.TARGET_CAVE_SURVEY:
        return String.format( res.getString( R.string.silexgis_target_survey ), upload.caveSurveyTitle,
          ( upload.caveName == null )? "-" : upload.caveName );
      case SilexGisUpload.TARGET_NEW_CAVE:
        String[] vis = res.getStringArray( R.array.silexgisVisibility );
        return String.format( res.getString( R.string.silexgis_target_new ), upload.newCaveName, vis[ visibilityIndex( upload.newCaveVisibility ) ] );
    }
    return res.getString( R.string.silexgis_target_auto );
  }

  /** @return the text of the outcome of an upload
   * @param res      resources
   * @param outcome  outcome
   */
  static String outcomeText( Resources res, SilexGisUploader.Outcome outcome )
  {
    SilexGisStatus st = outcome.status;
    switch ( outcome.kind ) {
      case SilexGisUploader.SUCCEEDED:
        return String.format( res.getString( R.string.silexgis_result_done ),
          ( st.caveSurveyTitle == null )? "-" : st.caveSurveyTitle, ( st.caveName == null )? "-" : st.caveName );
      case SilexGisUploader.FAILED:
        String why = ( st.errorMessage != null )? st.errorMessage : ( st.errorCode != null )? st.errorCode : st.status;
        if ( st.errorCode != null && st.errorMessage != null ) why = why + " (" + st.errorCode + ")";
        return String.format( res.getString( R.string.silexgis_result_failed ), why );
      case SilexGisUploader.CANCELLED:
        return res.getString( R.string.silexgis_result_cancelled );
      case SilexGisUploader.ABANDONED:
        return res.getString( R.string.silexgis_result_abandoned );
      case SilexGisUploader.NO_CAVE:
        return res.getString( R.string.silexgis_cave_required );
      case SilexGisUploader.FILES:
        SilexGisFiles.Problem p = outcome.problem;
        if ( p == null ) return res.getString( R.string.silexgis_archive_failed );
        switch ( p.kind ) {
          case SilexGisFiles.TOO_LARGE:
            return String.format( res.getString( R.string.silexgis_file_too_large ), p.name, (int)( outcome.maxFileBytes / ( 1024 * 1024 ) ) );
          case SilexGisFiles.TOO_MANY:
            return String.format( res.getString( R.string.silexgis_too_many_files ), outcome.maxFiles );
        }
        return res.getString( R.string.silexgis_archive_failed ); // NO_ARCHIVE
    }
    if ( outcome.kind == SilexGisUploader.KEY_REFUSED && outcome.hasUpload() ) { // the upload is not made again
      return res.getString( outcome.completed ? R.string.silexgis_result_key_watching : R.string.silexgis_result_key_open );
    }
    if ( outcome.result == null ) return res.getString( R.string.silexgis_archive_failed );
    String msg = SilexGisSettingsDialog.errorMessage( res, outcome.result );
    if ( outcome.result.code != null && outcome.result.kind == SilexGisClient.PROBLEM ) msg = msg + " (" + outcome.result.code + ")";
    return msg; // REFUSED, KEY_REFUSED
  }

  // ---------------------------------------------------------------------

  /** start the upload of the current survey, after the server access has been set if it is not
   * @param context   activity context
   * @param app       application
   */
  static void start( final Context context, final TopoDroidApp app )
  {
    Runnable again = new Runnable() { @Override public void run() { start( context, app ); } };
    if ( ! SilexGisSettingsDialog.ensureConfigured( context, again ) ) return;
    SurveyInfo info = TopoDroidApp.getSurveyInfo();
    if ( info == null || TDInstance.survey == null ) {
      TDToast.makeBad( R.string.no_survey );
      return;
    }
    // the pool, not the serial executor: the task watches the server for minutes, and every other task of the app would wait behind it
    (new SilexGisExportTask( context, app, info )).executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR );
  }

  /** cstr - on the UI thread
   * @param context   activity context
   * @param app       application
   * @param info      survey info
   */
  private SilexGisExportTask( Context context, TopoDroidApp app, SurveyInfo info )
  {
    mContext = context;
    mApp     = app;
    mRes     = context.getResources();
    mSurvey  = TDInstance.survey;
    mSid     = TDInstance.sid;
    mInfo    = info;
    mUpload  = loadUpload( context, mSurvey );
    mClient  = new SilexGisClient( TDSetting.mSilexGisUrl, TDSetting.mSilexGisKey );
    mDialog  = new SilexGisProgressDialog( context, new Runnable() { @Override public void run() { mCancelled = true; } } );
    mDialog.show();
  }

  // ---------------------------------------------------------------------
  // background

  @Override
  protected SilexGisUploader.Outcome doInBackground( Void... args )
  {
    SilexGisFiles files = new SilexGisFiles();

    // the limits of the server first: an upload that it would refuse for its target is refused before the survey is archived
    SilexGisUploader uploader = new SilexGisUploader( mClient, null );
    onStep( SilexGisUploader.STEP_CONNECT, null, 0, 0 );
    SilexGisUploader.Outcome refused = uploader.check( mUpload );
    getStore( mContext ).saveLinks( uploader.capabilities() ); // the link choice of the next export dialog starts from them
    if ( refused != null ) return refused;

    // the ordinary survey archive
    onStep( SilexGisUploader.STEP_ARCHIVE, null, 0, 0 );
    while ( ! TopoDroidApp.mEnableZip ) Thread.yield();
    Archiver archiver = new Archiver();
    String zipname = archiver.archiveSurvey( mApp, null )? archiver.getZipname() : null;
    if ( zipname == null || ! files.add( mSurvey + TDPath.ZIP, TDFile.getTopoDroidFile( zipname ), SilexGisFiles.ROLE_ARCHIVE ) ) {
      return localFailure();
    }
    if ( mCancelled ) return abandoned();

    // Therion: the sketches first, they decide whether the survey file writes its map commands live
    if ( mUpload.withTh ) {
      onStep( SilexGisUploader.STEP_THERION, null, 0, 0 );
      boolean all_sketches = true;
      List< PlotInfo > plots = TopoDroidApp.mData.selectAllPlots( mSid, TDStatus.NORMAL );
      for ( PlotInfo plot : plots ) {
        File tdr = TDFile.getTopoDroidFile( TDPath.getSurveyPlotTdrFile( mSurvey, plot.name ) );
        if ( ! tdr.exists() ) continue; // the Therion survey file names only the sketches that have a drawing
        String th2_name = mSurvey + "-" + plot.name + TDPath.TH2;
        File th2 = TDFile.getTopoDroidFile( TDPath.getOutFile( th2_name ) );
        if ( th2.isFile() && th2.length() > 0 ) {
          files.add( th2_name, th2, SilexGisFiles.ROLE_TH2 );
          if ( tdr.lastModified() > th2.lastModified() + SKETCH_MARGIN ) {
            mNotes.add( String.format( mRes.getString( R.string.silexgis_sketch_old ), plot.name ) );
            mSketchesBehind = true;
          }
        } else {
          all_sketches = false;
          mSketchesBehind = true;
          mNotes.add( String.format( mRes.getString( R.string.silexgis_sketch_missing ), plot.name ) );
        }
      }
      // with a sketch missing the map commands stay commented: the server makes live only those of the sketches it has
      String th = exportTherion( all_sketches );
      if ( th == null || ! files.add( mSurvey + TDPath.TH, TDFile.getTopoDroidFile( th ), SilexGisFiles.ROLE_TH ) ) {
        mNotes.add( String.format( mRes.getString( R.string.silexgis_export_failed ), mRes.getString( R.string.therion ) ) );
      }
    }
    if ( mCancelled ) return abandoned();

    // the other formats
    if ( mUpload.withSvx ) exportOther( TDConst.SURVEY_FORMAT_SVX, TDPath.SVX, R.string.survex, files );
    if ( mUpload.withDat ) exportOther( TDConst.SURVEY_FORMAT_DAT, TDPath.DAT, R.string.compass, files );
    if ( mUpload.withCsv ) exportOther( TDConst.SURVEY_FORMAT_CSV, TDPath.CSV, R.string.csv, files );
    if ( mUpload.withKml ) exportOther( TDConst.SURVEY_FORMAT_KML, TDPath.KML, R.string.kml, files );
    if ( mUpload.withGpx ) exportOther( TDConst.SURVEY_FORMAT_GPX, TDPath.GPX, R.string.gpx, files );
    if ( mCancelled ) return abandoned();

    onStep( SilexGisUploader.STEP_COLLECT, null, 0, 0 );
    if ( mUpload.withOut ) {
      List< String > skipped = files.addFolder( TDFile.getTopoDroidFile( TDPath.getOutFile( "" ) ), null ); // the out folder
      if ( ! skipped.isEmpty() ) mNotes.add( String.format( mRes.getString( R.string.silexgis_files_dropped ), join( skipped ) ) );
    }

    SilexGisUploader.Outcome out = uploader.run( mUpload, surveyTitle( mSurvey ), mSurvey, mInfo.comment, TDVersion.string(), files, this );
    if ( ! out.droppedFiles.isEmpty() ) {
      mNotes.add( String.format( mRes.getString( R.string.silexgis_files_dropped ), join( out.droppedFiles ) ) );
    }
    if ( ! out.droppedOutputs.isEmpty() ) {
      mNotes.add( String.format( mRes.getString( R.string.silexgis_outputs_dropped ), join( out.droppedOutputs ) ) );
    }
    if ( mUpload.compile && ! out.compilerAvailable && out.hasUpload() ) {
      mNotes.add( mRes.getString( R.string.silexgis_no_compiler ) );
    }
    return out;
  }

  /** export the Therion survey file into the "out" folder
   * @param uncommented   whether the map commands are written live
   * @return the pathname of the file, null on failure
   * @note the user settings are restored afterwards: the upload asks for the survey file alone, with
   *       the sketches named live when they are all uploaded, and no thconfig (the server writes its own)
   */
  private String exportTherion( boolean uncommented )
  {
    boolean maps   = TDSetting.mTherionUncommentedMaps;
    boolean config = TDSetting.mTherionWithConfig;
    try {
      TDSetting.mTherionUncommentedMaps = uncommented;
      TDSetting.mTherionWithConfig      = false;
      return exportData( TDConst.SURVEY_FORMAT_TH, mSurvey + TDPath.TH );
    } finally {
      TDSetting.mTherionUncommentedMaps = maps;
      TDSetting.mTherionWithConfig      = config;
    }
  }

  /** export another format into the "out" folder and add it to the files
   * @param index   export format
   * @param ext     file extension
   * @param name    format name (resource)
   * @param files   files of the upload
   */
  private void exportOther( int index, String ext, int name, SilexGisFiles files )
  {
    String format = mRes.getString( name );
    onStep( SilexGisUploader.STEP_EXPORT, format, 0, 0 );
    String path = exportData( index, mSurvey + ext );
    if ( path == null || ! files.add( mSurvey + ext, TDFile.getTopoDroidFile( path ), SilexGisFiles.ROLE_OTHER ) ) {
      mNotes.add( String.format( mRes.getString( R.string.silexgis_export_failed ), format ) );
    }
  }

  /** export the survey data into the "out" folder, as the survey export does
   * @param index      export format
   * @param filename   file name
   * @return the pathname of the file, null on failure (also for a geo-format without a geolocalized station)
   */
  private String exportData( int index, String filename )
  {
    TDPath.checkOutdir();
    String pathname = TDPath.getOutFile( filename );
    Uri uri = Uri.fromFile( new File( pathname ) );
    String format = mRes.getString( R.string.saved_file_1 );
    SaveDataFileTask task = new SaveDataFileTask( mApp, uri, format, mSid, mInfo, TopoDroidApp.mData, mSurvey, TDInstance.getDeviceA(),
                                                  new ExportInfo( index, null, filename, -1L ), false, false ); // no toast, no share
    String ret = task.immed_exec();
    if ( ret == null || ret.isEmpty() ) return null;
    return pathname;
  }

  /** @return the names separated by commas
   * @param names   names
   */
  private static String join( List< String > names )
  {
    StringBuilder sb = new StringBuilder();
    for ( String name : names ) {
      if ( sb.length() > 0 ) sb.append( ", " );
      sb.append( name );
    }
    return sb.toString();
  }

  private SilexGisUploader.Outcome localFailure()
  {
    SilexGisUploader.Outcome out = new SilexGisUploader.Outcome();
    out.kind = SilexGisUploader.FILES; // problem null: the archive could not be written
    return out;
  }

  private SilexGisUploader.Outcome abandoned()
  {
    SilexGisUploader.Outcome out = new SilexGisUploader.Outcome();
    out.kind = SilexGisUploader.ABANDONED;
    return out;
  }

  // ---------------------------------------------------------------------
  // SilexGisUploader.Listener - in the background

  @Override
  public void onStep( int step, String name, int k, int n )
  {
    switch ( step ) {
      case SilexGisUploader.STEP_ARCHIVE: mStepText = mRes.getString( R.string.silexgis_step_archive ); break;
      case SilexGisUploader.STEP_THERION: mStepText = mRes.getString( R.string.silexgis_step_therion ); break;
      case SilexGisUploader.STEP_EXPORT:  mStepText = String.format( mRes.getString( R.string.silexgis_step_export ), name ); break;
      case SilexGisUploader.STEP_COLLECT: mStepText = mRes.getString( R.string.silexgis_step_collect ); break;
      case SilexGisUploader.STEP_CONNECT: mStepText = mRes.getString( R.string.silexgis_step_connect ); break;
      case SilexGisUploader.STEP_OPEN:    mStepText = mRes.getString( R.string.silexgis_step_open ); break;
      case SilexGisUploader.STEP_PUT:     mStepText = String.format( mRes.getString( R.string.silexgis_step_put ), name, k, n ); break;
      case SilexGisUploader.STEP_PROCESS: mStepText = mRes.getString( R.string.silexgis_step_process ); break;
    }
    mPercent = 0;
    publishProgress( new Line( mStepText, 0, null ) );
  }

  @Override
  public void onBytes( long sent, long total )
  {
    int percent = ( total > 0 )? (int)( ( 100 * sent ) / total ) : 0;
    if ( percent == mPercent ) return; // one update per percent
    mPercent = percent;
    publishProgress( new Line( null, percent, null ) );
  }

  @Override
  public void onOpened( SilexGisStatus opened )
  {
    rememberAnswer( opened );
  }

  @Override
  public void onStatus( SilexGisStatus status )
  {
    String stage = ( status.stage != null )? status.stage : status.status;
    String detail = String.format( mRes.getString( R.string.silexgis_stage ), stage, status.progress, ( status.message == null )? "" : status.message );
    publishProgress( new Line( null, status.progress, detail ) );
  }

  @Override
  public boolean isStopped() { return mCancelled; }

  /** remember the survey of the cave the server answered, so that the next upload of this survey is a new version of it
   * @param st   answer of the server
   */
  private void rememberAnswer( SilexGisStatus st )
  {
    if ( st == null || st.caveSurveyId == null ) return;
    String title = ( st.caveSurveyTitle != null )? st.caveSurveyTitle : surveyTitle( mSurvey );
    String cave_id = ( st.caveId != null )? st.caveId : mUpload.caveId;
    String cave_name = st.caveName;
    if ( cave_name == null ) {
      switch ( mUpload.target ) {
        case SilexGisUpload.TARGET_CAVE:
        case SilexGisUpload.TARGET_CAVE_SURVEY: cave_name = mUpload.caveName; break;
        case SilexGisUpload.TARGET_NEW_CAVE:    cave_name = mUpload.newCaveName; break;
        default: cave_name = surveyTitle( mSurvey );
      }
    }
    getStore( mContext ).saveAnswer( mSurvey, st.caveSurveyId, title, cave_id, cave_name );
  }

  // ---------------------------------------------------------------------
  // UI thread

  @Override
  protected void onProgressUpdate( Line... lines )
  {
    for ( Line line : lines ) mDialog.setProgress( line.step, line.percent, line.detail );
  }

  @Override
  protected void onPostExecute( SilexGisUploader.Outcome out )
  {
    try {
      mDialog.dismiss();
    } catch ( IllegalArgumentException e ) {
      TDLog.e( "SilexGIS progress dialog already gone" );
    }
    if ( out.status != null && out.status.caveSurveyId != null ) rememberAnswer( out.status );
    if ( mContext instanceof Activity && ( ((Activity)mContext).isFinishing() || ((Activity)mContext).isDestroyed() ) ) {
      TDToast.makeLong( outcomeText( mRes, out ) ); // the window has gone during the upload: no dialog can be shown on it
      return;
    }
    if ( out.mayRestart() ) { // nothing is on the server yet: the upload starts again with the new key
      SilexGisSettingsDialog.showKeyRefused( mContext, new Runnable() { @Override public void run() { start( mContext, mApp ); } } );
      return;
    }
    if ( out.status != null && out.status.olderWarn ) { // past the warning size of the server
      mNotes.add( String.format( mRes.getString( R.string.silexgis_older_versions ), out.status.olderCount, SilexGisClient.sizeText( out.status.olderBytes, -1 ) ) );
    }
    StringBuilder notes = new StringBuilder();
    for ( String note : mNotes ) {
      if ( notes.length() > 0 ) notes.append( "\n" );
      notes.append( note );
    }
    (new SilexGisResultDialog( mContext, mClient, out, notes.toString(), mSketchesBehind, mUpload.linkAudience() )).show();
    if ( out.kind == SilexGisUploader.KEY_REFUSED ) SilexGisSettingsDialog.showKeyRefused( mContext, null ); // over the result: the upload is not sent again
  }

}
