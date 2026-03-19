package co.com.clients.parent.utility;

import io.micrometer.observation.Observation;

public class ContextAwareRunnable implements Runnable {
    private final Runnable delegate;
    private final Observation observation;

    public ContextAwareRunnable(Runnable delegate, Observation observation) {
        this.delegate = delegate;
        this.observation = observation;
    }

    @Override
    public void run() {
        try (Observation.Scope scope = observation.openScope()) {
            delegate.run();
        }
    }
}
