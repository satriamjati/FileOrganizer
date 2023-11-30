/*
 * The MIT License
 *
 * Copyright 2023 GaLa.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fileorganizer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 *
 * @author GaLa
 */
public class Main {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {
        
        /* Your directory for duplicates removal */
        String directoryPath = "\\duMmy";
        
        /* Choose algorithm: "MD5" | "SHA-1" | "SHA-256" */
        String checksumAlgorithm = "SHA-256";

        
        try {
            /* Choose exploration mode: deleteDuplicates() | deleteDuplicatesOverFolders() */
//            deleteDuplicates(directoryPath,checksumAlgorithm); //uncomment for deleteDuplicates() mode
            deleteDuplicatesOverFolders(directoryPath,checksumAlgorithm); //delete or comment this line for deleteDuplicates() mode
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException: " + e.getMessage());
        }
       
    }

    private static void deleteDuplicates(String directoryPath, String checksumAlgorithm) throws IOException, NoSuchAlgorithmException {
        HashMap<String, Path> fileChecksumMap = new HashMap<>();
        
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            for (Path filePath : directoryStream) {
                if (Files.isRegularFile(filePath)) {
                    String checksum = getChecksum(filePath,checksumAlgorithm);
                    if (fileChecksumMap.containsKey(checksum)) {
                        System.out.println("Deleting duplicate of "+fileChecksumMap.get(checksum).getFileName().toString()+" : " + filePath);
                        countDuplicates++;
                        Files.delete(filePath);
                    } else {
                        fileChecksumMap.put(checksum, filePath);
                    }
                }
            }
        }
        displayCountDuplicates();       
    }
    
    private static int countDuplicates = 0;
    private static void displayCountDuplicates(){
        System.out.println(countDuplicates + " duplicate(s) deleted");
        countDuplicates = 0;
    }
    private static void deleteDuplicatesOverFolders(String directoryPath, String checksumAlgorithm) throws IOException, NoSuchAlgorithmException {
        HashMap<String, Path> fileChecksumMap = new HashMap<>();
        
        
        Files.walkFileTree(Paths.get(directoryPath), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
                try {
                    if (Files.isRegularFile(filePath)) {
                        String checksum = getChecksum(filePath, checksumAlgorithm);
                        if (fileChecksumMap.containsKey(checksum)) {
                            System.out.println("Deleting duplicate of " + fileChecksumMap.get(checksum).getFileName().toString() + " : " + filePath);
                            countDuplicates++;
                            Files.delete(filePath);
                        } else {
                            fileChecksumMap.put(checksum, filePath);
                        }
                    }
                } catch (IOException | NoSuchAlgorithmException e) {
                    // Handle the exception as needed
                    System.err.println("Error processing file: " + filePath + ". " + e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        displayCountDuplicates(); 
    }

    private static String byteToHex(byte b){
        return String.format("%02x",b);
    }

    private static String getChecksum(Path filePath, String checksumAlgorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(checksumAlgorithm);
        try (DigestInputStream dis = new DigestInputStream(Files.newInputStream(filePath), md)) {
            while (dis.read() != -1) {}
        }   catch (IOException e) {
            throw new IOException("Error reading file: " + e.getMessage(), e);
        }

        byte[] digest = md.digest();
        StringBuilder checksum = new StringBuilder();
        for (byte b : digest) {
            checksum.append(byteToHex( b));
        }

        return checksum.toString();
    }
}
