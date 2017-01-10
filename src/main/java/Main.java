import actors.Supervisor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.commons.cli.*;
import util.TextWriter;
import util.Writer;
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
        Options options = new Options();

        options.addOption(OptionBuilder.withArgName("script")
                .hasArg()
                .withArgName("file")
                .withDescription("script file containing test functions")
                .isRequired(true)
                .create("script"));

        options.addOption(OptionBuilder.withArgName("config")
                .hasArg()
                .withArgName("file")
                .withDescription("yaml test configuration file")
                .isRequired(true)
                .create("config"));

        options.addOption(OptionBuilder.withArgName("in")
                .hasArg()
                .withArgName("file")
                .withDescription("input file as tsv")
                .isRequired(true)
                .create("in"));

        options.addOption(OptionBuilder.withArgName("out")
                .hasArg()
                .withArgName("file")
                .withDescription("output file to write report")
                .isRequired(true)
                .create("out"));

        options.addOption(OptionBuilder.withArgName("maxConcurrency")
                .hasArg()
                .withArgName("file")
                .withDescription("max number of concurrent tests (defaults to 5)")
                .create("maxConcurrency"));

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            String script = cmd.getOptionValue("script");
            String config = cmd.getOptionValue("config");
            String input = cmd.getOptionValue("in");
            String output = cmd.getOptionValue("out");

            int maxConcurrency = 5;

            if (cmd.hasOption("maxConcurrency")) {
                maxConcurrency = Integer.parseInt(cmd.getOptionValue("maxConcurrency"));
            }

            executeTests(script, config, maxConcurrency, input, output);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("adaptive", options, true);
        }

    }

    private static void executeTests(String script, String config, int maxConcurrency, String input, String output) {
        TestRunnerFactory factory = new PythonTestRunnerFactory(script, config);
        Reader reader = new CSVReader(input);
        Writer writer = new TextWriter(output);

        ActorSystem system = ActorSystem.create();

        ActorRef workflow = system.actorOf(Props.create(Supervisor.class, reader, writer, factory, maxConcurrency), "supervisor");
        workflow.tell(new Start(), ActorRef.noSender());
    }
}
