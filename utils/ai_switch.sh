#/bin/sh
#
# The AI dependence is confined in a class, src/com/topodroid/help/AIhelper.java
# and in the libraries, ehich are stored in libd8-ai.
#
# The AIhelper class is a sym-link to either a class with AI-support of a
# class with no AI-support.
#
# There are two build.xml files, one to include the AI libs (build-ai.xml)
# and one without their inclusion (build-noai.xml).
# build.xml is a sym-link to the appropriate file. 
#
cd /home/programs/android-sdk/samples/android-8/topodroid
if [ "$1" = "yes" ]; then
  echo "turning AI support ON"
  rm libd8
  ln -s libd8ai libd8
  rm build.xml
  ln -s build-ai.xml build.xml
  cd src/com/topodroid/help
  rm AIhelper.java
  ln -s ../../../../AIhelp/ai/AIhelper.java .
  touch AIhelper.java
  cd -
elif [ "$1" = "no" ]; then
  echo "turning AI support OFF"
  rm libd8
  ln -s libd8noai libd8
  rm build.xml
  ln -s build-noai.xml build.xml
  cd src/com/topodroid/help
  rm AIhelper.java
  ln -s ../../../../AIhelp/no/AIhelper.java .
  touch AIhelper.java
  cd -
else
  echo "Usage $0 [yes|no]"
fi
