import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'views/live_location_screen.dart';
import 'views/stored_location_screen.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      title: 'GPS Native Demo',
      home: HomeScreen(),
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Flutter Native GPS')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              child: Text('Live GPS Updates'),
              onPressed: () {
                Get.to(() => LiveLocationScreen());
              },
            ),
            SizedBox(height: 20),
            ElevatedButton(
              child: Text('Stored GPS (SQLite)'),
              onPressed: () {
                Get.to(() => StoredLocationScreen());
              },
            ),
          ],
        ),
      ),
    );
  }
}