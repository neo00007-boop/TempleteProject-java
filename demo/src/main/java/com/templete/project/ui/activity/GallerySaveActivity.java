package com.templete.project.ui.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;

import com.hjq.permissions.OnPermissionCallback;
import com.lib.base.rxjava.RxUtils;
import com.lib.base.ui.activity.BaseActivity;
import com.lib.base.util.GallerySaveUtil;
import com.lib.base.util.PermissionUtil;
import com.templete.project.databinding.ActivityGallerySaveBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * 保存图片、缓存文件到系统相册。
 */
public class GallerySaveActivity extends BaseActivity<ActivityGallerySaveBinding> {

    private Bitmap mBitmap;

    @Override
    protected ActivityGallerySaveBinding viewBinding() {
        return ActivityGallerySaveBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("保存到相册");
    }

    @Override
    public void initView() {
        mBitmap = Bitmap.createBitmap(900, 480, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        canvas.drawColor(Color.parseColor("#3D7EFF"));
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(64);
        canvas.drawText("GallerySaveUtil", 48, 220, paint);
        paint.setTextSize(40);
        canvas.drawText(String.valueOf(System.currentTimeMillis()), 48, 300, paint);
        mViewBinding.ivPreview.setImageBitmap(mBitmap);
    }

    @Override
    public void initEvent() {
        mViewBinding.tvSaveBitmap.setOnClickListener(v -> saveBitmap());
        mViewBinding.tvSaveFile.setOnClickListener(v -> saveFile());
    }

    @Override
    public void initData() {
    }

    private void saveBitmap() {
        runAfterPermission(() -> GallerySaveUtil.saveBitmapAsync(this, mBitmap, "demo_gallery.jpg", "TempleteDemo")
                .compose(RxUtils.toSimpleSingle(this))
                .subscribe(this::showResult, throwable -> showError(throwable)));
    }

    private void saveFile() {
        runAfterPermission(() -> {
            File file = new File(getCacheDir(), "demo_gallery_file.jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                if (!mBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)) {
                    showErrorText("图片压缩失败");
                    return;
                }
            } catch (Exception e) {
                showErrorText(e.getMessage() == null ? "写缓存失败" : e.getMessage());
                return;
            }
            GallerySaveUtil.saveFileAsync(this, file)
                    .compose(RxUtils.toSimpleSingle(this))
                    .subscribe(this::showResult, throwable -> showError(throwable));
        });
    }

    private void runAfterPermission(@NonNull Runnable action) {
        if (!GallerySaveUtil.needsWritePermission()
                || PermissionUtil.isGranted(this, GallerySaveUtil.writePermission())) {
            action.run();
            return;
        }
        PermissionUtil.requestPermission(this, new OnPermissionCallback() {
            @Override
            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                if (allGranted) {
                    action.run();
                } else {
                    showErrorText("需要存储权限才能保存到相册");
                }
            }

            @Override
            public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                if (doNotAskAgain) {
                    PermissionUtil.startPermissionActivity(GallerySaveActivity.this, permissions);
                }
                showErrorText("需要存储权限才能保存到相册");
            }
        }, GallerySaveUtil.writePermission());
    }

    private void showResult(@NonNull GallerySaveUtil.Result result) {
        if (result.success) {
            Uri uri = result.uri;
            mViewBinding.tvResult.setText("成功\n" + uri);
            logD("GallerySave", "saved " + uri);
        } else {
            mViewBinding.tvResult.setText("失败 " + result.reason + " " + result.message);
            logD("GallerySave", "fail " + result.reason + " " + result.message);
        }
    }

    private void showError(@NonNull Throwable throwable) {
        showErrorText(throwable.getMessage() == null ? "保存失败" : throwable.getMessage());
    }

    private void showErrorText(@NonNull String message) {
        mViewBinding.tvResult.setText(message);
        toast(message);
    }
}
