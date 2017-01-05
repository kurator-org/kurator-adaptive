import actors.Supervisor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import validation.PythonTestRunnerFactory;
import validation.TestRunnerFactory;
import messages.Start;
import util.CSVReader;
import util.Reader;

/**
 * Created by lowery on 1/4/17.
 */
public class Main {
    public static void main(String[] args) {
        TestRunnerFactory factory = new PythonTestRunnerFactory(args[0], args[1]);
        Reader reader = new CSVReader(args[2]);

        ActorSystem system = ActorSystem.create();

        ActorRef workflow = system.actorOf(Props.create(Supervisor.class, reader, factory), "workflow");
        workflow.tell(new Start(), ActorRef.noSender());
    }
}
