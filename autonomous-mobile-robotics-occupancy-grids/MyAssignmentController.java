import com.cyberbotics.webots.controller.Supervisor;

public class MyAssignmentController {

    public static void main(String[] args) {
    
        Supervisor robot = new Supervisor();

        int timeStep = (int) Math.round(robot.getBasicTimeStep());

        PioneerSimpleProxSensors pps = new PioneerSimpleProxSensors(robot);
        PioneerCLNav nav = new PioneerCLNav(robot, pps);
        OccupancyGrid ogrid = new OccupancyGrid(robot, 10, "display", nav.get_real_pose(), pps);

        // Set initial exploration goal
        Pose initialGoal = new Pose(0, 3, 0);
        nav.set_goal(initialGoal);

        boolean explorationComplete = false;

        // Main control loop
        while (robot.step(timeStep) != -1) {

            // Update navigation
            if (!explorationComplete) {
                explorationComplete = nav.update(); // If the goal is reached, stop navigation
            } else {
                // Find the next unexplored area
                Pose nextGoal = findNextGoal(ogrid, nav.get_real_pose());
                if (nextGoal == null) {
                    System.out.println("Exploration complete!");
                    break;
                } else {
                    nav.set_goal(nextGoal);
                    explorationComplete = false; // Reset to continue exploring
                }
            }

            // Update the occupancy grid and display it
            ogrid.map(nav.get_real_pose());
            ogrid.paint();
        }

        // Ensure motors stop after exit
        nav.update();
    }

    /**
     * Finds the next goal for exploration based on unexplored areas and avoiding obstacles.
     *
     * @param ogrid The occupancy grid
     * @param currentPose The robot's current pose
     * @return The next goal pose or null if no unexplored area is found
     */
    private static Pose findNextGoal(OccupancyGrid ogrid, Pose currentPose) {
        int numRows = ogrid.get_num_row_cells();
        int numCols = ogrid.get_num_col_cells();
        double unexploredThreshold = 0.3; // Threshold for unexplored cells
        double obstacleThreshold = 0.6; // Threshold to avoid obstacles

        Pose bestGoal = null;
        double minProximityToObstacles = Double.MAX_VALUE;

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                int cellIndex = i * numCols + j;
                double cellProbability = ogrid.get_cell_probability(cellIndex);

                // Consider only unexplored areas
                if (cellProbability < unexploredThreshold) {
                    double cellX = (j - numCols / 2) * (ogrid.get_arena_width() / numCols);
                    double cellY = (numRows / 2 - i) * (ogrid.get_arena_height() / numRows);
                    Pose candidatePose = new Pose(cellX, cellY, 0);

                    // Check the proximity of this candidate cell to obstacles
                    double proximityToObstacles = getProximityToObstacles(ogrid, i, j, obstacleThreshold);
                    if (proximityToObstacles > 0 && proximityToObstacles < minProximityToObstacles) {
                        bestGoal = candidatePose;
                        minProximityToObstacles = proximityToObstacles;
                    }
                }
            }
        }

        return bestGoal;
    }

    /**
     * Calculates the proximity of a cell to obstacles by checking neighboring cells.
     *
     * @param ogrid The occupancy grid
     * @param row The row index of the cell
     * @param col The column index of the cell
     * @param obstacleThreshold The probability threshold for considering a cell as an obstacle
     * @return The minimum proximity to an obstacle, or Double.MAX_VALUE if no obstacles are nearby
     */
    private static double getProximityToObstacles(OccupancyGrid ogrid, int row, int col, double obstacleThreshold) {
        int numRows = ogrid.get_num_row_cells();
        int numCols = ogrid.get_num_col_cells();
        double minDistance = Double.MAX_VALUE;

        // Check neighboring cells
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int neighborRow = row + i;
                int neighborCol = col + j;

                // Ensure the neighbour is within bounds
                if (neighborRow >= 0 && neighborRow < numRows && neighborCol >= 0 && neighborCol < numCols) {
                    int neighborIndex = neighborRow * numCols + neighborCol;
                    double neighborProbability = ogrid.get_cell_probability(neighborIndex);

                    // If the neighbouring cell is an obstacle, calculate distance
                    if (neighborProbability > obstacleThreshold) {
                        double distance = Math.sqrt(i * i + j * j);
                        minDistance = Math.min(minDistance, distance);
                    }
                }
            }
        }

        return minDistance;
    }
}
