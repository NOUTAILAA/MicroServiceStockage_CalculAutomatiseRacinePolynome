package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // --- Test pour sendVerificationEmail ---
    @Test
    void testSendVerificationEmail() throws MessagingException {
        emailService.sendVerificationEmail("user@example.com", "JohnDoe");

        // Vérifier que sendEmail a bien été appelé
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        // Capturer le message et vérifier le contenu
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        assertNotNull(captor.getValue());
    }

    // --- Test pour sendLoginNotification ---
    @Test
    void testSendLoginNotification() throws MessagingException {
        emailService.sendLoginNotification("login@example.com", "JohnDoe");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // --- Test pour sendPasswordResetEmail ---
    @Test
    void testSendPasswordResetEmail() throws MessagingException {
        emailService.sendPasswordResetEmail("reset@example.com", "newPass123");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // --- Test pour s'assurer qu'une exception est levée ---
    @Test
    void testSendEmail_ThrowsException() throws MessagingException {
        doThrow(new RuntimeException("Erreur d'envoi"))
            .when(mailSender)
            .send(any(MimeMessage.class));

        Exception exception = assertThrows(
            RuntimeException.class,
            () -> emailService.sendVerificationEmail("error@example.com", "ErrorUser")
        );

        assertEquals("Erreur d'envoi", exception.getMessage());
    }

}
