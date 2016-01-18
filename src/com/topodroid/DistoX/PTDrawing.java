/** @file PTDrawing.java
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief PocketTopo file IO
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.util.Log;

class PTDrawing
{
    PTMapping _mapping;
    ArrayList< PTElement > _elements;

    PTDrawing()
    { 
      _mapping = new PTMapping();
      _elements = new ArrayList< PTElement >();
    }

    // PTDrawing( PTDrawing drawing )
    // {
    //   _mapping = drawing._mapping;
    //   _elements = drawing._elements;
    // }

    // ---------------------------------------------------

    PTMapping mapping() { return _mapping; }

    int elementNumber() { return _elements.size(); }

    PTElement getElement( int k ) { return (PTElement)_elements.get(k); }

    /** insert an element
     * @param el   element to insert
     * @note the caller releases the ownership of the element to the PTdrawing
     */
    void insertElement( PTElement el ) { _elements.add( el ); }

    // ---------------------------------------------------

    void read( FileInputStream fs )
    {
      clear();
      _mapping.read( fs );
      while ( true ) {
        PTElement el = null;
        byte id = PTFile.readByte( fs );
        if ( id == PTElement.ID_POLYGON_ELEMENT ) {
          // TDLog.Log( TDLog.LOG_PTOPO, "PT drawing id polygon" );
          el = new PTPolygonElement();
        } else if ( id == PTElement.ID_XSECTION_ELEMENT) {
          // TDLog.Log( TDLog.LOG_PTOPO, "PT drawing id xsection" );
          el = new PTXSectionElement();
        } else if ( id == PTElement.ID_NO_ELEMENT ) {
          // TDLog.Log( TDLog.LOG_PTOPO, "PT drawing id no element" );
          break;
        }
        if ( el != null ) el.read( fs );
        _elements.add( el );
      }
      // Log.v("PTDistoX", "drawing read " + _elements.size() + " elements " );
    }

    void write( FileOutputStream fs )
    {
      _mapping.write( fs );
      // Log.v( "DistoX", "PT drawing write elements " + _elements.size() );
      for ( PTElement e : _elements ) e.write( fs );
      byte id = PTElement.ID_NO_ELEMENT;
      PTFile.writeByte( fs, id );
    }

    // void print()
    // {
    //   // Log.v( TopoDroidApp.TAG,  "drawing: size " + _elements.size() );
    //   _mapping.print();
    //   for ( PTElement el : _elements ) el.print();
    // }

    /** clear the drawing elements
     */
    void clear() 
    {
      _mapping.clear();
      _elements.clear();
    }

    void setElements( ArrayList< PTElement > elements )
    {
      for ( PTElement e : elements ) {
        _elements.add( e ); // FIXME copy the specific type ?
      }
    }

    /*
    void 
    PTdrawing::printTherion( FileOutputStream fp,
                       String name,
                       String proj,
                       String[] points, 
                       String[] lines )
    {
      int x0 = _mapping.origin().x();
      int y0 = _mapping.origin().y();
      int scale = _mapping.scale();
      double xmin=0.0, ymin=0.0, xmax=XTHERION_FACTOR, ymax=XTHERION_FACTOR;
      for ( size_t k = 0; k<_elements.size(); ++k ) {
        _elements[k]->xtherionBounds( x0, y0, scale, xmin, ymin, xmax, ymax );
      }
      xmin -= XTHERION_FACTOR;
      ymin -= XTHERION_FACTOR;
      xmax += XTHERION_FACTOR;
      ymax += XTHERION_FACTOR;
    
      fprintf(fp, "encoding utf-8\n");
      fprintf(fp, "##XTHERION## xth_me_area_adjust %.2f %.2f %.2f %.2f \n", FIXME Locale.ENGLISH
        xmin, ymin, xmax, ymax );
      fprintf(fp, "##XTHERION## xth_me_area_zoom_to 100\n\n");
    
      fprintf(fp, "  scrap %s -projection %s -scale 0.025\n", name, proj );
      for ( size_t k = 0; k<_elements.size(); ++k ) {
        _elements[k]->printTherion( fp, x0, y0, scale, points, lines );
      }
      fprintf(fp, "  endscrap\n\n");
    }
    */

}

/*
#include <map>

void header( const char * program )
{
  static bool header_done = false;
  if ( header_done ) return;
  fprintf(stderr, "PocketTopo to Therion converter.\n");
  header_done = true;
}

void usage( const char * program )
{
  static bool done = false;
  if ( done ) return;
  header( program );
  fprintf(stderr, "  Usage: %s [options] <input_filename>\n", program );
  fprintf(stderr, "  Options:\n");
  fprintf(stderr, "     -p: print PocketTopo format to stdout\n");
  fprintf(stderr, "     -t: convert to Therion\n");
  fprintf(stderr, "     -o <prefix>: therion filename prefix [default \"cave\"] (implies -t)\n");
  fprintf(stderr, "     -c <color_file>: color-to-type map for points and lines\n");
  fprintf(stderr, "     -v: verbose\n");
  fprintf(stderr, "     -h: this help\n\n");
  fprintf(stderr, "  If neither of -p or -t is specified, -p is assumed.\n");
  fprintf(stderr, "  The color file is a plain text file with the list of color-to-type maps\n");
  fprintf(stderr, "  for the points, and the color-to-type maps for the lines.\n");
  fprintf(stderr, "  Each map color-to-type consists of a color name followed by the\n");
  fprintf(stderr, "  corresponding therion point/line type\n");
  fprintf(stderr, "  The available colors are: black, gray, brown, blue, red, green.\n");
  fprintf(stderr, "  Example of map line: \"black point stalagmite\"\n\n");

  done = true;
}

void
read_colors( const char * filename, const char * cp[], const char * cl[] ) 
{
  std::map<const char *, int> col_index;
  col_index["black"] = 1;
  col_index["gray"]  = 2;
  col_index["brown"] = 3;
  col_index["blue"]  = 4;
  col_index["red"]   = 5;
  col_index["green"] = 6;
  static std::string col_pts[7];
  static std::string col_lns[7];
  FILE * fp = fopen( filename, "r" );
  if ( fp == NULL ) {
    fprintf(stderr, "Warning: unable to open color file \"%s\"\n", filename);
    return;
  }

  char * line = NULL;
  while ( getline( &line, NULL, fp ) ) {
    const char * ch = line;
    while ( *ch && isspace(*ch) ) ++ch;
    for ( std::map<const char *, int>::const_iterator it = col_index.begin();
          it != col_index.end();
          ++ it ) {
      if ( strncmp( it->first, ch, strlen(it->first) ) == 0 ) {
        int index = it->second;
        while( ! isspace(*ch) ) ++ch;
        while( *ch && isspace(*ch) ) ++ch;
        if ( strncmp( "point", ch, 5 ) == 0 ) {
          while ( ! isspace(*ch) ) ++ch;
          while ( *ch && isspace(*ch) ) ++ch;
          if ( strlen(ch) == 0 ) {
            fprintf(stderr, "Colorfile. Missing point type: %s\n", line);
          } else {
            col_pts[index] = ch;
          }
        } else if ( strncmp( "line", ch, 4 ) == 0 ) {
          while ( ! isspace(*ch) ) ++ch;
          while ( *ch && isspace(*ch) ) ++ch;
          if ( strlen(ch) == 0 ) {
            fprintf(stderr, "Colorfile. Missing line type: %s\n", line);
          } else {
            col_lns[index] = ch;
          }
        } else {
          fprintf(stderr, "Colorfile. Line format error: %s\n", line );
        }
        break;
      }
    }
    free( line );
    line =  NULL;
  }
  for (size_t k = 0; k<7; ++k ) {
    cp[k] = col_pts[k].c_str();
    cl[k] = col_lns[k].c_str();
  }
} 

int main( int argc, char ** argv ) 
{
  bool do_verbose = false;
  bool do_print = false;
  bool do_therion = false;
  const char * th_prefix = "cave";
  const char * color_point[7];
  const char * color_line[7];
  int argf = 1;

  PTfile::initColors( color_point, color_line );

  if ( argc <= argf ) {
    printf("Usage: %s [options] <filename>\n", argv[0] );
    return 0;
  }
  while ( argf < argc && argv[argf][0] == '-' ) {
    switch ( argv[argf][1] ) {
      case 'c':
        ++ argf;
        if ( argf < argc ) {
          read_colors( argv[argf], color_point, color_line );
        }
        break;
      case 'p':
        do_print = true;
        break;
      case 't':
        do_therion = true;
        break;
      case 'o':
        do_therion = true;
        ++ argf;
        if ( argf < argc ) {
          th_prefix = argv[argf];
        }
        break;
      case 'v':
        do_verbose = true;
        break;
      case 'h':
      default:
        usage( argv[0] );
        return 0;
    }
    ++ argf;
  }
  if ( ! ( do_print || do_therion ) ) do_print = true;

  if ( argc <= argf ) {
    usage( argv[0] );
    fprintf(stderr, "ERROR. Missing input filename\n");
    return 0;
  }

  FILE * fs = fopen( argv[argf], "r" );
  if ( fs == NULL ) {
    header( argv[0] );
    fprintf(stderr, "ERROR. Cannot open input file \"%s\"\n", argv[argf] );
    return 0;
  } 
  if ( do_verbose ) {
    header( argv[0] );
    for ( int k=1; k<7; ++k ) {
      fprintf(stderr, "Point[%d]: %20s Line[%d]: %s\n",
        k, color_point[k], k, color_line[k] );
    }
  }

  PTfile ptfile;
  ptfile.read( fs );
  fclose( fs );
  ptfile.setColorPoint( color_point );
  ptfile.setColorLine( color_line );
  if ( do_print ) {
    ptfile.print();
  } 
  if ( do_therion ) {
    ptfile.printTherion( th_prefix );
  }
  return 0;
}

*/
