import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

public class MeliChallengeCode {
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        //Load client secrets
        InputStream in = MeliChallengeCode.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the labels in the user's account.
        String user = "me";

        //hasta este punto quick start gmailapi

        //query para obtener el listado de todos los mails que coinciden con DevOps(solo ids)
        List<Message> messagesIds = listMessagesMatchingQuery(service,user,"+DevOps");
        //a partir de cada id obtenido consulto los datos particuares por cada id
        List<Message> messageBody = new ArrayList<Message>();
        for (Message message:messagesIds) {
            messageBody.add(getMessage(service,user,message.getId()));
        }
        //ya sabiendo cuales son los metofos que guardan los datos necesarios los guardo en una lista MessagesToDataBase
        List<MessagesToDataBase> messageToDataBase = new ArrayList<MessagesToDataBase>();
        for (Message message:messageBody ) {
            MessagesToDataBase mes = new MessagesToDataBase();
            mes.setFrom(message.getPayload().getHeaders().get(18).getValue());
            mes.setSubject(message.getPayload().getHeaders().get(20).getValue());
            String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
            //SimpleDateFormat formatter = new SimpleDateFormat();

            SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.ENGLISH);
            try {
                Date date = formatter.parse(message.getPayload().getHeaders().get(23).getValue());
                mes.setDate(date);

          } catch (ParseException e) {
                e.printStackTrace();
            }
            messageToDataBase.add(mes);
        }
        //y guardamos esto en la base de datos para pasarlo al doinsert

        JdbcLogin conexion = new JdbcLogin();
        conexion.StartBdd();
        for (MessagesToDataBase message :messageToDataBase) {
            conexion.doInsert(message);
        }
    }
    //Users.messages: list Gmail Api
    //Listar mensajes in the user's mailbox
    /**
     * List all Messages of the user's mailbox matching the query.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param query String used to filter the Messages listed.
     * @throws IOException
     */
    public static List<Message> listMessagesMatchingQuery(Gmail service, String userId,
                                                          String query) throws IOException {
        ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(userId).setQ(query)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }

        for (Message message : messages) {
            System.out.println(message.toPrettyString());
        }

        return messages;
    }
// metodo Get ApiGoogle
    /**
     * Get Message with given ID.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param messageId ID of Message to retrieve.
     * @return Message Retrieved Message.
     * @throws IOException
     */
    //  en el metodo lo use para obtener la parte que necesitaba from subject date
    public static Message getMessage(Gmail service, String userId, String messageId)
            throws IOException {
        Message message = service.users().messages().get(userId, messageId).execute();


        System.out.println(""+ message.getPayload().getHeaders().get(18).getValue());
        System.out.println(""+ message.getPayload().getHeaders().get(20).getValue());
        System.out.println(""+ message.getPayload().getHeaders().get(23).getValue());
        //System.out.println("Message snippet: " + message.getPayload().getParts().get(0));
        return message;
    }
}