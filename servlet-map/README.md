# Spring for oskari-server/servlet-map

For adding SAML-support, include servlet-saml-config as dependency to your webapp and see README under the module for 
configuration options.

Known issues:
- doesn't provide JAAS authentication (and propably wont either see servlet-saml-config for an example to plugin custom security modules) 
- JNDI datasource provided by container is not used (always creates datasource manually)
- Ibatis connection pool name (JNDI) is hardcoded to OskariPool

