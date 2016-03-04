#!/bin/bash
# Install SDL SmartTarget libraries and necessary third-party libraries in the local Maven repository

echo "Installing SDL SmartTarget libraries into the local Maven repository..."

mvn -q install:install-file -DgroupId=com.tridion -DartifactId=smarttarget_core -Dversion=2.1.0 -Dpackaging=jar -Dfile=smarttarget_core.jar
mvn -q install:install-file -DgroupId=com.tridion -DartifactId=smarttarget_entitymodel -Dversion=2.1.0 -Dpackaging=jar -Dfile=smarttarget_entitymodel.jar
mvn -q install:install-file -DgroupId=com.tridion -DartifactId=smarttarget_cartridge -Dversion=2.1.0 -Dpackaging=jar -Dfile=smarttarget_cartridge.jar
mvn -q install:install-file -DgroupId=com.tridion -DartifactId=smarttarget_odata_cartridge -Dversion=2.1.0 -Dpackaging=jar -Dfile=smarttarget_odata_cartridge.jar
mvn -q install:install-file -DgroupId=com.tridion -DartifactId=smarttarget_api_webservice -Dversion=2.1.0 -Dpackaging=jar -Dfile=smarttarget_api_webservice.jar
mvn -q install:install-file -DgroupId=com.tridion -DartifactId=session_cartridge -Dversion=2.1.0 -Dpackaging=jar -Dfile=session_cartridge.jar

echo "Finished"