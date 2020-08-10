package com.pichillilorenzo.flutter_inappwebview;

import android.util.Log;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebStorage;
import android.webkit.WebStorage.Origin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MyWebStorage implements MethodChannel.MethodCallHandler {

  static final String LOG_TAG = "MyWebStorage";

  public static MethodChannel channel;
  public static WebStorage webStorageManager;

  public MyWebStorage(BinaryMessenger messenger) {
    channel = new MethodChannel(messenger, "com.pichillilorenzo/flutter_inappwebview_webstoragemanager");
    channel.setMethodCallHandler(this);
    webStorageManager = WebStorage.getInstance();
  }

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    switch (call.method) {
      case "getOrigins":
        getOrigins(result);
        break;
      case "deleteAllData":
        webStorageManager.deleteAllData();
        result.success(true);
        break;
      case "deleteOrigin":
        {
          String origin = (String) call.argument("origin");
          webStorageManager.deleteOrigin(origin);
        }
        result.success(true);
        break;
      case "getQuotaForOrigin":
        {
          String origin = (String) call.argument("origin");
          getQuotaForOrigin(origin, result);
        }
        break;
      case "getUsageForOrigin":
       {
          String origin = (String) call.argument("origin");
         getUsageForOrigin(origin, result);
       }
       break;
      default:
        result.notImplemented();
    }
  }

  public void getOrigins(final MethodChannel.Result result) {
    webStorageManager.getOrigins(new ValueCallback<Map>() {
      @Override
      public void onReceiveValue(Map value) {
        List<Map<String, Object>> origins = new ArrayList<>();
        for(Object key : value.keySet()) {
          Origin originObj = (Origin) value.get(key);

          Map<String, Object> originInfo = new HashMap<>();
          originInfo.put("origin", originObj.getOrigin());
          originInfo.put("quota", originObj.getQuota());
          originInfo.put("usage", originObj.getUsage());

          origins.add(originInfo);
        }
        result.success(origins);
      }
    });
  }

  public void getQuotaForOrigin(String origin, final MethodChannel.Result result) {
    webStorageManager.getQuotaForOrigin(origin, new ValueCallback<Long>() {
      @Override
      public void onReceiveValue(Long value) {
        result.success(value);
      }
    });
  }

  public void getUsageForOrigin(String origin, final MethodChannel.Result result) {
    webStorageManager.getUsageForOrigin(origin, new ValueCallback<Long>() {
      @Override
      public void onReceiveValue(Long value) {
        result.success(value);
      }
    });
  }

  public void dispose() {
    channel.setMethodCallHandler(null);
  }
}
