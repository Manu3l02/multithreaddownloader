package io.manuel.multithreaddownloader;

import io.manuel.multithreaddownloader.ui.DownloadView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("MultiThread Downloader FX");
		
		DownloadView view = new DownloadView();
		
		Scene scene = new Scene(view, 600, 400);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) throws Exception {
		launch(args);
	}

}
