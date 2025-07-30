# 🧵 MultiThreadDownloaderFX

Un'applicazione Java per il download multi-threaded di file, dotata di interfaccia grafica JavaFX.  
Ogni thread scarica una parte del file in parallelo, migliorando le prestazioni ed esplorando la concorrenza in Java.

---

## 🚀 Funzionalità

- 📥 Scaricamento file via HTTP usando più thread (con `Range` per byte)
- 📊 Visualizzazione del progresso per ogni thread con barre dedicate
- 📶 Barra di avanzamento totale del download
- ⏸️ Possibilità di **mettere in pausa** e **riprendere** il download
- ⏹️ Possibilità di **annullare** il download in qualsiasi momento (con rimozione del file incompleto)
- 🧠 Scelta del numero di thread dalla GUI (da 1 a 16)
- 📂 Selezione della cartella di destinazione
- 🚫 Evita la sovrascrittura se il file esiste già (aggiunge suffisso)
- 🖥️ Interfaccia grafica moderna con JavaFX
- 📄 Log chiaro di ogni evento e progresso
- 💡 Architettura modulare: separazione netta tra logica di download, UI e controller

---

## 🛠️ Tecnologie utilizzate

- **Java 17**
- **JavaFX 21**
- **Maven** per la gestione delle dipendenze
- **JUnit 5** per i test (base)
- **Git** per il versionamento

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
│           │   ├── DownloadTask.java
│           │   └── ProgressListener.java
│           ├── controller/
│           │   ├── DownloadManager.java
│           │   └── DownloadControl.java
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
- JavaFX SDK disponibile (incluso via Maven o configurato manualmente)

### 🧪 Avvio da terminale:

```bash
mvn clean compile
mvn javafx:run
````

Oppure da IDE (es. Eclipse/IntelliJ): esegui `MainApp.java`.

---

## 🔭 Estensioni future (TODO)

* [x] Visualizzazione del progresso per ogni thread
* [x] Selettore del numero di thread
* [x] Selezione cartella di destinazione
* [x] Pausa / Ripresa del download
* [x] Annullamento e cancellazione file incompleto
* [ ] Supporto a HTTPS con autenticazione (token/cookie)
* [ ] Cronologia dei download effettuati
* [ ] Supporto a resume da file parziali dopo riavvio

---

## 🪪 Licenza

Questo progetto è open source e distribuito con licenza [MIT](LICENSE).

---

## ✍️ Autore

**Manuel**

* 🔗 [github.com/Manu3l02](https://github.com/Manu3l02)