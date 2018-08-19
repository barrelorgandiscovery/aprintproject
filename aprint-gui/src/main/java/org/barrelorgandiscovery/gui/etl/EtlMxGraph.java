package org.barrelorgandiscovery.gui.etl;

import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.TerminalParameterModelStep;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class EtlMxGraph extends mxGraph {

	public EtlMxGraph() {
	}

	public EtlMxGraph(mxIGraphModel model) {
		super(model);
	}

	public EtlMxGraph(mxStylesheet stylesheet) {
		super(stylesheet);
	}

	public EtlMxGraph(mxIGraphModel model, mxStylesheet stylesheet) {
		super(model, stylesheet);
	}

	
	@Override
	public boolean isCellResizable(Object cell) {
		if (cell instanceof mxCell) {
			mxCell c = (mxCell) cell;
			if (mxUtils.isNode(c.getValue(), ModelParameter.class.getName())) {
				return false;
			}
		}
		return super.isCellResizable(cell);
	}

	@Override
	public boolean isCellDeletable(Object cell) {

		if (cell instanceof mxCell) {

			mxCell c = (mxCell) cell;
			
			if (mxUtils.isNode(c.getValue(), ModelParameter.class.getName())) {
				return false;
			}
//			
//			if (mxUtils.isNode(c.getValue(), TerminalParameterModelStep.class.getName())) {
//				return false;
//			}

		}

		return super.isCellDeletable(cell);
	}

	@Override
	public boolean isCellSelectable(Object cell) {
		
		return super.isCellSelectable(cell);
	}

	@Override
	public boolean isCellEditable(Object cell) {
		return false;
	}

	@Override
	public String convertValueToString(Object cell) {

		if (cell instanceof mxCell) {
			mxCell c = (mxCell) cell;
			return c.getAttribute("label", super.convertValueToString(cell));
		}
		return super.convertValueToString(cell);
	}

	@Override
	public void cellLabelChanged(Object cell, Object value, boolean autoSize) {

		model.beginUpdate();
		try {

			if (cell instanceof mxCell) {
				mxCell c = (mxCell) cell;
				c.setAttribute("label", (String) value);
				if (autoSize) {
					cellSizeUpdated(cell, false);
				}
			}
		} finally {
			model.endUpdate();
		}
	}

}
