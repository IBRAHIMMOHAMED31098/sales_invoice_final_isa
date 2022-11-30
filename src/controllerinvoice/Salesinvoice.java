package controllerinvoice;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.HeadlessException;
import sales.view.Invoicehead2;
import sales.view.JFrame;
import sales.view.InvoiceLine2;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import Invoicemodel.InvoiceHeade;
import Invoicemodel.InvoiceModel2;
import Invoicemodel.InvoiceLine;
import Invoicemodel.Invoice_Table;


public class Salesinvoice implements ActionListener, ListSelectionListener {

    private final JFrame Frame;
    private Invoicehead2 invoice_D;
    private InvoiceLine2 line_D;

    public Salesinvoice(JFrame frame) {
        this.Frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent a) {
        String actionCommand = a.getActionCommand();
        
        switch (actionCommand) {
            case "Load File":
                lf();
                break;
            case "Create New Invoice":
                cni();
                break;
            case "Delete Invoice":
                di();
                break;
            case "Create New Item":
                cnitem();
                break;
            case "Delete Item":
                ditem();
                break;
            case "createInvoiceCancel":
                cic();
                break;
            case "createInvoiceAdd":
                cio();
                break;
            case "createLineAdd":
                clo();
                break;
            case "createLineCancel":
                clc();
                break;
            case "Save File":
                SF();
                break;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent m) {
        int selectedIndex = Frame.getInvoiceTable().getSelectedRow();
        if (selectedIndex != -1) {
          
            InvoiceHeade currentInvoice = Frame.getInvc().get(selectedIndex);
            Frame.getInvoiceNumLabel().setText("" + currentInvoice.getIdNumber());
            Frame.getInvoiceDateLabel1().setText("" + currentInvoice.getInvoiceDate());
            Frame.getCustomerNameLabel().setText(currentInvoice.getCustomerName());
            Frame.getInvoiceTotalLabel().setText("" + currentInvoice.getInvoiceTotal());
            Invoice_Table linesTableModel = new Invoice_Table(currentInvoice.getLines());
            Frame.getLineTable().setModel(linesTableModel);
            linesTableModel.fireTableDataChanged();
        }
    }

    private void lf() {
        JFileChooser filechooser = new JFileChooser();

        try {
            JOptionPane.showMessageDialog(Frame, "Select Invoice Header File",
                    "Information Message", JOptionPane.INFORMATION_MESSAGE);
            int res = filechooser.showOpenDialog(Frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                File hf = filechooser.getSelectedFile();
                Path hp = Paths.get(hf.getAbsolutePath());
                List<String> headerLines = Files.readAllLines(hp);
                System.out.println("Invoices have been read");
                ArrayList<InvoiceHeade> ia = new ArrayList<>();
                for (String headerLine : headerLines) {
                    try {
                        String[] hps = headerLine.split(",");
                        int invoiceN = Integer.parseInt(hps[0]);
                        String invoiceD = hps[1];
                        String customer_Name = hps[2];

                        InvoiceHeade invc = new InvoiceHeade(invoiceN, invoiceD, customer_Name);
                        ia.add(invc);
                    } catch (NumberFormatException excp) {
                        JOptionPane.showMessageDialog(Frame, "Error in line format", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
                JOptionPane.showMessageDialog(Frame, "Select Invoice Line File",
                        "Information Message", JOptionPane.INFORMATION_MESSAGE);
                res = filechooser.showOpenDialog(Frame);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File lin_File = filechooser.getSelectedFile();
                    Path lin_Path = Paths.get(lin_File.getAbsolutePath());
                    List<String> LL = Files.readAllLines(lin_Path);
                    System.out.println("Lines have been read");
                    for (String lineLine : LL) {
                        try {
                            String[] lps = lineLine.split(",");
                            int invcNum = Integer.parseInt(lps[0]);
                            String itm_Name = lps[1];
                            double itm_Price = Double.parseDouble(lps[2]);
                            int count = Integer.parseInt(lps[3]);
                            InvoiceHeade inv = null;
                            for (InvoiceHeade invoice : ia) {
                                if (invoice.getIdNumber() == invcNum) {
                                    inv = invoice;
                                    break;
                                }
                            }

                            InvoiceLine l = new InvoiceLine(itm_Name, itm_Price, count, inv);
                            inv.getLines().add(l);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(Frame, "Error in line format", "Error", JOptionPane.ERROR_MESSAGE);
                    
                        }
                    }
                    
                }
                Frame.setInvoices(ia);
                InvoiceModel2 invoicesTableModel = new InvoiceModel2(ia);
                Frame.setInvoicesTableModel(invoicesTableModel);
                Frame.getInvoiceTable().setModel(invoicesTableModel);
                Frame.getInvcTableModel().fireTableDataChanged();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(Frame, "Cannot read file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void SF() {
        ArrayList<InvoiceHeade> invcs = Frame.getInvc();
        String headers = "";
        String lines = "";
        for (InvoiceHeade invoice : invcs) {
            String invCSV = invoice.getAsCSV();
            headers += invCSV;
            headers += "\n";

            for (InvoiceLine line : invoice.getLines()) {
                String lineCSV = line.getAsCSV();
                lines += lineCSV;
                lines += "\n";
            }
        }
        System.out.println("Check point");
        
        try {
            JFileChooser filechooser = new JFileChooser();
            int res = filechooser.showSaveDialog(Frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                File headerFile = filechooser.getSelectedFile();
                try (FileWriter fr = new FileWriter(headerFile)) {
                    fr.write(headers);
                    fr.flush();
                }
                res = filechooser.showSaveDialog(Frame);
                JOptionPane.showMessageDialog(Frame, "File saved successfully",
           "Information Message", JOptionPane.INFORMATION_MESSAGE);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File lineFile = filechooser.getSelectedFile();
                    try (FileWriter linefilew = new FileWriter(lineFile)) {
                        linefilew.write(lines);
                        linefilew.flush();
                    }
                }
            }
        } catch (HeadlessException | IOException ex) {
            

        }
    }

    private void cni() {
        invoice_D = new Invoicehead2(Frame);
        invoice_D.setVisible(true);
    }

    private void di() {
        int selectedRow = Frame.getInvoiceTable().getSelectedRow();
        if (selectedRow != -1) {
            Frame.getInvc().remove(selectedRow);
            Frame.getInvcTableModel().fireTableDataChanged();
        }
    }

    private void cnitem() {
        line_D = new InvoiceLine2(Frame);
        line_D.setVisible(true);
    }

    private void ditem() {
        int sr = Frame.getLineTable().getSelectedRow();

        if (sr != -1) {
            Invoice_Table linesTableModel = (Invoice_Table) Frame.getLineTable().getModel();
            linesTableModel.getLines().remove(sr);
            linesTableModel.fireTableDataChanged();
            Frame.getInvcTableModel().fireTableDataChanged();
        }
    }

    private void cic() {
        invoice_D.setVisible(false);
        invoice_D.dispose();
        invoice_D = null;
    }

    private void cio() {
        String date = invoice_D.getInvDateField().getText();
        String customer = invoice_D.getCustNameField().getText();
        int num = Frame.getNextInvoiceNum();
        try {
            String[] dateParts = date.split("-");  // 
            if (dateParts.length < 3) {
                JOptionPane.showMessageDialog(Frame, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int d = Integer.parseInt(dateParts[0]);
                int m = Integer.parseInt(dateParts[1]);
                int y = Integer.parseInt(dateParts[2]);
                if (d > 31 || m > 12) {
                    JOptionPane.showMessageDialog(Frame, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    InvoiceHeade invoice = new InvoiceHeade(num, date, customer);
                    Frame.getInvc().add(invoice);
                    Frame.getInvcTableModel().fireTableDataChanged();
                    invoice_D.setVisible(false);
                    invoice_D.dispose();
                    invoice_D = null;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(Frame, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void clo() {
        String it = line_D.getItemNameField().getText();
        String co = line_D.getItemCountField().getText();
        String prs = line_D.getItemPriceField().getText();
        int count = Integer.parseInt(co);
        double price = Double.parseDouble(prs);
        int selectedInvoice = Frame.getInvoiceTable().getSelectedRow();
        if (selectedInvoice != -1) {
            InvoiceHeade invoice = Frame.getInvc().get(selectedInvoice);
            InvoiceLine l = new InvoiceLine(it, price, count, invoice);
            invoice.getLines().add(l);
            Invoice_Table linesTableModel = (Invoice_Table) Frame.getLineTable().getModel();
            linesTableModel.fireTableDataChanged();
            Frame.getInvcTableModel().fireTableDataChanged();
        }
        line_D.setVisible(false);
        line_D.dispose();
        line_D = null;
    }

    private void clc() {
        line_D.setVisible(false);
        line_D.dispose();
        line_D = null;
    }

}
