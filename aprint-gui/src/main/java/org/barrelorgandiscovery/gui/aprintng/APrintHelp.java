package org.barrelorgandiscovery.gui.aprintng;

import static javafx.concurrent.Worker.State.FAILED;

import java.awt.BorderLayout;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class APrintHelp extends JFrame {

	public APrintHelp() throws Exception {
		super();

		initComponents();
	}

	private JFXPanel jfxPanel;
	private JLabel lblStatus;
	
	private JProgressBar progress;

	protected void initComponents() throws Exception {

		jfxPanel = new JFXPanel();

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(jfxPanel, BorderLayout.CENTER);

		lblStatus = new JLabel();
		getContentPane().add(lblStatus, BorderLayout.SOUTH);

	
		progress = new JProgressBar();
		getContentPane().add(progress, BorderLayout.SOUTH);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				createScene();
			}
		});

	}

	private WebEngine engine;

	private void createScene() {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				WebView view = new WebView();
				engine = view.getEngine();

				engine.titleProperty().addListener(
						new ChangeListener<String>() {
							@Override
							public void changed(
									ObservableValue<? extends String> observable,
									String oldValue, final String newValue) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										APrintHelp.this.setTitle(newValue);
									}
								});
							}
						});

				engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
					@Override
					public void handle(final WebEvent<String> event) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								lblStatus.setText(event.getData());
							}
						});
					}
				});

				engine.locationProperty().addListener(
						new ChangeListener<String>() {
							@Override
							public void changed(
									ObservableValue<? extends String> ov,
									String oldValue, final String newValue) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										// txtURL.setText(newValue);
									}
								});
							}
						});

				engine.getLoadWorker().workDoneProperty()
						.addListener(new ChangeListener<Number>() {
							@Override
							public void changed(
									ObservableValue<? extends Number> observableValue,
									Number oldValue, final Number newValue) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										progress.setValue(newValue.intValue());
									}
								});
							}
						});

				engine.getLoadWorker().exceptionProperty()
						.addListener(new ChangeListener<Throwable>() {

							@Override
							public void changed(
									ObservableValue<? extends Throwable> o,
									Throwable old, final Throwable value) {
								if (engine.getLoadWorker().getState() == FAILED) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane
													.showMessageDialog(
															APrintHelp.this,
															(value != null) ? engine
																	.getLocation()
																	+ "\n"
																	+ value.getMessage()
																	: engine.getLocation()
																			+ "\nUnexpected error.",
															"Loading error...",
															JOptionPane.ERROR_MESSAGE);
										}
									});
								}
							}
						});

				jfxPanel.setScene(new Scene(view));
			}
		});
	}

	public void navigate(final String url) {
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				engine.load(url);

			}
		});
	}
	


	public static void main(String[] ags) throws Exception {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					APrintHelp h = new APrintHelp();

					h.setSize(800, 600);
					h.setVisible(true);
					
					h.navigate("http://www.barrel-organ-discovery.org/site/doc/2017");
					//h.navigate("http://localhost:8080/");
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			};

		});

	}
}
