package com.mt.flutter_aaos

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyConfig
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.property.CarPropertyManager.CarPropertyEventCallback
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener


class FlutterAaosPlugin : FlutterPlugin,
    ActivityAware, MethodCallHandler, RequestPermissionsResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private var methodChannel: MethodChannel? = null
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private var channels = arrayListOf<EventChannel>()
    private var flutterPluginBinding: FlutterPluginBinding? = null
    private var activityPluginBinding: ActivityPluginBinding? = null


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android " + Build.VERSION.RELEASE)
        } else if (call.method == "getPropertyList") {
            var props = getProperties()
            result.success(props)

        } else if (call.method == "listenProperty") {
            val propertyId = call.argument<Int>("propertyId")
            listenProperty(propertyId)
            result.success("Listening to property ${propertyId} ")
        } else {
            result.notImplemented()
        }
    }

    private fun getProperties(): Any {

        val clazz = Class.forName("android.car.VehiclePropertyIds")
        val fields = clazz.declaredFields

        var props = carPropertyManager!!.propertyList
        var propList = mutableListOf<Map<String, Any>>()
        for (prop in props) {
            val field = fields.find { it.get(null) == prop.propertyId } ?: continue
            println("${field.name} : ${prop.propertyId}")
            propList.add(mapOf("id" to prop.propertyId, "name" to field.name))
        }
        return propList
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        flutterPluginBinding = null
    }

    private fun listenProperty(propertyId: Int?) {
        if (propertyId == null) return
        val channel = EventChannel(flutterPluginBinding!!.binaryMessenger, propertyId.toString())
        channels.add(channel)
        println(VehiclePropertyIds.PERF_VEHICLE_SPEED)
        channel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventSink) {
                carPropertyManager!!.registerCallback(object : CarPropertyEventCallback {
                    override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
                        try {
                            events.success(carPropertyValue.value.toString())
                        } catch (e: Exception) {
                            Log.e("MainActivity:", "Error: ${e.message}")
                        }

                    }

                    override fun onErrorEvent(i: Int, i1: Int) {
                        Log.d(
                            "MainActivity:",
                            "Received error car property event, propId=$i"
                        )
                    }
                }, propertyId, CarPropertyManager.SENSOR_RATE_NORMAL)
                Log.d("MainActivity:", "listening")
            }


            override fun onCancel(arguments: Any?) {
                Log.d("MainActivity:", "cancel")
            }
        })
    }


    private fun setupChannels(context: Context, messenger: BinaryMessenger) {
//    ActivityCompat.requestPermissions(activityPluginBinding.getActivity(),new String[] {
//            Car.PERMISSION_POWERTRAIN
//    },0);
        ActivityCompat.requestPermissions(
            activityPluginBinding!!.activity, arrayOf(
                Car.PERMISSION_SPEED,
                Car.PERMISSION_ENERGY,
                Car.PERMISSION_CAR_INFO,
                Car.PERMISSION_POWERTRAIN,
            ), 0
        )
        car = Car.createCar(context)
        carPropertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
//        val carPropertyManagerChannel = EventChannel(messenger, "car_gear")
//        channels.add(carPropertyManagerChannel);
//        carPropertyManagerChannel.setStreamHandler(object : EventChannel.StreamHandler {
//            override fun onListen(arguments: Any?, events: EventSink) {
//                carPropertyManager!!.registerCallback(object : CarPropertyEventCallback {
//                    override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
//                        events.success(carPropertyValue.value)
//                    }
//
//                    override fun onErrorEvent(i: Int, i1: Int) {
//                        Log.d(
//                            "MainActivity:",
//                            "Received error car property event, propId=$i"
//                        )
//                    }
//                }, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_NORMAL)
//                Log.d("MainActivity:", "listening")
//            }
//
//            override fun onCancel(arguments: Any?) {
//                Log.d("MainActivity:", "cancel")
//            }
//        })
        methodChannel = MethodChannel(messenger, "flutter_aaos")
        methodChannel!!.setMethodCallHandler(this)
    }

    private fun teardownEventChannels() {
        methodChannel!!.setMethodCallHandler(null)

        for (c in channels) c.setStreamHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityPluginBinding = binding
        setupChannels(
            flutterPluginBinding!!.applicationContext,
            flutterPluginBinding!!.binaryMessenger
        )
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        teardownEventChannels()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        return if (permissions[0] === Car.PERMISSION_SPEED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            true
        } else true
    }
}
