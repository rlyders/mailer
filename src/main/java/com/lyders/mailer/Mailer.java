package com.lyders.mailer;
/* Richard@Lyders.com created on 2/14/2021 */

import com.lyders.properties.ApplicationProperties;
import com.lyders.properties.ApplicationPropertiesConfig;
import com.lyders.properties.PropertyEvaluatorException;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mailer {

    private static final Log LOG = LogFactory.getLog(Mailer.class);

    private final ApplicationProperties appProps;

    public Mailer() throws FileNotFoundException {
        this(null, null);
    }

    public Mailer(String propertiesFile) throws FileNotFoundException {
        this(FilenameUtils.getName(propertiesFile),
                "file:" + FilenameUtils.getPath(propertiesFile));
    }

    public Mailer(String propertiesFileName, String propertiesPathTypeStrs) throws FileNotFoundException {
        ApplicationPropertiesConfig.LoadClassPathRootPropertiesAsDefaults loadClassPathRootPropertiesAsDefaults = ApplicationPropertiesConfig.LoadClassPathRootPropertiesAsDefaults.NO;
        ApplicationPropertiesConfig.LogSourceFilePathsAndProperties logSourceFilePathsAndProperties = ApplicationPropertiesConfig.LogSourceFilePathsAndProperties.NO;

        if (StringUtils.isEmpty(propertiesFileName)) {
            propertiesFileName = "mailer.conf";
            loadClassPathRootPropertiesAsDefaults = ApplicationPropertiesConfig.LoadClassPathRootPropertiesAsDefaults.YES;
        }
        if (propertiesPathTypeStrs == null) {
            propertiesPathTypeStrs = ".";
        }
        ApplicationPropertiesConfig cfg = new ApplicationPropertiesConfig(propertiesFileName, null,
                loadClassPathRootPropertiesAsDefaults,
                logSourceFilePathsAndProperties);
        appProps = new ApplicationProperties(cfg, propertiesPathTypeStrs);
    }

    public void sendEmail() throws UnsupportedEncodingException, MessagingException, PropertyEvaluatorException {
        sendEmail(null, null, null);
    }

    public void sendEmail(
            String recipientsCsv,
            String subject,
            String body) throws UnsupportedEncodingException, MessagingException, PropertyEvaluatorException {
        sendEmail(recipientsCsv, subject, body, null);
    }

    public void sendEmail(
            String recipientsCsv,
            String subject,
            String body,
            String from
    ) throws MessagingException, UnsupportedEncodingException, PropertyEvaluatorException {
        if (StringUtils.isEmpty(recipientsCsv)) {
            recipientsCsv = appProps.get("mail.to");
        }
        if (StringUtils.isEmpty(recipientsCsv)) {
            throw new IllegalArgumentException("Missing value for email TO");
        }
        if (StringUtils.isEmpty(subject)) {
            subject = appProps.get("mail.subject");
        }
        if (StringUtils.isEmpty(body)) {
            body = appProps.get("mail.body");
        }
        String smtpUser = appProps.get("mail.smtp.user");
        if (StringUtils.isEmpty(from)) {
            from = appProps.getOrDefault("mail.from", smtpUser);
        }

        Properties props = new Properties();
        String smtpHost = appProps.getOrDefault("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.host", smtpHost);
        String smtpPortStr = appProps.getOrDefault("mail.smtp.port", "25");
        try {
            int smtpPort = Integer.parseInt(smtpPortStr);
            props.put("mail.smtp.port", smtpPort);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to convert smtp.port '%s' to integer: %s", smtpPortStr, e.getMessage()));
        }
        props.put("mail.smtp.starttls.enable", appProps.get("mail.smtp.starttls.enable"));
        props.put("mail.smtp.ssl.enable", appProps.get("mail.smtp.ssl.enable"));
        String smtpAuth = appProps.get("mail.smtp.auth");
        props.put("mail.smtp.auth", smtpAuth);

        Authenticator authenticator = null;
        if ("true".equals(smtpAuth)) {
            String smtpPassword = appProps.get("mail.smtp.password");
            authenticator = new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            };
        }
        Session session = Session.getInstance(props, authenticator);

        MimeMessage message = new MimeMessage(session);
        message.addHeader("Content-Type", "text/HTML; charset=UTF-8");
        message.addHeader("Content-Transfer-Encoding", "8bit");
        message.setFrom(getInternetAddress(from));
        message.setSubject(subject, "UTF-8");
        message.setContent(body, "text/html");
        message.setSentDate(new Date());

        message.setRecipients(Message.RecipientType.TO, parseEmailCsv(recipientsCsv).toArray(InternetAddress[]::new));
        Transport.send(message);
    }

    public static void main(String[] args) throws UnsupportedEncodingException, MessagingException {

        Options options = new Options();

        Option toOption = new Option("t", "to", true, "comma-separated list of email addresses");
        toOption.setRequired(true);
        options.addOption(toOption);

        Option subjectOption = new Option("s", "subject", true, "email subject");
        subjectOption.setRequired(true);
        options.addOption(subjectOption);

        Option bodyOption = new Option("b", "body", true, "email body");
        bodyOption.setRequired(false);
        options.addOption(bodyOption);

        Option fromOption = new Option("f", "from", true, "sender email");
        fromOption.setRequired(false);
        options.addOption(fromOption);

        Option confOption = new Option("c", "conf", true, "conf/properties file path");
        confOption.setRequired(false);
        options.addOption(confOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);
            String to = cmd.getOptionValue("to");
            String subject = cmd.getOptionValue("subject");
            String body = cmd.getOptionValue("body");
            String from = cmd.getOptionValue("from");
            String conf = cmd.getOptionValue("conf");

            if (StringUtils.isEmpty(body)) {
                body = subject;
            }

            Mailer mailer = new Mailer(conf);
            mailer.sendEmail(
                    to,
                    subject,
                    body,
                    from);

        } catch (ParseException | FileNotFoundException | PropertyEvaluatorException e) {
            LOG.error(e);
            formatter.printHelp("java -jar mailer.jar", options);

            System.exit(1);
        }

    }

    public static List<Address> parseEmailCsv(String emailCsv) throws UnsupportedEncodingException {
        ArrayList<Address> addressList = new ArrayList<>();
        String[] emailAddressStrs = emailCsv.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String emailAddress : emailAddressStrs) {
            addressList.add(getInternetAddress(emailAddress));
        }
        return addressList;
    }

    private static Address getInternetAddress(String emailNameAndAddressStr) throws UnsupportedEncodingException {
        Pattern p = Pattern.compile("^(.*?)<?([^<> \"]+)>?$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(emailNameAndAddressStr);
        if (!m.find()) {
            throw new IllegalArgumentException(String.format("Failed to parse email name and address: %s", emailNameAndAddressStr));
        }
        String name = m.group(1).trim().replace("\"", "");
        String emailAddress = m.group(2);
        return new InternetAddress(emailAddress, name);
    }

}
