/* @file SilexGisStatus.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload to a SilexGIS server: the status of an upload, as the server answers it
 *
 * The class uses only java and org.json, so that it can be tested on the JVM.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

class SilexGisStatus
{
  static final String RECEIVING = "receiving";
  static final String QUEUED    = "queued";
  static final String RUNNING   = "running";
  static final String SUCCEEDED = "succeeded";
  static final String FAILED    = "failed";
  static final String ABANDONED = "abandoned";

  /** a result of the processing of an upload
   */
  static class Artefact
  {
    String kind       = ""; // lox, 3d, plan-pdf, ..., log
    String name       = "";
    long   size       = -1;
    String contentUrl = null; // signed, short-lived
    String viewUrl    = null; // web path, relative to the server base URL
    String errorCode    = null; // what is wrong with this output, e.g. asked for and not produced (it then has no file)
    String errorMessage = null;

    /** @return true if the artefact is a 3D model that Cave3D opens
     */
    boolean isModel() { return "lox".equals( kind ) || "3d".equals( kind ); }
  }

  String id       = null;
  String status   = "";
  String stage    = null;
  int    progress = 0;
  String message  = null;
  String errorCode    = null;
  String errorMessage = null;
  String caveId          = null;
  String caveName        = null;
  String caveSurveyId    = null;
  String caveSurveyTitle = null;
  String version         = null; // UTC time of the upload: it names the version of the survey
  String statusPageUrl   = null; // web path of the status page
  String publicLinkUrl   = null; // web path (or URL) of the public link, null if none
  String publicLinkRefusal = null; // why there is no public link, if the server says
  final ArrayList< Artefact > artefacts = new ArrayList<>();

  /** @return true if the server does no more work on the upload
   */
  boolean isFinished() { return SUCCEEDED.equals( status ) || FAILED.equals( status ) || ABANDONED.equals( status ); }

  /** @return true if the processing succeeded
   */
  boolean isSucceeded() { return SUCCEEDED.equals( status ); }

  /** @return the artefacts that can be downloaded
   */
  List< Artefact > downloadable()
  {
    ArrayList< Artefact > ret = new ArrayList<>();
    for ( Artefact a : artefacts ) if ( a.contentUrl != null ) ret.add( a );
    return ret;
  }

  /** @return the artefacts the server tells something wrong about (an output not produced, a model it could not read)
   */
  List< Artefact > withError()
  {
    ArrayList< Artefact > ret = new ArrayList<>();
    for ( Artefact a : artefacts ) if ( a.errorCode != null ) ret.add( a );
    return ret;
  }

  /** @return the artefact of the same kind and name, null if none
   * @param a   artefact of another status of the upload
   */
  Artefact find( Artefact a )
  {
    if ( a == null ) return null;
    for ( Artefact b : artefacts ) if ( b.kind.equals( a.kind ) && b.name.equals( a.name ) ) return b;
    return null;
  }

  /** @return the status of an upload, from the answer of the server (GET /api/v1/survey-uploads/{id})
   * @param json   answer of the server
   */
  static SilexGisStatus parse( JSONObject json )
  {
    SilexGisStatus ret = new SilexGisStatus();
    if ( json == null ) return ret;
    ret.id       = text( json, "id" );
    String st    = text( json, "status" );
    ret.status   = ( st == null )? "" : st;
    ret.stage    = text( json, "stage" );
    ret.progress = Math.max( 0, Math.min( 100, json.optInt( "progress", 0 ) ) );
    ret.message  = text( json, "message" );
    JSONObject error = json.optJSONObject( "error" );
    if ( error != null ) {
      ret.errorCode    = text( error, "code" );
      ret.errorMessage = text( error, "message" );
    }
    JSONObject cave = json.optJSONObject( "cave" );
    if ( cave != null ) {
      ret.caveId   = text( cave, "id" );
      ret.caveName = text( cave, "name" );
    }
    JSONObject survey = json.optJSONObject( "caveSurvey" );
    if ( survey != null ) {
      ret.caveSurveyId    = text( survey, "id" );
      ret.caveSurveyTitle = text( survey, "title" );
    }
    ret.version       = text( json, "version" );
    ret.statusPageUrl = text( json, "statusPageUrl" );
    JSONObject link = json.optJSONObject( "publicLink" );
    if ( link != null ) {
      ret.publicLinkUrl = text( link, "url" );
    } else {
      ret.publicLinkUrl = null;
    }
    ret.publicLinkRefusal = text( json, "publicLinkRefusal" );
    if ( ret.publicLinkRefusal == null ) {
      JSONObject refusal = json.optJSONObject( "publicLinkRefusal" );
      if ( refusal != null ) {
        ret.publicLinkRefusal = text( refusal, "message" );
        if ( ret.publicLinkRefusal == null ) ret.publicLinkRefusal = text( refusal, "code" );
      }
    }
    JSONArray array = json.optJSONArray( "artefacts" );
    if ( array != null ) {
      for ( int k = 0; k < array.length(); ++k ) {
        JSONObject item = array.optJSONObject( k );
        if ( item == null ) continue;
        Artefact a = new Artefact();
        String kind = text( item, "kind" );
        a.kind = ( kind == null )? "" : kind;
        String name = text( item, "name" );
        a.name = ( name == null )? a.kind : name;
        a.size = item.optLong( "sizeBytes", -1 );
        a.contentUrl = text( item, "contentUrl" );
        a.viewUrl    = text( item, "viewUrl" );
        JSONObject err = item.optJSONObject( "error" );
        if ( err != null ) {
          a.errorCode    = text( err, "code" );
          a.errorMessage = text( err, "message" );
        }
        ret.artefacts.add( a );
      }
    }
    return ret;
  }

  /** @return a non-empty string member of a JSON object, or null
   * @param json   JSON object
   * @param name   member name
   */
  private static String text( JSONObject json, String name )
  {
    if ( json == null || ! json.has( name ) || json.isNull( name ) ) return null;
    Object v = json.opt( name );
    if ( ! ( v instanceof String ) ) return null; // an object where a text is expected is not a text
    String s = ( (String)v ).trim();
    return s.isEmpty()? null : s;
  }

}
