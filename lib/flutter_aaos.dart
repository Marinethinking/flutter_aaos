import 'dart:async';

import 'package:flutter/services.dart';

class FlutterAaos {
  static const MethodChannel _channel = MethodChannel('flutter_aaos');
  static const EventChannel _carGearEventChannel = EventChannel('car_gear');

  Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Stream get currentCarGear => _carGearEventChannel.receiveBroadcastStream();
}
