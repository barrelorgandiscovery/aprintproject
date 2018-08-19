package org.barrelorgandiscovery.groovy;

import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class APrintGroovyShell extends GroovyShell {

	public APrintGroovyShell() {
		super();
		
	}

	public APrintGroovyShell(Binding binding, CompilerConfiguration config) {
		super(binding, config);
		
	}

	public APrintGroovyShell(Binding binding) {
		super(binding);
		
	}

	public APrintGroovyShell(ClassLoader parent, Binding binding,
			CompilerConfiguration config) {
		super(parent, binding, config);
		
	}

	public APrintGroovyShell(ClassLoader parent, Binding binding) {
		super(parent, binding);
		
	}

	public APrintGroovyShell(ClassLoader parent) {
		super(parent);

	}

	public APrintGroovyShell(CompilerConfiguration config) {
		super(config);
	}

	public APrintGroovyShell(GroovyShell shell) {
		super(shell);
	}

}
