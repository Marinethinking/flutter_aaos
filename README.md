# flutter_aaos

Android Automotive OS plugin

## How to use

### Run on emulator

1. Open Android studio
2. Tools -> SDK Manager: download Android 13 or any version with Android Automotive
3. Tools -> Device Manager: Add Device -> Select Hardware Automotive ...

### Code Sample

```dart
final _flutterAaosPlugin = FlutterAaos();
getCarData() async {
    // list all available properties 
    carData = await _flutterAaosPlugin.propertyList;

    for (var item in carData!) {
      int id = item["id"];
      // listen to the property value change
      Stream s = await _flutterAaosPlugin.listenProperty(id);
      s.listen((event) {
        setState(() {
          item["value"] = event.toString();
        });
      });
    }
  }
```

<img src="https://github.com/Marinethinking/flutter_aaos/blob/main/image.png?raw=true" alt="drawing" width="800"/>

## Android Config

Add permissions you need in android/src/main/AndroidManifest.xml
Example:

```xml
<uses-permission android:name="android.car.permission.CAR_POWERTRAIN" />
<uses-permission android:name="android.car.permission.CAR_SPEED" />
<uses-permission android:name="android.car.permission.CAR_ENERGY" />
```
