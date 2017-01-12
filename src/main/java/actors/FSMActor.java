package actors;

import akka.actor.AbstractLoggingFSM;
import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

import akka.actor.Props;
import akka.japi.Creator;
import api.Worker;
import scala.concurrent.ExecutionContext;

import messages.*;
import scala.concurrent.Future;

/**
 * Created by lowery on 1/4/17.
 */
public class FSMActor extends AbstractLoggingFSM<FSMActor.State, FSMActor.Data> {
    private ExecutionContext ec = context().dispatcher();
    private Worker worker;

    public FSMActor(Worker worker) {
        this.worker = worker;
    }

    {
        startWith(State.CONSTRUCTED, new Data());

        when(State.CONSTRUCTED, matchEvent(Initialize.class, Data.class, (initialize, data) -> {
                worker.initialize();

                return goTo(State.IDLE);
            })
        );

        when(State.IDLE, matchEvent(DoWork.class, Data.class, (message, data) -> {
                Future<Worker.WorkComplete> future = future(worker.callable(message.obj), ec);
                pipe(future, ec).to(self(), sender());

                return goTo(State.BUSY);
            })
        );

        when(State.IDLE, matchEvent(EndOfStream.class, Data.class, (end, data) -> {
                worker.shutdown();
                context().stop(self());

                return goTo(State.ENDED);
            })
        );

        when(State.BUSY, matchEvent(Worker.WorkComplete.class, Data.class, (message, data) -> {
                ActorResult result = new ActorResult(message.obj);
                sender().tell(result, self());

                return goTo(State.IDLE).replying(new RequestWork());
            })
        );

        when(State.ENDED, matchAnyEvent((msg, data) -> {
                log().error("Message received during actor termination!");
                return stay();
            })
        );

        onTermination(matchStop(Shutdown(), (state, data) -> {
                log().info("Finished all tasks, shutting down");
            })
        );
    }

    public static Props props(final Worker worker) {
        return Props.create(new Creator<FSMActor>() {
            private static final long serialVersionUID = 1L;

            public FSMActor create() throws Exception {
                return new FSMActor(worker);
            }
        });
    }

    // states
    public enum State {
        CONSTRUCTED,
        IDLE,
        BUSY,
        ENDED

    }

    // state data
    public class Data {

    }

    // messages
    public static class RequestWork { }

    public static class DoWork {
        public final Object obj;

        public DoWork(Object obj) {
            this.obj = obj;
        }
    }

    public class ActorResult {
        public final Object obj;

        public ActorResult(Object obj) {
            this.obj = obj;
        }
    }

    public static class EndOfStream { }

}