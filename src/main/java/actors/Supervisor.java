package actors;

import akka.actor.ActorRef;
import akka.actor.FSM;
import akka.actor.Props;
import akka.actor.UntypedActor;
import validation.TestRunnerFactory;
import messages.*;
import util.Reader;

import java.util.*;

/**
 * Created by lowery on 1/4/17.
 */
public class Supervisor extends UntypedActor {
    private int maxConcurrency = 1000;
    private List<ActorRef> actors = new ArrayList<>();
    private long count;

    private Reader reader;
    private TestRunnerFactory factory;

    public Supervisor(Reader reader, TestRunnerFactory factory) {
        this.reader = reader;
        this.factory = factory;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof Start) {
            for (int i = 0; i < maxConcurrency; i++) {
                ActorRef actorRef = context().actorOf(Props.create(FSMActor.class, factory.createInstance()), "testRunner"+i);
                actorRef.tell(new FSM.SubscribeTransitionCallBack(self()), self());

                actors.add(actorRef);
            }
        } else if (message instanceof FSM.CurrentState) {
            //System.out.println(((FSM.CurrentState) message).state());
            sender().tell(new Initialize(), self());
        } else if (message instanceof FSM.Transition) {
            FSM.Transition transition = ((FSM.Transition) message);
            //System.out.println("from: " + transition.from() + " to: " + transition.to());

            if (transition.from().equals(FSMActor.State.CONSTRUCTED) && transition.to().equals(FSMActor.State.IDLE)) {
                //System.out.println("Done initializing.");
                self().tell(new RequestData(), sender());
            }
        } else if (message instanceof RequestData) {
            Map<String, String> record = reader.next();

            if (record != null) {
                sender().tell(record, self());

                count++;

                if (count % 10000 == 0) {
                    System.out.println("Processed " + count + " records.");
                }
            } else {
               sender().tell(new EndOfStream(), self());
            }
        }
    }
}
