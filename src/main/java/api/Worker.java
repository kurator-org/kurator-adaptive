package api;

import java.util.Map;
import java.util.concurrent.Callable;

public interface Worker {
    public void initialize();
    public Callable<WorkComplete> callable(final Object obj);
    public void shutdown();

    public static class WorkComplete {
        public final Object obj;

        public WorkComplete(Object obj) {
            this.obj = obj;
        }
    }
}