package com.crashinvaders.texturepackergui.controllers.model.filetype;

import com.badlogic.gdx.utils.*;
import com.crashinvaders.common.statehash.StateHashUtils;
import com.crashinvaders.texturepackergui.controllers.model.FileTypeType;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent;
import com.crashinvaders.texturepackergui.events.FileTypePropertyChangedEvent.Property;
import com.crashinvaders.texturepackergui.utils.CommonUtils;
import com.crashinvaders.texturepackergui.utils.KtxEtc2Processor;

import java.io.StringWriter;

public class BasisuFileTypeModel extends FileTypeModel {

    @Override
    public FileTypeType getType() {
        return FileTypeType.BASIS;
    }

    @Override
    public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
//            json.writeValue("format", format.name());
//            json.writeValue("encodingEtc1", encodingEtc1.name());
//            json.writeValue("encodingEtc2", encodingEtc2.name());
//            json.writeValue("zipping", zipping);
            json.writeObjectEnd();
            return buffer.toString();
        } finally {
            StreamUtils.closeQuietly(buffer);
        }
    }

    @Override
    public void deserializeState(String data) {
        if (data == null) return;

        JsonValue jsonValue = new JsonReader().parse(data);
//        this.format = CommonUtils.findEnumConstantSafe(Format.class,
//                jsonValue.getString("format", null), this.format);
//        this.encodingEtc1 = CommonUtils.findEnumConstantSafe(EncodingETC1.class,
//                jsonValue.getString("encodingEtc1", null), this.encodingEtc1);
//        this.encodingEtc2 = CommonUtils.findEnumConstantSafe(EncodingETC2.class,
//                jsonValue.getString("encodingEtc2", null), this.encodingEtc2);
//        this.zipping = jsonValue.getBoolean("zipping", this.zipping);
    }

    @Override
    public int computeStateHash() {
        return StateHashUtils.computeHash(/*format, encodingEtc1, encodingEtc2, zipping*/);
    }
}
