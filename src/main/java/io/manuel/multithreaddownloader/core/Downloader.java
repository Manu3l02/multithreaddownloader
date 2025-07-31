package io.manuel.multithreaddownloader.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import io.manuel.multithreaddownloader.controller.DownloadControl;

public class Downloader {

	private final String fileURL;
	private final String outputFile;
	private final int numThreads;
	private final ProgressListener listener;
	private final DownloadControl control;
	private final String username;
	private final String password;

	public Downloader(
			String fileURL, 
			String outputFile, 
			int numThreads, 
			DownloadControl control,
			String username,
			String password,
			ProgressListener listener) {
		this.fileURL = fileURL;
		this.outputFile = outputFile;
		this.numThreads = numThreads;
		this.listener = listener;
		this.control = control;
		this.username = username;
		this.password = password;
	}

	public void start() throws IOException {
		File f = new File(outputFile);
		if (f.exists()) {
			System.out.println("⚠️ Il file '" + outputFile + "' esiste già. Download annullato.");
			return;
		}

		URL url = new URL(fileURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (username != null && !username.isBlank()) {
		    String basicAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
		    conn.setRequestProperty("Authorization", "Basic " + basicAuth);
		}
		int contentLength = conn.getContentLength();
		conn.disconnect();

		if (contentLength <= 0) {
			System.out.println("❌ Impossibile determinare la dimensione del file.");
			return;
		}

		System.out.println("📦 Dimensione file: " + contentLength + " byte");

		RandomAccessFile file = new RandomAccessFile(outputFile, "rw");
		file.setLength(contentLength);
		file.close();

		int partSize = contentLength / numThreads;
		Thread[] threads = new Thread[numThreads];

		for (int i = 0; i < numThreads; i++) {
			int start = i * partSize;
			int end = (i == numThreads - 1) ? contentLength - 1 : start + partSize - 1;

			threads[i] = new Thread(new DownloadTask(fileURL, outputFile, start, end, i, listener, control, username, password ));
			threads[i].start();
		}

		// Aspetta la fine di tutti i thread
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				System.out.println("⚠️ Download interrotto: " + e.getMessage());
			}
		}
		
		if (control.isCancelled()) {
			File fileToDelete = new File(outputFile);
			if (fileToDelete.exists()) {
				fileToDelete.delete();
			}
			return;
		}

		System.out.println("🎉 Download completato: " + outputFile);
	}
}