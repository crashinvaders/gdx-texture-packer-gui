package com.crashinvaders.texturepackergui.services.model.compression;

import java.io.StringWriter;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.StreamUtils;
import com.crashinvaders.texturepackergui.services.model.EtcCompressionType;

public class EtcCompressionModel {
	private final EtcCompressionType type;
	
	private boolean compressed = false;
	private String etc1Comp = "-etc1";
	private String etc2Comp = null;

	public EtcCompressionModel(EtcCompressionType type) {
        this.type = type;
    }

    public EtcCompressionType getType() {
        return type;
    }
    
    public boolean isCompressed() {
		return compressed;
	}

	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	public String getEtc1Comp() {
		return etc1Comp;
	}

	public void setEtc1Comp(String etc1Comp) {
		this.etc1Comp = etc1Comp;
	}

	public String getEtc2Comp() {
		return etc2Comp;
	}

	public void setEtc2Comp(String etc2Comp) {
		this.etc2Comp = etc2Comp;
	}

	public String serializeState() {
        StringWriter buffer = new StringWriter();
        try {
            Json json = new Json();
            json.setWriter(new JsonWriter(buffer));
            json.writeObjectStart();
            json.writeValue("etc1Comp", etc1Comp);
            json.writeValue("etc2Comp", etc2Comp);
            json.writeValue("etcCompressed", compressed);
            json.writeObjectEnd();
            return buffer.toString();
        } finally {
            StreamUtils.closeQuietly(buffer);
        }
    }

    public void deserializeState(String data) {
        if (data == null) return;

        JsonValue jsonValue = new JsonReader().parse(data);
        etc1Comp = jsonValue.getString("etc1Comp", "-etc1");
        etc2Comp = jsonValue.getString("etc2Comp", etc2Comp);
        compressed = jsonValue.getBoolean("etcCompressed", compressed);
    }


}
