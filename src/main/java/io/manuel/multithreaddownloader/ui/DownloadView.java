package io.manuel.multithreaddownloader.ui;

import io.manuel.multithreaddownloader.controller.DownloadControl;
import io.manuel.multithreaddownloader.controller.DownloadManager;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import io.manuel.multithreaddownloader.util.FavoritesManager;
import java.nio.file.Path;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

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
	private FavoritesManager favoritesManager = new FavoritesManager();
	private StackPane rootPane;
	private VBox dragOverlay;
	
	public DownloadView() {
		initUI();
	}

    private void initUI() {
        rootPane = new StackPane();
        VBox mainContent = new VBox();
        mainContent.setPadding(new Insets(15));
        mainContent.setSpacing(10);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);
        formGrid.setPadding(new Insets(5, 10, 5, 10));
        formGrid.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("MultiThread Downloader");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: darkslategray;");
        GridPane.setColumnSpan(titleLabel, 2);
        GridPane.setHalignment(titleLabel, HPos.LEFT);
        formGrid.add(titleLabel, 0, 0);

        urlField = new TextField();
        urlField.setPromptText("Inserisci URL del file da scaricare");
        Label dragHint = new Label("💡 Puoi anche trascinare qui un link per scaricarlo.");
        dragHint.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
        GridPane.setColumnSpan(urlField, 2);
        formGrid.add(urlField, 0, 1);
        formGrid.add(dragHint, 2, 1);

        threadSpinner = new Spinner<>(1, 16, 4);
        threadSpinner.setEditable(true);
        HBox threadBox = new HBox(10, new Label("Numero di thread:"), threadSpinner);
        threadBox.setAlignment(Pos.CENTER_LEFT);
        GridPane.setColumnSpan(threadBox, 2);
        formGrid.add(threadBox, 0, 2);

        fileNameField = new TextField();
        fileNameField.setPromptText("Nome file (opzionale)");
        GridPane.setColumnSpan(fileNameField, 2);
        formGrid.add(fileNameField, 0, 3, 2, 1);

        folderButton = new Button("Scegli cartella...");
        folderButton.setOnAction(e -> chooseFolder());
        folderLabel = new Label("Nessuna cartella selezionata");
        folderLabel.setMaxWidth(400);
        folderLabel.setWrapText(true);

        MenuButton favoritesMenu = new MenuButton("📂 Percorsi preferiti");
        updateFavoritesMenu(favoritesMenu);

        HBox folderSelectionBox = new HBox(10,
                favoritesMenu,
                folderButton,
                folderLabel);
        folderSelectionBox.setAlignment(Pos.CENTER_LEFT);
        GridPane.setColumnSpan(folderSelectionBox, 2);
        formGrid.add(folderSelectionBox, 0, 4, 2, 1);

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

        mainContent.getChildren().addAll(formGrid, threadProgressTitle, threadScrollPane, bottomSection);
        rootPane.getChildren().add(mainContent);

        dragOverlay = new VBox(10);
        dragOverlay.getStyleClass().add("drag-overlay");
        dragOverlay.setVisible(false);
        dragOverlay.setAlignment(Pos.CENTER);

        ImageView icon = new ImageView(new Image(
                getClass().getResource("/icons/file_plus.png").toExternalForm(),
                64, 64, true, true
        ));
        Label iconLabel = new Label("", icon);
        iconLabel.getStyleClass().add("drag-icon");

        Label textLabel = new Label("Inserire file qui");
        textLabel.getStyleClass().add("drag-text");

        dragOverlay.getChildren().addAll(iconLabel, textLabel);
        rootPane.getChildren().add(dragOverlay);

        getChildren().add(rootPane);

        rootPane.setOnDragOver(event -> {
            if (event.getGestureSource() != this &&
                event.getDragboard().hasString() &&
                event.getDragboard().getString().startsWith("http")) {
                dragOverlay.setVisible(true);
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        rootPane.setOnDragExited(event -> {
            dragOverlay.setVisible(false);
            event.consume();
        });

        rootPane.setOnDragDropped(event -> {
            String dropped = event.getDragboard().getString();
            if (dropped != null && dropped.startsWith("http")) {
                urlField.setText(dropped);
                showMessage("🔗 URL ricevuto tramite drag and drop.");
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            dragOverlay.setVisible(false);
            event.consume();
        });
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

		try {
			URL testURL = new URL(fileURL);
			
			boolean verified = false;
			int code = -1;
			
			// 1. Tentativo: HEAD
			try {
				HttpURLConnection conn = (HttpURLConnection) testURL.openConnection();
				conn.setRequestMethod("HEAD");
				conn.connect();
				code = conn.getResponseCode();
				conn.disconnect();
				verified = true;
			} catch (Exception e) {
				showMessage("⚠️ HEAD non supportato sal server, provo con GET...");
			}
				
			// 2. Fallback: Get
			if (!verified) {
				try {
					HttpURLConnection conn = (HttpURLConnection) testURL.openConnection();
					conn.setRequestProperty("Range", "bytes=0-0");
					conn.connect();
					code = conn.getResponseCode();
					conn.disconnect();
					verified = true;
				} catch (Exception e) {
					showMessage("⚠️ GET (Range) non supportato, procedo comunque al download...");
				}
			}
			
			// Se ricevo codice HTTP
			if (verified && code == 401) {
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
						if (selectedFolder != null) {
							favoritesManager.addFavorite(selectedFolder.toPath());
						}
						
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

	private void updateFavoritesMenu(MenuButton menu) {
		menu.getItems().clear();

		for (Path path : favoritesManager.loadFavorites()) {
			Label pathLabel = new Label(path.toString());
			Button removeBtn = new Button("❌");

			removeBtn.setOnAction(e -> {
				if (confirmRemoval(path)) {
					favoritesManager.removeFavorites(path);
					updateFavoritesMenu(menu);
					showMessage("❌ Rimosso: " + path);
				}
			});

			HBox itemBox = new HBox(10, pathLabel, removeBtn);
			itemBox.setAlignment(Pos.CENTER_LEFT);
			CustomMenuItem customItem = new CustomMenuItem(itemBox, false);

			customItem.setOnAction(e -> {
				selectedFolder = path.toFile();
				folderLabel.setText("⭐ " + path.toString());
			});

			menu.getItems().add(customItem);
		}

		Button addBtn = new Button("➕ Aggiungi percorso");
		addBtn.setMaxWidth(Double.MAX_VALUE);
		addBtn.setOnAction(e -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Scegli una cartella da aggiungere ai preferiti");
			File folder = chooser.showDialog(getScene().getWindow());
			if (folder != null) {
				favoritesManager.addFavorite(folder.toPath());
				updateFavoritesMenu(menu);
				showMessage("➕ Aggiunto nuovo preferito: " + folder.getAbsolutePath());
			}
		});

		CustomMenuItem addItem = new CustomMenuItem(addBtn, false);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(addItem);
	}
	
	private boolean confirmRemoval(Path path) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Conferma Rimozione");
		alert.setHeaderText("Sei sicuro di voler rimuovere questo percorso dai preferiti?");
		alert.setContentText(path.toString());

		ButtonType ok = new ButtonType("Conferma");
		ButtonType cancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(ok, cancel);

		return alert.showAndWait().orElse(cancel) == ok;
	}

	
	private void showMessage(String msg) {
		logArea.appendText(msg + "\n");
	}
}