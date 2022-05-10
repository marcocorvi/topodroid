/** @file ExportGltf.java
 *
 * @author marco corvi
 * @date feb 2021
 *
 * @brief Model glTF exporter
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * The glTF exporter writes five files
 *   filename.gltf
 *   filename_stations.bin
 *   filename_legs.bin
 *   filename_splays.bin
 *   filename_surface.bin
 */
package com.topodroid.c3out;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.TDX.GlModel;
import com.topodroid.TDX.GlNames;
import com.topodroid.TDX.GlLines;
import com.topodroid.TDX.GlSurface;
import com.topodroid.TDX.Archiver;
import com.topodroid.TDX.TDPath;

// import android.util.Base64;
// import android.util.Base64OutputStream;

import java.util.Locale;
// import java.util.List;
import java.util.ArrayList;

// import java.nio.ByteBuffer;

// import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
// import java.io.PrintStream;
import java.io.OutputStream;
// import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
// import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



public class ExportGltf
{
  private final static String ARRAY_COORDS = "34962";
  private final static String ARRAY_ELEMS  = "34963";

  private final static String TYPE_SCALAR  = "SCALAR";
  private final static String TYPE_VEC3    = "VEC3";
  private final static String TYPE_VEC4    = "VEC4";
  private final static String TYPE_MAT4    = "MAT4";

  private final static String TYPE_USHORT  = "5123";
  private final static String TYPE_FLOAT   = "5126";

  private final static String MODE_POINT     = "0";
  private final static String MODE_LINES     = "1";
  private final static String MODE_TRIANGLES = "4";
  private final static String MODE_TRI_STRIP = "5";
  private final static String MODE_TRI_FAN   = "6";

  private final static String ALPHA_OPAQUE = "OPAQUE";
  private final static String ALPHA_MASK   = "MASK";
  private final static String ALPHA_BLEND  = "BLEND";

  private final static String LIGHT_DIRECTIONAL  = "directional";
  private final static String LIGHT_POINT        = "point";
  private final static String LIGHT_SPOT         = "spot";

  GlModel mModel;

  public ExportGltf( GlModel model )
  {
    mModel = model;
  }

  /** export model to a file
   * @param zos      zip output stream
   * @param dirname  dir pathname
   * @param name     survey name (glTF filename = name.gltf)
   * @return true if success
   */
  public boolean write( OutputStream zos, String dirname, String name )
  {
    if ( mModel == null ) return false;

    if ( ! TDFile.makeMSdir( dirname ) ) {
      TDLog.Error("mkdir " + dirname + " error");
      return false;
    }
    // TDLog.v( "mkdir created MSdir " + dirname );

    ArrayList< String > files = new ArrayList<>();
    String pathname = dirname + "/" + name + ".gltf"; // full pathname
    String subdir   = "c3export/" + name;
    // TDLog.v( "filepath " + pathname + " subdir " + subdir );
    try {
      // FileOutputStream dos = new FileOutputStream( filepath );
      files.add( name + ".gltf" ); 
      TDPath.checkPath( pathname );
      FileWriter fw = TDFile.getFileWriter( pathname ); // DistoX-SAF
      PrintWriter pw = new PrintWriter( fw );
      doExport( pw, dirname, subdir, files );
      pw.flush();
      fw.close();
      // compress files in the data output stream
      (new Archiver()).compressFiles( zos, subdir, files );

    } catch ( IOException e ) {
      TDLog.Error("TopoGL glTF export error " + e );
      for ( StackTraceElement ste : e.getStackTrace() ) TDLog.Error(ste.toString() );
      return false;
    } finally {
      TDFile.deleteMSdir( dirname ); // delete temporary dir
    }
    return true;
  }

  private class MinMax
  {
    float xmin, xmax;
    float ymin, ymax;
    float zmin, zmax;

    MinMax() { xmin = xmax = ymin = ymax = zmin = zmax = 0; }

    void reset() 
    { 
      xmin = ymin = zmin =  100000;
      xmax = ymax = zmax = -100000;
    }

    String formatMax() { return String.format(Locale.US, "[ %f, %f, %f ]", xmax, ymax, zmax ); }
    String formatMin() { return String.format(Locale.US, "[ %f, %f, %f ]", xmin, ymin, zmin ); }
  }

  private void doExport( PrintWriter pw, String rootpath, String subdir, ArrayList<String> files ) // throws IOException
  {
    int count_stations = (mModel.glNames  == null)? 0 : mModel.glNames.size();
    int count_legs     = (mModel.glLegs   == null)? 0 : mModel.glLegs.size()   * 2;
    int count_splays   = (mModel.glSplays == null)? 0 : mModel.glSplays.size() * 2;
    int count_surface  = 0;
    float[] surface = null;
    if ( mModel.glSurface != null ) {
      surface = mModel.glSurface.getSurfaceData();
      if ( surface != null ) count_surface = mModel.glSurface.getVertexCount();
    }
    boolean has_stations = count_stations > 0;
    boolean has_legs     = count_legs > 0;
    boolean has_splays   = count_splays > 0;
    boolean has_surface  = count_surface > 0;
    boolean has_splays_or_surface = has_splays || has_surface;
      
 
    // mModel.glNames.computeBBox();
    // float xmax = (float)mModel.glLegs.getXmax();
    // float ymax = (float)mModel.glLegs.getYmax();
    // float zmax = (float)mModel.glLegs.getZmax();
    // float xmin = (float)mModel.glLegs.getXmin();
    // float ymin = (float)mModel.glLegs.getYmin();
    // float zmin = (float)mModel.glLegs.getZmin();
    // float max = xmax; if ( ymax > max ) max = ymax; if (zmax > max) max = zmax;
    // float min = xmin; if ( ymin < min ) min = ymin; if (zmin < min) min = zmin;

    // String max_stations = String.format(Locale.US, "[  1.0,  1.0,  1.0 ]", mModel.glNames.getXmax(), mModel.glNames.getYmax(), mModel.glNames.getZmax() );
    // String min_stations = String.format(Locale.US, "[ -1.0, -1.0, -1.0 ]", mModel.glNames.getXmin(), mModel.glNames.getYmin(), mModel.glNames.getZmin() );
    // String max_legs     = String.format(Locale.US, "[  1.0,  1.0,  1.0 ]", mModel.glLegs.getXmax(), mModel.glLegs.getYmax(), mModel.glLegs.getZmax() );
    // String min_legs     = String.format(Locale.US, "[ -1.0, -1.0, -1.0 ]", mModel.glLegs.getXmin(), mModel.glLegs.getYmin(), mModel.glLegs.getZmin() );
    // String max_splays   = String.format(Locale.US, "[  1.0,  1.0,  1.0 ]", mModel.glSplays.getXmax(), mModel.glSplays.getYmax(), mModel.glSplays.getZmax() );
    // String min_splays   = String.format(Locale.US, "[ -1.0, -1.0, -1.0 ]", mModel.glSplays.getXmin(), mModel.glSplays.getYmin(), mModel.glSplays.getZmin() );

    String buffer_stations = rootpath + "/stations.bin"; // buffer pathnames are always prepared
    String buffer_legs     = rootpath + "/legs.bin";
    String buffer_splays   = rootpath + "/splays.bin";
    String buffer_surface  = rootpath + "/surface.bin";

    int bytelen_stations = 0;
    String max_stations  = null;
    String min_stations  = null;
    if ( has_stations ) {
      MinMax minMax1 = new MinMax();
      bytelen_stations = doExportNames( buffer_stations, mModel.glNames,  minMax1 );
      max_stations = minMax1.formatMax();
      min_stations = minMax1.formatMin();
      files.add( "stations.bin" );
    }

    int bytelen_legs = 0;
    String max_legs  = null;
    String min_legs  = null;
    if ( has_legs ) {
      MinMax minMax2 = new MinMax();
      bytelen_legs     = doExportLines( buffer_legs, mModel.glLegs, minMax2 );
      max_legs = minMax2.formatMax();
      min_legs = minMax2.formatMin();
      files.add( "legs.bin" );
    }

    int bytelen_splays = 0;
    String max_splays  = null;
    String min_splays  = null;
    if ( has_splays ) {
      MinMax minMax3 = new MinMax();
      bytelen_splays   = ( count_splays > 0 )? doExportLines( buffer_splays,   mModel.glSplays, minMax3 ) : 0;
      if ( bytelen_splays == 0 ) has_splays = false;
      max_splays = minMax3.formatMax();
      min_splays = minMax3.formatMin();
      files.add( "splays.bin" );
    }

    int bytelen_surface = 0;
    String max_surface  = null;
    String min_surface  = null;
    String max_surface_normals  = null;
    String min_surface_normals  = null;
    if ( has_surface ) {
      // TDLog.v("Gltf export surface" );
      MinMax minMax4 = new MinMax(); // positions
      MinMax minMax5 = new MinMax(); // normals
      bytelen_surface = doExportTriangles( buffer_surface, mModel.glSurface, minMax4, minMax5 );
      max_surface = minMax4.formatMax();
      min_surface = minMax4.formatMin();
      max_surface_normals = minMax5.formatMax();
      min_surface_normals = minMax5.formatMin();
      // TDLog.v("Gltf minmax_P " + min_surface + " " + max_surface );
      // TDLog.v("Gltf minmax_N " + min_surface_normals + " " + max_surface_normals );
      if ( bytelen_surface == 0 ) {
        has_surface = false;
      } else {
        files.add( "surface.bin" );
      }
    }

    // ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
    // ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
    // ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
    // int bytelen_stations = printBufferData( baos1, mModel.glNames,  min, max );
    // int bytelen_legs     = printBufferData( baos2, mModel.glLegs,   min, max );
    // int bytelen_splays   = printBufferData( baos3, mModel.glSplays, min, max );

    int sep = rootpath.lastIndexOf('/');
    if ( sep >= 0 ) {
      buffer_stations = buffer_stations.substring( sep+1 );
      buffer_legs     = buffer_legs.substring( sep+1 );
      buffer_splays   = buffer_splays.substring( sep+1 );
      buffer_surface  = buffer_surface.substring( sep+1 );
    }

    openElement( pw, 0 );

    // openArrayTag(  pw, 1, "extensionsUsed" );
    // printElement(  pw, 2, "KHR_lights_punctual", false );
    // closeArrayTag( pw, 1, true );

    openArrayTag(  pw, 1, "scenes" );
    openElement(   pw, 2 );
    printArrayTag( pw, 3, "nodes", "0" );
    closeElement(  pw, 2, false );
    closeArrayTag( pw, 1, true );

    // TDLog.v("Gltf buffers Station " + has_stations + " Leg " + has_legs + " Splay " + has_splays + " Surface " + has_surface );

    int mesh_stations = 0;
    int mesh_legs     = 0;
    int mesh_splays   = 0;
    int mesh_surface  = 0;
    openArrayTag( pw, 1, "nodes" );
    {
      StringBuilder children = new StringBuilder();
      children.append("1, 2");
      int child = 2;
      if ( has_splays ) { ++child; children.append(", ").append(child); }
      if ( has_surface ) {
        ++child; children.append(", ").append(child);
        // for ( int k=0; k<3; ++k ) { ++child; children.append(", ").append(child); }
      }
      openElement(  pw, 2 ); // root node "0"
      printArrayTag( pw, 3, "children", children.toString() );
      closeElement(  pw, 2, true );
      int mesh = 0;
      if ( has_stations ) { mesh_stations = mesh; printNode( pw, 2, mesh++, true ); } // stations
      if ( has_legs     ) { mesh_legs = mesh;     printNode( pw, 2, mesh++, has_splays_or_surface ); } // legs
      if ( has_splays   ) { mesh_splays = mesh;   printNode( pw, 2, mesh++, has_surface ); } // splays
      if ( has_surface  ) { mesh_surface = mesh;  printNode( pw, 2, mesh++, false ); // surface
        // printNodeLight( pw, 2, 0, "0.259, 0.0, 0.0, 0.966", true );
        // printNodeLight( pw, 2, 1, "0.259, 0.0, 0.0, 0.966", true );
        // printNodeLight( pw, 2, 2, "0.259, 0.0, 0.0, 0.966", false );
      }
    }
    closeArrayTag( pw, 1, true );

    openArrayTag( pw, 1, "meshes" ); // ------------- MESHES
    {
      int buffer = 0;
      if ( has_stations ) printMesh( pw, 2, buffer++, MODE_POINT, -1, true );
      if ( has_legs     ) printMesh( pw, 2, buffer++, MODE_LINES,  0, has_splays_or_surface );
      if ( has_splays   ) printMesh( pw, 2, buffer++, MODE_LINES,  1, has_surface );
      if ( has_surface  ) printMeshPN( pw, 2, buffer, buffer+1, MODE_TRIANGLES, 2, false );
    }
    closeArrayTag( pw, 1, true );

    openArrayTag( pw, 1, "materials" ); // --------- MATERIALS
    {
      printPbrMaterial( pw, 2, "1.000, 0.766, 0.366, 1.0", 0.5f, 0.2f, ALPHA_OPAQUE, true );  // legs
      printPbrMaterial( pw, 2, "0.366, 1.000, 0.766, 1.0", 0.5f, 0.2f, ALPHA_OPAQUE, true );  // splays
      printPbrMaterial( pw, 2, "0.500, 0.500, 0.500, 0.5", 0.5f, 0.5f, ALPHA_BLEND,  false ); // surface
    }
    closeArrayTag( pw, 1, true );

    openArrayTag( pw, 1, "buffers" ); // ----------- BUFFERS
    {
      if ( has_stations ) printBuffer( pw, 2, buffer_stations, bytelen_stations, true );
      if ( has_legs )     printBuffer( pw, 2, buffer_legs,     bytelen_legs,     has_splays_or_surface );
      if ( has_splays )   printBuffer( pw, 2, buffer_splays,  bytelen_splays,  has_surface );
      if ( has_surface )  printBuffer( pw, 2, buffer_surface, bytelen_surface, false );
    }
    closeArrayTag( pw, 1, true );

    openArrayTag( pw, 1, "bufferViews" ); // ----------- BUFFERVIEWS
    {
      int buffer = 0;
      if ( has_stations ) printBufferView( pw, 2, buffer++, 0, bytelen_stations, 12, ARRAY_COORDS, true );
      if ( has_legs )     printBufferView( pw, 2, buffer++, 0, bytelen_legs,     12, ARRAY_COORDS, has_splays_or_surface );
      if ( has_splays )   printBufferView( pw, 2, buffer++, 0, bytelen_splays,   12, ARRAY_COORDS, has_surface );
      if ( has_surface ) {
        printBufferView( pw, 2, buffer, 0, bytelen_surface,  24, ARRAY_COORDS, true );
        printBufferView( pw, 2, buffer, 0, bytelen_surface,  24, ARRAY_COORDS, false );
      }
    }
    closeArrayTag( pw, 1, true ); 

    openArrayTag( pw, 1, "accessors" ); // -------------- ACCESSORS
    {
      int view = 0;
      if ( has_stations ) printAccessor( pw, 2, view++, 0, TYPE_FLOAT, count_stations, TYPE_VEC3, max_stations, min_stations, true );
      if ( has_legs     ) printAccessor( pw, 2, view++, 0, TYPE_FLOAT, count_legs,     TYPE_VEC3, max_legs,     min_legs,     has_splays_or_surface );
      if ( has_splays   ) printAccessor( pw, 2, view++, 0, TYPE_FLOAT, count_splays,   TYPE_VEC3, max_splays,   min_splays,   has_surface );
      if ( has_surface  ) {
        printAccessor( pw, 2, view++,  0, TYPE_FLOAT, count_surface,  TYPE_VEC3, max_surface,  min_surface,  true );
        printAccessor( pw, 2, view, 12, TYPE_FLOAT, count_surface,  TYPE_VEC3, max_surface_normals,  min_surface_normals,  false );
      }
    }
    closeArrayTag( pw, 1, true ); // END accessors

    /*
    openSetTag( pw, 1, "extensions" );
    openSetTag( pw, 2, "KHR_lights_punctual" );
    openArrayTag( pw, 3, "lights" );
    printLight( pw, 4, "0.7, 0.8, 1.0",  3.0f, LIGHT_DIRECTIONAL, true );
    printLight( pw, 4, "1.0, 0.6, 0.4", 20.0f, LIGHT_POINT, true );
    printLight( pw, 4, "1.0, 0.6, 0.4", 40.0f, LIGHT_SPOT, 0.78f, 1.57f, false );
    closeArrayTag( pw, 3, false );
    closeSetTag( pw, 2, false );
    closeSetTag( pw, 1, true );
    */

    openSetTag( pw, 1, "asset" );
    printAttribute( pw, 2, "version", "\"2.0\"" );
    closeSetTag( pw, 1, false );

    closeElement( pw, 0, false );
  }

  private void printPbrMaterial( PrintWriter pw, int indent, String baseColor, float metallic, float roughness, String alpha, boolean comma )
  {
    openElement(  pw, indent ); // 
    openSetTag( pw, indent+1, "pbrMetallicRoughness" );
    printArrayTagWithComma( pw, indent+2, "baseColorFactor", baseColor );
    printAttributeWithComma( pw, indent+2, "metallicFactor",  String.format(Locale.US, "%.2f", metallic ) );
    printAttribute( pw, indent+2, "roughnessFactor", String.format(Locale.US, "%.2f", roughness ) );
    closeElement( pw, indent+1, true ); 
    // NOT AN ATTROBUTE printAttributeWithComma( pw, indent+1, "opacity", "0.5" );
    printAttributeString( pw, indent+1, "alphaMode", alpha );
    closeElement( pw, indent, comma ); 
  }

  private void printNode( PrintWriter pw, int indent, int mesh, boolean comma )
  {
    openElement( pw, indent ); 
    printAttribute( pw, indent+1, "mesh", mesh );
    closeElement( pw, indent, comma );
  }

  private void printNodeLight( PrintWriter pw, int indent, int light, String rotation, boolean comma )
  {
    openElement( pw, indent ); 
    openSetTag( pw, indent+1, "extensions" );
    openSetTag( pw, indent+2, "KHR_lights_punctual" );
    printAttribute( pw, indent+3, "light", light );
    closeElement( pw, indent+2, false ); 
    closeElement( pw, indent+1, true ); 
    printArrayTag( pw, indent+1, "rotation", rotation );
    closeElement( pw, indent, comma );
  }

  private void printLight( PrintWriter pw, int indent, String color, float intensity, String type, boolean comma )
  {
    openElement( pw, indent ); 
    printArrayTagWithComma( pw, indent+1, "color", color );
    printAttributeWithComma( pw, indent+1, "intensity", String.format(Locale.US, "%.2f", intensity) );
    printAttributeString( pw, indent+1, "type", type );
    closeElement( pw, indent, comma );
  }
  private void printLight( PrintWriter pw, int indent, String color, float intensity, String type, float inner, float outer, boolean comma )
  {
    openElement( pw, indent ); 
    printArrayTagWithComma( pw, indent+1, "color", color );
    printAttributeWithComma( pw, indent+1, "intensity", String.format(Locale.US, "%.2f", intensity) );
    printAttributeStringWithComma( pw, indent+1, "type", type );
    openSetTag( pw, indent+1, "spot" );
    printAttributeWithComma( pw, indent+1, "innerConeAngle", String.format(Locale.US, "%.2f", inner) );
    printAttribute( pw, indent+1, "outerConeAngle", String.format(Locale.US, "%.2f", outer) );
    closeSetTag( pw, indent+1, false );
    closeElement( pw, indent, comma );
  }

  private void printMesh( PrintWriter pw, int indent, int position, String mode, int material, boolean comma )
  {
    openElement(  pw, indent ); // stations
    int i = indent + 1;
    openArrayTag( pw, i, "primitives" );
    openElement(  pw, i+1 ); 
    openSetTag( pw, i+2, "attributes" );
    printAttribute( pw, i+3, "POSITION", position ); // accessor 
    closeSetTag( pw, i+2, true );
    if ( material >= 0 ) {
      printAttributeWithComma( pw, i+2, "material", material );
    }
    printAttribute( pw, i+2, "mode", mode );
    closeElement(  pw, i+1, false ); 
    closeArrayTag( pw, i, false );
    closeElement(  pw, indent, comma ); 
  }

  private void printMeshPN( PrintWriter pw, int indent, int position, int normal, String mode, int material, boolean comma )
  {
    openElement(  pw, indent ); // stations
    int i = indent + 1;
    openArrayTag( pw, i, "primitives" );
    openElement(  pw, i+1 ); 
    openSetTag( pw, i+2, "attributes" );
    printAttributeWithComma( pw, i+3, "POSITION", position ); // accessor 
    printAttribute( pw, i+3, "NORMAL",   normal ); // accessor 
    closeSetTag( pw, i+2, true );
    if ( material >= 0 ) {
      printAttributeWithComma( pw, i+2, "material", material );
    }
    printAttribute( pw, i+2, "mode", mode );
    closeElement(  pw, i+1, false ); 
    closeArrayTag( pw, i, false );
    closeElement(  pw, indent, comma ); 
  }

  private void printBuffer( PrintWriter pw, int indent, String uri, int byteLength, boolean comma )
  {
    openElement(  pw, indent ); 
    int i = indent + 1;
    printAttributeStringWithComma( pw, i, "uri", uri );
    // printAttribute( pw, i, "uri", "\"data:application/octet-stream;base64," );
    // pw.writeChars( baos1.toString() );
    // pw.writeChars("\",\n");
    printAttribute( pw, i, "byteLength", byteLength );
    closeElement(  pw, indent, comma );
  }

  private void printBufferView( PrintWriter pw, int indent, int buffer, int byteOffset, int byteLength, int byteStride, String target, boolean comma )
  {
    openElement(  pw, indent ); // 0: stations vertices
    int i = indent + 1;
    printAttributeWithComma( pw, i, "buffer",     buffer );
    printAttributeWithComma( pw, i, "byteOffset", byteOffset );
    printAttributeWithComma( pw, i, "byteLength", byteLength );
    printAttributeWithComma( pw, i, "byteStride", byteStride ); // FIXME
    printAttribute( pw, i, "target", target );
    closeElement(  pw, indent, comma ); 
  }

  private void printAccessor( PrintWriter pw, int indent,
                              int bufferView, int byteOffset, String compType, int count, String type, String max, String min, boolean comma )
  {
    openElement(  pw, indent ); // 2: splay positions
    int i = indent+1;
      printAttributeWithComma( pw, i, "bufferView", bufferView );
      printAttributeWithComma( pw, i, "byteOffset", byteOffset );
      printAttributeWithComma( pw, i, "componentType", compType );
      printAttributeWithComma( pw, i, "count", count );
      printAttributeStringWithComma( pw, i, "type", type );
      printAttributeWithComma( pw, i, "max", max );
      printAttribute( pw, i, "min", min );
    closeElement(  pw, indent, comma ); 
  }

  //                                       0   1__   2____   3______   4________   5__________   6____________
  private final static String[] INDENT = { "", "  ", "    ", "      ", "        ", "          ", "            " };

  private void openElement( PrintWriter pw, int indent )  // throws IOException
  { 
    pw.format( "%s{\n", INDENT[indent] );
  }
  
  private void closeElement( PrintWriter pw, int indent, boolean comma )  // throws IOException
  {
    if ( comma ) {
      pw.format( "%s},\n", INDENT[indent] );
    } else {
      pw.format( "%s}\n", INDENT[indent] );
    }
  }

  private void openArrayTag( PrintWriter pw, int indent, String tag ) // throws IOException
  { 
    pw.format( "%s\"%s\" : [\n", INDENT[indent], tag );
  }
  private void closeArrayTag( PrintWriter pw, int indent, boolean comma )  // throws IOException
  { 
    if ( comma ) {
      pw.format( "%s],\n", INDENT[indent] );
    } else {
      pw.format( "%s]\n", INDENT[indent] );
    }
  }

  private void printArrayTag( PrintWriter pw, int indent, String tag, String vals )  // throws IOException
  { 
    pw.format( "%s\"%s\" : [ %s ]\n", INDENT[indent], tag, vals );
  }
  private void printArrayTagWithComma( PrintWriter pw, int indent, String tag, String vals )  // throws IOException
  { 
    pw.format( "%s\"%s\" : [ %s ],\n", INDENT[indent], tag, vals );
  }

  private void openSetTag( PrintWriter pw, int indent, String tag )  // throws IOException
  {
    pw.format( "%s\"%s\" : {\n", INDENT[indent], tag );
  }
  private void closeSetTag( PrintWriter pw, int indent, boolean comma ) // throws IOException
  { 
    if ( comma ) {
      pw.format( "%s},\n", INDENT[indent] );
    } else {
      pw.format( "%s}\n", INDENT[indent] );
    }
  }

  private void printAttributeWithComma( PrintWriter pw, int indent, String tag, int val ) // throws IOException
  { 
    pw.format( "%s\"%s\" : %d,\n", INDENT[indent], tag, val );
  }
  private void printAttribute( PrintWriter pw, int indent, String tag, int val ) // throws IOException
  { 
    pw.format( "%s\"%s\" : %d\n", INDENT[indent], tag, val );
  }

  private void printAttributeStringWithComma( PrintWriter pw, int indent, String tag, String val )  // throws IOException
  { 
    pw.format( "%s\"%s\" : \"%s\",\n", INDENT[indent], tag, val );
  }
  private void printAttributeString( PrintWriter pw, int indent, String tag, String val ) // throws IOException
  { 
    pw.format( "%s\"%s\" : \"%s\"\n", INDENT[indent], tag, val );
  }

  private void printAttributeWithComma( PrintWriter pw, int indent, String tag, String val ) // throws IOException
  { 
    pw.format( "%s\"%s\" : %s,\n", INDENT[indent], tag, val );
  }
  private void printAttribute( PrintWriter pw, int indent, String tag, String val ) // throws IOException
  { 
    pw.format( "%s\"%s\" : %s\n", INDENT[indent], tag, val );
  }

  private void printElement( PrintWriter pw, int indent, String val, boolean comma ) // throws IOException
  { 
    if ( comma ) {
      pw.format( "%s\"%s\",\n", INDENT[indent], val );
    } else {
      pw.format( "%s\"%s\"\n", INDENT[indent], val );
    }
  }


  private final static int BUFFER_SIZE1 = 1024*12; // one vertex
  private final static int BUFFER_SIZE2 = 1024*24; // 2 * 3*4   two vertices pos
  private final static int BUFFER_SIZE6 = 1024*72; // (2*3) * 3*4 three vertices pos+normal

  // COORDS_PER_VERTEX = 3;
  // OFFSET_VERTEX     = 0;
  // STRIDE_VERTEX     = 12; // Float.BYTES * COORDS_PER_VERTEX;
  // @param min     minimun coord value
  // @param scale   (max - min)/2
  private int doExportNames( String filepath, GlNames names, MinMax minMax )
  {
    int len = 0;
    try {
      FileOutputStream fos = new FileOutputStream( filepath );
      DataOutputStream dos = new DataOutputStream( fos );
      if ( names != null && names.getVertexData() != null ) {
        int fstride = GlNames.getVertexStride(); 
        int fcoords = GlNames.getVertexSize(); 
        int fcount  = names.size();
        if ( fcount > 0 ) {
          minMax.reset();
          float[] array = names.getVertexData();
          int     flen  = names.size() * fstride;
          int offset = 0;
          while ( offset < flen ) {
            // TDLog.v("Gltf Names file " + filepath + " stride " + fstride + " coords " + fcoords + " flen " + flen + " offset " + offset );
            byte[] buffer = new byte[ BUFFER_SIZE1 ];
            int nb = getNextChunk( buffer, BUFFER_SIZE1, array, flen, offset, fcoords, fstride, minMax );
            dos.write( buffer, 0, nb );
            offset += fstride * nb / (4*fcoords);
            len += nb;
          }
          // TDLog.v("Gltf Names file " + filepath + " written len " + len + " offset " + offset );
        }
      }
      dos.flush();
      dos.close();
    } catch ( FileNotFoundException e ) { 
      TDLog.Error("Gltf file " + filepath + " not found");
    } catch ( IOException e ) {
      TDLog.Error("Gltf file " + filepath + " write failed");
    }
    return len;
  }

  // COORDS_PER_VERTEX = 3;
  // COORDS_PER_COLOR  = 4;
  // STRIDE = 7; // COORDS_PER_VERTEX + COORDS_PER_COLOR;
  // BYTE_STRIDE = 28; // STRIDE * Float.BYTES;
  // OFFSET_VERTEX = 0;
  // OFFSET_COLOR  = 3; // COORDS_PER_VERTEX;
  private int doExportLines( String filepath, GlLines lines, MinMax minMax )
  {
    int len = 0;
    try {
      FileOutputStream fos = new FileOutputStream( filepath );
      DataOutputStream dos = new DataOutputStream( fos );
      if ( lines != null && lines.getVertexData() != null ) {
        int fstride = GlLines.getVertexStride(); 
        int fcoords = GlLines.getVertexSize(); 
        int fcount  = lines.size(); // getVertexCount();
        if ( fcount > 0 ) {
          minMax.reset();
          float[] array = lines.getVertexData();
          int     flen  = lines.size() * fstride * 2; // 14 floats per line (two vertices of 3+4 floats)
          int offset = 0;
          // TDLog.v("Gltf Lines file stride " + fstride + " coords " + fcoords + " flen " + flen + " offset " + offset + " count " + fcount );
          while ( offset < flen ) {
            byte[] buffer = new byte[ BUFFER_SIZE2 ];
            int nb = getNextChunk( buffer, BUFFER_SIZE2, array, flen, offset, fcoords, fstride, minMax );
            dos.write( buffer, 0, nb );
            offset += fstride * nb / (4 * fcoords);
            len += nb;
          }
          // TDLog.v("Gltf Lines file written len " + len + " end offset " + offset );
        }
      }
      dos.flush();
      dos.close();
    } catch ( FileNotFoundException e ) { 
      TDLog.Error("Gltf File LINES not found");
    } catch ( IOException e ) {
      TDLog.Error("Gltf File LINES write failed");
    }
    return len;
  }

  // COORDS_PER_VERTEX = 3;
  // COORDS_PER_NORMAL = 3;
  // COORDS_PER_TEXEL  = 2;
  // STRIDE = 8; // COORDS_PER_VERTEX + COORDS_PER_NORMAL + COORDS_PER_TEXEL
  // BYTE_STRIDE = 32; // STRIDE * Float.BYTES;
  // OFFSET_VERTEX = 0;
  // OFFSET_NORMAL = 3; // COORDS_PER_VERTEX;
  private int doExportTriangles( String filepath, GlSurface surface, MinMax minMaxP, MinMax minMaxN )
  {
    // TDLog.v("Gltf export triangles file " + filepath );
    int len = 0;
    try {
      FileOutputStream fos = new FileOutputStream( filepath );
      DataOutputStream dos = new DataOutputStream( fos );
      if ( surface != null && surface.getSurfaceData() != null ) {
        int fstride = GlSurface.getVertexStride(); 
        int fcoords = GlSurface.getVertexSize(); // position + normal
        int fcount  = surface.getVertexCount();
        if ( fcount > 0 ) {
          minMaxP.reset();
          minMaxN.reset();
          float[] array = surface.getSurfaceData();
          int     flen  = surface.size() * fstride * 3; // 3*3*6 floats per triangle (three vertices of 3+3 floats)
          int offset = 0;
          // TDLog.v("Gltf Surface file stride " + fstride + " coords " + fcoords + " flen " + flen + " offset " + offset + " count " + fcount );
          while ( offset < flen ) {
            byte[] buffer = new byte[ BUFFER_SIZE6 ];
            int nb = getNextChunkPN( buffer, BUFFER_SIZE6, array, flen, offset, fcoords, fstride, minMaxP, minMaxN );
            dos.write( buffer, 0, nb );
            offset += fstride * nb / (4 * fcoords);
            len += nb;
          }
          // TDLog.v("Gltf Surface file written len " + len + " end offset " + offset );
        }
      }
      dos.flush();
      dos.close();
    } catch ( FileNotFoundException e ) { 
      TDLog.Error("Gltf File SURFACE not found");
    } catch ( IOException e ) {
      TDLog.Error("Gltf File SURFACE write failed");
    }
    return len;
  }

  /** copy a chunk of bytes from an array of float to and array of bytes
   * @param buf     array of bytes (preallocated)
   * @param clen    length of the array of bytes
   * @param array   array of floats
   * @param flen    length of the array of floats
   * @param offset  index in the array of float to start with
   * @param len     number of floats to copy for each "vertex"
   * @param stride  number of floats in a vertex
   * @return the number of bytes copied into the array of bytes
   */
  private int getNextChunk( byte[] buf, int clen, float[] array, int flen, int offset, int len, int stride, MinMax minMax )
  {
    // StringBuilder sb = new StringBuilder();
    int bpos = 0;
    int klen = clen / (4*len);
    // TDLog.v("Gltf Get chunk Flen " + flen + " offset " + offset + " len " + len + " klen " + klen );
    // sb.append("[" + klen + "]" );
    for ( int k=0; k<klen; ++k ) {
      int fpos = offset + k * stride;
      if ( fpos >= flen ) break;
      for ( int j=0; j<len; ++j ) {
        float fval = array[fpos+j];
        if ( j == 0 ) {
          if ( fval < minMax.xmin ) { minMax.xmin = fval; } else if  ( fval > minMax.xmax ) { minMax.xmax = fval; }
        } else if ( j == 1 ) {
          if ( fval < minMax.ymin ) { minMax.ymin = fval; } else if  ( fval > minMax.ymax ) { minMax.ymax = fval; }
        } else if ( j == 2 ) {
          if ( fval < minMax.zmin ) { minMax.zmin = fval; } else if  ( fval > minMax.zmax ) { minMax.zmax = fval; }
        }
        int val = Float.floatToRawIntBits( fval );
        // sb.append( " " + fval );
        buf[bpos++] = (byte) (val);
        buf[bpos++] = (byte) (val >> 8);
        buf[bpos++] = (byte) (val >> 16) ;
        buf[bpos++] = (byte) (val >> 24);
      }
    }
    // TDLog.v("Gltf Chunk " + sb.toString() );
    return bpos;
  }

  /** copy a chunk of bytes from an array of float to and array of bytes
   * @param buf     array of bytes (preallocated)
   * @param clen    length of the array of bytes
   * @param array   array of floats
   * @param flen    length of the array of floats
   * @param offset  index in the array of float to start with
   * @param len     number of floats to copy for each "vertex"
   * @param stride  number of floats in a vertex
   * @param minMaxP position min-max
   * @param minMaxN normal min-max
   * @return the number of bytes copied into the array of bytes
   */
  private int getNextChunkPN( byte[] buf, int clen, float[] array, int flen, int offset, int len, int stride, MinMax minMaxP, MinMax minMaxN )
  {
    // assert( len == 6 );
    // StringBuilder sb = new StringBuilder();
    int bpos = 0;
    int klen = clen / (4*len);
    // TDLog.v("Gltf Get chunk Flen " + flen + " offset " + offset + " len " + len + " klen " + klen );
    // sb.append("[" + klen + "]" );
    for ( int k=0; k<klen; ++k ) {
      int fpos = offset + k * stride;
      if ( fpos >= flen ) break;
      
      float xval = array[fpos+0]; // position
      float yval = array[fpos+1];
      float zval = array[fpos+2];
      if ( xval < minMaxP.xmin ) { minMaxP.xmin = xval; } else if  ( xval > minMaxP.xmax ) { minMaxP.xmax = xval; }
      if ( yval < minMaxP.ymin ) { minMaxP.ymin = yval; } else if  ( yval > minMaxP.ymax ) { minMaxP.ymax = yval; }
      if ( zval < minMaxP.zmin ) { minMaxP.zmin = zval; } else if  ( zval > minMaxP.zmax ) { minMaxP.zmax = zval; }
      int val = Float.floatToRawIntBits( xval );
      buf[bpos++] = (byte) (val);
      buf[bpos++] = (byte) (val >> 8);
      buf[bpos++] = (byte) (val >> 16) ;
      buf[bpos++] = (byte) (val >> 24);
      val = Float.floatToRawIntBits( yval );
      buf[bpos++] = (byte) (val);
      buf[bpos++] = (byte) (val >> 8);
      buf[bpos++] = (byte) (val >> 16) ;
      buf[bpos++] = (byte) (val >> 24);
      val = Float.floatToRawIntBits( zval );
      buf[bpos++] = (byte) (val);
      buf[bpos++] = (byte) (val >> 8);
      buf[bpos++] = (byte) (val >> 16) ;
      buf[bpos++] = (byte) (val >> 24);

      xval = array[fpos+3]; // normal
      yval = array[fpos+4];
      zval = array[fpos+5];
      float nlen = (float)Math.sqrt( xval*xval + yval*yval + zval*zval );
      xval /= nlen;
      yval /= nlen;
      zval /= nlen;
      if ( xval < minMaxN.xmin ) { minMaxN.xmin = xval; } else if  ( xval > minMaxN.xmax ) { minMaxN.xmax = xval; }
      if ( yval < minMaxN.ymin ) { minMaxN.ymin = yval; } else if  ( yval > minMaxN.ymax ) { minMaxN.ymax = yval; }
      if ( zval < minMaxN.zmin ) { minMaxN.zmin = zval; } else if  ( zval > minMaxN.zmax ) { minMaxN.zmax = zval; }
      val = Float.floatToRawIntBits( xval );
      buf[bpos++] = (byte) (val);
      buf[bpos++] = (byte) (val >> 8);
      buf[bpos++] = (byte) (val >> 16) ;
      buf[bpos++] = (byte) (val >> 24);
      val = Float.floatToRawIntBits( yval );
      buf[bpos++] = (byte) (val);
      buf[bpos++] = (byte) (val >> 8);
      buf[bpos++] = (byte) (val >> 16) ;
      buf[bpos++] = (byte) (val >> 24);
      val = Float.floatToRawIntBits( zval );
      buf[bpos++] = (byte) (val);
      buf[bpos++] = (byte) (val >> 8);
      buf[bpos++] = (byte) (val >> 16) ;
      buf[bpos++] = (byte) (val >> 24);
    }
    // TDLog.v("Gltf Chunk " + sb.toString() );
    return bpos;
  }

  /*
  private int getNextChunk2_3( byte[] buf, int clen, float[] array, int flen, int offset, int len, int stride, float min, float scale )
  {
    StringBuilder sb = new StringBuilder();
    int bpos = 0;
    int klen = clen / (4*len);
    sb.append("[" + klen + "]" );
    for ( int k=0; k<klen; ++k ) {
      int fpos = offset + k * stride;
      if ( fpos >= flen ) break;
      for ( int j=0; j<len; ++j ) {
        float fval1 = (array[fpos+j+0]-min)/scale - 1.0f;
        int val1 = Float.floatToRawIntBits( fval1 );
        // float fval2 = (array[fpos+j+1]-min)/scale - 1.0f;
        // int val2 = Float.floatToRawIntBits( fval2 );
        // sb.append( " " + fval1 );
        buf[bpos++] = (byte) (val1 >> 24);
        buf[bpos++] = (byte) (val1 >> 16) ;
        buf[bpos++] = (byte) (val1 >> 8);
        buf[bpos++] = (byte) (val1);
      }
    }
    // TDLog.v("Gltf Chunk " + sb.toString() );
    return bpos;
  }
  */

}

