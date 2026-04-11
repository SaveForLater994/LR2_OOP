module com.example.lr2_oop {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.example.lr2_oop to javafx.fxml;
    exports com.example.lr2_oop;
}