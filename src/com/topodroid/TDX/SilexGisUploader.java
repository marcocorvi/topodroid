/* @file SilexGisUploader.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload to a SilexGIS server: the conversation with the server
 *
 * capabilities (limits) -> open the upload -> send each file -> complete -> watch the status
 * until the server has processed the survey. Stopping while the files are sent abandons the
 * upload, and so does a file or a completion the server refuses; stopping while the server
 * processes it only stops watching: the server goes on.
 * The class uses only java and org.json, so that it can be tested on the JVM.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

class SilexGisUploader
{
  // steps, in the order they are made (the first ones are made by the export task on the device)
  static final int STEP_ARCHIVE  = 2; // archiving the survey
  static final int STEP_THERION  = 3; // exporting the Therion files
  static final int STEP_EXPORT   = 4; // exporting another format
  static final int STEP_COLLECT  = 5; // collecting the files
  static final int STEP_CONNECT  = 6; // asking the server its limits
  static final int STEP_OPEN     = 7; // opening the upload
  static final int STEP_PUT      = 8; // sending a file
  static final int STEP_PROCESS  = 9; // the server processes the survey

  // outcomes
  static final int SUCCEEDED   = 0; // the server processed the survey
  static final int FAILED      = 1; // the server could not process the survey: the status says why
  static final int CANCELLED   = 2; // stopped watching: the server goes on processing the survey
  static final int REFUSED     = 3; // a call failed: the result says why
  static final int KEY_REFUSED = 4; // the access key was refused
  static final int FILES       = 5; // the files do not fit the limits of the server: the problem says which
  static final int ABANDONED   = 6; // stopped while sending the files: the upload is abandoned
  static final int NO_CAVE     = 7; // the target needs a new cave, and the server makes no cave from an upload

  static final int  MAX_POLL_FAILURES = 3;   // network failures in a row while watching the status
  static final long PAUSE_SLICE       = 250; // [ms] a pause is checked for a stop at this interval

  /** listener of the upload
   */
  interface Listener
  {
    /** a step begins
     * @param step   step
     * @param name   file (only for STEP_PUT)
     * @param k      file number, from 1 (only for STEP_PUT)
     * @param n      number of files (only for STEP_PUT)
     */
    void onStep( int step, String name, int k, int n );

    /** progress of the sending of a file
     * @param sent    bytes sent
     * @param total   bytes of the file
     */
    void onBytes( long sent, long total );

    /** the server opened the upload
     * @param opened   answer of the server: upload id, cave, survey of the cave
     */
    void onOpened( SilexGisStatus opened );

    /** a status of the upload
     * @param status  status
     */
    void onStatus( SilexGisStatus status );

    /** @return true if the user asked to stop
     */
    boolean isStopped();
  }

  /** pause between two status requests
   */
  interface Sleeper
  {
    void sleep( long ms ) throws InterruptedException;
  }

  /** what the upload came to
   */
  static class Outcome
  {
    int kind = REFUSED;
    SilexGisClient.Result result  = null; // the call that failed (REFUSED, KEY_REFUSED)
    SilexGisStatus        status  = null; // the last status (SUCCEEDED, FAILED, CANCELLED)
    SilexGisFiles.Problem problem = null; // FILES
    String uploadId = null;
    String statusPageUrl = null;          // web path of the status page, from the opening
    boolean completed = false;            // the server accepted the completion: it processes the files
    long   maxFileBytes = SilexGisFiles.MAX_FILE_BYTES;
    int    maxFiles     = SilexGisFiles.MAX_FILES;
    boolean compilerAvailable = true;
    final ArrayList< String > droppedOutputs = new ArrayList<>(); // asked outputs the server does not produce
    final ArrayList< String > droppedFiles   = new ArrayList<>(); // files whose kind the server does not accept

    /** @return true if the server has the upload (the status page can be opened)
     */
    boolean hasUpload() { return uploadId != null; }

    /** @return true if the upload can be made again from the start once a new access key is set:
     *          only when the key was refused before the server opened the upload. Later the server
     *          has the files (archived as they arrive), and a new upload would send them all again
     */
    boolean mayRestart() { return kind == KEY_REFUSED && uploadId == null; }
  }

  private final SilexGisClient mClient;
  private final Sleeper mSleeper;
  private JSONObject mCaps = null; // capabilities of the server, asked once

  /** cstr
   * @param client   client of the server
   * @param sleeper  pause between two status requests (null for Thread.sleep)
   */
  SilexGisUploader( SilexGisClient client, Sleeper sleeper )
  {
    mClient  = client;
    mSleeper = ( sleeper != null )? sleeper : new Sleeper() {
      @Override public void sleep( long ms ) throws InterruptedException { Thread.sleep( ms ); }
    };
  }

  /** upload a survey and watch the server process it
   * @param upload       where to and what
   * @param title        survey title
   * @param name         survey name
   * @param description  survey description (can be null)
   * @param version      TopoDroid version
   * @param files        files to upload
   * @param listener     listener (not null)
   * @return the outcome
   */
  Outcome run( SilexGisUpload upload, String title, String name, String description, String version,
               SilexGisFiles files, Listener listener )
  {
    Outcome out = new Outcome();

    // limits of the server, unless check() has asked them already
    if ( mCaps == null ) listener.onStep( STEP_CONNECT, null, 0, 0 );
    Outcome refused = check( upload );
    if ( refused != null ) return refused;
    JSONObject json = mCaps;
    out.maxFileBytes = json.optLong( "maxFileBytes", SilexGisFiles.MAX_FILE_BYTES );
    if ( out.maxFileBytes <= 0 ) out.maxFileBytes = SilexGisFiles.MAX_FILE_BYTES;
    out.maxFiles = json.optInt( "maxFilesPerUpload", SilexGisFiles.MAX_FILES );
    if ( out.maxFiles <= 0 ) out.maxFiles = SilexGisFiles.MAX_FILES;
    List< String > accepted = lowercase( json.optJSONArray( "acceptedExtensions" ) );
    JSONObject compiler = json.optJSONObject( "compiler" );
    out.compilerAvailable = ( compiler != null ) && compiler.optBoolean( "available", false );
    List< String > outputs = ( compiler == null )? null : lowercase( compiler.optJSONArray( "outputs" ) );
    if ( upload.compile && out.compilerAvailable ) out.droppedOutputs.addAll( upload.droppedOutputs( outputs ) );
    long poll = 1000L * Math.max( 1, json.optInt( "pollIntervalSeconds", 3 ) );

    out.droppedFiles.addAll( files.dropNotAccepted( accepted ) );
    out.problem = files.check( out.maxFileBytes, out.maxFiles );
    if ( out.problem != null ) {
      out.kind = FILES;
      return out;
    }
    if ( listener.isStopped() ) {
      out.kind = ABANDONED;
      return out;
    }

    // opening
    listener.onStep( STEP_OPEN, null, 0, 0 );
    JSONObject body = upload.openRequest( title, name, description, version, files.items(), outputs, out.compilerAvailable );
    SilexGisClient.Result opened = mClient.openUpload( body );
    if ( ! opened.isOk() ) return failed( out, opened );
    SilexGisStatus open = SilexGisStatus.parse( opened.json );
    if ( open.id == null ) {
      out.result = opened;
      out.kind = REFUSED;
      return out;
    }
    out.uploadId = open.id;
    out.statusPageUrl = open.statusPageUrl;
    listener.onOpened( open );

    // files
    List< SilexGisFiles.Item > items = files.items();
    int n = items.size();
    for ( int k = 0; k < n; ++k ) {
      SilexGisFiles.Item item = items.get( k );
      listener.onStep( STEP_PUT, item.name, k+1, n );
      String path = SilexGisClient.filePath( out.uploadId, item.name ); // each file has its route: the answer names none
      SilexGisClient.Result r = put( path, item.file, listener );
      if ( r.kind == SilexGisClient.NETWORK && ! listener.isStopped() ) {
        r = put( path, item.file, listener ); // once again: the server answers a file it already has as the first time
      }
      if ( r.kind == SilexGisClient.CANCELLED || listener.isStopped() ) return abandon( out );
      if ( ! r.isOk() ) return refusedOpen( out, r );
    }
    if ( listener.isStopped() ) return abandon( out );

    // processing
    listener.onStep( STEP_PROCESS, null, 0, 0 );
    SilexGisClient.Result completed = mClient.completeUpload( out.uploadId );
    if ( ! completed.isOk() ) return refusedOpen( out, completed );
    out.completed = true;
    SilexGisStatus status = SilexGisStatus.parse( completed.json );
    out.status = status;
    int failures = 0;
    while ( true ) {
      if ( listener.isStopped() ) {
        out.kind = CANCELLED;
        return out;
      }
      SilexGisClient.Result r = mClient.uploadStatus( out.uploadId );
      if ( r.isOk() ) {
        failures = 0;
        status = SilexGisStatus.parse( r.json );
        out.status = status;
        listener.onStatus( status );
        if ( status.isFinished() ) {
          out.kind = status.isSucceeded()? SUCCEEDED : FAILED;
          return out;
        }
      } else if ( r.kind == SilexGisClient.NETWORK ) {
        if ( ++ failures > MAX_POLL_FAILURES ) return failed( out, r );
      } else {
        return failed( out, r );
      }
      if ( ! pause( poll, listener ) ) {
        out.kind = CANCELLED;
        return out;
      }
    }
  }

  /** ask the server its limits, and refuse at once an upload that it would refuse for its target
   * @param upload   where to and what
   * @return null if the upload can go on, else its outcome (the server has nothing of it)
   * @note the export task calls this before it archives and exports the survey
   */
  Outcome check( SilexGisUpload upload )
  {
    if ( mCaps == null ) {
      SilexGisClient.Result caps = mClient.capabilities();
      if ( ! caps.isOk() ) return failed( new Outcome(), caps );
      mCaps = ( caps.json != null )? caps.json : new JSONObject();
    }
    if ( upload.makesCave() && ! caveCreationAllowed( mCaps ) ) {
      Outcome out = new Outcome();
      out.kind = NO_CAVE;
      return out;
    }
    return null;
  }

  /** @return the capabilities of the server, null if they have not been asked
   */
  JSONObject capabilities() { return mCaps; }

  /** @return the link audience the account prefers, null if the server does not say
   * @param caps   capabilities (can be null)
   */
  static String accountLinkAudience( JSONObject caps )
  {
    JSONObject links = ( caps == null )? null : caps.optJSONObject( "publicLinks" );
    String audience = ( links == null )? null : links.optString( "defaultAudience", null );
    return SilexGisUpload.isAudience( audience )? audience : null;
  }

  /** @return whether the server makes a cave for an upload that names none, or asks a new one
   * @param caps   capabilities (null if not known: allowed)
   */
  static boolean caveCreationAllowed( JSONObject caps )
  {
    JSONObject creation = ( caps == null )? null : caps.optJSONObject( "caveCreation" );
    return creation == null || creation.optBoolean( "allowed", true );
  }

  /** @return the visibilities a cave made by the server can have, in the order of SilexGisUpload.VISIBILITIES
   * @param caps   capabilities (null if not known: all)
   */
  static List< String > caveVisibilities( JSONObject caps )
  {
    JSONObject creation = ( caps == null )? null : caps.optJSONObject( "caveCreation" );
    JSONArray array = ( creation == null )? null : creation.optJSONArray( "visibilities" );
    ArrayList< String > ret = new ArrayList<>();
    for ( String vis : SilexGisUpload.VISIBILITIES ) {
      boolean ok = ( array == null );
      for ( int k = 0; ! ok && k < array.length(); ++k ) ok = vis.equals( array.optString( k, "" ) );
      if ( ok ) ret.add( vis );
    }
    if ( ret.isEmpty() ) ret.addAll( Arrays.asList( SilexGisUpload.VISIBILITIES ) ); // none known: the server says no at the opening
    return ret;
  }

  /** @return true if the pause ended, false if the user asked to stop or the thread was interrupted
   * @param ms        pause [ms]
   * @param listener  listener
   */
  private boolean pause( long ms, Listener listener )
  {
    try {
      for ( long t = 0; t < ms; t += PAUSE_SLICE ) {
        if ( listener.isStopped() ) return false;
        mSleeper.sleep( Math.min( PAUSE_SLICE, ms - t ) );
      }
    } catch ( InterruptedException e ) {
      return false;
    }
    return ! listener.isStopped();
  }

  /** send one file
   */
  private SilexGisClient.Result put( String path, File file, final Listener listener )
  {
    return mClient.putFile( path, file, new SilexGisClient.Progress() {
      @Override public boolean onProgress( long done, long total ) {
        listener.onBytes( done, total );
        return ! listener.isStopped();
      }
    } );
  }

  /** abandon the upload after a stop while the files are sent
   */
  private Outcome abandon( Outcome out )
  {
    if ( out.uploadId != null ) mClient.abandonUpload( out.uploadId ); // best effort: an upload not completed expires anyway
    out.kind = ABANDONED;
    return out;
  }

  /** @return the outcome of a call that failed while the upload is open (files, completion):
   *          the upload is abandoned, so that the server does not keep it open until it expires
   */
  private Outcome refusedOpen( Outcome out, SilexGisClient.Result r )
  {
    // best effort; with the key refused the abandon would be refused too, and an upload the server
    // has already queued is not abandoned (the server refuses it)
    if ( r.kind != SilexGisClient.KEY_REFUSED ) mClient.abandonUpload( out.uploadId );
    return failed( out, r );
  }

  /** @return the outcome of a failed call
   */
  private static Outcome failed( Outcome out, SilexGisClient.Result r )
  {
    out.result = r;
    out.kind = ( r.kind == SilexGisClient.KEY_REFUSED )? KEY_REFUSED : REFUSED;
    return out;
  }

  /** @return the strings of a JSON array, lowercase; null if the array is null
   * @param array   JSON array
   */
  private static List< String > lowercase( JSONArray array )
  {
    if ( array == null ) return null;
    ArrayList< String > ret = new ArrayList<>();
    for ( int k = 0; k < array.length(); ++k ) {
      String s = array.optString( k, "" ).trim();
      if ( ! s.isEmpty() ) ret.add( s.toLowerCase( Locale.US ) );
    }
    return ret;
  }

  // ---------------------------------------------------------------------
  // artefacts

  /** download an artefact; a signed content URL that has expired is asked again with the status of the upload, once
   * @param upload_id   upload id
   * @param artefact    artefact
   * @param dest        destination file
   * @param progress    progress listener (can be null)
   * @return the result of the download
   */
  SilexGisClient.Result fetchArtefact( String upload_id, SilexGisStatus.Artefact artefact, File dest, SilexGisClient.Progress progress )
  {
    SilexGisClient.Result r = null;
    if ( artefact.contentUrl != null ) {
      r = mClient.download( artefact.contentUrl, dest, progress );
      if ( r.isOk() || ! mayHaveExpired( r ) || upload_id == null ) return r;
    }
    if ( upload_id == null ) return new SilexGisClient.Result( SilexGisClient.BAD_URL, 0, null, null, null, null, SilexGisClient.URL_INVALID );
    SilexGisClient.Result st = mClient.uploadStatus( upload_id );
    if ( ! st.isOk() ) return st;
    SilexGisStatus.Artefact again = SilexGisStatus.parse( st.json ).find( artefact );
    if ( again == null || again.contentUrl == null ) {
      return ( r != null )? r : new SilexGisClient.Result( SilexGisClient.BAD_URL, 0, null, null, null, null, SilexGisClient.URL_INVALID );
    }
    artefact.contentUrl = again.contentUrl;
    return mClient.download( again.contentUrl, dest, progress );
  }

  /** @return true if a failed download can be due to a signed URL that has expired
   * @param r   result of the download
   */
  static boolean mayHaveExpired( SilexGisClient.Result r )
  {
    if ( r.kind == SilexGisClient.KEY_REFUSED ) return true;
    if ( r.kind != SilexGisClient.PROBLEM ) return false;
    return r.status == 403 || r.status == 404 || r.status == 410;
  }

}
