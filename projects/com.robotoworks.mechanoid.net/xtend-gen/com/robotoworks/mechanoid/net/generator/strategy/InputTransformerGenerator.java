package com.robotoworks.mechanoid.net.generator.strategy;

import com.robotoworks.mechanoid.net.generator.CodeGenerationContext;
import com.robotoworks.mechanoid.net.generator.strategy.JsonReaderGenerator;
import com.robotoworks.mechanoid.net.netModel.ComplexTypeDeclaration;
import com.robotoworks.mechanoid.net.netModel.Model;
import org.eclipse.xtend2.lib.StringConcatenation;

@SuppressWarnings("all")
public class InputTransformerGenerator {
  private CodeGenerationContext context;
  
  public CodeGenerationContext setContext(final CodeGenerationContext context) {
    CodeGenerationContext _context = this.context = context;
    return _context;
  }
  
  public Object registerImports() {
    return null;
  }
  
  private JsonReaderGenerator jsonReaderGenerator;
  
  public JsonReaderGenerator setJsonReaderGenerator(final JsonReaderGenerator jsonReaderGenerator) {
    JsonReaderGenerator _jsonReaderGenerator = this.jsonReaderGenerator = jsonReaderGenerator;
    return _jsonReaderGenerator;
  }
  
  public CharSequence generate(final ComplexTypeDeclaration decl, final Model module) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package ");
    String _packageName = module.getPackageName();
    _builder.append(_packageName, "");
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    CharSequence body = this.generateClass(decl, module);
    _builder.newLineIfNotEmpty();
    Object _registerImports = this.registerImports();
    _builder.append(_registerImports, "");
    _builder.newLineIfNotEmpty();
    _builder.append("import com.robotoworks.mechanoid.net.Transformer;");
    _builder.newLine();
    _builder.append("import com.robotoworks.mechanoid.net.TransformException;");
    _builder.newLine();
    _builder.append("import com.robotoworks.mechanoid.internal.util.JsonReader;");
    _builder.newLine();
    StringConcatenation _printImports = this.context.printImports();
    _builder.append(_printImports, "");
    _builder.newLineIfNotEmpty();
    this.context.clearImports();
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append(body, "");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  public CharSequence generateClass(final ComplexTypeDeclaration decl, final Model module) {
    StringConcatenation _builder = new StringConcatenation();
    this.context.registerImport("java.util.List");
    _builder.newLineIfNotEmpty();
    _builder.append("public class ");
    String _name = decl.getName();
    _builder.append(_name, "");
    _builder.append("InputTransformer extends Transformer<JsonReader, ");
    String _name_1 = decl.getName();
    _builder.append(_name_1, "");
    _builder.append("> {\t\t\t");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append("public void transform(JsonReader source, ");
    String _name_2 = decl.getName();
    _builder.append(_name_2, "	");
    _builder.append(" target) throws TransformException {");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("try {");
    _builder.newLine();
    _builder.append("\t\t\t");
    CharSequence _genReadComplexType = this.jsonReaderGenerator.genReadComplexType(decl);
    _builder.append(_genReadComplexType, "			");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t\t");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("} catch (Exception x) {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("throw new TransformException(x);");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public void transform(JsonReader source, List<");
    String _name_3 = decl.getName();
    _builder.append(_name_3, "	");
    _builder.append("> target) throws TransformException {");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("try {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("source.beginArray();");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("while(source.hasNext()) {");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    String _name_4 = decl.getName();
    _builder.append(_name_4, "				");
    _builder.append(" targetItem = new ");
    String _name_5 = decl.getName();
    _builder.append(_name_5, "				");
    _builder.append("();");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t\t\t");
    _builder.append("transform(source, targetItem);");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("target.add(targetItem);");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("source.endArray();");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("} catch (Exception x) {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("throw new TransformException(x);");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    return _builder;
  }
}
