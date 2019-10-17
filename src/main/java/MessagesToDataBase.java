import java.util.Date;

public class MessagesToDataBase {
    private String from;
    private String subject;
    private Date date;
//constructor
    public MessagesToDataBase(String from, String subject) {
        this.from = from;
        this.subject = subject;
    }

    public MessagesToDataBase(String from, String subject, Date date) {
        this.from = from;
        this.subject = subject;
        this.date = date;
    }

    public MessagesToDataBase() {

    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public Date getDate() {
        return date;
    }
}
