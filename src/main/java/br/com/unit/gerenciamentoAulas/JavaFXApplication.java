package br.com.unit.gerenciamentoAulas;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Grupo 3 - Sistema de Gerenciamento de Aulas de Véridia
 */
public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext context;
    private static HostServices hostServices;


    @Override
    public void init() {
        hostServices = getHostServices();
        this.context = new SpringApplicationBuilder()
                .sources(ProjetoDeProgramacaoN02Grupo3Application.class)
                .run();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/MainView.fxml")
            );
            loader.setControllerFactory(context::getBean);

            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            primaryStage.setTitle("Sistema de Gerenciamento de Aulas - Véridia");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
        Platform.exit();
    }


    public static HostServices getHost() {
        return hostServices;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
