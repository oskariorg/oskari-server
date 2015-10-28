/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.renderer.oskari;

import org.geotools.renderer.style.MarkFactory;
import org.geotools.renderer.style.shape.ExplicitBoundsShape;
import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This factory accepts mark paths in the <code>oskari://fontName#code</code>
 * format, where fontName is the filename without extension (.ttf is added automatically) of a TrueType font found in classpath,
 * and the code is the character code, which may
 * be expressed in decimal, hexadecimal (e.g. <code>0x10</code>) octal (e.g.
 * <code>045</code>) form, as well as Unicode codes (e.g. <code>U+F054</code>
 * or <code>\uF054</code>).
 *
 * Its built on top of geotools TTFMarkFactory, but handles mark placement a bit differently.
 * The main purpose is to have point markers with correct placement in the font file and NOT trying to
 * determine placement using font size.
 *
 * @author Matti Pulakka
 * @author Sami Mäkinen
 *
 * @source $URL$
 */
public class TTFMarkFactoryOskari implements MarkFactory {

    /** The logger for the rendering module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("TTFMarkFactoryOskari.class");

    private static FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(
            new AffineTransform(), false, false);

    // TODO: maybe use org.geotools.renderer.style.FontCache.getDefaultInstance().registerFont(); instead?
    // need to check how font gets name with fontcache so we can use 'dot-markers' to get it back.
    // in the meantime, using custom cache to avoid any conflicts
    private Map<String, Font> fontCache = new HashMap<String, Font>();

    private final String DEFAULT_FONT = "dot-markers";

    public Shape getShape(Graphics2D graphics, Expression symbolUrl, Feature feature)
            throws Exception {
        String markUrl = symbolUrl.evaluate(feature, String.class);

        // if it does not start with the right prefix, it's not our business
        if (!markUrl.startsWith("oskari://"))
            return null;

        // if it does not match the expected format, complain before exiting
        if (!markUrl.matches("oskari://.+#.+")) {
            throw new IllegalArgumentException(
                    "Mark URL font found, but does not match the required "
                            + "structure font://<fontName>#<charNumber>, e.g., ttf://wingdigs#0x7B. You specified "
                            + markUrl);
        }
        String[] fontElements = markUrl.substring(9).split("#");

        // look up the font
        final Font font = getFont(fontElements[0]);

        // get the symbol number
        String code = fontElements[1];
        
        char character;
        try {
            // see if a unicode escape sequence has been used
            if (code.startsWith("U+") || code.startsWith("\\u")) {
                code = "0x" + code.substring(2);
            }
            
            // this will handle most numeric formats like decimal, hex and octal
            character = (char) Integer.decode(code).intValue();
            
            // handle charmap code reporting issues 
            if(!font.canDisplay(character)) {
                character = (char) (0xF000 | character);
            }
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid character specification " + fontElements[1], e);
        }

        // build the shape out of the font
        GlyphVector textGlyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT,
                new char[] { character });
        Shape s = textGlyphVector.getOutline();

        AffineTransform tx = new AffineTransform();
        double fontSize = font.getSize();
        tx.scale(1 / fontSize, -1 / fontSize);
        tx.translate(-0.5, 0.5);

        ExplicitBoundsShape shape = new ExplicitBoundsShape(tx.createTransformedShape(s));
        shape.setBounds(new Rectangle2D.Double(-0.5,0.5,1.0,1.0));
        return shape;
    }

    private Font getFont(final String name) throws Exception {

        Font font = fontCache.get(name);
        if(font != null) {
            return font;
        }
        font = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/" + name + ".ttf"));
        if(font != null) {
            fontCache.put(name, font);
            return font;
        }
        if(!DEFAULT_FONT.equals(name)) {
            LOGGER.warning("Couldn't find font with name: '" + name + "' - using default font instead: " + DEFAULT_FONT);
            return getFont(DEFAULT_FONT);
        }
        throw new IllegalArgumentException("Unknown font " + name);
    }
}
