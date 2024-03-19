import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_aaos/flutter_aaos.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _speed = '0.0';
  final _flutterAaosPlugin = FlutterAaos();
  List? carData;

  @override
  void initState() {
    super.initState();
    initPlatformState();
    listenSpeed();
  }

  getProperty() async {
    //555749208
    var prop = await _flutterAaosPlugin.getProperty(291504647, 0);
    print(prop);
  }

  getCarData() async {
    carData = await _flutterAaosPlugin.propertyList;

    for (var item in carData!) {
      int id = item["id"];
      Stream s = await _flutterAaosPlugin.listenProperty(id);
      s.listen((event) {
        setState(() {
          item["value"] = event.toString();
        });
      });
    }
  }

  listenSpeed() async {
    Stream s = await _flutterAaosPlugin.listenProperty(291504647);
    s.listen((event) {
      setState(() {
        _speed = event.toString();
      });
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _flutterAaosPlugin.platformVersion ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Running on: $_platformVersion\n'),
              Text('Speed on: $_speed\n'),
              Row(
                children: [
                  ElevatedButton(
                      onPressed: () {
                        getCarData();
                      },
                      child: const Text("Properties")),
                  ElevatedButton(
                      onPressed: () {
                        getProperty();
                      },
                      child: const Text("Property"))
                ],
              ),
              Wrap(children: [
                if (carData != null)
                  for (var item in carData!)
                    Container(
                      padding: const EdgeInsets.all(8),
                      child: Text(
                        "${item["name"]} : ${item["value"]}",
                      ),
                    )
              ]),
            ],
          ),
        ),
      ),
    );
  }
}
