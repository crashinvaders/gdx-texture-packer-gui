package com.crashinvaders.texturepackergui.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.Dtd;

import java.io.Writer;

public class LmlUtils {
    public static void saveDtdSchema(final LmlParser lmlParser, final FileHandle file) {
        try {
            final Writer appendable = file.writer(false, "UTF-8");
            final boolean strict = lmlParser.isStrict();
            lmlParser.setStrict(false); // Temporary setting to non-strict to generate as much tags as possible.
            Dtd.saveSchema(lmlParser, appendable);
            appendable.close();
            lmlParser.setStrict(strict);
        } catch (final Exception exception) {
            throw new GdxRuntimeException("Unable to save DTD schema.", exception);
        }
    }
}
