package de.jr.smtweaks.schulmanagerAPI;

import android.content.Context;

import de.jr.smtweaks.UserData;

public interface API {

    void fetchData(UserData userData, String token, Context context, OnFinishedUpdateRequest listener);

    interface OnFinishedUpdateRequest {
        void onFinishedUpdateRequest(String result);
    }
}
