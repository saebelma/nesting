package nesting.application;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nesting.geometry.elements.Polygon;
import nesting.io.PolygonParser;
import nesting.irregular.IrregularShapeNesting;
import nesting.packing.BoxPacking;
import nesting.packing.CirclePacking;
import nesting.svg.Drawable;
import nesting.svg.SVG;
import nesting.svg.SVG.Style;
import nesting.tuple.TupleNesting;

/**
 * A simple GUI application demonstrating various packing and nesting algorithms.
 */
public class GUI extends Application {

    // Model
    private Polygon outlinePolygon;
    private String preformName;
    private long timerStart;

    // View
    WebEngine webEngine;
    TextArea textArea;

    // Controller
    Stage stage;
    FileChooser fileChooser;
    VBox vBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Set stage
        stage = primaryStage;

        // Create file chooser and set initial directory
        fileChooser = new FileChooser();
        fileChooser
                .setInitialDirectory(new File(System.getProperty("user.dir")));

        // Set path for svg
        SVG.setPath(System.getProperty("user.dir"));

        // Create web view
        WebView webView = new WebView();
        webEngine = webView.getEngine();
        webView.addEventFilter(ScrollEvent.SCROLL, (ScrollEvent e) -> {
            double deltaY = e.getDeltaY();
            if (e.isControlDown() && deltaY > 0) {
                webView.setZoom(webView.getZoom() * 1.1);
                e.consume();
            } else if (e.isControlDown() && deltaY < 0) {
                webView.setZoom(webView.getZoom() / 1.1);
                e.consume();
            }
        });

        // Create text area
        textArea = new TextArea();
        textArea.setEditable(false);

        // Create controls vbox
        vBox = new VBox();

        // Create buttons
        createButton("Load Preform", () -> {
            loadPreform();
        });
        createButton("Box Packing", () -> {
            boxPacking();
        });
        createButton("Circle Packing", () -> {
            circlePacking();
        });

        // Create radio buttons and toggle group for irregular shape nesting criterion selection
        RadioButton convexHullCriterion = new RadioButton(
                "Convex Hull Criterion");
        RadioButton smallestEnclosingCircleCriterion = new RadioButton(
                "Smallest Enclosing Circle Criterion");
        ToggleGroup irregularShapeNestingCriterion = new ToggleGroup();
        convexHullCriterion.setToggleGroup(irregularShapeNestingCriterion);
        smallestEnclosingCircleCriterion
                .setToggleGroup(irregularShapeNestingCriterion);
        convexHullCriterion.setSelected(true);

        // Add controls
        createButton("Irregular Shape Nesting", () -> {
            IrregularShapeNesting.setUseSmallestEnclosingCircleCriterion(
                    smallestEnclosingCircleCriterion.isSelected());
            irregularShapeNesting();
        });
        vBox.getChildren().addAll(convexHullCriterion,
                smallestEnclosingCircleCriterion);

        // Create radio buttons and toggle groups for tuple nesting criterion selection
        
        RadioButton convexHullForPairs = new RadioButton(
                "Convex Hull Criterion");
        RadioButton smallestEnclosingCircleForPairs = new RadioButton(
                "Smallest Enclosing Circle Criterion");
        ToggleGroup criterionForPairs = new ToggleGroup();
        convexHullForPairs.setToggleGroup(criterionForPairs);
        smallestEnclosingCircleForPairs.setToggleGroup(criterionForPairs);
        smallestEnclosingCircleForPairs.setSelected(true);

        RadioButton convexHullForQuadruples = new RadioButton(
                "Convex Hull Criterion");
        RadioButton smallestEnclosingCircleForQuadruples = new RadioButton(
                "Smallest Enclosing Circle Criterion");
        ToggleGroup criterionForQuadruples = new ToggleGroup();
        convexHullForQuadruples.setToggleGroup(criterionForQuadruples);
        smallestEnclosingCircleForQuadruples
                .setToggleGroup(criterionForQuadruples);
        smallestEnclosingCircleForQuadruples.setSelected(true);

        RadioButton convexHullForNesting = new RadioButton(
                "Convex Hull Criterion");
        RadioButton smallestEnclosingCircleForNesting = new RadioButton(
                "Smallest Enclosing Circle Criterion");
        ToggleGroup criterionForNesting = new ToggleGroup();
        convexHullForNesting.setToggleGroup(criterionForNesting);
        smallestEnclosingCircleForNesting.setToggleGroup(criterionForNesting);
        convexHullForNesting.setSelected(true);

        // Add controls
        createButton("Tuple Nesting", () -> {
            TupleNesting.setUseSmallestEnclosingCircleCriterionForPairs(
                    smallestEnclosingCircleForPairs.isSelected());
            TupleNesting.setUseSmallestEnclosingCircleCriterionForQuadruples(
                    smallestEnclosingCircleForQuadruples.isSelected());
            TupleNesting.setUseSmallestEnclosingCircleCriterionForNesting(
                    smallestEnclosingCircleForNesting.isSelected());
            tupleNesting();
        });
        vBox.getChildren().add(new Label("Pairs"));
        vBox.getChildren().addAll(convexHullForPairs,
                smallestEnclosingCircleForPairs);
        vBox.getChildren().add(new Label("Quadruples"));
        vBox.getChildren().addAll(convexHullForQuadruples,
                smallestEnclosingCircleForQuadruples);
        vBox.getChildren().add(new Label("Nesting"));
        vBox.getChildren().addAll(convexHullForNesting,
                smallestEnclosingCircleForNesting);

        // Top level container is border pane
        BorderPane borderPane = new BorderPane();
        borderPane.setRight(vBox);
        borderPane.setCenter(webView);
        borderPane.setBottom(textArea);

        // Set scene
        Scene scene = new Scene(borderPane);

        // Set stage
        stage.setTitle("Nesting Irregular Shapes in a Circular Area");
        stage.setScene(scene);
        stage.show();
    }

    private void boxPacking() {

        // Perform box packing
        startTimer();
        BoxPacking boxPacking = new BoxPacking(outlinePolygon);
        long elapsedTimeInMS = stopTimer();

        // Show in web view
        showInWebView(boxPacking);

        // Display in text area
        textArea.appendText("Box packing in  " + elapsedTimeInMS + " ms\n");
        textArea.appendText("n =  " + boxPacking.getN() + "\n");
    }

    private void circlePacking() {

        // Perform circle packing
        startTimer();
        CirclePacking circlePacking = new CirclePacking(outlinePolygon);
        long elapsedTimeInMS = stopTimer();

        // Show in web view
        showInWebView(circlePacking);

        // Display in text area
        textArea.appendText("Circle packing in  " + elapsedTimeInMS + " ms\n");
        textArea.appendText("n =  " + circlePacking.getN() + "\n");

    }

    private void irregularShapeNesting() {

        // Perform irregular shape nesting
        startTimer();
        IrregularShapeNesting irregularShapeNesting = new IrregularShapeNesting(
                outlinePolygon);
        long elapsedTimeInMS = stopTimer();

        // Show in web view
        showInWebView(irregularShapeNesting);

        // Display in text area
        textArea.appendText(
                "Irregular shape nesting in  " + elapsedTimeInMS + " ms\n");
        textArea.appendText("n =  " + irregularShapeNesting.getN() + "\n");

    }

    private void tupleNesting() {

        // Perform irregular shape nesting
        startTimer();
        TupleNesting tupleNesting = new TupleNesting(outlinePolygon);
        long elapsedTimeInMS = stopTimer();

        // Show in web view
        showInWebView(tupleNesting);

        // Display in text area
        textArea.appendText("Tuple nesting in  " + elapsedTimeInMS + " ms\n");
        textArea.appendText("n =  " + tupleNesting.getN() + "\n");

    }

    private void loadPreform() {

        // Choose file
        File csvFile = fileChooser.showOpenDialog(stage);

        // Get polygon and name
        outlinePolygon = PolygonParser.parseOutlineFromCSVFile(csvFile);
        preformName = csvFile.getName().replace(".csv", "");

        // Show in web view
        SVG.setStyle(Style.SOLID);
        showInWebView(outlinePolygon);

        // Display in text area
        textArea.appendText("Loaded " + preformName + " preform\n");

    }

    private void createButton(String text, Runnable method) throws IOException {
        Button button = new Button();
        button.setText(text);
        button.setOnAction(e -> method.run());
        vBox.getChildren().add(button);
    }

    private void showInWebView(Drawable drawable) {

        // Write to html
        String htmlFileName = preformName + ".html";
        try {
            SVG.writeToHTMLFile(drawable.toSVGElement(), htmlFileName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Show in web view
        File htmlFile = new File(
                System.getProperty("user.dir") + "/" + htmlFileName);
        webEngine.load(htmlFile.toURI().toString());
    }

    private void startTimer() {
        timerStart = System.currentTimeMillis();
    }

    private long stopTimer() {
        return System.currentTimeMillis() - timerStart;
    }
}
