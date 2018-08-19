package org.barrelorgandiscovery.gui.repository;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.gui.aprint.APrintProperties;
import org.barrelorgandiscovery.gui.aprintng.IAPrintWait;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.repository.httpxmlrepository.HttpXmlRepository;

/**
 * Factory for getting gui form associated to the repository / instrument form
 * 
 * @author pfreydiere
 * 
 */
public class RepositoryGUIFormFactory {
	
	private static Logger logger = Logger.getLogger(RepositoryGUIFormFactory.class);

	public JAbstractRepositoryForm createAssociatedForm(Object owner,
			Repository2 repository, APrintProperties properties)
			throws Exception {

		if (repository instanceof HttpXmlRepository) {

			JRepositoryHttpXMLPanel form = new JRepositoryHttpXMLPanel(owner,
					repository, properties);

			if (owner instanceof IAPrintWait) {
				logger.debug("associate wait from owner :" + owner);
				form.setWait((IAPrintWait) owner);
			}

			return form;
		}

		JRepositoryForm jRepositoryForm = new JRepositoryForm(owner,
				repository, properties);
		return jRepositoryForm;
	}
}
