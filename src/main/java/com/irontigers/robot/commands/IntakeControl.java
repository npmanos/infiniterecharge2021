/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.irontigers.robot.commands;

import com.irontigers.robot.old.subsystems.MagazineSystem;

import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class IntakeControl extends ConditionalCommand {

  public IntakeControl(MagazineSystem magSystem) {
    super(
        // If true
        new SequentialCommandGroup(new InstantCommand(magSystem::disableIntake),
            new InstantCommand(magSystem::disableMagazine)),

        // If false
        new SequentialCommandGroup(new InstantCommand(magSystem::enableIntake),
            new InstantCommand(magSystem::enableMagazine)),

        // Condition
        magSystem::isMagFull);

        addRequirements(magSystem);
  }

}
