package com.liwux.io.V2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientFrame extends Frame {
    TextArea textArea = new TextArea();
    TextField textField = new TextField();

    public ClientFrame(){
        this.setSize(600,400);
        this.setLocation(100,20);
        this.add(textArea,BorderLayout.CENTER);
        this.add(textField,BorderLayout.SOUTH);
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(textArea.getText()+textField.getText());
                textField.setText("");
            }
        });

        this.setVisible(true);
        new Client().connect();

    }

    public static void main(String[] args) {
        new ClientFrame();
    }
}
