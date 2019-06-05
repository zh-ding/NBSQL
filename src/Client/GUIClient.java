package Client;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class GUIClient {

    JFrame frame;
    JTextField tf;
    JTextPane tp, history;
    Client c = null;
    enum textType  {
        REGULAR,
        ERROR,
        INFO
    }

    public GUIClient() {
        this.frame = new JFrame("NBSQL");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(960,720);
        JMenuBar mb = new JMenuBar();

        JMenu m0 = new JMenu("Server");
        JMenu m1 = new JMenu("File");
        JMenu m2 = new JMenu("Help");
        mb.add(m0);
        mb.add(m1);
        mb.add(m2);
        JMenuItem m00 = new JMenuItem("Connect");
        JMenuItem m01 = new JMenuItem("DisConnect");
        m0.add(m00);
        m0.add(m01);
        JMenuItem m11 = new JMenuItem("Import sql file");
        m1.add(m11);
        JMenuItem m20 = new JMenuItem("About NBSQL");
        m2.add(m20);

        m00.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createConnectDialog();
            }
        });

        m01.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                c.close();
                c = null;
            }
        });

        m20.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame,"NBSqlDB is a simple database written by TFMen.", "About NBSQL", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        m11.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(".");
                chooser.showOpenDialog(frame);
                String filePath = chooser.getSelectedFile().getAbsolutePath();

                File f = new File(filePath);

                try {
                    FileInputStream fis = new FileInputStream(f);
                    char current;
                    while(fis.available() > 0) {
                        String sql = "";
                        while (fis.available() > 0) {
                            current = (char) fis.read();
                            if(current == '\n')
                                continue;
                            sql = sql + current;
                            if (current == ';')
                                break;
                        }
                        c.sendSql(sql);
                        textType tt = textType.REGULAR;
                        while(true){
                            try {
                                String text = c.receiveText();
                                if(text.equals("over"))
                                    break;
                                if(text.equals("over"))
                                    break;
                                if(text.startsWith("@")){
                                    appendText(text.substring(1), textType.INFO);
                                }else if(text.startsWith("!")){
                                    tt = textType.ERROR;
                                    appendText(text.substring(1), textType.ERROR);
                                }else{
                                    appendText(text, textType.REGULAR);
                                }
                            }catch (IOException e2){
                                System.out.println(e2);
                            }
                        }

                        appendSql(sql, tt);
                    }
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        });

        JPanel panel = new JPanel(); // the panel is not visible in output
        JLabel label = new JLabel("Enter SQL");
        this.tf = new JTextField(70); // accepts upto 10 characters

        JButton send = new JButton("Enter");
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String sql = "";
                textType tt = textType.REGULAR;
                try{
                    sql = tf.getText();
                    c.sendSql(sql);
                    while(true){
                        String text = c.receiveText();
                        System.out.println(text);
                        if(text.equals("over"))
                            break;
                        if(text.startsWith("@")){
                            appendText(text.substring(1), textType.INFO);
                        }else if(text.startsWith("!")){
                            tt = textType.ERROR;
                            appendText(text.substring(1), textType.ERROR);
                        }else{
                            appendText(text, textType.REGULAR);
                        }
                    }
                }catch (IOException e1){
                    System.out.println(e1);
                    appendText("Connection Error\n", textType.ERROR);
                }

                appendSql(sql, tt);
            }
        });
        JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tf.setText("");
            }
        });
        panel.add(label); // Components Added using Flow Layout
        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(send);
        panel.add(reset);

        JPanel center = new JPanel(new GridBagLayout());

        this.tp = new JTextPane();
        JScrollPane jsp1 = new JScrollPane(this.tp);

        this.history = new JTextPane();
        JScrollPane jsp2 = new JScrollPane(this.history);

        this.frame.getContentPane().add(BorderLayout.NORTH, mb);
        this.frame.getContentPane().add(BorderLayout.SOUTH, panel);
        this.frame.getContentPane().add(BorderLayout.CENTER, jsp1);
        this.frame.getContentPane().add(BorderLayout.EAST, jsp2);
        jsp2.setPreferredSize(new Dimension(frame.getWidth()/3,frame.getHeight()));
        this.frame.setVisible(true);

        createConnectDialog();
    }

    void createConnectDialog(){
        ConnectDialog cd = new ConnectDialog(this.frame);
        if(cd.isOK){
            try {
                this.c = new Client(cd.ip, cd.port);
                this.appendText("Connected to " + cd.ip + ":" + cd.port + "\n", textType.INFO);
            }catch (IOException e){
                this.appendText("Connection refused\n", textType.ERROR);
            }
        }
    }

    void appendText(String s, textType t){
        StyledDocument doc = this.tp.getStyledDocument();
        switch (t){
            case REGULAR: {
                try {
                    doc.insertString(doc.getLength(), s, null);
                } catch (Exception e1) {
                    System.out.println(e1);
                }
                break;
            }
            case ERROR: {
                SimpleAttributeSet keyWord = new SimpleAttributeSet();
                StyleConstants.setForeground(keyWord, Color.RED);

                try {
                    doc.insertString(doc.getLength(), s, keyWord);
                } catch (Exception e1) {
                    System.out.println(e1);
                }
                break;
            }
            case INFO: {
                SimpleAttributeSet keyWord = new SimpleAttributeSet();
                StyleConstants.setForeground(keyWord, Color.GREEN);

                try {
                    doc.insertString(doc.getLength(), s, keyWord);
                } catch (Exception e1) {
                    System.out.println(e1);
                }
                break;
            }
        }
    }

    void appendSql(String s, textType t){
        StyledDocument doc = this.history.getStyledDocument();
        switch (t){
            case REGULAR: {
                try {
                    doc.insertString(doc.getLength(), s + "\n", null);
                } catch (Exception e1) {
                    System.out.println(e1);
                }
                break;
            }
            case ERROR: {
                SimpleAttributeSet keyWord = new SimpleAttributeSet();
                StyleConstants.setForeground(keyWord, Color.RED);

                try {
                    doc.insertString(doc.getLength(), s + "\n", keyWord);
                } catch (Exception e1) {
                    System.out.println(e1);
                }
                break;
            }
        }
    }

    public static void main(String[] args){
        GUIClient gc = new GUIClient();
    }
}
