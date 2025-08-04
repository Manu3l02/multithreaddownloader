package io.manuel.multithreaddownloader.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FavoritesManager {

	private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".multithreaddownloader");
	private static final Path FAVORITES_FILE = CONFIG_DIR.resolve("favorite.json");
	private static final Gson gson = new Gson();

	public FavoritesManager() {
		try {
			if (Files.notExists(CONFIG_DIR)) {
				Files.createDirectories(CONFIG_DIR);
			}
		} catch (IOException e) {
			System.err.println("❌ Errore nella creazione della cartella di configurazione: " + e.getMessage());
		}
	}

	public List<Path> loadFavorites() {
		if (Files.notExists(FAVORITES_FILE)) {
			return new ArrayList<Path>();
		}
		
		try (Reader reader = Files.newBufferedReader(FAVORITES_FILE)) {
			Type listType = new TypeToken<List<String>>() {}.getType();
			List<String> pathsAsStrings = gson.fromJson(reader, listType);
			
			List<Path> paths = new ArrayList<Path>();
			for (String s : pathsAsStrings) {
				paths.add(Paths.get(s));
			}
			return paths;
			
		} catch (IOException e) {
			System.err.println("❌ Errore durante la lettura dei progetti: " + e.getMessage());
			return new ArrayList<Path>();
		}
	}

	public void addFavorite(Path newPath) {
		List<Path> currentFavoites = loadFavorites();
		
		if (currentFavoites.contains(newPath)) {
			return;
		}
		
		if (currentFavoites.size() >= 3) {
			currentFavoites.remove(0);
		}
		
		currentFavoites.add(newPath);
		saveFavorites(currentFavoites);
	}

	private void saveFavorites(List<Path> paths) {
		List<String> toSave = new ArrayList<String>();
		for (Path path : paths) {
			toSave.add(path.toString());
		}
		
		try (Writer writer = Files.newBufferedWriter(FAVORITES_FILE)) {
			gson.toJson(toSave, writer);
		} catch (IOException e) {
			System.err.println("❌ Errore nel salvataggio dei preferiti: " + e.getMessage());
		}
	}

	public void removeFavorites(Path selected) {
		if (selected == null) {
			return;
		}
		
		List<Path> favorites = loadFavorites();
		if (favorites.removeIf(p -> p.equals(selected))) {
			saveFavorites(favorites);
		}
	}
}
