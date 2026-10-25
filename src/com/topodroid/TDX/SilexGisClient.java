/* @file SilexGisClient.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid client of a SilexGIS server (HTTP, JSON)
 *
 * Every call carries the personal access key as "Authorization: Bearer sgx_...".
 * The class uses only java.net and org.json, without android classes, so that it
 * can be tested on the JVM.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class SilexGisClient
{
  public static final int CONTRACT_VERSION = 1; // survey-upload contract version, sent with every call

  static final int CONNECT_TIMEOUT = 15000; // [ms]
  static final int READ_TIMEOUT    = 30000; // [ms]
  static final int PUT_READ_TIMEOUT = 120000; // [ms] the server stores a file before it answers

  // base-URL check
  public static final int URL_OK        = 0;
  public static final int URL_EMPTY     = 1; // no URL
  public static final int URL_INVALID   = 2; // not a http(s) URL
  public static final int URL_CLEARTEXT = 3; // http to a host that is not local

  // result kinds
  public static final int OK               = 0; // 2xx with a JSON object
  public static final int NOT_CONFIGURED   = 1; // no URL or no key
  public static final int BAD_URL          = 2; // URL refused (see the url field of the result)
  public static final int KEY_REFUSED      = 3; // 401: the access key is not valid (revoked, expired, mistyped)
  public static final int KEY_NOT_ACCEPTED = 4; // 403 auth.access_key_not_accepted: the route does not accept an access key
  public static final int PROBLEM          = 5; // any other refusal, with the Problem Details of the server
  public static final int NETWORK          = 6; // the server could not be reached
  public static final int BAD_RESPONSE     = 7; // the answer is not a JSON object
  public static final int BAD_KEY          = 8; // the access key is not of the form sgx_... (refused without connecting)
  public static final int CANCELLED        = 9; // a download stopped on request of the progress listener
  public static final int FILE_ERROR       = 10; // a download could not be written to the file (see the detail of the result)

  public static final int KEY_PREFIX_LENGTH = 12; // characters of the key shown, as the server lists the keys by their first 12 characters

  public static final String CODE_KEY_NOT_ACCEPTED = "auth.access_key_not_accepted";

  /** result of a call
   */
  public static class Result
  {
    public final int kind;
    public final int status;      // HTTP status (0 if there was no answer)
    public final String code;     // Problem Details "code" (null if none)
    public final String title;    // Problem Details "title" (null if none)
    public final String detail;   // Problem Details "detail", or the network error (null if none)
    public final JSONObject json; // answer (only if kind is OK)
    public final int url;         // base-URL check (only if kind is BAD_URL)

    Result( int k, int s, String c, String t, String d, JSONObject j, int u )
    {
      kind   = k;
      status = s;
      code   = c;
      title  = t;
      detail = d;
      json   = j;
      url    = u;
    }

    /** @return true if the call succeeded
     */
    public boolean isOk() { return kind == OK; }

    /** @return a short text of the refusal, for the log or a toast: detail, else title, else code
     */
    public String message()
    {
      if ( detail != null && ! detail.isEmpty() ) return detail;
      if ( title  != null && ! title.isEmpty()  ) return title;
      if ( code   != null ) return code;
      return ( status > 0 )? ( "HTTP " + status ) : "";
    }

    /** @return a string member of the answer, or the default
     * @param name  member name
     * @param dflt  default value
     */
    public String getString( String name, String dflt ) { return ( json == null )? dflt : json.optString( name, dflt ); }
  }

  private final String mBaseUrl; // normalized base URL
  private final String mKey;     // access key
  private final int mUrlCheck;

  /** cstr
   * @param base_url  server base URL, e.g. "https://silexgis.example.org"
   * @param key       personal access key ("sgx_...")
   */
  public SilexGisClient( String base_url, String key )
  {
    mBaseUrl  = normalizeBaseUrl( base_url );
    mKey      = ( key == null )? "" : key.trim();
    mUrlCheck = checkBaseUrl( mBaseUrl );
  }

  /** @return the normalized base URL
   */
  public String getBaseUrl() { return mBaseUrl; }

  /** @return the base URL without surrounding blanks and without trailing slashes
   * @param url   base URL
   */
  public static String normalizeBaseUrl( String url )
  {
    if ( url == null ) return "";
    url = url.trim();
    while ( url.endsWith( "/" ) ) url = url.substring( 0, url.length() - 1 );
    return url;
  }

  /** @return the check of a base URL: URL_OK, URL_EMPTY, URL_INVALID, or URL_CLEARTEXT
   * @param url   base URL
   * @note http is accepted only towards the emulator host alias and the loopback, for development:
   *       the connection succeeds only in a build whose network security config allows cleartext
   *       to those hosts (the app's own config allows cleartext only to marcocorvi.altervista.org)
   */
  public static int checkBaseUrl( String url )
  {
    url = normalizeBaseUrl( url );
    if ( url.isEmpty() ) return URL_EMPTY;
    try {
      URL u = new URL( url );
      String protocol = u.getProtocol().toLowerCase( Locale.US );
      String host = u.getHost();
      if ( host == null || host.isEmpty() ) return URL_INVALID;
      if ( protocol.equals( "https" ) ) return URL_OK;
      if ( ! protocol.equals( "http" ) ) return URL_INVALID;
      host = host.toLowerCase( Locale.US );
      if ( host.equals( "10.0.2.2" ) || host.equals( "127.0.0.1" ) || host.equals( "localhost" ) ) return URL_OK;
      return URL_CLEARTEXT;
    } catch ( MalformedURLException e ) {
      return URL_INVALID;
    }
  }

  /** @return true if both the base URL and the access key are set
   */
  public boolean isConfigured() { return ! mBaseUrl.isEmpty() && ! mKey.isEmpty(); }

  /** @return true if the access key has the form of a SilexGIS key: "sgx_" followed by base64url characters
   * @param key   access key
   * @note a pasted key can carry a newline, a tab or an invisible character, which cannot go in a header
   */
  public static boolean checkKey( String key )
  {
    if ( key == null ) return false;
    key = key.trim();
    if ( key.length() <= 4 || ! key.startsWith( "sgx_" ) ) return false;
    for ( int i = 4; i < key.length(); ++i ) {
      char c = key.charAt( i );
      if ( ! ( ( c >= 'A' && c <= 'Z' ) || ( c >= 'a' && c <= 'z' ) || ( c >= '0' && c <= '9' ) || c == '_' || c == '-' ) ) return false;
    }
    return true;
  }

  /** @return the access key masked for display: its prefix (the first 12 characters) followed by an ellipsis
   * @param key   access key
   */
  public static String maskKey( String key )
  {
    if ( key == null ) return "";
    key = key.trim();
    return ( key.length() <= KEY_PREFIX_LENGTH )? key : key.substring( 0, KEY_PREFIX_LENGTH ) + "…";
  }

  // ---------------------------------------------------------------------
  // calls

  /** @return the account of the access key: GET /api/v1/me
   */
  public Result me() { return request( "GET", "/api/v1/me", null ); }

  /** @return the server name and version: GET /api/v1/about
   */
  public Result about() { return request( "GET", "/api/v1/about", null ); }

  /** @return what the server accepts and produces: GET /api/v1/survey-uploads/capabilities
   */
  public Result capabilities() { return request( "GET", "/api/v1/survey-uploads/capabilities", null ); }

  /** @return whether the server compiles the surveys, from the answer of capabilities()
   * @param caps   capabilities result
   */
  public static boolean compilerAvailable( Result caps )
  {
    if ( caps == null || caps.json == null ) return false;
    JSONObject compiler = caps.json.optJSONObject( "compiler" );
    return compiler != null && compiler.optBoolean( "available", false );
  }

  /** @return the account label from the answer of me(): display name (or user name) and e-mail
   * @param me   me result
   */
  public static String accountLabel( Result me )
  {
    if ( me == null || me.json == null ) return "";
    String name = me.json.optString( "displayName", "" );
    if ( name.isEmpty() || name.equals( "null" ) ) name = me.json.optString( "userName", "" );
    String email = me.json.optString( "email", "" );
    if ( email.isEmpty() || email.equals( "null" ) ) return name;
    return name.isEmpty()? email : ( name + " <" + email + ">" );
  }

  /** perform a call and read the JSON answer
   * @param method   HTTP method
   * @param path     route path, starting with "/"
   * @param body     JSON body (null for none)
   * @return the result of the call
   */
  public Result request( String method, String path, JSONObject body )
  {
    if ( ! isConfigured() ) return new Result( NOT_CONFIGURED, 0, null, null, null, null, mUrlCheck );
    if ( mUrlCheck != URL_OK ) return new Result( BAD_URL, 0, null, null, null, null, mUrlCheck );
    if ( ! checkKey( mKey ) ) return new Result( BAD_KEY, 0, null, null, null, null, URL_OK );
    HttpURLConnection http = null;
    try {
      URL url = new URL( mBaseUrl + path );
      http = (HttpURLConnection) url.openConnection();
      http.setConnectTimeout( CONNECT_TIMEOUT );
      http.setReadTimeout( READ_TIMEOUT );
      http.setInstanceFollowRedirects( false ); // a redirect could move the key to another host
      http.setUseCaches( false );
      http.setRequestMethod( method );
      http.setRequestProperty( "Authorization", "Bearer " + mKey );
      http.setRequestProperty( "Accept", "application/json, application/problem+json" );
      http.setRequestProperty( "X-SilexGis-Contract", Integer.toString( CONTRACT_VERSION ) );
      if ( body != null ) {
        byte[] bytes = body.toString().getBytes( "UTF-8" );
        http.setDoOutput( true );
        http.setRequestProperty( "Content-Type", "application/json; charset=utf-8" );
        http.setFixedLengthStreamingMode( bytes.length );
        OutputStream os = http.getOutputStream();
        os.write( bytes );
        os.close();
      }
      int status = http.getResponseCode();
      InputStream is = ( status >= 400 )? http.getErrorStream() : http.getInputStream();
      String text = readText( is );
      if ( status == HttpURLConnection.HTTP_NO_CONTENT ) { // e.g. an upload abandoned: there is no answer to read
        return new Result( OK, status, null, null, null, new JSONObject(), URL_OK );
      }
      if ( status >= 200 && status < 300 ) {
        JSONObject json = parseObject( text );
        if ( json == null ) return new Result( BAD_RESPONSE, status, null, null, null, null, URL_OK );
        return new Result( OK, status, null, null, null, json, URL_OK );
      }
      return problemResult( status, text );
    } catch ( IOException e ) {
      return new Result( NETWORK, 0, null, null, e.getMessage(), null, URL_OK );
    } catch ( RuntimeException e ) { // e.g. a header value or a host the connection refuses: never let it out of a background task
      return new Result( NETWORK, 0, null, null, e.getMessage(), null, URL_OK );
    } finally {
      if ( http != null ) http.disconnect();
    }
  }

  /** @return the result of a refusal (HTTP status of 300 or more)
   * @param status   HTTP status
   * @param text     body of the answer: the Problem Details of the server, if any
   */
  private static Result problemResult( int status, String text )
  {
    JSONObject problem = parseObject( text );
    String code   = optText( problem, "code" );
    String title  = optText( problem, "title" );
    String detail = optText( problem, "detail" );
    if ( status == HttpURLConnection.HTTP_UNAUTHORIZED ) {
      return new Result( KEY_REFUSED, status, code, title, detail, null, URL_OK );
    }
    if ( status == HttpURLConnection.HTTP_FORBIDDEN && CODE_KEY_NOT_ACCEPTED.equals( code ) ) {
      return new Result( KEY_NOT_ACCEPTED, status, code, title, detail, null, URL_OK );
    }
    return new Result( PROBLEM, status, code, title, detail, null, URL_OK );
  }

  // ---------------------------------------------------------------------
  // survey uploads, for the export

  /** @return the caves whose name matches a text: GET /api/v1/caves
   * @param text       text to look for
   * @param page_size  most caves in the answer
   */
  public Result searchCaves( String text, int page_size ) { return request( "GET", cavesSearchPath( text, page_size ), null ); }

  /** @return the route of the cave search
   * @param text       text to look for
   * @param page_size  most caves in the answer
   * @note the cave list matches its "search" text against the cave names
   */
  static String cavesSearchPath( String text, int page_size )
  {
    String q = encodeQuery( ( text == null )? "" : text.trim() );
    return "/api/v1/caves?search=" + q + "&pageSize=" + page_size;
  }

  /** @return the caves of an answer of searchCaves(): { id, name, list line } each; empty if the answer has no list
   * @param json   answer of searchCaves(): { items: [ { id, name, identificationCode, region, ... } ], page, pageSize, totalItems }
   * @note the list line is "name (code) - region", without the parts the cave does not have
   */
  public static ArrayList< String[] > parseCaves( JSONObject json )
  {
    ArrayList< String[] > ret = new ArrayList<>();
    JSONArray items = ( json == null )? null : json.optJSONArray( "items" );
    if ( items == null ) return ret;
    for ( int k = 0; k < items.length(); ++k ) {
      JSONObject cave = items.optJSONObject( k );
      if ( cave == null ) continue;
      String id   = optString( cave, "id" );
      String name = optString( cave, "name" );
      if ( id.isEmpty() || name.isEmpty() ) continue;
      StringBuilder sb = new StringBuilder( name );
      String code   = optString( cave, "identificationCode" );
      String region = optString( cave, "region" );
      if ( ! code.isEmpty() ) sb.append( " (" ).append( code ).append( ")" );
      if ( ! region.isEmpty() ) sb.append( " - " ).append( region );
      ret.add( new String[] { id, name, sb.toString() } );
    }
    return ret;
  }

  /** @return the answer of the request that opens an upload: POST /api/v1/survey-uploads
   * @param body   request (SilexGisUpload.openRequest)
   */
  public Result openUpload( JSONObject body ) { return request( "POST", "/api/v1/survey-uploads", body ); }

  /** @return the answer of the request that closes the sending of the files: POST /api/v1/survey-uploads/{id}/complete
   * @param id   upload id
   */
  public Result completeUpload( String id ) { return request( "POST", "/api/v1/survey-uploads/" + encodePath( id ) + "/complete", new JSONObject() ); }

  /** @return the status of an upload: GET /api/v1/survey-uploads/{id}
   * @param id   upload id
   */
  public Result uploadStatus( String id ) { return request( "GET", "/api/v1/survey-uploads/" + encodePath( id ), null ); }

  /** @return the answer of the request that abandons an upload still receiving its files: DELETE /api/v1/survey-uploads/{id}
   * @param id   upload id
   */
  public Result abandonUpload( String id ) { return request( "DELETE", "/api/v1/survey-uploads/" + encodePath( id ), null ); }

  /** @return the default route of a file of an upload: /api/v1/survey-uploads/{id}/files/{name}
   * @param id     upload id
   * @param name   file name
   */
  static String filePath( String id, String name ) { return "/api/v1/survey-uploads/" + encodePath( id ) + "/files/" + encodePath( name ); }

  /** send a file of an upload: PUT, streamed from the file
   * @param path       route of the file (filePath()), a path of the server or a full URL of it
   * @param file       file
   * @param progress   progress listener (can be null): it can stop the sending
   * @return the result of the call: OK with the answer of the server, CANCELLED if the listener stopped it, or a failure as in request()
   * @note the access key goes only to the server of the base URL: a route on another host is refused
   */
  public Result putFile( String path, File file, Progress progress )
  {
    if ( ! isConfigured() ) return new Result( NOT_CONFIGURED, 0, null, null, null, null, mUrlCheck );
    if ( mUrlCheck != URL_OK ) return new Result( BAD_URL, 0, null, null, null, null, mUrlCheck );
    if ( ! checkKey( mKey ) ) return new Result( BAD_KEY, 0, null, null, null, null, URL_OK );
    String full = resolveUrl( path );
    if ( full == null || ! sameOrigin( mBaseUrl, full ) ) return new Result( BAD_URL, 0, null, null, null, null, URL_INVALID );
    HttpURLConnection http = null;
    FileInputStream fis = null;
    try {
      long size = file.length();
      fis = new FileInputStream( file );
      http = (HttpURLConnection) (new URL( full )).openConnection();
      http.setConnectTimeout( CONNECT_TIMEOUT );
      http.setReadTimeout( PUT_READ_TIMEOUT );
      http.setInstanceFollowRedirects( false );
      http.setUseCaches( false );
      http.setRequestMethod( "PUT" );
      http.setRequestProperty( "Authorization", "Bearer " + mKey );
      http.setRequestProperty( "Accept", "application/json, application/problem+json" );
      http.setRequestProperty( "X-SilexGis-Contract", Integer.toString( CONTRACT_VERSION ) );
      http.setRequestProperty( "Content-Type", "application/octet-stream" );
      http.setDoOutput( true );
      http.setFixedLengthStreamingMode( size );
      OutputStream os = http.getOutputStream();
      byte[] buf = new byte[65536];
      long sent = 0L;
      int n;
      while ( ( n = fis.read( buf ) ) > 0 ) {
        os.write( buf, 0, n );
        sent += n;
        if ( progress != null && ! progress.onProgress( sent, size ) ) {
          return new Result( CANCELLED, 0, null, null, null, null, URL_OK );
        }
      }
      os.close();
      int status = http.getResponseCode();
      InputStream is = ( status >= 400 )? http.getErrorStream() : http.getInputStream();
      String text = readText( is );
      if ( status >= 200 && status < 300 ) {
        JSONObject json = parseObject( text );
        if ( json == null ) json = new JSONObject(); // the file is received: the answer is only informative
        return new Result( OK, status, null, null, null, json, URL_OK );
      }
      return problemResult( status, text );
    } catch ( IOException e ) {
      return new Result( NETWORK, 0, null, null, e.getMessage(), null, URL_OK );
    } catch ( RuntimeException e ) {
      return new Result( NETWORK, 0, null, null, e.getMessage(), null, URL_OK );
    } finally {
      try { if ( fis != null ) fis.close(); } catch ( IOException e ) { /* nothing to do */ }
      if ( http != null ) http.disconnect();
    }
  }

  /** @return a text encoded for a query parameter
   * @param text   text
   */
  static String encodeQuery( String text )
  {
    try {
      return URLEncoder.encode( text, "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
      return "";
    }
  }

  /** @return a text encoded as a segment of a path: every byte but letters, digits and - . _ ~ is percent-encoded
   * @param text   text
   */
  static String encodePath( String text )
  {
    if ( text == null ) return "";
    StringBuilder sb = new StringBuilder();
    byte[] bytes;
    try {
      bytes = text.getBytes( "UTF-8" );
    } catch ( UnsupportedEncodingException e ) {
      return "";
    }
    for ( byte b : bytes ) {
      int c = b & 0xff;
      if ( ( c >= 'A' && c <= 'Z' ) || ( c >= 'a' && c <= 'z' ) || ( c >= '0' && c <= '9' ) || c == '-' || c == '.' || c == '_' || c == '~' ) {
        sb.append( (char)c );
      } else {
        sb.append( String.format( Locale.US, "%%%02X", c ) );
      }
    }
    return sb.toString();
  }

  // ---------------------------------------------------------------------
  // survey uploads, for the import

  /** a survey upload, as listed by the server: the archive of a TopoDroid survey and its Therion files
   */
  public static class Upload
  {
    public String id         = "";
    public String caveId     = "";    // id of the cave (empty if the server did not send it): cave names are not unique
    public String caveName   = "";
    public String surveyId   = "";    // id of the survey of the cave that the upload is a version of (empty if not sent)
    public String title      = "";    // title of the survey of the cave, else the survey title, else the survey name
    public String surveyName = "";
    public String createdAt  = "";    // UTC time of the upload
    public String version    = "";    // UTC time that names the version of the survey (empty if not sent: the time of the upload)
    public String uploader   = "";
    public long   size       = -1L;   // size of the archive [bytes], -1 if not known
    public String archiveName = "";
    public String archiveUrl  = "";
    public final ArrayList< String[] > therion = new ArrayList<>(); // Therion files: { name, content URL }

    /** @return the key of the cave of the upload: its id, else its name
     */
    public String caveKey() { return caveId.isEmpty()? caveName : caveId; }

    /** @return the key of the survey of the cave that the upload is a version of: its id, else its title
     */
    public String surveyKey() { return surveyId.isEmpty()? title : surveyId; }

    /** @return the time that orders the versions of a survey: the version, else the time of the upload
     */
    public String stamp() { return version.isEmpty()? createdAt : version; }
  }

  /** @return the answer of the list of the uploads of a source: GET /api/v1/survey-uploads
   * @param source     source of the uploads, eg, "topodroid"
   * @param page_size  maximum number of uploads in the answer
   */
  public Result listUploads( String source, int page_size )
  {
    return request( "GET", "/api/v1/survey-uploads?source=" + source + "&pageSize=" + page_size, null );
  }

  /** @return the uploads of an answer of listUploads(), or null if the answer has no list of items
   * @param json   answer of listUploads()
   * @note an item without the content URL of the archive is skipped: there is nothing to import from it
   */
  public static ArrayList< Upload > parseUploads( JSONObject json )
  {
    if ( json == null ) return null;
    JSONArray items = json.optJSONArray( "items" );
    if ( items == null ) return null;
    ArrayList< Upload > ret = new ArrayList<>();
    for ( int k = 0; k < items.length(); ++k ) {
      JSONObject item = items.optJSONObject( k );
      if ( item == null ) continue;
      JSONObject archive = item.optJSONObject( "archive" );
      if ( archive == null ) continue;
      Upload up = new Upload();
      up.archiveUrl = optString( archive, "contentUrl" );
      if ( up.archiveUrl.isEmpty() ) continue;
      up.archiveName = optString( archive, "name" );
      up.size        = archive.optLong( "sizeBytes", -1L );
      up.id          = optString( item, "id" );
      up.caveId      = optString( item, "caveId" );
      up.caveName    = optString( item, "caveName" );
      up.surveyName  = optString( item, "surveyName" );
      JSONObject cs  = item.optJSONObject( "caveSurvey" );
      if ( cs != null ) {
        up.surveyId  = optString( cs, "id" );
        up.title     = optString( cs, "title" );
      }
      if ( up.title.isEmpty() ) up.title = optString( item, "surveyTitle" );
      if ( up.title.isEmpty() ) up.title = up.surveyName;
      up.createdAt   = optString( item, "createdAt" );
      up.version     = optString( item, "version" );
      JSONObject by  = item.optJSONObject( "uploadedBy" );
      up.uploader    = ( by != null )? optString( by, "displayName" ) : "";
      JSONArray th = item.optJSONArray( "therionSources" );
      if ( th != null ) {
        for ( int j = 0; j < th.length(); ++j ) {
          JSONObject t = th.optJSONObject( j );
          if ( t == null ) continue;
          String url = optString( t, "contentUrl" );
          if ( ! url.isEmpty() ) up.therion.add( new String[] { optString( t, "name" ), url } );
        }
      }
      ret.add( up );
    }
    return ret;
  }

  /** @return the number of uploads the server holds for the list, as it says, or -1 if it does not say
   * @param json   answer of listUploads()
   */
  public static int totalUploads( JSONObject json )
  {
    return ( json == null || ! json.has( "total" ) )? -1 : json.optInt( "total", -1 );
  }

  /** @return the number of items of the answer of listUploads() (-1 if it has no list)
   * @param json   answer of listUploads()
   */
  public static int countItems( JSONObject json )
  {
    JSONArray items = ( json == null )? null : json.optJSONArray( "items" );
    return ( items == null )? -1 : items.length();
  }

  /** sort the uploads by cave (name, then id: two caves can have the same name), then by survey of the cave
   * (title, then id), then newest version first. The uploads of one cave, and of one survey, are next to each other
   * @param uploads  list of uploads
   */
  public static void sortUploads( List< Upload > uploads )
  {
    Collections.sort( uploads, new Comparator< Upload >() {
      @Override
      public int compare( Upload a, Upload b ) {
        int c = a.caveName.compareToIgnoreCase( b.caveName );
        if ( c == 0 ) c = a.caveName.compareTo( b.caveName ); // names that differ only in case do not alternate
        if ( c == 0 ) c = a.caveKey().compareTo( b.caveKey() );
        if ( c == 0 ) c = a.title.compareToIgnoreCase( b.title );
        if ( c == 0 ) c = a.surveyKey().compareTo( b.surveyKey() );
        if ( c == 0 ) c = b.stamp().compareTo( a.stamp() ); // ISO times: the later is greater
        return c;
      }
    } );
  }

  /** @return the uploads with the text in the cave name, the survey title or name, or the uploader
   * @param uploads  list of uploads
   * @param text     filter text (blank for all)
   */
  public static ArrayList< Upload > filterUploads( List< Upload > uploads, String text )
  {
    ArrayList< Upload > ret = new ArrayList<>();
    String t = ( text == null )? "" : text.trim().toLowerCase( Locale.getDefault() );
    for ( Upload up : uploads ) {
      if ( t.isEmpty()
        || up.caveName.toLowerCase( Locale.getDefault() ).contains( t )
        || up.title.toLowerCase( Locale.getDefault() ).contains( t )
        || up.surveyName.toLowerCase( Locale.getDefault() ).contains( t )
        || up.uploader.toLowerCase( Locale.getDefault() ).contains( t ) ) ret.add( up );
    }
    return ret;
  }

  /** @return the file name from a name given by the server: no folder, only letters, digits, dot, dash and underscore, with the extension
   * @param name   name given by the server
   * @param ext    extension, with the dot, eg, ".zip"
   * @note the name comes from a server: it must not move the file out of its folder
   */
  public static String safeFileName( String name, String ext )
  {
    if ( name == null ) name = "";
    int pos = Math.max( name.lastIndexOf( '/' ), name.lastIndexOf( '\\' ) );
    name = name.substring( pos + 1 ).replaceAll( "[^A-Za-z0-9._-]", "_" );
    while ( name.startsWith( "." ) ) name = name.substring( 1 );
    if ( name.isEmpty() ) name = "survey";
    if ( ! name.toLowerCase( Locale.US ).endsWith( ext ) ) name = name + ext;
    return name;
  }

  /** @return a file in the folder that does not exist yet: the name, or the name with -1, -2, ... before the extension
   * @param dir    folder
   * @param name   file name
   */
  public static File newFile( File dir, String name )
  {
    File file = new File( dir, name );
    if ( ! file.exists() ) return file;
    int pos = name.lastIndexOf( '.' );
    String base = ( pos > 0 )? name.substring( 0, pos ) : name;
    String ext  = ( pos > 0 )? name.substring( pos ) : "";
    for ( int k = 1; ; ++k ) {
      file = new File( dir, base + "-" + k + ext );
      if ( ! file.exists() ) return file;
    }
  }

  /** @return the time of an ISO UTC time, to the minute: "2026-10-25 01:32"
   * @param iso   ISO time
   */
  public static String shortTime( String iso )
  {
    String ret = iso.replace( 'T', ' ' );
    return ( ret.length() > 16 )? ret.substring( 0, 16 ) : ret;
  }

  /** @return the size in megabytes ("5.2 MB") or, under one megabyte, in kilobytes ("5.2 kB"); "5.2 / 12.0 MB" if the total is known
   * @param done   size [bytes]
   * @param total  total size [bytes] (-1 if not known)
   */
  public static String sizeText( long done, long total )
  {
    if ( done < 0 ) return "";
    boolean kb = Math.max( done, total ) < 1048576L;
    double unit = kb? 1024.0 : 1048576.0;
    String name = kb? "kB" : "MB";
    if ( total > 0 ) return String.format( Locale.US, "%.1f / %.1f %s", done / unit, total / unit, name );
    return String.format( Locale.US, "%.1f %s", done / unit, name );
  }

  // ---------------------------------------------------------------------
  // downloads

  /** listener of the progress of a download
   */
  public interface Progress
  {
    /** @return true to go on, false to stop the download
     * @param done    bytes received
     * @param total   bytes expected (-1 if not known)
     */
    boolean onProgress( long done, long total );
  }

  /** @return the full URL of a content URL of the server: a path is relative to the base URL, null if the URL is neither
   * @param content_url   content URL
   */
  String resolveUrl( String content_url )
  {
    if ( content_url == null ) return null;
    if ( content_url.startsWith( "/" ) ) return mBaseUrl + content_url;
    String lower = content_url.toLowerCase( Locale.US );
    if ( lower.startsWith( "https://" ) || lower.startsWith( "http://" ) ) return content_url;
    return null;
  }

  /** @return true if the two URLs have the same scheme, host and port
   */
  private static boolean sameOrigin( String url1, String url2 )
  {
    try {
      URL u1 = new URL( url1 );
      URL u2 = new URL( url2 );
      return u1.getProtocol().equalsIgnoreCase( u2.getProtocol() )
          && u1.getHost().equalsIgnoreCase( u2.getHost() )
          && u1.getPort() == u2.getPort()
          && u1.getDefaultPort() == u2.getDefaultPort();
    } catch ( MalformedURLException e ) {
      return false;
    }
  }

  /** download the archive or a Therion file of an upload. The content URLs are signed and expire a few minutes after the
   * server has listed them: if the download is refused with a 4xx other than 401 and 403, the list is read again, the URLs of
   * the upload are replaced with the fresh ones, and the download is tried once more
   * @param up        upload (its URLs are updated if they are refreshed)
   * @param therion   index of the Therion file, -1 for the archive
   * @param dest      destination file
   * @param progress  progress listener (can be null)
   * @return the result of the download, as in download()
   */
  public Result downloadUpload( Upload up, int therion, File dest, Progress progress )
  {
    Result res = download( contentUrlOf( up, therion ), dest, progress );
    if ( res.kind != PROBLEM || res.status < 400 || res.status >= 500 || res.status == 403 ) return res;
    Result list = listUploads( "topodroid", 100 );
    ArrayList< Upload > ups = list.isOk()? parseUploads( list.json ) : null;
    if ( ups == null ) return res;
    for ( Upload fresh : ups ) {
      if ( fresh.id.equals( up.id ) && ! up.id.isEmpty() ) {
        up.archiveUrl = fresh.archiveUrl;
        if ( therion >= 0 && therion < up.therion.size() ) {
          String name = up.therion.get( therion )[0];
          for ( String[] t : fresh.therion ) {
            if ( t[0].equals( name ) ) {
              up.therion.get( therion )[1] = t[1];
              break;
            }
          }
        }
        return download( contentUrlOf( up, therion ), dest, progress );
      }
    }
    return res;
  }

  /** @return the content URL of the archive (therion < 0) or of a Therion file of an upload
   */
  private static String contentUrlOf( Upload up, int therion )
  {
    return ( therion < 0 )? up.archiveUrl : up.therion.get( therion )[1];
  }

  /** download a file of the server
   * @param content_url   content URL of the file, a path of the server or a full URL
   * @param dest          destination file
   * @param progress      progress listener (can be null)
   * @return the result of the download: OK (no JSON), CANCELLED if the listener stopped it, FILE_ERROR if the file cannot be written,
   *         or a failure as in request()
   * @note the file is written as "dest.part" and renamed when it is complete. The access key goes only to the server
   *       of the base URL: a content URL on another host is signed and needs no key
   */
  public Result download( String content_url, File dest, Progress progress )
  {
    if ( ! isConfigured() ) return new Result( NOT_CONFIGURED, 0, null, null, null, null, mUrlCheck );
    if ( mUrlCheck != URL_OK ) return new Result( BAD_URL, 0, null, null, null, null, mUrlCheck );
    if ( ! checkKey( mKey ) ) return new Result( BAD_KEY, 0, null, null, null, null, URL_OK );
    String full = resolveUrl( content_url );
    if ( full == null ) return new Result( BAD_URL, 0, null, null, null, null, URL_INVALID );
    int check = checkBaseUrl( full );
    if ( check != URL_OK ) return new Result( BAD_URL, 0, null, null, null, null, check );
    File part = new File( dest.getPath() + ".part" );
    HttpURLConnection http = null;
    InputStream is = null;
    FileOutputStream fos = null;
    boolean done = false;
    try {
      http = (HttpURLConnection) (new URL( full )).openConnection();
      http.setConnectTimeout( CONNECT_TIMEOUT );
      http.setReadTimeout( READ_TIMEOUT );
      http.setInstanceFollowRedirects( false );
      http.setUseCaches( false );
      http.setRequestMethod( "GET" );
      if ( sameOrigin( mBaseUrl, full ) ) {
        http.setRequestProperty( "Authorization", "Bearer " + mKey );
        http.setRequestProperty( "X-SilexGis-Contract", Integer.toString( CONTRACT_VERSION ) );
      }
      http.setRequestProperty( "Accept", "application/octet-stream, */*" );
      int status = http.getResponseCode();
      if ( status < 200 || status >= 300 ) {
        return problemResult( status, readText( http.getErrorStream() ) );
      }
      long total = -1L;
      String length = http.getHeaderField( "Content-Length" );
      if ( length != null ) {
        try { total = Long.parseLong( length.trim() ); } catch ( NumberFormatException e ) { total = -1L; }
      }
      is = http.getInputStream();
      try {
        fos = new FileOutputStream( part );
      } catch ( IOException e ) {
        return new Result( FILE_ERROR, status, null, null, e.getMessage(), null, URL_OK );
      }
      byte[] buf = new byte[65536];
      long count = 0L;
      int n;
      while ( ( n = is.read( buf ) ) > 0 ) {
        try {
          fos.write( buf, 0, n );
        } catch ( IOException e ) {
          return new Result( FILE_ERROR, status, null, null, e.getMessage(), null, URL_OK );
        }
        count += n;
        if ( progress != null && ! progress.onProgress( count, total ) ) {
          return new Result( CANCELLED, status, null, null, null, null, URL_OK );
        }
      }
      if ( total >= 0 && count != total ) { // the connection dropped before the end
        return new Result( NETWORK, status, null, null, "received " + count + " of " + total + " bytes", null, URL_OK );
      }
      try {
        fos.close();
        fos = null;
        if ( dest.exists() && ! dest.delete() ) {
          return new Result( FILE_ERROR, status, null, null, dest.getPath(), null, URL_OK );
        }
        if ( ! part.renameTo( dest ) ) {
          return new Result( FILE_ERROR, status, null, null, dest.getPath(), null, URL_OK );
        }
      } catch ( IOException e ) {
        return new Result( FILE_ERROR, status, null, null, e.getMessage(), null, URL_OK );
      }
      done = true;
      return new Result( OK, status, null, null, null, null, URL_OK );
    } catch ( IOException e ) {
      return new Result( NETWORK, 0, null, null, e.getMessage(), null, URL_OK );
    } catch ( RuntimeException e ) {
      return new Result( NETWORK, 0, null, null, e.getMessage(), null, URL_OK );
    } finally {
      try { if ( is  != null ) is.close();  } catch ( IOException e ) { /* nothing to do */ }
      try { if ( fos != null ) fos.close(); } catch ( IOException e ) { /* nothing to do */ }
      if ( ! done ) part.delete();
      if ( http != null ) http.disconnect();
    }
  }

  // ---------------------------------------------------------------------
  // utilities

  /** @return a string member of a JSON object, empty if missing or null
   * @param json   JSON object
   * @param name   member name
   */
  private static String optString( JSONObject json, String name )
  {
    String ret = optText( json, name );
    return ( ret == null )? "" : ret;
  }

  /** @return the text of a stream (UTF-8), empty if the stream is null
   * @param is   input stream
   */
  private static String readText( InputStream is ) throws IOException
  {
    if ( is == null ) return "";
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    int n;
    try {
      while ( ( n = is.read( buf ) ) > 0 ) bos.write( buf, 0, n );
    } finally {
      is.close();
    }
    return bos.toString( "UTF-8" );
  }

  /** @return the JSON object of a text, or null if the text is not a JSON object
   * @param text   input text
   */
  private static JSONObject parseObject( String text )
  {
    if ( text == null ) return null;
    text = text.trim();
    if ( ! text.startsWith( "{" ) ) return null;
    try {
      return new JSONObject( text );
    } catch ( JSONException e ) {
      return null;
    }
  }

  /** @return a string member of a JSON object, or null if missing
   * @param json   JSON object (can be null)
   * @param name   member name
   */
  private static String optText( JSONObject json, String name )
  {
    if ( json == null || ! json.has( name ) || json.isNull( name ) ) return null;
    return json.optString( name, null );
  }

}
