package org.molgenis.filter;

import static java.util.Objects.requireNonNull;
import static org.molgenis.filter.FilterResultEnum.TRUE;
import static org.molgenis.filter.FilterTool.GZIP_EXTENSION;
import static org.molgenis.vcf.utils.VcfUtils.addOrUpdateInfoField;
import static org.molgenis.vcf.utils.VcfUtils.getRecordIdentifierString;
import static org.molgenis.vcf.utils.VcfUtils.getVcfReader;
import static org.molgenis.vcf.utils.VcfUtils.getVcfWriter;
import static org.molgenis.vcf.utils.VcfUtils.updateInfoField;
import static org.molgenis.vcf.utils.VcfUtils.writeRecord;
import static org.molgenis.vcf.utils.VepUtils.VEP_INFO_NAME;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import joptsimple.internal.Strings;
import org.apache.commons.io.output.NullWriter;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfWriter;
import org.molgenis.vcf.utils.VcfUtils;
import org.molgenis.vcf.utils.VepUtils;

public class FilterRunner {

  private static final String LOGIC_FILTER_RESULT_TRUE = "TRUE";
  private static final String LOGIC_FILTER_RESULT_FALSE = "FALSE";
  private final File inputFile;
  private final String extension;
  private final File outputFile;
  private final File archivedFilterFile;
  private final File routesFile;
  private final String filterFileHeaderName;
  private final String routesFileHeaderName;
  private final String filterLabelsInfoField;
  private final boolean isLogicFiltering;
  private final String routeInfoField;
  private boolean isLogRoute;

  private String LOGIC_FILTER_KEY = "FILTER_RESULT";
  private static final String LOGIC_FILTER_DESC = "";

  public FilterRunner(File inputFile, String extension, File outputFile,
      File archivedFilterFile, String filterFileHeaderName,
      String routesFileHeaderName, String filterLabelsInfoField, File routesFile,
      boolean isLogicFiltering) {
    this.inputFile = requireNonNull(inputFile);
    this.extension = requireNonNull(extension);
    this.outputFile = requireNonNull(outputFile);
    this.archivedFilterFile = archivedFilterFile;
    this.filterFileHeaderName = filterFileHeaderName;
    this.routesFileHeaderName = routesFileHeaderName;
    this.filterLabelsInfoField = filterLabelsInfoField + "_LABELS";
    this.routeInfoField = filterLabelsInfoField + "_ROUTE";
    this.routesFile = routesFile;
    this.isLogRoute = routesFile != null;
    this.isLogicFiltering = isLogicFiltering;
  }

  public void runFilters(Map<String, FilterStep> filters) throws Exception {
    Map<String, String> additionalHeaders = new HashMap();
    if (filterFileHeaderName != null && archivedFilterFile != null) {
      additionalHeaders.put(filterFileHeaderName, archivedFilterFile.getName());
    }
    if (isLogRoute) {
      additionalHeaders.put(routesFileHeaderName, routesFile.getName());
    }
    Writer routesWriter = getRoutesWriter(routesFile);

    VcfReader reader = getVcfReader(inputFile, extension.endsWith(GZIP_EXTENSION));
    VcfWriter vcfWriter = getVcfWriter(outputFile, reader.getVcfMeta(), additionalHeaders,
        extension.endsWith(
            GZIP_EXTENSION));

    Stream<VcfRecord> recordStream = StreamSupport
        .stream(reader.spliterator(), false);

    Stream<VcfRecord> filtered = recordStream
        .map(record -> VcfUtils
            .addOrUpdateInfoField(record, filterLabelsInfoField, ".", ".", ".", true))
        .map(record -> VcfUtils
            .addOrUpdateInfoField(record, routeInfoField, ".", ".", ".", true))
        .map(record -> {
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
    List<VcfRecord> splittedRecords = splitRecord(record);
    List<FilterResult> results = new ArrayList<>();
    int index = 0;
    for (VcfRecord splittedRecord : splittedRecords) {
      StringBuilder route = new StringBuilder(getRecordIdentifierString(record) + "_" + index);
      FilterResult result = processFilterAction(filters, filterStep, splittedRecord, route);
      if (result != null) {
        results.add(result);
      }
      routesWriter.write(route.toString());
      index++;
    }
    if (results.isEmpty()) {
      return null;
    }
    return mergeResults(results, record);
  }

  private FilterResult mergeResults(List<FilterResult> results, VcfRecord vcfRecord) {
    List<String> vepValues = new ArrayList<>();
    Set<String> labels = new HashSet<>();
    boolean isAtLeastOneKeep = false;
    String route = ".";//FIXME: always shows last one
    for (FilterResult result : results) {
      String[] values = VepUtils.getVepValues(result.getRecord());
      if (isLogicFiltering) {
        String resultValue = VcfUtils.getInfoFieldValue(result.getRecord(), LOGIC_FILTER_KEY);
        if (resultValue.equals(LOGIC_FILTER_RESULT_TRUE)) {
          isAtLeastOneKeep = true;
        }
      }
      if (values.length == 1) {
        vepValues.add(values[0]);
      } else if (values.length > 0) {
        throw new RuntimeException("More than one VEP value should not occur here.");
      }
      String[] labelValues = VcfUtils.getInfoFieldValue(result.getRecord(), filterLabelsInfoField)
          .split(",");
      if (labelValues.length > 0) {
        labels.addAll(Arrays.asList(labelValues));
      }
      route = VcfUtils.getInfoFieldValue(result.getRecord(), routeInfoField);
    }
    if (isLogicFiltering) {
      if (isAtLeastOneKeep) {
        vcfRecord = addOrUpdateInfoField(vcfRecord, LOGIC_FILTER_KEY,
            LOGIC_FILTER_RESULT_TRUE, LOGIC_FILTER_DESC, "1", true);
      } else {
        vcfRecord = addOrUpdateInfoField(vcfRecord, LOGIC_FILTER_KEY,
            LOGIC_FILTER_RESULT_FALSE, LOGIC_FILTER_DESC, "1", true);
      }
    }
    vcfRecord = updateInfoField(vcfRecord, filterLabelsInfoField, Strings.join(labels, ","));
    vcfRecord = updateInfoField(vcfRecord, routeInfoField, route);
    vcfRecord = updateInfoField(vcfRecord, VEP_INFO_NAME, Strings.join(vepValues, ","));
    return new FilterResult(TRUE, vcfRecord);
  }

  private List<VcfRecord> splitRecord(VcfRecord record) {
    String[] vepValues = VepUtils.getVepValues(record);
    List<VcfRecord> records = new ArrayList<>();
    for (String vepValue : vepValues) {
      records.add(updateInfoField(record.createClone(), VEP_INFO_NAME, vepValue));
    }
    return records;
  }

  private Writer getRoutesWriter(File routesFile) throws IOException {
    if (isLogRoute) {
      return new FileWriter(routesFile);
    } else {
      return new NullWriter();
    }
  }


  private FilterResult processFilterAction(Map<String, FilterStep> filters,
      FilterStep filterStep, VcfRecord record, StringBuilder route) {
    FilterResult filterResult = filterStep.getFilter().filter(record);
    FilterAction action = filterStep.getAction(filterResult.getResult());
    appendToRoute(route, " > ");
    appendToRoute(route, filterStep.getKey() + "=" + filterResult.getResult());
    filterResult = processLabels(filterResult, action);
    filterResult = processRoute(filterResult, action, filterStep.getFilter());
    if (action.getState() == FilterState.KEEP) {
      appendToRoute(route, " > KEEP" + "\n");
      if (isLogicFiltering) {
        filterResult.setRecord(addOrUpdateInfoField(filterResult.getRecord(), LOGIC_FILTER_KEY,
            LOGIC_FILTER_RESULT_TRUE, LOGIC_FILTER_DESC, ".", true));
      }
      return filterResult;
    } else if (action.getState() == FilterState.REMOVE) {
      appendToRoute(route, " > REMOVE" + "\n");
      if (isLogicFiltering) {
        filterResult.setRecord(addOrUpdateInfoField(filterResult.getRecord(), LOGIC_FILTER_KEY,
            LOGIC_FILTER_RESULT_FALSE, LOGIC_FILTER_DESC, ".", true));
        return filterResult;
      }
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

  private FilterResult processLabels(FilterResult filterResult,
      FilterAction action) {
    if (action.getLabel() != null) {
      Set<String> labels = new HashSet<>();
      String[] labelValues = VcfUtils
          .getInfoFieldValue(filterResult.getRecord(), filterLabelsInfoField).split(",");
      if ((labelValues.length == 1 && !labelValues[0].equals(".")) || labelValues.length > 1) {
        labels.addAll(Arrays.asList(labelValues));
      }
      labels.add(action.getLabel());
      filterResult.setRecord(VcfUtils
          .updateInfoField(filterResult.getRecord(), filterLabelsInfoField,
              Strings.join(labels, ",")));
    }
    return filterResult;
  }

  private FilterResult processRoute(FilterResult filterResult, FilterAction action, Filter filter) {
    String route = VcfUtils.getInfoFieldValue(filterResult.getRecord(), routeInfoField);
    if (route.equals(".")) {
      route = "";
    } else {
      route = route + ",";
    }
    route = route + filter.getName() + ":" + filterResult.getResult();
    if (action.getState() == FilterState.KEEP) {
      route = route + ",KEEP";
    } else if (action.getState() == FilterState.REMOVE) {
      route = route + ",REMOVE";
    }
    filterResult
        .setRecord(VcfUtils.updateInfoField(filterResult.getRecord(), routeInfoField, route));
    return filterResult;
  }
}
