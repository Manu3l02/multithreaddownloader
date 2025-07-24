package io.manuel.multithreaddownloader.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {

    private final String fileURL;
    private final String outputFile;
    private final int numThreads;

    public Downloader(String fileURL, String outputFile, int numThreads) {
        this.fileURL = fileURL;
        this.outputFile = outputFile;
        this.numThreads = numThreads;
    }

    public void start() throws IOException {
        File f = new File(outputFile);
        if (f.exists()) {
            System.out.println("⚠️ Il file '" + outputFile + "' esiste già. Download annullato.");
            return;
        }

        System.out.println("📥 Inizio download da: " + fileURL);
        System.out.println("💾 Salvataggio in: " + outputFile);
        System.out.println("🧵 Thread utilizzati: " + numThreads);

        URL url = new URL(fileURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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

            threads[i] = new Thread(new DownloadTask(fileURL, outputFile, start, end, i));
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

        System.out.println("🎉 Download completato: " + outputFile);
    }
}
