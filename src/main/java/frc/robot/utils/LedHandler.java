// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utils;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.util.Color;
import frc.robot.Constants;

/** Add your docs here. */
public final class LedHandler {
    private static final AddressableLED led = new AddressableLED(Constants.LED_PORT);
    private static final AddressableLEDBuffer buffer = new AddressableLEDBuffer(Constants.LED_COUNT);
    private static Thread thread = new Thread();

    public static void init() {
        led.setLength(Constants.LED_COUNT);
        setColor(255, 0, 255);
        led.start();
    }

    public static void setColor(int index, int red, int green, int blue) {
        stopCurrentTransition();
        buffer.setRGB(index, red, green, blue);
        led.setData(buffer);
    }

    public static void setColor(int red, int green, int blue) {
        stopCurrentTransition();
        for (int i = 0; i < Constants.LED_COUNT; i++) {
            buffer.setRGB(i, red, green, blue);
        }
        led.setData(buffer);
    }

    public static void setHsv(int index, double hue, double saturation, double value) {
        stopCurrentTransition();
        buffer.setHSV(index, (int) hue, (int) saturation, (int) value);
        led.setData(buffer);
    }

    public static void setHsv(double hue, double saturation, double value) {
        stopCurrentTransition();
        for (int i = 0; i < Constants.LED_COUNT; i++) {
            buffer.setHSV(i, (int) hue, (int) saturation, (int) value);
        }
        led.setData(buffer);
    }

    public static void transitionTo(double hue, double saturation, double value, double duration) {
        double millis = duration * 1000;
        stopCurrentTransition();
        thread = new Thread(() -> {
            double[] hsv = bgrToHsv(buffer.getLED(0));
            double startHue = hsv[0];
            double startSaturation = hsv[1];
            double startValue = hsv[2];
            double endHue = hue;
            double endSaturation = saturation;
            double endValue = value;
            int step = 0;
            while (step < millis) {
                double currentHue = (startHue + (endHue - startHue) * step / millis);
                double currentSaturation = (startSaturation + (endSaturation - startSaturation) * step / millis);
                double currentValue = (startValue + (endValue - startValue) * step / millis);
                setHsv(currentHue, currentSaturation, currentValue);
                step += 100;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            setHsv(hue, saturation, value);
        });
        thread.start();
    }

    public static double[] bgrToHsv(Color color) {
        double[] hsv = new double[3];
        double red = color.red / 255.0;
        double green = color.green / 255.0;
        double blue = color.blue / 255.0;
        double max = Math.max(red, Math.max(green, blue));
        double min = Math.min(red, Math.min(green, blue));
        double delta = max - min;
        hsv[2] = max;
        if (max != 0) {
            hsv[1] = delta / max;
        } else {
            hsv[1] = 0;
            hsv[0] = -1;
            return hsv;
        }
        if (red == max) {
            hsv[0] = (green - blue) / delta;
        } else if (green == max) {
            hsv[0] = 2 + (blue - red) / delta;
        } else {
            hsv[0] = 4 + (red - green) / delta;
        }
        hsv[0] *= 60;
        if (hsv[0] < 0) {
            hsv[0] += 360;
        }
        return hsv;
    }

    public static void stopCurrentTransition() {
        if (thread.isAlive()) {
            thread.interrupt();
        }
    }
}
