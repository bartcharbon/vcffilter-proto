package org.molgenis.inheritance.pedigree;

import java.util.HashMap;
import java.util.Map;

public class PedigreeUtils {
  public static Pedigree getSingleParentPedigree(Pedigree pedigree) {
    if(pedigree.getMother().isPresent() && pedigree.getFather().isPresent()){
      throw new RuntimeException("Cant get single parent pedigree for pedigree with both parents.");
    }
    else if(pedigree.getMother().isPresent()) {
      return pedigree.getMother().get();
    }
    else if(pedigree.getFather().isPresent()) {
      return pedigree.getFather().get();
    }
    throw new RuntimeException("Cant get single parent pedigree for pedigree with no parents.");
  }

  public static Pedigree getFather(Pedigree pedigree) {
    if(pedigree.getFather().isPresent()) {
      return pedigree.getFather().get();
    }
    throw new RuntimeException("No father present for this patient.");
  }

  public static Pedigree getMother(Pedigree pedigree) {
    if(pedigree.getMother().isPresent()) {
      return pedigree.getMother().get();
    }
    throw new RuntimeException("No mother present for this patient.");
  }

  public static Pedigree getAffectedParent(Pedigree pedigree) {
    if(pedigree.getMother().isPresent() && pedigree.getMother().get().getAffected() == Affected.TRUE) {
      return pedigree.getMother().get();
    }
    else if(pedigree.getFather().isPresent() && pedigree.getFather().get().getAffected() == Affected.TRUE) {
      return pedigree.getFather().get();
    }
    throw new RuntimeException("No affected parent present for this patient.");
  }

  public static Pedigree getUnaffectedParent(Pedigree pedigree) {
    if(pedigree.getMother().isPresent() && pedigree.getMother().get().getAffected() == Affected.FALSE) {
      return pedigree.getMother().get();
    }
    else if(pedigree.getFather().isPresent() && pedigree.getFather().get().getAffected() == Affected.FALSE) {
      return pedigree.getFather().get();
    }
    throw new RuntimeException("No unaffected parent present for this patient.");
  }

  public static Map<String, Pedigree> getParentPedigrees(Pedigree pedigree) {
    Map<String, Pedigree> pedigrees = new HashMap<>();
    Pedigree mother = pedigree.getMother().orElseThrow(() ->  new RuntimeException("No mother pedigree"));
    Pedigree father = pedigree.getFather().orElseThrow(() ->  new RuntimeException("No father pedigree"));
    pedigrees.put("mother", mother);
    pedigrees.put("father", father);
    return pedigrees;
  }

  public static int getNumberOfPresentParents(Pedigree pedigree) {
    if(pedigree.getMother().isPresent() && pedigree.getFather().isPresent()){
      return 2;
    }
    else if(pedigree.getMother().isPresent() || pedigree.getFather().isPresent()) {
      return 1;
    }
    return 0;
  }
}
