package Client;

import javax.swing.*;
import java.awt.*;

public class ConnectDialog {
    boolean isOK = false;
    String ip;
    int port;

    ConnectDialog(Frame f) {

        JTextField xField = new JTextField(15);
        JTextField yField = new JTextField(4);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("ip:"));
        myPanel.add(xField);
        myPanel.add(Box.createHorizontalStrut(15)); // a spacer
        myPanel.add(new JLabel("port:"));
        myPanel.add(yField);

        int result = JOptionPane.showConfirmDialog(f, myPanel,
                "Please Enter Server ip and port", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            this.isOK = true;
            this.ip = xField.getText();
            try {
                this.port = Integer.parseInt(yField.getText());
            }catch (Exception e){
                this.port = 0;
            }
        }
    }
}