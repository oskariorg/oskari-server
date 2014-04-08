The files under this folder are used as additional libs that can't be found in public repositories. The oskari-server/pom.xml defines this folder as an in-project repository so everything should work out of the box. If you have any issues, the files can be installed to local repository with these commands:
-------------------------------------------------------------------------

mvn install:install-file -Dfile=capabilities_1_3_0.jar -DgroupId=fi.mml -DartifactId=capabilities -Dversion=1.3.0 -Dpackaging=jar

mvn install:install-file -Dfile=deegree2.jar -DgroupId=org.deegree -DartifactId=deegree -Dversion=2 -Dpackaging=jar

mvn install:install-file -Dfile=liferay-rhino.jar -DgroupId=com.liferay.rhino -DartifactId=liferay-rhino -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=nameregister.jar -DgroupId=fi.mml -DartifactId=nameregister -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=wms111.jar -DgroupId=fi.mml -DartifactId=wms -Dversion=1.1.1 -Dpackaging=jar