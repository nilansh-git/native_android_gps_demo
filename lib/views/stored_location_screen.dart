import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../controllers/stored_location_controller.dart';

class StoredLocationScreen extends StatefulWidget {
  const StoredLocationScreen({super.key});

  @override
  State<StoredLocationScreen> createState() => _StoredLocationScreenState();
}

class _StoredLocationScreenState extends State<StoredLocationScreen> {
  final controller = Get.put(StoredLocationController());

  @override
  void initState() {
    super.initState();
    controller.fetchStoredLocations();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Stored Locations')),
      body: Column(
        children: [
          Expanded(
            child: RefreshIndicator(
              onRefresh: controller.fetchStoredLocations,
              child: GetBuilder<StoredLocationController>(
                builder: (_) {
                  if (controller.locations.isEmpty) {
                    return Center(
                      child: Text(
                        'No stored locations yet.',
                        style: TextStyle(fontSize: 16, color: Colors.grey),
                      ),
                    );
                  }

                  return ListView.builder(
                    padding: const EdgeInsets.all(8),
                    itemCount: controller.locations.length,
                    itemBuilder: (context, index) {
                      final loc = controller.locations[index];
                      final timestamp =
                          DateTime.fromMillisecondsSinceEpoch(loc['timestamp']);
                      final formattedTime =
                          "${timestamp.year}-${timestamp.month.toString().padLeft(2, '0')}-${timestamp.day.toString().padLeft(2, '0')} "
                          "${timestamp.hour.toString().padLeft(2, '0')}:${timestamp.minute.toString().padLeft(2, '0')}";

                      return Card(
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12)),
                        elevation: 3,
                        margin: const EdgeInsets.symmetric(
                            vertical: 6, horizontal: 4),
                        child: ListTile(
                          leading:
                              Icon(Icons.location_on, color: Colors.blueAccent),
                          title: Text("Lat: ${loc['lat']}, Lng: ${loc['lng']}"),
                          subtitle: Text("Time: $formattedTime"),
                        ),
                      );
                    },
                  );
                },
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12.0),
            child: ElevatedButton.icon(
              onPressed: () {
                controller.clearLocations();
              },
              icon: Icon(
                Icons.delete,
                color: Colors.white,
              ),
              label: Text(
                "Clear Stored Locations",
                style: TextStyle(color: Colors.white),
              ),
              style: ElevatedButton.styleFrom(
                padding: EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                backgroundColor: Colors.redAccent,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8)),
              ),
            ),
          )
        ],
      ),
    );
  }
}
