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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import org.geotools.renderer.style.shape.ExplicitBoundsShape;
import org.geotools.renderer.style.MarkFactory;
import org.geotools.renderer.style.FontCache;
import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;

/**
 * This factory accepts mark paths in the <code>oskari://fontName#code</code>
 * format, where fontName is the name of a TrueType font installed in the
 * system, or a URL to a TTF file, and the code is the character code, which may
 * be expressed in decimal, hexadecimal (e.g. <code>0x10</code>) octal (e.g.
 * <code>045</code>) form, as well as Unicode codes (e.g. <code>U+F054</code>
 * or <code>\uF054</code>).
 *
 * @author Andrea Aime - TOPP
 *
 *
 *
 *
 * @source $URL$
 */
public class TTFMarkFactoryOskari implements MarkFactory {

    /** The logger for the rendering module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("TTFMarkFactoryOskari.class");

    private static FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(
            new AffineTransform(), false, false);

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
        Font font = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/dot-markers.ttf"));
        
        if (font == null) {
            throw new IllegalArgumentException("Unknown font " + fontElements[0]);
        }

        // get the symbol number
        String code = fontElements[1];
        
        char character;
        try {
            // see if a unicode escape sequence has been used
            if (code.startsWith("U+") || code.startsWith("\\u")) 
                code = "0x" + code.substring(2);
            
            // this will handle most numeric formats like decimal, hex and octal
            character = (char) Integer.decode(code).intValue();
            
            // handle charmap code reporting issues 
            if(!font.canDisplay(character))
                character = (char) (0xF000 | character);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid character specification " + fontElements[1], e);
        }

        // build the shape out of the font
        GlyphVector textGlyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT,
                new char[] { (char) character });
        float offset = font.getSize();
        Shape s = textGlyphVector.getOutline();
        
        // have the shape be centered in the origin, and sitting in a square of side 1
        Rectangle2D bounds = s.getBounds2D();
        
        AffineTransform tx = new AffineTransform();
        double max = Math.max(bounds.getWidth(), bounds.getHeight());
        // all shapes are defined looking "upwards" (see ShapeMarkFactory or WellKnownMarkFactory)
        // but the fonts ones are flipped to compensate for the fact the y coords grow from top
        // to bottom on the screen. We have to flip the symbol so that it conforms to the
        // other marks convention
//      tx.scale(1 / max, -1 / max);

        double fontSize = font.getSize();
        tx.scale(1 / fontSize, -1 / fontSize);

//      tx.translate(-bounds.getCenterX(), -bounds.getCenterY());

        tx.translate(-0.5, 0.5);

        ExplicitBoundsShape shape = new ExplicitBoundsShape(tx.createTransformedShape(s));
        shape.setBounds(new Rectangle2D.Double(-0.5,0.5,1.0,1.0));
        return shape;
    }

}
