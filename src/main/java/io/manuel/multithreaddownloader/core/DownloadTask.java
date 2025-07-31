package io.manuel.multithreaddownloader.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import io.manuel.multithreaddownloader.controller.DownloadControl;

public class DownloadTask implements Runnable {

    private final String fileURL;
    private final String outputFile;
    private final int startByte;
    private final int endByte;
    private final int threadId;
    private final ProgressListener listener;
    private final DownloadControl control;
    private final String username;
    private final String password;

    public DownloadTask(String fileURL, 
    					String outputFile, 
    					int startByte, 
    					int endByte, 
    					int threadId,
    					ProgressListener listener,
    					DownloadControl control,
    					String username, 
    					String password) {
        this.fileURL = fileURL;
        this.outputFile = outputFile;
        this.startByte = startByte;
        this.endByte = endByte;
        this.threadId = threadId;
        this.listener = listener;
        this.control = control;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        try {
        	HttpURLConnection conn = (HttpURLConnection) new URL(fileURL).openConnection();
        	if (username != null && !username.isBlank()) {
        	    String basicAuth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        	    conn.setRequestProperty("Authorization", "Basic " + basicAuth);
        	}
            conn.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
            conn.connect();

            InputStream in = conn.getInputStream();
            RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
            raf.seek(startByte);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;
            long segmentSize = endByte - startByte + 1;

            while ((bytesRead = in.read(buffer)) != -1) {
            	
            	if (control.isCancelled()) {
            		break;
            	}
            	
            	control.waitIfPaused();
            	
                raf.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                double percent = (totalRead * 1.0 / segmentSize);
                listener.onProgress(threadId, percent);
            }

            raf.close();
            in.close();
            System.out.println("✅ Thread " + threadId + " completato.");

        } catch (IOException e) {
            System.out.println("❌ Thread " + threadId + " errore: " + e.getMessage());
        } catch (InterruptedException e) {
			System.out.println("❌ Thread interrotto");
		}
    }
}