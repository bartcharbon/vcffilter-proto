package org.molgenis.inheritance.pedigree;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Pedigreader {

  public static final String INPUT = "input";

  public static final int FAMILY_IDX = 0;
  public static final int PATIENT_IDX = 1;
  public static final int FATHER_IDX = 2;
  public static final int MOTHER_IDX = 3;
  public static final int SEX_IDX = 4;
  public static final int AFFECTED_IDX = 5;

  public static void main(String[] args) {
    OptionParser parser = createOptionParser();
    OptionSet options = parser.parse(args);

    File inputFile = (File) options.valueOf(INPUT);
    if (!inputFile.exists()) {
      throw new RuntimeException("Input VCF file not found at " + inputFile);
    } else if (inputFile.isDirectory()) {
      throw new RuntimeException("Input VCF file is a directory, not a file!");
    }

    Map<String, Pedigree> result = new Pedigreader().run(inputFile);
    System.out.println(result);
  }

  private static OptionParser createOptionParser() {
    OptionParser parser = new OptionParser();
    parser.acceptsAll(asList("i", INPUT), "Input VCF file").withRequiredArg().ofType(File.class);
    return parser;
  }

  public Map<String, Pedigree> run(File inputFile) {
    Map<String, Pedigree> result = null;
    try {
      result = new HashMap<>();
      Scanner scanner = new Scanner(inputFile);
      Map<String, String[]> preprocessed = new HashMap<>();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] splitted = line.split("\t");

        if (splitted.length == 6) {
          preprocessed.put(splitted[PATIENT_IDX], splitted);
        } else {
          throw new RuntimeException("Invalid number of pedigree columns");
        }
      }
      result = parsePedigree(preprocessed);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  private Map<String, Pedigree> parsePedigree(Map<String, String[]> preprocessed) {
    Map<String, Pedigree> result = new HashMap<>();
    for(Entry<String, String[]> row : preprocessed.entrySet()) {
      result.put(row.getKey(), parseSingleLine(result, row.getKey(), row.getValue(), preprocessed));
    }
    return result;
  }

  private Pedigree parseSingleLine(Map<String, Pedigree> result, String id, String[] row,
      Map<String, String[]> preprocessed) {
    Pedigree pedigree;
    if(!result.containsKey(id)) {
      String[] splitted = row;
      Pedigree father = null;
      if (!splitted[FATHER_IDX].equals("0")) {
        System.out
            .println("Mother found for sample: " + splitted[2] + " processing mother sample "
                + splitted[FATHER_IDX] + "  first");
        if(!preprocessed.containsKey(splitted[FATHER_IDX])){
          throw new RuntimeException("Unknown id: " + splitted[FATHER_IDX]);
        }
        father = parseSingleLine(result, splitted[FATHER_IDX], preprocessed.get(splitted[FATHER_IDX]), preprocessed);
      }
      Pedigree mother = null;
      if (!splitted[MOTHER_IDX].equals("0")) {
        System.out
            .println("Mother found for sample: " + splitted[2] + " processing mother sample "
                + splitted[MOTHER_IDX] + "  first");
        mother = parseSingleLine(result, splitted[MOTHER_IDX], preprocessed.get(splitted[MOTHER_IDX]), preprocessed);
        if(!preprocessed.containsKey(splitted[MOTHER_IDX])){
          throw new RuntimeException("Unknown id: " + splitted[MOTHER_IDX]);
        }
      }
      pedigree = new Pedigree(splitted[FAMILY_IDX],splitted[PATIENT_IDX],father,mother,Sex.get(splitted[SEX_IDX]), Affected.get(splitted[AFFECTED_IDX]));
    }else{
      return result.get(id);
    }
    return pedigree;
  }
}
