package at.tugraz.iti.programmablematter.elfmapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ElfAddressToNameTranslator {

    private final File elfDumpLookup;
    private final File inFileToTranslate;

    private final Set<String> knowTypes = new HashSet<>();

    private ElfAddressToNameTranslator(String[] args) {
        elfDumpLookup = new File(args[0]);
        inFileToTranslate = new File(args[1]);

        knowTypes.add("NOTYPE");
        knowTypes.add("FUNC");
    }

    private static String joinSet(Set<String> values) {
        String[] concatenated = new String[values.size()];
        int idx = 0;
        for (String value : values) {
            concatenated[idx++] = value;
        }
        return String.join(", ", concatenated);
    }

    public static void main(String[] args) throws IOException {
        ElfAddressToNameTranslator r;
        try {
            r = new ElfAddressToNameTranslator(args);
            try {
                for (Map.Entry<String, Set<String>> entry : r.conversionTable().entrySet()) {
                    System.out.println(entry.getKey() + "-> {" + joinSet(entry.getValue()) + "}");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            r.convert();
        } catch (Exception e) {
            usage();
        }
    }

    private static void usage() {

        String name;
        try {
            name = new File(ElfAddressToNameTranslator.class.getProtectionDomain().getCodeSource()
					.getLocation().toURI().getPath()).getName();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("\n\n");
        System.out.println("Usage");
        System.out.println("java -jar " + name + " <elfDumpFile> <fileToTranslate>");
        System.out.println("the converted result is stored in same folder as <fileToTranslate> named pretty-<fileToTranslate>");
        System.out.println("\n");
    }

    private void convert() throws IOException {
        System.out.println("process dump <" + elfDumpLookup.getName() + "> and dot <" + inFileToTranslate.getName() + ">");

        Map<String, Set<String>> conversionTable = conversionTable();

        File translatedOutFile = new File(inFileToTranslate.getParent() + "/pretty-" + inFileToTranslate.getName
				());
        translatedOutFile.delete();
        if (!translatedOutFile.createNewFile()) {
            throw new IOException("cannot create new file");
        }

        PrintWriter fileWriter = new PrintWriter(translatedOutFile);

        FileUtils.lineIterator(inFileToTranslate);
        Files.lines(Paths.get(inFileToTranslate.getName())).forEachOrdered(l -> fileWriter.println
				(renameAddresses(conversionTable, l).toLowerCase()));

        fileWriter.close();
        System.out.println("wrote file to " + translatedOutFile.getPath());
    }

    private String renameAddresses(Map<String, Set<String>> conversionTable, String line) {
        Pattern addressPattern = Pattern.compile("0x[A-Fa-f0-9]{4}");
        Matcher matcher = addressPattern.matcher(line);

        while (matcher.find()) {
            String address = matcher.group().toLowerCase();
            String lookUpKey = address.replace("0x", "0000");// .replace(" -",
            // "");
            System.out.print("inspect " + address + " " + lookUpKey + " -> ");
            Set<String> names = conversionTable.get(lookUpKey);

            if (names != null) {
                String name;
                if (names.size() <= 1) {
                    name = names.iterator().next();
                } else {
                    name = "{" + joinSet(names) + "}";
                }
                System.out.println(name);
                line = Pattern.compile(address).matcher(line).replaceAll(name);
            } else {
                System.out.println();
            }
        }
        return line;
    }

    private Map<String, Set<String>> conversionTable() throws IOException {
        LineIterator line = FileUtils.lineIterator(elfDumpLookup);
        // Num: Value Size Type Bind Vis Ndx Name
        // 0: 00000000 0 NOTYPE LOCAL DEFAULT UND NAME
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{8}");
        HashMap<String, Set<String>> addressToLabel = new HashMap<>();

        while (line.hasNext()) {
            try {
                String[] values = line.nextLine().replaceAll("[ ]+", " ").split(" ");
                Matcher matcher = pattern.matcher(values[2]);
                // line must contain address
                if (!matcher.matches()) {
                    continue;
                }

                String type = values[4];
                // line must contain a known type
                if (!knowTypes.contains(type)) {
                    continue;
                }

                String address = values[2];
                String name = values[8];

                Set<String> knownAddressNames = addressToLabel.get(address.toLowerCase());
                if (knownAddressNames == null) {
                    knownAddressNames = new HashSet<>();
                    knownAddressNames.add(name);
                    addressToLabel.put(address.toLowerCase(), knownAddressNames);
                } else {
                    knownAddressNames.add(name);
                }
            } catch (IndexOutOfBoundsException e) {
//                continue;
            }
        }
        line.close();
        return addressToLabel;
    }
}
