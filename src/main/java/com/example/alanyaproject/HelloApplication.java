package com.example.alanyaproject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HelloApplication extends Application {

    private HandleMessage handleMessage;

    {
        try {
            handleMessage = new HandleMessage("127.0.0.1",5000,"AZANGUE");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ; // Instance de HandleMessage
    private ListView<String> contactsList;
    private List<String> contacts = new ArrayList<>(); // Liste interne des contacts
    private Map<String, List<Message>> conversations = new HashMap<>(); // Gère les conversations de chaque contact
    private VBox messagesContainer;
    private Label contactNameLabel;
    private BorderPane chatRoot;

    //handleMessage = new HandleMessage("127.0.0.1",5000,"AZANGUE");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ALANYA");

        // --- Assemblage principal ---
        chatRoot = new BorderPane();

        // --- Liste des contacts (gauche) ---
        VBox contactsPane = new VBox();
        contactsPane.setPadding(new Insets(10));
        contactsPane.setSpacing(10);
        contactsPane.setPrefWidth(350);
        //contactsPane.setStyle("-fx-background-color: #F5F5F5;");
        contactsPane.setStyle(
                "-fx-background-radius: 15;" +             // Rayon pour l'arrière-plan
                "-fx-border-color: #cccccc;" +             // Couleur de la bordure
                "-fx-border-width: 1;" +                   // Épaisseur de la bordure
                "-fx-border-radius: 15;" +                 // Rayon pour la bordure
                "-fx-padding: 10;"
               );


        TextField searchBar = new TextField();
        //searchBar.setPromptText("Rechercher un contact...");
        //searchBar.setStyle("-fx-font-size: 17px;");
        searchBar.setStyle(
                "-fx-background-radius: 15;" +             // Rayon pour l'arrière-plan
                "-fx-border-color: #cccccc;" +             // Couleur de la bordure
                "-fx-border-width: 1;" +                   // Épaisseur de la bordure
                "-fx-border-radius: 15;" +                 // Rayon pour la bordure
                "-fx-padding: 10;"             );
        searchBar.setPromptText("Rechercher un contact...");
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            contactsList.getItems().clear();
            contacts.stream()
                    .filter(contact -> contact.toLowerCase().contains(newValue.toLowerCase()))
                    .forEach(filteredContact -> contactsList.getItems().add(filteredContact));
            if (newValue.isEmpty()) refreshContactsList();
        });

        contactsList = new ListView<>();
        contactsList.setStyle("-fx-font-size: 16px;"
                            + "-fx-border-color: #cccccc;" +             // Couleur de la bordure
                "-fx-border-width: 1;" +                   // Épaisseur de la bordure
                "-fx-border-radius: 15;" +                 // Rayon pour la bordure
                "-fx-padding: 10;");
        contactsList.setPrefHeight(500);

        contactsList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String contact, boolean empty) {
                super.updateItem(contact, empty);
                if (empty || contact == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellContent = new HBox(10);
                    cellContent.setAlignment(Pos.CENTER_LEFT);
                    cellContent.setStyle(
                            "-fx-background-color : #E8F0F2;"+
                           "-fx-background-radius: 18;" +             // Rayon pour l'arrière-plan
                                    "-fx-border-color: #cccccc;" +             // Couleur de la bordure
                                    "-fx-border-width: 1;" +                   // Épaisseur de la bordure
                                    "-fx-border-radius: 15;" +                 // Rayon pour la bordure
                                    "-fx-padding: 10;"
                    );

                    // Création de l'icône avec son cercle coloré
                    ImageView contactIcon = new ImageView(new Image(getClass().getResource("/icons/user.png").toExternalForm()));
                    contactIcon.setFitWidth(24);
                    contactIcon.setFitHeight(24);
                    Circle clip = new Circle(12, 12, 12);
                    contactIcon.setClip(clip);

                    Circle borderCircle = new Circle(12, 12, 18);
                    //borderCircle.setStroke(Color.BLUE);
                    borderCircle.setStrokeWidth(2);
                    borderCircle.setFill(Color.web("#87CEEB")); // Bleu ciel

                    StackPane iconContainer = new StackPane(borderCircle, contactIcon);


                    Label contactLabel = new Label(contact);
                    contactLabel.setStyle("-fx-font-size: 18px;");

                    cellContent.getChildren().addAll(iconContainer, contactLabel);
                    setGraphic(cellContent);
                    setStyle("-fx-background-color: #E8F0F2; -fx-text-fill: white;");
                }
            }
        });




        // Exemple de contacts initiaux
        contacts.addAll(List.of("AZANGUE", "BALOCK MON FRERE", "AGATZE", "ARREYNTOW", "KEVINE", "YVES", "BORIS", "SAMANTA","DEMANOU","TOULEPI","CHAPET","PETER PARKER", "Irving"));
        refreshContactsList();

        contactsPane.getChildren().addAll(searchBar, contactsList);

        contactsList.setOnMouseClicked((MouseEvent event) -> {
            String selectedContact = contactsList.getSelectionModel().getSelectedItem();
            if (selectedContact != null) {
                contactNameLabel.setText(selectedContact);
                loadConversation(selectedContact);
            }
        });

        // --- Zone de conversation (centre) ---
        BorderPane conversationPane = new BorderPane();
        conversationPane.setStyle("-fx-background-color: #FFFFFF;");

        // En-tête de la conversation
        HBox headerPane = new HBox();
        headerPane.setPadding(new Insets(10));
        headerPane.setSpacing(15);
        headerPane.setAlignment(Pos.CENTER_LEFT);
        headerPane.setStyle("-fx-background-color: #34B7F1;" +
                "-fx-background-radius: 15;" +             // Rayon pour l'arrière-plan
                "-fx-border-color: #cccccc;" +             // Couleur de la bordure
                "-fx-border-width: 1;" +                   // Épaisseur de la bordure
                "-fx-border-radius: 15;" +                 // Rayon pour la bordure
                "-fx-padding: 10;"             );

        //
        // Création de l'icône avec son cercle coloré
        ImageView contactIcon1 = new ImageView(new Image(getClass().getResource("/icons/user.png").toExternalForm()));
        contactIcon1.setFitWidth(24);
        contactIcon1.setFitHeight(24);
        Circle clip1 = new Circle(12, 12, 14);
        contactIcon1.setClip(clip1);

        Circle borderCircle = new Circle(12, 12, 18);
        //borderCircle.setStroke(Color.BLUE);
        borderCircle.setStrokeWidth(2);
        borderCircle.setFill(Color.web("#87CEEB")); // Bleu ciel

        StackPane iconContainer = new StackPane(borderCircle, contactIcon1);


        // Bouton pour cacher/afficher les contacts
        Button toggleContacts = new Button("☰");
        toggleContacts.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-text-fill: white;");
        toggleContacts.setOnAction(e -> {
            if (chatRoot.getLeft() == null) {
                chatRoot.setLeft(contactsPane);
            } else {
                chatRoot.setLeft(null);
            }
        });

        contactNameLabel = new Label("Sélectionnez un contact");
        contactNameLabel.setFont(new Font("Arial", 18));
        contactNameLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Deux icônes à l'extrême droite : appel vidéo et appel audio
        Button videoCallButton = createIconButton("/icons/camera.png", 24, 24);
        Button audioCallButton = createIconButton("/icons/phone-call.png", 24, 24);

        headerPane.getChildren().addAll(toggleContacts,iconContainer, contactNameLabel, spacer, videoCallButton, audioCallButton);
        conversationPane.setTop(headerPane);

        // Conteneur pour afficher les messages
        messagesContainer = new VBox();
        messagesContainer.setPadding(new Insets(10));
        messagesContainer.setSpacing(10);

        ScrollPane messagesScrollPane = new ScrollPane(messagesContainer);
        messagesScrollPane.setFitToWidth(true);
        conversationPane.setCenter(messagesScrollPane);

        // Zone d'entrée des messages (bas)
        HBox inputPane = new HBox();
        inputPane.setPadding(new Insets(10));
        inputPane.setSpacing(10);
        inputPane.setAlignment(Pos.CENTER);
        inputPane.setStyle("-fx-background-color: #34B7F1;" +
                "-fx-background-radius: 15;" +             // Rayon pour l'arrière-plan
                "-fx-border-color: #cccccc;" +             // Couleur de la bordure
                "-fx-border-width: 1;" +                   // Épaisseur de la bordure
                "-fx-border-radius: 15;" +                 // Rayon pour la bordure
                "-fx-padding: 10;"             );


        // Bouton pour envoyer des fichiers
        Button attachButton = createIconButton("/icons/plus.png", 24, 24);
        attachButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir un fichier");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                addMessage(contactNameLabel.getText(), "Fichier envoyé : " + file.getName(), false);
            }
        });

        // Zone de texte pour la saisie des messages
        TextField messageInput = new TextField();
        messageInput.setPromptText("Entrez votre message...");
        messageInput.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 15;" +             // Rayon pour l'arrière-plan
                "-fx-border-color: #cccccc;" +             // Couleur de la bordure
                "-fx-border-width: 1;" +                   // Épaisseur de la bordure
                "-fx-border-radius: 15;" +                 // Rayon pour la bordure
                "-fx-padding: 10;"             );

        HBox.setHgrow(messageInput, Priority.ALWAYS);

        handleMessage.listenForMessages(new HandleMessage.MessageListener() {
            @Override
            public void onTextMessageReceived(String message) {
                Platform.runLater(() -> {
                    // Utilisez le contact actuellement sélectionné ou "Serveur" par défaut
                    String contact = contactNameLabel.getText().equals("Sélectionnez un contact")
                            ? "Serveur"
                            : contactNameLabel.getText();

                    addMessage(contact, message, true); // Ajoute et affiche le message reçu
                });
            }

            @Override
            public void onFileReceived(String fileName, String filePath) {
                Platform.runLater(() -> {
                    // Notifier visuellement la réception d'un fichier (si nécessaire)
                    String contact = contactNameLabel.getText().equals("Sélectionnez un contact")
                            ? "Serveur"
                            : contactNameLabel.getText();

                    addMessage(contact, "Fichier reçu : " + fileName, true); // Notifie le fichier reçu
                });
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    // Affiche une erreur dans l'interface ou dans une boîte de dialogue
                    System.err.println("Erreur : " + errorMessage);
                });
            }
        });



        // Bouton pour envoyer le message texte
        Button sendButton = createIconButton("/icons/paper-plane.png", 24, 24);
        sendButton.setOnAction(e -> {
            String messageText = messageInput.getText().trim();
            String selectedContact = contactNameLabel.getText();
            if (!messageText.isEmpty() && !contactNameLabel.getText().equals("Sélectionnez un contact")) {

                String formattedMessage = "/msg "+selectedContact+" "+messageText;
                handleMessage.sendText(formattedMessage); // Envoie le message au serveur

                Platform.runLater(() -> addMessage(contactNameLabel.getText(), messageText, false));

                messageInput.clear();
            }



        });
        sendButton.setStyle("-fx-background-color: white; "+
                "                -fx-background-radius: 15;"+             // Rayon pour l'arrière-plan\n" +
                "                -fx-border-color: #cccccc; "+             // Couleur de la bordure\n" +
                "                -fx-border-width: 1;" +                   // Épaisseur de la bordure\n" +
                "                -fx-border-radius: 15;" +                 // Rayon pour la bordure\n" +
                "                -fx-padding: 10;");



        // Bouton pour envoyer un message audio
        Button voiceButton = createIconButton("/icons/microphone.png", 24, 24);
        voiceButton.setStyle("-fx-background-color: white; "+
                "                -fx-background-radius: 15;"+             // Rayon pour l'arrière-plan\n" +
                "                -fx-border-color: #cccccc; "+             // Couleur de la bordure\n" +
                "                -fx-border-width: 1;" +                   // Épaisseur de la bordure\n" +
                "                -fx-border-radius: 15;" +                 // Rayon pour la bordure\n" +
                "                -fx-padding: 10;");



        inputPane.getChildren().addAll(attachButton, messageInput, sendButton, voiceButton);
        conversationPane.setBottom(inputPane);

        // Assemblage de l'interface principale
        chatRoot.setLeft(contactsPane);
        chatRoot.setCenter(conversationPane);

        Scene chatScene = new Scene(chatRoot, 1000, 600);

        String css = """
            .scroll-bar:vertical {
                -fx-pref-width: 1px;
            }
            .scroll-bar .thumb {
                -fx-background-color: #888;
                -fx-background-radius: 1px;
            }
            .scroll-bar .thumb:hover {
                -fx-background-color: #555;
            }
        """;
        chatScene.getStylesheets().add("data:text/css," + css);

        primaryStage.setScene(chatScene);

        primaryStage.show();
    }

    // Permet de rafraîchir la liste des contacts affichés
    private void refreshContactsList() {
        contactsList.getItems().setAll(contacts);
    }

    // Charge l'ensemble des messages d'une conversation pour un contact
    private void loadConversation(String contact) {
        messagesContainer.getChildren().clear();
        List<Message> conversation = conversations.getOrDefault(contact, new ArrayList<>());
        for (Message msg : conversation) {
            displayMessage(msg.getSender(), msg.getContent(), msg.isReceived());
        }
    }

    // Ajoute un message à la conversation et réordonne le contact pour le mettre en haut
    private void addMessage(String contact, String message, boolean isReceived) {
        //ajouter le message a la liste de conversation
        conversations.putIfAbsent(contact, new ArrayList<>());
        conversations.get(contact).add(new Message(contact, message, isReceived));
        //afficher visuellement le message dans l'interface
        displayMessage(contact, message, isReceived);

        // Si le message est envoyé (non reçu), remonter le contact en tête de liste
        if (!isReceived) {
            contacts.remove(contact);//deplacer ce contact en hat de la liste
            contacts.add(0, contact);
            refreshContactsList();
        }
    }

    // Affiche le message dans une "bulle" avec la date et l'heure
    private void displayMessage(String sender, String message, boolean isReceived) {
        //ajouter un horodatage
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);//permet de couper le text
        messageLabel.setMaxWidth(300);//largeur maximal de la bulle
        //ajouter l'horodatage
        Label timeLabel = new Label(timeStamp);
        timeLabel.setFont(new Font(10));
        timeLabel.setTextFill(Color.GRAY);

        //conteneur pour le message
        VBox messageBubble = new VBox(messageLabel, timeLabel);
        messageBubble.setSpacing(5);
        messageBubble.setPadding(new Insets(8));
        //differencier les styles en fonction de l'envoi ou de la reception
        if (!isReceived) {
            messageBubble.setStyle("-fx-background-color: #DCF8C6; -fx-background-radius: 10;");
        } else {
            messageBubble.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #DCDCDC; -fx-border-radius: 10;");
        }
        //ajouter la bulle a gauche ou a droite
        HBox bubbleWrapper = new HBox(messageBubble);
        bubbleWrapper.setAlignment(isReceived ? Pos.TOP_LEFT : Pos.TOP_RIGHT);

        messagesContainer.getChildren().add(bubbleWrapper);
    }

    // Méthode utilitaire pour créer un bouton avec une icône
    private Button createIconButton(String iconPath, double width, double height) {
        Button button = new Button();
        try {
            ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
            icon.setFitWidth(width);
            icon.setFitHeight(height);
            button.setGraphic(icon);
            button.setStyle("-fx-background-color: transparent; -fx-padding: 5;");
        } catch (Exception e) {
            System.err.println("Erreur de chargement de l'icône : " + iconPath);
        }
        return button;
    }

    // Classe interne représentant un message
    private static class Message {
        private final String sender;
        private final String content;
        private final boolean isReceived;

        public Message(String sender, String content, boolean isReceived) {
            this.sender = sender;
            this.content = content;
            this.isReceived = isReceived;
        }

        public String getSender() {
            return sender;
        }

        public String getContent() {
            return content;
        }

        public boolean isReceived() {
            return isReceived;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
