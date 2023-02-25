package frc.robot.commands;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.ProfiledPIDCommand;
import frc.robot.Constants;
import frc.robot.subsystems.Arm;

public class ArmProfileCommand extends ProfiledPIDCommand {
    
    public ArmProfileCommand(double targetAngleDegrees, double armLength, Arm m_Arm) {
        //We will likely need another ProfiledPIDController to length the Arm - simple fix for now is to pass it into the command.
     
        super(new ProfiledPIDController(
            Constants.ArmKp, 
            Constants.ArmKi, 
            Constants.ArmKd,
            new TrapezoidProfile.Constraints(
                Constants.ArmMaxRotVel,
                Constants.ArmMaxRotAccel)),
            m_Arm::ArmAngle, 
            Math.toRadians(targetAngleDegrees), 
            (output ,setpoint) -> m_Arm.ArmMoveVolts(output), 
            m_Arm);

        getController().
        setTolerance(Math.toRadians(Constants.angleTolerance));
    }

    @Override
    public boolean isFinished() {
        return getController().atGoal();
    }
}