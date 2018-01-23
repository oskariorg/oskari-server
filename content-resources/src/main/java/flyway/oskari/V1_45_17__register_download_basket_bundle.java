package flyway.oskari;

import java.sql.Connection;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;

public class V1_45_17__register_download_basket_bundle implements JdbcMigration{

	private static final String NAMESPACE = "framework";
	private static final String DOWNLOAD_BASKET = "download-basket";

	public void migrate(Connection connection) {
	// BundleHelper checks if these bundles are already registered
		Bundle downloadBasket = new Bundle();
		downloadBasket.setConfig("{}");
		downloadBasket.setState("{}");
		downloadBasket.setName(DOWNLOAD_BASKET);
		downloadBasket.setStartup(BundleHelper.getDefaultBundleStartup(NAMESPACE, DOWNLOAD_BASKET, "Download basket"));
		BundleHelper.registerBundle(downloadBasket);
	}
}
