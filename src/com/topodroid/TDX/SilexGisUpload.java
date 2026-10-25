/* @file SilexGisUpload.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload to a SilexGIS server: where to and what to send
 *
 * The target is a cave, a survey of a cave, a new cave, or nothing: without a target
 * the server makes a cave named after the survey title, open to whom the switches say.
 * The class uses only java and org.json, so that it can be tested on the JVM.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

class SilexGisUpload
{
  // target of the upload
  static final int TARGET_AUTO        = 0; // no cave: the server makes one named after the survey title
  static final int TARGET_CAVE        = 1; // a cave picked from the server
  static final int TARGET_CAVE_SURVEY = 2; // the survey of a cave the server answered for an earlier upload of this survey
  static final int TARGET_NEW_CAVE    = 3; // a new cave, with its name and who may see it

  static final String[] VISIBILITIES = { "private", "cavingGroup", "authenticated", "public" };
  static final String VISIBILITY_CLUB = "cavingGroup";

  static final int MAX_DESCRIPTION = 4000; // characters of the description the server keeps

  int    target = TARGET_AUTO;
  String caveId          = null; // TARGET_CAVE, TARGET_CAVE_SURVEY (shown)
  String caveName        = null;
  String caveSurveyId    = null; // TARGET_CAVE_SURVEY
  String caveSurveyTitle = null;
  String newCaveName       = null; // TARGET_NEW_CAVE
  String newCaveVisibility = null;
  boolean newCaveProtected = false; // whether the server is to protect the location of the new cave

  // switches: the last combination is remembered
  boolean withTh     = true;
  boolean withSvx    = false;
  boolean withDat    = false;
  boolean withCsv    = false;
  boolean withKml    = false;
  boolean withGpx    = false;
  boolean withOut    = false;
  boolean compile    = true;
  boolean publicLink = true;
  String  outputs    = "lox,3d,plan-pdf,extended-pdf"; // comma separated
  String  visibility = "cavingGroup"; // documents of the upload, and the cave the server makes without a target

  /** default cstr: the defaults of the settings
   */
  SilexGisUpload() { }

  /** @return a copy of this upload
   */
  SilexGisUpload copy()
  {
    SilexGisUpload ret = new SilexGisUpload();
    ret.fromJson( toJson() );
    return ret;
  }

  /** @return true if the value is one of the visibilities the server knows
   * @param v   visibility
   */
  static boolean isVisibility( String v )
  {
    if ( v == null ) return false;
    for ( String vis : VISIBILITIES ) if ( vis.equals( v ) ) return true;
    return false;
  }

  /** @return the list of the outputs: lowercase, without blanks and repetitions
   */
  List< String > outputList()
  {
    ArrayList< String > ret = new ArrayList<>();
    if ( outputs == null ) return ret;
    for ( String name : outputs.split("[,\\s]+") ) {
      if ( name.isEmpty() ) continue;
      name = name.toLowerCase( Locale.US );
      if ( ! ret.contains( name ) ) ret.add( name );
    }
    return ret;
  }

  // ---------------------------------------------------------------------
  // remembered forms

  /** @return the switches as JSON
   */
  JSONObject switchesToJson()
  {
    JSONObject ret = new JSONObject();
    try {
      ret.put( "withTh",     withTh );
      ret.put( "withSvx",    withSvx );
      ret.put( "withDat",    withDat );
      ret.put( "withCsv",    withCsv );
      ret.put( "withKml",    withKml );
      ret.put( "withGpx",    withGpx );
      ret.put( "withOut",    withOut );
      ret.put( "compile",    compile );
      ret.put( "publicLink", publicLink );
      ret.put( "outputs",    outputs );
      ret.put( "visibility", visibility );
    } catch ( JSONException e ) { /* cannot happen with these values */ }
    return ret;
  }

  /** set the switches from JSON: a missing member keeps its value
   * @param json   remembered switches
   */
  void switchesFromJson( JSONObject json )
  {
    if ( json == null ) return;
    withTh     = json.optBoolean( "withTh",     withTh );
    withSvx    = json.optBoolean( "withSvx",    withSvx );
    withDat    = json.optBoolean( "withDat",    withDat );
    withCsv    = json.optBoolean( "withCsv",    withCsv );
    withKml    = json.optBoolean( "withKml",    withKml );
    withGpx    = json.optBoolean( "withGpx",    withGpx );
    withOut    = json.optBoolean( "withOut",    withOut );
    compile    = json.optBoolean( "compile",    compile );
    publicLink = json.optBoolean( "publicLink", publicLink );
    outputs    = optString( json, "outputs", outputs );
    String vis = optString( json, "visibility", visibility );
    if ( isVisibility( vis ) ) visibility = vis;
  }

  /** @return the target as JSON
   */
  JSONObject targetToJson()
  {
    JSONObject ret = new JSONObject();
    try {
      ret.put( "target", target );
      if ( caveId            != null ) ret.put( "caveId",            caveId );
      if ( caveName          != null ) ret.put( "caveName",          caveName );
      if ( caveSurveyId      != null ) ret.put( "caveSurveyId",      caveSurveyId );
      if ( caveSurveyTitle   != null ) ret.put( "caveSurveyTitle",   caveSurveyTitle );
      if ( newCaveName       != null ) ret.put( "newCaveName",       newCaveName );
      if ( newCaveVisibility != null ) ret.put( "newCaveVisibility", newCaveVisibility );
      if ( newCaveProtected ) ret.put( "newCaveProtected", true );
    } catch ( JSONException e ) { /* cannot happen with these values */ }
    return ret;
  }

  /** set the target from JSON; a target that lacks what it needs becomes automatic
   * @param json   remembered target
   */
  void targetFromJson( JSONObject json )
  {
    setAuto();
    if ( json == null ) return;
    int t = json.optInt( "target", TARGET_AUTO );
    caveId            = optString( json, "caveId", null );
    caveName          = optString( json, "caveName", null );
    caveSurveyId      = optString( json, "caveSurveyId", null );
    caveSurveyTitle   = optString( json, "caveSurveyTitle", null );
    newCaveName       = optString( json, "newCaveName", null );
    newCaveVisibility = optString( json, "newCaveVisibility", null );
    newCaveProtected  = json.optBoolean( "newCaveProtected", false );
    switch ( t ) {
      case TARGET_CAVE:        if ( caveId != null )       target = t; break;
      case TARGET_CAVE_SURVEY: if ( caveSurveyId != null ) target = t; break;
      case TARGET_NEW_CAVE:    if ( newCaveName != null && isVisibility( newCaveVisibility ) ) target = t; break;
    }
    if ( target == TARGET_AUTO ) setAuto();
  }

  /** @return the whole upload as JSON: target and switches
   */
  JSONObject toJson()
  {
    JSONObject ret = new JSONObject();
    try {
      ret.put( "switches", switchesToJson() );
      ret.put( "target",   targetToJson() );
    } catch ( JSONException e ) { /* cannot happen */ }
    return ret;
  }

  /** set the whole upload from JSON
   * @param json   upload
   */
  void fromJson( JSONObject json )
  {
    if ( json == null ) return;
    switchesFromJson( json.optJSONObject( "switches" ) );
    targetFromJson( json.optJSONObject( "target" ) );
  }

  // ---------------------------------------------------------------------
  // targets

  /** no target: the server makes a cave named after the survey title
   */
  void setAuto()
  {
    target = TARGET_AUTO;
    caveId = caveName = caveSurveyId = caveSurveyTitle = null;
    newCaveName = newCaveVisibility = null;
    newCaveProtected = false;
  }

  /** upload to a cave
   * @param id     cave id
   * @param name   cave name (shown)
   */
  void setCave( String id, String name )
  {
    setAuto();
    target   = TARGET_CAVE;
    caveId   = id;
    caveName = name;
  }

  /** upload a new version of a survey of a cave
   * @param id         cave-survey id answered by the server
   * @param title      survey title (shown)
   * @param cave_id    cave id (can be null)
   * @param cave_name  cave name (shown, can be null)
   */
  void setCaveSurvey( String id, String title, String cave_id, String cave_name )
  {
    setAuto();
    target          = TARGET_CAVE_SURVEY;
    caveSurveyId    = id;
    caveSurveyTitle = title;
    caveId          = cave_id;
    caveName        = cave_name;
  }

  /** upload to a new cave
   * @param name        cave name
   * @param visibility  who may see the cave (a club-visible cave goes to the club of the account)
   * @param protect     whether the server is to protect the location of the cave
   */
  void setNewCave( String name, String visibility, boolean protect )
  {
    setAuto();
    target            = TARGET_NEW_CAVE;
    newCaveName       = name;
    newCaveVisibility = visibility;
    newCaveProtected  = protect;
  }

  /** @return true if the server is to make a cave for the upload: without a target, or with a new cave
   */
  boolean makesCave() { return target == TARGET_AUTO || target == TARGET_NEW_CAVE; }

  // ---------------------------------------------------------------------
  // request

  /** @return the body of the request that opens the upload (POST /api/v1/survey-uploads)
   * @param survey_title   survey title: the server groups the results under it, and names a new cave after it
   * @param survey_name    TopoDroid survey name: a later upload of the same survey is matched by it
   * @param description    survey description (can be null)
   * @param app_version    TopoDroid version
   * @param files          files of the upload
   * @param compiler_outputs outputs the server can produce (null if not known): the others are not asked for
   * @param compiler_available whether the server compiles
   */
  JSONObject openRequest( String survey_title, String survey_name, String description, String app_version,
                          List< SilexGisFiles.Item > files, List< String > compiler_outputs, boolean compiler_available )
  {
    JSONObject ret = new JSONObject();
    try {
      ret.put( "source", "topodroid" );
      JSONObject client = new JSONObject();
      client.put( "name", "TopoDroid" );
      client.put( "version", ( app_version == null )? "" : app_version );
      ret.put( "client", client );
      switch ( target ) {
        case TARGET_CAVE:
          ret.put( "cave", ( new JSONObject() ).put( "id", caveId ) );
          break;
        case TARGET_CAVE_SURVEY:
          ret.put( "caveSurvey", ( new JSONObject() ).put( "id", caveSurveyId ) );
          break;
        case TARGET_NEW_CAVE:
          JSONObject cave = new JSONObject();
          cave.put( "name", newCaveName );
          cave.put( "visibility", newCaveVisibility );
          cave.put( "locationProtected", newCaveProtected ); // a server that does not know it yet ignores it
          ret.put( "newCave", cave );
          break;
        default: // TARGET_AUTO: a new cave with no name, so that the server names it after the survey title
                 // (numbered when a cave the uploader sees has that name), open to whom the switches say.
                 // The club is not named: the server's default is the club of the account, else private, while
                 // a club named outright is refused to an account without one or with several
          if ( isVisibility( visibility ) && ! VISIBILITY_CLUB.equals( visibility ) ) {
            ret.put( "newCave", ( new JSONObject() ).put( "visibility", visibility ) );
          }
      }
      ret.put( "surveyTitle", survey_title );
      ret.put( "surveyName",  survey_name );
      if ( description != null ) {
        description = description.trim();
        if ( description.length() > MAX_DESCRIPTION ) description = description.substring( 0, MAX_DESCRIPTION );
        if ( ! description.isEmpty() ) ret.put( "description", description );
      }
      JSONArray array = new JSONArray();
      if ( files != null ) {
        for ( SilexGisFiles.Item item : files ) {
          JSONObject file = new JSONObject();
          file.put( "name", item.name );
          file.put( "size", item.size );
          file.put( "role", item.role );
          array.put( file );
        }
      }
      ret.put( "files", array );
      JSONObject pipeline = new JSONObject();
      boolean compiled = compile && compiler_available;
      pipeline.put( "compile", compiled );
      List< String > asked = askedOutputs( compiler_outputs );
      if ( compiled && ! asked.isEmpty() ) { // none: the server default
        JSONArray outs = new JSONArray();
        for ( String out : asked ) outs.put( out );
        pipeline.put( "outputs", outs );
      }
      ret.put( "pipeline", pipeline );
      if ( isVisibility( visibility ) ) ret.put( "documentsVisibility", visibility );
      // ret.put( "publicLink", publicLink ); // the server reads no such member yet: the account preference in SilexGIS decides
    } catch ( JSONException e ) { /* cannot happen with these values */ }
    return ret;
  }

  /** @return the outputs to ask for: those of the switches that the server can produce
   * @param compiler_outputs  outputs the server can produce (null if not known: all are asked)
   */
  List< String > askedOutputs( List< String > compiler_outputs )
  {
    List< String > ret = outputList();
    if ( compiler_outputs == null ) return ret;
    ArrayList< String > asked = new ArrayList<>();
    for ( String out : ret ) if ( compiler_outputs.contains( out ) ) asked.add( out );
    return asked;
  }

  /** @return the outputs of the switches that the server cannot produce
   * @param compiler_outputs  outputs the server can produce (null if not known: none is dropped)
   */
  List< String > droppedOutputs( List< String > compiler_outputs )
  {
    ArrayList< String > ret = new ArrayList<>();
    if ( compiler_outputs == null ) return ret;
    for ( String out : outputList() ) if ( ! compiler_outputs.contains( out ) ) ret.add( out );
    return ret;
  }

  /** @return a string member of a JSON object, or the default if missing or null
   * @param json   JSON object
   * @param name   member name
   * @param dflt   default
   */
  static String optString( JSONObject json, String name, String dflt )
  {
    if ( json == null || ! json.has( name ) || json.isNull( name ) ) return dflt;
    return json.optString( name, dflt );
  }

}
