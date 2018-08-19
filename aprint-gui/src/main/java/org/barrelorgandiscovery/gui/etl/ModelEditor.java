package org.barrelorgandiscovery.gui.etl;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.JobEvent;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.etl.states.CellState;
import org.barrelorgandiscovery.gui.etl.states.ModelStateChecker;
import org.barrelorgandiscovery.gui.etl.steps.GuiConfigureModelStepRegistry;
import org.barrelorgandiscovery.gui.etl.steps.JConfigurePanelEnvironment;
import org.barrelorgandiscovery.gui.script.groovy.IScriptConsole;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.model.Model;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelRunner;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.ModelStepRegistry;
import org.barrelorgandiscovery.model.TerminalParameterModelStep;
import org.barrelorgandiscovery.model.execution.IModelExecutionListener;
import org.barrelorgandiscovery.model.steps.book.VirtualBookDemultiplexer;
import org.barrelorgandiscovery.model.steps.book.VirtualBookMultiplexer;
import org.barrelorgandiscovery.model.steps.scripts.GroovyScriptModelStep;
import org.barrelorgandiscovery.model.type.JavaType;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.Repository2Factory;
import org.barrelorgandiscovery.tools.BeanAsk;
import org.barrelorgandiscovery.tools.SwingUtils;
import org.barrelorgandiscovery.virtualbook.VirtualBook;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiEventGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxGraphModel.Filter;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.util.mxCellOverlay;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;

//import test.org.barrelorgandiscovery.model.TestProcessor;

public class ModelEditor extends JPanel {

  /** logger */
  private static Logger logger = Logger.getLogger(ModelEditor.class);

  /** */
  protected JTabbedPane libraryPane;

  /** */
  protected mxUndoManager undoManager = new mxUndoManager();

  /** */
  protected mxIEventListener undoHandler =
      new mxIEventListener() {
        public void invoke(Object source, mxEventObject evt) {
          undoManager.undoableEditHappened((mxUndoableEdit) evt.getProperty("edit")); //$NON-NLS-1$
        }
      };

  /** Selection options in the graph */
  protected mxRubberband rubberband;
  /** Keyboard commands */
  protected mxKeyboardHandler keyboardHandler;

  /** Registry for the graph model steps */
  protected ModelStepRegistry registry;

  /** Object for passing from JGraphX Model to application Model */
  protected ModelBridge bridge = new ModelBridge();

  /** Graph component */
  protected mxGraphComponent graphComponent;

  /** Graph model */
  private mxGraph mxGraph;

  /** Outer pane */
  private JSplitPane outer;

  /** Current repository */
  private Repository2 repository2;

  /** script Console */
  private IScriptConsole console;

  private AsyncJobsManager asyncJobsManager;

  /**
   * Connectivity helper
   *
   * @author pfreydiere
   */
  private class MyMultiplicity extends mxMultiplicity {

    public MyMultiplicity() {
      super(false, null, null, null, 10, "10", null, Messages.getString("ModelEditor.0"), null, true); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public String check(
        com.mxgraph.view.mxGraph graph,
        Object edge,
        Object source,
        Object target,
        int sourceOut,
        int targetIn) {

      logger.debug("checking " + source + " target " + target); //$NON-NLS-1$ //$NON-NLS-2$

      mxCell e = (mxCell) edge;
      mxCell s = (mxCell) source;
      mxCell t = (mxCell) target;

      try {

        Object ms = bridge.fromMxCell(s);
        Object mt = bridge.fromMxCell(t);

        if (!(ms instanceof ModelParameter) && !(mt instanceof ModelParameter)) {
          return Messages.getString("ModelEditor.5"); //$NON-NLS-1$
        }

        ModelParameter psource = (ModelParameter) ms;
        ModelParameter ptarget = (ModelParameter) mt;

        if (!(psource.isOut() && ptarget.isIn())) {
          return Messages.getString("ModelEditor.6") + Messages.getString("ModelEditor.7"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // OK, on a un flux out -> in

        mxICell parents = s.getParent();
        mxICell parentt = t.getParent();

        if (parents == parentt) {
          return Messages.getString("ModelEditor.8"); //$NON-NLS-1$
        }

        // vérification des types de paramètres

        if (!ptarget.getType().isAssignableFrom(psource.getType())) {
          return Messages.getString("ModelEditor.9") //$NON-NLS-1$
              + psource.getType()
              + Messages.getString("ModelEditor.10") //$NON-NLS-1$
              + ptarget.getType();
        }

        return ""; // ok we can connect //$NON-NLS-1$

      } catch (Exception ex) {
        logger.error("error in evaluating connectivity :" + ex.getMessage(), ex); //$NON-NLS-1$
        return Messages.getString("ModelEditor.13"); //$NON-NLS-1$
      }
    }
  }

  /** steps may want access to live objects for interacting with environment */
  private Map<String, Object> contextAmbiantObjects = null;

  /**
   * constructor with no ambiant objects
   *
   * @param registry
   * @param repository2
   */
  public ModelEditor(
      ModelStepRegistry registry, Repository2 repository2, AsyncJobsManager asyncJobManager)
      throws Exception {
    this(registry, repository2, asyncJobManager, null);
  }

  /**
   * Constructor
   *
   * @param registry the modelsteps registry
   */
  public ModelEditor(
      ModelStepRegistry registry,
      Repository2 repository2,
      AsyncJobsManager asyncJobManager,
      Map<String, Object> contextAmbiantObjects)
      throws Exception {

    assert registry != null;
    this.registry = registry;

    assert repository2 != null;
    this.repository2 = repository2;

    assert asyncJobManager != null;
    this.asyncJobsManager = asyncJobManager;

    // may be null
    this.contextAmbiantObjects = contextAmbiantObjects;

    setLayout(new BorderLayout());

    mxGraph = new EtlMxGraph();
    mxGraph.setAllowLoops(false);
    mxGraph.setAllowDanglingEdges(false);
    mxGraph.setMultigraph(false);

    ensureGraphMultiplicity();

    graphComponent = new mxGraphComponent(mxGraph);
    // graphComponent.setConnectable(true);
    graphComponent.setToolTips(true);

    addDefaultStyleSheet();

    // Adds the command history to the model and view
    mxGraph.getModel().addListener(mxEvent.UNDO, undoHandler);
    mxGraph.getView().addListener(mxEvent.UNDO, undoHandler);

    // Keeps the selection in sync with the command history
    mxIEventListener undoHandler =
        new mxIEventListener() {
          public void invoke(Object source, mxEventObject evt) {
            List<mxUndoableChange> changes =
                ((mxUndoableEdit) evt.getProperty("edit")).getChanges(); //$NON-NLS-1$
            mxGraph.setSelectionCells(mxGraph.getSelectionCellsForChanges(changes));
          }
        };

    // double click handler

    graphComponent
        .getGraphControl()
        .addMouseListener(
            new MouseAdapter() {

              @Override
              public void mouseClicked(MouseEvent e) {
                try {
                  if (e.getClickCount() >= 2) {
                    Object cell = graphComponent.getCellAt(e.getX(), e.getY());

                    if (cell != null) {
                      if (cell instanceof mxCell) {

                        mxCell mxcell = (mxCell) cell;

                        onDoubleClickOnValidCell(mxcell);
                      }
                    }
                  }

                } catch (Exception ex) {
                  logger.error("error in handling click :" + ex.getMessage(), ex); //$NON-NLS-1$
                }
              }
            });

    // forbid remove parameters

    graphComponent.getGraphHandler().setRemoveCellsFromParent(false);

    graphComponent.setFoldingEnabled(false);
    graphComponent.setGridVisible(true);

    undoManager.addListener(mxEvent.UNDO, undoHandler);
    undoManager.addListener(mxEvent.REDO, undoHandler);

    rubberband = new mxRubberband(graphComponent);
    keyboardHandler = new mxKeyboardHandler(graphComponent);

    // Creates the library pane that contains the tabs with the palettes
    libraryPane = new JTabbedPane();

    outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, libraryPane, graphComponent);
    outer.setOneTouchExpandable(true);
    outer.setDividerLocation(200);
    outer.setDividerSize(6);
    outer.setBorder(null);

    add(outer, BorderLayout.CENTER);

    EditorPalette shapesPalette = insertPalette(Messages.getString("ModelEditor.16")); //$NON-NLS-1$

    // create the elements in the palette

    List<ModelStep> rms = registry.getRegisteredModelStepList();
    if (rms != null) {
      for (Iterator iterator = rms.iterator(); iterator.hasNext(); ) {

        ModelStep modelStep = (ModelStep) iterator.next();
        try {
          mxCell c = bridge.convertModelStep(modelStep);
          ImageIcon ic = bridge.getAssociatedIcon(modelStep);
          if (ic == null) {
            ic = new ImageIcon(APrintNG.getAPrintApplicationIcon());
          }
          // c.setGeometry(new mxGeometry(0, 0, 64, 64));
          shapesPalette.addTemplate(c.getAttribute("label", Messages.getString("ModelEditor.18")), ic, c); //$NON-NLS-1$ //$NON-NLS-2$

        } catch (Exception ex) {
          logger.error(
              "error in creating modelstep cell :" + modelStep + ":" + ex.getMessage(), ex); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // shapesPalette.addEdgeTemplate("Test", null, "", 20, 20, 10);

      }
    }
  }

  /** ensure the graph has controlled connectivity element */
  protected void ensureGraphMultiplicity() {
    mxGraph.setMultiplicities(new mxMultiplicity[] {new MyMultiplicity()});
  }

  private static class ToReconnect {

    // from the deleted source --> to other
    public boolean isSource;

    public String removedCellParamName;
    public String destParamCellId;
  }

  @FunctionalInterface
  private static interface UIWorkWithLog {
    public void exec() throws Exception;
  }

  private void execInUIThread(UIWorkWithLog r) {
    if (SwingUtilities.isEventDispatchThread()) {
      try {
        r.exec();
      } catch (Exception ex) {
        logger.error("error in executing " + r + " :" + ex.getMessage(), ex); //$NON-NLS-1$ //$NON-NLS-2$
      }
      return;
    }

    SwingUtilities.invokeLater(
        () -> {
          try {
            r.exec();
          } catch (Exception ex) {
            logger.error("error in executing " + r + " :" + ex.getMessage(), ex); //$NON-NLS-1$ //$NON-NLS-2$
          }
        });
  }

  /**
   * Used when the configuration of a STEP has changed and we must replace the existing step with
   * the new one
   *
   * @param cellToReplace
   * @param step
   * @throws Exception
   */
  protected void replaceModelStep(mxCell cellToReplace, ModelStep step) throws Exception {

    // recreate the step mxCell,

    graphComponent.startEditing();
    try {

      mxCell newCell = bridge.convertModelStep(step);

      // set the new geometry
      // adjust geometry, position

      mxGeometry oldGeometry = cellToReplace.getGeometry();
      mxGeometry newGeometry = newCell.getGeometry();
      mxGeometry adaptedGeometry =
          new mxGeometry(
              oldGeometry.getX(),
              oldGeometry.getY(),
              newGeometry.getWidth(),
              newGeometry.getHeight());
      newCell.setGeometry(adaptedGeometry);

      // remember the connected links on the cells

      ArrayList<ToReconnect> connectedEdges = new ArrayList<>();
      recurseGetCellsEdges(cellToReplace, connectedEdges);

      // this remove all connection, so we have to replace them afterward
      mxGraph.removeCells(new Object[] {cellToReplace});

      mxGraph.addCell(newCell);

      // redefine the links

      for (ToReconnect c : connectedEdges) {
        reconnect(newCell, c);
      }

    } finally {
      graphComponent.stopEditing(false);
    }

    // try to reconnect all the elements , or adapt the connections

    // and we're done

  }

  private void reconnect(mxICell newCell, ToReconnect reconnect) {

    mxICell existingCell = findCellById(reconnect.destParamCellId);
    assert existingCell != null;

    // get the cell associated to the paramName;
    // reconnect.removedCellParamName

    // get all childs
    ArrayList<mxICell> potentialChilds = new ArrayList<>();
    for (int i = 0; i < newCell.getChildCount(); i++) {
      mxICell child = newCell.getChildAt(i);
      potentialChilds.add(child);
    }

    Object[] newParam =
        mxGraphModel.filterCells(
            potentialChilds.toArray(new mxICell[potentialChilds.size()]),
            new Filter() {
              @Override
              public boolean filter(Object cell) {
                if (cell == null) return false;
                if (!(cell instanceof mxICell)) {
                  return false;
                }

                mxICell c = (mxICell) cell;
                if (c.getValue() == null) return false;

                if (!(c.getValue() instanceof Element)) return false;

                Element e = (Element) c.getValue();
                String name = e.getAttribute("name"); //$NON-NLS-1$
                if (reconnect.removedCellParamName.equals(name)) return true;

                return false;
              }
            });

    if (newParam.length == 0) {
      logger.debug("parameter remove, can't reconnect"); //$NON-NLS-1$
      return;
    }

    assert newParam.length <= 1;

    mxICell newParamCell = (mxICell) newParam[0];

    mxICell source = newParamCell;
    mxICell target = existingCell;

    if (!reconnect.isSource) {
      mxICell tmp = source;
      source = target;
      target = tmp;
    }

    Object edge = mxGraph.createEdge(mxGraph.getCurrentRoot(), null, null, source, target, null);

    mxGraph.addEdge(edge, mxGraph.getDefaultParent(), source, target, null);
  }

  private mxICell findCellById(final String id) {
    if (id == null) return null;

    Collection<Object> res =
        mxGraphModel.filterDescendants(
            mxGraph.getModel(),
            new Filter() {
              @Override
              public boolean filter(Object cell) {
                if (!(cell instanceof mxICell)) {
                  return false;
                }
                mxICell c = (mxICell) cell;
                return id.equals(c.getId());
              }
            });

    assert res.size() <= 1;
    if (res.size() == 0) return null;

    mxICell c = (mxICell) res.iterator().next();
    return c;
  }

  /**
   * @param cellToReplace
   * @param collectedEdges
   */
  private void recurseGetCellsEdges(mxICell cellToReplace, ArrayList<ToReconnect> collectedEdges) {
    assert collectedEdges != null;

    for (int c = 0; c < cellToReplace.getChildCount(); c++) {
      mxICell child = cellToReplace.getChildAt(c);
      recurseGetCellsEdges(child, collectedEdges);
    }

    for (int i = 0; i < cellToReplace.getEdgeCount(); i++) {
      mxICell edge = cellToReplace.getEdgeAt(i);

      mxICell source = edge.getTerminal(true);
      mxICell target = edge.getTerminal(false);

      ToReconnect r = new ToReconnect();

      if (source.getParent() == cellToReplace.getParent()) {
        // from the deleted source --> to other
        r.isSource = true;

      } else {
        // to the deleted source
        r.isSource = false;

        // reverse to get the elements
        mxICell tmp = source;
        source = target;
        target = tmp;
      }

      r.removedCellParamName = ((Element) source.getValue()).getAttribute("name"); //$NON-NLS-1$
      r.destParamCellId = target.getId();

      collectedEdges.add(r);
    }
  }

  /** */
  public EditorPalette insertPalette(String title) {

    final EditorPalette palette = new EditorPalette();
    final JScrollPane scrollPane = new JScrollPane(palette);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    libraryPane.add(title, scrollPane);

    // Updates the widths of the palettes if the container size changes
    libraryPane.addComponentListener(
        new ComponentAdapter() {

          /** */
          public void componentResized(ComponentEvent e) {
            int w = scrollPane.getWidth() - scrollPane.getVerticalScrollBar().getWidth();
            if (w > 0) palette.setPreferredWidth(w);
          }
        });

    return palette;
  }

  // define the cell style and associated overlays
  private void changeStyle(Object cell, Map<String, CellState> newStyles) {
    logger.debug("introspect cell :" + cell); //$NON-NLS-1$
    if (cell instanceof mxCell) {
      mxCell m = (mxCell) cell;

      String id = m.getId();
      if (newStyles.containsKey(id)) {
        CellState cellState = newStyles.get(id);
        String style = cellState.getCellStyle();
        logger.debug("put style " + style + " for element :" + id); //$NON-NLS-1$ //$NON-NLS-2$
        m.setStyle(style);
        if (cellState.isError()) {

          mxCellOverlay mxcellOverlay =
              new mxCellOverlay(
                  new ImageIcon(CellState.class.getResource("messagebox_warning.png")), style); //$NON-NLS-1$

          mxcellOverlay.setVerticalAlign(mxConstants.ALIGN_TOP);

          // register actios to overlays
          mxcellOverlay.addMouseListener(
              new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                  logger.debug("overlay click"); //$NON-NLS-1$
                }
              });

          graphComponent.addCellOverlay(m, mxcellOverlay);
        }

      } else {
        logger.debug("no style for element :" + id); //$NON-NLS-1$
      }

      for (int i = 0; i < m.getChildCount(); i++) {
        changeStyle(m.getChildAt(i), newStyles);
      }
    }
  }

  /**
   * Test Routine for graph creation
   *
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    Logger.getRootLogger().setLevel(Level.DEBUG);

    BasicConfigurator.configure(new LF5Appender());

    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setSize(1200, 800);

    Properties p = new Properties();
    p.setProperty("folder", "C:\\Users\\use\\aprintstudio-beta\\private"); //$NON-NLS-1$ //$NON-NLS-2$

    APrintProperties aprintprops = new APrintProperties(true);
    AsyncJobsManager async = new AsyncJobsManager();

    final Repository2 r = Repository2Factory.create(p, aprintprops);

    final ModelEditor me =
        new ModelEditor(
            new ModelStepRegistry() {

              public List<ModelStep> getRegisteredModelStepList() {

                try {
                  ArrayList<ModelStep> ms = new ArrayList<ModelStep>();

                  ms.add(
                      new TerminalParameterModelStep(
                          true, new JavaType(MidiEventGroup.class), "Midi", "Midi", null)); //$NON-NLS-1$ //$NON-NLS-2$

                  ms.add(
                      new TerminalParameterModelStep(
                          false,
                          new JavaType(VirtualBook.class),
                          "VirtualBook", //$NON-NLS-1$
                          "VirtualBook", //$NON-NLS-1$
                          null));

                  ms.add(new VirtualBookMultiplexer());

                  ms.add(new VirtualBookDemultiplexer());

                  ms.add(
                      new TerminalParameterModelStep(
                          false, new JavaType(String.class), "String", "String", null)); //$NON-NLS-1$ //$NON-NLS-2$
                  // try {
                  //
                  // ms.add(new ObjectMethodStep(TestProcessor.class,
                  // "executeMethod"));
                  //
                  // } catch (Exception ex) {
                  // ex.printStackTrace(System.err);
                  // }

                  ms.add(new GroovyScriptModelStep());

                  return ms;
                } catch (Exception ex) {
                  logger.error("error in creating steps :" + ex.getMessage(), ex);
                  throw new RuntimeException(ex);
                }
              }
            },
            r,
            async);
    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(me, BorderLayout.CENTER);

    JToolBar tb = new JToolBar();
    JButton check = new JButton(Messages.getString("ModelEditor.43")); //$NON-NLS-1$
    tb.add(check);

    check.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            try {

              me.validateState();

            } catch (Exception ex) {
              logger.error("error in constructing graph :" + ex.getMessage(), ex); //$NON-NLS-1$
            }
          }
        });

    JButton load = new JButton("Load"); //$NON-NLS-1$
    tb.add(load);
    load.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try {
              me.loadGraph(new File("c:\\temp\\test.xml")); //$NON-NLS-1$

            } catch (Exception ex) {
              ex.printStackTrace(System.err);
            }
          }
        });

    JButton save = new JButton("Save"); //$NON-NLS-1$
    tb.add(save);

    save.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try {

              me.saveGraph(new File("c:\\temp\\test.xml")); //$NON-NLS-1$

            } catch (Exception ex) {
              ex.printStackTrace(System.err);
            }
          }
        });

    JButton undo = new JButton("undo"); //$NON-NLS-1$
    undo.setAction(
        new AbstractAction("undo") { //$NON-NLS-1$
          public void actionPerformed(ActionEvent e) {
            me.undo();
          }
        });
    tb.add(undo);

    JButton redo = new JButton("redo"); //$NON-NLS-1$
    redo.setAction(
        new AbstractAction("redo") { //$NON-NLS-1$
          public void actionPerformed(ActionEvent e) {
            me.redo();
          }
        });
    tb.add(redo);

    tb.add(new JButton(me.bind("Zoom In", mxGraphActions.getZoomInAction(), (String) null))); //$NON-NLS-1$
    tb.add(new JButton(me.bind("Zoom Out", mxGraphActions.getZoomOutAction(), (String) null))); //$NON-NLS-1$
    tb.add(
        new JButton(me.bind("Zoom Actual", mxGraphActions.getZoomActualAction(), (String) null))); //$NON-NLS-1$

    f.getContentPane().add(tb, BorderLayout.NORTH);

    // add terminal for the model
    me.addTerminal(
        new TerminalParameterModelStep(
            true, new JavaType(VirtualBook.class), "virtualBook", "label", null)); //$NON-NLS-1$ //$NON-NLS-2$

    f.setVisible(true);
  }

  public void undo() {
    undoManager.undo();
  }

  public void redo() {
    undoManager.redo();
  }

  public void load(InputStream inputStream) throws Exception {
    if (inputStream == null) return;

    mxCodec c = new mxCodec();
    DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
    DocumentBuilder docb = newInstance.newDocumentBuilder();

    Document doc = docb.parse(inputStream);

    com.mxgraph.view.mxGraph graph = graphComponent.getGraph();

    ((mxGraphModel) graph.getModel()).clear();
    c.decode(doc.getDocumentElement(), graph);

    // configure the graph multiplicity
    ensureGraphMultiplicity();

    addDefaultStyleSheet();

    graph.clearSelection();
    graph.refresh();
    graphComponent.refresh();
    graphComponent.zoomAndCenter();
  }

  public void loadGraph(File file) throws Exception {

    logger.debug("read XML"); //$NON-NLS-1$

    FileInputStream fis = new FileInputStream(file);
    try {
      load(fis);
    } finally {
      fis.close();
    }
  }

  /** */
  protected void addDefaultStyleSheet() {
    // add stylesheet
    // Loads the default stylesheet from an external file
    mxCodec codec = new mxCodec();
    Document docStyle =
        mxUtils.loadDocument(ModelEditor.class.getResource("default-style.xml").toString()); //$NON-NLS-1$
    codec.decode(docStyle.getDocumentElement(), graphComponent.getGraph().getStylesheet());
  }

  public void newGraph() throws Exception {
    // reset the graph

    com.mxgraph.view.mxGraph graph = graphComponent.getGraph();

    ((mxGraphModel) graph.getModel()).clear();

    // configure the graph multiplicity
    ensureGraphMultiplicity();

    addDefaultStyleSheet();

    graph.clearSelection();
    graph.refresh();
    graphComponent.refresh();
    graphComponent.zoomAndCenter();
  }

  public void saveGraph(File file) throws Exception {
    mxCodec c = new mxCodec();

    Node n = c.encode(graphComponent.getGraph()); // save and
    // output
    // in
    // XML

    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();
    t.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
    t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //$NON-NLS-1$ //$NON-NLS-2$

    FileOutputStream outputStream = new FileOutputStream(file);

    t.transform(new DOMSource(n), new StreamResult(outputStream));

    outputStream.close();
  }

  // this validate the current display state of the graph
  /**
   * this validate the current model and returned the error list
   *
   * @return
   * @throws Exception
   */
  public List<String> validateState() throws Exception {

    // validation des connections, connections manquantes
    Model m = bridge.constructModelFromGraph(mxGraph);
    m.schedule();

    ModelStateChecker msc = new ModelStateChecker();
    m.visit(msc);

    Map<String, CellState> states = msc.getStates();

    // visit the cells and put the new styles ....

    graphComponent.startEditing();
    try {
      graphComponent.clearCellOverlays();
      changeStyle(mxGraph.getDefaultParent(), states);
    } finally {
      graphComponent.stopEditing(false);
    }

    graphComponent.refresh();
    return msc.getErrors();
  }

  public void setModelExecutionListener(ModelExecutionListener modelExecutionListener) {
    this.modelExecutionListener = modelExecutionListener;
  }

  ////////////////////////////////////////////////////////////////////////
  // execution methods

  private ModelExecutionListener modelExecutionListener;

  /**
   * Execute the current model
   *
   * @throws Exception if problem occur
   */
  public void execute(Map<String, Object> terminalInputValues) throws Exception {

    logger.debug("execute the current model"); //$NON-NLS-1$

    ModelRunner modelRunner = createCurrentModelRunner();

    modelRunner.setModelExecutionListener(
        new IModelExecutionListener() {

          long start;

          @Override
          public void stepExecuting(ModelStep step) {
            try {
              if (console != null) {
                execInUIThread(
                    () -> {
                      console.appendOutputNl(Messages.getString("ModelEditor.64") + step.getLabel(), null); //$NON-NLS-1$
                    });
              }
            } catch (Exception ex) {
              logger.error(ex);
            }
          }

          @Override
          public void stepExecuted(ModelStep step) {
            try {
              if (console != null) {
                execInUIThread(
                    () -> {
                      console.appendOutputNl(Messages.getString("ModelEditor.65") + step.getLabel() + Messages.getString("ModelEditor.66"), null); //$NON-NLS-1$ //$NON-NLS-2$
                    });
              }
            } catch (Exception ex) {
              logger.error(ex);
            }
          }

          @Override
          public void startExecuteModel() {
            try {
              start = System.currentTimeMillis();
              if (console != null) {
                execInUIThread(
                    () -> {
                      console.appendOutputNl(Messages.getString("ModelEditor.67"), null); //$NON-NLS-1$
                    });
              }
            } catch (Exception ex) {
              logger.error(ex);
            }
          }

          @Override
          public void endExecuteModel() {
            try {
              if (console != null) {
                execInUIThread(
                    () -> {
                      console.appendOutputNl(Messages.getString("ModelEditor.68"), null); //$NON-NLS-1$

                      console.appendOutputNl(
                          String.format(
                              Messages.getString("ModelEditor.69"), //$NON-NLS-1$
                              (System.currentTimeMillis() - start) / 1000.0),
                          null);
                    });
              }
            } catch (Exception ex) {
              logger.error(ex);
            }
          }
        });

    logger.debug("hydrate context variables"); //$NON-NLS-1$

    modelRunner.getModel().hydrateContext(contextAmbiantObjects);

    logger.debug("hydrate done, execute"); //$NON-NLS-1$
    try {

      asyncJobsManager.submitAndExecuteJob(
          () -> {
            modelRunner.execute(terminalInputValues, null); // no console
            // for
            // instance

            return null;
          },
          new JobEvent() {

            @Override
            public void jobFinished(Object result) {}

            @Override
            public void jobError(Exception ex) {
              logger.error("error in executing the jobs :" + ex.getMessage(), ex); //$NON-NLS-1$
              execInUIThread(
                  () -> {
                    console.appendOutput(ex);
                  });
            }

            @Override
            public void jobAborted() {
              execInUIThread(
                  () -> {
                    console.appendOutput(Messages.getString("ModelEditor.73"), null); //$NON-NLS-1$
                  });
            }
          });

    } catch (Exception ex) {
      if (console != null) {
        console.appendOutput(ex);
      }
    }

    // adjust visual state
    if (modelExecutionListener != null) {
      modelExecutionListener.executed(modelRunner);
    }
  }

  /**
   * Handling event on double click on element on the graph
   *
   * @param mxcell the cell on wich the double click occur
   * @throws Exception
   */
  protected void onDoubleClickOnValidCell(final mxCell mxcell) throws Exception {

    if (mxcell.isVertex()) {
      editStepConfiguration(mxcell);
    }
  }

  /**
   * Edition des paramètres de configuration
   *
   * @param mxcell cellule editée
   * @throws Exception
   */
  protected void editStepConfiguration(final mxCell mxcell) throws Exception {

    Object from = bridge.fromMxCell(mxcell);
    if (!(from instanceof ModelStep)) {
      return;
    }

    final ModelStep ms = (ModelStep) from;

    assert ms != null;

    JConfigurePanelEnvironment env = new JConfigurePanelEnvironment(ModelEditor.this.repository2);
    
    final JConfigurePanel panel = GuiConfigureModelStepRegistry.getUIToConfigureStep(ms,  env);

    final BeanAsk parameterDialog =
        new BeanAsk((Frame) null, Messages.getString("ModelEditor.74") + ms.getLabel(), true); //$NON-NLS-1$

    Container contentPane = parameterDialog.getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(panel, BorderLayout.CENTER);

    JButton ok = new JButton(Messages.getString("APrint.240")); //$NON-NLS-1$
    ok.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {

            try {
              try {
                if (!panel.apply()) {
                  // don't close the panel
                  return;
                }

                parameterDialog.setVisible(false);
                logger.debug(" - OK parameters modified - "); //$NON-NLS-1$
                parameterDialog.dispose();

              } catch (Exception ex) {
                logger.error("error in editing configure parameters :" + ex.getMessage(), ex); //$NON-NLS-1$
              }

              // apply configuration
              ms.applyConfig();

              // replace the element in the graph with the new
              // configured step

              replaceModelStep(mxcell, ms);

            } catch (Exception ex) {
              logger.error("error in handling changing configuration : " + ex.getMessage(), ex); //$NON-NLS-1$
            }
          }
        });

    contentPane.add(ok, BorderLayout.SOUTH);
    parameterDialog.setSize(400, 300);
    SwingUtils.center(parameterDialog);

    parameterDialog.setModal(true);
    parameterDialog.setVisible(true);
  }

  /** Current executor of the model */
  protected ModelRunner currentModelRunner;

  /**
   * Create a new model and model runner
   *
   * @return the current model runner
   * @throws Exception
   */
  protected ModelRunner createCurrentModelRunner() throws Exception {

    Model m = bridge.constructModelFromGraph(mxGraph);
    m.schedule();

    ModelRunner modelRunner = new ModelRunner(m);
    this.currentModelRunner = modelRunner;
    return modelRunner;
  }

  /** do we have a current model in executing mode ? */
  protected boolean hasCurrentModelRunner() {
    return currentModelRunner != null;
  }

  /** reset and release the current model runner */
  protected void invalidateCurrentModelRunner() {
    currentModelRunner = null;
  }

  /**
   * bind action to graph component source
   *
   * @param name
   * @param action
   * @param iconUrl
   * @return
   */
  public Action bind(String name, final Action action, String iconUrl) {
    return new AbstractAction(
        name, (iconUrl != null) ? new ImageIcon(ModelEditor.class.getResource(iconUrl)) : null) {
      public void actionPerformed(ActionEvent e) {
        action.actionPerformed(new ActionEvent(graphComponent, e.getID(), e.getActionCommand()));
      }
    };
  }

  public Action bind(String shortDescription, final Action action, Icon iconUrl) {
    AbstractAction aa =
        new AbstractAction("", (iconUrl != null) ? iconUrl : null) { //$NON-NLS-1$
          public void actionPerformed(ActionEvent e) {
            action.actionPerformed(
                new ActionEvent(graphComponent, e.getID(), e.getActionCommand()));
          }
        };
    aa.putValue(Action.SHORT_DESCRIPTION, shortDescription);

    return aa;
  }

  /**
   * add a terminal to the graph
   *
   * @param terminalParameterModelStep
   */
  public void addTerminal(TerminalParameterModelStep terminalParameterModelStep) throws Exception {

    mxCell cell = bridge.convertModelStep(terminalParameterModelStep);

    graphComponent.startEditing();
    try {

      // mxGeometry adaptedGeometry = new mxGeometry(oldGeometry.getX(),
      // oldGeometry.getY(), newGeometry.getWidth(),
      // newGeometry.getHeight());

      cell.setStyle("terminalgroup"); //$NON-NLS-1$
      mxGraph.addCell(cell);

    } finally {
      graphComponent.stopEditing(false);
    }
  }

  /**
   * get a terminal value by its name
   *
   * @param name
   * @return
   * @throws Exception
   */
  public Object getExecutedTerminalValueByName(String name) throws Exception {

    if (currentModelRunner == null) return null;

    Model m = currentModelRunner.getModel();
    TerminalParameterModelStep t = m.getOutTerminalByName(name);
    if (t == null) {
      logger.warn("terminal " + name + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }

    return t.getValue();
  }

  public void setConsole(IScriptConsole console) {
    this.console = console;
  }

  public IScriptConsole getConsole() {
    return console;
  }
}
