import 'dart:async';
import 'package:flutter/services.dart';
import 'package:get/get.dart';

class StoredLocationController extends GetxController {
  static const MethodChannel _methodChannel = MethodChannel('native_sqlite_channel');

  var locations = <Map<String, dynamic>>[].obs;
  Timer? _pollingTimer;

  Future<void> fetchStoredLocations() async {
    try {
      final List<dynamic> result = await _methodChannel.invokeMethod('getStoredLocations');
      locations.value = result.map((e) => Map<String, dynamic>.from(e)).toList();
      update();
    } catch (e) {
      print("Error fetching stored locations: $e");
    }
  }

  Future<void> clearLocations() async {
    try {
      await _methodChannel.invokeMethod('clearStoredLocations');
      locations.clear();
      update();
    } catch (e) {
      print("Error clearing stored locations: $e");
    }
  }

  void startPolling({Duration interval = const Duration(seconds: 5)}) {
    _pollingTimer?.cancel();
    _pollingTimer = Timer.periodic(interval, (_) => fetchStoredLocations());
  }

  void stopPolling() {
    _pollingTimer?.cancel();
    _pollingTimer = null;
  }

  @override
  void onInit() {
    super.onInit();
    fetchStoredLocations();
    startPolling();
  }

  @override
  void onClose() {
    stopPolling();
    super.onClose();
  }
}
