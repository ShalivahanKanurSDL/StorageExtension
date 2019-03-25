@echo off
rem Install Tridion Content Delivery libraries and necessary third-party libraries in the local Maven repository

echo Installing Tridion libraries into the local Maven repository...

call mvn install:install-file -DgroupId=com.tridion -DartifactId=cd_model -Dversion=8.5.0 -Dpackaging=jar -Dfile=cd_model-8.5.0-1013.jar
call mvn install:install-file -DgroupId=com.tridion -DartifactId=cd_datalayer -Dversion=8.5.0 -Dpackaging=jar -Dfile=cd_datalayer-8.5.0-1014.jar
call mvn install:install-file -DgroupId=com.tridion -DartifactId=cd_core -Dversion=8.5.0 -Dpackaging=jar -Dfile=cd_core-8.5.0-1011.jar
call mvn install:install-file -DgroupId=com.tridion -DartifactId=cd_common_util -Dversion=8.5.0 -Dpackaging=jar -Dfile=cd_common_util-8.5.0-1009.jar
call mvn install:install-file -DgroupId=com.tridion -DartifactId=cd_common_config_legacy -Dversion=8.5.0 -Dpackaging=jar -Dfile=cd_common_config_legacy-8.5.0-1009.jar

echo Finished
pause