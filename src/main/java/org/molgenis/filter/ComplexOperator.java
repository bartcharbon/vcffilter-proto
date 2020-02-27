package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

public enum ComplexOperator implements Operator{
  AND("and"), OR("or");

  private final String symbol;

  ComplexOperator(String symbol){
    this.symbol = requireNonNull(symbol);
  }

  @Override
  public String getSymbol() {
    return symbol;
  }
}
