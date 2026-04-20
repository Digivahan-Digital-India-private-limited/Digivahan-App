package com.digivahan.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * AppExecutors helps manage background threads for disk IO, network IO,
 * and posting results to the main thread.
 */
public class AppExecutors {

    private static volatile AppExecutors instance;

    private final Executor diskIO;
    private final Executor networkIO;
    private final Executor mainThread;

    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors(
                            Executors.newSingleThreadExecutor(),   // Disk I/O (DB, File)
                            Executors.newFixedThreadPool(3),       // Network I/O
                            new MainThreadExecutor()               // UI Thread
                    );
                }
            }
        }
        return instance;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    /**
     * Executor for running tasks on the Android main thread
     */
    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}

// HOW TO USE

/*AppExecutors.getInstance().networkIO().execute(() -> {
        // Perform API call or DB query in background

        AppExecutors.getInstance().mainThread().execute(() -> {
        // Update LiveData or UI on main thread
        });
        });*/

