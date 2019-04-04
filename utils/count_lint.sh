#!/bin/sh

echo "count lint warnings for each translation"
for i in bg cn de es fr hu it pl pt ro ru sk ua ; do
	echo -n "$i "
	grep -c \"$i\" lint.out
done

