module com.example.classroomquizbattleapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.example.classroomquizbattleapp to javafx.fxml;
    opens com.example.classroomquizbattleapp.ui to javafx.fxml;

    exports com.example.classroomquizbattleapp;
    exports com.example.classroomquizbattleapp.model;
    exports com.example.classroomquizbattleapp.database;
    exports com.example.classroomquizbattleapp.server;
    exports com.example.classroomquizbattleapp.client;
    exports com.example.classroomquizbattleapp.ui;
}