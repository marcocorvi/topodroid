ANT    = /home/programs/apache-ant-1.8.2/bin/ant
CC     = gcc
CPLUS  = g++
AR     = ar
CFLAGS = -g -O0 -Wall -DMAIN

# verbose flag for ANT
AFLAGS = -v 

VERSION = `grep versionName AndroidManifest.xml | sed -e 's/ *android:versionName=//' | sed -e 's/"//g' `

APPNAME = DistoX
LOGNAME = $(APPNAME)
PACKAGE = com.topodroid.$(APPNAME)

default:
	$(ANT) debug

release:
	$(ANT) release
	mv bin/TopoDroid-release.apk TopoDroid-$(VERSION).apk
	ls -l TopoDroid-$(VERSION).apk
	md5sum TopoDroid-$(VERSION).apk

signed:
	$(ANT) release
	./howto/sign.sh
	mv TopoDroid-release-keysigned.apk TopoDroid-$(VERSION)-gp.apk
	ls -l TopoDroid-$(VERSION)-gp.apk
	md5sum TopoDroid-$(VERSION)-gp.apk

debug-signed:
	$(ANT) debug
	./howto/sign-debug.sh
	mv TopoDroid-debug-keysigned.apk TopoDroid-$(VERSION)-debug.apk

bundle:
	$(ANT) release
	./bundle.sh

install:
	adb install -r bin/TopoDroid-debug.apk

uninstall:
	adb uninstall $(PACKAGE)

reinstall:
	adb uninstall $(PACKAGE)
	adb install -r bin/TopoDroid-debug.apk

rebuild:
	$(ANT) clean
	$(ANT) debug

less:
	$(ANT) debug 2>&1 | less

pdf:
	./howto/pdf.sh

manual:
	./howto/pdf.sh

clean:
	$(ANT) clean

bclean:
	rm -rf bbin

symbols:
	./howto/symbols.sh

log:
	adb logcat | grep $(LOGNAME)

git-add:
	yes | git add -p

git-pull:
	git pull

SRC = \
  ./AndroidManifest.xml \
  ./ant/* \
  ./build.xml \
  ./bundle.sh \
  ./LICENSE \
  ./COPYING \
  ./Makefile \
  ./proguard.cfg \
  ./project.properties \
  ./README.md \
  ./regression-test.txt \
  ./assets/*/* \
  ./firmware/* \
  ./int18/*/* \
  ./res/values/* \
  ./res/values-normal/* \
  ./res/values-small/* \
  ./res/values-large/* \
  ./res/layout/* \
  ./res/drawable/* \
  ./res/mipmap-*/* \
  ./res/raw/* \
  ./res/xml/* \
  ./symbols-git/*/*/* \
  ./howto/* \
  ./utils/* \
  ./unused/idea/* \
  ./unused/TopoDroid-icon/* \
  ./src/com/topodroid/c3db/*.java \
  ./src/com/topodroid/c3in/*.java \
  ./src/com/topodroid/c3out/*.java \
  ./src/com/topodroid/c3walls/*/*.java \
  ./src/com/topodroid/common/*.java \
  ./src/com/topodroid/calib/*.java \
  ./src/com/topodroid/dev/*.java \
  ./src/com/topodroid/dev/*/*.java \
  ./src/com/topodroid/$(APPNAME)/*.java \
  ./src/com/topodroid/dln/*.java \
  ./src/com/topodroid/help/*.java \
  ./src/com/topodroid/inport/*.java \
  ./src/com/topodroid/io/*/*.java \
  ./src/com/topodroid/mag/*.java \
  ./src/com/topodroid/math/*.java \
  ./src/com/topodroid/num/*.java \
  ./src/com/topodroid/packetX/*.java \
  ./src/com/topodroid/prefs/*.java \
  ./src/com/topodroid/ptopo/*.java \
  ./src/com/topodroid/tdm/*.java \
  ./src/com/topodroid/trb/*.java \
  ./src/com/topodroid/ui/*.java \
  ./src/com/topodroid/utils/*.java

EXTRA_SRC = \
  ./studio/* \
  ./save/* 

version:
	echo $(VERSION)

archive:
	tar -chvzf ../topodroid.tgz --exclude-vcs $(SRC)

