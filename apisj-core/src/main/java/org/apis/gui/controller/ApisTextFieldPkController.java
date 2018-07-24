package org.apis.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisTextFieldPkController implements Initializable {
    private String style = "-fx-background-insets: 0, 0 0 0 0; -fx-background-color: transparent; -fx-prompt-text-fill: #999999; " +
            "-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;";

    @FXML
    private ImageView createWalletPkCover;
    @FXML
    private Image passwordPrivate, passwordPublic;
    @FXML
    private TextField textField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Pane borderLine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ApisTextFieldPkController");

        passwordPublic = new Image("image/ic_public@2x.png");
        passwordPrivate = new Image("image/ic_private@2x.png");

        textField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    public void togglePasswordFieldClick(InputEvent event) {
        String fxid = ((Node)event.getSource()).getId();

        if(fxid.equals("createWalletPkCover")) {
            togglePasswordField();

            if(this.passwordField.isVisible()) {
                this.createWalletPkCover.setImage(passwordPrivate);
            } else {
                this.createWalletPkCover.setImage(passwordPublic);
                this.textField.setStyle(style + " -fx-text-fill: #2b2b2b;");
                this.borderLine.setStyle("-fx-background-color: #2b2b2b;");
            }
        }else{

            System.out.println("else");
        }
    }

    public void togglePasswordField(){
        if(textField.isVisible()){
            passwordField.setVisible(true);
            textField.setVisible(false);
        } else {
            textField.setVisible(true);
            passwordField.setVisible(false);
        }
    }

    public void init() {
        this.textField.setVisible(false);
        this.passwordField.setVisible(true);
        this.createWalletPkCover.setImage(passwordPrivate);
    }

    public void copy() {
        String text = passwordField.getText();
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public void print() {
        Stage printStage = new Stage();
        Parent rootPrint;

        try {
            URL aliasHeaderUrl  = new File("apisj-core/src/main/resources/scene/popup_print_privatekey.fxml").toURI().toURL();
            rootPrint = FXMLLoader.load(aliasHeaderUrl);
            printStage.initModality(Modality.APPLICATION_MODAL);
            printStage.setTitle("Print Private Key");

            Screen screen = Screen.getPrimary();
            double dpi = screen.getDpi();
            printStage.setScene(new Scene(rootPrint, 539, 296));
            printStage.show();

            //doPrintFirst(rootPrint);

            doPrintSecond(rootPrint);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    //First
    public void doPrintFirst(Node printPane) {


        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            if(job.showPageSetupDialog(null)){
                boolean printed = job.printPage(printPane);
                if (printed) {
                    job.showPageSetupDialog(null);
                    job.endJob();
                } else {
                    System.out.print("Faled");

                }
            }

        } else {
            System.out.print("could");

        }
    }

    // Second
    public void doPrintSecond(final Parent parent) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Printer printer = Printer.getDefaultPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, 0, 0, 0, 0);
        //printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);

        PrinterJob job = PrinterJob.createPrinterJob();
        //parent.setPrefSize(, );
        parent.prefWidth(pageLayout.getPrintableWidth());
        parent.prefHeight(pageLayout.getPrintableHeight());
        if (job != null && job.showPrintDialog(parent.getScene().getWindow())) {
            boolean success = job.printPage(pageLayout, parent);
            if (success) {
                job.endJob();
            }
        }
    }


    public String getText() { return textField.getText(); }

    public void setText(String text) { this.textField.textProperty().setValue(text); }
}
