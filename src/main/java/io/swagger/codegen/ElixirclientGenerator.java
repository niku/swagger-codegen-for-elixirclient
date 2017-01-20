package io.swagger.codegen;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ElixirclientGenerator extends DefaultCodegen implements CodegenConfig {
  class ElixirclientGeneratorCannotHandleException extends RuntimeException {
    public ElixirclientGeneratorCannotHandleException(String message) {
      super();
    }
  };

  // source folder where to write the files
  protected String sourceFolder = "lib";
  protected String apiVersion = "1.0.0";

  String supportedElixirVersion = "1.4";
  List<String> extraApplications = Arrays.asList(":logger");
  List<String> deps = Arrays.asList(
    "{:tesla, \"~> 0.5.0\"}",
    "{:poison, \">= 1.0.0\"}"
  );



  /**
   * Configures the type of generator.
   * 
   * @return  the CodegenType for this generator
   * @see     io.swagger.codegen.CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.CLIENT;
  }

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -l flag.
   * 
   * @return the friendly name for the generator
   */
  public String getName() {
    return "ElixirClient";
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   * 
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a ElixirClient client library.";
  }

  public ElixirclientGenerator() {
    super();

    // set the output folder here
    outputFolder = "generated-code/ElixirClient";

    /**
     * Models.  You can write model files using the modelTemplateFiles map.
     * if you want to create one template for file, you can do so here.
     * for multiple files for model, just put another entry in the `modelTemplateFiles` with
     * a different extension
     */
    modelTemplateFiles.put(
      "model.mustache", // the template to use
      ".ex");       // the extension for each file to write

    /**
     * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
     * as with models, add multiple entries with different extensions for multiple files per
     * class
     */
    apiTemplateFiles.put(
      "api.mustache",   // the template to use
      ".ex");       // the extension for each file to write

    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "ElixirClient";

    /**
     * Reserved words.  Override this with reserved words specific to your language
     */
    reservedWords = new HashSet<String> (
      Arrays.asList(
        "sample1",  // replace with static values
        "sample2")
    );

    /**
     * Additional Properties.  These values can be passed to the templates and
     * are available in models, apis, and supporting files
     */
    additionalProperties.put("apiVersion", apiVersion);

    /**
     * Supporting Files.  You can write single files for the generator with the
     * entire object tree available.  If the input file has a suffix of `.mustache
     * it will be processed by the template engine.  Otherwise, it will be copied
     */
    supportingFiles.add(new SupportingFile("README.md.mustache",   // the input template or file
      "",                                                       // the destination folder, relative `outputFolder`
      "README.md")                                          // the output file
    );
    supportingFiles.add(new SupportingFile("config.exs.mustache",
      "config",
      "config.exs")
    );
    supportingFiles.add(new SupportingFile("mix.exs.mustache",
      "",
      "mix.exs")
    );
    supportingFiles.add(new SupportingFile("test_helper.exs.mustache",
      "test",
      "test_helper.exs")
    );

    /**
     * Language Specific Primitives.  These types will not trigger imports by
     * the client generator
     */
    languageSpecificPrimitives = new HashSet<String>(
      Arrays.asList(
        "Type1",      // replace these with your types
        "Type2")
    );
  }

  @Override
  public void processOpts() {
    super.processOpts();
    additionalProperties.put("supportedElixirVersion", supportedElixirVersion);
    additionalProperties.put("extraApplications", String.join(",", extraApplications));
    additionalProperties.put("deps", deps);
    additionalProperties.put("underscored", new Mustache.Lambda() {
      @Override
      public void execute(Template.Fragment fragment, Writer writer) throws IOException {
        writer.write(underscored(fragment.execute()));
      }
    });
    additionalProperties.put("modulized", new Mustache.Lambda() {
      @Override
      public void execute(Template.Fragment fragment, Writer writer) throws IOException {
        writer.write(modulized(fragment.execute()));
      }
    });
  }

  String underscored(String words) {
    ArrayList<String> underscoredWords = new ArrayList<String>();
    for (String word : words.split(" ")) {
      underscoredWords.add(underscore(word));
    }
    return String.join("_", underscoredWords);
  }

  String modulized(String words) {
    ArrayList<String> modulizedWords = new ArrayList<String>();
    for (String word : words.split(" ")) {
      modulizedWords.add(camelize(word));
    }
    return String.join("",  modulizedWords);
  }

  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
   * those terms here.  This logic is only called if a variable matches the reseved words
   * 
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    return "_" + name;  // add an underscore to the name
  }

  /**
   * Location to write model files.  You can use the modelPackage() as defined when the class is
   * instantiated
   */
  public String modelFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + underscored((String) additionalProperties.get("appName")) + "/" + "model";
  }

  /**
   * Location to write api files.  You can use the apiPackage() as defined when the class is
   * instantiated
   */
  @Override
  public String apiFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + underscored((String) additionalProperties.get("appName")) + "/" + "api";
  }

  @Override
  public String toApiName(String name) {
    if (name.length() == 0) {
      return "Default";
    }
    return initialCaps(name);
  }

  @Override
  public String toApiFilename(String name) {
    return snakeCase(name);
  }

  @Override
  public String toModelFilename(String name) {
    return snakeCase(name);
  }

  /**
   * Optional - type declaration.  This is a String which is used by the templates to instantiate your
   * types.  There is typically special handling for different property types
   *
   * @return a string value used as the `dataType` field for model templates, `returnType` for api templates
   */
  @Override
  public String getTypeDeclaration(Property p) {
    if(p instanceof ArrayProperty) {
      ArrayProperty ap = (ArrayProperty) p;
      Property inner = ap.getItems();
      return getSwaggerType(p) + "[" + getTypeDeclaration(inner) + "]";
    }
    else if (p instanceof MapProperty) {
      MapProperty mp = (MapProperty) p;
      Property inner = mp.getAdditionalProperties();
      return getSwaggerType(p) + "[String, " + getTypeDeclaration(inner) + "]";
    }
    return super.getTypeDeclaration(p);
  }

  /**
   * Optional - swagger type conversion.  This is used to map swagger types in a `Property` into 
   * either language specific types via `typeMapping` or into complex models if there is not a mapping.
   *
   * @return a string value of the type or complex model for this property
   * @see io.swagger.models.properties.Property
   */
  @Override
  public String getSwaggerType(Property p) {
    String swaggerType = super.getSwaggerType(p);
    String type = null;
    if(typeMapping.containsKey(swaggerType)) {
      type = typeMapping.get(swaggerType);
      if(languageSpecificPrimitives.contains(type))
        return toModelName(type);
    }
    else
      type = swaggerType;
    return toModelName(type);
  }
}