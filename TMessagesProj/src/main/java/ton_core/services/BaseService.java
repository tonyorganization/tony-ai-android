package ton_core.services;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ton_core.models.BaseResponse;

public class BaseService {

    protected Context context;

    public static final ExecutorService apiCallExecutor =
            Executors.newFixedThreadPool(5);

    protected static <T> void handle(Call<BaseResponse<T>> call, IOnApiCallback<T> callback) {
        call.enqueue(new Callback<BaseResponse<T>>() {

            @Override
            public void onResponse(@NonNull Call<BaseResponse<T>> call, @NonNull Response<BaseResponse<T>> response) {
                try {
                    if (!response.isSuccessful()) {
                        callback.onError("HTTP " + response.code());
                        return;
                    }

                    BaseResponse<T> body = response.body();
                    if (body == null) {
                        callback.onError("Empty response body");
                        return;
                    }

                    if (body.success) {
                        callback.onSuccess(body.data);
                    } else {
                        callback.onError(body.message != null ? body.message : "Unknown error");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<T>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    protected static <T> void handleWithoutBaseResponse(Call<T> call, IOnApiCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                try {
                    if (!response.isSuccessful()) {
                        callback.onError("HTTP " + response.code());
                        return;
                    }

                    T body = response.body();
                    if (body == null) {
                        callback.onError("Empty response body");
                        return;
                    }
                    callback.onSuccess(body);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
