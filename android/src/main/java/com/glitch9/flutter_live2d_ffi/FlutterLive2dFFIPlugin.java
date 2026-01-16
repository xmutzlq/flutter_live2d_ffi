package com.glitch9.flutter_live2d_ffi;

import androidx.annotation.NonNull;

// Flutter 插件核心
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

// PlatformView 相关
import io.flutter.plugin.platform.PlatformViewRegistry;

// Android 基础
import android.content.res.AssetManager;
import android.content.Context;

// 文件操作
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// 日志
import android.util.Log;

// 注解
import androidx.annotation.NonNull;
import android.os.Environment;

/** FlutterLive2DFFIPlugin */
public class FlutterLive2DFFIPlugin implements FlutterPlugin, MethodCallHandler {
  private static final String CHANNEL = "flutter_live2d_ffi";
  private MethodChannel channel;
  private AssetManager assetManager;
 
  @Override
  public void onAttachedToEngine(FlutterPluginBinding binding) {
      channel = new MethodChannel(binding.getBinaryMessenger(), CHANNEL);
      channel.setMethodCallHandler(this);
      assetManager = binding.getApplicationContext().getAssets();

      PlatformViewRegistry registry = binding.getPlatformViewRegistry();
      registry.registerViewFactory("live2d_view", new Live2DPlatformViewFactory());
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {
      channel.setMethodCallHandler(null);
  }   

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
      if (call.method.equals("getAssetPath")) {
          String assetPath = call.argument("asset");
          String fullPath = copyAssetToCache(assetPath);
          result.success(fullPath);
      } else {
          result.notImplemented();
      }
  }

  private String copyAssetToCache(String assetName) {
      File cacheDir = new File(Environment.getExternalStorageDirectory(), "live2d_cache");
      if (!cacheDir.exists()) cacheDir.mkdirs();

      File outFile = new File(cacheDir, assetName);
      if (outFile.exists()) return outFile.getAbsolutePath();

      try (InputStream in = assetManager.open(assetName);
            OutputStream out = new FileOutputStream(outFile)) {
          byte[] buffer = new byte[1024];
          int read;
          while ((read = in.read(buffer)) != -1) {
              out.write(buffer, 0, read);
          }
      } catch (IOException e) {
          Log.e("FlutterLive2D", "Failed to copy asset: " + assetName, e);
          return null;
      }
      return outFile.getAbsolutePath();
  }
}
