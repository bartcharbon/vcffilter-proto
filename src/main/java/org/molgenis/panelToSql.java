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
import java.util.Map.Entry;
import java.util.Scanner;

public class panelToSql {

  public static String NEWLINE = "\n";

  public static void main(String args[]) {
    try {
      File panelFile = new File("C:\\Users\\bartc\\Desktop\\panels.txt");

      Map<String, Map<String,String>> panels = new HashMap<>();

      Scanner scanner = new Scanner(panelFile);
      scanner.nextLine();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if(line.startsWith("--BEGIN--")){
          String title = line.replace("--BEGIN--","").replace(","," - ");
          Map<String,String> genes = new HashMap<>();
          boolean done = false;
          while (scanner.hasNextLine() && !done) {
            String nextLine = scanner.nextLine();
            if(nextLine.contains("--END--")){
              panels.put(title, genes);
              done = true;
            }
            else {
              if(!nextLine.isBlank()) {
                String gene = nextLine.split(" ")[0];
                genes.put(gene,gene);
              }
            }
          }

        }
      }

      File output = new File("C:\\Users\\bartc\\Documents\\git\\variant-workbench\\backend\\src\\main\\resources\\panels.sql");
      Writer writer = new FileWriter(output);
      for(Entry<String, Map<String, String>> panel : panels.entrySet()){
        String name = panel.getKey().trim().replace("'","''");
        writer.write("INSERT INTO gene_panel (id, name, created_date, last_modified_date) VALUES (hibernate_sequence.nextval, '"+name+"', '2020-01-01 00:00:00.000', '2020-01-01 00:00:00.000')"+NEWLINE);
        for(String gene : panel.getValue().values()){
          writer.write("INSERT INTO gene_panel_genes (gene_panel_id, genes_id) VALUES ((SELECT id FROM gene_panel WHERE name = '"+name+"'),(SELECT id FROM gene WHERE symbol = '"+gene.trim().replace("'","''").toUpperCase()+"'))"+NEWLINE);
        }
      }
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
