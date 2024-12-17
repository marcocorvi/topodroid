#!/usr/bin/perl
use strict;
use warnings;
use File::Basename;

# Check if the script to call is provided as a command line parameter
if (@ARGV != 1) {
  die "Usage: $0 <script_to_call>\n";
}

# Get the script to call from the command line parameter
my $script_to_call = $ARGV[0];
my $original_en_file = '../res/values/strings.xml';

# Directory containing the subdirectories
my $res_dir = '../res';

# Open the directory
opendir(my $dh, $res_dir) or die "Cannot open directory $res_dir: $!";

# Iterate over the subdirectories
while (my $subdir = readdir($dh)) {
  # Check if the subdirectory name matches the pattern "values-XX"
  if ($subdir =~ /^values-([a-z]{2})$/) {
    my $lang_code = $1;
    print "Found language code: $lang_code\n";

    # Call the specified script using the language code as parameter
    system("perl", $script_to_call, $original_en_file, $lang_code) == 0
      or warn "Failed to call $script_to_call with parameters '$original_en_file' and '$lang_code': $!";
  }
}

# Close the directory
closedir($dh);
