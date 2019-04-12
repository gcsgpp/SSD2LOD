PACKAGE_NAME=lssb-ssd2lod
DEB_BUILD_DIR=target/deb/build/
DEB_DIST_DIR=target/deb/dist/
all: deb 

deb-clean:
	rm -rf ${DEB_BUILD_DIR}* #${PACKAGE_NAME}/usr/local/bin/*
	rm -rf ${DEB_DIST_DIR}*

deb: deb-clean create-jar
	mkdir --parents ${DEB_BUILD_DIR}${PACKAGE_NAME}/usr/local/bin/
	mkdir --parents ${DEB_BUILD_DIR}${PACKAGE_NAME}/usr/share/lssb/
	mkdir --parents ${DEB_DIST_DIR}
	cp target/transformation_software*.jar ${DEB_BUILD_DIR}${PACKAGE_NAME}/usr/share/lssb/ssd2lod.jar
	cp resources/ssd2lod.bat ${DEB_BUILD_DIR}${PACKAGE_NAME}/usr/local/bin/
	cp -R resources/DEBIAN ${DEB_BUILD_DIR}${PACKAGE_NAME}
	cd ${DEB_BUILD_DIR}; find -iname .gitignore -exec rm {} \;
	cd ${DEB_BUILD_DIR}; dpkg-deb --build ${PACKAGE_NAME}
	mv ${DEB_BUILD_DIR}*.deb ${DEB_DIST_DIR}
	cp ${DEB_DIST_DIR}*.deb target/


create-jar:
	mvn -DskipTests package

