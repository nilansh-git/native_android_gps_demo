import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../controllers/foreground_service_controller.dart';
import '../controllers/live_location_controller.dart';

class LiveLocationScreen extends StatelessWidget {
  final controller = Get.put(LiveLocationController());

  LiveLocationScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Live GPS"),
        centerTitle: true,
      ),
      body: Column(
        children: [
          Expanded(
            child: Obx(() {
              if (controller.locations.isEmpty) {
                return const Center(
                  child: Text(
                    "Waiting for GPS data...",
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
                    elevation: 4,
                    margin:
                        const EdgeInsets.symmetric(vertical: 6, horizontal: 4),
                    child: ListTile(
                      leading:
                          const Icon(Icons.my_location, color: Colors.green),
                      title: Text("Lat: ${loc['lat']}, Lng: ${loc['lng']}"),
                      subtitle: Text("Time: $formattedTime"),
                    ),
                  );
                },
              );
            }),
          ),
        ],
      ),
    );
  }
}
