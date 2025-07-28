package io.manuel.multithreaddownloader.ui;

import io.manuel.multithreaddownloader.core.Downloader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadView extends VBox {

    private TextField urlField;
    private Button downloadButton;
    private ProgressBar progressBar;
    private TextArea logArea;
    private TextField fileNameField;
    private Button folderButton;
    private Label folderLabel;
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
        logArea.setPrefHeight(200);

        getChildren().addAll(
        	    titleLabel,
        	    urlField,
        	    fileNameField,
        	    folderButton,
        	    folderLabel,
        	    downloadButton,
        	    progressBar,
        	    new Label("Log:"),
        	    logArea
        	);

    }

    private void startDownload() {
        String fileURL = urlField.getText().trim();
        if (fileURL.isEmpty()) {
            showMessage("Inserisci un URL valido.");
            return;
        }

        try {
            URL url = new URL(fileURL);

            String fileNameInput = fileNameField.getText().trim();
            String outputFileName;

            if (!fileNameInput.isEmpty()) {
                outputFileName = fileNameInput;
                if (!outputFileName.contains(".")) {
                    String path = url.getPath();
                    if (path.contains(".")) {
                        outputFileName += path.substring(path.lastIndexOf("."));
                    }
                }
            } else {
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

                outputFileName = fileName;
            }

            File targetDir = (selectedFolder != null) ? selectedFolder : new File(".");
            File outputFile = new File(targetDir, outputFileName);

            int count = 1;
            while (outputFile.exists()) {
                String baseName = outputFileName;
                String ext = "";
                int dotIdx = outputFileName.lastIndexOf('.');
                if (dotIdx != -1) {
                    baseName = outputFileName.substring(0, dotIdx);
                    ext = outputFileName.substring(dotIdx);
                }
                outputFile = new File(targetDir, baseName + "(" + count + ")" + ext);
                count++;
            }

            String output = outputFile.getAbsolutePath();

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