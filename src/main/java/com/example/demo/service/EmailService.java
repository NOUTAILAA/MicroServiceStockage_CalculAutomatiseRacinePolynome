package com.example.demo.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Fonction pour envoyer un email avec du contenu HTML
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);  // 'true' pour activer le contenu HTML
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // 'true' pour envoyer le message en HTML
        mailSender.send(message);
    }

    // Envoi d'un email de vérification
    public void sendVerificationEmail(String email, String username) throws MessagingException {
        String subject = "Vérification de votre adresse e-mail";
        String body = "Bonjour " + username + ",<br><br>" +
                      "Veuillez vérifier votre adresse e-mail en cliquant sur le lien suivant :<br>" +
                      "<a href=\"http://localhost:8082/api/users/verify?email=" + email + "\">OK</a>";
        sendEmail(email, subject, body);
    }

    // Autres fonctions (login, réinitialisation du mot de passe, etc.)
    public void sendLoginNotification(String email, String username) throws MessagingException {
        String subject = "Notification de Connexion";
        String body = "Bonjour " + username + ",<br><br>Vous vous êtes connecté avec succès.";
        sendEmail(email, subject, body);
    }

    public void sendPasswordResetEmail(String email, String newPassword) throws MessagingException {
        String subject = "Réinitialisation de votre mot de passe";
        String body = "Votre mot de passe a été réinitialisé. Voici votre nouveau mot de passe : " + newPassword;
        sendEmail(email, subject, body);
    }
}
