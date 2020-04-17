package org.molgenis.inheritance.pedigree;

import static java.util.Objects.requireNonNull;

public enum Affected {
  TRUE,FALSE,UNKNOWN;

  public static Affected get(String value){
    if(value.equals("1")){
      return Affected.FALSE;
    }
    if(value.equals("2")){
      return Affected.TRUE;
    }
    return Affected.UNKNOWN;
  }
}
