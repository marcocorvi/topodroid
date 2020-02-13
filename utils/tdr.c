/**
* --------------------------------------------------------
*  Copyright This software is distributed under GPL-3.0 or later
*  See the file COPYING.
* --------------------------------------------------------
*
*/
#include <stdio.h>
#include <stdlib.h>
#include <arpa/inet.h>

int sc = sizeof( char );
int si = sizeof( int );
int sf = sizeof( float );
uint32_t s32 = sizeof( uint32_t );

int VERSION = 0;

int toLE( int * pi )
{
  unsigned char * ch = (unsigned char*)pi;
  return (int)( ( ((int)ch[0])<<24 ) | ( ((int)ch[1]) << 16 ) | ( ((int)ch[2]) << 8 ) | ( ((int)ch[3]) ) );
}

void readString( char * hdr, FILE * fp )
{
  // long pos = ftell( fp );
  char ch2[2];
  int j, len, c0, c1;
  char ch;
  fread( ch2, sc, 2, fp ); // skip 2 bytes
  c0 = ch2[0]; if ( c0 < 0 ) c0 += 256;
  c1 = ch2[1]; if ( c1 < 0 ) c1 += 256;
  len = ( c0 << 8 ) | c1;
  printf("%s [length %d] <", hdr, len);
  for ( j=0; j<len; ++j ) {
    fread( &ch, sc, 1, fp );
    printf("%c", ch, ch );
  }
  printf(">\n");
}

void read4ch( FILE * fp, int skip )
{
  char ch;
  int j;
  long pos = ftell( fp );
  printf("%ld= [skip %d]", pos, skip);
  for ( j=0; j<skip; ++j ) {
    fread( &ch, sc, 1, fp );
    printf(" %02x ", ch );
  }
  printf("\n");
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

void readVersion( FILE * fp )
{
  long pos = ftell( fp );
  int v = readInt( fp );
  VERSION = v;
  printf("%ld= VERSION: %d\n", pos, v );
}

void readScrap( FILE * fp )
{
  long pos = ftell( fp );
  int type, j, k;
  printf("%ld= SCRAP\n", pos);
  readString( "  Name ", fp );
  type = readInt( fp );
  printf("  Type %d \n", type );
  for ( int k=0; k<3; ++k ) {
    // j = readInt( fp );
    // printf("%d ", j );
    readString( "", fp );
    // printf("\n");
    // read4ch( fp, 2 ); printf("\n");
  }
}

void readBBox( FILE * fp )
{
  long pos = ftell( fp );
  float x1 = readFloat( fp );
  float y1 = readFloat( fp );
  float x2 = readFloat( fp );
  float y2 = readFloat( fp );
  int north = readInt( fp );
  printf("%ld= BBOX %.2f %.2f - %.2f %.2f (north %d)\n", pos, x1, y1, x2, y2, north );
  if ( north == 1 ) {
    x1 = readFloat( fp );
    y1 = readFloat( fp );
    x2 = readFloat( fp );
    y2 = readFloat( fp );
    printf("  North %.2f %.2f - %.2f %.2f\n", x1, y1, x2, y2 );
  }
}

void readPoint( FILE * fp )
{
  long pos = ftell( fp );
  float cx = readFloat( fp );
  float cy = readFloat( fp ); 
  printf("%ld= POINT: X %.3f Y %.3f\n", pos, cx, cy );
  readString( "  Type ", fp );
  if ( VERSION >= 401147 ) readString( "  Group ", fp );
  float orient = readFloat( fp );
  int scale  = readInt( fp );
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;
  printf("  Orientation %.2f Level %02x Scale %d Scrap %d\n", orient, lvl, scale, scrap );
  readString( "  Text ", fp );  // text
  readString( "  Options ", fp );  // options
}
  
void readLabel( FILE * fp )
{
  long pos = ftell( fp );
  float cx = readFloat( fp );
  float cy = readFloat( fp ); 
  // if ( VERSION >= 401147 ) readString( "  Group ", fp ); // label has null group 
  float azi   = readFloat( fp ); // orientation
  int scale = readInt( fp );   
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;
  printf("%ld= LABEL: X %.3f Y %.3f Orientattion %.1f Scale %d Level %02x Scrap %d\n", pos, cx, cy, azi, scale, lvl, scrap );
  readString( "  Text ", fp );      // text
  readString( "  Options ", fp );      // options
}

void readLinePoint( FILE * fp )
{
  long pos = ftell( fp );
  char ch;
  float x = readFloat( fp );
  float y = readFloat( fp );
  fread( &ch, sc, 1, fp );
  // int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff; N.B. Line Points do not have level
  printf("  %ld= %.2f %.2f %d ", pos, x, y, ch );
  if ( ch == 1 ) {
    x = readFloat( fp );
    y = readFloat( fp );
    printf(" %.2f %.2f", x, y );
    x = readFloat( fp );
    y = readFloat( fp );
    printf(" %.2f %.2f", x, y );
  }
  printf("\n");
}

void readLine( FILE * fp )
{
  long pos = ftell( fp );
  int k , np;
  char closed, reversed;
  printf("%ld= LINE:\n", pos);
  readString( "  Type ", fp );                // name
  if ( VERSION >= 401147 ) readString( "  Group ", fp );
  fread( &closed, sc, 1, fp );
  fread( &reversed, sc, 1, fp );
  int outline = readInt( fp );
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;
  printf("  Closed %d Rev. %d Outline %d Level %02x Scrap %d\n", closed, reversed, outline, lvl, scrap );
  readString( "  Options ", fp );     // options
  np = readInt( fp );   // nr. points
  printf("  Nr. Points %d\n", np );
  for ( int k=0; k<np; ++k ) readLinePoint( fp );
}

void readArea( FILE * fp )
{
  long pos = ftell( fp );
  char ch;
  printf("%ld= AREA: ", pos);
  readString( "  Type ", fp );         // name
  if ( VERSION >= 401147 ) readString( "  Group ", fp );
  readString( "  Prefix ", fp );         // prefix
  int cnt = readInt( fp );      // counter
  fread( &ch, sc, 1, fp );  // visibility
  float orient = readFloat( fp );
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;
  int np = readInt( fp );       // nr points
  printf("  Counter %d Visibility %d Orientation %.2f Level %02x Scrap %d Nr.Points %d\n", cnt, ch, orient, lvl, scrap, np );
  for ( int k=0; k<np; ++k ) readLinePoint( fp );
}

void readAutoStation( FILE * fp )
{
  // level and scrap do not make much sense for auto-stations
  long pos = ftell( fp );
  float x = readFloat( fp ); // position
  float y = readFloat( fp );
  printf("%ld= STATION:\n", pos);
  readString( "  Name ", fp );          // name
  // if ( VERSION >= 401147 ) readString( "  Group ", fp ); // no group
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;
  int section = readInt( fp );
  printf("  X %.2f Y %.2f Level %02x Scrap %d X-section %d", x, y, lvl, scrap, section );
  if ( section >= 0 ) { // PLOT_NULL == -1
    float a = readFloat( fp ); // azimuth
    float c = readFloat( fp ); // clino
    printf(" (azimuth %.1f clino %.1f)", a, c );
  }
  printf("\n");
}

void readUserStation( FILE * fp ) // user station has no group
{
  long pos = ftell( fp );
  float x = readFloat( fp );   // position
  float y = readFloat( fp );
  int s = readInt( fp );       // scale
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = (VERSION >= 401160 )? readInt( fp ) : 0;
  printf("%ld= USER-STTAION:\n", pos);
  readString( "  Name ", fp );            // name
  // if ( VERSION >= 401147 ) readString( "  Group ", fp ); // no group
  printf("  Scale %d X %.2f Y %.2f Level %02x Scrap %d\n", s, x, y, lvl, scrap );
}

void readSpecial( FILE * fp ) // special path has no group
{
  long pos = ftell( fp );
  int s   = readInt( fp );   // type
  float x = readFloat( fp ); // center X
  float y = readFloat( fp ); // center Y
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = (VERSION >= 401160 )? readInt( fp ) : 0;
  // if ( VERSION >= 401147 ) readString( "  Group ", fp ); // no group
  printf("%ld= SPECIAL: Type %d X %.2f Y %.2f Level %02x Scrap %d\n", pos, s, x, y, lvl, scrap );
}

void readFixedPoint( FILE * fp ) // special path has no group
{
  long pos = ftell( fp );
  float x = readFloat( fp ); // center X
  float y = readFloat( fp ); // center Y
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = (VERSION >= 401160 )? readInt( fp ) : 0;
  printf("%ld= FIXED: %.2f Y %.2f Level %02x Scrap %d\n", pos, x, y, lvl, scrap );
}

void readSpecialPoint( FILE * fp, const char * type )  // audio - photo
{
  long pos = ftell( fp );
  float x = readFloat( fp );
  float y = readFloat( fp );
  // if ( VERSION >= 401147 ) readString( "  Group ", fp ); // no group
  float o = ( VERSION > 207043 )? o = readFloat( fp ) : 0; // orientation 
  int s = readInt( fp ); // scale
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  int scrap = ( VERSION >= 401160 )? readInt( fp ) : 0;
  printf("%ld= SPECIAL POINT: Type %s Level %02x Scrap %d: ", pos, type, lvl, scrap );
  readString( "  Text ", fp ); // point text
  readString( "  Options ", fp ); // options
  int id = readInt( fp );
  printf("  Orientation %.2f Scale %d X %.2f Y %.2f ID %d\n", o, s, x, y, id );
}

void readScrapIndex( FILE * fp ) 
{
  int index = readInt( fp );
  printf("Scrap Index %d\n", index );
}


int main( int argc, char ** argv )
{
  long pos = 0;
  FILE * fp;
  int done = 0;
  char ch;
  char str[128];
  int  i, j;
  float f;

  if ( argc <= 1 ) {
    printf("Usage: %s filename\n", argv[0] );
    return 0;
  }

  fp = fopen( argv[1], "r" );
  if ( fp == NULL ) {
    printf("Cannot open file \"%s\"\n", argv[1] );
    return 0;
  }

  while ( done == 0 && fread( &ch, sc, 1, fp ) != 0 ) {
    // printf("Section <%c>\n", ch);
    // { char cc; scanf("%c\n", &cc); }
    switch ( ch ) {
      case 'V':
        readVersion( fp );
        break;
      case 'S':
        readScrap( fp );
        break;
      case 'I':
        readBBox( fp );
        break;
      case 'N': // scrap index
        readScrapIndex( fp );
        break;
      case 'P': // point
        readPoint( fp );
        break;
      case 'T': // text label
        readLabel( fp );
        break;
      case 'L': // line
        readLine( fp );
        break;
      case 'A': // area
        readArea( fp );
        break;
      case 'U': // user station
        readUserStation( fp );
        break;
      case 'X': // station name
        readAutoStation( fp );
        break;
      case 'G':
        readFixedPoint( fp );
        break;
      case 'J': // special
	readSpecial( fp );
        break;
      case 'Y': // photo
	readSpecialPoint( fp, "Photo" );
	break;
      case 'Z': // audio
	readSpecialPoint( fp, "Audio" );
	break;
      case 'F':
        printf("%ld= F-CHAR %02x <%c>\n", pos, ch, ch );
        break;
      default:
      case 'E':
        printf("%ld= E-CHAR %02x <%c>\n", pos, ch, ch );
        // done = 1;
        break;
    }
    pos = ftell( fp );
  }
  fclose( fp );
  return 0;
}


