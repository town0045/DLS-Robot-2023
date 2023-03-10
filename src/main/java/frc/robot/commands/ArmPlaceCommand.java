package frc.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.ArmExtend;

public class ArmPlaceCommand extends ParallelCommandGroup{
    Arm m_arm;
    ArmExtend m_armExtend;
    double angle;
    double length;
    public ArmPlaceCommand(double _angle, double _length, Arm _arm, ArmExtend _armExtend){
        m_arm = _arm;
        m_armExtend = _armExtend;
        angle = _angle;
        length = _length;
        addCommands(
        new ArmProfileCommand(Math.toRadians(angle), m_arm),
        new ArmLengthSet(length, m_armExtend));
    }

    
}
