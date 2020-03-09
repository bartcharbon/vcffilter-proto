package org.molgenis.filter.yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.molgenis.filter.ComplexFilter;
import org.molgenis.filter.ComplexOperator;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterAction;
import org.molgenis.filter.FilterState;
import org.molgenis.filter.FilterStep;
import org.molgenis.filter.InfoFilter;
import org.molgenis.filter.Operator;
import org.molgenis.filter.SampleFilter;
import org.molgenis.filter.SimpleFilter;
import org.molgenis.filter.SimpleOperator;
import org.molgenis.filter.VepFilter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlLoader {
  private static final String PARAM_PREFIX = "{{";
  private static final String PARAM_POSTFIX = "}}";
  private static Map<String, Filter> filters = new LinkedHashMap<>();
  private static Map<String, FilterStep> filterSteps = new LinkedHashMap<>();

  private YamlLoader() {

  }

  public static Map<String, FilterStep> loadFilters(File inputFile, Map<String,String> params, String sampleId){
    InputStream inputstream = preprocessFile(inputFile, params);
    Yaml yaml = new Yaml(new Constructor(FilterSpec
        .class));
    InputStream inputStream = inputstream;
    FilterSpec spec = yaml.load(inputStream);
    if(spec.getSteps().getSimple()!=null) {
      for (org.molgenis.filter.yaml.SimpleStep simple : spec.getSteps().getSimple()) {
        filters.put(simple.getName(), toFilter(simple.getFilter(), inputFile));
      }
    }
    if(spec.getSteps().getInfo()!=null){
      for(org.molgenis.filter.yaml.SimpleStep simple: spec.getSteps().getInfo()){
        filters.put(simple.getName(), toInfoFilter(simple.getFilter(), inputFile));
      }
    }
    if(spec.getSteps().getVep()!=null) {
      for (org.molgenis.filter.yaml.SimpleStep simple : spec.getSteps().getVep()) {
        filters.put(simple.getName(), toVepFilter(simple.getFilter(), inputFile));
      }
    }
    if(spec.getSteps().getSample()!=null) {
      for (org.molgenis.filter.yaml.SimpleStep sample : spec.getSteps().getSample()) {
        filters.put(sample.getName(), toFilter(sample.getFilter(), sampleId, inputFile));
      }
    }
    if(spec.getSteps().getComplex()!=null) {
      for (org.molgenis.filter.yaml.ComplexStep complex : spec.getSteps().getComplex()) {
        filters.put(complex.getName(), toFilter(complex.getFilter()));
      }
    }
    for(org.molgenis.filter.yaml.Node node: spec.getTree().getNodes()){
      filterSteps.put(node.getName(), toFilterStep(node));
    }
    return filterSteps;
  }

  private static Filter toVepFilter(org.molgenis.filter.yaml.SimpleFilter filter, File inputFile) {
    if(filter.getValue() != null){
      return new VepFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()),filter.getValue());
    }
    else{
      String file = preProcessFilePath(filter.getFile(), inputFile);
      return new VepFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), file, filter.getColumn());
    }
  }

  private static Filter toInfoFilter(org.molgenis.filter.yaml.SimpleFilter filter, File inputFile) {
    if(filter.getValue() != null){
      return new InfoFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), filter.getValue());
    }
    else{
      String file = preProcessFilePath(filter.getFile(), inputFile);
      return new InfoFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), file);
    }
  }

  private static InputStream preprocessFile(File inputFile, Map<String,String> params) {
    String contents = null;
    try {
      contents = new String(Files.readAllBytes(inputFile.toPath()));
    for(Entry<String,String> entry:params.entrySet()) {
      String key = PARAM_PREFIX +entry.getKey()+ PARAM_POSTFIX;
      contents = contents.replace(key,entry.getValue());
    }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ByteArrayInputStream(contents.getBytes());
  }

  private static FilterStep toFilterStep(Node node) {
    return new FilterStep(node.getName(), filters.get(node.getFilter()),toAction(node.getPass()),toAction(node.getFail()));
  }

  private static FilterAction toAction(NextStep action) {
    return new FilterAction(toFilterState(action.getNext()),action.getNext(),action.getLabel());
  }

  private static FilterState toFilterState(String action) {
    if (action.equals("keep")) {
      return FilterState.KEEP;
    } else if (action.equals("remove")) {
      return FilterState.REMOVE;
    } else if (!action.isEmpty()) {
      return FilterState.NEXT;
    } else {
      return null;
    }
  }

  private static Filter toFilter(org.molgenis.filter.yaml.ComplexFilter filter) {
    return new ComplexFilter(getFilters(filter.getFields()),(ComplexOperator) getOperator(filter.getOperator()));
  }

  private static List<Filter> getFilters(String fields) {
    List<Filter> result = new ArrayList<>();
    for(String field : fields.split(",")){
      result.add(filters.get(field));
    }
    return result;
  }

  private static Filter toFilter(org.molgenis.filter.yaml.SimpleFilter filter,
     File inputFile) {
    if(filter.getValue() != null){
      return new SimpleFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), filter.getValue());
    }
    else{
      String file = preProcessFilePath(filter.getFile(), inputFile);
      return new SimpleFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), file);
    }
  }

  private static Filter toFilter(org.molgenis.filter.yaml.SimpleFilter filter, String sampleId, File inputFile) {
    if(filter.getValue() != null){
      return new SampleFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), filter.getValue(), sampleId);
    }
    else{
      String file = preProcessFilePath(filter.getFile(), inputFile);
      return new SampleFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), file, sampleId);
    }
  }


  private static String preProcessFilePath(String filename, File inputFile) {
    Path path = Paths.get(inputFile.getAbsolutePath());
    String workingDir = path.getParent().toString();
    return workingDir+File.separator+filename;
  }

  private static Operator getOperator(String value) {
    Operator operator;
    switch (value) {
      case "==":
        operator = SimpleOperator.EQ;
        break;
      case "contains":
        operator = SimpleOperator.CONTAINS;
        break;
      case ">=":
        operator = SimpleOperator.GREATER_OR_EQUAL;
        break;
      case "<=":
        operator = SimpleOperator.LESS_OR_EQUAL;
        break;
      case ">":
        operator = SimpleOperator.GREATER;
        break;
      case "<":
        operator = SimpleOperator.LESS;
        break;
      case "!=":
        operator = SimpleOperator.NOT_EQ;
        break;
      case "in":
        operator = SimpleOperator.IN;
        break;
      case "AND":
        operator = ComplexOperator.AND;
        break;
      case "OR":
        operator = ComplexOperator.OR;
        break;
      default:
        throw new IllegalArgumentException(
            "Invalid filter operator ["+value+"], expecting one of [==,>=,<=,>,<,!=]");
    }
    return operator;
  }
}