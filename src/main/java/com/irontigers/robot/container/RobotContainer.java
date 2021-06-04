/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.irontigers.robot.container;

import static edu.wpi.first.wpilibj.XboxController.Button.kA;
import static edu.wpi.first.wpilibj.XboxController.Button.kB;
import static edu.wpi.first.wpilibj.XboxController.Button.kBack;
import static edu.wpi.first.wpilibj.XboxController.Button.kStart;
import static edu.wpi.first.wpilibj.XboxController.Button.kX;
import static edu.wpi.first.wpilibj.XboxController.Button.kY;

import java.util.function.BiConsumer;

import com.irontigers.robot.Constants;
import com.irontigers.robot.Robot;
import com.irontigers.robot.commands.IntakeControl;
import com.irontigers.robot.old.commands.AutonomousDrive;
import com.irontigers.robot.old.commands.JoystickDriveCommand;
import com.irontigers.robot.old.commands.RotateTurret;
import com.irontigers.robot.old.commands.RunShooter;
import com.irontigers.robot.old.commands.Shoot;
import com.irontigers.robot.old.commands.StopShooter;
import com.irontigers.robot.old.commands.VisionAim;
import com.irontigers.robot.old.subsystems.DriveSystem;
import com.irontigers.robot.old.subsystems.MagazineSystem;
import com.irontigers.robot.old.subsystems.ShooterSystem;
import com.irontigers.robot.old.subsystems.VisionSystem;
import com.irontigers.robot.triggers.BallPresenceTrigger;

// import org.graalvm.compiler.lir.amd64.vector.AMD64VectorShuffle.ConstShuffleBytesOp;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.MedianFilter;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.controller.RamseteController;
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a "declarative" paradigm, very little robot logic should
 * actually be handled in the {@link Robot} periodic methods (other than the
 * scheduler calls). Instead, the structure of the robot (including subsystems,
 * commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private ButtonContainer buttons = new ButtonContainer();

  private DriveSystem driveSystem = new DriveSystem();
  private ShooterSystem shooterSystem = new ShooterSystem();
  private MagazineSystem magSystem = new MagazineSystem();
  private VisionSystem visionSystem = new VisionSystem();

  private JoystickDriveCommand joyDrive = new JoystickDriveCommand(driveSystem, buttons.controller);
  private AutonomousDrive autonomousDrive = new AutonomousDrive(driveSystem);
  private BallPresenceTrigger topBallSensor = new BallPresenceTrigger(magSystem.getTopBallSensor());
  private BallPresenceTrigger bottomBallSensor = new BallPresenceTrigger(magSystem.getBottomBallSensor());
  private MedianFilter bottomSensorFilter = new MedianFilter(5);

  private IntakeControl intakeSwitchCommand = new IntakeControl(magSystem);

  private SequentialCommandGroup shootOnceCommand = new SequentialCommandGroup(
      new InstantCommand(visionSystem::setToVision), new VisionAim(shooterSystem, visionSystem),
      new Shoot(magSystem, shooterSystem, visionSystem, topBallSensor),
      new InstantCommand(magSystem::closeGate, magSystem), new WaitCommand(0.5), new StopShooter(shooterSystem),
      new InstantCommand(visionSystem::setToDriving));

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();

    driveSystem.setDefaultCommand(joyDrive);
    // magSystem.setDefaultCommand(MagazineOn());

    intakeSwitchCommand.initialize();
    magSystem.setDefaultCommand(intakeSwitchCommand);
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be
   * created by instantiating a {@link GenericHID} or one of its subclasses
   * ({@link edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then
   * passing it to a {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  
  private void configureButtonBindings() {
    buttons.shootOne.whenPressed(shootOnceCommand);
    buttons.shootAll.whenPressed(getShootAllCommand());

    buttons.incrementCount.whenPressed(new InstantCommand(magSystem::incrementBalls));
    buttons.decrementCount.whenPressed(new InstantCommand(magSystem::decrementBalls));

    buttons.turretLeft.whenHeld(new RotateTurret(shooterSystem, RotateTurret.Direction.LEFT));
    buttons.turretRight.whenHeld(new RotateTurret(shooterSystem, RotateTurret.Direction.RIGHT));

    buttons.openGate.whenPressed(new InstantCommand(magSystem::openGate, magSystem));
    buttons.closeGate.whenPressed(new InstantCommand(magSystem::closeGate, magSystem));

    // buttons.startIntake.whenPressed(voluntaryIntakeCommand());
    buttons.cancelShooting.whenPressed(new InstantCommand(shooterSystem::stopFlywhel, shooterSystem));

    // visionSystem.disableLeds();

    // bottomBallSensor.whenInactive(magSystem::incrementBalls, magSystem);
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  // public Command getAutonomousCommand() {
  //   // An ExampleCommand will run in autonomous
  //   return new SequentialCommandGroup(
  //     new InstantCommand(visionSystem::setToVision),
  //     new InstantCommand(magSystem::closeGate), new AutonomousDrive(driveSystem),
  //       new InstantCommand(visionSystem::enableLeds), new WaitUntilCommand(visionSystem::seesTarget),
  //       new VisionAim(shooterSystem, visionSystem), getShootAllCommand(), new InstantCommand(visionSystem::disableLeds),
  //       new InstantCommand(visionSystem::setToDriving));
  // }


 /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {

    // Create a voltage constraint to ensure we don't accelerate too fast
    var autoVoltageConstraint =
        new DifferentialDriveVoltageConstraint(
            new SimpleMotorFeedforward(Constants.Characterization.FeedForward.KS,
              Constants.Characterization.FeedForward.KV,
              Constants.Characterization.FeedForward.KA),
            Constants.Characterization.kDriveKinematics,
            6);

    // Create config for trajectory
    TrajectoryConfig config =
        new TrajectoryConfig(Constants.Characterization.kMaxSpeedMetersPerSecond,
                             Constants.Characterization.kMaxAccelerationMetersPerSecondSquared)
            // Add kinematics to ensure max speed is actually obeyed
            .setKinematics(Constants.Characterization.kDriveKinematics)
            // Apply the voltage constraint
            .addConstraint(autoVoltageConstraint);

    // An example trajectory to follow.  All units in meters.
    Trajectory exampleTrajectory = DriveSystem.path("Debug");
    // Trajectory mini = TrajectoryGenerator.generateTrajectory(new Pose2d(0, 0, new Rotation2d(0)),
    //                   java.util.List.of(
    //                     new Translation2d(1, 1),
    //                     new Translation2d(5, -1)
    //                   ),
    //                   new Pose2d(6, 0, new Rotation2d(0)),


    //                   config);


    BiConsumer<Double, Double> outVolts = (l, r) -> driveSystem.tankDriveVolts(l, r);

    RamseteCommand ramseteCommand = new RamseteCommand(
        exampleTrajectory,
        // mini,
        driveSystem.getPose2d(),
        new RamseteController(Constants.Characterization.kRamseteB, Constants.Characterization.kRamseteZeta),
        new SimpleMotorFeedforward(Constants.Characterization.FeedForward.KS,
                                   Constants.Characterization.FeedForward.KV,
                                   Constants.Characterization.FeedForward.KA),
        Constants.Characterization.kDriveKinematics,
        driveSystem.getWheelSpeeds(),
        new PIDController(Constants.Characterization.kPDriveVel, 0, 0),
        new PIDController(Constants.Characterization.kPDriveVel, 0, 0),
        // RamseteCommand passes volts to the callback
        outVolts,
        driveSystem
    );

    // Reset odometry to the starting pose of the trajectory.
    driveSystem.resetOdometry(exampleTrajectory.getInitialPose());

    // Run path following command, then stop at the end.
    return ramseteCommand.andThen(() -> driveSystem.tankDriveVolts(0, 0));
  }

/////////////////////////////////////////
  public void initTesting() {
    // visionSystem.disableLeds();
    visionSystem.enableLeds();
    visionSystem.setToVision();
    JoystickButton enableShooterButton = new JoystickButton(buttons.testController, kX.value);
    JoystickButton disableMagButton = new JoystickButton(buttons.testController, kY.value);
    JoystickButton shootButton = new JoystickButton(buttons.testController, kA.value);
    JoystickButton stopShooterButton = new JoystickButton(buttons.testController, kB.value);
    JoystickButton increaseFlywheelButton = new JoystickButton(buttons.testController, kStart.value);
    JoystickButton decreaseFlywheelButton = new JoystickButton(buttons.testController, kBack.value);
    

    // buttons.turretLeftButton.whenPressed(shooterSystem.setTurretPower(0.5));

    enableShooterButton.whenPressed(() -> shooterSystem.setFlywheelPower(0.2), shooterSystem);
    disableMagButton.whenPressed(magSystem::disableMagazine, magSystem);

    shootButton.whenPressed(new RunShooter(shooterSystem));
    stopShooterButton.whenPressed(new StopShooter(shooterSystem));

    shooterSystem.setFlywheelPower(.2);

    increaseFlywheelButton.whenPressed(() -> shooterSystem.setFlywheelPower(shooterSystem.getFlywheelPower() + 0.025),
        shooterSystem);
    decreaseFlywheelButton.whenPressed(() -> shooterSystem.setFlywheelPower(shooterSystem.getFlywheelPower() - 0.025),
        shooterSystem);

    
  }

  // Shoots all the balls
  private Command getShootAllCommand() {
    SequentialCommandGroup shootAllCommand = new SequentialCommandGroup(new VisionAim(shooterSystem, visionSystem));
    
    for (int i = 0; i < magSystem.getStoredBalls(); i++) {
      shootAllCommand = shootAllCommand.andThen(new Shoot(magSystem, shooterSystem, visionSystem, topBallSensor));
    }

    return shootAllCommand.andThen(new InstantCommand(magSystem::closeGate, magSystem), new WaitCommand(0.5),
        new StopShooter(shooterSystem), new InstantCommand(visionSystem::setToDriving));

  }

  public VisionSystem getVisionSystem() {
    return visionSystem;
  }
}