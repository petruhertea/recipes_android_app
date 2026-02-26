package com.cookcraft.retrofit;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cookcraft.BuildConfig;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.101.13:8080/";
    private static final long CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int MAX_AGE_ONLINE = 60 * 5; // 5 minutes when online
    private static final int MAX_STALE_OFFLINE = 60 * 60 * 24 * 7; // 7 days offline


    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


            Cache cache = new Cache(context.getCacheDir(), CACHE_SIZE);
            httpClient.cache(cache).addInterceptor(new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request request = chain.request();

                    if (NetworkCheck.hasNetwork(context)) {
                        // Online: cache for 5 minutes
                        request = request.newBuilder()
                                .header("Cache-Control", "public, max-age=" + MAX_AGE_ONLINE)
                                .build();
                    } else {
                        // Offline: use cache for up to 7 days
                        request = request.newBuilder()
                                .header("Cache-Control", "public, only-if-cached, max-stale=" + MAX_STALE_OFFLINE)
                                .build();
                    }

                    return chain.proceed(request);
                }
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}