#!/bin/sh
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#
lang=$1
dirs='symbols_archeo symbols_bio symbols_extra symbols_geo symbols_karst symbols_mine symbols_paleo symbols_speleo';

for d in $dirs ; do 
	j="../symbols-git/$d";
	# echo "$lang $j";
	for k in point line area; do
		for i in $j/$k/* ; do
			./symbols.pl $lang $i 2>/dev/null ;
		done
	done
done
