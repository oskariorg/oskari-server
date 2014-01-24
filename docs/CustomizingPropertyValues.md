# Customizing the application for your environment

Maven modify/filter some properties and xml files during compile/build. The default values for the placeholder values can be seen here:

[oskari-server/servlet-map/filter/filter-base.properties](../servlet-map/filter/filter-base.properties)

## Compile filter properties

To modify these compile-time defaults you can setup a profile in your maven global settings.xml:

Example ${MAVEN_HOME}/conf/settings.xml profile:

    <profile>
        <id>oskari-profile</id>
        <activation>
            <file>
                <exists>oskari-profile-trigger</exists>
            </file>
        </activation>
        <properties>
            <filter-path>/path/to/my/private/oskari-filter-profiles/${project.artifactId}</filter-path>
    		<filter-profile>my-local</filter-profile>
        </properties>
    </profile>

This will look for a file "my-local.properties" in directory /path/to/my/private/oskari-filter-profiles/map-servlet, and use the properties in that file during filtering of properties instead of the default "template.properties" found in map-servlet/filter directory. (For example if building using the "tomcat-profile" the "tomcat-template.properties" is used).

## Runtime properties

To override properties defined in `oskari-server/servlet-map/src/main/resources/oskari.properties` you can add an oskari-ext.properties file in your classpath. Any existing property will be overridden and any new ones will be available through PropertyUtil methods (for example PropertyUtil.get("propertyName")).