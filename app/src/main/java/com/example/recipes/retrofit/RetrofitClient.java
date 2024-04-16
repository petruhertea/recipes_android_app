package com.example.recipes.retrofit;

import android.content.Context;

import com.example.recipes.BuildConfig;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://" + BuildConfig.SERVER_ADDRESS + ":" + BuildConfig.SERVER_PORT;

    private static final long CACHE_SIZE = 10 * 1024 * 1024; // 10 MB cache size

    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Add caching to the OkHttpClient
            Cache cache = new Cache(context.getCacheDir(), CACHE_SIZE);
            httpClient.cache(cache);

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}