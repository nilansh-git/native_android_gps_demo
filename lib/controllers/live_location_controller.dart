import 'package:flutter/services.dart';
import 'package:get/get.dart';

class LiveLocationController extends GetxController {
  static const EventChannel _eventChannel = EventChannel('native_location_stream');

  var locations = <Map<String, dynamic>>[].obs;

  @override
  void onInit() {
    super.onInit();

    _eventChannel.receiveBroadcastStream().listen((event) {
      if (event is Map) {
        locations.add({
          'lat': event['lat'],
          'lng': event['lng'],
          'timestamp': event['timestamp'],
        });
      }
    });
  }
}