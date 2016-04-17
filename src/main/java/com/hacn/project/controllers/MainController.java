package com.hacn.project.controllers;

import com.hacn.project.logic.Crypto;
import com.hacn.project.logic.ECCSignature;
import com.hacn.project.logic.Simetric3DES;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

/**
 * Created by familia on 10/04/2016.
 */
public class MainController implements MessageListener {


    @Autowired
    RabbitTemplate amqpTemplate;
    File file;
    private final static String ID = new BigInteger(130, new SecureRandom()).toString(32);
    private String cipher = "";

    final static String TRIPLE_DES_CIPHER = "3DES";
    final static String ECC_CIPHER = "ECC";

    GridPane gridPane;
    Button buttonViewFile;
    int sizeRows = 0;

    private static final String PATH_FILES = "files" + File.separator;

    private Desktop desktop;
    private Label labelViewFile;
    private long lasTimeDecrypt = 0;


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

        if (file != null && !cipher.equals(""))
            try {
                MessageProperties props = MessagePropertiesBuilder.newInstance()
                        .setHeader("file_name", file.getName().replaceAll(" ", "-"))
                        .setHeader("ID", ID)
                        .setHeader("CIPHER", cipher)
                        .build();
                byte[] body = Files.readAllBytes(Paths.get(file.toURI()));

                //System.out.println(new String(body));


                Crypto crypto = crypto(cipher);
                byte[] encode = crypto.encrypt(body);
                System.out.println("segundos despercidiados haciendo la codificación " + crypto.getTime() / 1000);
                //System.out.println(new String(Simetric3DES.decrypt(encode)));

                //System.out.println(new String(Simetric3DES.decrypt(body)));


                Message message = MessageBuilder.withBody(encode)
                        .andProperties(props)
                        .build();
                amqpTemplate.convertAndSend(message);
                labelInfo.setText("El archivo:\ncifrado en " + crypto.getTime() + " milisegundos y  enviado ");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        else {
            labelInfo.setText("El archivo no puede ser vacío y/o nose ha escogido un algoritmo");
        }

    }

    //@FXML
    public String listenFiles(byte[] bytes, String fileName, String cipher) {

        FileOutputStream trueFile = null;
        FileOutputStream receivedFile = null;
        byte[] bytesDecode;
        byte[] clone = bytes.clone();
        Random rnd = new Random();
        int i = Math.abs(rnd.nextInt());
        String trueFileName = PATH_FILES + cipher + "_" + i + fileName;
        try {

            String receivedFileName = PATH_FILES + "received" + File.separator + cipher + "_" + i + fileName + "." + cipher;

            if (!new File("files" + File.separator + "received").exists()) {
                new File("files" + File.separator + "received").mkdirs();
            }
            receivedFile = new FileOutputStream(receivedFileName);
            receivedFile.write(clone);

            Crypto crypto = crypto(cipher);
            bytesDecode = crypto.decrypt(bytes);
            lasTimeDecrypt = crypto.getTime();
            trueFile = new FileOutputStream(trueFileName);
            trueFile.write(bytesDecode);


        } catch (Exception e) {
            e.printStackTrace();
            trueFileName = "";
        } finally {
            if (trueFile != null)
                try {
                    trueFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            if (receivedFile != null)
                try {
                    receivedFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return trueFileName;
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
    public void init(GridPane gridPane) {
        if (this.buttonViewFile == null)
            this.buttonViewFile = buttonViewFile;

        if (this.labelViewFile == null)
            this.labelViewFile = labelViewFile;

        if (this.gridPane == null) {
            this.gridPane = gridPane;
            cleanGrid();
        }

    }

    private Crypto crypto(String cipher) throws Exception {
        switch (cipher) {
            case ECC_CIPHER:
                return new ECCSignature();
            case TRIPLE_DES_CIPHER:
                return new Simetric3DES();

        }
        return null;
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
        String cipher = message.getMessageProperties().getHeaders().get("CIPHER").toString();
        if (!_ID.equals(ID)) {
            final String fileName = message.getMessageProperties().getHeaders().get("file_name").toString();
            final String realName = listenFiles(message.getBody(), fileName, cipher);
            if (!realName.equals(""))
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        //int size = gridPane.getRowConstraints().size();

                        gridPane.addRow(sizeRows);
                        Label node1 = new Label("descifrado:" + lasTimeDecrypt +
                                "milisegundos \narchivo: " + realName);
                        node1.setMinHeight(50);
                        gridPane.add(node1, 0, sizeRows);
                        Button node = new Button("Ver archivo");
                        node.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                openFile(new File(realName));

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

    @FXML
    public void setCrypto(Object val) {
        System.out.println(val);
        cipher = val.toString();
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
