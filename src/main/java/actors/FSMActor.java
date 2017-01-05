package actors;

import akka.actor.AbstractLoggingFSM;
import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

import validation.PythonTestRunner;
import validation.TestResult;
import messages.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by lowery on 1/4/17.
 */
public class FSMActor extends AbstractLoggingFSM<FSMActor.State, FSMActor.Data> {
    private PythonTestRunner testRunner;

    public FSMActor(PythonTestRunner testRunner) {
        this.testRunner = testRunner;
    }

    {
        startWith(State.CONSTRUCTED, new Data());

        when(State.CONSTRUCTED, matchEvent(Initialize.class, Data.class, (initialize, data) -> {
                    log().info("Initializing jython interpreter");

                    testRunner.init();
                    log().info("Jython interpreter initialized");
                    return goTo(State.IDLE).replying(new Initialized());
                })
        );

        when(State.IDLE, matchEvent(Map.class, Data.class, (message, data) -> {
                    pipe(future(new Callable<ActorResult>() {
                        @Override
                        public ActorResult call() throws Exception {
                            List<TestResult> result = testRunner.run(message);
                            return new ActorResult(result);
                        }
                    }, context().dispatcher()), context().dispatcher()).to(self(), sender());
                    return goTo(State.BUSY); //.replying(new RequestData());
                })
        );

        when(State.IDLE, matchEvent(EndOfStream.class, Data.class, (end, data) -> {
            testRunner.close();
            return stop();
        }));

        when(State.BUSY, matchEvent(ActorResult.class, Data.class, (result, data) -> {
                    //System.out.println(result.obj);
                    return goTo(State.IDLE).replying(new RequestData());
                })
        );
    }

    // states
    enum State {
        CONSTRUCTED,
        INITIALIZING,
        IDLE,
        BUSY,
        ENDED

    }

    // state data
    class Data {

    }
}