package io.manuel.multithreaddownloader.controller;

import io.manuel.multithreaddownloader.core.Downloader;
import io.manuel.multithreaddownloader.core.ProgressListener;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadManager {
	
    public interface Callback {
        void onLog(String message);
        void onProgress(int threadId, double percent, double average);
        void onComplete();
        void onError(String errorMessage);
    }

    public void startDownload(
    		String fileURL, 
    		String manualName, 
    		File folder, 
    		int threads,
    		DownloadControl control,
    		Callback callback) {
        try {
            URL url = new URL(fileURL);

            String outputFileName = determineFileName(url, manualName);
            File targetDir = (folder != null) ? folder : new File(".");
            File outputFile = resolveOutputFile(targetDir, outputFileName);
            String output = outputFile.getAbsolutePath();

            callback.onLog("Avvio download: " + output);

            double[] progresses = new double[threads];
            
            double[] lastNotified = new double[threads];

            Downloader downloader = new Downloader(fileURL, output, threads, control, new ProgressListener() {
            	@Override
            	public void onProgress(int threadId, double percent) {
            	    if (Math.abs(percent - lastNotified[threadId]) < 0.01) return; // ignora aggiornamenti < 1%
            	    lastNotified[threadId] = percent;

            	    progresses[threadId] = percent;

            	    double total = 0;
            	    for (double p : progresses) total += p;
            	    double average = total / threads;

            	    Platform.runLater(() -> callback.onProgress(threadId, percent, average));
            	}
            });

            new Thread(() -> {
                try {
                    downloader.start();
                    if (control.isCancelled()) {
                    	Platform.runLater(() -> {
                    		callback.onLog("⚠️ Download annullato dall'utente. File eliminato.");
                    	});
                    	return;
                    }
                    Platform.runLater(callback::onComplete);
                } catch (IOException e) {
                    Platform.runLater(() -> callback.onError(e.getMessage()));
                }
            }).start();

        } catch (Exception e) {
            callback.onError("URL non valido: " + e.getMessage());
        }
    }

    private String determineFileName(URL url, String manualName) {
        if (manualName != null && !manualName.isBlank()) {
            if (!manualName.contains(".")) {
                String path = url.getPath();
                if (path.contains(".")) {
                    manualName += path.substring(path.lastIndexOf("."));
                }
            }
            return manualName;
        } else {
            String path = url.getPath();
            String name = path.substring(path.lastIndexOf("/") + 1);
            if (name.isEmpty()) {
                name = "multithread_downloader_tmgdw";
            } else {
                int queryIdx = name.indexOf('?');
                if (queryIdx != -1) {
                    name = name.substring(0, queryIdx);
                }
                int dot = name.lastIndexOf('.');
                if (dot != -1) {
                    name = name.substring(0, dot) + "_tmgdw" + name.substring(dot);
                } else {
                    name += "_tmgdw";
                }
            }
            return name;
        }
    }

    private File resolveOutputFile(File dir, String name) {
        File file = new File(dir, name);
        int count = 1;
        while (file.exists()) {
            String base = name;
            String ext = "";
            int dot = name.lastIndexOf('.');
            if (dot != -1) {
                base = name.substring(0, dot);
                ext = name.substring(dot);
            }
            file = new File(dir, base + "(" + count + ")" + ext);
            count++;
        }
        return file;
    }
}