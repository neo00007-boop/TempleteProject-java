package com.lib.base.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.permissions.Permission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 保存图片、视频到系统相册。
 * <p>
 * Android 10 及以上用 MediaStore，不需要存储权限。
 * Android 9 及以下写入公共目录并扫描，需要 {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE}。
 * 权限用 {@link PermissionUtil#requestPermission} 自己申请，回调里再保存。
 * Async 方法返回 {@link Single}，只负责 io 线程写文件、主线程给结果。
 * 页面里再 {@code compose(RxUtils.toSimpleSingle(this))} 绑生命周期。订阅结束前不要 recycle bitmap。
 */
public final class GallerySaveUtil {

    public static final int REASON_OK = 0;
    public static final int REASON_PERMISSION = 1;
    public static final int REASON_ARGUMENT = 2;
    public static final int REASON_IO = 3;

    private static final int BUFFER = 8 * 1024;

    private GallerySaveUtil() {
    }

    public static boolean needsWritePermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }

    @NonNull
    public static String writePermission() {
        return Permission.WRITE_EXTERNAL_STORAGE;
    }

    @NonNull
    public static Result saveBitmap(@NonNull Context context, @Nullable Bitmap bitmap) {
        return saveBitmap(context, bitmap, null, null);
    }

    @NonNull
    public static Result saveBitmap(@NonNull Context context, @Nullable Bitmap bitmap, @Nullable String displayName) {
        return saveBitmap(context, bitmap, displayName, null);
    }

    @NonNull
    public static Result saveBitmap(@NonNull Context context, @Nullable Bitmap bitmap,
                                    @Nullable String displayName, @Nullable String album) {
        if (bitmap == null || bitmap.isRecycled()) {
            return Result.fail(REASON_ARGUMENT, "图片为空");
        }
        String mime = mimeForBitmap(bitmap, displayName);
        ByteArrayOutputHolder holder = new ByteArrayOutputHolder();
        Bitmap.CompressFormat format = compressFormat(mime);
        int quality = format == Bitmap.CompressFormat.PNG ? 100 : 92;
        if (!bitmap.compress(format, quality, holder.stream)) {
            return Result.fail(REASON_IO, "图片压缩失败");
        }
        return saveBytes(context, holder.stream.toByteArray(), displayName, mime, album);
    }

    @NonNull
    public static Result saveBytes(@NonNull Context context, @Nullable byte[] bytes,
                                   @Nullable String displayName, @Nullable String mimeType) {
        return saveBytes(context, bytes, displayName, mimeType, null);
    }

    @NonNull
    public static Result saveBytes(@NonNull Context context, @Nullable byte[] bytes,
                                   @Nullable String displayName, @Nullable String mimeType, @Nullable String album) {
        if (bytes == null || bytes.length == 0) {
            return Result.fail(REASON_ARGUMENT, "文件内容为空");
        }
        return save(context, displayName, mimeType, album, out -> out.write(bytes));
    }

    @NonNull
    public static Result saveFile(@NonNull Context context, @Nullable File file) {
        return saveFile(context, file, null, null);
    }

    @NonNull
    public static Result saveFile(@NonNull Context context, @Nullable File file, @Nullable String album) {
        return saveFile(context, file, null, album);
    }

    /**
     * 读完后会关闭 {@code input}。
     */
    @NonNull
    public static Result saveStream(@NonNull Context context, @Nullable InputStream input,
                                    @Nullable String displayName, @Nullable String mimeType) {
        try {
            return saveStream(context, input, displayName, mimeType, null);
        } finally {
            close(input);
        }
    }

    @NonNull
    public static Result saveUri(@NonNull Context context, @Nullable Uri uri) {
        return saveUri(context, uri, null);
    }

    @NonNull
    public static Result saveUri(@NonNull Context context, @Nullable Uri uri, @Nullable String album) {
        if (uri == null) {
            return Result.fail(REASON_ARGUMENT, "文件地址为空");
        }
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            String path = uri.getPath();
            if (path == null || path.length() == 0) {
                return Result.fail(REASON_ARGUMENT, "文件地址为空");
            }
            return saveFile(context, new File(path), album);
        }
        ContentResolver resolver = context.getContentResolver();
        String mime = resolver.getType(uri);
        String name = queryDisplayName(resolver, uri);
        InputStream input = null;
        try {
            input = resolver.openInputStream(uri);
            if (input == null) {
                return Result.fail(REASON_IO, "打不开要保存的文件");
            }
            return saveStream(context, input, name, mime, album);
        } catch (SecurityException e) {
            return Result.fail(REASON_PERMISSION, "没有读取该文件的权限");
        } catch (IOException e) {
            return Result.fail(REASON_IO, message(e, "读取文件失败"));
        } finally {
            close(input);
        }
    }

    @NonNull
    public static Single<Result> saveBitmapAsync(@NonNull Context context, @Nullable Bitmap bitmap) {
        return saveBitmapAsync(context, bitmap, null, null);
    }

    @NonNull
    public static Single<Result> saveBitmapAsync(@NonNull Context context, @Nullable Bitmap bitmap, @Nullable String displayName) {
        return saveBitmapAsync(context, bitmap, displayName, null);
    }

    /**
     * 订阅结束前不要 recycle 这张 bitmap。
     */
    @NonNull
    public static Single<Result> saveBitmapAsync(@NonNull Context context, @Nullable Bitmap bitmap,
                                                @Nullable String displayName, @Nullable String album) {
        return saveRx(() -> saveBitmap(context, bitmap, displayName, album));
    }

    @NonNull
    public static Single<Result> saveBytesAsync(@NonNull Context context, @Nullable byte[] bytes,
                                               @Nullable String displayName, @Nullable String mimeType) {
        return saveRx(() -> saveBytes(context, bytes, displayName, mimeType));
    }

    @NonNull
    public static Single<Result> saveFileAsync(@NonNull Context context, @Nullable File file) {
        return saveRx(() -> saveFile(context, file));
    }

    @NonNull
    public static Single<Result> saveUriAsync(@NonNull Context context, @Nullable Uri uri) {
        return saveRx(() -> saveUri(context, uri));
    }

    @NonNull
    private static Result saveFile(@NonNull Context context, @Nullable File file,
                                   @Nullable String mimeType, @Nullable String album) {
        if (file == null || !file.isFile()) {
            return Result.fail(REASON_ARGUMENT, "文件不存在");
        }
        String mime = mimeType;
        if (mime == null || mime.length() == 0) {
            mime = mimeFromName(file.getName());
        }
        final String finalMime = mime;
        FileInputStream input;
        try {
            input = new FileInputStream(file);
        } catch (IOException e) {
            return Result.fail(REASON_IO, message(e, "读取文件失败"));
        }
        try {
            return saveStream(context, input, file.getName(), finalMime, album);
        } finally {
            close(input);
        }
    }

    @NonNull
    private static Result saveStream(@NonNull Context context, @Nullable InputStream input,
                                     @Nullable String displayName, @Nullable String mimeType, @Nullable String album) {
        if (input == null) {
            return Result.fail(REASON_ARGUMENT, "文件内容为空");
        }
        return save(context, displayName, mimeType, album, out -> {
            byte[] buffer = new byte[BUFFER];
            int count;
            while ((count = input.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        });
    }

    @NonNull
    private static Result save(@NonNull Context context, @Nullable String displayName,
                               @Nullable String mimeType, @Nullable String album, @NonNull Writer writer) {
        String mime = normalizeMime(mimeType, displayName);
        if (!isImage(mime) && !isVideo(mime)) {
            return Result.fail(REASON_ARGUMENT, "只支持图片和视频");
        }
        String name = normalizeName(displayName, mime);
        String folder = normalizeAlbum(context, album);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return saveByMediaStore(context.getApplicationContext(), name, mime, folder, writer);
        }
        return saveByPublicFile(context.getApplicationContext(), name, mime, folder, writer);
    }

    @NonNull
    private static Single<Result> saveRx(@NonNull Callable<Result> task) {
        return Single.fromCallable(task)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(error -> Result.fail(REASON_IO, message(error, "写入相册失败")));
    }

    @NonNull
    private static Result saveByMediaStore(@NonNull Context context, @NonNull String displayName,
                                           @NonNull String mime, @NonNull String album, @NonNull Writer writer) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = baseValues(displayName, mime);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath(mime, album));
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        Uri item = null;
        OutputStream out = null;
        try {
            item = insertMedia(resolver, mime, values);
            if (item == null) {
                return Result.fail(REASON_IO, "写入相册失败");
            }
            out = resolver.openOutputStream(item);
            if (out == null) {
                deleteQuietly(resolver, item);
                return Result.fail(REASON_IO, "写入相册失败");
            }
            writer.write(out);
            out.flush();
            ContentValues ready = new ContentValues();
            ready.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(item, ready, null, null);
            if (!readable(resolver, item)) {
                deleteQuietly(resolver, item);
                return Result.fail(REASON_IO, "写入相册失败");
            }
            return Result.ok(item);
        } catch (SecurityException e) {
            deleteQuietly(resolver, item);
            return Result.fail(REASON_PERMISSION, "没有保存到相册的权限");
        } catch (Exception e) {
            deleteQuietly(resolver, item);
            return Result.fail(REASON_IO, message(e, "写入相册失败"));
        } finally {
            close(out);
        }
    }

    @SuppressWarnings("deprecation")
    @NonNull
    private static Result saveByPublicFile(@NonNull Context context, @NonNull String displayName,
                                           @NonNull String mime, @NonNull String album, @NonNull Writer writer) {
        if (!isWriteGranted(context)) {
            return Result.fail(REASON_PERMISSION, "需要存储权限才能保存到相册");
        }
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Result.fail(REASON_IO, "存储不可用");
        }
        String root = isVideo(mime) ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES;
        File dir = new File(Environment.getExternalStoragePublicDirectory(root), album);
        if (!dir.exists() && !dir.mkdirs()) {
            return Result.fail(REASON_IO, "创建相册目录失败");
        }
        File target = uniqueFile(dir, displayName);
        OutputStream out = null;
        try {
            out = new FileOutputStream(target);
            writer.write(out);
            out.flush();
        } catch (Exception e) {
            close(out);
            out = null;
            //noinspection ResultOfMethodCallIgnored
            target.delete();
            return Result.fail(REASON_IO, message(e, "写入相册失败"));
        } finally {
            close(out);
        }
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = baseValues(target.getName(), mime);
        values.put(MediaStore.MediaColumns.DATA, target.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, target.length());
        Uri item = null;
        try {
            item = resolver.insert(collection(mime), values);
        } catch (Exception ignored) {
            item = null;
        }
        MediaScannerConnection.scanFile(context, new String[]{target.getAbsolutePath()}, new String[]{mime}, null);
        if (item == null) {
            return Result.fail(REASON_IO, "写入相册失败");
        }
        return Result.ok(item);
    }

    @NonNull
    private static ContentValues baseValues(@NonNull String displayName, @NonNull String mime) {
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
        values.put(MediaStore.MediaColumns.DATE_ADDED, now / 1000);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, now / 1000);
        if (isImage(mime)) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, now);
        }
        return values;
    }

    @Nullable
    private static Uri insertMedia(@NonNull ContentResolver resolver, @NonNull String mime, @NonNull ContentValues values) {
        try {
            Uri item = resolver.insert(collection(mime), values);
            if (item != null) {
                return item;
            }
        } catch (Exception ignored) {
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null;
        }
        Uri legacy = isVideo(mime)
                ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        try {
            return resolver.insert(legacy, values);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean readable(@NonNull ContentResolver resolver, @NonNull Uri uri) {
        InputStream input = null;
        try {
            input = resolver.openInputStream(uri);
            return input != null && input.read() >= 0;
        } catch (Exception ignored) {
            return false;
        } finally {
            close(input);
        }
    }

    @NonNull
    private static Uri collection(@NonNull String mime) {
        if (isVideo(mime)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            }
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @NonNull
    private static String relativePath(@NonNull String mime, @NonNull String album) {
        String root = isVideo(mime) ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES;
        return root + "/" + album + "/";
    }

    private static boolean isWriteGranted(@NonNull Context context) {
        return PermissionUtil.isGranted(context, writePermission());
    }

    @Nullable
    private static String queryDisplayName(@NonNull ContentResolver resolver, @NonNull Uri uri) {
        android.database.Cursor cursor = null;
        try {
            cursor = resolver.query(uri, new String[]{android.provider.OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    @NonNull
    private static String normalizeMime(@Nullable String mimeType, @Nullable String displayName) {
        if (mimeType != null) {
            String mime = mimeType.trim().toLowerCase(Locale.US);
            int semi = mime.indexOf(';');
            if (semi >= 0) {
                mime = mime.substring(0, semi).trim();
            }
            if (mime.indexOf('/') > 0) {
                return mime;
            }
        }
        String guessed = mimeFromName(displayName);
        return guessed == null ? "" : guessed;
    }

    @Nullable
    private static String mimeFromName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        String lower = name.toLowerCase(Locale.US);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".heic") || lower.endsWith(".heif")) {
            return "image/heic";
        }
        if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }
        if (lower.endsWith(".mp4")) {
            return "video/mp4";
        }
        if (lower.endsWith(".mov")) {
            return "video/quicktime";
        }
        if (lower.endsWith(".3gp")) {
            return "video/3gpp";
        }
        if (lower.endsWith(".mkv")) {
            return "video/x-matroska";
        }
        if (lower.endsWith(".webm")) {
            return "video/webm";
        }
        return null;
    }

    @NonNull
    private static String mimeForBitmap(@NonNull Bitmap bitmap, @Nullable String displayName) {
        String guessed = mimeFromName(displayName);
        if ("image/png".equals(guessed) || "image/jpeg".equals(guessed) || "image/webp".equals(guessed)) {
            return guessed;
        }
        return bitmap.hasAlpha() ? "image/png" : "image/jpeg";
    }

    @SuppressWarnings("deprecation")
    @NonNull
    private static Bitmap.CompressFormat compressFormat(@NonNull String mime) {
        if ("image/png".equals(mime)) {
            return Bitmap.CompressFormat.PNG;
        }
        if ("image/webp".equals(mime)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return Bitmap.CompressFormat.WEBP_LOSSY;
            }
            return Bitmap.CompressFormat.WEBP;
        }
        return Bitmap.CompressFormat.JPEG;
    }

    @NonNull
    private static String extension(@NonNull String mime) {
        if ("image/png".equals(mime)) {
            return ".png";
        }
        if ("image/gif".equals(mime)) {
            return ".gif";
        }
        if ("image/webp".equals(mime)) {
            return ".webp";
        }
        if ("image/heic".equals(mime)) {
            return ".heic";
        }
        if ("image/bmp".equals(mime)) {
            return ".bmp";
        }
        if ("video/mp4".equals(mime)) {
            return ".mp4";
        }
        if ("video/quicktime".equals(mime)) {
            return ".mov";
        }
        if ("video/3gpp".equals(mime)) {
            return ".3gp";
        }
        if ("video/x-matroska".equals(mime)) {
            return ".mkv";
        }
        if ("video/webm".equals(mime)) {
            return ".webm";
        }
        if (isVideo(mime)) {
            return ".mp4";
        }
        return ".jpg";
    }

    @NonNull
    private static String normalizeName(@Nullable String displayName, @NonNull String mime) {
        String name = sanitize(displayName);
        String ext = extension(mime);
        if (name.length() == 0) {
            String prefix = isVideo(mime) ? "VID_" : "IMG_";
            return prefix + System.currentTimeMillis() + ext;
        }
        int dot = name.lastIndexOf('.');
        if (dot <= 0) {
            return limit(name + ext);
        }
        return limit(name);
    }

    @NonNull
    private static String normalizeAlbum(@NonNull Context context, @Nullable String album) {
        String name = sanitize(album);
        if (name.length() == 0) {
            CharSequence label = context.getApplicationInfo().loadLabel(context.getPackageManager());
            name = sanitize(label == null ? "" : label.toString());
        }
        return name.length() == 0 ? "Pictures" : name;
    }

    @NonNull
    private static String sanitize(@Nullable String name) {
        if (name == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length() && builder.length() < 80; i++) {
            char c = name.charAt(i);
            if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?'
                    || c == '"' || c == '<' || c == '>' || c == '|' || c < 32) {
                continue;
            }
            builder.append(c);
        }
        return builder.toString().trim();
    }

    @NonNull
    private static String limit(@NonNull String name) {
        if (name.length() <= 120) {
            return name;
        }
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot < name.length() - 8) {
            return name.substring(0, 120);
        }
        String ext = name.substring(dot);
        return name.substring(0, 120 - ext.length()) + ext;
    }

    @NonNull
    private static File uniqueFile(@NonNull File dir, @NonNull String name) {
        File file = new File(dir, name);
        if (!file.exists()) {
            return file;
        }
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        return new File(dir, base + "_" + System.currentTimeMillis() + ext);
    }

    private static boolean isImage(@NonNull String mime) {
        return mime.startsWith("image/");
    }

    private static boolean isVideo(@NonNull String mime) {
        return mime.startsWith("video/");
    }

    private static void deleteQuietly(@NonNull ContentResolver resolver, @Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            resolver.delete(uri, null, null);
        } catch (Exception ignored) {
        }
    }

    private static void close(@Nullable java.io.Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    @NonNull
    private static String message(@NonNull Throwable e, @NonNull String fallback) {
        String text = e.getMessage();
        return text == null || text.length() == 0 ? fallback : text;
    }

    private interface Writer {
        void write(@NonNull OutputStream out) throws IOException;
    }

    /**
     * 压缩结果先落在内存流里，避免直接依赖未公开的实现细节。
     */
    private static final class ByteArrayOutputHolder {
        final java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
    }

    public static final class Result {
        public final boolean success;
        @Nullable
        public final Uri uri;
        public final int reason;
        @NonNull
        public final String message;

        private Result(boolean success, @Nullable Uri uri, int reason, @NonNull String message) {
            this.success = success;
            this.uri = uri;
            this.reason = reason;
            this.message = message;
        }

        @NonNull
        static Result ok(@NonNull Uri uri) {
            return new Result(true, uri, REASON_OK, "");
        }

        @NonNull
        static Result fail(int reason, @NonNull String message) {
            return new Result(false, null, reason, message);
        }
    }
}
