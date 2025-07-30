package io.manuel.multithreaddownloader.controller;

public class DownloadControl {

	private volatile boolean paused = false;
	private volatile boolean cancelled = false;

	public synchronized void pause() {
		this.paused = true;
	}

	public synchronized void resume() {
		this.paused = false;
		notifyAll();
	}

	public synchronized void waitIfPaused() throws InterruptedException {
		while (paused) {
			wait();
		}
	}

	public synchronized void cancel() {
		this.cancelled = true;
		this.paused = false;
		notifyAll();
	}

	public boolean isCancelled() {
		return cancelled;
	}
}