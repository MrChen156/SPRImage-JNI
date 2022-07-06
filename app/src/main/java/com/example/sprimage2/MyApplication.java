package com.example.sprimage2;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
}
