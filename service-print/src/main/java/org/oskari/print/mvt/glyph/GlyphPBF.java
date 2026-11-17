package org.oskari.print.mvt.glyph;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.CodedInputStream;

/**
 * Reads a Mapbox glyph PBF.
 *
 * The schema is small and has not changed since it was published, so it is read
 * with {@link CodedInputStream} rather than from generated classes:
 *
 * <pre>
 * message glyph {
 *   required uint32 id = 1;
 *   optional bytes bitmap = 2;   // SDF, (width+6) * (height+6)
 *   required uint32 width = 3;
 *   required uint32 height = 4;
 *   required sint32 left = 5;
 *   required sint32 top = 6;
 *   required uint32 advance = 7;
 * }
 * message fontstack { required string name = 1; required string range = 2; repeated glyph glyphs = 3; }
 * message glyphs { repeated fontstack stacks = 1; }
 * </pre>
 */
public class GlyphPBF {

    private static final int FIELD_GLYPHS_STACKS = 1;
    private static final int FIELD_STACK_GLYPHS = 3;

    private static final int FIELD_GLYPH_ID = 1;
    private static final int FIELD_GLYPH_BITMAP = 2;
    private static final int FIELD_GLYPH_WIDTH = 3;
    private static final int FIELD_GLYPH_HEIGHT = 4;
    private static final int FIELD_GLYPH_LEFT = 5;
    private static final int FIELD_GLYPH_TOP = 6;
    private static final int FIELD_GLYPH_ADVANCE = 7;

    private GlyphPBF() {}

    /**
     * @return the glyphs of every fontstack in the message, keyed by code point.
     *         The first stack that carries a code point wins, matching the order
     *         the fontstack was asked for.
     */
    public static Map<Integer, SDFGlyph> parse(byte[] encoded) throws IOException {
        Map<Integer, SDFGlyph> glyphs = new HashMap<>();
        CodedInputStream in = CodedInputStream.newInstance(encoded);
        while (!in.isAtEnd()) {
            int tag = in.readTag();
            if (getFieldNumber(tag) == FIELD_GLYPHS_STACKS) {
                readLengthDelimited(in, nested -> readFontstack(nested, glyphs));
            } else {
                in.skipField(tag);
            }
        }
        return glyphs;
    }

    private static void readFontstack(CodedInputStream in, Map<Integer, SDFGlyph> glyphs)
            throws IOException {
        while (!in.isAtEnd()) {
            int tag = in.readTag();
            if (getFieldNumber(tag) == FIELD_STACK_GLYPHS) {
                readLengthDelimited(in, nested -> {
                    SDFGlyph glyph = readGlyph(nested);
                    if (glyph != null) {
                        glyphs.putIfAbsent(glyph.getCodePoint(), glyph);
                    }
                });
            } else {
                in.skipField(tag);
            }
        }
    }

    private static SDFGlyph readGlyph(CodedInputStream in) throws IOException {
        int id = -1;
        byte[] bitmap = null;
        int width = 0;
        int height = 0;
        int left = 0;
        int top = 0;
        int advance = 0;

        while (!in.isAtEnd()) {
            int tag = in.readTag();
            switch (getFieldNumber(tag)) {
            case FIELD_GLYPH_ID:
                id = in.readUInt32();
                break;
            case FIELD_GLYPH_BITMAP:
                bitmap = in.readByteArray();
                break;
            case FIELD_GLYPH_WIDTH:
                width = in.readUInt32();
                break;
            case FIELD_GLYPH_HEIGHT:
                height = in.readUInt32();
                break;
            case FIELD_GLYPH_LEFT:
                left = in.readSInt32();
                break;
            case FIELD_GLYPH_TOP:
                top = in.readSInt32();
                break;
            case FIELD_GLYPH_ADVANCE:
                advance = in.readUInt32();
                break;
            default:
                in.skipField(tag);
                break;
            }
        }

        if (id < 0) {
            return null;
        }
        return new SDFGlyph(id, bitmap, width, height, left, top, advance);
    }

    /**
     * Reads a nested message, keeping the reader's size limit to the message so
     * that a truncated field can't run into the next one.
     */
    private static void readLengthDelimited(CodedInputStream in, NestedReader reader)
            throws IOException {
        int length = in.readRawVarint32();
        int limit = in.pushLimit(length);
        reader.read(in);
        in.popLimit(limit);
    }

    private static int getFieldNumber(int tag) {
        return tag >>> 3;
    }

    @FunctionalInterface
    private interface NestedReader {
        void read(CodedInputStream in) throws IOException;
    }
}
