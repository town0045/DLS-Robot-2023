// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import frc.robot.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.SimVisionSystem;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.TalonFXSimCollection;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.ADIS16448_IMU;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.PWMMotorController;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.simulation.ADIS16448_IMUSim;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
//import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class DriveBot_DrivetrainSubsystem extends SubsystemBase {  
  private final WPI_VictorSPX Cim0 = new WPI_VictorSPX(Constants.drive_cim_0);
  private final WPI_VictorSPX Cim2 = new WPI_VictorSPX(Constants.drive_cim_2);
  private final WPI_VictorSPX Cim3 = new WPI_VictorSPX(Constants.drive_cim_3);
  private final WPI_VictorSPX Cim4 = new WPI_VictorSPX(Constants.drive_cim_4);

  private final MotorControllerGroup _leftMotor = new MotorControllerGroup(Cim0, Cim2);
  private final MotorControllerGroup _rightMotor = new MotorControllerGroup(Cim3, Cim4);
  private PhotonPoseEstimator photonPoseEstimator;

//Creating the odometry object to allow tracking of our robot
private final DifferentialDriveOdometry m_odometry;
//Creating a field, primarily for testing
public final Field2d m_field = new Field2d();
//Creating an object that will hold the aprilTag locations
private AprilTagFieldLayout aprilTagFieldLayout;

private Pose3d m_objectInField;

//private final ADIS16448_IMU m_gyro = new ADIS16448_IMU();
 PhotonCamera camera = new PhotonCamera("Cube_cam");
 final double ANGULAR_P = 0.03;
 final double ANGULAR_D = 0.0;
 PIDController turnController = new PIDController(ANGULAR_P, 0, ANGULAR_D);
 final double LINEAR_P = 0.03;
 final double LINEAR_D = 0.0;
 PIDController ForwardController = new PIDController(ANGULAR_P, 0, ANGULAR_D);

 PhotonCamera llCamera = new PhotonCamera("LimeLight");
 
Pose2d prevPose = new Pose2d(10,10,new Rotation2d(0));

  
//Camera Simulation Code
double camDiagFOV = 75.0; // degrees
double camPitch = 15.0; // degrees
double camHeightOffGround = 0.85; // meters
double maxLEDRange = 20; // meters
int camResolutionWidth = 640; // pixels
int camResolutionHeight = 480; // pixels
double minTargetArea = 10; // square pixels
SimVisionSystem simVision =
            new SimVisionSystem(
                    "LimeLight",
                    camDiagFOV,
                    Constants.robotToCam,
                    maxLEDRange,
                    camResolutionWidth,
                    camResolutionHeight,
                    minTargetArea);

  
  //Defining the drivetrain subsystem
  public DriveBot_DrivetrainSubsystem() {
  _rightMotor.setInverted(true);

  try {
    AprilTagFieldLayout fieldLayout = new AprilTagFieldLayout("Games/AprilTag.json");
    photonPoseEstimator = new PhotonPoseEstimator(fieldLayout, PoseStrategy.MULTI_TAG_PNP, llCamera, Constants.robotToCam);
    photonPoseEstimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);
  } catch (IOException e) {
    DriverStation.reportError("Failed to load AprilTagFieldLayout",e.getStackTrace());
    photonPoseEstimator = null;
  }
  SmartDashboard.putData("Field", m_field);

}
  private final DifferentialDrive _drivetrain = new DifferentialDrive(_leftMotor, _rightMotor);

//Drivetrain Methods:

  public void drive(double leftSpeed, double rightSpeed) {
    //Sends speeds to tank drive. Considered implementing a reverse driving capacity, but eventually abandoned that.
    // Note: tankdrive is a method of the DifferentialDrive class.
    _drivetrain.tankDrive(leftSpeed, rightSpeed);
// left side = stick 
// right side = -stick

  }

  public void drive_Arcade(double speed, double rotation) {
    
    _drivetrain.arcadeDrive(speed, rotation);

  }

 
//Camera Methods
//This method will return forward and rotation values- will need to be called by cube gathering command
public double[] find_cube(){
    
  var result = cubeCam.getLatestResult();
  double rotationSpeed;
  double forwardSpeed;
  if (result.hasTargets()) {
    SmartDashboard.putBoolean("Target", true);
      // Calculate angular turn power
      // -1.0 required to ensure positive PID controller effort _increases_ yaw
      forwardSpeed = forwardController.calculate(result.getBestTarget().getArea(), 15); //<-- The target area should be in the Constants class.
      rotationSpeed = turnController.calculate(result.getBestTarget().getYaw(), 0); // <-- Target Yaw will need to be updated once camera and intake are mounted. Also should be in Constants
      if (rotationSpeed > 0.5){rotationSpeed = 0.5;} // limit rotation speed
  } else {
      SmartDashboard.putBoolean("Target", false);
      forwardSpeed = 0;
      rotationSpeed = 0;
  }
// Use our forward/turn speeds to control the drivetrain
double[] array = {forwardSpeed, rotationSpeed};
return array;
}

//This method returns if AT Target is acquired
public boolean haveATTarget(){
  var result = llCamera.getLatestResult();
  if (result.hasTargets()) {SmartDashboard.putBoolean("ATTarget", true);
} else {SmartDashboard.putBoolean("ATTarget", false);}
  return result.hasTargets();
}

//This method returns the position of the AT target, if none returns current position.
public Pose3d getobjectInFieldID(){
  var result = llCamera.getLatestResult();
  int objectID;
  if (result.hasTargets()) {
    objectID = result.getBestTarget().getFiducialId();
    return aprilTagFieldLayout.getTagPose(objectID).get();
  } else {
    //For Testing always target aprilt tag 3 :
    //return aprilTagFieldLayout.getTagPose(3).get();
    return new Pose3d(m_odometry.getPoseMeters());
  }
  
}

//Returns a pose that is slightly removed from the target in the x, aligned in the y, and oppisite in the rotation.
public Pose2d getPoseTarget(){
  Pose2d object =  this.getobjectInFieldID().toPose2d();
  //This transform moves the center of the robot away from the vision target and faces the target.
  Transform2d robotToTarget = new Transform2d(new Translation2d(0.75, 0.0), new Rotation2d(Math.PI));
  return object.transformBy(robotToTarget);
}

//Creates the trajectory needed to get to the target pose.
public Trajectory targetTrajectory() {
  //Has issues if curPose.x is lower than tarPose.x, the trajectory moves past target.
  Pose2d curPose = m_odometry.getPoseMeters();
  Pose2d tarPose = this.getPoseTarget();
  var interiorWaypoint = new ArrayList<Translation2d>();
  interiorWaypoint.add(new Translation2d((tarPose.getX() - curPose.getX())/2 + curPose.getX(),tarPose.getY() ));
 
  Trajectory traj = TrajectoryGenerator.generateTrajectory(
    curPose, interiorWaypoint, tarPose, config);
    m_field.getObject("Target").setTrajectory(traj);
    
    System.out.println("Sent new traj");
    return traj;
}


public void updateOdometry(){
  photonPoseEstimator.setReferencePose(prevPose);
  Optional<EstimatedRobotPose> result = photonPoseEstimator.update();
  if (result.isPresent()) {
    EstimatedRobotPose camPose = result.get();
    m_field.getObject("Cam Est Pos").setPose(camPose.estimatedPose.toPose2d());
} else {
    // move it way off the screen to make it disappear
    m_field.getObject("Cam Est Pos").setPose(new Pose2d(10, 10, new Rotation2d()));
}
}
@Override
  public void periodic() {
    // This method will be called once per scheduler run and update the position and orientation of the robot.
    this.updateOdometry();
    simVision.processFrame(m_field.getRobotPose());
    }

  
/* 
  public double getHeading() {
    //Method returns heading in degrees from original heading.
    return m_gyro.getGyroAngleZ();
  }
  

public void zeroHeading() {
  m_gyro.reset();
}
*/
}
