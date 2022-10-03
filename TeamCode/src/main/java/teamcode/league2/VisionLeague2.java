package teamcode.league2;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import teamcode.common.Vector3D;

public class VisionLeague2 {

    private static final String VUFORIA_KEY = "AQR2KKb/////AAABmcBOjjqXfkjtrjI9/Ps5Rs1yoVMyJe0wdjaX8pHqOaPu2gRcObwPjsuWCCo7Xt52/kJ4dAZfUM5Gy73z3ogM2E2qzyVObda1EFHZuUrrYkJzKM3AhY8vUz6R3fH0c/R9j/pufFYAABOAFoc5PtjMQ2fbeFI95UYXtl0u+6OIkCUJ3Zw71tvoD9Fs/cOiLB45FrWrxHPbinEhsOlCTWK/sAC2OK2HuEsBFCebaV57vKyATHW4w2LMWEZaCByHMk9RJDR38WCqivXz753bsiBVMbCzPYzwzc3DKztTbK8/cXqPPBLBKwU8ls0RN52akror1xE9lPwwksMXwJwolpyIZGnZngWcBWX4lLH+HlDNZ8Qm";
    private static final String ASSET_NAME = "Skystone";

    private VuforiaTrackables trackables;

    public VisionLeague2(HardwareMap hardwareMap) {
        this(hardwareMap, CameraType.PHONE);
    }

    public VisionLeague2(HardwareMap hardwareMap, CameraType cameraType) {
        createVuforia(hardwareMap, cameraType);
    }

    private VuforiaLocalizer createVuforia(HardwareMap hardwareMap, CameraType cameraType) {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().
                getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        if (cameraType == CameraType.WEBCAM) {
            parameters.cameraName = hardwareMap.get(CameraName.class,"webcam");
        }
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        VuforiaLocalizer vuforia = ClassFactory.getInstance().createVuforia(parameters);
        trackables = vuforia.loadTrackablesFromAsset(ASSET_NAME);
        trackables.activate();
        return vuforia;
    }

    public Vector3D getSkystonePosition() {
        VuforiaTrackable skystone = trackables.get(0);
        OpenGLMatrix pose = ((VuforiaTrackableDefaultListener) skystone.getListener()).getPose();
        if (pose == null) {
            return null;
        }
        VectorF position = pose.getTranslation();
        double x = position.get(0);
        double y = position.get(1);
        double z = position.get(2);
        return new Vector3D(x, y, z);
    }

    public enum CameraType {
        PHONE, WEBCAM
    }

}
