The files under this folder are used as additional libs that can't be found in public repositories. The oskari-server/pom.xml defines this folder as an in-project repository so everything should work out of the box. If you have any issues, the files can be installed to local repository with these commands:
-------------------------------------------------------------------------

mvn install:install-file -Dfile=fi/mml/capabilities/1.3.0/capabilities-1.3.0.jar -DgroupId=fi.mml -DartifactId=capabilities -Dversion=1.3.0 -Dpackaging=jar

mvn install:install-file -Dfile=org/deegree/deegree/2/deegree-2.jar -DgroupId=org.deegree -DartifactId=deegree -Dversion=2 -Dpackaging=jar

mvn install:install-file -Dfile=com/liferay/rhino/liferay-rhino/1.0/liferay-rhino-1.0.jar -DgroupId=com.liferay.rhino -DartifactId=liferay-rhino -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=fi/mml/nameregister/1.0/nameregister-1.0.jar -DgroupId=fi.mml -DartifactId=nameregister -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=fi/mml/wms/1.1.1/wms-1.1.1.jar -DgroupId=fi.mml -DartifactId=wms -Dversion=1.1.1 -Dpackaging=jar
--------------------------------------------------------------------------------------------------------------------------------------------------
To deploy to a custom repository (example for -DrepositoryId=oskari_org -Durl=http://oskari.org/nexus/content/repositories/releases/):
-------------------------------------------------------------------------
mvn deploy:deploy-file -Dfile=fi/mml/capabilities/1.3.0/capabilities-1.3.0.jar -DgroupId=fi.mml -DartifactId=capabilities -Dversion=1.3.0 -Dpackaging=jar -DrepositoryId=oskari_org -Durl=http://oskari.org/nexus/content/repositories/releases/

mvn deploy:deploy-file -Dfile=org/deegree/deegree/2/deegree-2.jar -DgroupId=org.deegree -DartifactId=deegree -Dversion=2 -Dpackaging=jar -DrepositoryId=oskari_org -Durl=http://oskari.org/nexus/content/repositories/releases/

mvn deploy:deploy-file -Dfile=com/liferay/rhino/liferay-rhino/1.0/liferay-rhino-1.0.jar -DgroupId=com.liferay.rhino -DartifactId=liferay-rhino -Dversion=1.0 -Dpackaging=jar -DrepositoryId=oskari_org -Durl=http://oskari.org/nexus/content/repositories/releases/

mvn deploy:deploy-file -Dfile=fi/mml/nameregister/1.0/nameregister-1.0.jar -DgroupId=fi.mml -DartifactId=nameregister -Dversion=1.0 -Dpackaging=jar -DrepositoryId=oskari_org -Durl=http://oskari.org/nexus/content/repositories/releases/

mvn deploy:deploy-file -Dfile=fi/mml/wms/1.1.1/wms-1.1.1.jar -DgroupId=fi.mml -DartifactId=wms -Dversion=1.1.1 -Dpackaging=jar -DrepositoryId=oskari_org -Durl=http://oskari.org/nexus/content/repositories/releases/
