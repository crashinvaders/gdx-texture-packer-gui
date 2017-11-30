package com.crashinvaders.texturepackergui.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A ThreadPrintStream replaces the normal System.out and ensures
 * that output to System.out goes to a different PrintStream for
 * each thread.  It does this by using ThreadLocal to maintain a
 * PrintStream for each thread.
 */
public class ThreadPrintStream extends PrintStream {

    /**
     * Changes System.out to a ThreadPrintStream which will
     * send output to a separate file for each thread.
     */
    public static void replaceSystemOut() {

        // Create a ThreadPrintStream and install it as System.out
        final ThreadPrintStream threadStdOut = new ThreadPrintStream();
        System.setOut(threadStdOut);
        System.setErr(threadStdOut);

        // Use the original System.out as the current thread's System.out
        threadStdOut.setThreadOut(System.out);
    }

    public static PrintStream getThreadLocalSystemOut() {
        return out.get();
    }

    public static void setThreadLocalSystemOut(final PrintStream newSystemOut) {
        out.set(newSystemOut);
    }

    /**
     * Thread specific storage to hold a PrintStream for each thread
     */
    private static final InheritableThreadLocal<PrintStream> out = new InheritableThreadLocal<PrintStream>() {
        @Override
        protected PrintStream initialValue() {
            return System.out;
        }
    };

    private ThreadPrintStream() {
        super(new ByteArrayOutputStream(0));
    }

    /**
     * Sets the PrintStream for the currently executing thread.
     */
    public void setThreadOut(PrintStream out) {
        this.out.set(out);
    }

    /**
     * Returns the PrintStream for the currently executing thread.
     */
    public PrintStream getThreadOut() {
        return this.out.get();
    }

    @Override
    public boolean checkError() {
        return getThreadOut().checkError();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        getThreadOut().write(buf, off, len);
    }

    @Override
    public void write(int b) {
        getThreadOut().write(b);
    }

    @Override
    public void flush() {
        getThreadOut().flush();
    }

    @Override
    public void close() {
        getThreadOut().close();
    }
}
