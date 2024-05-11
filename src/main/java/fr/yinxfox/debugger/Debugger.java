package fr.yinxfox.debugger;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Debugger {

    private Stage stage;

    public Debugger() {
        this.stage = new Stage();
        this.stage.setResizable(false);
        this.stage.setTitle("Debugger");
        this.stage.setScene(new Scene(new VBox(), 450, 450));
        this.stage.show();
    }

}
