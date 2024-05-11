package fr.yinxfox.debugger;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Debugger {

    private Stage mainStage;
    private Scene mainScene;
    private Canvas canvas;
    private VBox root;
    private GraphicsContext graphicsContext;

    public Debugger() {
        this.canvas = new Canvas(450, 450);
        this.root = new VBox();
        this.root.getChildren().add(this.canvas);
        this.mainScene = new Scene(this.root);
        this.mainStage = new Stage();

        setupDisplay();
    }

    public void setupDisplay() {
        this.mainStage.setResizable(false);
        this.mainStage.setTitle("Debugger");
        this.mainStage.setScene(this.mainScene);
        this.mainStage.show();

        graphicsContext = this.canvas.getGraphicsContext2D();
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

}
