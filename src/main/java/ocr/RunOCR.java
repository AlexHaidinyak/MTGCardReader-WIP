package ocr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RunOCR {
    private final String pythonPath = "C:\\Users\\alexh\\AppData\\Local\\Programs\\Python\\Python313\\python.exe";
    private final String ocrPath = "C:\\Users\\alexh\\PycharmProjects\\PythonProject\\OcrTest.py";
    private ProcessBuilder processBuilder;

    public String[] runImage(Path imagePath) throws IOException, InterruptedException {
        processBuilder = new ProcessBuilder(pythonPath, ocrPath, imagePath.toAbsolutePath().toString());
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = processBuilder.start();

        List<String> results = new ArrayList<>();
        BufferedReader reader= new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            results.add(line);
        }
        process.waitFor();

        for(String result : results) {
            System.out.println("OCR Result: " + result);
        }

        return filterResults(results);
    }

    private String[] filterResults(List<String> results) {
        String setNumber, setId;
        String[] combinedId = new String[2];

        String firstPart = results.getFirst();
        String[] filterFirst = firstPart.split(" ");
        if(filterFirst.length > 1) {
            setNumber = filterFirst[1];
        }
        else {
            setNumber = filterFirst[0];
        }
        while(setNumber.charAt(0) == '0'){
            if(setNumber.length() > 1){
                setNumber = setNumber.substring(1);
            }
        }

        String secondPart = results.get(1);
        String[] filterSecond = secondPart.split(" ");
        setId = filterSecond[0].toLowerCase();

        combinedId[0] = setId;
        combinedId[1] = setNumber;

        System.out.println("Combined ID: " + Arrays.toString(combinedId));

        return combinedId;
    }
}
