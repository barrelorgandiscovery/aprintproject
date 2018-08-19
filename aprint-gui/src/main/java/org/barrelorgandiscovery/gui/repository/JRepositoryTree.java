package org.barrelorgandiscovery.gui.repository;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.gui.ainstrument.RepositoryTreeListener;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.instrument.Instrument;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.FilteredRepository;
import org.barrelorgandiscovery.repository.FilteredRepositoryCollection;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Collection;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.repository.RepositoryChangedListener;
import org.barrelorgandiscovery.repository.RepositoryTreeFilter;
import org.barrelorgandiscovery.scale.Scale;

import com.birosoft.liquid.LiquidLookAndFeel;

public class JRepositoryTree extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4012787127931117139L;

	private Repository2 repository;

	private RepositoryTreeModel rtm;

	private RepositoryTreeListener listener = null;

	public void setRepositoryTreeListener(RepositoryTreeListener listener) {
		this.listener = listener;
	}

	public RepositoryTreeListener getRepositoryTreeListener() {
		return this.listener;
	}

	private static class ScalesNode {

		private RepositoryDisplayer d;

		public ScalesNode(RepositoryDisplayer parent) {
			this.d = parent;
		}

		@Override
		public String toString() {
			return Messages.getString("JRepositoryTree.0"); //$NON-NLS-1$
		}

		public RepositoryDisplayer getParentRepositoryDisplayer() {
			return d;
		}

	}

	private static class InstrumentsNode {

		private RepositoryDisplayer d;

		public InstrumentsNode(RepositoryDisplayer parent) {
			this.d = parent;
		}

		@Override
		public String toString() {
			return Messages.getString("JRepositoryTree.1"); //$NON-NLS-1$
		}

		public RepositoryDisplayer getParentRepositoryDisplayer() {
			return d;
		}
	}

	private static class InstrumentDisplayer {

		private Instrument instrument;

		public InstrumentDisplayer(Instrument instrument) {
			this.instrument = instrument;
		}

		public Instrument getInstrument() {
			return instrument;
		}

		@Override
		public String toString() {
			return instrument.getName();
		}

	}

	private static class ScaleDisplayer {
		private Scale scale;

		public ScaleDisplayer(Scale scale) {

			assert scale != null;

			this.scale = scale;
		}

		public Scale getScale() {
			return scale;
		}

		@Override
		public String toString() {
			return scale.getName();
		}

	}

	/**
	 * Class for Repository Display ...
	 * 
	 * @author Freydiere Patrice
	 * 
	 */
	private static class RepositoryDisplayer {

		private Repository2 r2ref = null;

		public RepositoryDisplayer(Repository2 r2) {
			this.r2ref = r2;
		}

		@Override
		public String toString() {
			return Messages.getString("JRepositoryTree.2") + (r2ref != null ? r2ref.getName() : ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		public Repository2 getRepository() {
			return r2ref;
		}

	}

	private class RepositoryTreeModel implements TreeModel,
			RepositoryChangedListener {

		private ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(l);
		}

		public void removeTreeModelListener(TreeModelListener l) {
			listeners.remove(l);
		}

		public Object getChild(Object parent, int index) {

			if (parent instanceof RepositoryDisplayer) {

				RepositoryDisplayer d = (RepositoryDisplayer) parent;
				if (d.getRepository() != null
						&& d.getRepository() instanceof Repository2Collection) {
					return new RepositoryDisplayer(((Repository2Collection) d
							.getRepository()).getRepository(index));

				} else {

					if (index == 0)
						// return new ScalesNode(d);
					// if (index == 1)
						return new InstrumentsNode(d);
				}

			} else if (parent instanceof ScalesNode) {

				ScalesNode s = (ScalesNode) parent;

				Repository2 repository2 = s.getParentRepositoryDisplayer()
						.getRepository();

				String[] scaleNames = repository2.getScaleNames();

				Arrays.sort(scaleNames, new StringComparator());

				Scale scale = repository2.getScale(scaleNames[index]);

				assert scale != null;

				return new ScaleDisplayer(scale);

			} else if (parent instanceof InstrumentsNode) {

				InstrumentsNode s = (InstrumentsNode) parent;

				Repository2 repository2 = s.getParentRepositoryDisplayer()
						.getRepository();

				Instrument[] listInstruments = repository2.listInstruments();
				
				// tri en fonction du nom de l'instrument
				Arrays.sort(listInstruments, new Comparator<Instrument>() {
					public int compare(Instrument o1, Instrument o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
				
				
				return new InstrumentDisplayer(listInstruments[index]);

			}

			return null;
		}

		public int getChildCount(Object parent) {

			if (parent instanceof RepositoryDisplayer) {
				RepositoryDisplayer d = (RepositoryDisplayer) parent;
				if (d.getRepository() != null
						&& d.getRepository() instanceof Repository2Collection) {
					return ((Repository2Collection) d.getRepository())
							.getRepositoryCount();
				} else {
					return 1; // 2
				}

			} else if (parent instanceof ScalesNode) {
				ScalesNode s = (ScalesNode) parent;
				return s.getParentRepositoryDisplayer().getRepository()
						.getScaleNames().length;

			} else if (parent instanceof InstrumentsNode) {
				InstrumentsNode s = (InstrumentsNode) parent;
				return s.getParentRepositoryDisplayer().getRepository()
						.listInstruments().length;
			}

			return 0;
		}

		public int getIndexOfChild(Object parent, Object child) {

			if (parent instanceof RepositoryDisplayer) {
				if (child instanceof ScalesNode)
					return 0;

				if (child instanceof InstrumentsNode)
					return 1;

				return -1;

			} else if (parent instanceof ScalesNode) {
				String[] scaleNames = repository.getScaleNames();
				Arrays.sort(scaleNames, new StringComparator());

				Scale child2 = ((ScaleDisplayer) child).getScale();

				for (int i = 0; i < scaleNames.length; i++) {
					String string = scaleNames[i];

					if (string.equals((child2).getName()))
						return i;
				}

				return -1;

			} else if (parent instanceof InstrumentsNode) {

				Instrument instrument = ((InstrumentDisplayer) child)
						.getInstrument();

				Instrument[] listInstruments = repository.listInstruments();
				for (int i = 0; i < listInstruments.length; i++) {
					if (listInstruments[i].getName().equals(
							(instrument).getName()))
						return i;

				}
				return -1;
			}

			return -1;
		}

		public Object getRoot() {
			return new RepositoryDisplayer(JRepositoryTree.this.repository);
		}

		public boolean isLeaf(Object node) {
			return (node instanceof ScaleDisplayer)
					|| (node instanceof InstrumentDisplayer);
		}

		public void valueForPathChanged(TreePath path, Object newValue) {

		}

		public void instrumentsChanged() {
			for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
				TreeModelListener listener = (TreeModelListener) iterator
						.next();
			}
		}

		public void scalesChanged() {
			// TODO Auto-generated method stub

		}

		public void transformationAndImporterChanged() {
			// TODO Auto-generated method stub

		}

	}

	private JTree tree;

	public JRepositoryTree() throws Exception {
		buildComponent();

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (listener != null) {
					Object lastSelectedPathComponent = tree
							.getLastSelectedPathComponent();

					if (lastSelectedPathComponent instanceof InstrumentDisplayer) {
						InstrumentDisplayer id = (InstrumentDisplayer) lastSelectedPathComponent;
						listener.repositoryObjectSelected(id.getInstrument());
					} else if (lastSelectedPathComponent instanceof RepositoryDisplayer) {
						RepositoryDisplayer rd = (RepositoryDisplayer) lastSelectedPathComponent;
						listener.repositoryObjectSelected(rd.getRepository());
					}
					if (lastSelectedPathComponent instanceof ScaleDisplayer) {
						ScaleDisplayer sd = (ScaleDisplayer) lastSelectedPathComponent;
						listener.repositoryObjectSelected(sd.getScale());
					}

				}

			}
		});

	}

	private void buildComponent() throws Exception {

		tree = new JTree();

		setLayout(new BorderLayout());
		add(new JScrollPane(tree), BorderLayout.CENTER);

	}

	private class RepositoryListener implements RepositoryChangedListener {
		public void instrumentsChanged() {
			changeModel();

		}

		public void scalesChanged() {
			changeModel();
		}

		public void transformationAndImporterChanged() {
			changeModel();
		}
	}

	private RepositoryListener currentRepositoryListener = null;

	public void setRepository(Repository2 repository) {

		if (this.repository != null && currentRepositoryListener != null) {
			this.repository
					.removeRepositoryChangedListener(currentRepositoryListener);
			currentRepositoryListener = null;
		}

		this.repository = repository;

		if (repository != null) {
			currentRepositoryListener = new RepositoryListener();
			repository.addRepositoryChangedListener(currentRepositoryListener);
		}

		changeModel();

	}

	private void changeModel() {
		rtm = new RepositoryTreeModel();
		tree.setModel(rtm);
	}

	/**
	 * Get the repository associated to the component ...
	 * 
	 * @return
	 */
	public Repository2 getRepository() {
		return this.repository;
	}

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure(new LF5Appender());

		javax.swing.UIManager
				.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel"); //$NON-NLS-1$
		LiquidLookAndFeel.setLiquidDecorations(true, "mac"); //$NON-NLS-1$
		// LiquidLookAndFeel.setStipples(false);
		LiquidLookAndFeel.setToolbarFlattedButtons(true);

		JFrame f = new JFrame();

		Properties p = new Properties();
		p.setProperty("repositorytype", "folder"); //$NON-NLS-1$ //$NON-NLS-2$
		p
				.setProperty("folder", //$NON-NLS-1$
						"C:\\Documents and Settings\\Freydiere Patrice\\workspace\\APrint\\gammes"); //$NON-NLS-1$

		APrintProperties aprintproperties = new APrintProperties(true);

		Repository2 repository = Repository2Factory.create(p, aprintproperties);

		JRepositoryTree t = new JRepositoryTree();

		Repository2 filtered = null;

		RepositoryTreeFilter treeFilter = new RepositoryTreeFilter() {
			public boolean keepInstrument(Instrument instrument) {

				return true;
			}

			public boolean keepScale(Scale scale) {
				if (scale == Scale.getGammeMidiInstance())
					return false;

				return true;
			}
		};

		if (repository instanceof Repository2Collection) {

			filtered = new FilteredRepositoryCollection(
					(Repository2Collection) repository, treeFilter);

		} else {

			filtered = new FilteredRepository(repository, treeFilter);
		}

		t.setRepository(filtered);

		t.setRepositoryTreeListener(new RepositoryTreeListener() {
			public void repositoryObjectSelected(Object object) {
				System.out.println("element selected :" + object); //$NON-NLS-1$
			}
		});

		f.getContentPane().add(t, BorderLayout.CENTER);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(400, 400);
		f.setVisible(true);

	}
}
