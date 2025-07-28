package io.manuel.multithreaddownloader.ui;

import io.manuel.multithreaddownloader.core.Downloader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadView extends VBox {

    private TextField urlField;
    private Button downloadButton;
    private ProgressBar progressBar;
    private TextArea logArea;

    public DownloadView() {
        initUI();
    }

    private void initUI() {
        setPadding(new Insets(20));
        setSpacing(10);

        Label titleLabel = new Label("MultiThread Downloader");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: darkslategray;");

        urlField = new TextField();
        urlField.setPromptText("Inserisci URL del file da scaricare");

        downloadButton = new Button("Scarica");
        downloadButton.setOnAction(e -> startDownload());

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        getChildren().addAll(titleLabel, urlField, downloadButton, progressBar, new Label("Log:"), logArea);
    }

    private void startDownload() {
        String fileURL = urlField.getText().trim();
        if (fileURL.isEmpty()) {
            showMessage("Inserisci un URL valido.");
            return;
        }

        try {
            URL url = new URL(fileURL);

            String path = url.getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);

            if (fileName.isEmpty()) {
                fileName = "multithread_downloader_tmgdw";
            } else {
                int queryIdx = fileName.indexOf('?');
                if (queryIdx != -1) {
                    fileName = fileName.substring(0, queryIdx);
                }

                int dotIdx = fileName.lastIndexOf('.');
                if (dotIdx != -1) {
                    String base = fileName.substring(0, dotIdx);
                    String ext = fileName.substring(dotIdx);
                    fileName = base + "_tmgdw" + ext;
                } else {
                    fileName = fileName + "_tmgdw";
                }
            }

            File outputFile = new File(fileName);
            int count = 1;
            while (outputFile.exists()) {
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    String base = fileName.substring(0, dotIndex);
                    String ext = fileName.substring(dotIndex);
                    outputFile = new File(base + "(" + count + ")" + ext);
                } else {
                    outputFile = new File(fileName + "(" + count + ")");
                }
                count++;
            }

            String output = outputFile.getName();

            int threads = 4;

            downloadButton.setDisable(true);
            logArea.clear();
            showMessage("Avvio download: " + output);

            new Thread(() -> {
                try {
                    Downloader downloader = new Downloader(fileURL, output, threads);
                    downloader.start();
                    Platform.runLater(() -> {
                        progressBar.setProgress(1.0);
                        showMessage("Download completato!");
                        downloadButton.setDisable(false);
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showMessage("Errore: " + e.getMessage());
                        downloadButton.setDisable(false);
                    });
                }
            }).start();

        } catch (Exception e) {
            showMessage("URL non valido: " + e.getMessage());
        }
    }

    private void showMessage(String msg) {
        logArea.appendText(msg + "\n");
    }
}