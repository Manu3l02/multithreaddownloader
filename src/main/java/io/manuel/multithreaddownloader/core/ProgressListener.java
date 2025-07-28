package io.manuel.multithreaddownloader.core;

public interface ProgressListener {
    void onProgress(int threadId, double percent);
}
