import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class JdbcLogin {
    public String Login;
    public String MotDePasse;
    private boolean Logged = false;
    Connection conn;

    public void StartBdd()  {
        String driverName = "com.mysql.cj.jdbc.Driver";


        String serverName = "localhost";
        String mydatabase = "melichalenge";
        String url = "jdbc:mysql://" + serverName + "/" + mydatabase;

        String username = "root";
        String password = "Cris.2093";

        try {
            Class.forName(driverName); //here is the ClassNotFoundException
            conn  = DriverManager.getConnection(url, username, password);
            System.out.println("Connection Successful");
        } catch (Exception ex) {
            System.out.println("Error" + ex);

        }
    }

    public void doInsert(MessagesToDataBase message)
    {
        System.out.print("\n[Performing INSERT] ... ");
        try
        {

            Statement st = conn.createStatement();
            st.executeUpdate("INSERT INTO datos  (from, subject, date1)" +
                      "  VALUES  ('" +message.getFrom()+ "','" +message.getSubject()+ "','" +message.getDate()+"');");
            conn .close();
        }
        catch (SQLException ex)
        {
            System.err.println(ex.getMessage());
        }
        System.out.println(""+ message.getFrom());
        System.out.println(""+ message.getSubject());
        System.out.println(""+ message.getDate());
    }

}