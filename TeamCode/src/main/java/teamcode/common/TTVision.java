package teamcode.common;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

public class TTVision {

    private static final String VUFORIA_KEY = "AQR2KKb/////AAABmcBOjjqXfkjtrjI9/Ps5Rs1yoVMyJe0wdjaX8pHqOaPu2gRcObwPjsuWCCo7Xt52/kJ4dAZfUM5Gy73z3ogM2E2qzyVObda1EFHZuUrrYkJzKM3AhY8vUz6R3fH0c/R9j/pufFYAABOAFoc5PtjMQ2fbeFI95UYXtl0u+6OIkCUJ3Zw71tvoD9Fs/cOiLB45FrWrxHPbinEhsOlCTWK/sAC2OK2HuEsBFCebaV57vKyATHW4w2LMWEZaCByHMk9RJDR38WCqivXz753bsiBVMbCzPYzwzc3DKztTbK8/cXqPPBLBKwU8ls0RN52akror1xE9lPwwksMXwJwolpyIZGnZngWcBWX4lLH+HlDNZ8Qm";
    private static final String ASSET_NAME = "Skystone.tflite";
    public static final String LABEL_STONE = "Stone";
    public static final String LABEL_SKYSTONE = "Skystone";
    private static final double MINIMUM_CONFIDENCE = 0.75;

    private HardwareMap hardwareMap;
    private TFObjectDetector tfod;
    private boolean enabled;

    public TTVision(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    /**
     * Must be called from an OpMode before use.
     */
    public void enable() {
        VuforiaLocalizer vuforia = createVuforia();
        tfod = createTFOD(vuforia);
        tfod.activate();
        enabled = true;
    }

    public void disable() {
        if (enabled) {
            tfod.shutdown();
            enabled = false;
        }
    }

    private VuforiaLocalizer createVuforia() {
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        VuforiaLocalizer vuforia = ClassFactory.getInstance().createVuforia(parameters);
        return vuforia;
    }

    private TFObjectDetector createTFOD(VuforiaLocalizer vuforia) {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minimumConfidence = MINIMUM_CONFIDENCE;
        TFObjectDetector tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(ASSET_NAME, LABEL_STONE, LABEL_SKYSTONE);
        return tfod;
    }

    public List<Recognition> getRecognitions() {
        if (!enabled) {
            throw new IllegalStateException("Vision must be enabled first");
        }

        return tfod.getRecognitions();
    }

    public static Vector2 getTopLeft(Recognition recognition) {
        return new Vector2(recognition.getLeft(), recognition.getTop());
    }

    public static Vector2 getBottomRight(Recognition recognition) {
        return new Vector2(recognition.getRight(), recognition.getBottom());
    }

    public static Vector2 getCenter(Recognition recognition) {
        float x = (recognition.getLeft() + recognition.getRight()) / 2;
        float y = (recognition.getBottom() + recognition.getTop()) / 2;
        return new Vector2(x, y);
    }

}
