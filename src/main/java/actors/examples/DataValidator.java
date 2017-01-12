package actors.examples;

import actors.FSMActor;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import api.Worker;
import messages.Initialize;
import python.PythonConfig;
import util.CSVReader;
import util.TextWriter;
import validation.TestResult;
import validation.TestRunner;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by lowery on 1/12/17.
 */
public class DataValidator extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final CSVReader reader;
    private final TextWriter writer;

    private Worker testRunner;
    private ActorRef actorRef;

    private int count;

    public DataValidator(File inFile, File outFile) {
        this.reader = new CSVReader(inFile);
        this.writer = new TextWriter(outFile);
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof Configure) {
            File yamlFile = ((Configure) message).yamlFile;
            PythonConfig pythonConfig = ((Configure) message).pythonConfig;

            // initialize test runner instance and supervising actor
            testRunner = new TestRunner(pythonConfig, yamlFile);
            actorRef = context().actorOf(FSMActor.props(testRunner));

            // subscribe to actor state transition events
            actorRef.tell(new FSM.SubscribeTransitionCallBack(self()), self());

            // monitor actor for termination
            getContext().watch(actorRef);
        } else if (message instanceof FSMActor.RequestWork) {
            Map<String, String> record = reader.next();

            if (record != null) {
                actorRef.tell(new FSMActor.DoWork(record), self());
            } else {
                actorRef.tell(new FSMActor.EndOfStream(), self());
            }
        } else if (message instanceof FSMActor.ActorResult) {
            count++;

            List<TestResult> results = (List<TestResult>) ((FSMActor.ActorResult) message).obj;

            for (TestResult result : results) {
                    log.debug("test: " + result.getTest().getName() + " success: "
                            + result.isSuccess() + " message: " + result.getMessage());

                    writer.append(result.toString());
            }
        }

            // monitor fsm state change events
            if (message instanceof FSM.CurrentState) {
                log.debug(((FSM.CurrentState) message).state().toString());

                // initialize
                sender().tell(new Initialize(), self());
            } else if (message instanceof FSM.Transition) {
                FSM.Transition transition = ((FSM.Transition) message);
                log.debug("transition from: " + transition.from() + " to: " + transition.to());

                if (transition.from().equals(FSMActor.State.CONSTRUCTED) &&
                        transition.to().equals(FSMActor.State.IDLE)) {
                    // transition from constructed to initialize, start sending data
                    self().tell(new FSMActor.RequestWork(), actorRef);
                }
            }

            // monitor child actor termination
            if (message instanceof Terminated) {
                log.debug("child actor, " + actorRef + "terminated");
                writer.close();
                log.info("Wrote test results for " + count + " records to file");
                context().system().shutdown();
        }

    }

    public static Props props(final File inFile, final File outFile) {
        return Props.create(new Creator<DataValidator>() {
            private static final long serialVersionUID = 1L;

            public DataValidator create() throws Exception {
                return new DataValidator(inFile, outFile);
            }
        });
    }

    // messages
    public static class Start { }

    public static class Configure {
        public final PythonConfig pythonConfig;
        public final File yamlFile;

        public Configure(PythonConfig pythonConfig, File yamlFile) {
            this.pythonConfig = pythonConfig;
            this.yamlFile = yamlFile;
        }
    }
}
