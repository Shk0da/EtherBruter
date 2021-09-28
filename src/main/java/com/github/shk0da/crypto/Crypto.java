package com.github.shk0da.crypto;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import static java.lang.System.out;
import static java.lang.System.setOut;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.TimeZone.setDefault;
import static java.util.concurrent.ThreadLocalRandom.current;

public class Crypto {

    private static final File walletsDir = new File("wallets");
    private static final String ETHEREUM_NODE_LOCAL = "http://localhost:8545";
    private static final Web3j WEB3 = Web3j.build(new HttpService(ETHEREUM_NODE_LOCAL));

    static {
        if (!walletsDir.exists()) {
            if (walletsDir.mkdir()) {
                out.printf("wallets: %s%n", walletsDir.getAbsolutePath());
            }
        }
    }

    public static void main(String[] args) {
        setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, UTF_8));
        out.printf("%s: Start Crypto%n", new Date());

        try {
            out.printf("Current Gas price: %s%n", getGasPrice());
            var alphabet = readAlphabet();
            while (true) {
                StringBuilder phrase = new StringBuilder();
                for (int i = 0; i < 12; i++) {
                    int random = current().nextInt(0, alphabet.length);
                    phrase.append(alphabet[random]);
                    phrase.append(" ");
                }
                var mnemonic = phrase.toString().trim();
                out.println("mnemonic: " + mnemonic);
                checkWallet(mnemonic);
                out.println();
                Thread.sleep(20);
            }
        } catch (Exception ex) {
            out.printf("Error: %s%n", ex.getMessage());
            ex.printStackTrace();
        }
        out.printf("%s: Finish Crypto%n", new Date());
    }

    public static String[] readAlphabet() throws IOException {
        List<String> words = new ArrayList<>(500);
        try (FileInputStream inputStream = new FileInputStream("alphabet/words.txt");
             Scanner sc = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().toLowerCase(Locale.ROOT);
                if (line.length() >= 4 && line.length() <= 9 && line.chars().allMatch(Character::isLetter)) {
                    words.add(line);
                }
            }
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        }
        return words.toArray(new String[]{});
    }

    private static void checkWallet(String mnemonic) throws InterruptedException, ExecutionException, IOException, CipherException {
        Credentials credentials = WalletUtils.loadBip39Credentials("", mnemonic);
        String address = credentials.getAddress();
        out.printf("Address: %s%n", address);
        var balance = getBalance(address);
        if (balance > 0.0) {
            out.printf("Balance: %s%n", balance);
            FileWriter fileWriter = new FileWriter(walletsDir.getPath() + "/" + address);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.printf("Balance: %s%n", balance);
            printWriter.close();
            generateWallet(mnemonic);
        }
    }

    private static void generateWallet(String mnemonic) throws CipherException, IOException {
        var wallet = WalletUtils.generateBip39WalletFromMnemonic("", mnemonic, walletsDir);
        out.printf("Wallet: %s%n", wallet.getFilename());
        out.printf("Mnemonic: %s%n", wallet.getMnemonic());
    }

    public static double getGasPrice() throws InterruptedException, ExecutionException {
        var ethGasPrice = WEB3.ethGasPrice().sendAsync().get();
        return ethGasPrice.getGasPrice().doubleValue();
    }

    private static double getBalance(String address) throws InterruptedException, ExecutionException {
        EthGetBalance ethGetBalance = WEB3.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger wei = ethGetBalance.getBalance();
        BigDecimal divValue = BigDecimal.valueOf(1_000_000_000L);
        BigDecimal gwei = new BigDecimal(wei).divide(divValue, MathContext.DECIMAL64);
        BigDecimal ether = gwei.divide(divValue, MathContext.DECIMAL64);
        return ether.doubleValue();
    }
}
