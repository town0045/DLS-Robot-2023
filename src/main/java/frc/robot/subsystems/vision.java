// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;



// Wpilibj
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

// Networktables
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

// Constants
import frc.robot.Constants;


public class vision extends SubsystemBase{
  
 //Declaring the Subsystem \/
 public vision() {

  //Putting the PID constants on the SmartDashboard is a good way to tune them.
  //Although it is not ideal to leave them there for the competion.
  
 }

 // Create arrays to hold limelight values
 double[] txlist = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
 double[] tylist = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
 double[] talist = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
 double[] tllist = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
 int focus = 0; // Which element the program is focused on

 
 // Get the average of all doubles in an array
 private double getAverage(double[] array){

  double sum = 0.0; 
  int values = 10; // Amount to divide by

  // Add all values of the array together 
  for (int i = 0; i < 9; i++){
    if (array[i] == 0.0){
      values--; // Decrease amount to divide by
    }
    sum += array[i];
  }

  // Don't divide by 0
  if (values == 0){
    values = 1;
  }

  // Divide by amount of nonzero numbers
  return sum / values;
 }

 // Swap between pipelines
 public void setPipeline(int pipe){
    // Limelight table
    NetworkTable limelight = NetworkTableInstance.getDefault().getTable("limelight");
    
    // Set the pipeline to pipe
    // (Checks if the param is within range to avoid errors)
    if (pipe >= 0 && pipe <= 9){
      limelight.getEntry("pipeline").setNumber(pipe);
    }
  }


 
 
 @Override
  public void periodic() {
    // This method will be called once per scheduler run
    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");

    // Retrieve limelight data
    NetworkTableEntry tx = table.getEntry("tx");
    NetworkTableEntry ty = table.getEntry("ty");
    NetworkTableEntry ta = table.getEntry("ta");
    NetworkTableEntry tl = table.getEntry("tl");
    

    // read values periodically
    double xRaw = tx.getDouble(0.0);
    double yRaw = ty.getDouble(0.0);
    double areaRaw = ta.getDouble(0.0);
    double lengthRaw = tl.getDouble(0.0);


    // Add values to arrays
    txlist[focus] = xRaw;
    tylist[focus] = yRaw;
    talist[focus] = areaRaw;
    tllist[focus] = lengthRaw;

    // Increment focus
    if (focus < 9){
      focus++;
    } else {
      focus = 0;
    }

    // Get averages of arrays
    double x = getAverage(txlist);
    double y = getAverage(tylist);
    double area = getAverage(talist);
    double length = getAverage(tllist);
    
    //post to smart dashboard periodically
    
    
  }

/*
  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
*/
}
