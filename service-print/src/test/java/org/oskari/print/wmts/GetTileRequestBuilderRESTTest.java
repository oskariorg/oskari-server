package org.oskari.print.wmts;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GetTileRequestBuilderRESTTest {

    @Test(expected = IllegalArgumentException.class)
    public void nullTemplateThrowsException() {
        new GetTileRequestBuilderREST(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyTemplateThrowsException() {
        new GetTileRequestBuilderREST("");
    }

    @Test
    public void singleKnownParameterWorks() {
        GetTileRequestBuilderREST builder = new GetTileRequestBuilderREST("foo/{layer}/baz");
        builder.layer("bar");

        String expected = "foo/bar/baz";
        String actual = builder.build();
        assertEquals(expected, actual);
    }

    @Test
    public void settingTheWrongParameterChangesNothing() {
        GetTileRequestBuilderREST builder = new GetTileRequestBuilderREST("foo/{layer}/baz");
        builder.style("bar");

        String expected = "foo//baz";
        String actual = builder.build();
        assertEquals(expected, actual);
    }

    @Test
    public void unknownKeyInTemplateIsHandledAsText() {
        GetTileRequestBuilderREST builder = new GetTileRequestBuilderREST("foo/{bar}/baz");

        String expected = "foo/{bar}/baz";
        String actual = builder.build();
        assertEquals(expected, actual);
    }

    @Test
    public void changingTheBuildersValueUpdatesResult() {
        String template = "http://www.maps.bob/wmts/1.0.0/{layer}/{style}/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png";
        GetTileRequestBuilderREST builder = new GetTileRequestBuilderREST(template).layer("etopo2")
                .style("default")
                .tileMatrixSet("WholeWorld_CRS_84")
                .tileMatrix("1g")
                .tileRow(1)
                .tileCol(1);

        String expected = "http://www.maps.bob/wmts/1.0.0/etopo2/default/WholeWorld_CRS_84/1g/1/1.png";
        assertEquals(expected, builder.build());

        builder.tileRow(2).tileCol(0);
        expected = "http://www.maps.bob/wmts/1.0.0/etopo2/default/WholeWorld_CRS_84/1g/2/0.png";
        assertEquals(expected, builder.build());

        builder.tileMatrix("2g");
        expected = "http://www.maps.bob/wmts/1.0.0/etopo2/default/WholeWorld_CRS_84/2g/2/0.png";
        assertEquals(expected, builder.build());
    }

}