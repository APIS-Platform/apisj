package org.apis.gui.common;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apis.gui.jsinterface.APISConsole;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.standard.Fidelity;
import javax.print.attribute.standard.PrinterResolution;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class APISPrintDialog extends JDialog {
    private static final int WEBVIEW_WIDTH = 552;
    private static final int WEBVIEW_HEIGHT = 214;

    private static final int CLOSE_BUTTON_WIDTH = 30;
    private static final int CLOSE_BUTTON_HEIGHT = 24;

    private static final int HEADER_HEIGHT = 24;
    private static final int BODY_HEIGHT = WEBVIEW_HEIGHT - 10;

    private static final int FRAME_WIDTH = WEBVIEW_WIDTH + 4;
    private static final int FRAME_HEIGHT = HEADER_HEIGHT + BODY_HEIGHT + 4;
    public static final String INDEX_HTML_PATH = "/webView/print_popup.html";

    private APISConsole console = new APISConsole();
    private JPanel headerPanel, bodyPanel;
    private APISCloseButton closeBtn;
    private JButton printBtn;
    private WebView webView;
    private WebEngine webEngine;
    private JFXPanel fxPanel;
    private JEditorPane jePane;

    public APISPrintDialog(){

        FrameDragListener listener = new FrameDragListener(this);

        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setUndecorated(true);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        Container contentPane = this.getContentPane();
        contentPane.setBackground(Color.decode("#ffffff"));
        contentPane.setLayout(null);

        // header
        headerPanel = new JPanel();
        headerPanel.setBounds(0,0, FRAME_WIDTH, HEADER_HEIGHT);
        headerPanel.setLayout(null);
        headerPanel.addMouseListener(listener);
        headerPanel.addMouseMotionListener(listener);
        contentPane.add(headerPanel);

        // header - close button
        closeBtn = new APISCloseButton();
        closeBtn.setSize(CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT);
        closeBtn.setLocation(FRAME_WIDTH - CLOSE_BUTTON_WIDTH, 0);
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                APISPrintDialog.this.setVisible(false);
            }
        });
        headerPanel.add(closeBtn);

        // body
        bodyPanel = new JPanel();
        bodyPanel.setBounds((FRAME_WIDTH - WEBVIEW_WIDTH)/2, HEADER_HEIGHT + 2, WEBVIEW_WIDTH, WEBVIEW_HEIGHT);
        bodyPanel.setLayout(null);
        contentPane.add(bodyPanel);

    }

    public void init(byte[] addr, byte[] privatekey, String imgUrl1, String imgUrl2){
        fxPanel = new JFXPanel();
        fxPanel.setBounds(0,0, bodyPanel.getWidth(), bodyPanel.getHeight());
        fxPanel.setBackground(Color.decode("#ffffff"));
        fxPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed  (MouseEvent e) {
                htmlPrintComponent();
            }
        });
        bodyPanel.add(fxPanel);


        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                // WebView setting
                webView = new WebView();
                webView.setContextMenuEnabled(false);
                webEngine = webView.getEngine();
                webEngine.getLoadWorker().stateProperty().addListener(
                        new ChangeListener<Worker.State>() {
                            @Override
                            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {

                                JSObject window = (JSObject)webEngine.executeScript("window");
                                window.setMember("console", console);

                                if(newState == Worker.State.SUCCEEDED){
                                    webEngine.executeScript("init('"+new String(addr)+"', '"+new String(privatekey)+"', '"+imgUrl1+"', '"+imgUrl2+"')");
                                }
                            }
                        }
                );

                // Path setup
                URL mainURL = this.getClass().getResource(INDEX_HTML_PATH);
                // Load URL from setting Path
                webEngine.load(mainURL.toExternalForm());
                fxPanel.setScene(new Scene(webView));

            }
        });
    }

    private class FrameDragListener extends MouseAdapter {
        private final JDialog dialog;
        private Point mouseDownCompCoords = null;
        private boolean mouseMoveFrameFlag = false;

        public FrameDragListener(JDialog dialog) {
            this.dialog = dialog;
        }

        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
            mouseMoveFrameFlag = false;
        }

        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
            if(mouseDownCompCoords.y >= 0 && mouseDownCompCoords.y <= HEADER_HEIGHT) {
                mouseMoveFrameFlag = true;
            }
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            if(mouseMoveFrameFlag) {
                dialog.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
        }
    }

    private class APISCloseButton extends JButton{
        private ImageIcon normalImageIcon, activeImageIcon;

        public APISCloseButton(){
            this.setBorder(BorderFactory.createEmptyBorder());
            this.setOpaque(true);

            // setting icon
            try {
                File file = new File(System.getProperty("user.dir") + "/apisj-core/src/main/resources/webView/img/new/btn_status_close_black.png");
                normalImageIcon = new ImageIcon(ImageIO.read(file));
                file = null;
                file = new File(System.getProperty("user.dir") + "/apisj-core/src/main/resources/webView/img/new/btn_status_close_white.png");
                activeImageIcon = new ImageIcon(ImageIO.read(file));
                file = null;
            } catch (Exception ex) {
                System.out.println("icon Error > "+ex);
            }
            this.setIcon(normalImageIcon);

            // add mouse event
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    APISCloseButton.this.setIcon(activeImageIcon);
                    APISCloseButton.this.setBackground(Color.decode("#910000"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    APISCloseButton.this.setIcon(normalImageIcon);
                    APISCloseButton.this.setBackground(null);
                }
            });

        }

    }

    private void htmlPrintComponent(){
        try {
            APISPrintDialog.this.printComponent(APISPrintDialog.this.fxPanel, false);
        } catch (PrinterException err) {
            err.printStackTrace();
        }
    }

    /* ======================================================
     *  Custom Printer Dialog
     * ====================================================== */
    private static class ComponentPrinter implements Printable {

        private Component comp;
        private boolean fill;

        public ComponentPrinter(Component comp, boolean fill) {
            this.comp = comp;
            this.fill = fill;
        }

        @Override
        public int print(Graphics g, PageFormat format, int page_index) throws PrinterException {

            if (page_index > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.translate(format.getImageableX(), format.getImageableY());

            double width = (int) Math.floor(format.getImageableWidth());
            double height = (int) Math.floor(format.getImageableHeight());

            if (!fill) {
                width = Math.min(width, comp.getPreferredSize().width);
                height = Math.min(height, comp.getPreferredSize().height);
            }

            comp.setBounds(0, 0, (int) Math.floor(width), (int) Math.floor(height));
            if (comp.getParent() == null) {
                comp.addNotify();
            }
            comp.validate();
            comp.doLayout();
            comp.printAll(g2);
            if (comp.getParent() != null) {
                //comp.removeNotify();
            }

            return Printable.PAGE_EXISTS;
        }

    }

    private void printComponent(Component comp, boolean fill) throws PrinterException {
        PrinterJob pjob = PrinterJob.getPrinterJob();
        PageFormat pf = pjob.defaultPage();
        pf.setOrientation(PageFormat.PORTRAIT); //or PageFormat.LANDSCAPE

        if(OSInfo.getOs() == OSInfo.OS.WINDOWS){
            // page setting
            Paper paper = new Paper();
            paper.setSize(8.3 * 72, 11.7 * 72);
            paper.setImageableArea(18, 18, 560, 214);
            pf.setPaper(paper);

            // dpi
            PrintRequestAttributeSet settings=new HashPrintRequestAttributeSet();
            PrinterResolution pr = new PrinterResolution(300, 300, ResolutionSyntax.DPI);
            PrintService ps = pjob.getPrintService();
            boolean resolutionSupported = ps.isAttributeValueSupported(pr, null, null);
            if (resolutionSupported) {
                System.out.println("Resolution is supported.\nTest is not applicable, PASSED");
            }
            settings.add(pr);
            settings.add(Fidelity.FIDELITY_TRUE);
        }
        else if(OSInfo.getOs() == OSInfo.OS.MAC){
        }

        // show dialog
        PageFormat postformat = pjob.pageDialog(pf);
        if (pf != postformat) {
            //Set print component
            pjob.setPrintable(new ComponentPrinter(comp, fill), postformat);
            if (pjob.printDialog()) {
                pjob.print();
            }
        }
    }


    private void printComponentToFile(Component comp, boolean fill) throws PrinterException {
        Paper paper = new Paper();
        paper.setSize(8.3 * 72, 11.7 * 72);
        paper.setImageableArea(18, 18, 559, 783);

        PageFormat pf = new PageFormat();
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.LANDSCAPE);

        BufferedImage img = new BufferedImage(
                (int) Math.round(pf.getWidth()),
                (int) Math.round(pf.getHeight()),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle(0, 0, img.getWidth(), img.getHeight()));
        ComponentPrinter cp = new ComponentPrinter(comp, fill);
        try {
            cp.print(g2d, pf, 0);
        } finally {
            g2d.dispose();
        }

        try {
            ImageIO.write(img, "png", new File("Page-" + (fill ? "Filled" : "") + ".png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}