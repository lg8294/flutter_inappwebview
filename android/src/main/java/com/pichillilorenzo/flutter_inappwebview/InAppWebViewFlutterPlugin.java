package com.pichillilorenzo.flutter_inappwebview;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;

import com.pichillilorenzo.flutter_inappwebview.InAppWebView.FlutterWebViewFactory;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.platform.PlatformViewRegistry;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterView;

public class InAppWebViewFlutterPlugin implements FlutterPlugin, ActivityAware {

  protected static final String LOG_TAG = "InAppWebViewFlutterPL";

  public static InAppBrowserManager inAppBrowserManager;
  public static HeadlessInAppWebViewManager headlessInAppWebViewManager;
  public static ChromeSafariBrowserManager chromeSafariBrowserManager;
  public static InAppWebViewStatic inAppWebViewStatic;
  public static MyCookieManager myCookieManager;
  public static CredentialDatabaseHandler credentialDatabaseHandler;
  public static MyWebStorage myWebStorage;
  public static ValueCallback<Uri> filePathCallbackLegacy;
  public static ValueCallback<Uri[]> filePathCallback;

  public InAppWebViewFlutterPlugin() {}

  public static void registerWith(PluginRegistry.Registrar registrar) {
    final InAppWebViewFlutterPlugin instance = new InAppWebViewFlutterPlugin();
    Shared.registrar = registrar;
    instance.onAttachedToEngine(
            registrar.context(), registrar.messenger(), registrar.activity(), registrar.platformViewRegistry(), registrar.view());
  }

  @Override
  public void onAttachedToEngine(FlutterPluginBinding binding) {
    Shared.flutterAssets = binding.getFlutterAssets();

    // Shared.activity could be null or not.
    // It depends on who is called first between onAttachedToEngine event and onAttachedToActivity event.
    //
    // See https://github.com/pichillilorenzo/flutter_inappwebview/issues/390#issuecomment-647039084
    onAttachedToEngine(
            binding.getApplicationContext(), binding.getBinaryMessenger(), Shared.activity, binding.getPlatformViewRegistry(), null);
  }

  private void onAttachedToEngine(Context applicationContext, BinaryMessenger messenger, Activity activity, PlatformViewRegistry platformViewRegistry, FlutterView flutterView) {

    Shared.applicationContext = applicationContext;
    Shared.activity = activity;
    Shared.messenger = messenger;

    inAppBrowserManager = new InAppBrowserManager(messenger);
    headlessInAppWebViewManager = new HeadlessInAppWebViewManager(messenger);
    chromeSafariBrowserManager = new ChromeSafariBrowserManager(messenger);

    platformViewRegistry.registerViewFactory(
                    "com.pichillilorenzo/flutter_inappwebview", new FlutterWebViewFactory(messenger, flutterView));
    inAppWebViewStatic = new InAppWebViewStatic(messenger);
    myCookieManager = new MyCookieManager(messenger);
    myWebStorage = new MyWebStorage(messenger);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      credentialDatabaseHandler = new CredentialDatabaseHandler(messenger);
    }

    //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。

    QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

      @Override
      public void onViewInitFinished(boolean arg0) {
        // TODO Auto-generated method stub
        //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
        Log.d("app", " onViewInitFinished is " + arg0);
      }

      @Override
      public void onCoreInitFinished() {
        // TODO Auto-generated method stub
      }
    };
    //x5内核初始化接口
    QbSdk.initX5Environment(applicationContext,  cb);
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {
    if (inAppBrowserManager != null) {
      inAppBrowserManager.dispose();
      inAppBrowserManager = null;
    }
    if (headlessInAppWebViewManager != null) {
      headlessInAppWebViewManager.dispose();
      headlessInAppWebViewManager = null;
    }
    if (chromeSafariBrowserManager != null) {
      chromeSafariBrowserManager.dispose();
      chromeSafariBrowserManager = null;
    }
    if (myCookieManager != null) {
      myCookieManager.dispose();
      myCookieManager = null;
    }
    if (myWebStorage != null) {
      myWebStorage.dispose();
      myWebStorage = null;
    }
    if (credentialDatabaseHandler != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      credentialDatabaseHandler.dispose();
      credentialDatabaseHandler = null;
    }
    if (inAppWebViewStatic != null) {
      inAppWebViewStatic.dispose();
      inAppWebViewStatic = null;
    }
    filePathCallbackLegacy = null;
    filePathCallback = null;
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
    Shared.activityPluginBinding = activityPluginBinding;
    Shared.activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    Shared.activityPluginBinding = null;
    Shared.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
    Shared.activityPluginBinding = activityPluginBinding;
    Shared.activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    Shared.activityPluginBinding = null;
    Shared.activity = null;
  }
}
