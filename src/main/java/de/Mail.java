package de;

import javax.mail.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Mail {

    public Mail(String saveDirectory, String host, String port, String userName, String password) {
        this.saveDirectory = saveDirectory + "/";
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    private String saveDirectory;
    private String host;
    private String port;
    private String userName;
    private String password;

    public void getMails() {
        Properties properties = new Properties();

        // server setting
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);

        // SSL setting
        properties.setProperty("mail.pop3.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.pop3.socketFactory.fallback", "false");
        properties.setProperty("mail.pop3.socketFactory.port",
                String.valueOf(port));

        Session session = Session.getDefaultInstance(properties);

        try {
            Store store = session.getStore("pop3");
            store.connect(userName, password);

            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);

            Message[] arrayMessages = folderInbox.getMessages();

            for (int i = 0; i < arrayMessages.length; i++) {
                Message message = arrayMessages[i];
                //message.setFlag(Flags.Flag.DELETED, this.deleteMessage);
                Address[] fromAddress = message.getFrom();
                String from = fromAddress[0].toString();
                String subject = message.getSubject();
                String sentDate = message.getSentDate().toString();

                String contentType = message.getContentType();
                String messageContent = "";

                // store attachment file name, separated by comma
                String attachFiles = "";
                List<File> files = null;

                if (contentType.contains("multipart")) {
                    Multipart multiPart = (Multipart) message.getContent();
                    files = extractAttachment(multiPart);
                }

                // print out details of each message
                System.out.println("Message #" + (i + 1) + ":");
                System.out.println("\t From: " + from);
                System.out.println("\t Subject: " + subject);
                System.out.println("\t Sent Date: " + sentDate);
                System.out.println("\t Message: " + messageContent);
                assert files != null;
                System.out.println("\t Attachments: " + files.size());

            }

            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for pop3.");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<File> extractAttachment(Multipart multipart) {
        List<File> attachments = new ArrayList<>();
        try {

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (bodyPart.getContent() instanceof Multipart) {
                    attachments.addAll(extractAttachment((Multipart) bodyPart.getContent())) ;
                }

                if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    continue;
                }

                InputStream is = bodyPart.getInputStream();
                String filePath = this.saveDirectory + bodyPart.getFileName();
                System.out.println("Saving: " + filePath);
                File f = new File(filePath);
                FileOutputStream fos = new FileOutputStream(f);
                byte[] buf = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buf)) != -1) {
                    fos.write(buf, 0, bytesRead);
                }
                fos.close();
                attachments.add(f);
            }
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
        return attachments;
    }

}
