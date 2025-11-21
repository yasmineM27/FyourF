package yasminemassaoudi.grp3.fyourf;

public class Config {
    // ============================================
    // SUPABASE CONFIGURATION (Legacy)
    // ============================================
    public static final String SUPABASE_URL = "https://skbttjztscyebsrvghqu.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrYnR0anp0c2N5ZWJzcnZnaHF1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjEzNzkxNTEsImV4cCI6MjA3Njk1NTE1MX0.qrwGvXaMEZP7K31UfmDkJOdAswG-n3SA__aeUdrBrlo";
    public static final String SUPABASE_SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrYnR0anp0c2N5ZWJzcnZnaHF1Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2MTM3OTE1MSwiZXhwIjoyMDc2OTU1MTUxfQ.Pkd2JMYdT2YlSiuOlZVI9EIBmgsqv5AHW0A51aO56Mw";

    // ============================================
    // MYSQL CONFIGURATION (New)
    // ============================================
    // IMPORTANT: Modifier cette IP avec votre adresse IPv4
    // Pour trouver votre IPv4: Windows CMD > ipconfig
    public static String MYSQL_SERVER_IP = "192.168.1.17"; // MODIFIER ICI
    public static int MYSQL_SERVER_PORT = 80;
    public static String MYSQL_SERVICE_FOLDER = "servicephp";

    // URLs générées automatiquement
    public static String MYSQL_BASE_URL = "http://" + MYSQL_SERVER_IP + ":" + MYSQL_SERVER_PORT + "/" + MYSQL_SERVICE_FOLDER;
    public static String MYSQL_GET_ALL_URL = MYSQL_BASE_URL + "/get_all.php";
    public static String MYSQL_ADD_POSITION_URL = MYSQL_BASE_URL + "/add_position.php";

    // ============================================
    // LEGACY SERVER CONFIGURATION
    // ============================================
    public static String IP_Serveur = "192.168.1.17";
    public static int PORT_Serveur = 8080;
    public static String URL_Serveur = "http://" + IP_Serveur + ":" + PORT_Serveur;

    // ============================================
    // DATABASE MODE SELECTION
    // ============================================
    public static boolean USE_MYSQL = true; // true = MySQL, false = Supabase
}
