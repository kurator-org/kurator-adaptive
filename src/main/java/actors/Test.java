package actors;

import actors.kafka.InputActor;
import actors.kafka.KafkaConsumerActor;
import actors.kafka.KafkaSupervisor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import messages.Start;
import util.CSVReader;
import util.Reader;
import util.TextWriter;
import util.Writer;
import validation.PythonTestRunnerFactory;
import validation.TestRunnerFactory;

/**
 * Created by lowery on 1/11/2017.
 */
public class Test {
    public static void main(String[] args) {
        Reader reader = new CSVReader("C:\\Users\\lowery\\IdeaProjects\\kurator-adaptive\\src\\main\\resources\\occurrence.txt");

        ActorSystem system = ActorSystem.create();
        ActorRef supervisor = system.actorOf(Props.create(KafkaSupervisor.class, reader, "eventDate"), "supervisor");

        supervisor.tell(new Start(), ActorRef.noSender());
    }
}
