import 'dart:async';

import 'package:flutter/services.dart';

class FlutterAaos {
  static const MethodChannel methodChannel = MethodChannel('flutter_aaos');
  // static const EventChannel _carGearEventChannel = EventChannel('car_gear');
  // var properties = [VehicleProperty(id: 291504647,name:"Speed"),VehicleProperty(id: 291504648)];

  Future<String?> get platformVersion async {
    final String? version =
        await methodChannel.invokeMethod('getPlatformVersion');
    return version;
  }

  getProperty(int propertyId, int areaId) async {
    var prop = await methodChannel.invokeMethod(
        'getProperty', {"propertyId": propertyId, "areaId": areaId});
    return prop;
  }

  Future<List?> get propertyList async {
    final List? version = await methodChannel.invokeMethod('getPropertyList');
    return version;
  }

  Future<Stream> listenProperty(int id) async {
    await methodChannel.invokeMethod('listenProperty', {"propertyId": id});

    EventChannel eventChannel = EventChannel('$id');
    return eventChannel.receiveBroadcastStream();
  }
}

class VehicleProperty {
  int id;
  String name;
  dynamic value;
  VehicleProperty({required this.id, this.name = ""});
}
