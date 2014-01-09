package navigation;

public class PidController {
    protected final float p_;
    protected final float i_;
    protected final float d_;
    protected volatile float last_err;
    protected volatile float cumulative_err;

    public PidController(float p, float i, float d, final ErrorCalculator ec, final ErrorHandler eh) {
        this.p_ = p; // This should be a positive number.
        this.i_ = i; // This should be 0 or near 0.
        this.d_ = d; // This should be used if we are experiencing overshoot.
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    float err = ec.getError();
                    float p_term = p_ * err;
                    cumulative_err += err; // TODO: Multiply by dt.
                    float i_term = i_ * cumulative_err;
                    float d_term = d_ * (err - last_err);
                    last_err = err;
                    eh.handleError(p_term + i_term + d_term);
                }
            }
        })).run();
    }
}
