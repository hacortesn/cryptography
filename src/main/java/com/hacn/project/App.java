package com.hacn.project;

import com.hacn.project.controllers.MainController;
import com.hacn.project.controllers.SpringFxmlLoader;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by familia on 10/04/2016.
 */
public class App extends Application {

    private static final double MINIMUM_WINDOW_WIDTH = 400;
    private static final double MINIMUM_WINDOW_HEIGHT = 600;


    private Stage stage;


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        AbstractApplicationContext ctx =
                new ClassPathXmlApplicationContext("classpath:com/hacn/project/controllers/app.xml");
        SpringFxmlLoader loader = new SpringFxmlLoader(ctx);

        Parent root = (Parent) loader.load("main.fxml", MainController.class);
        Scene scene = new Scene(root, 600, 600);
//        scene.getStylesheets().add("css/caspian.css");
        stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
        stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Crypto");
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                System.exit(0);
            }
        });
    }


    public Stage getStage() {
        return stage;
    }
}
