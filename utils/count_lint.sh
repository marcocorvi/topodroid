#!/bin/sh
#
# --------------------------------------------------------
#  Copyright This software is distributed under GPL-3.0 or later
#  See the file COPYING.
# --------------------------------------------------------
#

echo "count lint warnings for each translation"
for i in bg cn de es fr hu it pl pt ro ru sk ua ; do
	echo -n "$i "
	grep -c \"$i\" lint.out
done

