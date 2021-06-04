// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.irontigers.robot.container;

import com.irontigers.robot.Constants.Controllers;
import com.irontigers.robot.triggers.DPadButton;
import com.irontigers.robot.triggers.DPadButton.DPadDirection;
import com.irontigers.robot.util.CorrectXboxController;
import static edu.wpi.first.wpilibj.XboxController.Button.*;

import edu.wpi.first.wpilibj2.command.button.JoystickButton;

/** Add your docs here. */
class ButtonContainer {
    CorrectXboxController controller = new CorrectXboxController(Controllers.PORT);
    CorrectXboxController testController = new CorrectXboxController(Controllers.TEST_PORT);

    JoystickButton shootAll = new JoystickButton(controller, kA.value);
    JoystickButton shootOne = new JoystickButton(controller, kB.value);
    JoystickButton cancelShooting = new JoystickButton(controller, kY.value);
    JoystickButton startIntake = new JoystickButton(controller, kX.value);
    JoystickButton autoAim = new JoystickButton(controller, kBumperRight.value);

    JoystickButton incrementCount = new JoystickButton(controller, kStart.value);
    JoystickButton decrementCount = new JoystickButton(controller, kBack.value);

    JoystickButton openGate = new JoystickButton(testController, kBumperRight.value);
    JoystickButton closeGate = new JoystickButton(testController, kBumperLeft.value);

    DPadButton turretLeft = new DPadButton(controller, DPadDirection.LEFT);
    DPadButton turretRight = new DPadButton(controller, DPadDirection.RIGHT);

    JoystickButton startAutonomous = new JoystickButton(controller, kBack.value);
}
