package org.barrelorgandiscovery.gui.search;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class ReadOnlyDefaultTableModel extends DefaultTableModel {

	public ReadOnlyDefaultTableModel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReadOnlyDefaultTableModel(int rowCount, int columnCount) {
		super(rowCount, columnCount);
		
	}

	public ReadOnlyDefaultTableModel(Object[] columnNames, int rowCount) {
		super(columnNames, rowCount);
		
	}

	public ReadOnlyDefaultTableModel(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
		
	}

	public ReadOnlyDefaultTableModel(Vector columnNames, int rowCount) {
		super(columnNames, rowCount);
		
	}

	public ReadOnlyDefaultTableModel(Vector data, Vector columnNames) {
		super(data, columnNames);
		
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	
}
