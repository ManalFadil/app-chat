module ma.enset.appchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens ma.enset.appchat to javafx.fxml;
    opens ma.enset.appchat.controllers to javafx.fxml;
    exports ma.enset.appchat;
}