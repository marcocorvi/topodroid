#!/bin/sh
#
# Create zip archive of a TopoDroid v. 5 survey.
#
#Andoid version
# 
# Install Termux from GP
# change Termux storage permission
# Termux:
# $pkg install sqlite
# $pkg install zip
# $cd /storage/emulated/0
# go to TopoDroid directory: $cd TopoDroid or $cd Documents/TopoDroid
#
# Usage:
#   [1] copy this script in the TopoDroid folder
#   [2] execute this script with argument the survey name ($bash zip.sh survey_name)
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
    rm -rf tmp1
    mkdir tmp1
    name=$1
    touch tmp1/manifest
    echo "5.1.40 501040" >> tmp1/manifest
    echo "42" >> tmp1/manifest
    echo "$name" >> tmp1/manifest
    /bin/date +%Y-%m-%d >> tmp1/manifest

cat > $name.sqlite << x123end
.output tmp1/survey.sql
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
select sensors.* from sensors,surveys where surveys.name="$name" and surveys.id == sensors.surveyId;
x123end

    /usr/bin/sqlite3 distox14.sqlite < $name.sqlite
    rm -f $name.sqlite
    cp tdr/$name-*.tdr tmp1
    if [ -e audio/$name ]
    then
        cp audio/$name/*.wav tmp1 
    fi
    if [ -e photo/$name ]
    then
        cp photo/$name/*.jpg tmp1
    fi
    if [ -e photo/$name.txt ]
    then
        cp text/$name.txt tmp1
    fi

while read a; do
    echo ${a//\'/\"}
done < tmp1/survey.sql > tmp1/survey.sql.t
mv tmp1/survey.sql{.t,}

    cd tmp1
    /usr/bin/zip ../$name.zip ./*
    cd -
    rm -rf tmp1
fi

