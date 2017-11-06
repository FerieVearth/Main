package main;

import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.stage.Screen;
import javafx.util.Duration;

public class Main extends Application {
    
    Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
    
    private double SCENE_WIDTH = primaryScreenBounds.getWidth();
    private double SCENE_HEIGHT = primaryScreenBounds.getHeight();
    
    private double scaleFactor = 1;

    static Random random = new Random();

    ResizeableCanvas canvas;
    GraphicsContext graphicsContext;

    AnimationTimer loop;

    double brushMaxSize = 15;

    Point2D mouseLocation = new Point2D( 0, 0);
    boolean mousePressed = false;
    Point2D prevMouseLocation = new Point2D( 0, 0);

    Slider slider = new Slider();

    Label label = new Label();

    Scene scene;

    Image brush = createBrush( 30.0, Color.CHOCOLATE);

    private Image[] brushVariations = new Image[256];

    ColorPicker colorPicker = new ColorPicker();

    double brushWidthHalf = brush.getWidth() / 2.0;
    double brushHeightHalf = brush.getHeight() / 2.0;

    Button button1 = new Button("ZOOM IN");
    Button button2 = new Button("ZOOM OUT");
    
    public class ResizeableCanvas extends Canvas {

        public ResizeableCanvas() {
            widthProperty().addListener(e -> draw());
            heightProperty().addListener(e -> draw());
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) {
            return getWidth();
        }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }

        public void draw() {
            /*
            double w = getWidth();
            double h = getHeight();
            GraphicsContext gc = getGraphicsContext2D();
            gc.setFill(Color.RED);
            gc.fillRect(0, 0, w, h);
            gc.setFill(Color.YELLOW);
            gc.fillRect(w/4, h/4, w/2, h/2);
            */
        }
    }
    
            
    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();
        
        canvas = new ResizeableCanvas();

        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(0, 0, SCENE_WIDTH, SCENE_HEIGHT);
        

        colorPicker.setValue(Color.CHOCOLATE);
        colorPicker.setOnAction(e -> {
            createBrushVariations();
        });

        slider.setMin(10);
        slider.setMax(50);
        slider.setValue(30);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);

        label.setText("10");

        slider.valueProperty().addListener(e -> {
            brushMaxSize = slider.getValue();
            createBrushVariations();
        });

        button1.setOnMouseClicked(e -> {
            scaleFactor += 0.1;
            canvas.setScaleX(canvas.getScaleX() * scaleFactor);
            canvas.setScaleY(canvas.getScaleY() * scaleFactor);
        });

        button2.setOnMouseClicked(e -> {
            scaleFactor -= 0.1;
            canvas.setScaleX(canvas.getScaleX() * scaleFactor);
            canvas.setScaleY(canvas.getScaleY() * scaleFactor);
        });
        
        canvas.setOnZoom(new EventHandler<ZoomEvent>() {
            @Override public void handle(ZoomEvent event) {
                event.consume();
                scaleFactor = canvas.getScaleX() * event.getZoomFactor();
                canvas.setScaleX(canvas.getScaleX() * event.getZoomFactor());
                canvas.setScaleY(canvas.getScaleY() * event.getZoomFactor());
                
            }
        });
        

        FlowPane layerPane = new FlowPane();
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        layerPane.getChildren().addAll(canvas);

        root.setTop(layerPane);
        scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.isMaximized();
        primaryStage.show();

        createBrushVariations();

        //addListeners();
        
        
        
        canvas.onTouchPressedProperty().set(new EventHandler<TouchEvent>() {
            @Override
            public void handle(final TouchEvent t) {
                mouseLocation = new Point2D(t.getTouchPoint().getSceneX(), t.getTouchPoint().getSceneY());
                mousePressed = false;
            }
        });
        
        
        canvas.onTouchMovedProperty().set(new EventHandler<TouchEvent>() {
            @Override
            public void handle(final TouchEvent t) {
                if(t.getTouchCount() == 1){
                    mouseLocation = new Point2D(t.getTouchPoint().getSceneX(), t.getTouchPoint().getSceneY());
                    mousePressed = true;
                    return;
                }
                mousePressed = false;
            }
        });
        
        canvas.onTouchReleasedProperty().set(new EventHandler<TouchEvent>() {
            @Override
            public void handle(final TouchEvent t) {
                mouseLocation = new Point2D(t.getTouchPoint().getSceneX(), t.getTouchPoint().getSceneY());
                mousePressed = false;
            }
        });
        
        
        startAnimation();

    }

    private void createBrushVariations() {
        for (int i = 0; i < brushVariations.length; i++) {

            double size = (brushMaxSize - 1) / (double) brushVariations.length * (double) i + 1;

            brushVariations[i] = createBrush(size, colorPicker.getValue());
        }

    }

    private void startAnimation() {

        loop = new AnimationTimer() {
            
            @Override
            public void handle(long now) {
                if( mousePressed) {
                    
                    System.out.println(scaleFactor);

                    // try this
                    //graphicsContext.drawImage( brush, mouseLocation.getX() - brushWidthHalf, mouseLocation.getY() - brushHeightHalf);

                    // then this
                    bresenhamLine( prevMouseLocation.getX(), prevMouseLocation.getY(), mouseLocation.getX(), mouseLocation.getY());          
                }
                prevMouseLocation = new Point2D( mouseLocation.getX(), mouseLocation.getY());
            }
        };

        loop.start();
    }

    // https://de.wikipedia.org/wiki/Bresenham-Algorithmus
    private void bresenhamLine(double x0, double y0, double x1, double y1) {
        double dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1. : -1.;
        double dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1. : -1.;
        double err = dx + dy, e2; /* error value e_xy */

        while (true) {

            int variation = (int) (brushVariations.length - 1);
            Image brushVariation = brushVariations[variation];

            //graphicsContext.setGlobalAlpha(pressure);
            graphicsContext.drawImage(brushVariation, x0 - brushVariation.getWidth() / 2.0, y0 - brushVariation.getHeight() / 2.0);
            
            if (x0 == x1 && y0 == y1){
                break;
            }
            
            e2 = 2. * err;
            if (e2 > dy) {
                err += dy;
                x0 += sx;
            } /* e_xy+e_x > 0 */
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            } /* e_xy+e_y < 0 */
            
        }
    }


    private void addListeners() {

        scene.addEventFilter(MouseEvent.ANY, e -> {

            mouseLocation = new Point2D(e.getX(), e.getY());

            mousePressed = e.isPrimaryButtonDown();

        });


    }


    public static Image createImage(Node node) {

        WritableImage wi;

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        int imageWidth = (int) node.getBoundsInLocal().getWidth();
        int imageHeight = (int) node.getBoundsInLocal().getHeight();

        wi = new WritableImage(imageWidth, imageHeight);
        node.snapshot(parameters, wi);

        return wi;

    }


    public static Image createBrush( double radius, Color color) {

        // create gradient image with given color
        Circle brush = new Circle(radius);

        RadialGradient gradient1 = new RadialGradient(0, 0, 0, 0, radius, false, CycleMethod.NO_CYCLE, new Stop(0, color.deriveColor(1, 1, 1, 0.3)), new Stop(1, color.deriveColor(1, 1, 1, 0)));

        brush.setFill(gradient1);

        // create image
        return createImage(brush);

    }


    public static void main(String[] args) {
        launch(args);
    }
}