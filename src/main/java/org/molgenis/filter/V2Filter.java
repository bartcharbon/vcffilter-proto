package org.molgenis.filter;

public interface V2Filter {
  Object getVcfValue(Field field);
  Operator getOperator();
  Object getFilterValue();
}
