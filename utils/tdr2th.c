/** convert tdr file to Therion th2 file
*
* --------------------------------------------------------
*  Copyright This software is distributed under GPL-3.0 or later
*  See the file COPYING.
* --------------------------------------------------------
*
* Usage:
*   tdr2th file1.tdr [file2.tdr ...]
* The output files are file1.th2 [file2.th2 ...]
*
*/
#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <string.h>
#include <assert.h>
#include <arpa/inet.h>

int sc = sizeof( char );
int si = sizeof( int );
int sf = sizeof( float );
uint32_t s32 = sizeof( uint32_t );

int VERSION = 0;
int max = INT_MAX - 2;  // max pos read
const float THERION_SCALE = 196.8503937f;
const float ONE_METER = THERION_SCALE / 5; // THERION_SCALE * 20.0f / 100;

char * OUTLINE[] = { "in", "none", "out" };
char * SCALE[] = { "XS", "S", "M", "L", "XL" };
char * PROJECTION[] = { "none", "plan", "extended", "none", "photo", "none", "3d", "none", "elevation" };
char * STATIONS = NULL;
int memStations = 2000;
int posStations = 0;

int toLE( int * pi )
{
  unsigned char * ch = (unsigned char*)pi;
  return (int)( ( ((int)ch[0])<<24 ) | ( ((int)ch[1]) << 16 ) | ( ((int)ch[2]) << 8 ) | ( ((int)ch[3]) ) );
}

int checkFilePos( FILE * fp )
{
  if ( ftell( fp ) > max ) {
    fprintf(stderr, "#: Exceeded maximum file size.\n");
    return 1;
  }
  return 0;
}

char * readString( char * hdr, FILE * fp, int null_return )
{
  // long pos = ftell( fp );
  char ch2[2];
  int j, len, c0, c1;
  char ch;
  fread( ch2, sc, 2, fp ); // skip 2 bytes
  c0 = ch2[0]; if ( c0 < 0 ) c0 += 256;
  c1 = ch2[1]; if ( c1 < 0 ) c1 += 256;
  len = ( c0 << 8 ) | c1;
  fprintf(stderr, "#: %s [length %d] <", hdr, len);
  char * ret = NULL;
  if ( len > 0 ) {
    if ( null_return == 0 ) ret = (char *)malloc( len+1 );
    for ( j=0; j<len; ++j ) {
      fread( &ch, sc, 1, fp );
      // if ( fp2 ) fprintf( fp2, "%c", ch, ch );
      if ( null_return == 0 ) ret[j] = ch;
      if ( checkFilePos( fp ) ) break;
    }
    if ( null_return == 0 ) ret[len] = 0;
  }
  // if ( fp2 ) fprintf( fp2, ">\n");
  return ret;
}

void read4ch( FILE * fp, int skip )
{
  char ch;
  int j;
  long pos = ftell( fp );
  fprintf(stderr, "#: %ld= [skip %d]", pos, skip);
  for ( j=0; j<skip; ++j ) {
    fread( &ch, sc, 1, fp );
    fprintf(stderr, " %02x ", ch );
    if ( checkFilePos( fp ) ) break;
  }
  fprintf(stderr, "\n");
}

int readInt( FILE * fp )
{
  uint32_t i32;
  fread( &i32, s32, 1, fp ); 
  return ntohl( i32 );
}

float readFloat( FILE * fp )
{
  uint32_t i32;
  int i;
  float * pf;
  fread( &i32, s32, 1, fp ); 
  i = ntohl( i32 ); 
  pf = (float *)&i;
  return *pf;
}

void readVersion( FILE * fp, FILE * fp2 )
{
  long pos = ftell( fp );
  int v = readInt( fp );
  VERSION = v;
  fprintf(stderr, "#: %ld= VERSION: %d\n", pos, v );
}


void readScrap( FILE * fp, FILE * fp2 )
{
  long pos = ftell( fp );
  int type, j, k;
  int azimuth = 0;
  fprintf(stderr, "#: %ld= SCRAP\n", pos);
  char * name = readString( "  Name ", fp, 0 );
  type = readInt( fp );
  if ( type == 8 ) azimuth = readInt( fp ); // type PROJECTED
  fprintf(stderr, "#:   Type %d \n", type );
  for ( int k=0; k<3; ++k ) {
    // j = readInt( fp );
    // fprintf(stderr, "#: %d ", j );
    readString( "", fp, 1 );
    // fprintf(stderr, "\n");
    // read4ch( fp, 2 ); fprintf(stderr, "\n");
    if ( checkFilePos( fp ) ) break;
  }
  if ( fp2 ) {
    if ( type >= 0 && type != 4 && type != 6 && type < 9 ) {
      if ( type == 8 ) {
        fprintf(fp2, "scrap %s -proj [%s %d]", name, PROJECTION[type], azimuth );
      } else {
        fprintf( fp2, "scrap %s -proj %s", name, PROJECTION[type] );
      }
      fprintf( fp2, " -scale [0 0 %.4f 0 0 0 1 0]", ONE_METER );
      fprintf( fp2, "\n");
    }
  }
}

void readBBox( FILE * fp, FILE * fp2 )
{
  long pos = ftell( fp );
  float x1 = readFloat( fp );
  float y1 = readFloat( fp );
  float x2 = readFloat( fp );
  float y2 = readFloat( fp );
  int north = readInt( fp );
  fprintf(stderr, "#: %ld= BBOX %.2f %.2f - %.2f %.2f (north %d)\n", pos, x1, y1, x2, y2, north );
  if ( north == 1 ) {
    x1 = readFloat( fp );
    y1 = readFloat( fp );
    x2 = readFloat( fp );
    y2 = readFloat( fp );
    fprintf(stderr, "#:   North %.2f %.2f - %.2f %.2f\n", x1, y1, x2, y2 );
  }
}

void readPlotData( FILE * fp, FILE * fp2 )
{
  long pos = ftell( fp );
  char ch;
  fprintf(stderr, "#: %ld= PlotData: ", pos);
  float xoff      = readFloat( fp );
  float yoff      = readFloat( fp );
  float azimuth   = readFloat( fp );
  float clino     = readFloat( fp );
  float intercept = readFloat( fp );
  fprintf(stderr, "#:   Offset %.2f %.2f Orientation %.2f %.2f Intercept %.2f\n", xoff, yoff, azimuth, clino, intercept );
  readString( "  start ", fp, 1 );
  readString( "  view  ", fp, 1 );
  readString( "  hide  ", fp, 1 );
  readString( "  nick  ", fp, 1 );
}

void readPoint( FILE * fp, FILE * fp2 )
{
  long pos = ftell( fp );
  float cx = readFloat( fp );
  float cy = readFloat( fp ); 
  fprintf(stderr, "#: %ld= POINT: X %.3f Y %.3f\n", pos, cx, cy );
  char * type = readString( "  Type ", fp, 0 );
  if ( VERSION >= 401147 ) readString( "  Group ", fp, 1 );
  float orient = readFloat( fp );
  int scale  = readInt( fp );
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;
  fprintf(stderr, "#:   Orientation %.2f Level %02x Scale %d Scrap %d\n", orient, lvl, scale, scrap );
  char * text = readString( "  Text ", fp, 0 );  // text
  char * options = readString( "  Options ", fp, 0 );  // options
  if ( fp2 ) {
     fprintf(fp2, "point %.2f %.2f -type %s -orientation %.2f -scale %s", cx, cy, type, orient, SCALE[scale+2] );
    if ( text != NULL ) fprintf( fp2, " -text \"%s\"", text );
    if ( options != NULL && strlen( options ) > 1 ) fprintf( fp2, " %s", options);
    fprintf( fp2, "\n");
  }
  if ( text != NULL ) free( text );
  if ( options != NULL ) free( options );
  free( type );
}
  
void readLabel( FILE * fp, FILE * fp2 )
{
  long pos = ftell( fp );
  float cx = readFloat( fp );
  float cy = readFloat( fp ); 
  // if ( VERSION >= 401147 ) readString( "  Group ", fp ); // label has null group 
  float azi   = readFloat( fp ); // orientation
  int scale = readInt( fp );   
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;
  fprintf(stderr, "#: %ld= LABEL: X %.3f Y %.3f Orientattion %.1f Scale %d Level %02x Scrap %d\n", pos, cx, cy, azi, scale, lvl, scrap );
  char * text = readString( "  Text ", fp, 0 );        // text
  char * options = readString( "  Options ", fp, 0 );  // options 
  if ( fp2 ) {
    fprintf( fp2, "point %.3f %.3f -type label -orientation %.1f -scale %s -text \"%s\"", cx, cy, azi, SCALE[2+scale], text );
    if ( options != NULL && strlen( options ) > 1 ) fprintf( fp2, " %s", options);
    fprintf( fp2, "\n");
  }
  if ( options != NULL ) free( options );
  free( text );
}

void readLinePoint( FILE * fp, int k, FILE * fp2 )
{
  long pos = ftell( fp );
  char ch;
  float x = readFloat( fp );
  float y = readFloat( fp );
  fread( &ch, sc, 1, fp );
  // int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff; N.B. Line Points do not have level
  // fprintf( fp2, "  %ld= [%d] %.2f %.2f %d ", pos, k, x, y, ch );
  if ( ch == 1 ) {
    float x1 = readFloat( fp );
    float y1 = readFloat( fp );
    float x2 = readFloat( fp );
    float y2 = readFloat( fp );
    if ( fp2 ) fprintf( fp2, " %.2f %.2f %.2f %.2f", x1, y1, x2, y2 );
  }
  if ( fp2 ) fprintf( fp2, " %.2f %.2f\n", x, y );
}

/** read a line:
  name, group, closed. reversed. outline, lside, level, scrap, options, nr_points, points
 */
void readLine( FILE * fp, FILE * fp2 )
{
  long pos = ftell( fp );
  int k , np;
  char closed, reversed;
  char * group = NULL;
  fprintf(stderr, "#: %ld= LINE:\n", pos);
  char * type = readString( "  Type ", fp, 0 );             // name
  if ( VERSION >= 401147 ) group = readString( "  Group ", fp, 0 ); // NOT USED
  fread( &closed, sc, 1, fp );
  fread( &reversed, sc, 1, fp );
  int outline = readInt( fp );
  int lside = (VERSION >= 602055 )? readInt( fp ) : -1;
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff; // NOT USED
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0; // NOT USED
  int scale = ( VERSION >= 604088 )? readInt( fp ) : 0; // NOT USED
  fprintf(stderr, "#:   Closed %d Rev. %d LSide %d Outline %d Level %02x Scrap %d Scale %d\n", closed, reversed, lside, outline, lvl, scrap, scale );
  char * options = readString( "  Options ", fp, 0 );     // options
  if ( fp2 ) fprintf( fp2, "line %s", type );
  if ( group == NULL ) {
    if ( fp2 ) fprintf( fp2, " -outline %s", OUTLINE[1+outline] );
  } else if ( strcmp( group, "wall" ) == 0 ) {
    if ( outline != 1 ) {
      if ( fp2 ) fprintf( fp2, " -outline %s", OUTLINE[1+outline] );
    }
  } else {
    if ( outline != 0 ) {
      if ( fp2 ) fprintf( fp2, " -outline %s", OUTLINE[1+outline] );
    }
  }
  if ( fp2 ) {
    if ( closed != 0 ) fprintf( fp2, " -closed" );
    if ( reversed != 0 ) fprintf( fp2, " -reversed" );
    if ( lside > 0 ) fprintf( fp2, " -lside %d", lside );
    if ( options != NULL && strlen( options ) > 1 ) fprintf( fp2, " %s", options);
    fprintf( fp2, "\n");
  }
  if ( options != NULL ) free( options );
  if ( group != NULL ) free( group );
  free( type );
  np = readInt( fp );   // nr. points
  fprintf(stderr, "#:  Nr. Points %d\n", np );
  for ( int k=0; k<np; ++k ) {
    readLinePoint( fp, k, fp2 );
    if ( checkFilePos( fp ) ) break;
  }
  if ( fp2 ) fprintf( fp2, "endline\n");
}

/** read an area object:
  name, group, prefix, counter, border_visibility, orientation, levle, scrap, nr_points, point
 */
void readArea( FILE * fp, FILE * fp2 )
{
  long pos = ftell( fp );
  char ch;
  char * options = NULL;
  fprintf(stderr, "#: %ld= AREA: ", pos);
  char * type = readString( "  Type ", fp, 0 );         // name
  if ( VERSION >= 401147 ) {
    readString( "  Group ", fp, 1 );
  }
  char * prefix = readString( "  Prefix ", fp, 0 );     // prefix ???
  int cnt = readInt( fp );                              // counter
  fread( &ch, sc, 1, fp );                              // visibility - NOT USED
  float orient = readFloat( fp );                       // orientation - NOT USED
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff; // level - NOT USED
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0; // scrap index
  int scale = ( VERSION >= 604096 )? readInt( fp ) : 0; // scale - NOT USED
  if ( VERSION >= 604098 ) options = readString( "  Options ", fp, 0 ); 

  if ( fp2 ) fprintf( fp2, "line border -id area_border_%d\n", cnt );
  int np = readInt( fp );       // nr points
  fprintf(stderr, "#:   Counter %d Visibility %d Orientation %.2f Level %02x Scrap %d Scale %d Nr.Points %d\n", cnt, ch, orient, lvl, scrap, scale, np );
  for ( int k=0; k<np; ++k ) {
    readLinePoint( fp, k, fp2 );
    if ( checkFilePos( fp ) ) break;
  }
  if ( fp2 ) {
    fprintf( fp2, "endline\n");
    fprintf( fp2, "area %s", type );
    if ( options != NULL && strlen(options) > 0 ) fprintf( fp2, " %s", options );
    fprintf( fp2, "\n");
    fprintf( fp2, "  area_border_%d\n", cnt );
    fprintf( fp2, "endarea\n");
  }
  if ( options != NULL ) free( options );
  free( prefix );
  free( type );
}

void readAutoStation( FILE * fp, FILE * fp2 )
{
  // level and scrap do not make much sense for auto-stations
  long pos = ftell( fp );
  float x = readFloat( fp ); // position
  float y = readFloat( fp );
  fprintf(stderr, "#: %ld= STATION:\n", pos);
  char * name = readString( "  Name ", fp, 0 );          // name
  // if ( VERSION >= 401147 ) readString( "  Group ", fp, 1 ); // no group
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;  // NOT USED
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;  // NOT USED
  int section = readInt( fp );
  fprintf( stderr, "#:   X %.2f Y %.2f Level %02x Scrap %d X-section %d", x, y, lvl, scrap, section );
  if ( section >= 0 ) { // PLOT_NULL == -1
    float a = readFloat( fp ); // azimuth
    float c = readFloat( fp ); // clino
    fprintf(stderr, "#:  (azimuth %.1f clino %.1f)", a, c );
  }
  fprintf(stderr, "#: \n");
  if ( fp2 == NULL ) {
    // fprintf( fp2, "point %.2f %.2f -type station -name %s\n", x, y, name );
    if ( posStations + 100 > memStations ) {
      memStations += 1000;
      STATIONS = realloc( STATIONS, memStations );
    }
    assert( posStations == strlen(STATIONS) );
    sprintf( STATIONS + posStations,  "point %.2f %.2f -type station -name %s\n", x, y, name );
    posStations = strlen( STATIONS );
  }
  free( name );
}

void readUserStation( FILE * fp, FILE * fp2 ) // user station has no group
{
  long pos = ftell( fp );
  float x = readFloat( fp );   // position
  float y = readFloat( fp );
  int s = readInt( fp );       // scale
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = (VERSION >= 401160 )? readInt( fp ) : 0;
  fprintf(stderr, "#: %ld= USER-STATION:\n", pos);
  char * name = readString( "  Name ", fp, 0 );            // name
  // if ( VERSION >= 401147 ) readString( "  Group ", fp, 1 ); // no group
  fprintf(stderr, "#:   Scale %d X %.2f Y %.2f Level %02x Scrap %d\n", s, x, y, lvl, scrap );
  if ( fp2 ) fprintf( fp2, "point %.2f %.2f -type station -name %s\n", x, y, name );
  free( name );
}

void readSpecial( FILE * fp, FILE * fp2 ) // special path has no group // NOT USED IN THERION
{
  long pos = ftell( fp );
  int s   = readInt( fp );   // type
  float x = readFloat( fp ); // center X
  float y = readFloat( fp ); // center Y
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = (VERSION >= 401160 )? readInt( fp ) : 0;
  // if ( VERSION >= 401147 ) readString( "  Group ", fp, 1 ); // no group
  fprintf(stderr, "#: %ld= SPECIAL: Type %d X %.2f Y %.2f Level %02x Scrap %d\n", pos, s, x, y, lvl, scrap );
}

void readFixedPoint( FILE * fp, FILE * fp2 ) // special path has no group // NOT USED IN THERION
{
  long pos = ftell( fp );
  float x = readFloat( fp ); // center X
  float y = readFloat( fp ); // center Y
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = (VERSION >= 401160 )? readInt( fp ) : 0;
  fprintf(stderr, "#: %ld= FIXED: %.2f Y %.2f Level %02x Scrap %d\n", pos, x, y, lvl, scrap );
}

void readSpecialPoint( FILE * fp, const char * type, int what, FILE * fp2 )  // audio - photo // NOT USED IN THERION
{
  long pos = ftell( fp );
  float x = readFloat( fp );
  float y = readFloat( fp );
  // if ( VERSION >= 401147 ) readString( "  Group ", fp, 1 ); // no group
  float o = ( VERSION > 207043 )? o = readFloat( fp ) : 0; // orientation 
  int s = readInt( fp ); // scale
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;
  fprintf(stderr, "#: %ld= SPECIAL POINT: Type %s Level %02x Scrap %d: ", pos, type, lvl, scrap );
  char * text = readString( "  Text ", fp, 0 ); // point text
  char * options = readString( "  Options ", fp, 0 ); // options
  int id = readInt( fp );
  fprintf(stderr, "#:   Orientation %.2f Scale %d X %.2f Y %.2f ID %d\n", o, s, x, y, id );
  if ( what == 1 ) { // photo
    if ( VERSION >= 602067 ) {
      readString( "  Code ", fp, 1 ); // geocode
      int with_picture = readInt( fp );
      if ( with_picture == 1 ) {
        x = readFloat( fp );
        y = readFloat( fp );
        float z = readFloat( fp );
        fprintf(stderr, "#: With picture %.2f %.2f size %.2f \n", x, y, z );
      } else {
        fprintf(stderr, "#: Without picture \n");
      }
    }
  }
  if ( options != NULL ) free( options );
  if ( text != NULL ) free( text );
}

int readScrapIndex( FILE * fp, FILE * fp2 ) 
{
  int index = readInt( fp );
  fprintf(stderr, "#: Scrap Index %d\n", index );
  return index;
}


int processFile( char * filename, FILE * fp2 )
{
  long pos = 0;
  FILE * fp;
  int done = 0;
  char ch;
  char str[128];
  int  i, j;
  float f;
  int scrap_index = -1;

  fp = fopen( filename, "r" );
  if ( fp == NULL ) {
    fprintf(stderr, "#: Cannot open file \"%s\"\n", filename );
    return 0;
  }

  pos = ftell( fp );
  while ( done == 0 && fread( &ch, sc, 1, fp ) != 0 ) {
    fprintf(stderr, "#: Section %d <%c>\n", pos, ch);
    // { char cc; scanf("%c\n", &cc); }
    switch ( ch ) {
      case 'A': // area
        readArea( fp, fp2 );
        break;
      case 'D': // plot data
        readPlotData( fp, fp2 );
        break;
      case 'E':
        fprintf(stderr, "#: %ld= E-CHAR %02x <%c>\n", pos, ch, ch );
        // done = 1;
        break;
      case 'F':
        fprintf(stderr, "#: %ld= F-CHAR %02x <%c>\n", pos, ch, ch );
        break;
      case 'G':
        readFixedPoint( fp, fp2 );
        break;
      case 'I':
        readBBox( fp, fp2 );
        break;
      case 'J': // special
	readSpecial( fp, fp2 );
        break;
      case 'L': // line
        readLine( fp, fp2 );
        break;
      case 'N': // scrap index
        scrap_index = readScrapIndex( fp, fp2 );
        break;
      case 'P': // point
        readPoint( fp, fp2 );
        break;
      case 'S':
        if ( scrap_index != -1 ) {
          if ( fp2 ) {
            fprintf( fp2, "endscrap\n");
            if ( STATIONS != NULL ) fprintf( fp2, "%s", STATIONS );
          }
          scrap_index = -1;
        }
        readScrap( fp, fp2 );
        break;
      case 'T': // text label
        readLabel( fp, fp2 );
        break;
      case 'U': // user station
        readUserStation( fp, fp2 );
        break;
      case 'V':
        readVersion( fp, fp2 );
        break;
      case 'X': // station name
        readAutoStation( fp, fp2 );
        break;
      case 'Y': // photo
	readSpecialPoint( fp, "Photo", 1, fp2 );
	break;
      case 'Z': // audio
	readSpecialPoint( fp, "Audio", 2, fp2 );
	break;
      default:
        fprintf(stderr, "#: %ld= Unexpected char %02x <%c>\n", pos, ch, ch );
        return 0;
        break;
    }
    pos = ftell( fp );
  }
  fclose( fp );
  if ( scrap_index != -1 ) {
    if ( fp2 ) {
      if ( STATIONS != NULL ) fprintf( fp2, "%s", STATIONS );
      fprintf( fp2, "endscrap\n"); 
    }
  }
  return 1;
}


int main( int argc, char ** argv )
{
  if ( argc <= 1 ) {
    fprintf(stdout, "Usage: %s filename\n", argv[0] );
    return 0;
  }
  for ( int arg = 1; arg < argc; ++arg ) {
    int len = strlen( argv[arg] );
    char * output = (char *)malloc( len + 1 );
    strcpy( output, argv[arg] );
    output[ len ] = 0;
    output[ len-1 ] = '2';
    output[ len-2 ] = 'h';
    fprintf( stderr, "# FILE %s -> %s\n",  argv[arg], output );
    FILE * fp2 = fopen( output, "w" );

    posStations = 0;
    STATIONS = malloc( memStations );
    STATIONS[ posStations ] = 0;
    processFile( argv[arg], NULL );
    assert( posStations == strlen(STATIONS) );
    processFile( argv[arg], fp2 );
    free( STATIONS );
    STATIONS = NULL;
    fclose( fp2 );
  }
  return 0;
}

