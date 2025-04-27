package org.barrelorgandiscovery.gui.etl;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.model.Model;
import org.barrelorgandiscovery.model.ModelLink;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelStep;
import org.barrelorgandiscovery.model.xml.SDGroovyModelStep;
import org.barrelorgandiscovery.model.xml.SDMidiDemultiplexer;
import org.barrelorgandiscovery.model.xml.SDMidiFileInput;
import org.barrelorgandiscovery.model.xml.SDModelParameter;
import org.barrelorgandiscovery.model.xml.SDNewBookFrame;
import org.barrelorgandiscovery.model.xml.SDObjectMethodStep;
import org.barrelorgandiscovery.model.xml.SDTerminalParameterModelStep;
import org.barrelorgandiscovery.model.xml.SDVirtualBookDemultiplexer;
import org.barrelorgandiscovery.model.xml.SDVirtualBookMultiplexer;
import org.barrelorgandiscovery.model.xml.XmlSerContext;
import org.barrelorgandiscovery.tools.ImageTools;
import org.barrelorgandiscovery.tools.StringTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

/**
 * Bridge class between JGraphX and APrint Framework
 *
 * @author pfreydiere
 */
public class ModelBridge {

	private static Logger logger = Logger.getLogger(ModelBridge.class);

	private XmlSerContext ctx;

	public ModelBridge() {

		ctx = new XmlSerContext();
		ctx.register(new SDModelParameter());
		ctx.register(new SDVirtualBookMultiplexer());
		ctx.register(new SDVirtualBookDemultiplexer());

		ctx.register(new SDTerminalParameterModelStep());
		ctx.register(new SDObjectMethodStep());

		ctx.register(new SDMidiDemultiplexer());
		ctx.register(new SDMidiFileInput());

		ctx.register(new SDNewBookFrame());

		ctx.register(new SDGroovyModelStep());
	}

	protected Object fromMxCell(mxCell cell) throws Exception {
		Element e = (Element) cell.getValue();

		if (e == null)
			return null;

		// add id on the element
		Element cloneElement = (Element) e.cloneNode(true);
		cloneElement.setAttribute("id", cell.getId()); //$NON-NLS-1$

		return ctx.fromXML(cloneElement);
	}

	/**
	 * Try load the model object, return null if it fails
	 *
	 * @param cell
	 * @return
	 */
	protected Object tryLoadObjectFromCell(mxCell cell) {

		if (cell == null)
			return null;

		try {
			return fromMxCell(cell);
		} catch (Exception ex) {
			logger.error("error loading cell :" + ex.getMessage(), ex); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * convert a step into the mx graph cell
	 *
	 * @param step
	 * @return
	 * @throws Exception
	 */
	protected mxCell convertModelStep(ModelStep step) throws Exception {

		if (step == null)
			return null;

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder docbuilder = fact.newDocumentBuilder();
		String x = "<?xml version=\"1.0\" ?><step />"; //$NON-NLS-1$
		Document doc = docbuilder.parse(new ByteArrayInputStream(x.getBytes()));
		Element el = doc.getDocumentElement();

		try {
			el = ctx.toXML(step, el);
		} catch (Exception ex) {
			logger.error("error while serializing to xml step :" + step + " :" + ex.getMessage(), ex); //$NON-NLS-1$ //$NON-NLS-2$
			throw new Exception(ex.getMessage(), ex);
		}

		// Main Cell
		mxCell cell = new mxCell(el);
		cell.setConnectable(false);

		// compute the width
		double width = 150.0;

		{
			// adjust the width for ease the reading
			ModelParameter[] params = step.getAllParametersByRef();
			for (int i = 0; i < params.length; i++) {
				ModelParameter modelParameter = params[i];
				boolean isIn = modelParameter.isIn();
				String paramLabel = modelParameter.getLabel();

				double labelWidth = (paramLabel != null ? paramLabel.length() : 0) * 7;
				width = Math.max(width, labelWidth);
			}
		}

		cell.setGeometry(new mxGeometry(0.0, 0.0, width, width));
		cell.setVertex(true);
		cell.setStyle(StyleConstants.GROUP); // $NON-NLS-1$

		int icpt = 0;
		int ocpt = 0;

		double cellwidth = 10.0;

		ModelParameter[] params = step.getAllParametersByRef();
		for (int i = 0; i < params.length; i++) {

			ModelParameter modelParameter = params[i];
			try {
				double xpos = 10.0;
				int ypos;

				mxCell connected = convertParam(modelParameter);

				boolean isIn = modelParameter.isIn();
				if (isIn) {
					icpt++;
					ypos = icpt;
				} else {
					ocpt++;
					xpos = width - xpos - 10.0;
					ypos = ocpt;
				}

				connected.setGeometry(new mxGeometry(xpos, ypos * 20.0, 10.0, 10.0));

				cell.insert(connected);
				connected.setVertex(true);
				if (isIn) {
					connected.setStyle(StyleConstants.INPARAM);
				} else {
					connected.setStyle(StyleConstants.OUTPARAM);
				}
			} catch (Exception ex) {
				logger.error("error while converting parameter :" + modelParameter + " :" + ex.getMessage(), ex); //$NON-NLS-1$ //$NON-NLS-2$
				throw ex;
			}
		}
		cell.getGeometry().setHeight(2 * cellwidth * Math.max(icpt, ocpt) + 2 * cellwidth);
		cell.setAttribute("label", step.getLabel()); //$NON-NLS-1$

		return cell;
	}

	/*
	 *
	 */
	protected mxCell convertParam(ModelParameter param) throws Exception {
		if (param == null)
			return null;

		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
		DocumentBuilder docbuilder = fact.newDocumentBuilder();
		String x = "<?xml version=\"1.0\" ?><param/>"; //$NON-NLS-1$
		Document doc = docbuilder.parse(new ByteArrayInputStream(x.getBytes()));
		Element el = doc.getDocumentElement();

		try {
			el = ctx.toXML(param, el);
		} catch (Exception ex) {
			logger.error("error while converting parameter :" + param + " :" + ex.getMessage(), ex); //$NON-NLS-1$ //$NON-NLS-2$
			throw new Exception(ex.getMessage(), ex);
		}
		return new mxCell(el);
	}

	private StringBuilder dumpCellsForDebug(int indent, mxICell cell) throws Exception {
		StringBuilder sb = new StringBuilder();
		if (cell == null) {
			sb.append("null");
			return sb;
		}

		sb.append(StringTools.spaces(indent) + "Cell :" + cell.getId()).append("\n");

		String cellStyle = cell.getStyle();
		if (cellStyle != null) {
			sb.append(StringTools.spaces(indent) + " CellStyle :" + cell.getStyle()).append("\n");
		}
		if (cell instanceof mxCell) {
			mxCell mxC = (mxCell) cell;
			Object v = mxC.getValue();
			if (v != null) {
				if (v instanceof ModelParameter) {
					ModelParameter param = (ModelParameter) v;
					sb.append(StringTools.spaces(indent) + " Parameter :" + param.getLabel() + " of "
							+ param.getStep().getLabel()).append("\n");

				} else if (v instanceof ModelStep) {
					ModelStep step = (ModelStep) v;
					sb.append(StringTools.spaces(indent) + " ModelStep :" + step.getName()).append("\n");
				} else if (v instanceof Element) {
					Element e = (Element) v;
					StringWriter sw = new StringWriter();
					Transformer t = TransformerFactory.newInstance().newTransformer();
					t.transform(new DOMSource(e), new StreamResult(sw));
					sb.append(StringTools.spaces(indent) + " " + sw.toString()).append("\n");
				} else {
					sb.append(StringTools.spaces(indent) + " CellValue :" + v).append("\n");
				}
			}

			mxICell source = mxC.getSource();
			if (source != null) {
				sb.append(StringTools.spaces(indent) + " Source : " + dumpCellsForDebug(indent, source));
			}
			mxICell target = mxC.getTarget();
			if (target != null) {
				sb.append(StringTools.spaces(indent) + " Target : " + dumpCellsForDebug(indent, target));
			}

		}

		sb.append(StringTools.spaces(indent + 3) + "Parent :" + dumpCellsForDebug(indent + 3, cell.getParent()))
				.append("\n");

		return sb;
	}

	/**
	 * Construct the model from mxcells
	 *
	 * @param graph
	 * @return
	 * @throws Exception
	 */
	protected Model constructModelFromGraph(mxGraph graph) throws Exception {
		Model model = new Model();

		Object parent = graph.getDefaultParent();
		Object[] childs = graph.getChildCells(parent);

		for (int i = 0; i < childs.length; i++) {
			mxCell child = (mxCell) childs[i];
			if (child.isVertex()) {
				Object s = tryLoadObjectFromCell(child);
				if (s instanceof ModelStep) {
					try {
						ModelStep modelStep = (ModelStep) s;

						// grab parameters ids ...
						Object[] paramCells = graph.getChildCells(child);
						for (int j = 0; j < paramCells.length; j++) {
							Object paramCell = paramCells[j];

							if (paramCell instanceof mxCell) {
								try {
									mxCell paramMxCell = (mxCell) paramCell;
									Object cmxc = tryLoadObjectFromCell(paramMxCell);
									if (cmxc instanceof ModelParameter) {
										ModelParameter mp = (ModelParameter) cmxc;
										String modelParameterName = mp.getName();
										ModelParameter msp = modelStep.getParameterByName(modelParameterName);
										assert mp.getId() != null;
										assert msp != null;
										msp.setId(mp.getId());
									}
								} catch (Exception ex) {
									logger.error(ex.getMessage(), ex);
									throw new Exception("error in constructing graph, inspecting param : "
											+ dumpCellsForDebug(0, child), ex);
								}
							}
						}
						model.addModelStep(modelStep);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						throw new Exception(
								"error in constructing graph, inspecting vertex : " + dumpCellsForDebug(0, child), ex);
					}
				}
			}
		}

		childs = graph.getAllEdges(new Object[] { parent });
		// load the links ...
		for (int i = 0; i < childs.length; i++) {
			mxCell child = (mxCell) childs[i];
			if (child.isEdge()) {

				mxCell source = (mxCell) child.getTerminal(true);
				mxCell target = (mxCell) child.getTerminal(false);

				if (source == null || target == null) {
					logger.info("invalid state for model, on edge : " + dumpCellsForDebug(0, child));
					continue;
				}

				try {

					if (source == null) {
						logger.info("no terminal for cell :" + child);
						String cellAdditionalInformations = dumpCellsForDebug(0, child).toString();
						throw new Exception("invalid graph, cell " + cellAdditionalInformations + " has no terminal");
					}

					ModelParameter pSource = (ModelParameter) fromMxCell(source);
					ModelStep msSource = (ModelStep) fromMxCell((mxCell) source.getParent());

					if (target == null) {
						logger.info("no target for cell :" + child);
						String cellAdditionalInformations = dumpCellsForDebug(0, child).toString();
						throw new Exception("invalid graph, cell " + cellAdditionalInformations + " has no terminal");
					}
					ModelParameter pTarget = (ModelParameter) fromMxCell(target);
					ModelStep msTarget = (ModelStep) fromMxCell((mxCell) target.getParent());

					if (model.findLinkById(child.getId()) != null)
						continue;

					ModelLink modelLink = new ModelLink();
					modelLink.setId(child.getId());

					modelLink.setFrom(
							model.findModelStepById(msSource.getId()).findModelParameterRefByName(pSource.getName()));

					modelLink.setTo(
							model.findModelStepById(msTarget.getId()).findModelParameterRefByName(pTarget.getName()));

					model.addModelLink(modelLink);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
					throw new Exception("error in constructing graph, inspecting edge : " + dumpCellsForDebug(0, child),
							ex);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("model :\n" + model.dump()); //$NON-NLS-1$
		}

		return model;
	}

	/**
	 * get the icon associated to the model step
	 *
	 * @param modelStep
	 * @return
	 */
	protected ImageIcon getAssociatedIcon(ModelStep modelStep) {

		if (modelStep == null) {
			return null;
		}

		Class modelStepClass = modelStep.getClass();
		String simpleName = modelStepClass.getSimpleName();
		try {
			return ImageTools.loadIconIfExists(modelStepClass, simpleName + ".png"); //$NON-NLS-1$
		} catch (Exception ex) {
			logger.warn("warning :" + ex.getMessage(), ex); //$NON-NLS-1$
			return null;
		}
	}
}
