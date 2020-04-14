package org.molgenis.filter;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.molgenis.filter.yaml.YamlLoader;

public class FilterTool {

  public static final String INPUT = "input";
  public static final String OUTPUT = "output";
  public static final String REPLACE = "replace";
  public static final String SAMPLE = "sample";
  public static final String PARAMS = "params";
  private static final String FILTERFILE = "filterFile";
  public static final String FILTER_LABELS = "FILTER_LABELS";
  private static final String ROUTE = "route";
  private static final String OUTPUT_FILE_POSTFIX = ".filtered";
  private static final String FILTER_FILE_POSTFIX = ".filter";
  private static final String ROUTE_FILE_POSTFIX = ".route";
  private static final String TSV = ".tsv";
  private static final String YML = ".yml";
  public static final String GZIP_EXTENSION = ".gz";
  private static final String FILTER_FILE_HEADER = "Filterfile";
  private static final String ROUTES_FILE_HEADER = "Routesfile";
  private static final String IS_LOGIC = "isLogicFiltering";

  public static void main(String[] args) {
    OptionParser parser = createOptionParser();
    OptionSet options = parser.parse(args);
    new FilterTool().run(options);
  }

  private static OptionParser createOptionParser() {
    OptionParser parser = new OptionParser();

    parser.acceptsAll(asList("f", FILTERFILE), "Filter rules file").withRequiredArg()
        .ofType(File.class);
    parser.acceptsAll(asList("i", INPUT), "Input VCF file").withRequiredArg().ofType(File.class);
    parser.acceptsAll(asList("o", OUTPUT), "Output directory").withRequiredArg().ofType(File.class);
    parser.acceptsAll(asList("r", REPLACE), "Enables output files overwrite");
    parser.acceptsAll(asList("q", ROUTE), "Generate a 'route' file");
    parser.acceptsAll(asList("p", PARAMS), "Parameters to be replaced in the filter file, formet 'KEY1=VALUE1;KEY2=VALUE2'").withRequiredArg().ofType(String.class);
    parser.acceptsAll(asList("s", SAMPLE), "Sample identifier").withRequiredArg().ofType(String.class);
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

    File filterFile = (File) options.valueOf(FILTERFILE);
    if (!filterFile.exists()) {
      System.out.println("Filter rule file not found at " + filterFile);
      return;
    } else if (filterFile.isDirectory()) {
      System.out.println("Filter rule file is a directory, not a file!");
      return;
    }

    File outputDir = (File) options.valueOf(OUTPUT);
    if (!outputDir.exists()) {
      System.out.println("Output directory does not exist, creating it now.");
      outputDir.mkdir();
    } else if (!outputDir.isDirectory()) {
      throw new RuntimeException("Output directory does not seem to be a directory.");
    }

    boolean isReplace = options.has(REPLACE);
    boolean isLogicFiltering = options.has(IS_LOGIC);

    String fullInputFileName = inputFile.getName();
    String extension = fullInputFileName.substring(fullInputFileName.indexOf('.'));
    String inputFileName = fullInputFileName.replace(extension,"");
    File outputFile = createOutputFile(inputFileName, outputDir, OUTPUT_FILE_POSTFIX + extension, isReplace);
    File archivedFilterFile = createOutputFile(inputFileName, outputDir, FILTER_FILE_POSTFIX+YML, isReplace);
    File routesFile = createOutputFile(inputFileName, outputDir, ROUTE_FILE_POSTFIX+TSV, isReplace);

    Map<String, String> params;
    if(options.hasArgument(PARAMS)){
      params = loadParams(options.valueOf(PARAMS).toString());
    }else{
      params = Collections.emptyMap();
    }

    String sampleId = null;
    if(options.hasArgument(SAMPLE)){
      sampleId = options.valueOf(SAMPLE).toString();
    }

    try {
      Map<String, FilterStep> filters = YamlLoader.loadFilterTree(filterFile, params, sampleId);

      try (FileOutputStream copyStream = new FileOutputStream(archivedFilterFile)) {
        Files.copy(filterFile.toPath(), copyStream);
      }

      String filterFileHeaderName = FILTER_FILE_HEADER;
      String routesFileHeaderName = ROUTES_FILE_HEADER;
      String filterLabelsInfoField = FILTER_LABELS;

      FilterRunner filterRunner = new FilterRunner(inputFile, extension, outputFile, archivedFilterFile,filterFileHeaderName, routesFileHeaderName, filterLabelsInfoField, routesFile, isLogicFiltering);
      filterRunner.runFilters(filters);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Map<String, String> loadParams(String paramString) {
    Map<String, String> params = new HashMap<>();
    String[] paramsArray = paramString.split(";");
    for(String param : paramsArray){
      String[] paramArray = param.split("=");
      if(paramArray.length == 2){
        params.put(paramArray[0],paramArray[1]);
      }else{
        throw new RuntimeException("Params should be of format 'key=value'");
      }
    }
    return params;
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
