package io.github.majchjan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;

public final class App {
    public static void drawPage(PDPageContentStream contentStream, PDFormXObject page, boolean isLeft) throws Exception {


        boolean isLandscape = page.getBBox().getWidth() > page.getBBox().getHeight();
        float currentWidth = page.getBBox().getWidth();
        float currentHeight = page.getBBox().getHeight();
        
        float scale = 1.0f;

        if(isLandscape){
            scale = (PDRectangle.A4.getHeight() / 2) / currentWidth;
        } else {
            scale = PDRectangle.A4.getWidth() / currentHeight;
        }

        contentStream.saveGraphicsState();
        contentStream.transform(Matrix.getRotateInstance(Math.toRadians(-90), 0, 0));
        contentStream.transform(Matrix.getTranslateInstance(isLeft ? - PDRectangle.A4.getHeight() : - PDRectangle.A4.getHeight() / 2, (PDRectangle.A4.getWidth() - currentHeight * scale) / 2));
        contentStream.transform(Matrix.getScaleInstance(scale, scale));                
        contentStream.drawForm(page);
        contentStream.restoreGraphicsState();
    }
    
    public static String printUsage() {
    return "BookletImposer – convert A4 PDF into booklet layout.\n" + 
            "USAGE\n" + 
            "\tjava -jar bookletimposer.jar -i <input.pdf> [-o <output.pdf>] [--isDuplexPrinter]\n" + 
            "\n" + 
            "OPTIONS\n" + 
            "-i, --input <file>\n" + 
            "    \tInput PDF file (required)" + 
            "\n" + 
            "-o, --output <file>\n" + 
            "    \tOutput PDF file\n" + 
            "    \tDefault: <input>_booklet.pdf\n" + 
            "\n" + 
            "--isDuplexPrinter\n" + 
            "    \tOptimize output order for duplex printers\n" + 
            "\n" + 
            "DESCRIPTION\n" + 
            "The program rearranges pages so that the document can be printed\n" + 
            "as a folded booklet (A4 sheets folded into A5 pages).\n" + 
            "\n" + 
            "EXAMPLES\n" + 
            "Duplex printer:\n" + 
            "    \tjava -jar bookletimposer.jar -i book.pdf --isDuplexPrinter\n" + 
            "\n" + 
            "Manual duplex (single-sided printer):\n" + 
            "    \tjava -jar bookletimposer.jar -i book.pdf\n" + 
            "\n" + 
            "Custom output file:\n" + 
            "    \tjava -jar bookletimposer.jar -i book.pdf -o booklet.pdf\n";
    }

    public static Map<String, String> getArgumentValue(String[] args){

        Map<String, String> argMap = new HashMap<>();

        for(int i = 0; i < args.length; i++){
            switch (args[i]) {

                case "-h":
                case "--help":
                    printUsage();
                    throw new IllegalArgumentException("Help requested.");

                case "-i":
                case "--input":
                    argMap.put("inputFilePath", args[++i]);
                    break;

                case "-o":
                case "--output":
                    argMap.put("outputFilePath", args[++i]);
                    break;

                case "--isDuplexPrinter":
                    argMap.put("isDuplexPrinter", "true");
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unknown argument: " + args[i] + "\n" + printUsage());
            }
        }
        return argMap;
    }

    public static void main(String[] args) throws Exception {

        File inputFile = null;
        File outputFile = null;
        boolean isDuplexPrinter = false;

        try {
            Map<String, String> argMap = getArgumentValue(args);

            if (!argMap.containsKey("inputFilePath")) {
                throw new IllegalArgumentException("Input file path is required.\n" + printUsage());
            }

            inputFile = new File(argMap.get("inputFilePath"));
            outputFile = new File(argMap.getOrDefault("outputFilePath", argMap.get("inputFilePath").substring(0, argMap.get("inputFilePath").lastIndexOf(".")) + "_booklet.pdf"));
            isDuplexPrinter = argMap.getOrDefault("isDuplexPrinter", "false").equalsIgnoreCase("true");

        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        try(PDDocument input = PDDocument.load(inputFile);
             PDDocument output = new PDDocument()) {

            int totalPages = input.getNumberOfPages();

            // Number of pages in output file must be a multiple of 4
            int padded = ((totalPages + 3) / 4) * 4;

            LayerUtility layerUtility = new LayerUtility(output);

            List<int[]> pagePairsFirstSide = new ArrayList<>();
            for (int i = 0; i < padded / 4; i++){
                pagePairsFirstSide.add(new int[]{padded - (2*i), 1 + (2*i)});
            }

            List<int[]> pagePairsSecondSide = new ArrayList<>();
            for (int i = 0; i < padded / 4; i++){
                pagePairsSecondSide.add(new int[]{2 + (2*i), padded - 1 - (2*i)});
            }

            if(pagePairsFirstSide.size() != pagePairsSecondSide.size()){
                throw new Exception("Page pairs must be of the same size");
            }

            List<int[]> pagePairs = new ArrayList<>();

            if (isDuplexPrinter){
                for(int i = 0; i < pagePairsFirstSide.size(); i++){
                    pagePairs.add(pagePairsFirstSide.get(i));
                    pagePairs.add(pagePairsSecondSide.get(i));
                }

            } else {

                pagePairs.addAll(pagePairsFirstSide);
                pagePairs.addAll(pagePairsSecondSide);
            }

            for(int[] pair : pagePairs){
                int leftIndex = pair[0];
                int rightIndex = pair[1];

                PDPage newPage = new PDPage(PDRectangle.A4);
                output.addPage(newPage);

                try (PDPageContentStream contentStream = new PDPageContentStream(output, newPage)) {

                    if (leftIndex <= totalPages) {
                        PDFormXObject leftPage = layerUtility.importPageAsForm(input, leftIndex - 1);
                        
                        drawPage(contentStream, leftPage, true);
                    }

                    if (rightIndex <= totalPages) {
                        PDFormXObject rightPage = layerUtility.importPageAsForm(input, rightIndex - 1);

                        drawPage(contentStream, rightPage, false);
                    }
                }
            }

            output.save(outputFile);
        }
    }
}