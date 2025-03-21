package com.example.alanyaproject;

import java.io.*;
import java.net.Socket;

public class HandleMessage {
    private Socket socket;
    private BufferedReader in;         // Pour recevoir des messages texte
    private PrintWriter out;           // Pour envoyer des messages texte
    private DataOutputStream fileOut;  // Pour envoyer des fichiers
    private DataInputStream fileIn;    // Pour recevoir des fichiers
    private volatile boolean running;  // Contrôle de l'état du thread de réception

    // Constructeur pour initialiser la connexion avec le serveur
    public HandleMessage(String serverAddress, int port, String clientId) throws IOException {
        socket = new Socket(serverAddress, port);

        // Initialisation des flux d'entrée et sortie
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        fileOut = new DataOutputStream(socket.getOutputStream());
        fileIn = new DataInputStream(socket.getInputStream());

        //directement  Envoyer l'ID unique du client au serveur
        out.println(clientId);

        running = true; // Indique que le thread d'écoute doit fonctionner
    }

    // Méthode pour envoyer un message texte
    public void sendText(String message) {
        if (out != null) {
            out.println(message); // implementons donc la methode d'envoi des messages
        }
    }

    // Méthode pour envoyer un fichier
    public void sendFile(String filePath, String targetClientId) throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("Fichier introuvable : " + filePath);
        }

        // Envoyer la commande de transfert de fichier au serveur
        out.println("/file " + targetClientId);
        out.println(file.getName()); // Envoyer le nom du fichier

        // Envoyer les données du fichier
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
                fileOut.flush();
            }
        }
    }

    // Méthode pour écouter les messages en arrière-plan (thread séparé)
    public void listenForMessages(MessageListener listener) {
        new Thread(() -> {
            try {
                while (running) {
                    String message = in.readLine(); // Lire un message du serveur
                    if (message == null) break; // Déconnexion du serveur

                    if (message.startsWith("/file")) {
                        // Réception d'un fichier
                        String fileName = message.split(" ", 2)[1];
                        receiveFile(fileName);
                        listener.onFileReceived(fileName, "downloads/" + fileName); // Notifie le fichier reçu
                    } else {
                        // Réception d'un message texte
                        listener.onTextMessageReceived(message);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    listener.onError("Erreur lors de la réception des messages : " + e.getMessage());
                }
            } finally {
                stop(); // Fermer les connexions proprement
            }
        }).start();
    }

    // Méthode pour recevoir un fichier
    private void receiveFile(String fileName) throws IOException {
        File file = new File("downloads/" + fileName);
        file.getParentFile().mkdirs(); // Crée le répertoire "downloads" si nécessaire

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                if (bytesRead < buffer.length) break; // Fin du fichier
            }
        }
    }

    // Méthode pour arrêter la connexion et le thread d'écoute
    public void stop() {
        running = false;
        try {
            if (socket != null) socket.close();

            if (in != null) in.close();
            if (out != null) out.close();
            if (fileOut != null) fileOut.close();
            if (fileIn != null) fileIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Interface pour écouter les événements (messages texte et fichiers)
    public interface MessageListener {
        void onTextMessageReceived(String message);
        void onFileReceived(String fileName, String filePath);
        void onError(String errorMessage);
    }
}
