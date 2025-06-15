module org.example.fxdemo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.fxdemo to javafx.fxml;
    exports org.example.fxdemo;
}