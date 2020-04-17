package org.molgenis.filter;

import static java.util.Objects.requireNonNull;

public enum FlagOperator implements Operator{
  PRESENT("and"), NOT_PRESENT("or");

  private final String symbol;

  FlagOperator(String symbol){
    this.symbol = requireNonNull(symbol);
  }

  @Override
  public String getSymbol() {
    return symbol;
  }
}
