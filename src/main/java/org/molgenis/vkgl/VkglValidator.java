package org.molgenis.vkgl;

import static java.util.Arrays.asList;
import static org.molgenis.filter.FilterTool.GZIP_EXTENSION;
import static org.molgenis.vcf.utils.VcfUtils.getVcfReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.molgenis.genotype.Allele;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.utils.VcfUtils;
import org.molgenis.vcf.utils.VepUtils;

public class VkglValidator {

  public static final String INPUT = "input";
  public static final String SYMBOLS = "symbols";
  private static final String TAB = "\t";

  public static void main(String[] args) {
    OptionParser parser = createOptionParser();
    OptionSet options = parser.parse(args);
    new VkglValidator().run(options);
  }

  private static OptionParser createOptionParser() {
    OptionParser parser = new OptionParser();
    parser.acceptsAll(asList("i", INPUT), "Input VCF file").withRequiredArg().ofType(File.class);
    parser.acceptsAll(asList("s", SYMBOLS), "Symbols file").withRequiredArg().ofType(File.class);
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
    File symbolsFile = (File) options.valueOf(SYMBOLS);
    if (!symbolsFile.exists()) {
      System.out.println("symbolsFile not found at " + symbolsFile);
      return;
    } else if (symbolsFile.isDirectory()) {
      System.out.println("symbolsFile is a directory, not a file!");
      return;
    }
    Scanner scanner = null;
    try {
      scanner = new Scanner(symbolsFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    Map<String, List<String>> symbols = new HashMap<>();
    while(scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] split = line.split("\t");
      List<String> synonyms = Arrays.asList(split[8].split("\\|")).stream().map(String::toUpperCase).map(symbol -> symbol.replace("\"","")).collect(
          Collectors.toList());
      synonyms.add(split[1].toUpperCase());
      synonyms.addAll(Arrays.asList(split[10].split("\\|")).stream().map(String::toUpperCase).map(symbol -> symbol.replace("\"","")).collect(
          Collectors.toList()));
      for(String syn : synonyms) {
        symbols.put(syn, synonyms);
      }
    }

    String fullInputFileName = inputFile.getName();
    String extension = fullInputFileName.substring(fullInputFileName.indexOf('.'));
    File outputFile = new File(inputFile.getAbsolutePath()+".output");
    try {
      Writer writer = new FileWriter(outputFile);
      writer.write("CHROM"+ TAB + "POS" + TAB + "REF" + TAB + "ALT" + TAB + "VKGL gene" + TAB +"VEP genes" + TAB + "Alternative Symbols" + TAB + "VKGL gene unknown\n");
      VcfReader reader = getVcfReader(inputFile, extension.endsWith(GZIP_EXTENSION));
      for(VcfRecord record : reader){
        Set<String> genes = VepUtils.getVepValues("SYMBOL", record);
        Set<String> vepGenes = new HashSet<>();
        for(String vepGene : genes){
          vepGenes.add(vepGene.toUpperCase());
        }
        String gene = VcfUtils.getInfoFieldValue(record, "GENE").toUpperCase();
        boolean match = false;
        boolean isUnknown = false;

          for (String vep : vepGenes) {
            if(symbols.get(vep)!=null){
              if (symbols.get(vep).contains(gene)) {
                match = true;
              }
            }
          }
        if(symbols.get(gene)==null){
          isUnknown = true;
        }
        if(!match && !isUnknown){
          System.out.println("Mismatch between VKGL gene and VEP genes: " + VcfUtils.getRecordIdentifierString(record) +" VEP: "+String.join(",", vepGenes)+" VKGL: "+gene);
          writer.write(record.getChromosome() + TAB + record.getPosition() + TAB + record.getReferenceAllele() + TAB + String.join(",",record.getAlternateAlleles().stream().map(Allele::toString).collect(
              Collectors.toList())) + TAB + gene + TAB + String.join(",", vepGenes)+TAB + symbols.get(gene.toUpperCase())+TAB+isUnknown+"\n");
        }
      }
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
