package frc.robot.commands;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.ProfiledPIDCommand;
import frc.robot.Constants;
import frc.robot.subsystems.MiniArm;

public class MiniArmProfileCommand extends ProfiledPIDCommand {
    
    public MiniArmProfileCommand(double targetAngleDegrees, MiniArm m_miniArm) {
        super(new ProfiledPIDController(
            Constants.miniArmKp, 
            Constants.miniArmKi, 
            Constants.miniArmKd,
            new TrapezoidProfile.Constraints(
                Constants.miniArmMaxRotVel,
                Constants.miniArmMaxRotAccel)),
            m_miniArm::ArmAngle, 
            Math.toRadians(targetAngleDegrees), 
            (output, setpoint) -> m_miniArm.ArmMoveVolts(output), 
            m_miniArm);

        getController().
        setTolerance(Math.toRadians(Constants.angleTolerance));
    }

    @Override
    public boolean isFinished() {
        return getController().atGoal();
    }
}
