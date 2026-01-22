package org.oskari.control.layer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.control.layer.model.LayerListResponse;
import org.oskari.map.myfeatures.service.MyFeaturesLayerProvider;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.PermissionExternalType;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;
import org.oskari.service.maplayer.LayerProvider;
import org.oskari.service.maplayer.OskariMapLayerGroupService;
import org.oskari.service.maplayer.OskariMapLayerProvider;
import org.oskari.user.GuestUser;
import org.oskari.user.Role;
import org.oskari.user.User;

import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;

public class LayerListHandlerTest {

    @Test
    public void happyCase() {
        String language = "en";

        Role guestRole = new Role();
        guestRole.setId(1337);
        guestRole.setName("Guest");

        Permission guestViewLayer = new Permission();
        guestViewLayer.setExternalType(PermissionExternalType.ROLE);
        guestViewLayer.setExternalId((int) guestRole.getId());
        guestViewLayer.setType(PermissionType.VIEW_LAYER);

        DataProvider provider = new DataProvider();
        provider.setId(418);
        provider.setName(language, "Teapot");

        OskariLayer layer = new OskariLayer();
        layer.setId(400);
        layer.setName(language, "My testcase");
        layer.setDataproviderId(provider.getId());

        Resource resource = new Resource();
        resource.setId(401);
        resource.setType(ResourceType.maplayer);
        resource.setMapping(Integer.toString(layer.getId()));
        resource.addPermission(guestViewLayer);

        MaplayerGroup group = new MaplayerGroup();
        group.setName(language, "The very best Group");
        group.setId(403);

        OskariLayerGroupLink link = new OskariLayerGroupLink(layer.getId(), group.getId());

        User guest = new GuestUser();
        guest.setUuid(UUID.randomUUID().toString());
        guest.addRole(guestRole);

        MyFeaturesLayer myFeaturesLayer = new MyFeaturesLayer();
        myFeaturesLayer.setId(UUID.randomUUID());
        myFeaturesLayer.setName(language, "My very own feature layer");
        myFeaturesLayer.setCreated(Instant.now());
        myFeaturesLayer.setUpdated(Instant.now());

        OskariLayerService layerService = when(mock(OskariLayerService.class).findAll())
                .thenReturn(Arrays.asList(layer)).getMock();
        PermissionService permissionService = when(mock(PermissionService.class).findResourcesByUser(guest, ResourceType.maplayer))
                        .thenReturn(Arrays.asList(resource)).getMock();

        OskariMapLayerProvider oskariLayerProvider = new OskariMapLayerProvider();
        oskariLayerProvider.setLayerService(layerService);
        oskariLayerProvider.setPermissionService(permissionService);

        MyFeaturesService myFeaturesService = when(mock(MyFeaturesService.class).getLayersByOwnerUuid(guest.getUuid()))
                        .thenReturn(Arrays.asList(myFeaturesLayer)).getMock();

        MyFeaturesLayerProvider myfLayerProvider = new MyFeaturesLayerProvider();
        myfLayerProvider.setService(myFeaturesService);

        List<LayerProvider> layerProviders = Arrays.asList(oskariLayerProvider, myfLayerProvider);

        LayerListHandler handler = new LayerListHandler();
        handler.setLayerProviders(layerProviders);
        handler.setGroupService(
                when(mock(OskariMapLayerGroupService.class).findAll())
                        .thenReturn(Arrays.asList(group)).getMock());
        handler.setLinkService(
                when(mock(OskariLayerGroupLinkService.class).findAll())
                        .thenReturn(Arrays.asList(link)).getMock());
        handler.setDataProviderService(
                when(mock(DataProviderService.class).findAll())
                        .thenReturn(Arrays.asList(provider)).getMock());

        LayerListResponse response = handler.getLayerList(guest, language);
        Assertions.assertEquals(2, response.layers.size());
        Assertions.assertEquals(1, response.groups.size());
        Assertions.assertEquals(1, response.providers.size());

        Assertions.assertTrue(response.layers.stream().anyMatch(l -> "My testcase".equals(l.name)));
        Assertions.assertTrue(response.layers.stream().anyMatch(l -> "My very own feature layer".equals(l.name)));
        Assertions.assertEquals("The very best Group", response.groups.get(0).name);
        Assertions.assertEquals("Teapot", response.providers.values().stream().findAny().get().name);
    }

    @Test
    public void adminsGetEverythingBackButOtherPeoplesMyFeaturesLayers() {
        String language = "en";

        DataProvider dp1 = new DataProvider();
        dp1.setId(418);
        dp1.setName(language, "Teapot");

        DataProvider dp2 = new DataProvider();
        dp2.setId(419);
        dp2.setName(language, "Mr. Coffee");

        OskariLayer layer = new OskariLayer();
        layer.setId(400);
        layer.setName(language, "My testcase");
        layer.setDataproviderId(dp1.getId());

        Permission anyPermission = new Permission();
        anyPermission.setExternalType(PermissionExternalType.ROLE);
        anyPermission.setType(PermissionType.VIEW_LAYER);

        Resource resource = new Resource();
        resource.setId(401);
        resource.setType(ResourceType.maplayer);
        resource.setMapping(Integer.toString(layer.getId()));
        resource.addPermission(anyPermission);

        MaplayerGroup g1 = new MaplayerGroup();
        g1.setId(403);

        MaplayerGroup g2 = new MaplayerGroup();
        g2.setId(404);

        User admin = new User();
        // Expected: Outputs an ERROR and a WARNING to log but works
        admin.addRole(Role.getAdminRole());

        MyFeaturesLayer myFeaturesLayer = new MyFeaturesLayer();
        myFeaturesLayer.setId(UUID.randomUUID());
        myFeaturesLayer.setName(language, "My very own feature layer");
        myFeaturesLayer.setCreated(Instant.now());
        myFeaturesLayer.setUpdated(Instant.now());

        OskariLayerService layerService = when(mock(OskariLayerService.class).findAll())
                .thenReturn(Arrays.asList(layer)).getMock();
        PermissionService permissionService = when(mock(PermissionService.class).findResourcesByUser(admin, ResourceType.maplayer))
                        .thenReturn(Arrays.asList(resource)).getMock();

        OskariMapLayerProvider oskariLayerProvider = new OskariMapLayerProvider();
        oskariLayerProvider.setLayerService(layerService);
        oskariLayerProvider.setPermissionService(permissionService);

        MyFeaturesService myFeaturesService = when(mock(MyFeaturesService.class).getLayersByOwnerUuid(any()))
                        .thenReturn(Collections.emptyList()).getMock();

        MyFeaturesLayerProvider myfLayerProvider = new MyFeaturesLayerProvider();
        myfLayerProvider.setService(myFeaturesService);

        List<LayerProvider> layerProviders = Arrays.asList(oskariLayerProvider, myfLayerProvider);

        LayerListHandler handler = new LayerListHandler();
        handler.setLayerProviders(layerProviders);
        handler.setGroupService(
                when(mock(OskariMapLayerGroupService.class).findAll())
                        .thenReturn(Arrays.asList(g1, g2)).getMock());
        handler.setLinkService(
                when(mock(OskariLayerGroupLinkService.class).findAll())
                        .thenReturn(Collections.emptyList()).getMock());
        handler.setDataProviderService(
                when(mock(DataProviderService.class).findAll())
                        .thenReturn(Arrays.asList(dp1, dp2)).getMock());

        LayerListResponse response = handler.getLayerList(admin, language);
        Assertions.assertEquals(1, response.layers.size());
        Assertions.assertEquals(2, response.groups.size());
        Assertions.assertEquals(2, response.providers.size());

        Assertions.assertTrue(response.layers.stream().anyMatch(l -> "My testcase".equals(l.name)));
        Assertions.assertFalse(response.layers.stream().anyMatch(l -> "My very own feature layer".equals(l.name)));

    }

}
