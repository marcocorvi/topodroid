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
cd /home/programs/android-sdk/samples/android-8/TD
rm libd8 \
   build.xml \
   src/com/topodroid/help/AIhelper.java \
   src/com/topodroid/help/AIdialog.java \
   src/com/topodroid/help/HelpAIdialog.java \
   src/com/topodroid/prefs/GeminiDialog.java \
   src/com/topodroid/prefs/PrefAIdialog.java \
   res/layout/user_manual_activity.xml
rm -f assets/ai/dict.txt \
      assets/ai/names.txt \
      assets/ai/setting.txt 

if [ "$1" = "yes" ]; then
  echo "turning AI support ON"
  ln -s libd8ai libd8
  ln -s build-ai.xml build.xml
  cp ./AIhelp/ai/AIhelper.java src/com/topodroid/help/
  cp ./AIhelp/ai/AIdialog.java src/com/topodroid/help/
  cp ./AIhelp/ai/HelpAIdialog.java src/com/topodroid/help/
  cp ./AIhelp/ai/GeminiDialog.java src/com/topodroid/prefs/
  cp ./AIhelp/ai/PrefAIdialog.java src/com/topodroid/prefs/
  cp ./AIhelp/ai/user_manual_activity.xml res/layout/
  cp ./AIhelp/ai/dict.txt assets/ai/
  cp ./AIhelp/ai/names.txt assets/ai/
  cp ./AIhelp/ai/settings.txt assets/ai/
  make clean
elif [ "$1" = "no" ]; then
  echo "turning AI support OFF"
  ln -s libd8noai libd8
  ln -s build-noai.xml build.xml
  cp ./AIhelp/no/AIhelper.java src/com/topodroid/help/
  cp ./AIhelp/no/AIdialog.java src/com/topodroid/help/
  cp ./AIhelp/no/HelpAIdialog.java src/com/topodroid/help/
  cp ./AIhelp/no/GeminiDialog.java src/com/topodroid/prefs/
  cp ./AIhelp/no/PrefAIdialog.java src/com/topodroid/prefs/
  cp ./AIhelp/no/user_manual_activity.xml res/layout/
  make clean
else
  echo "Usage $0 [yes|no]"
fi
# echo "Remember to do a \"make clean\""
