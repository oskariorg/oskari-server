package org.oskari.permissions;

import fi.nls.oskari.domain.GuestUser;
import fi.nls.test.util.TestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oskari.permissions.model.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

@Disabled("Was ignored even before, requires db")
public class PermissionServiceMybatisImplPerfTest {

    private static PermissionServiceMybatisImpl permissionService;

    @BeforeAll
    public static void init() throws SQLException, IOException, URISyntaxException {
        DataSource dataSource = TestHelper.createMemDBforUnitTest();
        permissionService = new PermissionServiceMybatisImpl(dataSource);
    }

    @Test
    public void findRes() {
        long start = System.currentTimeMillis();
        List<Resource> list = permissionService.findResourcesByUser(new GuestUser(), ResourceType.maplayer);
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(list.size());
    }

}
