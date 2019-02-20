

COMMENT ON TABLE oskari_backendstatus IS 'Results of layer data source availability probes';
COMMENT ON TABLE oskari_capabilities_cache IS 'Cache of GetCapabilities results for WMS/WMTS layers';
COMMENT ON TABLE oskari_dataprovider IS 'Layer data provider name localizations';
COMMENT ON TABLE oskari_jaas_users IS 'Credentials for users when using built-in login';
COMMENT ON TABLE oskari_maplayer IS 'Map layers configuration';
COMMENT ON TABLE oskari_maplayer_externalid IS 'Legacy "external id" associated with map layer';
COMMENT ON TABLE oskari_maplayer_group IS 'Logical group for layers';
COMMENT ON TABLE oskari_maplayer_group_link IS 'Bridge table between map layer and its group';
COMMENT ON TABLE oskari_maplayer_metadata IS 'Metadata about map layers';
COMMENT ON TABLE oskari_maplayer_projections IS 'Supported projections (EPSG-codes) for map layers';
COMMENT ON TABLE oskari_permission IS 'Permissions for resources';
COMMENT ON TABLE oskari_resource IS 'Abstraction for map layer or functionality to resource that permissions are linked to';
COMMENT ON TABLE oskari_role_external_mapping IS 'For mapping roles from external system to Oskari roles (requires custom code to use)';
COMMENT ON TABLE oskari_role_oskari_user IS 'Bridge table connecting role and user';
COMMENT ON TABLE oskari_roles IS 'Roles that have associated permissions';
COMMENT ON TABLE oskari_statistical_datasource IS 'Data source for statistical data (thematic maps)';
COMMENT ON TABLE oskari_statistical_layer IS 'Link map layers with region geometry to statistical data sources (based on data available on the data source)';
COMMENT ON TABLE oskari_user_indicator IS 'Metadata for statistical indicators created by users';
COMMENT ON TABLE oskari_user_indicator_data IS 'Data for statistical indicators created by users';
COMMENT ON TABLE oskari_users IS 'Oskari instance user accounts';
COMMENT ON TABLE oskari_users_pending IS 'Users that have started registration process but not completed it yet (used when end-user registration is enabled)';
COMMENT ON TABLE oskari_wfs_parser_config IS 'Configuration for WFS parser describing parsing instructions for complex features';
COMMENT ON TABLE oskari_wfs_search_channels IS 'Configuration for using WFS-services as search services';
COMMENT ON TABLE portti_bundle IS 'List of available front-end functionality modules';
COMMENT ON TABLE portti_keyword_association IS 'Conceptual linking of keywords';
COMMENT ON TABLE portti_keywords IS 'Keywords that can be associated with resources';
COMMENT ON TABLE portti_layer_keywords IS 'Bridge table linking map layers and keywords describing them';
COMMENT ON TABLE portti_published_map_statistics IS 'Counter for total usage on embedded maps (not used)';
COMMENT ON TABLE portti_published_map_usage IS 'Counter for monthly usage limits on embedded maps (not used)';
COMMENT ON TABLE portti_terms_of_use_for_publishing IS 'Approval of terms for publishing by user';
COMMENT ON TABLE portti_view IS 'Map views/appsetups';
COMMENT ON TABLE portti_view_bundle_seq IS 'Bundles present in a view/appsetup and their loading order';
COMMENT ON TABLE portti_wfs_layer IS 'Additional information about WFS-services for the "transport" vector layer implementation';
COMMENT ON TABLE portti_wfs_layer_style IS 'Style SLD for "transport" vector layers';
COMMENT ON TABLE portti_wfs_layers_styles IS 'Bridge table between style and "transport" vector layer';
COMMENT ON TABLE portti_wfs_template_model IS 'Templates for oskari_wfs_parser_config';
COMMENT ON TABLE ratings IS 'Table for rating metadata/anything really (not used currently)';


