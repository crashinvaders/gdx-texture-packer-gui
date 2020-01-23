package com.crashinvaders.texturepackergui.lml.attributes.seekbar;

import com.crashinvaders.texturepackergui.views.seekbar.SeekBar;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.tag.LmlAttribute;
import com.github.czyzby.lml.parser.tag.LmlTag;

public class SeekBarChangePolicyLmlAttribute implements LmlAttribute<SeekBar> {
    @Override
    public Class<SeekBar> getHandledType() {
        return SeekBar.class;
    }

    @Override
    public void process(LmlParser parser, LmlTag tag, SeekBar seekBar, String rawAttributeData) {
        String value = parser.parseString(rawAttributeData);
        SeekBar.ChangeEventPolicy eventPolicy;
        try {
            eventPolicy = SeekBar.ChangeEventPolicy.valueOf(value);
        } catch (IllegalArgumentException e) {
            parser.throwError("Unexpected change policy value: " + value, e);
            return;
        }
        seekBar.setChangeEventPolicy(eventPolicy);
    }
}
