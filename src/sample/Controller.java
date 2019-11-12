package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.util.ArrayList;

public class Controller {

    private ObservableList list_of_stations = FXCollections.observableArrayList();

    @FXML
    ChoiceBox<String> stations_From;
    @FXML
    ChoiceBox<String> stations_To;
    @FXML
    Button btn;
    @FXML
    Spinner<Double> hours;
    @FXML
    TextArea screen;
    @FXML
    Spinner<Double> minutes;

    SelectRoute trip=new SelectRoute();

    private void loadStations() {
        list_of_stations.removeAll(list_of_stations);
        String a = "Kobenhavn H";
        String b = "Hoje Tastrup";
        String c = "Roskilde";
        String d = "Ringsted";
        String e = "Odense";
        String f = "Nastved";
        String g = "Nykobing F";
        list_of_stations.addAll(a, b, c, d, e, f, g);
        stations_From.getItems().addAll(list_of_stations);
        stations_To.getItems().addAll(list_of_stations);
    }

    private void loadSpinner(){

        SpinnerValueFactory<Double> svf = new SpinnerValueFactory.DoubleSpinnerValueFactory(00.00, 24.00);
        hours.setValueFactory(svf);

        SpinnerValueFactory<Double> svf2 = new SpinnerValueFactory.DoubleSpinnerValueFactory(00.00, 59.00);
        minutes.setValueFactory(svf2);
    }

    @FXML
    public void initialize() {
        loadStations();
        loadSpinner();
        btn.addEventHandler(ActionEvent.ACTION, event -> {
            String from = stations_From.getValue();
            String to = stations_To.getValue();

            double hour= hours.getValue();
            double minute=minutes.getValue()/100;
            double t=hour+minute;

            try{
                if (from.equals(to)) {
                    screen.setText("Please, enter a valid route!");
                }else {
                    screen.setText(trip.getTrip(t, from,to));
                }

            } catch (NullPointerException e){
                screen.setText("We undergo some technical problems. Sorry for the inconvenience!");
            }
        });
    }
}

class SelectRoute {

    private Connection connect() {
        String url = "jdbc:sqlite:TrainDatabase.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public String getTrip(double tim, String from, String to) {

        int fromStation = stationToInteger(from);
        int toStation = stationToInteger(to);

        String sql = " SELECT * " +
                " FROM Arrivals AS start" +
                " JOIN Arrivals AS finish ON start.trainID = finish.trainID " +
                " INNER JOIN" +
                "       Trains ON Trains.trainID = start.trainID"+
                " WHERE start.arrivalTime >= ? AND " +
                "       start.arrivalTime < finish.arrivalTime AND " +
                "       start.stationID = ? AND " +
                "       finish.stationID = ?" +
                " ORDER BY start.arrivalTime";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, tim);
            pstmt.setInt(2, fromStation);
            pstmt.setInt(3, toStation);

            ResultSet routeResult = pstmt.executeQuery();

            if (!routeResult.next()) {
                return "Sorry, there is no scheduled route!";
            }
            String routes="";
            ArrayList<String> routesList=new ArrayList<>();
            do {
                routesList.add("Train Code: " + routeResult.getString("trainCode") + " --> "
                        + "Departure Time: " + routeResult.getDouble("arrivalTime") + "." );
            } while (routeResult.next()) ;

            for (int i = 0; i < routesList.size(); i++) {
                routes += routesList.get(i) + "\n";
            }
            return routes;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return sql;
    }

    public int stationToInteger(String station) {
        int f = -1;
        switch (station) {
            case "Kobenhavn H":
                f = 1;
                break;
            case "Hoje Tastrup":
                f = 2;
                break;
            case "Roskilde":
                f = 3;
                break;
            case "Ringsted":
                f = 4;
                break;
            case "Odense":
                f = 5;
                break;
            case "Nastved":
                f = 6;
                break;
            case "Nykobing F":
                f = 7;
                break;
        }
        return f;
    }
}
