import actors.examples.DataValidator;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.commons.cli.*;
import python.PythonConfig;

import java.io.*;

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

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            String script = cmd.getOptionValue("script");
            String config = cmd.getOptionValue("config");
            String input = cmd.getOptionValue("in");
            String output = cmd.getOptionValue("out");

            executeTests(script, config, input, output);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar adaptive.jar", options, true);
        }

    }

    private static void executeTests(String script, String config, String input, String output) {
        if (!(new File(script).exists())) {
            System.err.println("Error: python script, " + script + " not found");
            System.exit(-1);
        }

        PythonConfig pythonConfig = new PythonConfig();
        pythonConfig.setScript(script);

        if (System.getProperty("python.home") == null) {
            System.err.println("Error: missing python.home system property. Set it to point to the location of ");
            System.err.println("your jython installation directory by supplying -Dpython.home=/path/to/jython");
            System.exit(-1);
        }

//        TODO: this does not work
//        try {
//            //OutputStream errStream = new FileOutputStream("error.log");
//            //OutputStream outStream = new FileOutputStream("output.log");
//
//            //pythonConfig.setErrStream(errStream);
//            //pythonConfig.setOutStream(outStream);
//
//            pythonConfig.setErrStream(System.err);
//            pythonConfig.setOutStream(System.out);
//        } catch (IOException e) {
//            System.err.println("Error: could not create log files in current dir");
//            System.err.println(e.getMessage());
//            System.exit(-1);
//        }

        pythonConfig.setErrStream(System.err);
        pythonConfig.setOutStream(System.out);

        File inFile = new File(input);
        File outFile = new File(output);
        File yamlFile = new File(config);

        if (!inFile.exists()) {
            System.err.println("Error: input file, " + input + " not found");
            System.exit(-1);
        }

        if (!inFile.exists()) {
            System.err.println("Error: yaml file, " + yamlFile + " not found");
            System.exit(-1);

        }

        // start actor system
        ActorSystem system = ActorSystem.create("adaptive-demo");

        ActorRef actor = system.actorOf(DataValidator.props(inFile, outFile), "data-validator");
        actor.tell(new DataValidator.Configure(pythonConfig, yamlFile), ActorRef.noSender());

        system.awaitTermination();
    }
}
