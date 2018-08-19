package org.barrelorgandiscovery.gaerepositoryclient;

public class RepositoryInstrument {

	private String name;
	private long id;
	private long version;
	private String[] tags;

	public RepositoryInstrument(String name, long id, long version,
			String[] tags) {
		this.name = name;
		this.id = id;
		this.version = version;
		this.tags = tags;
	}

	public long getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}

	public String[] getTags() {
		return this.tags;
	}

}
