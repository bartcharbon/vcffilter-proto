package org.molgenis.filter.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.molgenis.filter.FilterResultEnum;

public class FilterUtils {
  public static final String SEPARATOR = ",";

  public static boolean containsWord(String value, String filterValue) {
    List<String> values = new ArrayList<>();
    String[] splitted = value.split(",");
    for(String split : splitted){
      String[] doubleSplitted = split.split("\\|");
      values = Arrays.asList(doubleSplitted);
    }
    return values.contains(filterValue);
  }

  public static  boolean contains(String value, String filterValue) {
    return value.contains(filterValue);
  }

  public static  boolean containsAll(String[] values, String[] filterValues) {
    List filterValueList = Arrays.asList(filterValues);
    List valueList = Arrays.asList(values);
    return valueList.containsAll(filterValueList);
  }

  public static  boolean containsAny(String[] values, String[] filterValues) {
    List filterValueList = Arrays.asList(filterValues);
    List valueList = Arrays.asList(values);
    for(Object filterValue : filterValueList){
      if(valueList.contains(filterValue)){
        return true;
      }
    }
    return false;
  }

  public static  boolean containsNone(String[] values, String[] filterValues) {
    List filterValueList = Arrays.asList(filterValues);
    List valueList = Arrays.asList(values);
    for(Object filterValue : filterValueList){
      if(valueList.contains(filterValue)){
        return false;
      }
    }
    return true;
  }

  public static FilterResultEnum toFilterResultEnum(Boolean bool){
    if(Boolean.TRUE == bool){
      return FilterResultEnum.TRUE;
    }else if(Boolean.FALSE == bool){
      return FilterResultEnum.FALSE;
    }
    return FilterResultEnum.MISSING;
  }
}
