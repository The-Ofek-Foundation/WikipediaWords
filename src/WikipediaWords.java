import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * WikipediaWords
 *
 * This program loads and parses random Wikipedia articles
 * in order to create statistics on the most common words
 * in article titles, headers, and general content.
 *
 * @author Ofek Gila
 * @since September 3rd, 2016
 * @version September 6th, 2016
 */

public class WikipediaWords {

	public static Option help = new Option("h", "help", false, "print this message");
	public static Option runTimeOption = Option.builder("t")
			.longOpt("run-time")
			.hasArg()
			.type(Double.class)
			.desc("number of seconds to run for (float)")
			.build();
	public static Option numThreadsOption = Option.builder("n")
			.longOpt("num-threads")
			.hasArg()
			.type(Integer.class)
			.desc("number of threads to use (int)")
			.build();
	public static Option saveToFileOption = Option.builder("o")
			.longOpt("output")
			.hasArg(false)
			.desc("save sorted list to results file")
			.build();
	public static Option cumulativeOption = Option.builder("c")
			.longOpt("cumulative")
			.hasArg(false)
			.desc("load and save results to build off other executions")
			.build();
	public static Options options = new Options()
		.addOption(help)
		.addOption(runTimeOption)
		.addOption(numThreadsOption)
		.addOption(saveToFileOption)
		.addOption(cumulativeOption);
	public static CommandLineParser parser = new DefaultParser();

	public static void main(String... pumpkins) {

		boolean printHelp = true;

		try {
			CommandLine line = parser.parse(options, pumpkins);
			if (!line.hasOption("help"))	{
				double runTime = Double.parseDouble(line.getOptionValue("run-time"));
				int numThreads = line.hasOption("num-threads") ? Integer.parseInt(line.getOptionValue("num-threads")):1;
				boolean saveToFile = line.hasOption("output");
				boolean cumulative = line.hasOption("cumulative");

				// try {
				// 	for (int i = numThreads; 1 == 1; i++) {
				// 		WikipediaWordsRunner WWR = new WikipediaWordsRunner(100f, i, false, false);
				// 		WWR.run();
				// 		Thread.sleep(110000);
				// 	}
				// }	catch (Exception e) {}
				// System.exit(0);

				WikipediaWordsRunner WWR = new WikipediaWordsRunner(runTime, numThreads, saveToFile, cumulative);
				WWR.run();
				printHelp = false;
			}
		}	catch (MissingArgumentException maException) {
			System.err.printf("Argument required for %s!!!\n", maException.getOption().getOpt());
		}	catch (MissingOptionException moException) {
			System.err.printf("Runtime option required %s!!!\n", moException.getMissingOptions());
		}	catch (ParseException pException)	{
			System.err.println("Error parsing args");
			System.exit(51232);
		}	catch (NumberFormatException nfException) {
			System.err.println("Input format mismatch!!!");
			System.exit(51233);
		}	catch (NullPointerException npException) {}
		if (printHelp)
			printHelpText();
	}

	/**
	 * Prints the command line help text for this program.
	 */
	public static void printHelpText() {
		System.out.println();
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("./run -t <run-time> <params>", options);
		System.out.println();
	}
}
