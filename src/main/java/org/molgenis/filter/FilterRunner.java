package org.molgenis.filter;

import static java.util.Objects.requireNonNull;
import static org.molgenis.filter.FilterTool.GZIP_EXTENSION;
import static org.molgenis.vcf.utils.VcfUtils.getRecordIdentifierString;
import static org.molgenis.vcf.utils.VcfUtils.getVcfReader;
import static org.molgenis.vcf.utils.VcfUtils.getVcfWriter;
import static org.molgenis.vcf.utils.VcfUtils.writeRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.io.output.NullWriter;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfWriter;
import org.molgenis.vcf.utils.VcfUtils;

public class FilterRunner {
  private final File inputFile;
  private final String extension;
  private final File outputFile;
  private final File archivedFilterFile;
  private final File routesFile;
  private final String filterFileHeaderName;
  private final String routesFileHeaderName;
  private final String filterLabelsInfoField;
  private boolean isLogRoute;

  public FilterRunner(File inputFile, String extension, File outputFile,
      File archivedFilterFile, String filterFileHeaderName,
      String routesFileHeaderName, String filterLabelsInfoField, File routesFile) {
    this.inputFile = requireNonNull(inputFile);
    this.extension = requireNonNull(extension);
    this.outputFile = requireNonNull(outputFile);
    this.archivedFilterFile = archivedFilterFile;
    this.filterFileHeaderName = filterFileHeaderName;
    this.routesFileHeaderName = routesFileHeaderName;
    this.filterLabelsInfoField = filterLabelsInfoField;
    this.routesFile = routesFile;
    this.isLogRoute = routesFile != null;
  }

  public void runFilters(Map<String, FilterStep> filters) throws Exception {
    Map<String, String> additionalHeaders = new HashMap();
    if(filterFileHeaderName != null && archivedFilterFile != null) {
      additionalHeaders.put(filterFileHeaderName, archivedFilterFile.getName());
    }
    if(isLogRoute) {
      additionalHeaders.put(routesFileHeaderName, routesFile.getName());
    }
    Writer routesWriter = getRoutesWriter(routesFile);

    VcfReader reader = getVcfReader(inputFile, extension.endsWith(GZIP_EXTENSION));
    VcfWriter vcfWriter = getVcfWriter(outputFile, reader.getVcfMeta(), additionalHeaders, extension.endsWith(
        GZIP_EXTENSION));

    Stream<VcfRecord> recordStream = StreamSupport
        .stream(reader.spliterator(), false);

    Stream<VcfRecord> filtered = recordStream
        .map(record -> VcfUtils.addInfoField(record, filterLabelsInfoField, ".")).map(record -> {
          try {
            return startFiltering(record, filters, routesWriter);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }).filter(Objects::nonNull).map(FilterResult::getRecord);
    filtered.forEach(record -> writeRecord(record, vcfWriter));
    vcfWriter.close();
    routesWriter.close();
    System.out.println("Filtered!");
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

  private Writer getRoutesWriter(File routesFile) throws IOException {
    if(isLogRoute) {
      return new FileWriter(routesFile);
    }else{
      return new NullWriter();
    }
  }



  private FilterResult processFilterAction(Map<String, FilterStep> filters,
      FilterStep filterStep, VcfRecord record, StringBuilder route) {
    FilterResult filterResult = filterStep.getFilter().filter(record);
    FilterAction action = filterStep.getAction(filterResult.getResult());
    appendToRoute(route, " > ");
    appendToRoute(route, filterStep.getKey()+"("+filterResult.getResult()+")");
    processLabels(record, filterResult, action);
    if (action.getState() == FilterState.KEEP) {
      appendToRoute(route, " > KEEP" + "\n");
      return filterResult;
    } else if (action.getState() == FilterState.REMOVE) {
      appendToRoute(route, " > REMOVE" + "\n");
      return null;
    } else {
      String nextFilter = action.getNextStep();
      FilterStep nextFilterStep = filters.get(nextFilter);
      if (nextFilterStep == null) {
        throw new RuntimeException("Filterstep '" + nextFilter + "' could not be resolved.");
      }
      return processFilterAction(filters, nextFilterStep, filterResult.getRecord(), route);
    }
  }

  private void appendToRoute(StringBuilder route, String s) {
    if (isLogRoute) {
      route.append(s);
    }
  }

  private void processLabels(VcfRecord record, FilterResult filterResult, FilterAction action) {
    if (action.getLabel() != null) {
      String currentLabels = VcfUtils.getInfoFieldValue(record, filterLabelsInfoField);
      currentLabels = currentLabels.equals(".") ? "" : currentLabels + ",";
      String labels = currentLabels + action.getLabel();
      filterResult.setRecord(VcfUtils.updateInfoField(record, filterLabelsInfoField, labels));
    }
  }
}
