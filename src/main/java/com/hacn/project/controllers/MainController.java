package com.hacn.project.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * Created by familia on 10/04/2016.
 */
public class MainController implements Initializable, MessageListener {


    @Autowired
    RabbitTemplate amqpTemplate;
    File file;

    GridPane gridPane;
    Button buttonViewFile;

    private static final String PATH_FILES = "files" + File.separator;


    private Desktop desktop = Desktop.getDesktop();
    private Label labelViewFile;

    public void listen(byte[] bytes) {
        System.out.println(new String(bytes));

    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("opsss");


    }

    @FXML
    public void fileChooser(Label label) {
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            label.setText(file.getName());
            this.file = file;
            //sendFile(file);
            //openFile(file);
        }

    }

    @FXML
    public void sendFile(Label labelInfo) {
        labelInfo.setText("");

        if (file != null)
            try {
                labelInfo.setText("El archivo fue enviado");
                MessageProperties props = MessagePropertiesBuilder.newInstance()
                        .setHeader("file_name", file.getName())
                        .build();
                Message message = MessageBuilder.withBody(Files.readAllBytes(Paths.get(file.toURI())))
                        .andProperties(props)
                        .build();
                amqpTemplate.convertAndSend(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        else {
            labelInfo.setText("El archivo no puede ser nulo");
        }

    }

    //@FXML
    public void listenFiles(byte[] bytes, String fileName) {
        FileOutputStream fileOutputStream = null;
        try {
            if (!new File("files").exists()) {
                new File("files").mkdir();
            }


            fileOutputStream = new FileOutputStream(PATH_FILES + fileName);
            fileOutputStream.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private void openFile(File file) {
        try {
            desktop.open(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void init(Button buttonViewFile, Label labelViewFile) {
        if (this.buttonViewFile == null)
            this.buttonViewFile = buttonViewFile;

        if (this.labelViewFile == null)
            this.labelViewFile = labelViewFile;


    }

    public void setAmqpTemplate(RabbitTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void onMessage(final Message message) {

        final String fileName = message.getMessageProperties().getHeaders().get("file_name").toString();
        listenFiles(message.getBody(), fileName);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                labelViewFile.setText("archivo: " + fileName + "\n cripto : 3DES");
                //buttonViewFile.setText("ver archivo " + fileName);
            }
        });

        buttonViewFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openFile(new File(PATH_FILES + fileName));

            }
        });
    }

    /*MenuItem cmItem2 = new MenuItem("Save Image");
    cmItem2.setOnAction(new EventHandler<ActionEvent>() {
        public void handle(ActionEvent e) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            System.out.println(pic.getId());
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(pic.getImage(),
                            null), "png", file);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }
    );*/
}
