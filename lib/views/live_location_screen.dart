import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../controllers/foreground_service_controller.dart';
import '../controllers/live_location_controller.dart';

class LiveLocationScreen extends StatelessWidget {
  final controller = Get.put(LiveLocationController());
  // final fgController = Get.put(ForegroundServiceController());

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
          // Padding(
          //   padding: const EdgeInsets.all(16.0),
          //   child: Obx(() => ElevatedButton.icon(
          //         icon: Icon(
          //           fgController.isServiceRunning.value
          //               ? Icons.stop_circle
          //               : Icons.play_circle_fill,
          //           color: Colors.white,
          //         ),
          //         onPressed: fgController.toggleService,
          //         label: Text(
          //             fgController.isServiceRunning.value
          //                 ? "Stop Foreground Service"
          //                 : "Start Foreground Service",
          //             style: TextStyle(color: Colors.white)),
          //         style: ElevatedButton.styleFrom(
          //           padding: const EdgeInsets.symmetric(
          //               horizontal: 20, vertical: 14),
          //           backgroundColor: fgController.isServiceRunning.value
          //               ? Colors.redAccent
          //               : Colors.blueAccent,
          //           shape: RoundedRectangleBorder(
          //             borderRadius: BorderRadius.circular(10),
          //           ),
          //         ),
          //       )),
          // ),
        ],
      ),
    );
  }
}
