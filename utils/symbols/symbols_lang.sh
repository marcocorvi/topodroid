#!/bin/sh
#
# symbols_lang produces symbols string file with possibly repeated keys
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
lang=$1

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
echo "<resources>"
  ./do_symbols.sh $lang | sort | uniq
echo "</resources>"
