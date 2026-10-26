/* @file SilexGisLink.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload to a SilexGIS server: the link to the results of an upload
 *
 * A link is opened by anyone who has its address (audience "public"), or only by the
 * accounts signed in to the server (audience "organization"). It can be withdrawn, and a
 * new one made in its place. The server makes no link for a cave whose position is
 * protected unless it is asked for on purpose (allowProtected), because the results show
 * where the passages of the cave are.
 * The class uses only java and org.json, so that it can be tested on the JVM.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import org.json.JSONObject;
import org.json.JSONException;

class SilexGisLink
{
  // outcomes of a request
  static final int MADE        = 0; // a new link was made
  static final int WITHDRAWN   = 1; // the link was withdrawn
  static final int PROTECTED   = 2; // no link: the position of the cave is protected, and the link was not asked for on purpose
  static final int REFUSED     = 3; // the server refused: the result says why
  static final int KEY_REFUSED = 4; // the access key was refused

  static final String CODE_PROTECTED = "survey_upload.public_link_refused_protected";
  static final String CODE_NOT_FOUND = "survey_upload.public_link_not_found";
  static final String REVOKED        = "revoked"; // refusal code of a withdrawn link

  String url      = null; // web path (or URL) of the link, null if none
  String audience = null; // who may open it
  String refusal  = null; // why there is no link (code), null if not known
  boolean protectedRefused = false; // the last new link was refused because the position of the cave is protected
  SilexGisClient.Result result = null; // the last call

  /** cstr: no link */
  SilexGisLink() { }

  /** @return the link of an upload, as its status says it
   * @param st   status (can be null)
   */
  static SilexGisLink of( SilexGisStatus st )
  {
    SilexGisLink ret = new SilexGisLink();
    if ( st != null ) {
      ret.url      = st.publicLinkUrl;
      ret.audience = st.publicLinkAudience;
      ret.refusal  = ( st.publicLinkUrl == null )? st.publicLinkRefusal : null;
    }
    return ret;
  }

  /** @return true if there is a link */
  boolean hasLink() { return url != null; }

  /** @return true if the link opens only for the signed-in accounts of the server */
  boolean isOrganization() { return hasLink() && SilexGisUpload.AUDIENCE_ORGANIZATION.equals( audience ); }

  /** @return true if there is no link because the upload asked for none */
  boolean isNotWanted() { return ! hasLink() && SilexGisStatus.REFUSAL_NOT_WANTED.equals( refusal ); }

  /** @return true if a link is made only on purpose because the position of the cave is protected:
   *          the upload got no link for that, or the last new link was refused for that (the link it had is kept)
   */
  boolean isProtected() { return protectedRefused || ( ! hasLink() && SilexGisStatus.REFUSAL_PROTECTED.equals( refusal ) ); }

  /** @return the body of the request that makes a link
   * @param audience          who may open it: public, organization
   * @param allow_protected   whether the link is asked for although the position of the cave is protected
   */
  static JSONObject makeRequest( String audience, boolean allow_protected )
  {
    JSONObject ret = new JSONObject();
    try {
      ret.put( "audience", SilexGisUpload.isAudience( audience )? audience : SilexGisUpload.AUDIENCE_PUBLIC );
      ret.put( "allowProtected", allow_protected );
    } catch ( JSONException e ) { /* cannot happen with these values */ }
    return ret;
  }

  /** make a new link in place of the one the upload has - not on the UI thread
   * @param client           client of the server
   * @param upload_id        upload id
   * @param audience         who may open the link
   * @param allow_protected  whether the link is asked for although the position of the cave is protected
   * @return the outcome: MADE, PROTECTED, REFUSED, KEY_REFUSED
   */
  int make( SilexGisClient client, String upload_id, String audience, boolean allow_protected )
  {
    result = client.makePublicLink( upload_id, makeRequest( audience, allow_protected ) );
    protectedRefused = false;
    if ( ! result.isOk() ) return refused( result );
    String made = optText( result.json, "url" );
    if ( made == null ) { // the answer does not carry the link: the status of the upload does
      SilexGisClient.Result st = client.uploadStatus( upload_id );
      if ( st.isOk() ) {
        SilexGisLink link = of( SilexGisStatus.parse( st.json ) );
        made = link.url;
        if ( link.audience != null ) audience = link.audience;
      }
      if ( made == null ) {
        result = st.isOk()? result : st;
        return st.isOk()? REFUSED : refused( st );
      }
    } else {
      String aud = optText( result.json, "audience" );
      if ( SilexGisUpload.isAudience( aud ) ) audience = aud;
    }
    url      = made;
    this.audience = audience;
    refusal  = null;
    return MADE;
  }

  /** withdraw the link of the upload - not on the UI thread
   * @param client      client of the server
   * @param upload_id   upload id
   * @return the outcome: WITHDRAWN, REFUSED, KEY_REFUSED
   */
  int withdraw( SilexGisClient client, String upload_id )
  {
    result = client.withdrawPublicLink( upload_id );
    if ( ! result.isOk() && ! CODE_NOT_FOUND.equals( result.code ) ) return refused( result ); // a link that is not there is withdrawn
    url      = null;
    refusal  = REVOKED;
    protectedRefused = false;
    return WITHDRAWN;
  }

  /** @return the outcome of a refusal
   * @param r   result of the call
   */
  private int refused( SilexGisClient.Result r )
  {
    if ( r.kind == SilexGisClient.KEY_REFUSED ) return KEY_REFUSED;
    if ( isProtectedCode( r.code ) ) { // the server refuses before it replaces: a link the upload had is still there
      protectedRefused = true;
      if ( url == null ) refusal = SilexGisStatus.REFUSAL_PROTECTED;
      return PROTECTED;
    }
    return REFUSED;
  }

  /** @return true if a refusal code says that the position of the cave is protected
   * @param code   refusal code (can be null)
   */
  static boolean isProtectedCode( String code )
  {
    return CODE_PROTECTED.equals( code ) || SilexGisStatus.REFUSAL_PROTECTED.equals( code );
  }

  /** @return a non-empty string member, or null
   */
  private static String optText( JSONObject json, String name )
  {
    if ( json == null || ! json.has( name ) || json.isNull( name ) ) return null;
    Object v = json.opt( name );
    if ( ! ( v instanceof String ) ) return null;
    String s = ( (String)v ).trim();
    return s.isEmpty()? null : s;
  }

}
