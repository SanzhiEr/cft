import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CtfTask {
    static String paramSort = "-a";
    static String type = "";
    static String output = "";
    static String[] input = null;

    static boolean out = false;
    static short count = 0;
    static short index = 0;


    public static void main(String[] args) throws IOException {

        initParams(args);
        repairData();
        saveInFile();
    }


    private static void saveInFile() {
        try {
            String[] strings = new String[input.length];
            BufferedReader[] bufferedReaders = new BufferedReader[(input.length)];//
            for (int i = 0; i < input.length; i++) {
                bufferedReaders[i] = Files.newBufferedReader(Paths.get(input[i]));
            }


            for (int i = 0; i < bufferedReaders.length; i++) {
                strings[i] = bufferedReaders[i].readLine();
            }


            BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(output));

            boolean work = true;
            while (work) {
                int indexMin = -1;
                for (int i = 0; i < strings.length; i++) {
                    if (strings[i] != null) {
                        indexMin = i;
                        break;
                    }
                }
                for (int i = 0; i < strings.length; i++) {
                    if (strings[i] != null) {
                        if (type.equals("-s")) {
                            if (paramSort.equals("-a")) {
                                if (getRes(type, strings[indexMin], strings[i]) >= 0) {
                                    indexMin = i;
                                }
                            } else {
                                if (getRes(type, strings[i], strings[indexMin]) >= 0) {
                                    indexMin = i;
                                }
                            }
                        } else {
                            if (paramSort.equals("-a")) {
                                if (getRes(type, strings[indexMin], strings[i]) >= 0) {
                                    indexMin = i;
                                }
                            } else {
                                if (getRes(type, strings[i], strings[indexMin]) >= 0) {
                                    indexMin = i;
                                }
                            }
                        }
                    }
                }

                //todo:запись в файл
                bufferedWriter.write(strings[indexMin] + "\n");

                strings[indexMin] = bufferedReaders[indexMin].readLine();

                work = false;
                for (String string : strings) {
                    if (string != null) {
                        work = true;
                        continue;
                    }
                }
            }

            bufferedWriter.flush();
            bufferedWriter.close();

            for (BufferedReader bufferedReader : bufferedReaders) {
                bufferedReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initParams(String args[]) {
        /** аргументы командной строки
         *
         */
        for (String arg : args) {
            count++;
            if (arg.startsWith("-")) {
                if (arg.equals("-a") || arg.equals("-d")) {
                    paramSort = arg;
                } else if (arg.equals("-s") || arg.equals("-i")) {
                    type = arg;
                }
            } else {
                if (!out) {
                    output = arg;
                    out = true;
                } else {
                    if (input == null) {
                        input = new String[args.length - count + 1];
                    }
                    input[index++] = arg;
                }
            }
        }
    }

    private static void repairData() {
        /**
         * нужные данные пересохраняются в другой файл
         */
        for (String s : input) {
            if (Files.exists(Paths.get(s))
                    && Files.isRegularFile(Paths.get(s))) {
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(s.replaceFirst(".txt", "") + "temp"), StandardCharsets.UTF_8)) {
                    try (BufferedReader br = Files.newBufferedReader(Paths.get(s), StandardCharsets.UTF_8)) {
                        String line = br.readLine(), line2 = br.readLine();
                        while (line != null && line2 != null) {
                            /***
                             * если сортируем числа
                             */
                            if (type.equals("-i")) {
                                if (!IsNumber.isNumber(line) && IsNumber.isNumber(line2)) {
                                    line = line2;
                                    continue;
                                }

                                while (!IsNumber.isNumber(line)) {
                                    line = br.readLine();
                                    if (line == null) {
                                        break;
                                    }

                                }

                                if (IsNumber.isNumber(line)) {
                                    while (!IsNumber.isNumber(line2)) {
                                        line2 = br.readLine();
                                        if (line2 == null) {
                                            break;
                                        }
                                    }
                                }
                            }


                            /**
                             * основная проверка
                             */
                            if (line != null && line2 != null) {
                                if (checkSort(paramSort, type, line, line2)) {
                                    bufferedWriter.write(line + "\n");
                                    line = line2;
                                    line2 = br.readLine();
                                } else {
                                    line2 = br.readLine();
                                }
                            }

                            if (line2 == null && line != null) {
                                bufferedWriter.write(line);
                            }
                        }
                        bufferedWriter.flush();

                    } catch (IOException e) {
                        System.out.println("error when read file");
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.out.println("error when write file");
                    e.printStackTrace();
                }
            } else {
                System.out.println("FILE NOT FOUND: " + s);
            }


            try {
                if (Files.exists(Paths.get(s.replaceFirst(".txt", "") + "temp"))) {
                    Files.delete(Paths.get(s));
                    Files.move(Paths.get(s.replaceFirst(".txt", "temp")), Paths.get(s));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("ALLDATA FIXED");
    }

    public static boolean checkSort(String paramSort, String type, String s1, String s2) {
        int res;
        if (paramSort.equals("-a")) {
            res = getRes(type, s1, s2);
        } else {
            res = getRes(type, s2, s1);
        }

        if (res <= 0) {
            return true;
        } else {
            return false;
        }
    }

    private static int getRes(String type, String s1, String s2) {
        if (type.equals("-i")) {
            return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
        } else {
            return s1.compareTo(s2);
        }
    }
}

