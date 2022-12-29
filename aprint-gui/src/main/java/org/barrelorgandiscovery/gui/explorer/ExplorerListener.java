package org.barrelorgandiscovery.gui.explorer;

import org.apache.commons.vfs2.FileObject;

public interface ExplorerListener {

	void selectionChanged(FileObject[] fo) throws Exception;

	void doubleClick(FileObject fo) throws Exception;

}
