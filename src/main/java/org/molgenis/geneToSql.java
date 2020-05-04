package org.molgenis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class geneToSql {

  public static String NEWLINE = "\n";

  public static void main(String args[]) {
    try {
      File genes = new File("C:\\Users\\bartc\\Desktop\\custom.tsv");

      Map<String, String> geneLines = new HashMap<>();
      Map<String, String> aliasLines = new HashMap<>();
      Map<String, String> prevLines = new HashMap<>();

      Scanner scanner = new Scanner(genes);
      scanner.nextLine();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] split = line.split("\t", -1);
        String approved = split[0].trim().replace("'","''").toUpperCase();
        String desc = split[1].replace("'","''");
        if(!split[1].contains(" pseudogene ")) {
          geneLines.put(approved,
              "INSERT INTO gene (id, symbol, name, approved_symbol, status) VALUES (hibernate_sequence.nextval, '"
                  + approved + "', '" + desc + "', '" + approved + "',0)" + NEWLINE);
          //split prev symbols
          String[] prevs = split[3].split(",");
          for (String prev : prevs) {
            if (!prev.isEmpty()) {
              if(!geneLines.containsKey(prev.trim())) {
                geneLines.put(prev.trim().replace("'","''").toUpperCase(),
                    "INSERT INTO gene (id, symbol, name, approved_symbol, status) VALUES (hibernate_sequence.nextval, '"
                        + prev.trim().replace("'","''").toUpperCase() + "', '" + desc + "', '" + approved + "',2)"
                        + NEWLINE);
              }
              prevLines.put(prev.trim().replace("'","''").toUpperCase(),
                  "INSERT INTO gene_previous_symbols (gene_id, previous_symbols_id) VALUES ((SELECT id FROM gene WHERE symbol = '"
                      + approved + "'),(SELECT id FROM gene WHERE symbol = '" + prev.trim().replace("'","''").toUpperCase() + "'))"
                      + NEWLINE);
            }
          }
          //split aliasses
          String[] aliasses = split[4].split(",");
          for (String alias : aliasses) {
            String alias2 = alias.trim().replace("'","''").toUpperCase();
            if (!alias.isEmpty()) {
              if(!geneLines.containsKey(alias.trim().replace("'","''"))) {
                geneLines.put(alias.trim().replace("'","''").toUpperCase(),
                    "INSERT INTO gene (id, symbol, name, approved_symbol, status) VALUES (hibernate_sequence.nextval, '"
                        + alias2 + "', '" + desc + "', '" + approved + "',1)"
                        + NEWLINE);
              }
              aliasLines.put(approved,
                  "INSERT INTO gene_aliasses (gene_id, aliasses_id) VALUES ((SELECT id FROM gene WHERE symbol = '"
                      + approved + "'),(SELECT id FROM gene WHERE symbol = '" + alias2 + "'))"
                      + NEWLINE);
              List<String> newAliasses = new ArrayList<>();
              newAliasses.addAll(Arrays.asList(aliasses));
              newAliasses.add(approved);
              newAliasses.stream().filter(a -> !a.equals(2)).forEach(a -> {
                if(!aliasLines.containsKey(alias2)) {
                  aliasLines.put(alias2,
                      "INSERT INTO gene_aliasses (gene_id, aliasses_id) VALUES ((SELECT id FROM gene WHERE symbol = '"
                          + alias2 + "'),(SELECT id FROM gene WHERE symbol = '" + a.trim()
                          .replace("'", "''").toUpperCase()
                          + "'))"
                          + NEWLINE);

                }    }
              );
            }

          }
        }else{
          System.out.print("");
        }
      }

      File output = new File("C:\\Users\\bartc\\Documents\\git\\variant-workbench\\backend\\src\\main\\resources\\data-h2.sql");
      Writer writer = new FileWriter(output);
      geneLines.values().forEach(line -> {
        try {
          writer.write(line);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      prevLines.values().forEach(line -> {
        try {
          writer.write(line);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      aliasLines.values().forEach(line -> {
        try {
          writer.write(line);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
