package org.barrelorgandiscovery.extensionsng.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

public class TestJConsole {

	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		frame.setSize(800,600);
		frame.getContentPane().setLayout(new BorderLayout());
		
		JConsole console = new JConsole();
		console.setBlinkDelay(1000);
		console.setCursorBlink(true);
		console.setAutoscrolls(true);
		console.setBackground(Color.black);
		console.setForeground(Color.white);
		console.setCursorPos(0, 0);
		
		frame.getContentPane().add(console, BorderLayout.CENTER);
		
		JButton btn = new JButton("button");
		btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e);
			
				console.write("buttssssssssssssssssss\nssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssson", Color.white, Color.black);
				console.repaint();
			}
		});
		frame.getContentPane().add(btn, BorderLayout.SOUTH);
		// frame.setFocusable(true);
		// console.setFocusable(true);
		console.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(e);
				console.write(e.getKeyChar());
			
			}
			@Override
			public void keyTyped(KeyEvent e) {
				
				System.out.println(e);
				console.write(e.getKeyChar());
			}
			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println(e);
				console.write(e.getKeyChar());
			
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

}
