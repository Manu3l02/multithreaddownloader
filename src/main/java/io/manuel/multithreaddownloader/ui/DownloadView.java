package io.manuel.multithreaddownloader.ui;

import io.manuel.multithreaddownloader.controller.DownloadManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class DownloadView extends VBox {

    private TextField urlField;
    private TextField fileNameField;
    private Button folderButton;
    private Label folderLabel;
    private Button downloadButton;
    private ProgressBar progressBar;
    private TextArea logArea;
    private VBox threadProgressContainer;
    private ProgressBar[] threadBars;
    private File selectedFolder;

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

        fileNameField = new TextField();
        fileNameField.setPromptText("Nome file (opzionale)");

        folderButton = new Button("Scegli cartella...");
        folderLabel = new Label("Nessuna cartella selezionata");
        folderButton.setOnAction(e -> chooseFolder());

        downloadButton = new Button("Scarica");
        downloadButton.setOnAction(e -> startDownload());

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setMaxHeight(100);
        logArea.setPrefRowCount(3);

        threadProgressContainer = new VBox(5);
        threadProgressContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(threadProgressContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);

        getChildren().addAll(
            titleLabel,
            urlField,
            fileNameField,
            folderButton,
            folderLabel,
            downloadButton,
            progressBar,
            new Label("Log:"),
            logArea,
            scrollPane
        );
    }

    private void startDownload() {
        String fileURL = urlField.getText().trim();
        String fileName = fileNameField.getText().trim();

        if (fileURL.isEmpty()) {
            showMessage("Inserisci un URL valido.");
            return;
        }

        int threads = 4;
        threadBars = new ProgressBar[threads];
        threadProgressContainer.getChildren().clear();

        for (int i = 0; i < threads; i++) {
            ProgressBar bar = new ProgressBar(0);
            bar.setPrefWidth(400);
            threadBars[i] = bar;
            threadProgressContainer.getChildren().add(new Label("Thread " + i));
            threadProgressContainer.getChildren().add(bar);
        }

        downloadButton.setDisable(true);
        logArea.clear();

        DownloadManager manager = new DownloadManager();
        manager.startDownload(
            fileURL,
            fileName,
            selectedFolder,
            threads,
            new DownloadManager.Callback() {
                @Override
                public void onLog(String message) {
                    showMessage(message);
                }

                @Override
                public void onProgress(int threadId, double percent, double average) {
                    if (threadId >= 0 && threadId < threadBars.length) {
                        threadBars[threadId].setProgress(percent);
                    }
                    progressBar.setProgress(average);
                }

                @Override
                public void onComplete() {
                    showMessage("✅ Download completato!");
                    downloadButton.setDisable(false);
                }

                @Override
                public void onError(String errorMessage) {
                    showMessage("❌ Errore: " + errorMessage);
                    downloadButton.setDisable(false);
                }
            }
        );
    }

    private void chooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Scegli una cartella di destinazione");
        File folder = chooser.showDialog(getScene().getWindow());
        if (folder != null) {
            selectedFolder = folder;
            folderLabel.setText("📁 " + folder.getAbsolutePath());
        }
    }

    private void showMessage(String msg) {
        logArea.appendText(msg + "\n");
    }
}