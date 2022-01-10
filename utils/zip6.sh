#!/bin/sh
#
# Create zip archive of a TopoDroid v. 6 survey.
#
# Usage:
#   [1] copy this script in the TopoDroid folder
#   [2] copy device10.sqlite in the TopoDroid folder
#   [3] execute this script with argument the survey name
# 
# If run without argument the script list the available surveys.
#
# Requirements: sqlite3, zip, date, sed
#
SQLITE=`which sqlite3`
ZIP=`which zip`
DATE=`which date`
if [ -z $SQLITE ] ; then
    echo "Missing sqlite3"
    exit 1
fi
if [ -z $ZIP ] ; then
    echo "Missing zip"
    exit 1
fi
if [ -z $DATE ] ; then
    echo "Missing date"
    exit 1
fi
echo "Found $SQLITE"
echo "      $ZIP"
echo "      $DATE"

td_version="6.0.37"
db_version="44"

distox14="distox14.sqlite"
device10="device10.sqlite"
if [ ! -f $distox14 ] ; then
     echo "TopoDroid database not found"
     exit 2
else 
     db_version=`echo "PRAMGA user_version;" | $SQLITE $distox14`
fi
if [ ! -f $device10 ] ; then
     td_version=`echo "select value from configs where key=\"version\";" | $SQLITE $device10`
fi
td_version_code=`echo $td_version | sed -e s'/\./0/g'`

if [ $# -eq 0 ] ; then
    echo "select name from surveys;" > __input
    echo "Usage: zip.sh <survey_name>"
    echo "Available surveys:"
    $SQLITE $distox14 < __input
    rm -rf __input
else
    rm -rf __tmp
    mkdir __tmp
    name=$1
    echo "$td_version $td_version_code" > __tmp/manifest
    echo "$db_version" >> __tmp/manifest
    echo "$name" >> __tmp/manifest
    $DATE +%Y-%m-%d >> __tmp/manifest

cat > $name.sqlite << x123end
.output __tmp/survey.sqlite
.mode insert surveys
select surveys.* from surveys where surveys.name="$name";
.mode insert plots
select plots.* from plots,surveys where surveys.name="$name" and surveys.id == plots.surveyId;
.mode insert shots
select shots.* from shots,surveys where surveys.name="$name" and surveys.id == shots.surveyId;
.mode insert audios
select audios.* from audios,surveys where surveys.name="$name" and surveys.id == audios.surveyId;
.mode insert photos
select photos.* from photos,surveys where surveys.name="$name" and surveys.id == photos.surveyId;
.mode insert fixeds
select fixeds.* from fixeds,surveys where surveys.name="$name" and surveys.id == fixeds.surveyId;
.mode insert stations
select stations.* from stations,surveys where surveys.name="$name" and surveys.id == stations.surveyId;
.mode insert sensors
select sensors.* from sensors,surveys where surveys.name=$name and surveys.id == sensors.surveyId;
x123end

    $SQLITE $distox14 < $name.sqlite
    rm -f $name.sqlite

tdr="$name/tdr"
audio="$name/audio"
photo="$name/photo"
note="$name/note/$name.txt"

    if [ -e $tdr && -z "$(ls -A $tdr/*.tdr)" ] ; then
        cp -l $tdr/$name-*.tdr __tmp
    fi
    if [ -e $audio && -z "$(ls -A $audio/*.wav)" ] ; then
        cp -l $audio/*.wav __tmp 
    fi
    if [ -e $photo && -z "$(ls -A $photo/*.wav)" ] ; then
        cp -l $photo/*.jpg __tmp
    fi
    if [ -e $note ] ; then
        cp -l $note __tmp
    fi

    cd __tmp
    $ZIP ../$name.zip ./*
    cd -
    rm -rf __tmp
fi

