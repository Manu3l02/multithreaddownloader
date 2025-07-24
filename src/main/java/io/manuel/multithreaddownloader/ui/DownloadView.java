package io.manuel.multithreaddownloader.ui;

import io.manuel.multithreaddownloader.core.Downloader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;

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
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

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

        String output = "download_from_gui.bin";
        int threads = 4;

        downloadButton.setDisable(true);
        logArea.clear();
        showMessage("Avvio download...");

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
    }

    private void showMessage(String msg) {
        logArea.appendText(msg + "\n");
    }
}