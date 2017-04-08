package com.honeyedoak.resourcefingerprinter;

import org.apache.commons.cli.Options;

public class CommandLineOptions {

    public static final Options constructOptions() {
        final Options options = new Options();
        options.addOption("i", "inFile", true, "The describing the source files, and current fingerprint")
                .addOption("o", "outFile", true, "the filePath of the output file");

        return options;
    }
}
