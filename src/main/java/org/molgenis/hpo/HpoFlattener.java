package org.molgenis.hpo;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class HpoFlattener {

  public static final String INPUT = "input";

  public static void main(String[] args) {
    OptionParser parser = createOptionParser();
    OptionSet options = parser.parse(args);
    new HpoFlattener().run(options);
  }

  private static OptionParser createOptionParser() {
    OptionParser parser = new OptionParser();
    parser.acceptsAll(asList("i", INPUT), "Input VCF file").withRequiredArg().ofType(File.class);
    return parser;
  }

  public void run(OptionSet options) {
    File inputFile = (File) options.valueOf(INPUT);
    if (!inputFile.exists()) {
      System.out.println("Input VCF file not found at " + inputFile);
      return;
    } else if (inputFile.isDirectory()) {
      System.out.println("Input VCF file is a directory, not a file!");
      return;
    }

    File outputFile = new File(inputFile.getAbsolutePath()+".output");
    try {
      Writer writer = new FileWriter(outputFile);
      Map<String, String> result = new HashMap<>();
      Scanner scanner = new Scanner(inputFile);
      while(scanner.hasNextLine()){
        String line = scanner.nextLine();
        if(line.equals("Term_PK\tTerm_Name\tIdentifier\tIs_Leaf\tParent_term_name\tParent_term_ID\tGrandParent_term_name\tGrandParent_term_ID")){
          writer.write("Identifier\tLabel\tParent\n");
        }
        String[] tokens = line.split("\t");
        String key = tokens[2]+"\t"+tokens[1];
        String value = "";
        if(tokens.length > 5) {
          value = tokens[5];
          if (result.containsKey(key)) {
            if(!result.get(key).contains(value)) {
              value = value + "," + result.get(key);
            }else{
              value = result.get(key);
            }
          }
        }
        result.put(key, value);
      }
      for(Entry<String, String> entry : result.entrySet()){
        writer.write(entry.getKey() +"\t"+entry.getValue()+"\n");
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
