#!/bin/sh
#
# Create zip archive of a TopoDroid v. 5 survey.
#
# Usage:
#   [1] copy this script in the TopoDroid folder
#   [2] execute this script with argument the survey name
# 
# If run without argument the script list the available surveys.
#
if [ $# -eq 0 ]
then
    echo "select name from surveys;" > __input
    echo "Usage: zip.sh <survey_name>"
    echo "Available surveys:"
    /usr/bin/sqlite3 distox14.sqlite < __input
    rm -rf __input
else
    rm -rf tmp
    mkdir tmp
    name=$1
    touch tmp/manifest
    echo "5.1.40 500140" >> tmp/manifest
    echo "42" >> tmp/manifest
    echo "$name" >> tmp/manifest
    /bin/date +%Y-%m-%d >> tmp/manifest

cat > $name.sqlite << x123end
.output tmp/survey.sqlite
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

    /usr/bin/sqlite3 distox14.sqlite < $name.sqlite
    rm -f $name.sqlite
    cp -l tdr/$name-*.tdr tmp
    if [ -e audio/$name ]
    then
        cp -l audio/$name/*.wav tmp 
    fi
    if [ -e photo/$name ]
    then
        cp -l photo/$name/*.jpg tmp
    fi
    if [ -e photo/$name.txt ]
    then
        cp -l text/$name.txt tmp
    fi

    cd tmp
    /usr/bin/zip ../$name.zip ./*
    cd -
    rm -rf tmp
fi

