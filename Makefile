VERSION = `grep versionName AndroidManifest.xml | sed -e 's/ *android:versionName=//' | sed -e 's/"//g' `

default:
	ant debug

release:
	ant release
	mv bin/TopoDroid-release.apk TopoDroid-$(VERSION).apk
	ls -l TopoDroid-$(VERSION).apk
	md5sum TopoDroid-$(VERSION).apk

signed:
	ant release
	./howto/sign.sh

install:
	adb install -r bin/TopoDroid-debug.apk

uninstall:
	adb uninstall com.topodroid.DistoX

reinstall:
	adb uninstall com.topodroid.DistoX
	adb install -r bin/TopoDroid-debug.apk

rebuild:
	ant clean
	ant debug

less:
	ant debug 2>&1 | less

pdf:
	./howto/pdf.sh

manual:
	./howto/pdf.sh

clean:
	ant clean

symbols:
	./howto/symbols.sh

log:
	adb logcat | grep DistoX

git-add:
	yes | git add -p

git-pull:
	git pull

SRC = \
  ./AndroidManifest.xml \
  ./ant/* \
  ./studio/* \
  ./build.xml \
  ./LICENSE \
  ./COPYING \
  ./Makefile \
  ./proguard.cfg \
  ./project.properties \
  ./README.md \
  ./regression-test.txt \
  ./howto/* \
  ./assets/*/* \
  ./firmware/* \
  ./int18/*/* \
  ./res/values/* \
  ./res/layout/* \
  ./res/drawable/* \
  ./res/mipmap-*/* \
  ./res/raw/* \
  ./res/values-normal/* \
  ./res/values-small/* \
  ./res/values-large/* \
  ./res/xml/* \
  ./symbols-git/*/*/* \
  ./save/* \
  ./utils/* \
  ./unused/* \
  ./src/com/topodroid/*/* 

version:
	echo $(VERSION)

archive:
	tar -chvzf ../topodroid.tgz --exclude-vcs $(SRC)

