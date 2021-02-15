package com.lyders.mailer;

/* Richard@Lyders.com created on 2/14/2021 */

import com.lyders.properties.ApplicationProperties;
import com.lyders.properties.ApplicationPropertiesConfig;
import com.lyders.properties.PropertyEvaluatorException;
import org.junit.Ignore;
import org.junit.Test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import static com.lyders.mailer.Mailer.parseEmailCsv;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MailerTests {

    @Test
    @Ignore("manual test, not a unit test")
    public void sendEmailTest() throws UnsupportedEncodingException, MessagingException, FileNotFoundException, PropertyEvaluatorException {
        String recipients = "Joe Blow<Joe.Blow@test.com>,Bill Blass<Bill@Blass.com>";
        String subject = "test";
        String body = "test";

        Mailer mailer = new Mailer();
        assertNotNull(mailer);
        mailer.sendEmail(
                recipients,
                subject,
                body);
    }

    @Test
    public void findEmailsTest() throws UnsupportedEncodingException {
        List<Address> addressArrayList = parseEmailCsv("bob@home.com, \"Jane\" <jane@home.com>, \"Smith, Mr\" <smith@home.com>");
        assertNotNull(addressArrayList);
        assertEquals(3, addressArrayList.size());
    }

    @Test
    @Ignore("manual test, not a unit test")
    public void propertiesTest() throws FileNotFoundException {
        ApplicationProperties properties = getTestAppProps();
        assertNotNull(properties);
        properties.printAllSourcesAndProperties(System.out::println);
    }

    public ApplicationProperties getTestAppProps() throws FileNotFoundException {
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig("mailer.conf", null,
                ApplicationPropertiesConfig.LoadClassPathRootPropertiesAsDefaults.YES,
                ApplicationPropertiesConfig.LogSourceFilePathsAndProperties.YES);
        return new ApplicationProperties(cfg, "conf");
    }

    @Test
    @Ignore("manual test, not a unit test")
    public void testEmail() throws FileNotFoundException, PropertyEvaluatorException {
        ApplicationProperties appProps = getTestAppProps();
        // Recipient's email ID needs to be mentioned.
        String to = appProps.get("mail.smtp.user");

        // Sender's email ID needs to be mentioned
        String from = appProps.get("mail.from");

        // Assuming you are sending email from through gmails smtp
        String host = appProps.get("mail.smtp.host");

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", appProps.get("mail.smtp.port"));
        properties.put("mail.smtp.ssl.enable", appProps.get("mail.smtp.ssl.enable"));
        properties.put("mail.smtp.auth", appProps.get("mail.smtp.auth"));
        properties.put("mail.smtp.starttls.enable", appProps.get("mail.smtp.starttls.enable"));

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                try {
                    return new PasswordAuthentication(appProps.get("mail.smtp.user"), appProps.get("mail.smtp.password"));
                } catch (PropertyEvaluatorException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        });

        // Used to debug SMTP issues
        session.setDebug(true);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            assertNotNull(message);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("This is the Subject Line!");

            // Now set the actual message
            message.setText("This is actual message");

            System.out.println("sending...");
            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

    }

    @Test
    @Ignore("manual test, not a unit test")
    public void sendEmailTestWithPropFile() throws UnsupportedEncodingException, MessagingException, FileNotFoundException, PropertyEvaluatorException {
        String recipients = "Joe Blow<Joe.Blow@test.com>,Bill Blass<Bill@Blass.com>";
        String subject = "test";
        String body = "test";
        String propFile = "./mymailer.conf";

        Mailer mailer = new Mailer(propFile);
        assertNotNull(mailer);
        mailer.sendEmail(
                recipients,
                subject,
                body);
    }

    @Test
    @Ignore("manual test, not a unit test")
    public void sendEmailTestWithPropFileNoParams() throws UnsupportedEncodingException, MessagingException, FileNotFoundException, PropertyEvaluatorException {
        String propFile = "./mymailer.conf";

        Mailer mailer = new Mailer(propFile);
        assertNotNull(mailer);
        mailer.sendEmail();
    }

}
