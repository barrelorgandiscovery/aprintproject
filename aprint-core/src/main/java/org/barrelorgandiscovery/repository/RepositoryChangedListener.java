package org.barrelorgandiscovery.repository;

public interface RepositoryChangedListener {

	void scalesChanged();

	void instrumentsChanged();

	void transformationAndImporterChanged();

}
