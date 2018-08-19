package org.barrelorgandiscovery.model.steps.scripts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.exec.IConsoleLog;
import org.barrelorgandiscovery.groovy.APrintGroovyShell;
import org.barrelorgandiscovery.gui.script.groovy.IScriptConsole;
import org.barrelorgandiscovery.model.AbstractParameter;
import org.barrelorgandiscovery.model.ContextVariables;
import org.barrelorgandiscovery.model.IModelStepContextAware;
import org.barrelorgandiscovery.model.ModelParameter;
import org.barrelorgandiscovery.model.ModelStep;
import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.Binding;
import groovy.lang.Script;

public class GroovyScriptModelStep extends ModelStep implements IModelStepContextAware {

  /**
   * 
   */
  private static final long serialVersionUID = 2652958322654071573L;

  private static Logger logger = Logger.getLogger(GroovyScriptModelStep.class);

  private IScriptConsole console = null;
  
  /**
   * given context
   */
  private Map<String, Object> context = null;

  @Override
  public String getLabel() {
    return "Script Box";
  }
  
  @Override
  public void defineContext(Map<String, Object> context) {
    if (context != null) {
      Object o = context.get(ContextVariables.CONTEXT_CONSOLE);
      if (o != null && (o instanceof IScriptConsole)) {
        this.console = (IScriptConsole) o;
      }
    }
    this.context = context;
  }

  @Override
  public ModelParameter[] getAllParametersByRef() {
    if (modelParameters == null) {
      if (compiledScript == null) {

        try {
        	if (scriptContent != null) {
        		compileScript();
        		applyConfig();
        	}

        } catch (Exception ex) {
          logger.error("error in applying config for processor :" + this);
        }
      }

      if (modelParameters == null) {
        return new ModelParameter[0];
      }
    }
    return modelParameters;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void applyConfig() throws Exception {

    if (compiledScript == null) return;

    logger.debug("call the groovy script");
    assert compiledScript != null;

    ModelParameter[] parameters = compiledScript.configureParameters();
    if (parameters != null) {
      for (ModelParameter m : parameters) {
        m.setStep(this);
      }
    }

    this.modelParameters = parameters;
  }

  @Override
  public Map<AbstractParameter, Object> execute(Map<AbstractParameter, Object> inputValues)
      throws Exception {

    if (!isCompiled()) throw new Exception("script is not compiled");

    logger.debug("execute");
    Map<String, ModelParameter> Res =
        Arrays.stream(modelParameters)
            .collect(Collectors.toMap(AbstractParameter::getName, Function.identity()));
    Map<ModelParameter, String> revRes =
        Arrays.stream(modelParameters)
            .collect(Collectors.toMap(Function.identity(), AbstractParameter::getName));

    // grab input parameters
    HashMap<String, Object> collectedValues = new HashMap<String, Object>();

    for (Entry<AbstractParameter, Object> e : inputValues.entrySet()) {
      String k = revRes.get(e.getKey());
      if (k != null) {
        if (collectedValues.containsKey(k)) {
          logger.warn("value for input :" + k + " already exists, it will be overriden");
        }
        collectedValues.put(k, e.getValue());
      }
    }

    // call script
    Map<String, Object> scriptReturnedValues = compiledScript.execute(collectedValues);

    // transform result
    HashMap<AbstractParameter, Object> result = new HashMap<>();
    for (String k : scriptReturnedValues.keySet()) {
      AbstractParameter p = Res.get(k);
      result.put(p, scriptReturnedValues.get(k));
    }
    logger.debug("end of script execution");
    return result;
  }

  ///////////////////////////////////////////////////////////////////////
  // script implementation

  private String scriptContent;

  /** compiled version of the script if script does not compile, this member is null */
  private ModelGroovyScript compiledScript;

  private ModelParameter[] modelParameters;

  public void setScriptContent(String scriptContent) {
    this.scriptContent = scriptContent;
    razScriptAssociatedStructures();
  }

  private void razScriptAssociatedStructures() {
    this.compiledScript = null;
    this.modelParameters = null;
  }

  public String getScriptContent() {
    return scriptContent;
  }

  public boolean isCompiled() {
    return compiledScript != null;
  }

  /**
   * compile the script Content, and define the compiledScript member
   *
   * @throws Exception
   */
  public void compileScript() throws Exception {

    if (scriptContent == null || scriptContent.isEmpty())
      throw new Exception("script content is not defined");

    logger.debug("compiling script " + scriptContent);
    Binding binding = new Binding();
    
 // binding for the output in the console ...
    binding.setProperty(
        "out",
        new PrintStream(
            new OutputStream() { //$NON-NLS-1$

              @Override
              public void write(byte[] b, int off, int len) throws IOException {
                if (console == null) {
                  // no console available
                  logger.debug("no console available");
                  return;
                }

                String s = new String(b, off, len);
                try {
                  console.appendOutput(s, null);
                } catch (Exception ex) {
                  ex.printStackTrace(System.err);
                }
              }

              @Override
              public void write(int b) throws IOException {
                if (console == null) {
            		  // no console available
                      logger.debug("no console available");
            		  return;
                }
                try {
                  console.appendOutput("" + (char) b, null);
                } catch (Exception ex) {
                  ex.printStackTrace(System.err);
                }
              }
            }));

    

   
    CompilerConfiguration conf = new CompilerConfiguration();

    // Add imports for script.
    // ImportCustomizer importCustomizer = new ImportCustomizer();
    // import static com.mrhaki.blog.Type.*

    // importCustomizer.addStaticStars 'com.mrhaki.blog.Type'

    // import com.mrhaki.blog.Post as Article
    //importCustomizer.addImport 'Article', 'com.mrhaki.blog.Post'

    // conf.addCompilationCustomizers(importCustomizer);

    APrintGroovyShell gs = new APrintGroovyShell(binding);
    

    Script parsedScript = gs.parse(scriptContent);
    parsedScript.setBinding(binding);
    Object ret = parsedScript.run();
    if (ret == null || !(ret instanceof ModelGroovyScript)) {
      throw new Exception(
          "bad script, must return a " + ModelGroovyScript.class.getName() + " instance");
    }

    compiledScript = (ModelGroovyScript) ret;
  }
}
