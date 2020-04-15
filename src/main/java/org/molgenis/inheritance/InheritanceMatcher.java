package org.molgenis.inheritance;

import static java.util.Arrays.asList;
import static org.molgenis.inheritance.InheritanceFilters.NON_PENETRANCE;

import java.io.File;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.molgenis.filter.FilterRunner;

public class InheritanceMatcher {

  public static final String INPUT = "input";
  public static final String OUTPUT = "output";
  public static final String REPLACE = "replace";
  public static final String SAMPLE = "sample";
  private static final String PEDIGREE_FILE = "pedigreeFile";
  private static final String INHERITANCE_LABELS = "INHERITANCE";
  private static final String ROUTE = "route";
  private static final String OUTPUT_FILE_POSTFIX = ".inheritance";
  private static final String ROUTE_FILE_POSTFIX = ".route";
  private static final String TSV = ".tsv";
  private static final String FILTER_FILE_HEADER = "Filterfile";
  private static final String ROUTES_FILE_HEADER = "Routesfile";
  private static final String IS_LOGIC = "isLogicFiltering";

  public static void main(String[] args) {
    OptionParser parser = createOptionParser();
    OptionSet options = parser.parse(args);
    new InheritanceMatcher().run(options);
  }

  private static OptionParser createOptionParser() {
    OptionParser parser = new OptionParser();

    parser.acceptsAll(asList("p", PEDIGREE_FILE), "pedigree file").withRequiredArg()
        .ofType(File.class);
    parser.acceptsAll(asList("i", INPUT), "Input VCF file").withRequiredArg().ofType(File.class);
    parser.acceptsAll(asList("o", OUTPUT), "Output directory").withRequiredArg().ofType(File.class);
    parser.acceptsAll(asList("r", REPLACE), "Enables output files overwrite");
    parser.acceptsAll(asList("q", ROUTE), "Generate a 'route' file");
    parser.acceptsAll(asList("s", SAMPLE), "Sample identifier").withRequiredArg().ofType(String.class);
    parser.acceptsAll(asList("n", NON_PENETRANCE), "Non penetrance");
    parser.acceptsAll(asList("l", IS_LOGIC), "Logic filtering");
    return parser;
  }

  public void run(OptionSet options) {
    File inputFile = (File) options.valueOf(INPUT);
    if (!inputFile.exists()) {
      System.out.println("Input VCF file not found at " + inputFile);
      return;
    } else if (inputFile.isDirectory()) {
      System.out.println("Input VCF file is a directory, not a file!");
      return;
    }

    File pedigreeFile = (File) options.valueOf(PEDIGREE_FILE);
    if (!pedigreeFile.exists()) {
      System.out.println("Pedigree file not found at " + pedigreeFile);
      return;
    } else if (pedigreeFile.isDirectory()) {
      System.out.println("Pedigree file is a directory, not a file!");
      return;
    }

    File outputDir = (File) options.valueOf(OUTPUT);
    if (!outputDir.exists()) {
      System.out.println("Output directory does not exist, creating it now.");
      outputDir.mkdir();
    } else if (!outputDir.isDirectory()) {
      throw new RuntimeException("Output directory does not seem to be a directory.");
    }

    boolean nonPenetrance = options.has(NON_PENETRANCE);
    if (!outputDir.exists()) {
      System.out.println("Output directory does not exist, creating it now.");
      outputDir.mkdir();
    } else if (!outputDir.isDirectory()) {
      throw new RuntimeException("Output directory does not seem to be a directory.");
    }

    boolean isReplace = options.has(REPLACE);
    boolean isLogicFiltering = options.has(IS_LOGIC);

    String fullInputFileName = inputFile.getName();
    String extension = fullInputFileName.substring(fullInputFileName.indexOf("."));
    String inputFileName = fullInputFileName.replace(extension,"");
    File outputFile = createOutputFile(inputFileName, outputDir, OUTPUT_FILE_POSTFIX + extension, isReplace);
    File routesFile = createOutputFile(inputFileName, outputDir, ROUTE_FILE_POSTFIX+TSV, isReplace);

    String sampleId = null;
    if(options.hasArgument(SAMPLE)){
      sampleId = options.valueOf(SAMPLE).toString();
    }

    try {
      String filterFileHeaderName = FILTER_FILE_HEADER;
      String routesFileHeaderName = ROUTES_FILE_HEADER;
      String filterLabelsInfoField = INHERITANCE_LABELS;

      FilterRunner filterRunner = new FilterRunner(inputFile, extension, outputFile, null,filterFileHeaderName, routesFileHeaderName, filterLabelsInfoField, routesFile, isLogicFiltering);
      InheritanceTree inheritanceTree = new InheritanceTree(filterRunner, nonPenetrance, inputFile, pedigreeFile, sampleId);
      inheritanceTree.match();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private File createOutputFile(String inputFileName, File outputDir,
      String postfix, boolean isReplace) {
    File outputFile = new File(outputDir.getAbsolutePath()+File.separator+inputFileName+postfix);
    if (outputFile.exists()) {
      if (isReplace) {
        System.out
            .println("Override enabled, replacing existing output file with specified output: "
                + outputFile.getAbsolutePath());
      } else {
        throw new RuntimeException(
            "Output file already exists, please either enter a different output name or use the '-r' option to overwrite the output file.");
      }
    }
    return outputFile;
  }

}
