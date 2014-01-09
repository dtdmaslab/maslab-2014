package tests;

import util.Grapher;
import navigation.ErrorCalculator;
import navigation.ErrorHandler;
import navigation.PidController;

public class PidControllerTest {
    protected static float desired = 3;
    protected static float initial = 0;
    protected static float current = initial;
    protected static float velocity = 0;
    protected static final ErrorCalculator mockErrorCalculator = new ErrorCalculator() {
        public float getError() {
            return desired - current;
        }
        
    };
    protected static Grapher grapher = new Grapher();
    protected static int k = 0;
    protected static final ErrorHandler mockErrorHandler = new ErrorHandler() {
        @Override
        public void handleError(float pid_res) {
            grapher.note(k, current);
            current += velocity;
            float diff = pid_res - velocity;
            velocity += Math.signum(diff) * Math.min(Math.abs(diff), 0.2);
            k += 1;
            if (k > 100) {
                grapher.note(k, current);
                // TODO: This is terrible. Find a better way to exit the loop.
                throw new RuntimeException("Abort!");
            }
        }
    };
    public static void main(String[] args) {
        test(1, 0, 0); // Oscillates
        test(1, 0, -1); // Diverges
        test(1, 0, 1); // Damped
        test(1, 0, 2); // Close to critically damped
    }

    public static void test(float p, float i, float d) {
        grapher.clear();
        k = 0;
        current = initial;
        grapher.setName("P=" + p + ", I=" + i + ", D=" + d);
        try {
            new PidController(p, i, d, mockErrorCalculator, mockErrorHandler);
        } catch (Exception e) {
            grapher.graph();
        }
    }
}