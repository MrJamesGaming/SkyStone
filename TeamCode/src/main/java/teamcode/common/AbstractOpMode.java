package teamcode.common;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public abstract class AbstractOpMode extends LinearOpMode {

    private static AbstractOpMode opMode;

    private List<Timer> timers;

    public static AbstractOpMode currentOpMode() {
        return opMode;
    }

    @Override
    public final void runOpMode() {
        opMode = this;
        timers = new ArrayList<>();
        onInitialize();
        waitForStart();
        if (opModeIsActive()) {
            onStart();
        }
        onStop();
        for (Timer timer : timers) {
            timer.cancel();
        }
        Debug.clear();
    }

    protected abstract void onInitialize();

    protected abstract void onStart();

    protected abstract void onStop();

    /**
     * Use getNewTimer() instead.
     */
    @Deprecated
    public Timer getTimer() {
        return getNewTimer();
    }

    public Timer getNewTimer() {
        Timer timer = new Timer();
        timers.add(timer);
        return timer;
    }

    public void cancelTimer(Timer timer) {
        timer.cancel();
        timers.remove(timer);
    }

}
