# 🧵 MultiThreadDownloaderFX

Un'applicazione Java per il download multi-threaded di file, dotata di interfaccia grafica JavaFX.  
Ogni thread scarica una parte del file in parallelo, migliorando le prestazioni ed esplorando la concorrenza in Java.

---

## 🚀 Funzionalità

- 📥 Scaricamento file via HTTP usando più thread (divisione per byte range)
- 🧪 Barra di avanzamento del download globale nella GUI
- 🔎 Verifica se il file è già presente per evitare sovrascrittura
- 🖥️ Interfaccia semplice creata con JavaFX
- 📄 Log chiaro del progresso di ogni thread
- 💡 Struttura modulare con separazione tra core e UI

---

## 🛠️ Tecnologie utilizzate

- **Java 17**
- **JavaFX 21**
- **Maven** per la gestione delle dipendenze
- **JUnit 5** per i test (base)
- **Git** per il controllo di versione

---

## 🧩 Struttura del progetto

<pre>
src/
├── main/
│   └── java/
│       └── io.manuel.multithreaddownloader
│           ├── MainApp.java
│           ├── core/
│           │   ├── Downloader.java
│           │   └── DownloadTask.java
│           └── ui/
│               └── DownloadView.java
└── test/
    └── java/
        └── io.manuel.multithreaddownloader.AppTest.java
</pre>

---

## ▶️ Come eseguire il progetto

### ⚙️ Requisiti:

- Java 17 installato
- Maven installato
- JavaFX SDK scaricato (oppure usato via Maven)

### 🧪 Avviare l'applicazione:

```bash
mvn clean compile
mvn javafx:run
```

oppure da IDE (Eclipse/IntelliJ): avvia MainApp.java

## 🔭 Estensioni future (TODO)

- Visualizzazione del progresso per ogni thread nella GUI

- Selettore del numero di thread nella GUI

- Selezione della cartella di salvataggio

- Pausa / Ripresa del download

- Supporto per download HTTPS con autenticazione (token, cookie)

## 🪪 Licenza

Questo progetto è open source e distribuito con licenza [MIT](LICENSE).

## ✍️ Autore

Manuel

- 🔗 github.com/Manu3l02
