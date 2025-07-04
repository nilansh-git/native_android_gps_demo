import 'package:flutter/services.dart';
import 'package:get/get.dart';

class ForegroundServiceController extends GetxController {
  static const _channel = MethodChannel('foreground_service_channel');
  var isServiceRunning = false.obs;

  Future<void> toggleService() async {
    if (isServiceRunning.value) {
      await _channel.invokeMethod("stopService");
      isServiceRunning.value = false;
    } else {
      await _channel.invokeMethod("startService");
      isServiceRunning.value = true;
    }
  }
}