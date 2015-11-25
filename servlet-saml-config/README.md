# SAML support for oskari-server/servlet-map

Adds SAML-security module for the servlet. Out of the box authenticates against ssocircle.com if SAML is enabled.

Registering Oskari as Service Provider to an Identity provider:
- define configuration in oskari-ext.properties (at least 'oskari.saml.sp.entityId'). Example properties below.
- Provide the IDP metadata in location referenced by 'oskari.saml.idp.metadata' property
- deploy the webapp and call /saml/metadata URL to get the SP metadata XML
- Register Oskari as Service provider by providing the SP metadata XML to your IDP

To map roles based on IDP user data you can configure:

    oskari.saml.mapper=fi.nls.oskari.spring.security.saml.SimpleAttributeRoleMapper
    oskari.saml.mapper.attribute=FirstName
    oskari.saml.mapper.role.User=*
    oskari.saml.mapper.role.Admin=Sami, Matti

SimpleAttributeRoleMapper is provided as an example and can be used to map roles based on attribute values. The
above means that:
- a role named 'User' will be mapped to all users (* has special handling and means any value)
- a role named 'Admin' will be mapped to users where attribute 'FirstName' has value of 'Sami' or 'Matti'

Known issues:
- default logged in user role is mapped to users added with SAML authentication. To parse roles based on SAML response a hook is available (see above).
- Single IDP is supported (there might be an issue of duplicates if different users share an ID on separate IDPs)
- Tests missing

Additional properties for configuration for SAML login (no additional configuration is needed for DB login):
-------------------------------------------------------------------
    ###################################
    # Login profiles/configurations
    ###################################
    # Comma-separated list of spring profiles to use
    # Basic auth profile is 'LoginDatabase' which uses database tables to authenticate.
    # To disable login option remove it from 'oskari.profiles'
    oskari.profiles=LoginSAML, LoginDatabase

    # SAML requires additional configuration:
    # location of IDP metadata (default downloaded from http://idp.ssocircle.com/idp-meta.xml)
    # URL or location in classpath
    oskari.saml.idp.metadata=/saml/idp-meta-ssocircle.xml

    # Optional config. Baseurl is constructed from  'oskari.domain' + 'oskari.map.url' if not defined.
    # If 'oskari.domain' is http://mydomain.com you might want to define the base url as https://mydomain.com + 'oskari.map.url' property value
    #oskari.saml.sp.baseurl=https://mydomain.com/oskari

    # entity id that will identify our application to the IDP
    oskari.saml.sp.entityId=urn:org:oskari:servlet-app

    # When working on default ports and using load-balancer, comment out this property
    #  Otherwise you will get errors like: SAML endpoint doesn't match
    oskari.saml.lb.includePort=true

    # mapping from SAML attributes
    oskari.saml.credential.firstname = FirstName
    oskari.saml.credential.lastname = LastName
    oskari.saml.credential.email = EmailAddress
    # optional property for custom SAMLCredential to Oskari User mapping hook
    #oskari.saml.mapper=[fqcn for class implementing OskariUserMapper]

    # optional property to tell which attribute should be used as unique id (defaults to nameId on non-transient nameId and email when nameId is transient)
    #oskari.saml.mapper.uniqueAttribute=MyUniqueAttribName

    # project includes a dummy keystore, but you should use your own
    # Commands used to generate the dummy keystore:
    # * Generate new keystore: keytool -genkey -alias oskari -keyalg RSA -keystore oskariSAML.jks -keysize 2048
    # * Add key to keystore: keytool -genkeypair -alias oskariKey -keypass oskariPass -keystore oskariSAML.jks

    # classpath location and credentials of keystore
    oskari.keystore.saml=classpath:/saml/oskariSAML.jks
    oskari.keystore.saml.storepass=oskari
    oskari.keystore.saml.defaultKey=oskariKey
    # passwords for keys if not same as storepass. Format: 'oskari.keystore.saml.key.[key]=[password]'
    # oskari.keystore.saml.key.oskariKey=oskariPass
-------------------------------------------------------------------