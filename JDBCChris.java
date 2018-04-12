package jdbcchris;

//Christopher Connolly
//A00247198
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

class JDBCChris extends JFrame implements ActionListener {

    private JButton exportButton = new JButton("Export All Data to CSV");
    private JButton irishPopbyCounty = new JButton("Latest Population by County");
    private JButton county = new JButton("2016 Population for County Name: ");
    private JButton popByCountyforYear = new JButton("Population by Each County for Year: ");
   
//JDBC Variables
    private Connection con = null;
    private Statement stmt = null;

    //Connection Variables
    private String host = "localhost";
    private int port = 3306;
    private String dbTable = "/census16prelim";
    private String username = "root";
    private String password = "admin";

    private String countyList = null;

    //Combination Selector Variables
    private int i = 0;
    private int j = 0;
    private String countySelected = "";
    private String yearSelected = "";

    public JDBCChris(String str) {
        super(str);

        initDBConnection();
        getContentPane().setLayout(new GridLayout(3, 3));
        getContentPane().add(exportButton);
        getContentPane().add(irishPopbyCounty);
        getContentPane().add(populateArray(con));
        getContentPane().add(county);
        getContentPane().add(populateYear(con));
        getContentPane().add(popByCountyforYear);

        exportButton.addActionListener(this);
        irishPopbyCounty.addActionListener(this);
        county.addActionListener(this);
        popByCountyforYear.addActionListener(this);
        setSize(500, 400);
        setVisible(true);

        //Close nicely
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initDBConnection() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + dbTable;
            System.out.println("Connecting to SQL server " + url + " on port " + port);
            con = DriverManager.getConnection(url, username, password);
            stmt = con.createStatement();
        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            System.err.println(e.getMessage());
            // e.printStackTrace();
            //Extra error information provided
        }
        try {
            System.out.println("Populating counties array");

        } catch (Exception m) {
            System.err.println("Error retrieving counties - call");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //ResultSet rs = null;             
        Object target = e.getSource();

        String cmd = null;
        if (target.equals(exportButton)) {
            cmd = "select * from population";
        } else if (target.equals(irishPopbyCounty)) {
            cmd = "select county, sum(pop2016) from census16prelim.population GROUP BY county;";
        } else if (target.equals(county)) {
            //String countySelection = countySelector.getActionCommand();
            // countySelected = (String) populateCounty(countySelection.getSelectedItem());
            cmd = ("SELECT * from census16prelim.population where county = '" + countySelected + "';");
        } else if (target.equals(popByCountyforYear)) {
            //String year = yearInput.getText();
            cmd = ("SELECT electoraldistrict, county, " + yearSelected + " from census16prelim.population;");
        }
        try {
            System.out.println("Command is set to: " + cmd);
            ResultSet rs = stmt.executeQuery(cmd);
            writeToFile(rs);
        } catch (Exception e1) {
            System.err.println("Error passing SQL commands");
            System.err.println(e1.getMessage());
            //e1.printStackTrace();
        }
    }

    //The following code attempts to pull data from the counties column and
    //uses it to populate an array which is used to make a dropdown list
    public JComboBox populateArray(Connection con) {
        Statement stmt = null;
        String query = "select county from census16prelim.population group by county order by county;";
        // System.out.println("method in");
        try {
            stmt = con.createStatement();
            // System.out.println("method mid");
            ResultSet rs = stmt.executeQuery(query);
            ArrayList<String> countiesAL = new ArrayList<>();
            while (rs.next()) {
                i++;
                String countyList = rs.getString("county");
                System.out.println("List of counties available: " + countyList);
                countiesAL.add(countyList);
            }
            System.out.println("Found " + i + " Counties");
            String[] countiesArray = countiesAL.toArray(new String[0]);
            System.out.println(Arrays.toString(countiesArray));
            JComboBox countySelector = new JComboBox(countiesArray);
            countySelected = (String) countySelector.getSelectedItem();
            countySelector.addItemListener(new ItemListener() {
                public void countySelectedM(ItemEvent ie) {
                    yearSelected = (String) countySelector.getSelectedItem();
                }

                @Override
                public void itemStateChanged(ItemEvent e) {
                    countySelected = (String) countySelector.getSelectedItem();
                }
            });
            return countySelector;
        } catch (Exception z) {
            System.out.println("Error in Converting counties to an array");
        }
        return null;
    }

    //Main method
    public static void main(String args[]) {

        new JDBCChris("Population Data Export");

    }

    //This creates a JComboBox for the Year
    public JComboBox populateYear(Connection con) {
        Statement stmt = null;
        String yearlist = null;
        String query = "select column_name from information_schema.columns where column_name like 'pop%%%%';";
        try {
            stmt = con.createStatement();
            // System.out.println("method mid");
            ResultSet rs = stmt.executeQuery(query);
            ArrayList<String> yearsAL = new ArrayList<>();
            while (rs.next()) {
                j++;
                yearlist = rs.getString("column_name");
                System.out.println("List of years available: " + yearlist);
                yearsAL.add(yearlist);
            }
            System.out.println("Found " + j + " Years");
            String[] yearsArray = yearsAL.toArray(new String[0]);
            System.out.println(Arrays.toString(yearsArray));
            JComboBox yearsSelector = new JComboBox(yearsArray);
            yearsSelector.addItemListener(new ItemListener() {
                public void yearSelectedM(ItemEvent ie) {
                    yearSelected = (String) yearsSelector.getSelectedItem();
                }

                @Override
                public void itemStateChanged(ItemEvent e) {
                    yearSelected = (String) yearsSelector.getSelectedItem();
                }
            });
            return yearsSelector;
        } catch (Exception y) {
            System.out.println("Error in Converting counties to an array");
        }
        return null;
    }

    //This method provides a nicely formatted comma separated values file.
    private void writeToFile(ResultSet rs) {
        try {
            FileWriter outputFile = new FileWriter("populationoutput.csv");
            PrintWriter printWriter = new PrintWriter(outputFile);
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();

            for (int i = 0; i < numColumns; i++) {
                printWriter.print(rsmd.getColumnLabel(i + 1) + ",");
                System.out.println(rsmd.getColumnLabel(i + 1) + ",");
            }
            printWriter.print("\n");
            while (rs.next()) {
                for (int i = 0; i < numColumns; i++) {
                    printWriter.print(rs.getString(i + 1) + ",");
                    System.out.println(rs.getString(i + 1) + ",");
                }
                printWriter.print("\n");
                printWriter.flush();
            }
            printWriter.close();
        } catch (Exception e) {
            System.out.println("Error Exporting to CSV");
            //e.printStackTrace();
        }

    }
}
