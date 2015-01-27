package quickfix;

import static edu.umd.cs.findbugs.plugin.eclipse.quickfix.util.ASTUtil.getASTNode;

import java.util.List;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.plugin.eclipse.quickfix.ApplicabilityVisitor;
import edu.umd.cs.findbugs.plugin.eclipse.quickfix.BugResolution;
import edu.umd.cs.findbugs.plugin.eclipse.quickfix.CustomLabelVisitor;
import edu.umd.cs.findbugs.plugin.eclipse.quickfix.exception.BugResolutionException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.projection.Fragment;

import util.TraversalUtil;

public class CompareFloatResolution extends BugResolution {

    @Override
    protected boolean resolveBindings() {
        return true;
    }

    @Override
    protected ASTVisitor getApplicabilityVisitor() {
        return new CompareToVisitor();
    }
    
    @Override
    protected ASTVisitor getCustomLabelVisitor() {
        return new CompareToVisitor();
    }

    @Override
    protected void repairBug(ASTRewrite rewrite, CompilationUnit workingUnit, BugInstance bug) throws BugResolutionException {
        ASTNode node = getASTNode(workingUnit, bug.getPrimarySourceLineAnnotation());
        node = TraversalUtil.backtrackToBlock(node);
        CompareToVisitor visitor = new CompareToVisitor();
        node.accept(visitor);

        if (visitor.expressionToReplace != null) {

            AST ast = rewrite.getAST();
            ast.newSimpleName(visitor.firstFloat.getIdentifier());
            MethodInvocation newMethod = ast.newMethodInvocation();
            newMethod.setName(ast.newSimpleName("compare"));
            newMethod.setExpression(ast.newSimpleName(visitor.floatOrDouble));
            newMethod.arguments().add(rewrite.createCopyTarget(visitor.firstFloat));
            newMethod.arguments().add(rewrite.createCopyTarget(visitor.secondFloat));
            
            if (visitor.optionalTempVariableToDelete != null) {
                rewrite.remove(visitor.optionalTempVariableToDelete, null);
            }
            rewrite.replace(visitor.expressionToReplace, newMethod, null);
        }
    }

    private static class CompareToVisitor extends ASTVisitor implements ApplicabilityVisitor, CustomLabelVisitor {

        ConditionalExpression expressionToReplace;

        VariableDeclarationStatement optionalTempVariableToDelete;

        SimpleName firstFloat;

        SimpleName secondFloat;

        String floatOrDouble;

        @Override
        public boolean visit(ConditionalExpression node) {
            if (expressionToReplace != null) {
                return false;
            }
            
            if (node.getExpression() instanceof InfixExpression) {
                InfixExpression condExpr = (InfixExpression) node.getExpression();
                boolean retVal = findFirstAndSecondFloat(node, condExpr);
                if (condExpr.getOperator() == InfixExpression.Operator.GREATER) {
                    return retVal;
                } 
                else if (condExpr.getOperator() == InfixExpression.Operator.LESS) {
                    swapFirstAndSecondFloat();
                    return retVal;
                } 
            }
            return true;
        }

        private boolean findFirstAndSecondFloat(ConditionalExpression node, InfixExpression condExpr) {
            if (areSimpleNames(condExpr.getLeftOperand(), condExpr.getRightOperand())) {
                firstFloat = (SimpleName) condExpr.getLeftOperand();
                secondFloat = (SimpleName) condExpr.getRightOperand();
                expressionToReplace = node;
                floatOrDouble = getFloatOrDouble(firstFloat, secondFloat);
            } else {
                // diff
                try {
                    if (condExpr.getLeftOperand() instanceof SimpleName) {
                        findDiffAndFloats((SimpleName) condExpr.getLeftOperand());
                    } else if (condExpr.getRightOperand() instanceof SimpleName) {
                        findDiffAndFloats((SimpleName) condExpr.getRightOperand());
                    } else {
                        return true; // unexpected comparison
                    }

                } catch (CouldntFindDiffException e) {
                    return true; // keep nesting if we have a problem
                }
            }
            return false;
        }

        private void swapFirstAndSecondFloat() {
            SimpleName temp = firstFloat;
            firstFloat = secondFloat;
            secondFloat = temp;
        }

        private void findDiffAndFloats(SimpleName diffName) throws CouldntFindDiffException {
            ConditionalExpression originalLine = TraversalUtil.findClosestAncestor(diffName, ConditionalExpression.class);
            
            if (originalLine == null) {
                throw new CouldntFindDiffException();
            }
            
            findOptionalDiffStatement(originalLine, diffName);
            
        }

        private void findOptionalDiffStatement(ASTNode originalLine, SimpleName diffName) {
            Block surroundingBlock = TraversalUtil.findClosestAncestor(originalLine, Block.class);
            
            List<Statement> blockStatements = surroundingBlock.statements();
            for(int i = blockStatements.indexOf(originalLine); i>= 0;i--) {
                Statement statement = blockStatements.get(i);
                if (statement instanceof VariableDeclarationStatement) {
                    List<VariableDeclarationFragment> frags = ((VariableDeclarationStatement) statement).fragments();
                    if (frags.size() == 1) {
                        // I won't fix the the diff variable if it's nested with other frags, if they exist
                        VariableDeclarationFragment fragment = frags.get(0);
                        if (fragment.getName().getIdentifier().equals(diffName.getIdentifier())) {
                            this.optionalTempVariableToDelete = (VariableDeclarationStatement) statement;
                        }
                    }
                }
            }
        }

        private String getFloatOrDouble(SimpleName... variables) {
            boolean isDouble =false;
            for(SimpleName v : variables) {
                if ("double".equals(v.resolveTypeBinding().getQualifiedName())) {
                    isDouble = true;
                }
            }
            return isDouble ? "Double" : "Float";
        }

        private boolean areSimpleNames(Expression... expressions) {  //returns true if all expressions are simple names
            for (Expression e: expressions) {
                if (!(e instanceof SimpleName)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean isApplicable() {
            return expressionToReplace != null;
        }
        
        @Override
        public String getLabelReplacement() {
            return String.format("%s.compare(%s,%s)", this.floatOrDouble, this.firstFloat, this.secondFloat);
        }
        
        
    }
    
    private static class CouldntFindDiffException extends Exception {
        
    }

}
