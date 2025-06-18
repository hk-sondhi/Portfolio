# Autonomous Mobile Robotics - Occupancy Grids (Java)

## Overview
This project implements core components of an autonomous mobile robot navigation system in Java. It simulates or controls a robot navigating through a 2D environment using proximity sensors and an occupancy grid for environment mapping.

## Features
- **Occupancy Grid Mapping**: Maintains a 2D representation of known/unknown space using sensor data.
- **Robot Navigation**: Pioneer-based robot navigation logic via `PioneerCLNav`.
- **Pose Estimation**: Tracks the robot's position and orientation using the `Pose` class.
- **Proximity Sensing**: Processes basic proximity data from simulated sensors.
- **Control Architecture**: Modular control structure with `MyAssignmentController` as the main controller logic.

## Technologies Used
- Java (object-oriented design)
- Custom simulation or interface with Pioneer robot simulator
- Grid-based mapping logic


## How to Run
1. Open the project in a Java IDE (e.g., Eclipse or IntelliJ).
2. Compile all `.java` files.
3. Run `MyAssignmentController.java` as the main entry point.
4. Visualize the occupancy grid or review logs for robot behavior.

## Notes
- The project is structured for educational use (e.g., university robotics coursework).
- The robot’s behavior can be extended by modifying controller logic or grid update algorithms.
- Sensor input handling can be adapted for real-world sensors or simulators.

## Author
hk-sondhi
