package nesting.application;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import nesting.geometry.elements.Point;
import nesting.packing.CirclePacking;
import nesting.svg.SVG;
import nesting.svg.SVG.StrokeWidth;
import nesting.svg.SVGElement;

/**
 * Tests our circle packing algorithm against optimal solutions.
 */
public class CirclesInCircle {

    /**
     * The radii of the containing circle for the first couple of optimal solutions.
     */
    public final static double[] radiiOptimalCases = { 1, 2,
            1 + 2 / Math.sqrt(3), 1 + Math.sqrt(2),
            1 + Math.sqrt(2 * (1 + 1 / Math.sqrt(5))), 3, 3,
            1 + 1 / Math.sin(Math.PI / 7),
            1 + Math.sqrt(2 * (2 + Math.sqrt(2))), 3.813,
            1 + 1 / Math.sin(Math.PI / 9), 4.029, 2 + Math.sqrt(5), 4.328,
            4.521, 4.615, 4.792, 4.863, 4.863, 5.122,
            1 / 0.190392146849053511968808973057,
            1 / 0.183833026581681677264411480513,
            1 / 0.180336009254436500349252193252,
            1 / 0.176939130595961682695732682453,
            1 / 0.173827661421222233359259594413,
            1 / 0.171580252187166848283936226172,
            1 / 0.169307931134573046568970317658,
            1 / 0.166252750038606935814942389058,
            1 / 0.162903649276644353945069772319,
            1 / 0.161349109064689865167358846536,
            1 / 0.158944541560340043740860426196,
            1 / 0.155533985422770861614341265770,
            1 / 0.154161517947058164731937622494,
            1 / 0.151264028246755464100569487881,
            1 / 0.149316776635116022601721177537 };
    static String imagePath = "C:\\Users\\Markus Säbel\\Dropbox\\MASTER OF SCIENCE\\Abschlussmodul\\Nesting-Saebel\\Latex\\images\\";

    /**
     * The radii of the optimal solutions are multiplied by this factor. By varying this factors, you
     * can estimate the quality of the packing heuristic.
     */
    public static double rFactor = 1.01;

    public static double scale = 100;

    private CirclesInCircle() {
    };

    /**
     * Executes the simulation.
     * 
     * @param args (not used)
     * @throws IOException if something goes wrong with writing into the output file
     */
    public static void main(String[] args) throws IOException {
        runAllCases();
//        runImages();
    }

    // Runs all cases for which optimal solutions are given here:
    // https://erich-friedman.github.io/packing/cirincir/
    private static void runAllCases() throws IOException {
        BufferedWriter writer = SVG.openHTMLFile("CirclesInCircle.html");
        SVG.setStrokeWidth(new StrokeWidth(0.1, "%"));
        double total_n = 0;

        for (int i = 0; i < radiiOptimalCases.length; i++) {
            double radius = radiiOptimalCases[i] * scale * rFactor;
            CirclePacking circlePacking = new CirclePacking(radius, scale);
            List<Point> circleCenters = circlePacking.getCircleCenters();

            SVGElement svg = circlePacking.toSVGElement();
            SVG.openSVGViewBox(writer, svg.bounds);
            writer.write(svg.string);
            SVG.caption(writer,
                    "n = " + circleCenters.size() + " / " + (i + 1));
            total_n += circleCenters.size();
        }

        SVG.caption(writer, "n_average = "
                + (total_n / radiiOptimalCases.length) + " / 18");
        SVG.closeHTMLFile(writer);
    }

    // Generates images for the first nine cases (see thesis, p. 18)
    private static void runImages() throws IOException {
        SVG.setPath(imagePath);
        SVG.setStrokeWidth(new StrokeWidth(0.3, "%"));

        for (int i = 0; i < 9; i++) {
            double radius = radiiOptimalCases[i] * scale * rFactor;
            CirclePacking circlePacking = new CirclePacking(radius, scale);
            BufferedWriter writer = SVG
                    .openSVGFile("ccc" + (i + 1) + "image.svg");
            SVGElement svg = circlePacking.toSVGElement();
            SVG.openSVGViewBox(writer, svg.bounds);
            writer.write(svg.string);
            SVG.closeSVGViewBox(writer);
            SVG.closeSVGFile(writer);
        }
    }
}
