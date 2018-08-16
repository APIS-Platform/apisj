package org.apis.gui.controller;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PopupMyAddressItem implements Initializable {
    private final float MAX_WIDTH = 307;

    @FXML
    private ImageView icon, btnEdit, btnDelete, btnSelete, btnLeft, btnRight;
    @FXML
    private Label aliasLabel, addressLabel;
    @FXML
    private HBox list;

    private ArrayList<String> textList = new ArrayList<>();
    private ArrayList<ArrayList<String>> groupList = new ArrayList<>();
    private int cursorIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set a clip to apply rounded border to the original image.
        Rectangle clip = new Rectangle( this.icon.getFitWidth()-0.5, this.icon.getFitHeight()-0.5 );
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        icon.setClip(clip);

        ArrayList<String> tempTextList = new ArrayList<>();
        tempTextList.add("#Family");
        tempTextList.add("#PrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivate");
        tempTextList.add("#Company");
        tempTextList.add("#CompanyCompanyCompanyCompanyCompanyCompanyCompanyCompanyCompanyCompanyCompanyCompanyCompanyCompanyCompany");
        tempTextList.add("#Untagged");
        tempTextList.add("#PrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivatePrivate");
        tempTextList.add("#Private");
        tempTextList.add("#Family");
        tempTextList.add("#Untagged");
        setTextList(tempTextList);
        showTextList(0);
    }

    @FXML
    public void onMouseClicked(InputEvent event){
        String id = ((Node)event.getSource()).getId();
        if(id.equals("btnEdit")){

        }else if(id.equals("btnDelete")){

        }else if(id.equals("btnSelete")){

        }else if(id.equals("btnLeft")){
            prevTextList();
        }else if(id.equals("btnRight")){
            nextTextList();
        }
    }

    // split textList to groupList
    private void setTextList(ArrayList<String> _textList){
        textList = _textList;
        ArrayList<String> tempTextList = new ArrayList<>();

        // 1. sum(글자길이 + 좌우여백 + 사이간격) <= 스크롤 가로길이
        // 2. 일정단어 넣은 후 길이 비교 : HBox의 가로길이 <= 스크롤 가로길이
        float widthSum = 0;
        for(int i=0; i<textList.size(); i++){

            Label label = new Label(textList.get(i));
            label.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:10px; -fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-border-color:#d8d8d8; ");
            label.setPadding(new Insets(5, 10, 5, 10));
            if(widthSum > 0){
                widthSum += 10; // spacing
            }

            float fontWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(label.getText(), label.getFont());
            widthSum += (fontWidth <= (MAX_WIDTH - 10) / 2 ) ? fontWidth : (MAX_WIDTH - 10) / 2 ;

            // split group
            if(MAX_WIDTH >= widthSum){
                tempTextList.add(textList.get(i));
            }else{
                groupList.add(tempTextList);
                //init
                tempTextList = new ArrayList<>();
                widthSum = 0;
                i--;
            }

            // add last group
            if(i == textList.size() -1 && tempTextList.size() > 0){
                groupList.add(tempTextList);
            }
        }
    }

    public void showTextList(int cursorIndex){
        this.cursorIndex = cursorIndex;
        list.getChildren().clear();
        for(int i=0; i<this.groupList.get(cursorIndex).size(); i++){
            String text = this.groupList.get(cursorIndex).get(i);
            Label label = new Label(text);
            label.setStyle("-fx-font-family: 'Open Sans SemiBold'; -fx-font-size:10px; -fx-border-radius : 24 24 24 24; -fx-background-radius: 24 24 24 24; -fx-border-color:#d8d8d8; ");
            label.setPadding(new Insets(5, 10, 5, 10));

            label.setMaxWidth( (MAX_WIDTH - 10) / 2 + 20 );
            list.getChildren().add(label);
        }
    }
    public void prevTextList(){
        if(cursorIndex > 0){
            cursorIndex--;
        }
        showTextList(cursorIndex);
    }

    public void nextTextList(){
        if(cursorIndex < this.groupList.size()-1){
            cursorIndex++;
        }
        showTextList(cursorIndex);
    }
}
