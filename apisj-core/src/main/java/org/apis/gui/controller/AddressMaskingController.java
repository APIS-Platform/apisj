package org.apis.gui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class AddressMaskingController implements Initializable {

    @FXML
    private Label tabLabel1, tabLabel2, sideTabLabel1, sideTabLabel2;
    @FXML
    private Pane tabLinePane1, tabLinePane2, sideTabLinePane1, sideTabLinePane2;
    @FXML
    private AnchorPane tab1LeftPane, tab1RightPane, tab2LeftPane1, tab2LeftPane2, tab2LeftPane3;
    @FXML
    private GridPane commercialDescGrid, publicDescGrid, tab2RightPane1;
    @FXML
    private ImageView domainDragDrop;
    @FXML
    private TextField addrMaskingIDTextField, commercialDomainTextField, publicDomainTextField, emailTextField;
    @FXML
    private TextArea publicTextArea;

    private Image domainDragDropGrey, domainDragDropColor, domainDragDropCheck;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize Images
        domainDragDropGrey = new Image("image/bg_domain_dragdrop_grey@2x.png");
        domainDragDropColor = new Image("image/bg_domain_dragdrop_color@2x.png");
        domainDragDropCheck = new Image("image/bg_domain_dragdrop_check@2x.png");

        this.tab1LeftPane.setVisible(true);
        this.tab1RightPane.setVisible(true);
        this.tab2LeftPane1.setVisible(false);
        this.addrMaskingIDTextField.setText("");
        this.tabLabel1.setTextFill(Color.web("#910000"));
        this.tabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
        this.tabLinePane1.setVisible(true);
        this.tabLabel2.setTextFill(Color.web("#999999"));
        this.tabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
        this.tabLinePane2.setVisible(false);

        this.commercialDomainTextField.focusedProperty().addListener(textFieldListener);
        this.publicDomainTextField.focusedProperty().addListener(textFieldListener);
    }

    private ChangeListener<Boolean> textFieldListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            // Focus in Function
            if(newValue) {
                if(tab2LeftPane2.isVisible()) {
                    commercialDomainTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                } else if(tab2LeftPane3.isVisible()) {
                    publicDomainTextField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #999999; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }

            // Focus out Function
            else {
                if(tab2LeftPane2.isVisible()) {
                    commercialDomainTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                } else if(tab2LeftPane3.isVisible()) {
                    publicDomainTextField.setStyle("-fx-background-color: #f2f2f2; -fx-border-color: #d8d8d8; -fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 12px;" +
                            " -fx-border-radius : 4 4 4 4; -fx-background-radius: 4 4 4 4; -fx-prompt-text-fill: #999999; -fx-text-fill: #2b2b2b;");
                }
            }
        }
    };

    @FXML
    private void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();

        if(id.equals("tab1")) {
            initTab(0);

        } else if(id.equals("tab2")) {
            initTab(1);

        } else if(id.equals("sideTab1")) {
            initSideTab(0);

        } else if(id.equals("sideTab2")) {
            initSideTab(1);

        } else if(id.equals("domainRequestBtn")) {
            if(commercialDescGrid.isVisible()) {
                this.tab2LeftPane1.setVisible(false);
                this.tab2RightPane1.setVisible(true);
                this.tab2LeftPane2.setVisible(true);
                this.commercialDomainTextField.setText("");
                this.emailTextField.setText("");
            } else {
                this.tab2LeftPane1.setVisible(false);
                this.tab2LeftPane3.setVisible(true);
                this.publicDomainTextField.setText("");
                this.publicTextArea.setText("");
            }

        } else if(id.equals("commercialBackBtn")) {
            this.tab2LeftPane2.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tab2LeftPane1.setVisible(true);

        } else if(id.equals("publicBackBtn")) {
            this.tab2LeftPane3.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tab2LeftPane1.setVisible(true);

        } else if(id.equals("publicSendBtn")) {
            this.tab2RightPane1.setVisible(true);
            this.emailTextField.setText("");
        }

    }

    public void initTab(int index){
        if(index == 0) {
            this.tab1LeftPane.setVisible(true);
            this.tab1RightPane.setVisible(true);
            this.tab2LeftPane1.setVisible(false);
            this.tab2LeftPane2.setVisible(false);
            this.tab2LeftPane3.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.addrMaskingIDTextField.setText("");
            this.tabLabel1.setTextFill(Color.web("#910000"));
            this.tabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.tabLinePane1.setVisible(true);
            this.tabLabel2.setTextFill(Color.web("#999999"));
            this.tabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
            this.tabLinePane2.setVisible(false);

        } else if(index == 1) {
            this.tab1LeftPane.setVisible(false);
            this.tab2LeftPane2.setVisible(false);
            this.tab2LeftPane3.setVisible(false);
            this.tab1RightPane.setVisible(false);
            this.tab2RightPane1.setVisible(false);
            this.tab2LeftPane1.setVisible(true);
            this.tabLabel2.setTextFill(Color.web("#910000"));
            this.tabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:12px;");
            this.tabLinePane2.setVisible(true);
            this.tabLabel1.setTextFill(Color.web("#999999"));
            this.tabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size:12px;");
            this.tabLinePane1.setVisible(false);

            this.publicDescGrid.setVisible(false);
            this.commercialDescGrid.setVisible(true);
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 14px;");
            this.sideTabLinePane1.setVisible(true);
            this.sideTabLabel2.setTextFill(Color.web("#999999"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size: 14px;");
            this.sideTabLinePane2.setVisible(false);

        }
    }
    public void initSideTab(int index){
        if(index == 0) {
            this.commercialDescGrid.setVisible(true);
            this.publicDescGrid.setVisible(false);
            this.sideTabLabel1.setTextFill(Color.web("#910000"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 14px;");
            this.sideTabLinePane1.setVisible(true);
            this.sideTabLabel2.setTextFill(Color.web("#999999"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size: 14px;");
            this.sideTabLinePane2.setVisible(false);

        } else if(index == 1) {
            this.commercialDescGrid.setVisible(false);
            this.publicDescGrid.setVisible(true);
            this.sideTabLabel2.setTextFill(Color.web("#910000"));
            this.sideTabLabel2.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size: 14px;");
            this.sideTabLinePane2.setVisible(true);
            this.sideTabLabel1.setTextFill(Color.web("#999999"));
            this.sideTabLabel1.setStyle("-fx-font-family: 'Open Sans'; -fx-font-size: 14px;");

            this.sideTabLinePane1.setVisible(false);
        }
    }

    public void domainDragDropMouseEntered() {
        this.domainDragDrop.setImage(domainDragDropColor);
    }

    public void domainDragDropMouseExited() {
        this.domainDragDrop.setImage(domainDragDropGrey);
    }



}
