package org.apis.gui.run;

import org.apis.gui.view.APISWalletGUI;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        APISWalletGUI gui = new APISWalletGUI();
        gui.start();

        //AppManager.getInstance().setApisWalletGUI(gui);
        //AppManager.getInstance().start();

//        try {
//            PrinterJob pjob = PrinterJob.getPrinterJob();
//            pjob.setJobName("Graphics Demo Printout");
//            pjob.setCopies(1);
//            pjob.setPrintable(new Printable() {
//                public int print(Graphics pg, PageFormat pf, int pageNum) {
//                    if (pageNum > 0) // we only print one page
//                        return Printable.NO_SUCH_PAGE; // ie., end of job
//
//
//                    for(int i=0; i<100; i++){
//                        pg.drawString("("+(10*i)+","+(10*i)+")", 10*i, 10*i);
//                    }
//
//                    return Printable.PAGE_EXISTS;
//                }
//            });
//
//            if (pjob.printDialog() == false) // choose printer
//                return;
//            pjob.print();
//        } catch (PrinterException pe) {
//            pe.printStackTrace();
//        }

    }
}
