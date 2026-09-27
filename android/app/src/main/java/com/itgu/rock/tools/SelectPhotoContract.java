package com.itgu.rock.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


//这里只是指定协议，如何传递数据以及如何接受数据
public class SelectPhotoContract extends ActivityResultContract<Intent, Uri> {
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Intent intent) {
        return new Intent(Intent.ACTION_PICK).setType("image/*");
    }

    @Override
    public Uri parseResult(int i, @Nullable Intent intent) {
        return intent == null ? null : intent.getData();
    }
}
