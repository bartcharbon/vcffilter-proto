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
import org.molgenis.filter.CustomFilter;
import org.molgenis.filter.Filter;
import org.molgenis.filter.FilterAction;
import org.molgenis.filter.FilterState;
import org.molgenis.filter.FilterStep;
import org.molgenis.filter.InfoFilter;
import org.molgenis.filter.InfoFlagFilter;
import org.molgenis.filter.NoOpFilter;
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
  private static final String NO_OP = "No-Op";
  private static Map<String, Filter> filters = new LinkedHashMap<>();
  private static Map<String, FilterStep> filterSteps = new LinkedHashMap<>();

  private YamlLoader() {

  }

  public static Map<String, FilterStep> loadFilterTree(File inputFile, Map<String,String> params, String sampleId){
    InputStream inputstream = preprocessFile(inputFile, params);
    Yaml yaml = new Yaml(new Constructor(FilterSpec
        .class));
    InputStream inputStream = inputstream;
    FilterSpec spec = yaml.load(inputStream);

    createFilters(inputFile, sampleId, spec);
    createTree(spec);

    return filterSteps;
  }

  private static void createFilters(File inputFile, String sampleId, FilterSpec spec) {
    if(spec.getSteps().getSimple()!=null) {
      for (SimpleStep simple : spec.getSteps().getSimple()) {
        filters.put(simple.getName(), toFilter(simple.getFilter(), inputFile));
      }
    }
    if(spec.getSteps().getCustom()!=null) {
      for (SimpleStep simple : spec.getSteps().getSimple()) {
        filters.put(simple.getName(), toCustomFilter(simple.getFilter(), inputFile));
      }
    }
    if(spec.getSteps().getInfo()!=null){
      for(SimpleStep simple: spec.getSteps().getInfo()){
        filters.put(simple.getName(), toInfoFilter(simple.getFilter(), inputFile));
      }
    }
    if(spec.getSteps().getInfoFlag()!=null){
      for(FlagStep flag: spec.getSteps().getInfoFlag()){
        filters.put(flag.getName(), toInfoFlagFilter(flag.getFilter()));
      }
    }
    if(spec.getSteps().getVep()!=null) {
      for (SimpleStep simple : spec.getSteps().getVep()) {
        filters.put(simple.getName(), toVepFilter(simple.getFilter(), inputFile));
      }
    }
    if(spec.getSteps().getSample()!=null) {
      for (SimpleStep sample : spec.getSteps().getSample()) {
        filters.put(sample.getName(), toFilter(sample.getFilter(), sampleId, inputFile));
      }
    }
    if(spec.getSteps().getComplex()!=null) {
      for (ComplexStep complex : spec.getSteps().getComplex()) {
        filters.put(complex.getName(), toFilter(complex.getFilter()));
      }
    }
    filters.put(NO_OP, new NoOpFilter());
  }

  private static void createTree(FilterSpec spec) {
    for(Node node: spec.getTree().getNodes()){
      filterSteps.put(node.getName(), toFilterStep(node));
    }
  }

  private static Filter toInfoFlagFilter(FlagFilter filter) {
    return new InfoFlagFilter(filter.getField(), getOperator(filter.getOperator()));
  }

  private static Filter toVepFilter(org.molgenis.filter.yaml.SimpleFilter filter, File inputFile) {
    if(filter.getFile() != null){
      String file = preProcessFilePath(filter.getFile(), inputFile);
      return new VepFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), file, filter.getColumn());
    }
    else{
      return new VepFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()),filter.getValue());
    }
  }

  private static Filter toCustomFilter(org.molgenis.filter.yaml.SimpleFilter filter, File inputFile) {
      return new CustomFilter(filter.getField(),inputFile,filter.getValue());

  }

  private static Filter toInfoFilter(org.molgenis.filter.yaml.SimpleFilter filter, File inputFile) {
    if(filter.getFile() != null){
      String file = preProcessFilePath(filter.getFile(), inputFile);
      return new InfoFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), file);
    }
    else{
      return new InfoFilter(filter.getField(),(SimpleOperator) getOperator(filter.getOperator()), filter.getValue());
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