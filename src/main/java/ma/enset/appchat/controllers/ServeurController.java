package ma.enset.appchat.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ServeurController implements Initializable {

    @FXML
    private Button startServer;
    @FXML
    private TextArea messageServer;
    @FXML
    private Button buttonEnvoyer;
    @FXML
    private ListView<String> listeChatServer;

    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private ObservableList<String> chatMessages;

    public void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(9090);
                Platform.runLater(() -> listeChatServer.getItems().add("Serveur démarré..."));

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                Platform.runLater(() -> listeChatServer.getItems().add("Erreur Serveur: " + e.getMessage()));
            }
        }).start();
    }

    // Envoyer un message à tous les clients
    public void broadcastMessage(String message) {
        Platform.runLater(() -> chatMessages.add(message));
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // Envoyer un message depuis l'interface
    @FXML
    private void envoyerMessage(ActionEvent event) {
        String message = messageServer.getText();
        if (!message.isEmpty()) {
            broadcastMessage("Serveur: " + message);
            messageServer.clear();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatMessages = FXCollections.observableArrayList();
        listeChatServer.setItems(chatMessages);
    }

    private static int clientCount = 1;

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private ServeurController server;
        private String clientName;

        public ClientHandler(Socket socket, ServeurController server) {
            this.socket = socket;
            this.server = server;
            this.clientName = "Client " + clientCount++;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                server.broadcastMessage("🔹 " + clientName + " a rejoint le chat !");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            writer.println(message);
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    String formattedMessage = clientName + ": " + message;
                    server.broadcastMessage(formattedMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
