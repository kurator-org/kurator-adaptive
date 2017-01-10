package actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import util.Writer;
import validation.TestResult;
import validation.TestRunnerFactory;
import messages.*;
import util.Reader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by lowery on 1/4/17.
 */
public class Supervisor extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private int maxConcurrency;
    private int activeInstances = 0;
    private boolean endOfStream = false;

    private List<ActorRef> actors = new ArrayList<>();

    private long startTime;

    private long count;
    private int numResults;
    private int numFailed;
    private int numPassed;

    private Reader reader;
    private Writer writer;
    private TestRunnerFactory factory;

    public Supervisor(Reader reader, Writer writer, TestRunnerFactory factory, int maxConcurrency) {
        this.reader = reader;
        this.writer = writer;
        this.factory = factory;
        this.maxConcurrency = maxConcurrency;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof Start) {
            log.info("Workflow supervisor received start message, initializing test runners");
            for (int i = 0; i < maxConcurrency; i++) {
                ActorRef actorRef = context().actorOf(Props.create(FSMActor.class, factory.createInstance()), "testRunner"+i);
                actorRef.tell(new FSM.SubscribeTransitionCallBack(self()), self());
                getContext().watch(actorRef); // monitor actor for things like termination message
                actors.add(actorRef);
            }
        } else if (message instanceof FSM.CurrentState) {
            //System.out.println(((FSM.CurrentState) message).state());
            sender().tell(new Initialize(), self());
        } else if (message instanceof FSM.Transition) {
            FSM.Transition transition = ((FSM.Transition) message);
            //System.out.println("from: " + transition.from() + " to: " + transition.to());

            if (transition.from().equals(FSMActor.State.CONSTRUCTED) && transition.to().equals(FSMActor.State.IDLE)) {
                activeInstances++;
                if (activeInstances == actors.size()) {
                    log.info("Successfully initialized " + activeInstances + " test runner instance(s), running tests on input data");
                    startTime = System.currentTimeMillis();
                }
                self().tell(new RequestData(), sender());
            }
        } else if (message instanceof RequestData) {
            Map<String, String> record = reader.next();

            if (record != null) {
                sender().tell(record, self());

                count++;

                if (count % 10000 == 0) {
                    //System.out.println("Processed " + count + " records.");
                }
            } else {
                if (!endOfStream) {
                    Date endTime = new Date(System.currentTimeMillis() - startTime);
                    DateFormat format = new SimpleDateFormat("mm:ss");
                    log.info("End of stream reached, processed " + count + " records, (time: " + format.format(endTime)+ "). Waiting for test runners to shut down");
                    endOfStream = true;
                }
                sender().tell(new EndOfStream(), self());
            }
        } else if (message instanceof ActorResult) {
            List<TestResult> results = (List<TestResult>) ((ActorResult) message).obj;
            for (TestResult result : results) {
                if (!result.passed()) {
                    writer.append(result.toString());
                    numFailed++;
                } else {
                    numPassed++;
                }
            }
            numResults++;
            if (endOfStream && numResults == count) {
                writer.close();
                log.info("Wrote " + numFailed + " actionable test results to file (passed: " + numPassed + ", failed: " + numFailed + ", total: " + (numPassed + numFailed) + ")");
            }
        } else if (message instanceof Terminated) {
            activeInstances--;
            if (activeInstances == 0) {
                log.info("Termination message received from all test runners. Shutting down actor system.");
                getContext().system().shutdown();
            }
        }
    }
}
