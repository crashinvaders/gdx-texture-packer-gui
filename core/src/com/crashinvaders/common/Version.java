package com.crashinvaders.common;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class Version {
    /** the current major version */
    private final int major;
    /** the current minor version */
    private final int minor;
    /** the current revision version */
    private final int revision;

    public Version(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    /** @param version should be in the major.minor.revision format */
    public Version(String version) {
        try {
            String[] v = version.split("\\.");
            major = v.length < 1 ? 0 : Integer.valueOf(v[0]);
            minor = v.length < 2 ? 0 : Integer.valueOf(v[1]);
            revision = v.length < 3 ? 0 : Integer.valueOf(v[2]);
        } catch (Throwable t) {
            // Should never happen
            throw new GdxRuntimeException("Invalid version " + version, t);
        }
    }


    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    public boolean isHigher(Version version) {
        if (this.major != version.major)
            return this.major > version.major;
        if (this.minor != version.minor)
            return this.minor > version.minor;
        return this.revision > version.revision;
    }

    public boolean isLower(Version version) {
        if (this.major != version.major)
            return this.major < version.major;
        if (this.minor != version.minor)
            return this.minor < version.minor;
        return this.revision < version.revision;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append(".").append(minor).append(".").append(revision);
        return sb.toString();
    }

    //TODO override equals and hashcode
}
