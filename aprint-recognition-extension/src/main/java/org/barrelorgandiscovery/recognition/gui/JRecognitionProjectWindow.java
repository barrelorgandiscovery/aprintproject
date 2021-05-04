package org.barrelorgandiscovery.recognition.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.ICancelTracker;
import org.barrelorgandiscovery.gui.aedit.CreationTool;
import org.barrelorgandiscovery.gui.aedit.ImageAndHolesVisualizationLayer;
import org.barrelorgandiscovery.gui.aedit.JEditableVirtualBookComponent;
import org.barrelorgandiscovery.gui.aedit.Tool;
import org.barrelorgandiscovery.gui.aedit.UndoStack;
import org.barrelorgandiscovery.gui.aedit.snapping.HolesSnappingEnvironnement;
import org.barrelorgandiscovery.gui.aprintng.APrintNGInternalFrame;
import org.barrelorgandiscovery.prefs.DummyPrefsStorage;
import org.barrelorgandiscovery.prefs.IPrefsStorage;
import org.barrelorgandiscovery.recognition.RecognitionProject;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.virtualbook.Hole;
import org.barrelorgandiscovery.virtualbook.VirtualBook;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

/**
 * Project window for book recognition and digit
 * 
 * @author pfreydiere
 * 
 */
public class JRecognitionProjectWindow extends APrintNGInternalFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4123987665125294334L;

	/**
	 * logger
	 */
	private static Logger logger = Logger
			.getLogger(JRecognitionProjectWindow.class);

	/**
	 * recognition project
	 */
	private RecognitionProject rp;

	/**
	 * Layer for visualizing the book image
	 */
	private ImageAndHolesVisualizationLayer iv;

	/**
	 * image previewer
	 */
	private JTileRecognitionImages ri;

	private JEditableVirtualBookComponent sc;

	public JRecognitionProjectWindow(IPrefsStorage prefsStorage,RecognitionProject rp) throws Exception {
		super(prefsStorage);
		this.rp = rp;
		ri.setRecognitionProject(rp);

		sc.setVirtualBook(rp.getVirtualBook());

	}

	@Override
	protected void initializeComponents() throws Exception {
		super.initializeComponents();

		getContentPane().setLayout(new BorderLayout());

		FormPanel fp = new FormPanel(getClass().getResourceAsStream(
				"recognition.jfrm"));

		sc = new JEditableVirtualBookComponent();

		getContentPane().add(fp, BorderLayout.CENTER);

		FormAccessor fa = fp.getFormAccessor();
		fa.replaceBean("vbcomponent", sc);

		iv = new ImageAndHolesVisualizationLayer();

		sc.addLayer(iv);

		ri = new JTileRecognitionImages();

		fa.replaceBean("imagescomponent", ri);

		ri.setRecognitionImageSelectListener(new RecognitionImageSelectListener() {

			public void imageSelected(String name) throws Exception {

				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					displayImage(name);
				} finally {
					setCursor(Cursor.getDefaultCursor());
				}
			}
		});

		JButton btn = (JButton) fp.getComponentByName("ar");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					final String[] name = ri.getAllSelected();
					if (name == null)
						return;

					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try {

						final ExecutorService es = Executors
								.newSingleThreadExecutor();

						final AtomicReference<Boolean> cancel = new AtomicReference<Boolean>(
								Boolean.FALSE);

						Runnable r = new Runnable() {

							public void run() {

								for (int i = 0; i < name.length; i++) {

									if (cancel.get())
										break;

									String n = name[i];
									try {
										infiniteChangeText("Recognize " + n);
										rp.recognize(n);
									} catch (Exception ex) {
										logger.error("error recognizing " + n);
									}
								}
								try {
									displayImage(ri.getCurrentSelected());
								} catch (Exception ex) {
									logger.error("fail to display image "
											+ ri.getCurrentSelected());
								}
								setCursor(Cursor.getDefaultCursor());
								infiniteEndWait();

								es.shutdownNow();
							}
						};

						infiniteStartWait("Reconnaissance des images",
								new ICancelTracker() {
									public boolean cancel() {
										cancel.set(Boolean.TRUE);
										return cancel.get();
									}

									public boolean isCanceled() {
										return cancel.get();
									}
								});

						es.submit(r);

					} finally {

					}

				} catch (Exception ex) {
					logger.error("error in recognizing " + ex.getMessage(), ex);
					JMessageBox.showError(this, ex);
				}
			}
		});

		final CreationTool ct = new CreationTool(sc, new UndoStack(),
				new HolesSnappingEnvironnement(sc));

		JButton btnCreationTool = (JButton) fp.getComponentByName("tools");
		btnCreationTool.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sc.setCurrentTool(ct);

			}
		});

		final Tool t = new Tool() {

			@Override
			public void activated() {
				setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			}

			@Override
			public void unactivated() {
				setCursor(Cursor.getDefaultCursor());
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				try {
					double cs = sc.convertScreenXToCarton(e.getX());

					if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
						double delta = ((double) iv.getXoffset() - cs) / 100.0;
						iv.setXscale(delta);
						rp.setHolesXOffset(ri.getCurrentSelected(),
								iv.getXscale());
					} else {
						iv.setXoffset(cs);
						rp.setImageOffset(ri.getCurrentSelected(), cs);
					}

					sc.repaint();

				} catch (Exception ex) {
					logger.error("error in define offset " + ex.getMessage(),
							ex);
					JMessageBox.showError(this, ex);
				}
			}

			@Override
			public void mouseWheel(MouseWheelEvent e) {
				try {

					int wheelRotation = e.getWheelRotation();

					sc.repaint();

				} catch (Exception ex) {
					logger.error("error in define scale " + ex.getMessage(), ex);
					JMessageBox.showError(this, ex);
				}
			}

		};

		JButton btnScaleOffset = (JButton) fp.getComponentByName("cip");
		btnScaleOffset.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				sc.setCurrentTool(t);
			}
		});

		JButton btnsave = (JButton) fp.getComponentByName("save");
		btnsave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {

					rp.saveVirtualBook();

				} catch (Exception ex) {
					logger.error("error in define offset " + ex.getMessage(),
							ex);
					JMessageBox.showError(this, ex);
				}

			}
		});

		JButton mergeHoles = (JButton) fp.getComponentByName("mergeholes");
		mergeHoles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					VirtualBook virtualBook = sc.getVirtualBook();

					ArrayList<Hole> holes = iv.getHoles();
					long l = sc.MMToTime(iv.getXoffset());
					if (holes != null) {
						for (Hole h : holes) {

							virtualBook.addAndMerge(h.newHoleWithOffset(l));
						}
					}

					sc.repaint();

				} catch (Exception ex) {
					logger.error("error in define offset " + ex.getMessage(),
							ex);
					JMessageBox.showError(this, ex);
				}
			}
		});

		JButton editEdges = (JButton) fp.getComponentByName("editedges");
		editEdges.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				String imageName = ri.getCurrentSelected();

				if (imageName == null)
					return;

				try {
					EdgeEditingWindow edgeEditingWindow = new EdgeEditingWindow(new DummyPrefsStorage(),
							rp.getImage(imageName), rp.getEdges(imageName));

					edgeEditingWindow.setSize(600, 400);

					edgeEditingWindow.show(true);
					
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					JMessageBox.showError(this, ex);
					BugReporter.sendBugReport();
				}
			}
		});

	}

	/**
	 * @param name
	 * @throws Exception
	 */
	protected void displayImage(String name) throws Exception {
		logger.debug("change image");

		iv.setBackgroundimage(rp.computeWarpImage(name));
		iv.setHoles(rp.getHoles(name));

		Double ro = rp.getImageOffset(name);
		if (ro != null) {
			iv.setXoffset(ro);
		}
		sc.repaint();
	}
}
