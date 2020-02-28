package org.molgenis.filter;

import static java.util.Arrays.asList;
import static org.molgenis.filter.FilterLoader.loadFilters;
import static org.molgenis.vcf.utils.VcfUtils.getRecordIdentifierString;
import static org.molgenis.vcf.utils.VcfUtils.getVcfReader;
import static org.molgenis.vcf.utils.VcfUtils.getVcfWriter;
import static org.molgenis.vcf.utils.VcfUtils.writeRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.io.output.NullWriter;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfWriter;
import org.molgenis.vcf.utils.VcfUtils;

public class FilterTool {

  public static final String INPUT = "input";
  public static final String OUTPUT = "output";
  public static final String REPLACE = "replace";
  private static final String FILTERFILE = "filterFile";
  private static final String FILTER_LABELS = "FILTER_LABELS";
  private static final String ROUTE = "route";
  private static final String OUTPUT_FILE_POSTFIX = ".filtered";
  private static final String FILTER_FILE_POSTFIX = ".filter";
  private static final String ROUTE_FILE_POSTFIX = ".route";
  private static final String TSV = ".tsv";
  private static final String GZIP_EXTENSION = ".gz";
  private static boolean isLogRoute;

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

    String fullInputFileName = inputFile.getName();
    String extension = fullInputFileName.substring(fullInputFileName.indexOf("."));
    String inputFileName = fullInputFileName.replace(extension,"");
    File outputFile = createOutputFile(inputFileName, outputDir, OUTPUT_FILE_POSTFIX + extension, isReplace);
    File archivedFilterFile = createOutputFile(inputFileName, outputDir, FILTER_FILE_POSTFIX+TSV, isReplace);
    File routesFile = createOutputFile(inputFileName, outputDir, ROUTE_FILE_POSTFIX+TSV, isReplace);

    if (options.has(ROUTE)) {
      isLogRoute = true;
    }

    try {
      Map<String, FilterStep> filters = loadFilters(filterFile);

      try (FileOutputStream copyStream = new FileOutputStream(archivedFilterFile)) {
        Files.copy(filterFile.toPath(), copyStream);
      }

      Map<String, String> additionalHeaders = new HashMap();
      additionalHeaders.put("Filterfile", archivedFilterFile.getName());
      if(isLogRoute) {
        additionalHeaders.put("Routesfile", routesFile.getName());
      }
      Writer routesWriter = getRoutesWriter(routesFile);

      VcfReader reader = getVcfReader(inputFile, extension.endsWith(GZIP_EXTENSION));
      VcfWriter vcfWriter = getVcfWriter(outputFile, reader.getVcfMeta(), additionalHeaders, extension.endsWith(
          GZIP_EXTENSION));

      Stream<VcfRecord> recordStream = StreamSupport
          .stream(reader.spliterator(), false);

      Stream<VcfRecord> filtered = recordStream
          .map(record -> VcfUtils.addInfoField(record, FILTER_LABELS, ".")).map(record -> {
            try {
              return startFiltering(record, filters, routesWriter);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }).filter(FilterResult::getPass)
          .map(FilterResult::getRecord);
      filtered.forEach(record -> writeRecord(record, vcfWriter));
      vcfWriter.close();
      routesWriter.close();
      System.out.println("Filtered!");
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

  private Writer getRoutesWriter(File routesFile) throws IOException {
    if(isLogRoute) {
      return new FileWriter(routesFile);
    }else{
      return new NullWriter();
    }
  }

  private FilterResult startFiltering(VcfRecord record, Map<String, FilterStep> filters,
      Writer routesWriter) throws IOException {
    FilterStep filterStep = StreamSupport.stream(filters.entrySet().spliterator(), false)
        .findFirst().get().getValue();
    StringBuilder route = new StringBuilder(getRecordIdentifierString(record));
    FilterResult result = processFilterAction(filters, filterStep, record, route);
    routesWriter.write(route.toString());
    return result;
  }

  private FilterResult processFilterAction(Map<String, FilterStep> filters,
      FilterStep filterStep, VcfRecord record, StringBuilder route) {
    FilterResult filterResult = filterStep.getFilter().filter(record);
    FilterAction action = filterStep.getAction(filterResult.getPass());
    appendToRoute(route, " > ");
    appendToRoute(route, filterStep.getKey());
    processLabels(record, filterResult, action);
    if (action.getState() == FilterState.KEEP) {
      appendToRoute(route, " > KEEP" + "\n");
    } else if (action.getState() == FilterState.REMOVE) {
      appendToRoute(route, " > REMOVE" + "\n");
    } else {
      String nextFilter = action.getNextStep();
      FilterStep nextFilterStep = filters.get(nextFilter);
      if (nextFilterStep == null) {
        throw new RuntimeException("Filterstep '" + nextFilter + "' could not be resolved.");
      }
      return processFilterAction(filters, nextFilterStep, filterResult.getRecord(), route);
    }
    return filterResult;
  }

  private static void appendToRoute(StringBuilder route, String s) {
    if (isLogRoute) {
      route.append(s);
    }
  }

  private void processLabels(VcfRecord record, FilterResult filterResult, FilterAction action) {
    if (action.getLabel() != null) {
      String currentLabels = VcfUtils.getInfoFieldValue(record, FILTER_LABELS);
      currentLabels = currentLabels.equals(".") ? "" : currentLabels + ",";
      String labels = currentLabels + action.getLabel();
      filterResult.setRecord(VcfUtils.updateInfoField(record, FILTER_LABELS, labels));
    }
  }
}
