package paws;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import java.util.Iterator;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import java.util.ArrayList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;

// 
// Decompiled by Procyon v0.5.30
// 

public class Parser
{
    private List<String> concepts;
    private List<String> methodNames;
    private List<String> classNames;
    private List<String> imports;
    private ASTNode curNode;
    private CompilationUnit unit;
    private String content_name;
    boolean isExample;
    boolean isTester;
    String[] boxingTypes;
    Export export;
    
    public Parser() {
        this.isExample = false;
        this.isTester = false;
        this.boxingTypes = new String[] { "Integer", "Float", "Short", "Double", "Long", "Byte", "Character", "Boolean" };
        this.export = null;
    }
    
    public void clearLists() {
        if (this.concepts != null) {
            this.concepts.clear();
        }
        if (this.methodNames != null) {
            this.methodNames.clear();
        }
        if (this.classNames != null) {
            this.classNames.clear();
        }
        if (this.imports != null) {
            this.imports.clear();
        }
        this.unit = null;
        this.curNode = null;
    }
    
    private CompilationUnit createAST(final String source) {
        final ASTParser parser = ASTParser.newParser(3);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setKind(8);
        parser.setSource(source.toCharArray());
        parser.setStatementsRecovery(true);
        return (CompilationUnit)parser.createAST(null);
    }
    
    public void parse(final Export export, final String question, String source, final boolean isTester) {
        this.export = export;
        this.isTester = isTester;
        this.unit = this.createAST(source);
        this.content_name = question;
        this.curNode = this.unit;
        this.initializeParserData();
        if (!this.classNames.isEmpty()) {
            this.index(this.curNode);
        }
        else {
            String[] classMethodDefinitionConcepts = { "ClassDefinition", "PublicClassSpecifier", "MethodDefinition", "PublicMethodSpecifier", "VoidDataType" };
            String[] classDefinitionConcepts = { "ClassDefinition", "PublicClassSpecifier" };
            boolean hasFakeMethodConcept = true;
            if (this.methodNames.isEmpty()) {
                source = "public class Tester  { public void test() {  " + source + " }}";
            }
            else {
                source = "public class Tester { " + source + " }";
                hasFakeMethodConcept = false;
            }
            this.clearParserData();
            this.unit = this.createAST(source);
            this.curNode = this.unit;
            this.initializeParserData();
            this.index(this.curNode);
            String[] conceptsToBeRemoved;
            if (hasFakeMethodConcept) {
                conceptsToBeRemoved = classMethodDefinitionConcepts;
            }
            else {
                conceptsToBeRemoved = classDefinitionConcepts;
            }
            this.removeConcept(conceptsToBeRemoved);
            classMethodDefinitionConcepts = null;
            classDefinitionConcepts = null;
            conceptsToBeRemoved = null;
        }
        this.clearParserData();
        this.export = null;
    }
    
    private void removeConcept(final String[] conceptsToBeRemoved) {
        Boolean connected = true;

        if(this.export.getType() == "db"){
          DB exp = (DB)this.export;
          connected = exp.isConnectedToParser();
        }

        if (connected) {
            this.export.deleteConcept(this.content_name, conceptsToBeRemoved, this.isExample);
        }
        else {
            System.out.println("Connection To Webex is lost!");
        }
    }
    
    private void initializeParserData() {
        this.concepts = new ArrayList<String>();
        this.methodNames = new ArrayList<String>();
        this.classNames = new ArrayList<String>();
        this.imports = new ArrayList<String>();
        for (final Object o : this.unit.types()) {
            final TypeDeclaration t = (TypeDeclaration)o;
            this.classNames.add(t.getName().toString());
            final MethodDeclaration[] methods = t.getMethods();
            MethodDeclaration[] array;
            for (int length = (array = methods).length, j = 0; j < length; ++j) {
                final MethodDeclaration m = array[j];
                this.methodNames.add(m.getName().toString());
            }
        }
        for (final Object i : this.unit.imports()) {
            final ImportDeclaration temp = (ImportDeclaration)i;
            final String imTxt = temp.getName().toString();
            this.imports.add(imTxt);
        }
    }
    
    private void index(final ASTNode node) {
        final List<Object> properties = (List<Object>)node.structuralPropertiesForType();
        final int nodeType = node.getNodeType();
        final String nodeClassTxt = node.getClass().getSimpleName();
        for (final Object descriptor : properties) {
            if (descriptor instanceof SimplePropertyDescriptor) {
                final SimplePropertyDescriptor simple = (SimplePropertyDescriptor)descriptor;
                final Object value = node.getStructuralProperty(simple);
                if (!this.isConceptNode(node)) {
                    continue;
                }
                final int parentType = node.getParent().getNodeType();
                if (node instanceof SimpleName) {
                    ((SimpleName)node).resolveBinding();
                    ((SimpleName)node).resolveTypeBinding();
                }
                if (nodeType == 45) {
                    this.addConcept("StringLiteral");
                    this.addConcept("StringDataType");
                }
                if (nodeType == 55) {
                    this.addConcept("ClassDefinition");
                }
                if (nodeType == 7) {
                    final Assignment.Operator op = ((Assignment)node).getOperator();
                    if (op == Assignment.Operator.ASSIGN) {
                        this.addConcept("SimpleAssignmentExpression");
                    }
                    else if (op == Assignment.Operator.PLUS_ASSIGN) {
                        this.addConcept("AddAssignmentExpression");
                    }
                    else if (op == Assignment.Operator.TIMES_ASSIGN) {
                        this.addConcept("MultiplyAssignmentExpression");
                    }
                    else if (op == Assignment.Operator.MINUS_ASSIGN) {
                        this.addConcept("MinusAssignmentExpression");
                    }
                    else if (op == Assignment.Operator.DIVIDE_ASSIGN) {
                        this.addConcept("DivideAssignmentExpression");
                    }
                }
                if (nodeType == 27) {
                    final InfixExpression infixEx = (InfixExpression)node;
                    final Expression leftOperand = infixEx.getLeftOperand();
                    final Expression rightOperad = infixEx.getRightOperand();
                    if (leftOperand instanceof ClassInstanceCreation) {
                        final ClassInstanceCreation ci = (ClassInstanceCreation)leftOperand;
                        if (this.isAutoBoxingType(ci.getType())) {
                            this.addConcept("AutoBoxing");
                        }
                    }
                    if (rightOperad instanceof ClassInstanceCreation) {
                        final ClassInstanceCreation ci = (ClassInstanceCreation)rightOperad;
                        if (this.isAutoBoxingType(ci.getType())) {
                            this.addConcept("AutoBoxing");
                        }
                    }
                    if (infixEx.getOperator() == InfixExpression.Operator.AND | infixEx.getOperator() == InfixExpression.Operator.CONDITIONAL_AND) {
                        this.addConcept("AndExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.DIVIDE) {
                        this.addConcept("DivideExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.EQUALS) {
                        this.addConcept("EqualExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.GREATER) {
                        this.addConcept("GreaterExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.GREATER_EQUALS) {
                        this.addConcept("GreaterEqualExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.LESS) {
                        this.addConcept("LessExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.LESS_EQUALS) {
                        this.addConcept("LessEqualExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.MINUS) {
                        this.addConcept("SubtractExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.NOT_EQUALS) {
                        this.addConcept("NotEqualExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.OR | infixEx.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
                        this.addConcept("OrExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.PLUS) {
                        if (leftOperand instanceof StringLiteral | rightOperad instanceof StringLiteral) {
                            this.addConcept("StringAddition");
                            this.addConcept("StringDataType");
                        }
                        else {
                            this.addConcept("AddExpression");
                        }
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.REMAINDER) {
                        this.addConcept("ModulusExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.TIMES) {
                        this.addConcept("MultiplyExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.XOR) {
                        this.addConcept("XORExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.LEFT_SHIFT) {
                        this.addConcept("LeftShiftExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_SIGNED) {
                        this.addConcept("RightShiftSignedExpression");
                    }
                    else if (infixEx.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
                        this.addConcept("RightShiftUnsignedExpression");
                    }
                    if (rightOperad instanceof NullLiteral | leftOperand instanceof NullLiteral) {
                        this.addConcept("null");
                    }
                    if (leftOperand instanceof QualifiedName && !((QualifiedName)leftOperand).getName().getIdentifier().equals("length")) {
                        this.addConcept("ConstantInvocation");
                    }
                    if (rightOperad instanceof QualifiedName && !((QualifiedName)rightOperad).getName().getIdentifier().equals("length")) {
                        this.addConcept("ConstantInvocation");
                    }
                    if (!(leftOperand instanceof CastExpression | rightOperad instanceof CastExpression)) {
                        continue;
                    }
                    if (leftOperand instanceof CastExpression) {
                        if (((CastExpression)leftOperand).getExpression() instanceof QualifiedName) {
                            this.addConcept("ConstantInvocation");
                        }
                        else {
                            this.addConcept("ExplicitTypeCasting");
                        }
                    }
                    else {
                        if (!(rightOperad instanceof CastExpression)) {
                            continue;
                        }
                        if (((CastExpression)rightOperad).getExpression() instanceof QualifiedName) {
                            this.addConcept("ConstantInvocation");
                        }
                        else {
                            this.addConcept("ExplicitTypeCasting");
                        }
                    }
                }
                else if (nodeType == 37) {
                    final PostfixExpression pfexp = (PostfixExpression)node;
                    if (pfexp.getOperator() == PostfixExpression.Operator.INCREMENT) {
                        this.addConcept("PostIncrementExpression");
                    }
                    else {
                        if (pfexp.getOperator() != PostfixExpression.Operator.DECREMENT) {
                            continue;
                        }
                        this.addConcept("PostDecrementExpression");
                    }
                }
                else if (nodeType == 38) {
                    final PrefixExpression pfexp2 = (PrefixExpression)node;
                    if (pfexp2.getOperator() == PrefixExpression.Operator.INCREMENT) {
                        this.addConcept("PreIncrementExpression");
                    }
                    else if (pfexp2.getOperator() == PrefixExpression.Operator.DECREMENT) {
                        this.addConcept("PreDecrementExpression");
                    }
                    else {
                        if (pfexp2.getOperator() != PrefixExpression.Operator.NOT) {
                            continue;
                        }
                        this.addConcept("NotExpression");
                    }
                }
                else if (nodeType == 9) {
                    if (((BooleanLiteral)node).booleanValue()) {
                        this.addConcept("True");
                    }
                    else {
                        this.addConcept("False");
                    }
                }
                else if (nodeType == 31) {
                    final MethodDeclaration md = (MethodDeclaration)node;
                    final List<Modifier> modifiers = (List<Modifier>)md.modifiers();
                    if (md.getBody() != null) {
                        final List<Statement> list = (List<Statement>)md.getBody().statements();
                        if (!md.parameters().isEmpty()) {
                            this.addConcept("FormalMethodParameter");
                        }
                        if (md.isConstructor()) {
                            this.addConcept("ConstructorDefinition");
                            for (final Modifier m : modifiers) {
                                if (m.isPrivate()) {
                                    this.addConcept("PrivateConstructorSpecifier");
                                }
                                else if (m.isPublic()) {
                                    this.addConcept("PublicConstructorSpecifier");
                                }
                                else {
                                    if (!m.isProtected()) {
                                        continue;
                                    }
                                    this.addConcept("ProtectedConstructorSpecifier");
                                }
                            }
                        }
                        else {
                            this.addConcept("MethodDefinition");
                            for (final Modifier m : modifiers) {
                                if (m.isFinal()) {
                                    this.addConcept("FinalMethodSpecifier");
                                }
                                else if (m.isStatic()) {
                                    this.addConcept("StaticMethodSpecifier");
                                }
                                else if (m.isPrivate()) {
                                    this.addConcept("PrivateMethodSpecifier");
                                }
                                else if (m.isPublic()) {
                                    this.addConcept("PublicMethodSpecifier");
                                }
                                else if (m.isAbstract()) {
                                    this.addConcept("AbstractMethodDefinition");
                                    this.addConcept("AbstractMethodSpecifier");
                                }
                                else if (m.isNative()) {
                                    this.addConcept("NativeMethodSpecifier");
                                }
                                else if (m.isProtected()) {
                                    this.addConcept("ProtectedMethodSpecifier");
                                }
                                else if (m.isSynchronized()) {
                                    this.addConcept("SynchronizedMethodSpecifier");
                                }
                                else {
                                    if (!m.isStrictfp()) {
                                        continue;
                                    }
                                    this.addConcept("StrictfpMethodSpecifier");
                                }
                            }
                        }
                        for (final Statement s : list) {
                            final int startPosition = s.getStartPosition();
                            final int length = s.getLength();
                            if (s instanceof SuperConstructorInvocation) {
                                this.addConcept("SuperclassConstructorCall", startPosition, length);
                            }
                            else if (s instanceof ExpressionStatement) {
                                if (!(((ExpressionStatement)s).getExpression() instanceof SuperMethodInvocation)) {
                                    continue;
                                }
                                this.addConcept("SuperclassMethodCall", startPosition, length);
                                this.addConcept("SuperReference", startPosition, length);
                            }
                            else {
                                if (!(s instanceof ReturnStatement)) {
                                    continue;
                                }
                                final Expression e = ((ReturnStatement)s).getExpression();
                                if (e instanceof SuperMethodInvocation) {
                                    this.addConcept("SuperclassMethodCall", startPosition, length);
                                    this.addConcept("SuperReference", startPosition, length);
                                }
                                else {
                                    if (!(e instanceof InfixExpression)) {
                                        continue;
                                    }
                                    final InfixExpression ie = (InfixExpression)e;
                                    if (!(ie.getLeftOperand() instanceof SuperMethodInvocation | ie.getRightOperand() instanceof SuperMethodInvocation)) {
                                        continue;
                                    }
                                    this.addConcept("SuperclassMethodCall", startPosition, length);
                                    this.addConcept("SuperReference", startPosition, length);
                                }
                            }
                        }
                    }
                    if (md.getParent() instanceof TypeDeclaration) {
                        final TypeDeclaration td = (TypeDeclaration)md.getParent();
                        if (td.isInterface()) {
                            this.addConcept("AbstractMethodDefinition");
                        }
                    }
                    if (this.isOverridingEquals(md)) {
                        this.addConcept("OverridingEquals");
                    }
                    if (this.isOverridingToString(md)) {
                        this.addConcept("OverridingToString");
                    }
                    if (!md.thrownExceptions().isEmpty()) {
                        this.addConcept("ThrowsSpecification");
                    }
                    if (md.getReturnType2() == null || !md.getReturnType2().toString().equals("String")) {
                        continue;
                    }
                    this.addConcept("StringDataType");
                }
                else if (nodeType == 26) {
                    final ImportDeclaration imd = (ImportDeclaration)node;
                    final String importName = imd.getName().getFullyQualifiedName();
                    if (importName.equals("java.util.ArrayList")) {
                        this.addConcept("java.util.ArrayList");
                    }
                    this.addConcept("ImportStatement");
                    if (simple.getId().equals("static") & value.toString().equals("true")) {
                        this.addConcept("StaticImport");
                    }
                    else {
                        if (!(simple.getId().equals("onDemand") & value.toString().equals("true"))) {
                            continue;
                        }
                        this.addConcept("OnDemandImport");
                    }
                }
                else if (nodeType == 55) {
                    final TypeDeclaration td2 = (TypeDeclaration)node;
                    final List<Modifier> modifiers = (List<Modifier>)td2.modifiers();
                    if (td2.isInterface()) {
                        this.addConcept("InterfaceDefinition");
                        for (final Modifier i : modifiers) {
                            if (i.isStatic()) {
                                this.addConcept("StaticInterfaceSpecifier");
                            }
                            else if (i.isPrivate()) {
                                this.addConcept("PrivateInterfaceSpecifier");
                            }
                            else if (i.isPublic()) {
                                this.addConcept("PublicInterfaceSpecifier");
                            }
                            else if (i.isAbstract()) {
                                this.addConcept("AbstractInterfaceSpecifier");
                            }
                            else if (i.isProtected()) {
                                this.addConcept("ProtectedInterfaceSpecifier");
                            }
                            else {
                                if (!i.isStrictfp()) {
                                    continue;
                                }
                                this.addConcept("StrictfpInterfaceSpecifier");
                            }
                        }
                    }
                    else {
                        for (final Modifier i : modifiers) {
                            if (i.isFinal()) {
                                this.addConcept("FinalClassSpecifier");
                            }
                            else if (i.isStatic()) {
                                this.addConcept("StaticClassSpecifier");
                            }
                            else if (i.isPrivate()) {
                                this.addConcept("PrivateClassSpecifier");
                            }
                            else if (i.isPublic()) {
                                this.addConcept("PublicClassSpecifier");
                            }
                            else if (i.isAbstract()) {
                                this.addConcept("AbstractClassSpecifier");
                            }
                            else if (i.isProtected()) {
                                this.addConcept("ProtectedClassSpecifier");
                            }
                            else {
                                if (!i.isStrictfp()) {
                                    continue;
                                }
                                this.addConcept("StrictfpClassSpecifier");
                            }
                        }
                    }
                }
                else if (nodeType == 59) {
                    final VariableDeclarationFragment vd = (VariableDeclarationFragment)node;
                    if (vd.getParent().toString().contains("ArrayList")) {
                        this.addConcept("java.util.ArrayList");
                    }
                    if (vd.getInitializer() == null) {
                        this.addConcept("SimpleVariable");
                    }
                    else {
                        this.addConcept("SimpleAssignmentExpression");
                    }
                }
                else {
                    if (nodeType != 39) {
                        continue;
                    }
                    this.addPrimitiveTypeConcept((PrimitiveType)node);
                }
            }
            else if (descriptor instanceof ChildPropertyDescriptor) {
                final ChildPropertyDescriptor child = (ChildPropertyDescriptor)descriptor;
                final ASTNode childNode = (ASTNode)node.getStructuralProperty(child);
                if (childNode == null) {
                    continue;
                }
                if (child.getId().equals("body")) {
                    if (nodeType == 54) {
                        this.addConcept("TryCatchStatement");
                    }
                }
                else if (child.getId().equals("qualifier")) {
                    if (nodeType == 40) {
                        final QualifiedName q = (QualifiedName)node;
                        if (q.getQualifier().toString().equals("Math") && (q.getName().getIdentifier().equals("PI") | q.getName().getIdentifier().equals("E"))) {
                            this.addConcept("Constant");
                            this.addConcept("ConstantInvocation");
                        }
                    }
                }
                else if (child.getId().equals("superclassType")) {
                    if (nodeType == 55) {
                        this.addConcept("ExtendsSpecification");
                    }
                }
                else if (child.getId().equals("expression")) {
                    if (node instanceof ReturnStatement) {
                        this.addConcept("ReturnStatement");
                    }
                    else if (node instanceof CastExpression) {
                        this.addConcept("ExplicitTypeCasting");
                    }
                    Statement body = null;
                    Statement elseStatement = null;
                    if (node instanceof ForStatement) {
                        this.addConcept("ForStatement");
                        body = ((ForStatement)node).getBody();
                    }
                    else if (node instanceof EnhancedForStatement) {
                        this.addConcept("ForEachStatement");
                        body = ((EnhancedForStatement)node).getBody();
                    }
                    else if (node instanceof WhileStatement) {
                        this.addConcept("WhileStatement");
                        body = ((WhileStatement)node).getBody();
                    }
                    else if (node instanceof DoStatement) {
                        this.addConcept("DoStatement");
                        this.addConcept("WhileStatement");
                        final DoStatement ds = (DoStatement)node;
                        body = ds.getBody();
                    }
                    else if (node instanceof SwitchStatement) {
                        this.addConcept("SwitchStatement");
                        final List<Statement> stats = (List<Statement>)((SwitchStatement)node).statements();
                        for (final Statement s : stats) {
                            final int startPosition = s.getStartPosition();
                            final int length = s.getLength();
                            if (s instanceof SwitchCase) {
                                final SwitchCase sc = (SwitchCase)s;
                                if (sc.isDefault()) {
                                    this.addConcept("DefaultClause", startPosition, length);
                                }
                                else {
                                    this.addConcept("CaseClause", startPosition, length);
                                }
                            }
                            else {
                                if (!(s instanceof BreakStatement)) {
                                    continue;
                                }
                                this.addConcept("BreakStatement", startPosition, length);
                            }
                        }
                    }
                    else if (node instanceof TryStatement) {
                        this.addConcept("TryCatchStatement");
                        body = ((TryStatement)node).getBody();
                    }
                    else if (node instanceof IfStatement) {
                        final IfStatement ifs = (IfStatement)node;
                        body = ifs.getThenStatement();
                        if (ifs.getElseStatement() == null) {
                            this.addConcept("IfStatement");
                        }
                        else if (ifs.getElseStatement() instanceof IfStatement) {
                            this.addConcept("IfElseIfStatement");
                        }
                        else {
                            this.addConcept("IfElseStatement");
                            elseStatement = ifs.getElseStatement();
                        }
                    }
                    else if (node instanceof ThrowStatement) {
                        this.addConcept("ThrowStatement");
                    }
                    if (body != null) {
                        final Statement nestedStatement = this.getNestedStatement(body);
                        if (nestedStatement != null && nestedStatement instanceof ForStatement) {
                            if (nestedStatement.getParent() instanceof Block) {
                                final List<Statement> stats2 = (List<Statement>)((Block)nestedStatement.getParent()).statements();
                                for (final Statement s2 : stats2) {
                                    if (s2 instanceof ForStatement) {
                                        this.addConcept("NestedForLoops", nestedStatement.getParent().getStartPosition(), nestedStatement.getParent().getLength());
                                    }
                                }
                            }
                            else if (nestedStatement.getParent() instanceof ForStatement) {
                                this.addConcept("NestedForLoops", nestedStatement.getParent().getStartPosition(), nestedStatement.getParent().getLength());
                            }
                        }
                    }
                }
                else if (child.getId().equals("array") && node instanceof ArrayAccess) {
                    this.addConcept("ArrayElement");
                }
                if (nodeType == 47) {
                    this.addConcept("SuperReference");
                }
                else if (nodeType == 22) {
                    this.addConcept("InstanceFieldInvocation");
                    final FieldAccess fa = (FieldAccess)node;
                    if (fa.getExpression() instanceof ThisExpression) {
                        this.addConcept("ThisReference");
                    }
                }
                else if (nodeType == 23) {
                    boolean isStatic = false;
                    boolean isFinal = false;
                    final FieldDeclaration fd = (FieldDeclaration)node;
                    final List<Modifier> modifiers2 = (List<Modifier>)fd.modifiers();
                    if (fd.getType().isParameterizedType()) {
                        this.checkAndAddStringParametrizedType((ParameterizedType)fd.getType());
                    }
                    this.checkAndAddStringVariableConcept(fd.getType());
                    if (fd.getType().isArrayType()) {
                        if (((ArrayType)fd.getType()).getDimensions() > 1) {
                            this.addConcept("MultiDimensionalArrayDataType");
                        }
                        else {
                            this.addConcept("ArrayDataType");
                        }
                    }
                    for (final Modifier j : modifiers2) {
                        if (j.isStatic()) {
                            isStatic = true;
                            this.addConcept("StaticFieldSpecifier");
                        }
                        if (j.isFinal()) {
                            isFinal = true;
                            this.addConcept("FinalFieldSpecifier");
                        }
                        if (j.isPrivate()) {
                            this.addConcept("PrivateFieldSpecifier");
                        }
                        if (j.isProtected()) {
                            this.addConcept("ProtectedFieldSpecifier");
                        }
                        if (j.isPublic()) {
                            this.addConcept("PublicFieldSpecifier");
                        }
                        if (j.isTransient()) {
                            this.addConcept("TransientFieldSpecifier");
                        }
                        if (j.isVolatile()) {
                            this.addConcept("VolatileFieldSpecifier");
                        }
                    }
                    if (isStatic) {
                        this.addConcept("ClassField");
                    }
                    else {
                        this.addConcept("InstanceField");
                    }
                    for (final Object x : fd.fragments()) {
                        if (x instanceof VariableDeclarationFragment && ((VariableDeclarationFragment)x).getInitializer() != null) {
                            if (isFinal && isStatic) {
                                this.addConcept("ClassConstantInitializationStatement");
                            }
                            else {
                                this.addConcept("InstanceFieldInitializationStatement");
                            }
                        }
                    }
                }
                else if (nodeType == 60) {
                    final VariableDeclarationStatement vs = (VariableDeclarationStatement)node;
                    final List<VariableDeclarationFragment> fragments = (List<VariableDeclarationFragment>)vs.fragments();
                    final List<Modifier> modifiers3 = (List<Modifier>)vs.modifiers();
                    for (final Modifier m : modifiers3) {
                        if (m.isFinal()) {
                            this.addConcept("Constant");
                            for (final VariableDeclarationFragment f : fragments) {
                                if (f.getInitializer() != null) {
                                    this.addConcept("ConstantInitializationStatement");
                                }
                            }
                        }
                    }
                    for (final VariableDeclarationFragment vf : fragments) {
                        if (vf.getInitializer() instanceof NullLiteral) {
                            this.addConcept("nullInitialization");
                        }
                        else if (vf.getInitializer() instanceof ArrayInitializer) {
                            this.addConcept("ArrayInitializationStatement");
                            this.addConcept("ArrayInitializer");
                            this.addConcept("ArrayVariable");
                        }
                        else if (vf.getInitializer() instanceof ArrayCreation) {
                            this.addConcept("ArrayCreationStatement");
                            this.addConcept("ArrayVariable");
                        }
                        else if (vf.getInitializer() instanceof StringLiteral) {
                            this.addConcept("StringInitializationStatement");
                        }
                        if ((vf.getInitializer() instanceof NumberLiteral | vf.getInitializer() instanceof BooleanLiteral | vf.getInitializer() instanceof CharacterLiteral) && this.isAutoBoxingType(vs.getType())) {
                            this.addConcept("AutoBoxing");
                        }
                    }
                    this.checkAndAddObjectVariableConcept(vs.getType());
                    this.checkAndAddStringVariableConcept(vs.getType());
                    if (vs.getType().isPrimitiveType()) {
                        final PrimitiveType pType = (PrimitiveType)vs.getType();
                        this.addPrimitiveTypeConcept(pType);
                    }
                    else if (vs.getType().isSimpleType() | vs.getType().isQualifiedType()) {
                        this.checkAndAddWrapperClassTypes(vs.getType());
                        if (!vs.getType().toString().equals("String")) {
                            for (final VariableDeclarationFragment f2 : fragments) {
                                if (f2.getInitializer() == null) {
                                    this.addConcept("ObjectVariable");
                                }
                            }
                        }
                    }
                    else if (vs.getType().isArrayType()) {
                        if (((ArrayType)vs.getType()).getDimensions() > 1) {
                            this.addConcept("MultiDimensionalArrayDataType");
                        }
                        else {
                            this.addConcept("ArrayDataType");
                        }
                    }
                    else if (vs.getType().isParameterizedType()) {
                        this.checkAndAddStringParametrizedType((ParameterizedType)vs.getType());
                    }
                }
                else if (nodeType == 32) {
                    boolean isGeneral = true;
                    final MethodInvocation mi = (MethodInvocation)node;
                    this.checkAndAddStringMethods(mi.getName().getIdentifier());
                    final Expression mexp = mi.getExpression();
                    if (mi.getName().getIdentifier().equals("length") && mi.arguments().isEmpty()) {
                        this.addConcept("java.lang.String.length");
                        isGeneral = false;
                    }
                    if (this.concepts.contains("java.util.ArrayList")) {
                        if (mi.getName().getIdentifier().equals("size") && mi.arguments().isEmpty()) {
                            this.addConcept("java.util.ArrayList.size");
                            isGeneral = false;
                        }
                        else if (mi.getName().getIdentifier().equals("add")) {
                            this.addConcept("java.util.ArrayList.add");
                            isGeneral = false;
                        }
                        else if (mi.getName().getIdentifier().equals("get")) {
                            this.addConcept("java.util.ArrayList.get");
                            isGeneral = false;
                        }
                        else if (mi.getName().getIdentifier().equals("remove")) {
                            this.addConcept("java.util.ArrayList.remove");
                            isGeneral = false;
                        }
                        else if (mi.getName().getIdentifier().equals("set")) {
                            this.addConcept("java.util.ArrayList.set");
                            isGeneral = false;
                        }
                    }
                    if (mexp instanceof StringLiteral) {
                        isGeneral = false;
                        this.addConcept("StringDataType");
                        this.addConcept("StringLiteralMethodInvocation");
                        if (mi.getName().getIdentifier().equals("substring")) {
                            this.addConcept("java.lang.String.substring");
                        }
                    }
                    if (mexp instanceof QualifiedName) {
                        final QualifiedName mq = (QualifiedName)mexp;
                        if (mq.toString().equals("System.out")) {
                            isGeneral = false;
                            if (mi.getName().toString().equals("println")) {
                                this.addConcept("java.lang.System.out.println");
                            }
                            else if (mi.getName().toString().equals("print")) {
                                this.addConcept("java.lang.System.out.print");
                            }
                        }
                    }
                    else if (mexp instanceof SimpleName) {
                        if (((SimpleName)mexp).getIdentifier().equals("Double") && mi.getName().getIdentifier().equals("parseDouble")) {
                            this.addConcept("java.lang.Double.parseDouble");
                            isGeneral = false;
                        }
                        else if (((SimpleName)mexp).getIdentifier().equals("Integer") && mi.getName().getIdentifier().equals("parseInt")) {
                            this.addConcept("java.lang.Integer.parseInt");
                            isGeneral = false;
                        }
                        else if (((SimpleName)mexp).getIdentifier().equals("Math")) {
                            isGeneral = false;
                            if (mi.getName().getIdentifier().equals("pow")) {
                                this.addConcept("java.lang.Math.pow");
                            }
                            else if (mi.getName().getIdentifier().equals("round")) {
                                this.addConcept("java.lang.Math.round");
                            }
                            else if (mi.getName().getIdentifier().equals("sqrt")) {
                                this.addConcept("java.lang.Math.sqrt");
                            }
                            else if (mi.getName().getIdentifier().equals("ceil")) {
                                this.addConcept("java.lang.Math.ceil");
                            }
                            else if (mi.getName().getIdentifier().equals("abs")) {
                                this.addConcept("java.lang.Math.abs");
                            }
                            else if (mi.getName().getIdentifier().equals("max")) {
                                this.addConcept("java.lang.Math.max");
                            }
                            else if (mi.getName().getIdentifier().equals("min")) {
                                this.addConcept("java.lang.Math.min");
                            }
                            else if (mi.getName().getIdentifier().equals("floor")) {
                                this.addConcept("java.lang.Math.floor");
                            }
                        }
                    }
                    if (mexp != null && !(mexp instanceof StringLiteral) && isGeneral) {
                        this.addConcept("ObjectMethodInvocation");
                    }
                    if (!mi.arguments().isEmpty()) {
                        this.addConcept("ActualMethodParameter");
                    }
                }
                else if (nodeType == 41) {
                    final ReturnStatement rs = (ReturnStatement)node;
                    if (rs.getExpression() instanceof StringLiteral) {
                        this.addConcept("StringLiteral");
                        this.addConcept("StringDataType");
                    }
                    if (rs.getExpression() instanceof ClassInstanceCreation) {
                        final ClassInstanceCreation ci2 = (ClassInstanceCreation)rs.getExpression();
                        this.checkAndAddWrapperClassTypes(ci2.getType());
                    }
                }
                else if (nodeType == 40) {
                    if (((QualifiedName)node).getName().getIdentifier().equals("length")) {
                        this.addConcept("ArrayLength");
                    }
                }
                else if (nodeType == 14) {
                    final ClassInstanceCreation ci3 = (ClassInstanceCreation)node;
                    this.checkAndAddWrapperClassTypes(ci3.getType());
                    if (ci3.getType().isParameterizedType()) {
                        this.addConcept("GenericObjectCreationStatement");
                    }
                    else if (ci3.getType().isSimpleType()) {
                        if (ci3.getType().toString().equals("String")) {
                            this.addConcept("StringCreationStatement");
                            this.addConcept("StringConstructorCall");
                        }
                        else if (!this.isAutoBoxingType(ci3.getType())) {
                            this.addConcept("ObjectCreationStatement");
                            this.addConcept("ConstructorCall");
                        }
                        else {
                            this.addConcept("WrapperClassCreationStatement");
                            this.addConcept("WrapperClassConstructorCall");
                        }
                    }
                    if (!ci3.arguments().isEmpty()) {
                        this.addConcept("ActualMethodParameter");
                    }
                }
                else if (nodeType == 44) {
                    final SingleVariableDeclaration sv = (SingleVariableDeclaration)node;
                    if (sv.getType().toString().equals("Object")) {
                        this.addConcept("java.lang.Object");
                    }
                    this.checkAndAddWrapperClassTypes(sv.getType());
                }
                else if (nodeType == 55) {
                    final TypeDeclaration td2 = (TypeDeclaration)node;
                    if (td2.getName().getIdentifier().toLowerCase().contains("exception")) {
                        this.addConcept("ExceptionClass");
                    }
                }
                this.index(this.curNode = childNode);
            }
            else {
                final ChildListPropertyDescriptor list2 = (ChildListPropertyDescriptor)descriptor;
                final List<Object> temp = (List<Object>)node.getStructuralProperty(list2);
                if (list2.getId().equals("superInterfaceTypes") && !temp.isEmpty()) {
                    this.addConcept("ImplementsSpecification");
                    this.addConcept("MethodImplementation");
                }
                if (temp.isEmpty()) {
                    continue;
                }
                final Iterator<Object> itr = temp.iterator();
                while (itr.hasNext()) {
                    this.index(this.curNode = (ASTNode) itr.next());
                }
            }
        }
    }
    
    private void checkAndAddStringMethods(final String name) {
        if (name.equals("charAt")) {
            this.addConcept("java.lang.String.charAt");
        }
        else if (name.equals("equals")) {
            this.addConcept("java.lang.String.equals");
        }
        else if (name.equals("equalsIgnoreCase")) {
            this.addConcept("java.lang.String.equalsIgnoreCase");
        }
        else if (name.equals("replace")) {
            this.addConcept("java.lang.String.replace");
        }
        else if (name.equals("substring")) {
            this.addConcept("java.lang.String.substring");
        }
    }
    
    private void checkAndAddWrapperClassTypes(final Type type) {
        if (type.toString().equals("Double")) {
            this.addConcept("java.lang.Double");
        }
        else if (type.toString().equals("Integer")) {
            this.addConcept("java.lang.Integer");
        }
        else if (type.toString().equals("Float")) {
            this.addConcept("java.lang.Float");
        }
        else if (type.toString().equals("Short")) {
            this.addConcept("java.lang.Short");
        }
        else if (type.toString().equals("Long")) {
            this.addConcept("java.lang.Long");
        }
        else if (type.toString().equals("Byte")) {
            this.addConcept("java.lang.Byte");
        }
        else if (type.toString().equals("Character")) {
            this.addConcept("java.lang.Character");
        }
        else if (type.toString().equals("Boolean")) {
            this.addConcept("java.lang.Boolean");
        }
    }
    
    private void addPrimitiveTypeConcept(final PrimitiveType pType) {
        if (pType.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN) {
            this.addConcept("BooleanDataType");
        }
        else if (pType.getPrimitiveTypeCode() == PrimitiveType.BYTE) {
            this.addConcept("ByteDataType");
        }
        else if (pType.getPrimitiveTypeCode() == PrimitiveType.CHAR) {
            this.addConcept("CharDataType");
        }
        else if (pType.getPrimitiveTypeCode() == PrimitiveType.DOUBLE) {
            this.addConcept("DoubleDataType");
        }
        else if (pType.getPrimitiveTypeCode() == PrimitiveType.FLOAT) {
            this.addConcept("FloatDataType");
        }
        else if (pType.getPrimitiveTypeCode() == PrimitiveType.INT) {
            this.addConcept("IntDataType");
        }
        else if (pType.getPrimitiveTypeCode() == PrimitiveType.LONG) {
            this.addConcept("LongDataType");
        }
        else if (pType.getPrimitiveTypeCode() == PrimitiveType.SHORT) {
            this.addConcept("ShortDataType");
        }
        else if (pType.getPrimitiveTypeCode() == PrimitiveType.VOID) {
            this.addConcept("VoidDataType");
        }
    }
    
    private Statement getNestedStatement(final Statement body) {
        if (body instanceof Block) {
            final List<Statement> statements = (List<Statement>)((Block)body).statements();
            for (final Statement s : statements) {
                if (this.isCommonStatement(s)) {
                    return s;
                }
            }
        }
        else if (this.isCommonStatement(body)) {
            return body;
        }
        return null;
    }
    
    private boolean isCommonStatement(final Statement s) {
        return s instanceof DoStatement | s instanceof EnhancedForStatement | s instanceof ForStatement | s instanceof IfStatement | s instanceof SwitchStatement | s instanceof TryStatement | s instanceof WhileStatement;
    }
    
    private boolean isOverridingToString(final MethodDeclaration md) {
        return (md.getName().toString().equals("toString") & md.getReturnType2() != null) && md.getReturnType2().toString().equals("String") && md.parameters().size() == 0;
    }
    
    private boolean isOverridingEquals(final MethodDeclaration md) {
        if ((md.getName().toString().equals("equals") & md.getReturnType2() != null) && md.getReturnType2().toString().equals("boolean")) {
            final List<Object> params = (List<Object>)md.parameters();
            if (params.size() == 1) {
                final Object o = params.get(0);
                if (o instanceof SingleVariableDeclaration) {
                    final SingleVariableDeclaration sv = (SingleVariableDeclaration)o;
                    if (sv.getType().toString().equals("Object")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean isConceptNode(final ASTNode node) {
        final int type = node.getNodeType();
        return !(type == 34 | type == 13 | type == 57);
    }
    
    private void addConcept(final String concept) {
        if (!this.concepts.contains(concept)) {
            this.concepts.add(concept.toString());
        }
        final int startLineNumber = this.unit.getLineNumber(this.curNode.getStartPosition()) - 1;
        final int nodeLength = this.curNode.getLength();
        int endLineNumber = this.unit.getLineNumber(this.curNode.getStartPosition() + nodeLength) - 1;
        if (startLineNumber < 0 & endLineNumber < 0) {
            return;
        }
        if (concept.equals("ClassDefinition")) {
            endLineNumber = startLineNumber;
        }
        final List<AbstractTypeDeclaration> types = (List<AbstractTypeDeclaration>)this.unit.types();
        String className = types.get(0).getName().getIdentifier();
        if (this.isTester) {
            className = "Tester";
        }
        this.export.insertContentConcept(this.content_name, concept, startLineNumber, endLineNumber);
    }
    
    private void addConcept(final String concept, final int startPosition, final int length) {
        if (!this.concepts.contains(concept)) {
            this.concepts.add(concept.toString());
        }
        final int startLineNumber = this.unit.getLineNumber(startPosition) - 1;
        int endLineNumber = this.unit.getLineNumber(startPosition + length) - 1;
        if (startLineNumber < 0 & endLineNumber < 0) {
            return;
        }
        if (concept.equals("ClassDefinition")) {
            endLineNumber = startLineNumber;
        }
        final List<AbstractTypeDeclaration> types = (List<AbstractTypeDeclaration>)this.unit.types();
        String className = types.get(0).getName().getIdentifier();
        if (this.isTester) {
            className = "Tester";
        }
        this.export.insertContentConcept(this.content_name, concept, startLineNumber, endLineNumber);
    }
    
    private boolean isAutoBoxingType(final Type type) {
        String[] boxingTypes;
        for (int length = (boxingTypes = this.boxingTypes).length, i = 0; i < length; ++i) {
            final String s = boxingTypes[i];
            if (s.equals(type.toString())) {
                return true;
            }
        }
        return false;
    }
    
    private void checkAndAddStringVariableConcept(final Type t) {
        if (t.toString().equals("String")) {
            this.addConcept("StringVariable");
            this.addConcept("StringDataType");
        }
    }
    
    private void checkAndAddStringParametrizedType(final ParameterizedType pt) {
        final List<Type> types = (List<Type>)pt.typeArguments();
        for (final Type t : types) {
            if (t.toString().equals("String")) {
                this.addConcept("StringDataType");
            }
        }
    }
    
    private void checkAndAddObjectVariableConcept(final Type t) {
        if (t.toString().equals("Object")) {
            this.addConcept("java.lang.Object");
        }
    }
    
    public void clearParserData() {
        this.unit = null;
        this.curNode = null;
        this.clearLists();
    }
    
    public void parseExample(final Export export, final String title, final String code, final boolean isTester) {
        this.isExample = true;
        this.parse(export, title, code, isTester);
    }
}
