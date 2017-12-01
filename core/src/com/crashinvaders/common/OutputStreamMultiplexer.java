package com.crashinvaders.common;

import com.badlogic.gdx.utils.Array;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamMultiplexer extends OutputStream {

    private final Array<OutputStream> outputStreams = new Array<>();

    public OutputStreamMultiplexer() {
    }

    public OutputStreamMultiplexer(OutputStream outputStream) {
        outputStreams.add(outputStream);
    }

    public OutputStreamMultiplexer(OutputStream... outputStreams) {
        this.outputStreams.addAll(outputStreams);
    }

    public void addOutputStream(OutputStream outputStream) {
        outputStreams.add(outputStream);
    }

    public void removeOutputStream(OutputStream outputStream) {
        outputStreams.removeValue(outputStream, true);
    }

    @Override
    public void write(int b) throws IOException {
        for (int i = 0; i < outputStreams.size; i++) {
            outputStreams.get(i).write(b);
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        for (int i = 0; i < outputStreams.size; i++) {
            outputStreams.get(i).flush();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.flush();
        for (int i = 0; i < outputStreams.size; i++) {
            outputStreams.get(i).close();
        }
    }
}
