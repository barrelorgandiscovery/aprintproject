package org.barrelorgandiscovery.gui.issues;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.issues.AbstractIssue;
import org.barrelorgandiscovery.issues.IssueCollection;
import org.barrelorgandiscovery.issues.IssueCollectionListener;
import org.barrelorgandiscovery.issues.IssueLayer;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.AbstractScaleConstraint;
import org.barrelorgandiscovery.scale.ConstraintList;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.bugsreports.BugReporter;
import org.barrelorgandiscovery.ui.tools.VFSTools;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.checker.Checker;
import org.barrelorgandiscovery.virtualbook.checker.CheckerFactory;

/**
 * a simple panel for showing the issues
 * 
 * @author Freydiere Patrice
 * 
 */
public class JIssuePresenter extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2080804889381861595L;

	private static Logger logger = Logger.getLogger(JIssuePresenter.class);
	private JList list;
	private JScrollPane scrollPane;

	private static class IssueDisplayer implements Comparable<IssueDisplayer> {

		private AbstractIssue abstractIssue;
		private String label;

		public IssueDisplayer(AbstractIssue ai) {
			this.abstractIssue = ai;
		}

		public AbstractIssue getIssue() {
			return abstractIssue;
		}

		@Override
		public String toString() {
			return issueToString(abstractIssue);
		}

		public int compareTo(IssueDisplayer o) {
			return toString().compareTo(o.toString());
		}

	}

	public static String issueToString(AbstractIssue abstractIssue) {

		return abstractIssue.toLabel();
	}

	/**
	 * Text area with the check informations
	 */
	private JTextArea informations;

	private Object owner;

	private IssueRevalidateHook issueRevalidateHook;

	
	
	public JIssuePresenter(Object owner) {
		this(owner,true, true);
	}

	
	public JIssuePresenter(Object owner,boolean revalidatedVisible, boolean exportVisible) {
		super(new BorderLayout());

		this.owner = owner;

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(new TitledBorder(Messages
				.getString("JIssuePresenter.0"))); //$NON-NLS-1$

		list = new JList();

		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {

				if (listener != null) {

					IssueDisplayer selIssue = (IssueDisplayer) list
							.getSelectedValue();
					if (selIssue != null)
						listener.issueSelected(selIssue.getIssue());
				}
			}
		});

		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if (listener != null) {
						listener.issueDoubleClick(((IssueDisplayer) list
								.getSelectedValue()).getIssue());
					}
				}
			}
		});

		scrollPane = new JScrollPane(list);

		informations = new JTextArea();
		informations.setMinimumSize(new Dimension(50, 200));
		informations.setEditable(false);

		contentPanel.add(scrollPane, BorderLayout.CENTER);

		panelInfo = new JPanel();
		panelInfo.setLayout(new BorderLayout());
		panelInfo.add(informations, BorderLayout.CENTER);

		JButton revalidate = new JButton();
		revalidate.setText(Messages.getString("JIssuePresenter.30")); //$NON-NLS-1$
		revalidate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reCheckConstraints();
			}
		});
		if (!revalidatedVisible)
			revalidate.setVisible(false);
		
		panelInfo.add(revalidate, BorderLayout.SOUTH);

		contentPanel.add(panelInfo, BorderLayout.NORTH);

		JButton exportAsTextFile = new JButton(
				Messages.getString("JIssuePresenter.9")); //$NON-NLS-1$
		
		if (!exportVisible)
			exportAsTextFile.setVisible(false);
		
		exportAsTextFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					APrintFileChooser fileChooser = new APrintFileChooser();
					fileChooser.setFileFilter(new VFSFileNameExtensionFilter(
							Messages.getString("JIssuePresenter.10"), new String[] { "txt" })); //$NON-NLS-1$ //$NON-NLS-2$

					int showSaveDialog = fileChooser
							.showSaveDialog(JIssuePresenter.this);
					if (showSaveDialog == APrintFileChooser.APPROVE_OPTION) {
						logger.debug(Messages.getString("JIssuePresenter.12")); //$NON-NLS-1$

						AbstractFileObject selectedFile = fileChooser.getSelectedFile();
						String filename = selectedFile.getName().getBaseName();
						if (!filename.toLowerCase()
								.endsWith(".txt")) { //$NON-NLS-1$
							selectedFile = (AbstractFileObject)
									selectedFile.getFileSystem().resolveFile(selectedFile.getName().toString()
									+ ".txt"); //$NON-NLS-1$
						}

						logger.debug(Messages.getString("JIssuePresenter.15")); //$NON-NLS-1$

						OutputStream fos = VFSTools.transactionalWrite(selectedFile);
						try {

							OutputStreamWriter osw = new OutputStreamWriter(fos);
							try {

								osw.write(informations.getText());
								osw.write("\r\n"); //$NON-NLS-1$

								IssueCollection issueCollection = issueLayer
										.getIssueCollection();
								if (issueCollection != null) {
									for (AbstractIssue i : issueCollection) {
										osw.write(issueToString(i) + "\r\n"); //$NON-NLS-1$
									}
								}
							} finally {
								osw.close();
							}
						} finally {
							fos.close();
						}

						logger.debug("done !"); //$NON-NLS-1$

						JMessageBox
								.showMessage(
										JIssuePresenter.this.owner,
										Messages.getString("JIssuePresenter.19") //$NON-NLS-1$
												+ selectedFile
														.getName().toString()
												+ Messages
														.getString("JIssuePresenter.20")); //$NON-NLS-1$
					}

				} catch (Exception ex) {
					logger.error("error in exporting issues as text file :" //$NON-NLS-1$
							+ ex.getMessage(), ex);
					BugReporter.sendBugReport();
					JMessageBox.showMessage(JIssuePresenter.this.owner,
							Messages.getString("JIssuePresenter.22") //$NON-NLS-1$
									+ ex.getMessage());
				}
			}
		});

		add(contentPanel, BorderLayout.CENTER);
		add(exportAsTextFile, BorderLayout.SOUTH);
	}
	

	@Override
	public void setToolTipText(String text) {
		list.setToolTipText(text);
	}

	/**
	 * issue collection
	 */
	private IssueLayer issueLayer;

	private VirtualBook virtualBook = null;

	public void setVirtualBook(VirtualBook book) {

		this.virtualBook = book;

		refreshText();

	}

	private void refreshText() {
		if (issueLayer != null && virtualBook != null) {

			logger.debug("changing the information Area ... "); //$NON-NLS-1$

			StringBuilder sb = new StringBuilder();
			sb.append(Messages.getString("JIssuePresenter.6")); //$NON-NLS-1$
			sb.append("\n"); //$NON-NLS-1$
			sb.append("" + (issueLayer.getIssueCollection() != null ? issueLayer.getIssueCollection().size() : 0) //$NON-NLS-1$
					+ Messages.getString("JIssuePresenter.8")); //$NON-NLS-1$
			sb.append("\n"); //$NON-NLS-1$

			ConstraintList constraints = virtualBook.getScale()
					.getConstraints();
			if (constraints != null) {

				for (AbstractScaleConstraint abstractScaleConstraint : constraints) {
					sb.append("" + "-" + abstractScaleConstraint.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}

			}
			informations.setText(sb.toString());
		}
	}

	/**
	 * Define the issue collection shown in the panel
	 * 
	 * @param issues
	 */
	public void setIssueLayer(IssueLayer issueLayer) {

		// @@@ remove listener ...

		this.issueLayer = issueLayer;

		issueLayer.addIssueCollectionListener(new IssueCollectionListener() {
			public void issuesChanged(IssueCollection ic) {
				reloadIssues(JIssuePresenter.this.issueLayer
						.getIssueCollection());
			}
		});

		IssueCollection issueCollection = this.issueLayer.getIssueCollection();

		reloadIssues(issueCollection);

	}

	public void loadIssues(IssueCollection issueCollection) {
		reloadIssues(issueCollection);
	}

	public void setInfoPanelVisible(boolean isVisible) {
		this.panelInfo.setVisible(isVisible);
	}

	/**
	 * Get the issue Layer associated to the component
	 * 
	 * @return
	 */
	public IssueLayer getIssueLayer() {
		return issueLayer;
	}

	private void reloadIssues(IssueCollection issueCollection) {

		logger.debug(Messages.getString("JIssuePresenter.32")); //$NON-NLS-1$

		Vector<IssueDisplayer> v = new Vector<IssueDisplayer>();

		if (issueCollection != null) {

			TreeSet<IssueDisplayer> t = new TreeSet<IssueDisplayer>();
			for (AbstractIssue abstractIssue : issueCollection) {
				t.add(new IssueDisplayer(abstractIssue));
			}

			v.addAll(t);
		}

		list.setListData(v);

		refreshText();

		scrollPane.revalidate();
	}

	private IssueSelectionListener listener = null;

	private JPanel panelInfo;

	public void setIssueSelectionListener(IssueSelectionListener listener) {
		this.listener = listener;
	}

	/**
	 * 
	 */
	protected void reCheckConstraints() {
		try {

			if (virtualBook == null || issueLayer == null)
				return;

			// base checkers

			Checker[] chkrs = CheckerFactory.createCheckers(virtualBook
					.getScale());
			Checker composite = CheckerFactory.toComposite(chkrs);
			IssueCollection ic = composite.check(virtualBook);

			if (issueRevalidateHook != null) {
				issueRevalidateHook.addAdditionalChecks(virtualBook, ic);
			}

			issueLayer.setIssueCollection(ic, virtualBook);

			reloadIssues(ic);

		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			JMessageBox.showMessage(JIssuePresenter.this.owner,
					Messages.getString("JIssuePresenter.31") //$NON-NLS-1$
							+ t.getMessage());
		}
	}

	public void setIssueRevalidateHook(IssueRevalidateHook issueRevalidateHook) {
		this.issueRevalidateHook = issueRevalidateHook;
	}

	public IssueRevalidateHook getIssueRevalidateHook() {
		return issueRevalidateHook;
	}

}
