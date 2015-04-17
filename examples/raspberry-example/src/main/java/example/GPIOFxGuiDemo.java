package example;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;


public class GPIOFxGuiDemo extends Application{

    public static void main(String args[]){
        System.out.println("main...");
            launch(args);
    }

    @Override
    public void start(Stage stage){
        System.out.println("start...");
            Button btn = new Button(">> Click <<");
            btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle( ActionEvent arg0 ) {
                    System.out.println("Hello JavaFX 8");
                }
            });
            StackPane root = new StackPane();
            root.getChildren().add(btn);
            stage.setScene(new Scene(root));
            stage.setWidth(300);
            stage.setHeight(300);
            stage.setTitle("JavaFX 8 app");
            System.out.println("Show app...");
            stage.show();
    }

}

