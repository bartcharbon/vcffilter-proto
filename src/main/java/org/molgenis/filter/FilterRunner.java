package org.molgenis.filter;

import static java.util.Objects.requireNonNull;
import static org.molgenis.filter.FilterResultEnum.TRUE;
import static org.molgenis.filter.FilterTool.GZIP_EXTENSION;
import static org.molgenis.filter.utils.VcfUtils.addOrUpdateInfoField;
import static org.molgenis.filter.utils.VcfUtils.getRecordIdentifierString;
import static org.molgenis.filter.utils.VcfUtils.getVcfReader;
import static org.molgenis.filter.utils.VcfUtils.getVcfWriter;
import static org.molgenis.filter.utils.VcfUtils.updateInfoField;
import static org.molgenis.filter.utils.VcfUtils.writeRecord;

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
import org.molgenis.filter.utils.ComplexVcfInfoUtils;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfWriter;
import org.molgenis.filter.utils.VcfUtils;

public class FilterRunner {

  private static final String LOGIC_FILTER_RESULT_TRUE = "TRUE";
  private static final String LOGIC_FILTER_RESULT_FALSE = "FALSE";
  private static final String VEP_INFO = "CSQ";
  private final File inputFile;
  private final String extension;
  private final String labelsInfoField;
  private final String routeInfoField;
  private final FilterConfig filterConfig;
  private final OutputConfig outputConfig;

  private String LOGIC_FILTER_KEY = "FILTER_RESULT";
  private static final String LOGIC_FILTER_DESC = "";

  public FilterRunner(File inputFile, String extension, FilterConfig filterConfig, OutputConfig outputConfig) {
    this.inputFile = requireNonNull(inputFile);
    this.extension = requireNonNull(extension);
    this.labelsInfoField = outputConfig.getInfoFieldPrefix() + "_LABELS";
    this.routeInfoField = outputConfig.getInfoFieldPrefix() + "_ROUTE";
    this.filterConfig = filterConfig;
    this.outputConfig = outputConfig;
  }

  public void runFilters() throws Exception {
    Map<String, String> additionalHeaders = new HashMap();
    if (outputConfig.getArchivedFilterFile().isActive()) {
      additionalHeaders.put(outputConfig.getArchivedFilterFile().getHeaderName().get(), outputConfig.getArchivedFilterFile().getFile().get().getName());
    }
    if (outputConfig.isLogRoute()) {//FIXME: seperate file and info field route
      additionalHeaders.put(outputConfig.getRoutesFile().getHeaderName().get(), outputConfig.getRoutesFile().getFile().get().getName());
    }
    Writer routesWriter = getRoutesWriter(outputConfig.getRoutesFile());

    VcfReader reader = getVcfReader(inputFile, extension.endsWith(GZIP_EXTENSION));
    VcfWriter vcfWriter = getVcfWriter(outputConfig.getOutputFile(), reader.getVcfMeta(), additionalHeaders,
        extension.endsWith(
            GZIP_EXTENSION));

    Stream<VcfRecord> recordStream = StreamSupport
        .stream(reader.spliterator(), false);

    Stream<VcfRecord> filtered = recordStream
        .map(record -> VcfUtils
            .addOrUpdateInfoField(record, labelsInfoField, ".", ".", ".", true))
        .map(record -> VcfUtils
            .addOrUpdateInfoField(record, routeInfoField, ".", ".", ".", true))
        .map(record -> {
          try {
            return startFiltering(record, routesWriter);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }).filter(Objects::nonNull).map(FilterResult::getRecord);
    filtered.forEach(record -> writeRecord(record, vcfWriter));
    vcfWriter.close();
    routesWriter.close();
    System.out.println("Filtered!");
  }

  private FilterResult startFiltering(VcfRecord record, Writer routesWriter) throws IOException {

    FilterStep filterStep = StreamSupport.stream(filterConfig.getFilters().entrySet().spliterator(), false)
        .findFirst().get().getValue();
    List<VcfRecord> splittedRecords = splitRecord(record);
    List<FilterResult> results = new ArrayList<>();
    int index = 0;
    for (VcfRecord splittedRecord : splittedRecords) {
      StringBuilder route = new StringBuilder(getRecordIdentifierString(record) + "_" + index);
      FilterResult result = processFilterAction(filterStep, splittedRecord, route);
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
      String[] values = ComplexVcfInfoUtils.getSubValues(result.getRecord(), VEP_INFO);
      if (filterConfig.isLogicFiltering()) {
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
      String[] labelValues = VcfUtils.getInfoFieldValue(result.getRecord(), labelsInfoField)
          .split(",");
      if (labelValues.length > 0) {
        labels.addAll(Arrays.asList(labelValues));
      }
      route = VcfUtils.getInfoFieldValue(result.getRecord(), routeInfoField);
    }
    if (filterConfig.isLogicFiltering()) {
      if (isAtLeastOneKeep) {
        vcfRecord = addOrUpdateInfoField(vcfRecord, LOGIC_FILTER_KEY,
            LOGIC_FILTER_RESULT_TRUE, LOGIC_FILTER_DESC, "1", true);
      } else {
        vcfRecord = addOrUpdateInfoField(vcfRecord, LOGIC_FILTER_KEY,
            LOGIC_FILTER_RESULT_FALSE, LOGIC_FILTER_DESC, "1", true);
      }
    }
    vcfRecord = updateInfoField(vcfRecord, labelsInfoField, Strings.join(labels, ","));
    vcfRecord = updateInfoField(vcfRecord, routeInfoField, route);
    vcfRecord = updateInfoField(vcfRecord, VEP_INFO, Strings.join(vepValues, ","));
    return new FilterResult(TRUE, vcfRecord);
  }

  private List<VcfRecord> splitRecord(VcfRecord record) {
    String[] vepValues = ComplexVcfInfoUtils.getSubValues(record, VEP_INFO);
    List<VcfRecord> records = new ArrayList<>();
    for (String vepValue : vepValues) {
      records.add(updateInfoField(record.createClone(), VEP_INFO, vepValue));
    }
    return records;
  }

  private Writer getRoutesWriter(ArchivedFile routesFile) throws IOException {
    if (outputConfig.isLogRoute() && routesFile.isActive()) {
      return new FileWriter(routesFile.getFile().get());
    } else {
      return new NullWriter();
    }
  }

  private FilterResult processFilterAction(FilterStep filterStep, VcfRecord record, StringBuilder route) {
    FilterResult filterResult = filterStep.getFilter().filter(record);
    FilterAction action = filterStep.getAction(filterResult.getResult());
    appendToRoute(route, " > ");
    appendToRoute(route, filterStep.getKey() + "=" + filterResult.getResult());
    filterResult = processLabels(filterResult, action);
    filterResult = processRoute(filterResult, action, filterStep.getFilter());
    if (action.getState() == FilterState.KEEP) {
      appendToRoute(route, " > KEEP" + "\n");
      if (filterConfig.isLogicFiltering()) {
        filterResult.setRecord(addOrUpdateInfoField(filterResult.getRecord(), LOGIC_FILTER_KEY,
            LOGIC_FILTER_RESULT_TRUE, LOGIC_FILTER_DESC, ".", true));
      }
      return filterResult;
    } else if (action.getState() == FilterState.REMOVE) {
      appendToRoute(route, " > REMOVE" + "\n");
      if (filterConfig.isLogicFiltering()) {
        filterResult.setRecord(addOrUpdateInfoField(filterResult.getRecord(), LOGIC_FILTER_KEY,
            LOGIC_FILTER_RESULT_FALSE, LOGIC_FILTER_DESC, ".", true));
        return filterResult;
      }
      return null;
    } else {
      String nextFilter = action.getNextStep();
      FilterStep nextFilterStep = filterConfig.getFilters().get(nextFilter);
      if (nextFilterStep == null) {
        throw new RuntimeException("Filterstep '" + nextFilter + "' could not be resolved.");
      }
      return processFilterAction(nextFilterStep, filterResult.getRecord(), route);
    }
  }

  private void appendToRoute(StringBuilder route, String s) {
    if (outputConfig.isLogRoute()) {
      route.append(s);
    }
  }

  private FilterResult processLabels(FilterResult filterResult,
      FilterAction action) {
    if (action.getLabel() != null) {
      Set<String> labels = new HashSet<>();
      String[] labelValues = VcfUtils
          .getInfoFieldValue(filterResult.getRecord(), labelsInfoField).split(",");
      if ((labelValues.length == 1 && !labelValues[0].equals(".")) || labelValues.length > 1) {
        labels.addAll(Arrays.asList(labelValues));
      }
      labels.add(action.getLabel());
      filterResult.setRecord(VcfUtils
          .updateInfoField(filterResult.getRecord(), labelsInfoField,
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
