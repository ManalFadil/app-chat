package ma.enset.appchat.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;

public class ClientController {

    @FXML
    private TextArea message;
    @FXML
    private ListView<String> listeChat;
    @FXML
    private Button buttonEnvoyer;
    @FXML
    private Button startClient;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private ObservableList<String> chatMessages;

    @FXML
    private void initialize() {
        chatMessages = FXCollections.observableArrayList();
        listeChat.setItems(chatMessages);
    }

    @FXML
    public void startClient(ActionEvent event) {
        new Thread(() -> {
            try {
                socket = new Socket("127.0.0.1", 9090);
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Platform.runLater(() -> chatMessages.add("Connecté au serveur."));

                new Thread(this::receiveMessages).start();
            } catch (IOException e) {
                Platform.runLater(() -> chatMessages.add("Erreur de connexion."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void sendMessage(ActionEvent event) {
        String messageText = message.getText();
        if (!messageText.isEmpty()) {
            writer.println(messageText); // Envoyer au serveur
            message.clear();
        }
    }


    private void receiveMessages() {
        String messageText;
        try {
            while ((messageText = reader.readLine()) != null) {
                addMessageToChat(messageText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addMessageToChat(String messageText) {
        Platform.runLater(() -> chatMessages.add(messageText));
    }
}
