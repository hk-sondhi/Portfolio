// PioneerCLNav.java
/*
 * PioneerNavigation Class Definition
 * File: PioneerCLNav.java
 * Date: 9th Nov 2024
 * Description: More Advanced Navigation Class support (2024)
 * Author: Terry Payne (trp@liv.ac.uk)
 */
 
 // ==============================================================
// COMP329 2024 Programming Assignment
// ==============================================================
// 
// The aim of the assignment is to move the robot around the arena
// in such a way as to generate an occupancy grid map of the arena
// itself.  Full details can be found on CANVAS for COMP329
//
// This copy of the navigator class is the same as that downloaded from
// COMP329 Lab Tutorial 6.  If you have completed the lab, then replace this
// file with your completed version.
//
// You can also develop a completely different implementation of the
// navigator class; however, ensure that it implements the method
//
//      public final Pose get_real_pose()
//
// The easiest thing is to reuse the implementation of this method
// from this class.
// ==============================================================

 
import com.cyberbotics.webots.controller.Motor;
import com.cyberbotics.webots.controller.Supervisor;
import com.cyberbotics.webots.controller.Node;

public class PioneerCLNav {

  private Supervisor robot;         // reference to the robot
  private Node robot_node;          // reference to the robot node
  private PioneerSimpleProxSensors pps; // reference to the sensor model
  private Motor left_motor;
  private Motor right_motor;
  
  private Pose start;               // Initial location - set by set_goal()
  private Pose goal;                // goal location - set by set_goal()

  private boolean state;            // state of the robot - goal or wall following
  private boolean WALLFOLLOWING = true;
  private boolean GOALSEEKING = false;

  private int wf_state;         // state of the wall following algorithm
  private int WF_HIT = 0;
  private int WF_SEARCH = 1;
  private int WF_LEAVE = 2;
  private int WF_ROTATE = 3;


  private double bug_radius = 0.4;  // Because of wall following, allow a greater distance to hit/leave points
  private double goal_radius = 0.1; // If the robot is this dist from goal, then success
  private double goal_vel = 1;      // min velocity when robot reaches goal before stopping
  private double start_vel = 3;     // Initial move velocity
  private double accel_dist = 0.4;  // Accelerate up to distance away from the start  
  private double decel_dist = 0.8;  // Decelerate when this distance away from the goal  
  private double accel_angle = 0.524;  // Decelerate rotation when this angle (rads) from goal
  private double wf_dist = 0.3;     // Distance when wall following
  private double wf_vel = 5;        // Default velocity when wall following
  
  private double max_vel;           // Maximum velocity when robot travels

  private double prev_error;        // Previous error (PID controller)     
  private double total_error;       // Previous error (PID controller)     

  private long pid_counter;
  private double pid_diff;
  private Pose hit_point;           // Whenre the robot encountered the obstacle
  private Pose leave_point;         // Where the robot should leave an obstacle
  private double leave_dist;        // cached distance from leave point to goal

  // ==================================================================================
  // Constructor
  // ==================================================================================
  public PioneerCLNav(Supervisor robot, PioneerSimpleProxSensors pps) {
    this.robot = robot;                       // reference to the robot
    this.robot_node = this.robot.getSelf();   // reference to the robot node
    this.pps = pps;                           // reference to the sensor model
    this.goal = this.get_real_pose();         // assume the goal is our current position
    this.start = this.get_real_pose();        // retain our initial position
    this.state = GOALSEEKING;

    this.prev_error = 0;              // Reset values used by the PID controller
    this.total_error = 0;             // Reset values used by the PID controller
        
    // enable motors
    this.left_motor = robot.getMotor("left wheel");
    this.right_motor = robot.getMotor("right wheel");
    this.left_motor.setPosition(Double.POSITIVE_INFINITY);
    this.right_motor.setPosition(Double.POSITIVE_INFINITY);
    
    this.max_vel = this.left_motor.getMaxVelocity();

    // Initialise motor velocity
    this.stop();
  }

  // ==================================================================================
  // Internal (helper) methods
  // ==================================================================================
  
  private final void set_velocity(double lv, double rv) {
    this.left_motor.setVelocity(lv);
    this.right_motor.setVelocity(rv);
  }
  
  // returns the range to an obstacle in front of the robot
  // up to max_range. Used to trigger wall following
  private double range_to_frontobstacle() {
    // uses front sensors so3 & so4
    double range = this.pps.get_maxRange();
    range = Math.min (range, this.pps.get_value(2));
    range = Math.min (range, this.pps.get_value(3));
    range = Math.min (range, this.pps.get_value(4));
    range = Math.min (range, this.pps.get_value(5));    
    return range;
  }

  private double range_to_frontcornerobstacle() {
    // uses front sensors so3 & so4
    double range = this.pps.get_maxRange();
    range = Math.min (range, this.pps.get_value(1));
    range = Math.min (range, this.pps.get_value(2));
    range = Math.min (range, this.pps.get_value(5));
    range = Math.min (range, this.pps.get_value(6));    
    return range;
  }
  
  private double range_to_leftobstacle() {
    // uses front sensors so0 & so15
    double range = this.pps.get_maxRange();
    //range = Math.min (range, this.pps.get_value(1));
    range = Math.min (range, this.pps.get_value(0));
    //range = Math.min (range, this.pps.get_value(15));
    //range = Math.min (range, this.pps.get_value(14));
    return range;
  }

  

  // ==================================================================================
  // Internal (navigation) methods
  // ==================================================================================


  // ==================================================================================
  private double pid(double error) {
    
    double kp = 6.0; // proportional weight (may need tuning)
    double kd = 40.0; // differential weight (may need tuning)
    double ki = 0.0; // integral weight (may need tuning)
    
    double prop = error;
    this.total_error += error;
    
    if ((this.pid_counter % 4)==0) {
        pid_diff = error - this.prev_error;
        this.prev_error = error;
    }
      
    this.pid_counter+= 1; // increment our counter

    return (kp * prop) + (ki * this.total_error) + (kd * this.pid_diff);
  }
  
  // ==================================================================================
  public void adjust_velocity(double bearing, double velocity) {
    double rotate_vel = Math.min(this.max_vel, 
    Math.abs(bearing*this.max_vel/this.accel_angle));
     
    // Determine which way to turn
    if (bearing < 0)
        rotate_vel = -rotate_vel;
    double lv = Math.min(this.max_vel, velocity-rotate_vel);
    double rv = Math.min(this.max_vel, velocity+rotate_vel);
    
    this.set_velocity(lv, rv);
  }   
  
  public boolean rotate(double bearing, double velocity) {
    // Insert code here    
    if (Math.abs(bearing) < 0.1) {
        return true;
    }
    
    this.adjust_velocity(bearing, wf_vel);
    return false;
  }

  // ==================================================================================
  private void track_leave_point(Pose p) {
      // Insert code here
    double dist = p.get_range(this.goal);   // distance to goal
    if (dist < this.leave_dist) {
        this.leave_dist = dist;
        this.leave_point = p;
    }
       
  }

  // ==================================================================================
  // Note we don't handle unaccessible goals
  private void bug1(Pose p) {
    // Insert Bug1 code here
    
    double hit_dist = p.get_range(this.hit_point);
    if (this.wf_state == WF_ROTATE) {
        // About to leave, but need to rotate to the goal
        double bearing = p.get_bearing(this.goal);
        System.out.println(String.format(
          "State: %d, goal dist %.03f bearing: %.03f",
        this.wf_state, p.get_range(this.goal), bearing));
          
        if (this.rotate(bearing, wf_vel)) {
            this.state = GOALSEEKING;
            this.start = this.get_real_pose(); // reset start point
        }
        return;
    }
    
    if (this.wf_state == WF_LEAVE) {
        System.out.println(String.format(
          "State: %d, goal dist %.03f dist to leave: %.03f",
        this.wf_state, p.get_range(this.goal), 
          p.get_range(this.leave_point)));
        // Found Leave point; now need to rotate to goal
        if(p.get_range(this.leave_point) < this.bug_radius) {
            this.wf_state = WF_ROTATE;
            return;
        }
    }
    
    if (this.wf_state == WF_SEARCH) {
        System.out.println(String.format(
          "State: %d, hit dist: %.03f goal dist %.03f leave point: %s %.03f",
        this.wf_state, hit_dist, p.get_range(this.goal), 
        this.leave_point, this.leave_dist));
        // Tracking distance to goal, and checking for hit point
        
        if (hit_dist < this.bug_radius) {
            this.wf_state = WF_LEAVE;
        }    
        else {
            track_leave_point(p);
        }    
    }
    
    if (this.wf_state == WF_HIT) {
        System.out.println(String.format(
          "State: %d, hit dist: %.03f goal dist %.03f leave point: %s %.03f",
        this.wf_state, hit_dist, p.get_range(this.goal), 
        this.leave_point, this.leave_dist));
        // Don't want to check for hit point until we have moved away
        if (hit_dist > this.bug_radius) {
            this.wf_state = WF_SEARCH;
        }
        
        track_leave_point(p);
    }
       
    wall_following(p);
  }     
 
  private void wall_following(Pose p) {
    // Insert wall following code here
    double front_range, left_range, control, error;
    double hit_dist = p.get_range(this.hit_point);
    
    // Is the obstacle in front of us?  If so, then rotate right,
    // around the right wheel
    front_range = Math.min(range_to_frontobstacle(),
                                range_to_frontcornerobstacle());
    if (front_range <= wf_dist) {
        this.set_velocity(wf_vel, 0.0);
    } 
      
    else {
        left_range = this.range_to_leftobstacle();
        
        if (left_range < this.pps.get_maxRange()) {
            error = left_range - wf_dist;
            control = this.pid(error);
      
            // Keep control in a set range
            control = Math.min(control, wf_vel);
            control = Math.max(control, -wf_vel);      
            this.set_velocity(wf_vel, wf_vel+control);
        } 
        
        else {
            // No wall, so turn
            this.set_velocity(wf_vel*0.25, wf_vel*0.75);
        }
    }
    
    return;
  }
  
  private void goal_seeking(Pose p) {
    // Insert goal seeking code here
    double accel_rate = (this.max_vel-start_vel)/this.accel_dist;
    double decel_rate = (this.max_vel-goal_vel)/(this.decel_dist-this.goal_radius);
    double yintercept = this.max_vel - (decel_rate * this.decel_dist);
    
    double start_dist = p.get_range(this.start);    // distance from start
      
    double goal_dist = p.get_range(this.goal);      // distance to goal
    double bearing = p.get_bearing(this.goal);      // bearing to goal
      
    double obstacle_dist = this.range_to_frontobstacle();
      
    if (obstacle_dist <= this.wf_dist) {
        this.state = WALLFOLLOWING;
        this.wf_state = WF_HIT;
        this.prev_error = 0;       // Reset PID controller values
        this.total_error = 0;      // Reset PID controller values 
        this.pid_counter = 0;      // update every loop
        this.pid_diff = 0.0;
        this.hit_point = p;
        this.leave_point = p;
        this.leave_dist = goal_dist;
    } 
      
    else {
        // Calculate acceleration velocity when leaving start
        double accel_vel = Math.min(this.max_vel, 
                          (start_dist*accel_rate) + this.start_vel);
        // Calculate deceleration velocity when approaching goal      
        double decel_vel = Math.min(this.max_vel, 
                          (goal_dist*decel_rate) + yintercept);
                            
        // Update deceleration velocity (e.g. when approaching obstacle)
        if ((obstacle_dist - this.wf_dist) < this.decel_dist) {
            decel_vel = Math.min(decel_vel, 
                         ((obstacle_dist - this.wf_dist)*decel_rate) + yintercept);
        }
          
        // Determine final velocity
        double final_vel = Math.min(decel_vel, accel_vel);
          
        // Simple, no-acceleration approach
        //double final_vel = this.max_vel;
        this.adjust_velocity(bearing, final_vel);  
    }
      
  }

  // ==================================================================================
  // External methods (Navigation)
  // ==================================================================================

  public final void stop() {
    this.set_velocity(0.0, 0.0);
  }
  
  // ==================================================================================
  // Get Real Pose - ask the supervisor where the robot is
  // ==================================================================================
  // Note that the following method is defined as final, as it is used in the constructor

  public final Pose get_real_pose() {
    if (this.robot_node == null)
      return new Pose(0,0,0);
      
    double[] realPos = robot_node.getPosition();
    double[] rot = this.robot_node.getOrientation(); // 3x3 Rotation matrix as vector of length 9
    double theta1 = Math.atan2(-rot[0], rot[3]);
    double halfPi = Math.PI/2;
    double theta2 = theta1 + halfPi;
    if (theta1 > halfPi)
        theta2 = -(3*halfPi)+theta1;
    
    return new Pose(realPos[0], realPos[1], theta2);
  }
  
  // ==================================================================================
  // Go to some pose
  // ==================================================================================
  // update() updates the current action of the robot in moving to the goal
  // returns false if at goal or true if moving

  public boolean update() {
    Pose p = this.get_real_pose(); // current position
    if (p.get_range(this.goal) < goal_radius) {
      this.stop();
      return true;  // Found goal
    }
    if (state==GOALSEEKING)
      goal_seeking(p);
    else
      bug1(p);
    return false;  // Not yet found goal
  }
  
  public void set_goal(Pose p) {
    this.goal = new Pose(p);
    this.start = get_real_pose();
    this.update();
  }

}
