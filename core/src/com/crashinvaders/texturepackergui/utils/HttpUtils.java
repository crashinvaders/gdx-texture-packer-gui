package com.crashinvaders.texturepackergui.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class used to quickly download files on distant servers.
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class HttpUtils {

	/**
	 * Asynchronously downloads the file located at the given url. Content is
	 * written to the given stream. If the url is malformed, the method returns
	 * null. Else, a {@link DownloadTask} is returned. Use it if you need to
	 * cancel the download at any time.
	 * <p/>
	 * The method takes an optional callback as parameter, used to warn you
	 * when the download is complete, if an error happens (such as a connection
	 * loss). The callback also lets you be notified of the download progress.
	 */
	public static DownloadTask downloadAsync(String url, OutputStream output, Callback callback) {
		URL input;

		try {
			input = new URL(url);
		} catch (MalformedURLException ex) {
			callback.onError(ex);
			return null;
		}

		final DownloadTask task = new DownloadTask(input, output, callback);
		Thread th = new Thread(new Runnable() {@Override public void run() {task.download();}});
		th.start();
		return task;
	}

	// -------------------------------------------------------------------------
	// Classes
	// -------------------------------------------------------------------------

	/**
	 * Callback for a {@link DownloadTask}. Used to get notified about all the download
	 * events: completion, errors and progress.
	 */
	public static class Callback {
		public void onComplete() {}
		public void onCancel() {}
		public void onError(IOException ex) {}
		public void onUpdate(int length, int totalLength) {}
	}

	/**
	 * A download task lets you cancel the current download in progress. You
	 * can also access its parameters, such as the input and output streams.
	 */
	public static class DownloadTask {
		private final URL input;
		private final OutputStream output;
		private final Callback callback;
		private boolean run = true;

		public DownloadTask(URL input, OutputStream output, Callback callback) {
			this.input = input;
			this.output = output;
			this.callback = callback;
		}

		/**
		 * Cancels the download. If a callback is associated to the download
		 * task, its onCancel() method will be raised instead of the
		 * onComplete() one.
		 */
		public void stop() {
			run = false;
		}

		public URL getInput() {return input;}
		public OutputStream getOutput() {return output;}
		public Callback getCallback() {return callback;}

		private void download() {
			OutputStream os = null;
			InputStream is = null;
			IOException ex = null;

			try {
				HttpURLConnection connection = (HttpURLConnection) input.openConnection();
				connection.setDoInput(true);
				connection.setDoOutput(false);
				connection.setUseCaches(true);
				connection.setConnectTimeout(3000);
				connection.connect();

				is = new BufferedInputStream(connection.getInputStream(), 4096);
				os = output;

				byte[] data = new byte[4096];
				int length = connection.getContentLength();
				int total = 0;

				int count;
				while (run && (count = is.read(data)) != -1) {
					total += count;
					os.write(data, 0, count);
					if (callback != null) callback.onUpdate(total, length);
				}

			} catch (IOException ex1) {
				ex = ex1;

			} finally {
				if (os != null) try {os.flush(); os.close();} catch (IOException ex1) {}
				if (is != null) try {is.close();} catch (IOException ex1) {}

				if (callback != null) {
					if (ex != null) callback.onError(ex);
					else if (run == true) callback.onComplete();
					else callback.onCancel();
				}
			}
		}
	}
}
