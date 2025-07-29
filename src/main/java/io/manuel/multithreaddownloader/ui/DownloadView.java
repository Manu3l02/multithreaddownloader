package io.manuel.multithreaddownloader.ui;

import io.manuel.multithreaddownloader.controller.DownloadManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
    private Spinner<Integer> threadSpinner;
    private ScrollPane threadScrollPane;

    public DownloadView() {
        initUI();
    }

    private void initUI() {
        setPadding(new Insets(15));
        setSpacing(10);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);
        formGrid.setPadding(new Insets(10));
        formGrid.setAlignment(Pos.TOP_LEFT);

        // Titolo
        Label titleLabel = new Label("MultiThread Downloader");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: darkslategray;");
        GridPane.setColumnSpan(titleLabel, 2);
        GridPane.setHalignment(titleLabel, HPos.LEFT);
        formGrid.add(titleLabel, 0, 0);

        // URL Field
        urlField = new TextField();
        urlField.setPromptText("Inserisci URL del file da scaricare");
        GridPane.setColumnSpan(urlField, 2);
        formGrid.add(urlField, 0, 1);

        // Thread Spinner
        threadSpinner = new Spinner<>(1, 16, 4);
        threadSpinner.setEditable(true);
        // HBox per allineare la label e lo spinner
        HBox threadBox = new HBox(10, new Label("Numero di thread:"), threadSpinner);
        threadBox.setAlignment(Pos.CENTER_LEFT);
        GridPane.setColumnSpan(threadBox, 2);
        formGrid.add(threadBox, 0, 2);

        // File Name Field
        fileNameField = new TextField();
        fileNameField.setPromptText("Nome file (opzionale)");
        GridPane.setColumnSpan(fileNameField, 2);
        formGrid.add(fileNameField, 0, 3);

        // Folder Chooser
        folderButton = new Button("Scegli cartella...");
        folderButton.setOnAction(e -> chooseFolder());
        folderLabel = new Label("Nessuna cartella selezionata");
        folderLabel.setMaxWidth(400);
        folderLabel.setWrapText(true);

        
        // HBox per allineare il bottone e la label della cartella
        HBox folderSelectionBox = new HBox(10, folderButton, folderLabel);
        folderSelectionBox.setAlignment(Pos.CENTER_LEFT);
        GridPane.setColumnSpan(folderSelectionBox, 2);
        formGrid.add(folderSelectionBox, 0, 4);

        // Download Button
        downloadButton = new Button("Scarica");
        downloadButton.setOnAction(e -> startDownload());
        
        GridPane.setColumnSpan(downloadButton, 2);
        GridPane.setHalignment(downloadButton, HPos.LEFT);
        formGrid.add(downloadButton, 0, 5);


        // ---------- Sezione Centrale: Thread Progress Bars ScrollPane ----------
        Label threadProgressTitle = new Label("Progresso per thread:");
        threadProgressTitle.setStyle("-fx-font-weight: bold;");
        VBox.setMargin(threadProgressTitle, new Insets(0, 0, 5, 10));

        threadProgressContainer = new VBox(5);
        threadProgressContainer.setPadding(new Insets(10));
        threadProgressContainer.setAlignment(Pos.TOP_LEFT);

        threadScrollPane = new ScrollPane(threadProgressContainer);
        threadScrollPane.setFitToWidth(true);
        threadScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        threadScrollPane.setPrefHeight(150);
        VBox.setMargin(threadScrollPane, new Insets(0, 10, 0, 10));

        // ---------- Sezione Inferiore: Progress Bar Principale + Log ----------
        VBox bottomSection = new VBox(8);
        bottomSection.setPadding(new Insets(10, 10, 10, 10));
        bottomSection.setAlignment(Pos.TOP_LEFT);
        VBox.setMargin(bottomSection, new Insets(0, 0, 10, 0));

        Label totalProgressTitle = new Label("Progresso totale:");
        totalProgressTitle.setStyle("-fx-font-weight: bold;");
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(progressBar, Priority.NEVER);

        Label logTitle = new Label("Log:");
        logTitle.setStyle("-fx-font-weight: bold;");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(3);
        logArea.setWrapText(true);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        bottomSection.getChildren().addAll(
                totalProgressTitle,
                progressBar,
                logTitle,
                logArea
        );

        getChildren().addAll(
                formGrid,
                threadProgressTitle,
                threadScrollPane,
                bottomSection
        );
    }

    private void startDownload() {
        String fileURL = urlField.getText().trim();
        String fileName = fileNameField.getText().trim();

        if (fileURL.isEmpty()) {
            showMessage("Inserisci un URL valido.");
            return;
        }

        int threads = threadSpinner.getValue();
        threadBars = new ProgressBar[threads];
        threadProgressContainer.getChildren().clear();

        for (int i = 0; i < threads; i++) {
            ProgressBar bar = new ProgressBar(0);
            bar.setMaxWidth(Double.MAX_VALUE);
            threadBars[i] = bar;
            threadProgressContainer.getChildren().add(new Label("Thread " + i + ":"));
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