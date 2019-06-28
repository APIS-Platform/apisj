package org.apis.gui.controller.popup;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apis.gui.controller.base.BaseFxmlController;
import org.apis.gui.controller.base.BasePopupController;
import org.apis.gui.manager.StringManager;
import org.apis.gui.model.PeersModel;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import static javafx.scene.control.TableColumn.SortType.ASCENDING;

public class PopupPeersController extends BasePopupController {
    @FXML private GridPane defaultGrid, detailGrid;
    @FXML private VBox peersDetailVBox;
    @FXML private Label titleLabel, subTitleLabel, guideLabel, selectedNode, viaNode;
    @FXML private TableView peersTable;
    @FXML private TableColumn<PeersModel, Integer> nodeIdCol;
    @FXML private TableColumn<PeersModel, String> nodeServiceCol, userAgentCol, pingCol;

    private final Label peersTablePlaceholder = new Label();
    private ObservableList<PeersModel> peersData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageSetting();

        // Add peers list to model
        addPeersList();

        // Initialize table columns
        initializeColumns();

        // Add custom comparator for ping column('ms' sort processing)
        customComparator();

        // Add list to table
        peersTable.setItems(peersData);

        // Disable table header reordering
        peersTable.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                TableHeaderRow header = (TableHeaderRow) peersTable.lookup("TableHeaderRow");
                header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        header.setReordering(false);
                    }
                });
            }
        });

        // Add select table contents listener
        peersTable.setOnMouseClicked(mouseEventHandler);
        setDefaultVisible();
    }

    public void languageSetting() {
        titleLabel.textProperty().bind(StringManager.getInstance().popup.peersTitle);
        subTitleLabel.textProperty().bind(StringManager.getInstance().popup.peersSubTitle);
        nodeIdCol.textProperty().bind(StringManager.getInstance().popup.nodeIdCol);
        nodeServiceCol.textProperty().bind(StringManager.getInstance().popup.nodeServiceCol);
        userAgentCol.textProperty().bind(StringManager.getInstance().popup.userAgentCol);
        pingCol.textProperty().bind(StringManager.getInstance().popup.pingCol);
        peersTablePlaceholder.textProperty().bindBidirectional(StringManager.getInstance().popup.peersTablePlaceholder);
        peersTable.setPlaceholder(peersTablePlaceholder);
        guideLabel.textProperty().bind(StringManager.getInstance().popup.peersGuideLabel);
    }

    public void addPeersList() {
        peersData.add(new PeersModel());
        peersData.add(new PeersModel(1, "192.168.0.3:8080", "/Apis Core:0.8.810/", "12 ms"));
        peersData.add(new PeersModel(2, "192.168.0.2:8080", "/Apis Core:0.8.810/", "65548 ms"));
        peersData.add(new PeersModel(3, "192.168.0.5:8080", "/Apis Core:0.8.810/", "102 ms"));
        peersData.add(new PeersModel(4, "192.168.0.6:8080", "/Apis Core:0.8.810/", "250 ms"));
        peersData.add(new PeersModel());
        peersData.add(new PeersModel(1, "192.168.0.3:8080", "/Apis Core:0.8.810/", "12 ms"));
        peersData.add(new PeersModel(2, "192.168.0.2:8080", "/Apis Core:0.8.810/", "65548 ms"));
        peersData.add(new PeersModel(3, "192.168.0.5:8080", "/Apis Core:0.8.810/", "102 ms"));
        peersData.add(new PeersModel(4, "192.168.0.6:8080", "/Apis Core:0.8.810/", "250 ms"));
        peersData.add(new PeersModel());
        peersData.add(new PeersModel(1, "192.168.0.3:8080", "/Apis Core:0.8.810/", "12 ms"));
        peersData.add(new PeersModel(2, "192.168.0.2:8080", "/Apis Core:0.8.810/", "65548 ms"));
        peersData.add(new PeersModel(3, "192.168.0.5:8080", "/Apis Core:0.8.810/", "102 ms"));
        peersData.add(new PeersModel(4, "192.168.0.6:8080", "/Apis Core:0.8.810/", "250 ms"));
        peersData.add(new PeersModel());
        peersData.add(new PeersModel(1, "192.168.0.3:8080", "/Apis Core:0.8.810/", "12 ms"));
        peersData.add(new PeersModel(2, "192.168.0.2:8080", "/Apis Core:0.8.810/", "65548 ms"));
        peersData.add(new PeersModel(3, "192.168.0.5:8080", "/Apis Core:0.8.810/", "102 ms"));
        peersData.add(new PeersModel(4, "192.168.0.6:8080", "/Apis Core:0.8.810/", "250 ms"));
        peersData.add(new PeersModel());
        peersData.add(new PeersModel(1, "192.168.0.3:8080", "/Apis Core:0.8.810/", "12 ms"));
        peersData.add(new PeersModel(2, "192.168.0.2:8080", "/Apis Core:0.8.810/", "65548 ms"));
        peersData.add(new PeersModel(3, "192.168.0.5:8080", "/Apis Core:0.8.810/", "102 ms"));
        peersData.add(new PeersModel(4, "192.168.0.6:8080", "/Apis Core:0.8.810/", "250 ms"));
    }

    public void initializeColumns() {
        nodeIdCol.setCellValueFactory(cellData -> cellData.getValue().nodeIdProperty().asObject());
        nodeServiceCol.setCellValueFactory(cellData -> cellData.getValue().nodeServiceProperty());
        userAgentCol.setCellValueFactory(cellData -> cellData.getValue().userAgentProperty());
        pingCol.setCellValueFactory(cellData -> cellData.getValue().pingProperty());

        // Automatic column width
        nodeIdCol.prefWidthProperty().bind(peersTable.widthProperty().divide(15).multiply(2));
        nodeServiceCol.prefWidthProperty().bind(peersTable.widthProperty().divide(15).multiply(5));
        userAgentCol.prefWidthProperty().bind(peersTable.widthProperty().divide(15).multiply(5));
        pingCol.prefWidthProperty().bind(peersTable.widthProperty().divide(5).multiply(1));
    }

    private void customComparator() {
        FilteredList<PeersModel> filteredList = new FilteredList<PeersModel>(peersData);
        peersTable.sortPolicyProperty().set(new Callback<TableView<PeersModel>, Boolean>() {
            @Override
            public Boolean call(TableView<PeersModel> param) {
                final Comparator<PeersModel> tableComparator = peersTable.getComparator();

                Comparator<PeersModel> comparator = tableComparator == null ? null : new Comparator<PeersModel>() {
                    @Override
                    public int compare(PeersModel o1, PeersModel o2) {
                        TableColumn<PeersModel, ?> sortOrderCol = (TableColumn<PeersModel, ?>)peersTable.getSortOrder().get(0);
                        final int result;

                        if(sortOrderCol.getId().equals("pingCol")) {
                            String o1Str = o1.pingProperty().get();
                            String o2Str = o2.pingProperty().get();
                            int o1Int, o2Int;

                            if(ASCENDING.equals(sortOrderCol.getSortType())) {
                                o1Int = Integer.parseInt(o1Str.split(" ")[0]);
                                o2Int = Integer.parseInt(o2Str.split(" ")[0]);
                            } else {
                                o1Int = Integer.parseInt(o2Str.split(" ")[0]);
                                o2Int = Integer.parseInt(o1Str.split(" ")[0]);
                            }

                            if (o1Int > o2Int) {
                                result = 1;
                            } else if (o1Int == o2Int) {
                                result = 0;
                            } else {
                                result = -1;
                            }

                        } else {
                            final Object value1 = sortOrderCol.getCellData(o1);
                            final Object value2 = sortOrderCol.getCellData(o2);

                            @SuppressWarnings("unchecked") final Comparator<Object> c = (Comparator<Object>) sortOrderCol.getComparator();
                            result = ASCENDING.equals(sortOrderCol.getSortType()) ? c.compare(value1, value2)
                                    : c.compare(value2, value1);
                        }

                        return result;
                    }
                };

                peersTable.setItems(filteredList.sorted(comparator));
                return true;
            }
        });
    }

    private final EventHandler<MouseEvent> mouseEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            String fxid = ((Node)event.getSource()).getId();

            if(fxid.equals("peersTable")) {
                if(peersTable.getSelectionModel().getSelectedIndex() >= 0) {
                    setDetailList(peersTable.getSelectionModel().getSelectedIndex());
                    setDetailVisible();
                }
            }
        }
    };

    private void setDetailList(int index) {
        // Exception handling for reordering list
        ObservableList<PeersModel> currentPeersData = peersTable.getItems();
        PeersModel model = currentPeersData.get(index);

        selectedNode.setText(model.getNodeService() + " (node id: " + model.getNodeId() + ")");
        viaNode.setText("via " + "model.getViaNodeService");

        // Add Details
        peersDetailVBox.getChildren().clear();
        addDetailItem("WhiteListed", "No");
        addDetailItem("Direction", "Outbound");
        addDetailItem("Version", "70208");
        addDetailItem("User Agent", model.getUserAgent());
        addDetailItem("Services", "NETWORK & BLOOM");
        addDetailItem("Starting Block", "774365");
        addDetailItem("Synced Headers", "774365");
        addDetailItem("Synced Blocks", "774365");
        addDetailItem("Ban Score", "0");
        addDetailItem("Connection Time", "57 s");
        addDetailItem("Last Send", "0 s");
        addDetailItem("Last Receive", "0 s");
        addDetailItem("Sent", "77 KB");
        addDetailItem("Received", "137 KB");
        addDetailItem("Ping Time", model.getPing());
        addDetailItem("Ping Wait", "N/A");
        addDetailItem("Min Ping", model.getPing());
        addDetailItem("Time Offset", "-3 s");
    }

    private void addDetailItem(String head, String contents) {
        try {
            BaseFxmlController fxmlController = new BaseFxmlController("popup/popup_peers_detail_item.fxml");
            PopupPeersDetailItemController itemController = (PopupPeersDetailItemController) fxmlController.getController();

            itemController.setHeadText(head);
            itemController.setContentsText(contents);

            peersDetailVBox.getChildren().add(fxmlController.getNode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDefaultVisible() {
        detailGrid.setVisible(false);
        defaultGrid.setVisible(true);
    }

    public void setDetailVisible() {
        defaultGrid.setVisible(false);
        detailGrid.setVisible(true);
    }

    public ObservableList<PeersModel> getPeersData() {
        return peersData;
    }

}
