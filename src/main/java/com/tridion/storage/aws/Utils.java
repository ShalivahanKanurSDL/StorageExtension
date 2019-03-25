package com.tridion.storage.aws;

import java.io.*;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.net.FileNameMap;
import java.net.URLConnection;

/**
 * Utils.
 *
 * Helper functions
 */
public class Utils
{
    public static String NEWLINE = System.getProperty("line.separator");

    public static String EscapeRegex(String input)
    {
        return input.replace("\\", "\\\\").replace("[", "\\[").replace("^", "\\^").replace("]", "\\]").replace("$", "\\$").replace(".", "\\.").replace("|", "\\|").replace("?", "\\?").replace("+", "\\+").replace("{", "\\{").replace("}", "\\}").replace("(", "\\(").replace(")", "\\)").replace("-", "\\-").replace("*", "\\*").replace("=", "\\=").replace("<", "\\<");
    }

    public static String UnescapeRegex(String input)
    {
        return input.replace("\\\\", "\\").replace("\\[", "[").replace("\\^", "^").replace("\\]", "]").replace("\\$", "$").replace("\\.", ".").replace("\\|", "|").replace("\\?", "?").replace("\\+", "+").replace("\\{", "{").replace("\\}", "}").replace("\\(", "(").replace("\\)", ")").replace("\\-", "-").replace("\\*", "*").replace("\\=", "=").replace("\\<", "<");
    }

    public static String RegexReplaceFirst(String pattern, String input, String replacement)
    {
        if (!StringIsNullOrEmpty(input) && !StringIsNullOrEmpty(pattern))
        {
            String i = EscapeRegex(input);
            i = Pattern.quote(i);
            String result = i.replaceFirst(pattern, EscapeRegex(replacement));
            result = UnescapeRegex(result);
            result = result.substring(2);
            return result.substring(0, result.lastIndexOf("\\E"));
        }
        if (!StringIsNullOrEmpty(input))
        {
            return input;
        }
        return "";
    }

    public static String RegexReplaceAll(String pattern, String input, String replacement)
    {
        if (!StringIsNullOrEmpty(input) && !StringIsNullOrEmpty(pattern))
        {
            String i = EscapeRegex(input);
            i = Pattern.quote(i);
            String result = i.replaceAll(pattern, EscapeRegex(replacement));
            result = UnescapeRegex(result);

            result = result.substring(2);
            return result.substring(0, result.lastIndexOf("\\E"));
        }
        if (!StringIsNullOrEmpty(input))
        {
            return input;
        }
        return "";
    }

    public static String GetBinaryFileExtension(String filepath)
    {
        int dotPos = filepath.lastIndexOf(".");
        if (dotPos < 0)
            return new String("");
        return filepath.substring(dotPos + 1);
    }

    public static String GetBinaryFileName(String filepath)
    {
        // Can't use File.pathSeparator as Tridion gives / in paths

        String separator = "";
        if (filepath.contains("\\"))
        {
            separator = "\\";
        }
        else if (filepath.contains("/"))
        {
            separator = "/";
        }
        else
        {
            return filepath;
        }

        int slashPos = filepath.lastIndexOf(separator);
        if (slashPos < 0)
            return filepath;

        String fullFileName = filepath.substring(slashPos + 1);

        return fullFileName.substring(0, fullFileName.lastIndexOf("."));
    }

    public static boolean StringIsNullOrEmpty(String input)
    {
        return !(input != null && input.trim().length() > 0);
    }

    public static boolean StringArrayContains(String[] stringArray, String testString)
    {
        if (stringArray == null || stringArray.length == 0 || Utils.StringIsNullOrEmpty(testString))
        {
            return false;
        }
        return Arrays.asList(stringArray).contains(testString);
    }

    public static String convertTransactionIdToPath(String id)
    {
        return id.replace(":", "_");
    }

    public static String stacktraceToString(StackTraceElement[] s)
    {
        StringBuilder toReturn = new StringBuilder();
        for (StackTraceElement e : s)
        {
            toReturn.append(e.getClassName() + " - " + e.getLineNumber() + " : " + e.getMethodName());
            toReturn.append(NEWLINE);
        }
        return toReturn.toString();
    }

    public static String GetItemIdFromFilepath(String filepath, String extension)
    {
        int slashPos = filepath.lastIndexOf("/");
        if (slashPos < 0)
            slashPos = 0;
        int dotPos = filepath.lastIndexOf(extension) - 1;
        if (dotPos < 0 || dotPos <= slashPos)
        {
            dotPos = filepath.length() - 1;
        }
        if (filepath.startsWith("/"))
            filepath = filepath.replaceFirst("/", "");
        return filepath.substring(slashPos, dotPos);
    }

    public static String RemoveLineBreaks(String input)
    {
        if (!Utils.StringIsNullOrEmpty(input))
        {
            return input.replaceAll(NEWLINE, "");
        }
        return input;
    }

    public static String getMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(fileName);
        return mimeType;
    }

    public static void WriteFile(String strval) {
        try {
            //Whatever the file path is.
            File statText = new File("E:/Test/statsTest.txt");
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            w.write(strval);
            w.close();
        } catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
        }
    }

    public static void writeXMLFile(String canonicalFilename, String text,String filePath,String locale)
    {
        try {
            CreateDirectory(filePath,locale);
            File file = new File (filePath + "\\" + locale + "\\" + canonicalFilename);
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(text);
            out.close();
        }
        catch (IOException e) {
            System.err.println("Problem writing to the file statsTest.txt");
        }
    }

    private static Boolean CreateDirectory(String filePath,String locale ){

        boolean isDirectoryCreated=false;
        File dir = new File(filePath + "\\" + locale);
        //if directory exists?
        if (!dir.exists()) {
            try {
                isDirectoryCreated = dir.mkdirs();
            } catch (Exception e) {
                //fail to create directory
                System.err.println("Problem while creating directory");
            }
        }
        return isDirectoryCreated;
    }

    public static Boolean isValideFilePath(String FilePath)
    {
        Boolean isDirectoryExist=false;
        try {

            File file = new File(FilePath);
            if (file.isDirectory()) {
                isDirectoryExist= true;
            }
        }
        catch(Exception e) {
            System.err.println("Problem writing to the file statsTest.txt");
            isDirectoryExist=false;
        }
        return isDirectoryExist;
    }
}

