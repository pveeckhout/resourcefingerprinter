package com.honeyedoak.resourcefingerprinter;

import com.honeyedoak.resourcefingerprinter.models.ResourceFile;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@ToString
public class ResourceFingerprinter {

    private String infilePath, outFilePath;
    private String digest = "SHA-512";
    private boolean delete;
    private boolean create = true;

    private static String[] testArgs = new String[]{"-i", "target/classes/inFile.json", "-o", "target/classes/outFilePath.json"};

    public static void main(String[] args) {
        //new ResourceFingerprinter(args);

        ResourceFingerprinter fingerprinter = new ResourceFingerprinter(testArgs);

        JSONArray obj = fingerprinter.parseInfile();
        List<ResourceFile> resourceFileList = new ArrayList<>();

        for (int i = 0; i < obj.length(); i++) {
            resourceFileList.add(ResourceFile.fromInputJsonObject(obj.getJSONObject(i)));
        }

        fingerprinter.generateNewFingerprints(resourceFileList);
        try {
            fingerprinter.processResourceFiles(resourceFileList);
        } catch (IOException e) {
            System.out.println("error during saving new file: " + e.getClass().getSimpleName() + " | " + e.getMessage());
            e.printStackTrace();
            System.exit(-3);
        }

        fingerprinter.generateOutFile(resourceFileList);
    }

    private void generateOutFile(List<ResourceFile> resourceFileList) {
        JSONArray jsonArray = new JSONArray();
        for (ResourceFile resourceFile : resourceFileList) {
            jsonArray.put(ResourceFile.toOutputJsonObject(resourceFile));
        }

        try {
            File outFile = new File(outFilePath);
            if (outFile.exists()) {
                outFile.delete();
            }
            outFile.createNewFile();


            FileUtils.writeStringToFile(new File(outFilePath), jsonArray.toString(), "UTF-8");
        } catch (IOException e) {
            System.out.println("error during writing outputFile: " + e.getClass().getSimpleName() + " | " + e.getMessage());
            e.printStackTrace();
            System.exit(-4);
        }
    }

    private void processResourceFiles(List<ResourceFile> resourceFileList) throws IOException {
        for (ResourceFile resourceFile : resourceFileList) {
            if (!resourceFile.getNewFingerprint().equals(resourceFile.getOldFingerprint())) {
                if (delete || create) {
                    int lastDotIndex = resourceFile.getPath().lastIndexOf(".");
                    if (delete) {
                        StringBuilder builder = new StringBuilder(resourceFile.getPath());
                        builder.insert(lastDotIndex, resourceFile.getOldFingerprint()).insert(lastDotIndex, ".");
                        File oldFile = new File(builder.toString());
                        oldFile.deleteOnExit();
                    }
                    if (create) {
                        StringBuilder builder = new StringBuilder(resourceFile.getPath());
                        builder.insert(lastDotIndex, resourceFile.getNewFingerprint()).insert(lastDotIndex, ".");
                        FileUtils.copyFile(new File(resourceFile.getPath()), new File(builder.toString()));
                    }
                }
            }
        }
    }

    public void generateNewFingerprints(List<ResourceFile> resourceFileList) {
        try {
            for (ResourceFile resourceFile : resourceFileList) {
                File file = new File(resourceFile.getPath());
                MessageDigest messageDigest = MessageDigest.getInstance(digest);
                InputStream fis = new FileInputStream(file);
                int n = 0;
                byte[] buffer = new byte[8192];
                while (n != -1) {
                    n = fis.read(buffer);
                    if (n > 0) {
                        messageDigest.update(buffer, 0, n);
                    }
                }
                resourceFile.setNewFingerprint(String.format("%032X", new BigInteger(1, messageDigest.digest())));
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.println("error during generation of fingerprints: " + e.getClass().getSimpleName() + " | " + e.getMessage());
            e.printStackTrace();
            System.exit(-2);
        }
    }

    private JSONArray parseInfile() {
        try {
            return new JSONArray(new String(Files.readAllBytes(Paths.get(infilePath))));
        } catch (IOException e) {
            System.out.println("error during reading and parsing infile: " + e.getClass().getSimpleName() + " | " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        //we can never arrive here
        return null;
    }

    public ResourceFingerprinter(String[] args) {

        parseInputArgs(args);

        System.out.println(this.toString());
    }

    private void parseInputArgs(String[] args) {
        Iterator<String> argIt = Arrays.asList(args).iterator();

        try {
            while (argIt.hasNext()) {
                String arg = argIt.next();

                switch (arg) {
                    case "-i":
                    case "--inFile":
                        infilePath = argIt.next();
                        break;
                    case "-o":
                    case "--outFilePath":
                        outFilePath = argIt.next();
                        break;
                    case "-d":
                    case "--delete":
                        delete = true;
                        break;
                    case "-C":
                    case "--no-create":
                        create = false;
                        break;
                    default:
                        throw new IllegalArgumentException("argument " + arg + " not recognized");
                }
            }
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("program started with malformed argument string");
        }
    }
}
