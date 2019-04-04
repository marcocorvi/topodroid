#include <stdio.h>
#include <stdlib.h>


int main( int argc, char ** argv ) 
{
  FILE * fp;
  unsigned int check, k, len=0;

  if ( argc <= 1 ) {
    printf("Usage: %s <firmware_file>\n", argv[0] );
    return 0;
  }

  fp = fopen( argv[1], "r" );
  if ( fp == NULL ) {
    printf("cannot open firmware_file %s\n", argv[1] );
    return 0;
  }

  check = 0;
  while ( fread( &k, sizeof(int), 1, fp ) > 0 ) {
    ++len;
    check ^= k;
  }
  fclose( fp );
  printf("Length %d checksum 0x%02x%02x%02x%02x (use as this is Android code)\n", len*4, 
    (check & 0xff), ((check>>8) & 0xff), ((check>>16) & 0xff), ((check>>24) & 0xff) );

  return 0;
}
