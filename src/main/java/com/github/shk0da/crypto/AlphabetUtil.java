package com.github.shk0da.crypto;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class AlphabetUtil {

    public static void main(String[] args) throws IOException {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter("alphabet/clean_words.txt"));
             FileInputStream inputStream = new FileInputStream("alphabet/words.txt");
             Scanner sc = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().toLowerCase(Locale.ROOT);
                if (line.length() >= 4 && line.length() <= 9 && line.chars().allMatch(Character::isLetter)) {
                    printWriter.println(line);
                }
            }
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        }
    }
}
