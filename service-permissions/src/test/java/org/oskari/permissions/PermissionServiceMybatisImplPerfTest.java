package org.oskari.permissions;

import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.oskari.permissions.model.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@Ignore
public class PermissionServiceMybatisImplPerfTest {

    private static PermissionServiceMybatisImpl permissionService;

    @BeforeClass
    public static void init() throws SQLException, IOException, URISyntaxException {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/oskaridb");
        dataSource.setUsername("oskari");
        dataSource.setPassword("oskari");
        permissionService = new PermissionServiceMybatisImpl(dataSource);
    }

    @Test
    public void findRes() {
        long start = System.currentTimeMillis();
        List<Resource> list = permissionService.findResourcesByUser(new GuestUser());
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(list.size());

        start = System.currentTimeMillis();
        list = permissionService.findResourcesByUser(new GuestUser(), ResourceType.maplayer);
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(list.size());
    }

}
