package com.hacn.project.controllers;

import com.hacn.project.util.DesktopApi;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;

/**
 * Created by familia on 10/04/2016.
 */
public class MainController implements MessageListener {


    @Autowired
    RabbitTemplate amqpTemplate;
    File file;
    private final static String ID = new BigInteger(130, new SecureRandom()).toString(32);

    GridPane gridPane;
    Button buttonViewFile;
    int sizeRows = 0;

    private static final String PATH_FILES = "files" + File.separator;

    private Desktop desktop;
    private Label labelViewFile;


    public MainController() {
        if (Desktop.isDesktopSupported())
            desktop = Desktop.getDesktop();

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
                        .setHeader("ID", ID)
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
            if (Desktop.isDesktopSupported())
                desktop.open(file);
            else
                DesktopApi.browse(new URI(file.getAbsolutePath()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void init(Button buttonViewFile, Label labelViewFile, GridPane gridPane) {
        if (this.buttonViewFile == null)
            this.buttonViewFile = buttonViewFile;

        if (this.labelViewFile == null)
            this.labelViewFile = labelViewFile;

        if (this.gridPane == null) {
            this.gridPane = gridPane;
            cleanGrid();
        }

    }

    private void cleanGrid() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                List<RowConstraints> rowConstraintses = gridPane.getRowConstraints();
                gridPane.getRowConstraints().removeAll(rowConstraintses);
            }
        });
    }

    public void setAmqpTemplate(RabbitTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void onMessage(final Message message) {

        String _ID = message.getMessageProperties().getHeaders().get("ID").toString();
        if (!_ID.equals(ID)) {
            final String fileName = message.getMessageProperties().getHeaders().get("file_name").toString();
            listenFiles(message.getBody(), fileName);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    //int size = gridPane.getRowConstraints().size();

                    gridPane.addRow(sizeRows);
                    gridPane.add(new Label("archivo: " + fileName), 0, sizeRows);
                    Button node = new Button("Ver archivo");
                    node.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            openFile(new File(PATH_FILES + fileName));

                        }
                    });
                    gridPane.add(node, 1, sizeRows);
                    sizeRows++;
                    //labelViewFile.setText("archivo: " + fileName + "\n cripto : 3DES");
                    //buttonViewFile.setText("ver archivo " + fileName);
                }
            });

            /*buttonViewFile.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    openFile(new File(PATH_FILES + fileName));

                }
            });*/
        }
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
