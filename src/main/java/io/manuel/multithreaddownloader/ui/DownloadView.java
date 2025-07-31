package io.manuel.multithreaddownloader.ui;

import io.manuel.multithreaddownloader.controller.DownloadControl;
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
	private Button pauseButton;
	private Button resumeButton;
	private Button cancelButton;
	private DownloadControl control;

	public DownloadView() {
		initUI();
	}

	private void initUI() {
		setPadding(new Insets(15));
		setSpacing(10);

		GridPane formGrid = new GridPane();
		formGrid.setHgap(10);
		formGrid.setVgap(8);
		formGrid.setPadding(new Insets(5, 10, 5, 10));
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
		HBox threadBox = new HBox(10, new Label("Numero di thread:"), threadSpinner);
		threadBox.setAlignment(Pos.CENTER_LEFT);
		GridPane.setColumnSpan(threadBox, 2);
		formGrid.add(threadBox, 0, 2);

		// File Name Field
		fileNameField = new TextField();
		fileNameField.setPromptText("Nome file (opzionale)");
		GridPane.setColumnSpan(fileNameField, 2);
		formGrid.add(fileNameField, 0, 3, 2, 1);

		// Folder Chooser
		folderButton = new Button("Scegli cartella...");
		folderButton.setOnAction(e -> chooseFolder());
		folderLabel = new Label("Nessuna cartella selezionata");
		folderLabel.setMaxWidth(400);
		folderLabel.setWrapText(true);
		HBox folderSelectionBox = new HBox(10, folderButton, folderLabel);
		folderSelectionBox.setAlignment(Pos.CENTER_LEFT);
		GridPane.setColumnSpan(folderSelectionBox, 2);
		formGrid.add(folderSelectionBox, 0, 4, 2, 1);

		// Control Buttons
		downloadButton = new Button("Scarica");
		pauseButton = new Button("⏸️ Pausa");
		resumeButton = new Button("▶️ Riprendi");
		cancelButton = new Button("⏹️ Annulla");

		pauseButton.setDisable(true);
		resumeButton.setDisable(true);
		cancelButton.setDisable(true);

		downloadButton.setOnAction(e -> startDownload());

		pauseButton.setOnAction(e -> {
			if (control != null) {
				control.pause();
				showMessage("⏸️ Download in pausa.");
				pauseButton.setDisable(true);
				resumeButton.setDisable(false);
			}
		});

		resumeButton.setOnAction(e -> {
			if (control != null) {
				control.resume();
				showMessage("▶️ Download ripreso.");
				pauseButton.setDisable(false);
				resumeButton.setDisable(true);
			}
		});

		cancelButton.setOnAction(e -> {
			if (control != null) {
				control.cancel();
				showMessage("⏹️ Download in fase di annullamento...");
				pauseButton.setDisable(true);
				resumeButton.setDisable(true);
				cancelButton.setDisable(true);
				downloadButton.setDisable(false);
			}
		});

		HBox controlButtons = new HBox(10, downloadButton, pauseButton, resumeButton, cancelButton);
		GridPane.setColumnSpan(controlButtons, 2);
		GridPane.setHalignment(controlButtons, HPos.LEFT);
		formGrid.add(controlButtons, 0, 5, 2, 1);

		// -------- Thread Progress --------
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

		// -------- Bottom Section --------
		VBox bottomSection = new VBox(8);
		bottomSection.setPadding(new Insets(10));
		bottomSection.setAlignment(Pos.TOP_LEFT);
		VBox.setMargin(bottomSection, new Insets(0, 0, 10, 0));

		Label totalProgressTitle = new Label("Progresso totale:");
		totalProgressTitle.setStyle("-fx-font-weight: bold;");
		progressBar = new ProgressBar(0);
		progressBar.setMaxWidth(Double.MAX_VALUE);

		Label logTitle = new Label("Log:");
		logTitle.setStyle("-fx-font-weight: bold;");
		logArea = new TextArea();
		logArea.setEditable(false);
		logArea.setPrefRowCount(3);
		logArea.setWrapText(true);
		VBox.setVgrow(logArea, Priority.ALWAYS);

		bottomSection.getChildren().addAll(totalProgressTitle, progressBar, logTitle, logArea);

		getChildren().addAll(formGrid, threadProgressTitle, threadScrollPane, bottomSection);
	}

	private void startDownload() {
		String fileURL = urlField.getText().trim();
		String fileName = fileNameField.getText().trim();
		String username = "";
		String password = "";

		if (fileURL.isEmpty()) {
			showMessage("Inserisci un URL valido.");
			return;
		}

		// Verifica se è richiesta autenticazione
		try {
			java.net.URL testURL = new java.net.URL(fileURL);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) testURL.openConnection();
			conn.setRequestMethod("HEAD");
			conn.connect();
			int code = conn.getResponseCode();
			conn.disconnect();

			if (code == 401) {
				// Richiede autenticazione
				String[] credentials = promptForCredentials();
				if (credentials == null) {
					showMessage("🔒 Download annullato: credenziali non fornite.");
					return;
				}
				username = credentials[0];
				password = credentials[1];
			}
		} catch (Exception e) {
			showMessage("❌ Errore durante la verifica dell'autenticazione: " + e.getMessage());
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

		control = new DownloadControl();
		pauseButton.setDisable(false);
		resumeButton.setDisable(true);
		cancelButton.setDisable(false);

		DownloadManager manager = new DownloadManager();
		manager.startDownload(fileURL, fileName, selectedFolder, threads, control, username, password,
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
						pauseButton.setDisable(true);
						resumeButton.setDisable(true);
						cancelButton.setDisable(true);
					}

					@Override
					public void onError(String errorMessage) {
						showMessage("❌ Errore: " + errorMessage);
						downloadButton.setDisable(false);
						pauseButton.setDisable(true);
						resumeButton.setDisable(true);
						cancelButton.setDisable(true);
					}
				});
	}

	private String[] promptForCredentials() {
		Dialog<String[]> dialog = new Dialog<>();
		dialog.setTitle("Autenticazione richiesta");
		dialog.setHeaderText("Questo link richiede autenticazione");

		Label userLabel = new Label("Username:");
		Label passLabel = new Label("Password:");
		TextField userField = new TextField();
		PasswordField passField = new PasswordField();

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.add(userLabel, 0, 0);
		grid.add(userField, 1, 0);
		grid.add(passLabel, 0, 1);
		grid.add(passField, 1, 1);

		dialog.getDialogPane().setContent(grid);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK) {
				return new String[] { userField.getText(), passField.getText() };
			}
			return null;
		});

		return dialog.showAndWait().orElse(null);
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