package org.apis.gui.controller.module.textfield;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apis.gui.controller.base.BaseViewController;
import org.apis.gui.controller.popup.PopupPrintPrivatekeyController;
import org.apis.gui.manager.StringManager;
import org.apis.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ApisTextFieldPkController extends BaseViewController {
    private String style = "-fx-background-insets: 0, 0 0 0 0; -fx-background-color: transparent; -fx-prompt-text-fill: #999999; " +
            "-fx-font-family: 'Noto Sans CJK JP Medium'; -fx-font-size: 12px;";

    private String address;

    @FXML private ImageView createWalletPkCover;
    @FXML private Image passwordPrivate, passwordPublic;
    @FXML private TextField textField;
    @FXML private PasswordField passwordField;
    @FXML private Pane borderLine;
    @FXML private Label copyBtn, saveLabel;

    private ApisTextFieldPkImpl handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        passwordPublic = new Image("image/ic_public@2x.png");
        passwordPrivate = new Image("image/ic_private@2x.png");

        textField.textProperty().bindBidirectional(passwordField.textProperty());
        copyBtn.textProperty().bind(StringManager.getInstance().common.copyButton);
        saveLabel.textProperty().bind(StringManager.getInstance().common.savePDF);

        textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.TAB){
                    if(handler != null){
                        handler.onKeyTab();
                    }
                }else if(event.getCode() == KeyCode.ENTER){
                    if(handler != null){
                        handler.onAction();
                    }
                }else if(event.getCode() == KeyCode.PAGE_UP && event.isControlDown()) {
                    event.consume();
                }else if(event.getCode() == KeyCode.PAGE_DOWN && event.isControlDown()) {
                    event.consume();
                }
            }
        });

        passwordField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.TAB){
                    if(handler != null){
                        handler.onKeyTab();
                    }
                }else if(event.getCode() == KeyCode.ENTER){
                    if(handler != null){
                        handler.onAction();
                    }
                }else if(event.getCode() == KeyCode.PAGE_UP && event.isControlDown()) {
                    event.consume();
                }else if(event.getCode() == KeyCode.PAGE_DOWN && event.isControlDown()) {
                    event.consume();
                }
            }
        });

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
        String text = passwordField.getText().trim();
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        if(handler != null) {
            handler.copy();
        }
    }

    public void print() {
        Stage printStage = new Stage();
        Parent rootPrint;

        try {
            URL aliasHeaderUrl  = getClass().getClassLoader().getResource("scene/popup/popup_print_privatekey.fxml");

            FXMLLoader loader = new FXMLLoader(aliasHeaderUrl);
            rootPrint = loader.load();
            PopupPrintPrivatekeyController controller = (PopupPrintPrivatekeyController)loader.getController();

            controller.init(ByteUtil.hexStringToBytes(this.address), ByteUtil.hexStringToBytes(passwordField.getText().trim()));
            printStage.initModality(Modality.APPLICATION_MODAL);
            printStage.getIcons().add(new Image("image/ic_favicon@2x.png"));
            printStage.setTitle("Print Private Key");

            printStage.setScene(new Scene(rootPrint, 543, 203));
            printStage.show();

            Screen screen = Screen.getPrimary();

            Printer printer = Printer.getDefaultPrinter();
            PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, 15, 0, 15, 0);

            PrinterJob job = PrinterJob.createPrinterJob();
            rootPrint.prefWidth(pageLayout.getPrintableWidth());
            rootPrint.prefHeight(pageLayout.getPrintableHeight());
            if (job != null) {
                boolean success = job.printPage(pageLayout, rootPrint);
                if (success) {
                    job.endJob();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getText() { return textField.getText().trim(); }

    public void setText(String text) { this.textField.textProperty().setValue(text); }
    public void setAddress(String address) { this.address = address; }

    public ApisTextFieldPkImpl getHandler() {
        return handler;
    }

    public void setHandler(ApisTextFieldPkImpl handler) {
        this.handler = handler;
    }

    public interface ApisTextFieldPkImpl {
        void copy();
        void onAction();
        void onKeyTab();
    }
}
