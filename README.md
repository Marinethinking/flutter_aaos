# flutter_aaos

Android Automotive OS plugin

## How to use

### Run on emulator

1. Open Android studio
2. Tools -> SDK Manager: download Android 13 or any version with Android Automotive
3. Tools -> Device Mnager: Add Device -> Select Hardware Automotive ...

### Code Sample

```dart
final _flutterAaosPlugin = FlutterAaos();
_flutterAaosPlugin.currentCarGear.listen((event) {
      setState(() {
        _speed = event.toString();
      });
    });
```
