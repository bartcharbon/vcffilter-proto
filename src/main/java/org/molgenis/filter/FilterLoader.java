package org.molgenis.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class FilterLoader {

  private FilterLoader() {

  }

  public static Map<String, FilterStep> loadFilters(File inputFile)
      throws FileNotFoundException {
    Path path = Paths.get(inputFile.getAbsolutePath());
    String workingDir = path.getParent().toString();
    Scanner inputStream = new Scanner(inputFile);
    Map<String, Filter> filters = new LinkedHashMap<>();
    LinkedHashMap<String, FilterStep> result = new LinkedHashMap<>();
    boolean isParseSteps = false;
    boolean isParseTree = false;
    while (inputStream.hasNext()) {
      String data = inputStream.nextLine();
      if(!data.isEmpty()){
        if (data.startsWith("#")) {
          if (data.equals("#Steps:")) {
            isParseSteps = true;
            isParseTree = false;
          } else if (data.equals("#Tree:")) {
            isParseSteps = false;
            isParseTree = true;
          }
        } else {
          if (isParseSteps) {
            parseSteps(filters, data, workingDir);
          } else if (isParseTree) {
            parseTree(filters, result, data);
          }
        }
      }
    }
    inputStream.close();
    return result;
  }

  private static void parseTree(Map<String, Filter> filters,
      LinkedHashMap<String, FilterStep> result, String data) {
    String pattern = "([a-zA-Z0-9]*)\\t([a-zA-Z0-9]*)\\t([a-zA-Z0-9_\\(\\)]*)\\t([a-zA-Z0-9_\\(\\)]*)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(data);
    if (m.matches()) {
      String key = m.group(1);
      String filterStep  = m.group(2);
      FilterAction trueAction = toAction(m.group(3));
      FilterAction falseAction = toAction(m.group(4));
      Filter filter = filters.get(filterStep);
      result.put(key, new FilterStep(key, filter, trueAction, falseAction));
    } else {
      throw new IllegalArgumentException(
          "Filter '" + data
              + "' is not correctly formatted, valid example: 'step1\\tFILTER\\tTrueNext\\tFalseNext\\tsimple'");
    }
  }

  private static void parseSteps(Map<String, Filter> filters, String data, String path) {
    String pattern = "([a-zA-Z0-9]*)\\t([^\\t]*)\\t*((\\b(simple)\\b|\\b(complex)\\b|\\b(file)\\b)*)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(data);
    String filterString;
    String key;
    if (m.matches()) {
      key = m.group(1);
      filterString = m.group(2);
      FilterType type = toType(m.group(3));
      Filter filter = parseFilterString(filterString, type, filters, path);
      filters.put(key, filter);
    } else {
      throw new IllegalArgumentException(
          "Filter '" + data
              + "' is not correctly formatted, valid example: 'step1\\tFILTER\\tsimple'");
    }
  }

  private static FilterType toType(String typeString) {
    if (typeString.isEmpty()) {
      return FilterType.SIMPLE;
    }
    return FilterType.valueOf(typeString.toUpperCase());
  }

  private static Filter parseFilterString(String filterString, FilterType type,
      Map<String, Filter> currentFilters, String path) {
    String[] values = filterString.split(" ");
    String field = values[0];
    String operator = values[1];
    if (type == FilterType.SIMPLE) {
      String value = values[2];
      return new SimpleFilter(field, getOperator(operator), value);
    } else if (type == FilterType.COMPLEX) {
      //referenced filters in complex filter should always be mentioned earlier in the file than the complex filter itself
      //complex filters contain a list of filters and the operator between them
      List<String> filterNames = Arrays.asList(field.split(","));
      ComplexOperator complexOperator = ComplexOperator.valueOf(operator.toUpperCase());

      return new ComplexFilter(
          filterNames.stream().map(currentFilters::get).collect(
              Collectors.toList()), complexOperator);
    } else if (type == FilterType.FILE) {
      String value = values[2];
      String[] filterValueParts = value.split(",");
      String filename = filterValueParts[0];
      if (getOperator(operator) != SimpleOperator.IN) {
        throw new RuntimeException("File filters can only be used with the 'in' operator.");
      }
      return new FileFilter(field,filterValueParts[1],path+File.separator+filename);
    }
    throw new IllegalArgumentException("Filters can be either of type 'simple' or 'complex'");
  }

  @Nullable
  private static FilterAction toAction(String action) {
    String label = null;
    if(action.indexOf('(') != -1){
      label = action.substring(action.indexOf('(')+1, action.length()-1);
      action = action.substring(0, action.indexOf('('));
    }
    if (action.equals("keep")) {
      return new FilterAction(FilterState.KEEP, null, label);
    } else if (action.equals("remove")) {
      return new FilterAction(FilterState.REMOVE, null, label);
    } else if (!action.isEmpty()) {
      return new FilterAction(FilterState.NEXT, action, label);
    } else {
      return null;
    }
  }

  private static SimpleOperator getOperator(String value) {
    SimpleOperator operator;
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
      default:
        throw new IllegalArgumentException(
            "Invalid filter operator, expecting one of [==,>=,<=,>,<,!=]");
    }
    return operator;
  }
}