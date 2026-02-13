#/bin/sh
#
cd /home/programs/android-sdk/samples/android-8/topodroid
if [ "$1" = "yes" ]; then
  echo "turning AI support ON"
  rm libd8
  ln -s libd8ai libd8
else
  echo "turning AI support OFF"
  rm libd8
  ln -s libd8noai libd8
fi
touch libd8/AIhelper.java
