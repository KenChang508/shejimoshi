package Interpreter;

public class InterpreterDemo {
    public static Expression getMaleExpression(){
        TerminalExpression robert = new TerminalExpression("Robert");
        TerminalExpression john = new TerminalExpression("John");
        return new OrExpression(robert, john);
    }

    public static Expression getMarriedWomanExpression(){
        TerminalExpression julie = new TerminalExpression("Julie");
        TerminalExpression married = new TerminalExpression("Married");
        return new AndExpression(julie, married);
    }

    public static void main(String[] args) {
        Expression isMale = getMaleExpression();
        System.out.println("Robert是男性吗？" + isMale.interpret("Robert"));
        System.out.println("John是男性吗？" + isMale.interpret("John"));

        Expression isMarriedWoman = getMarriedWomanExpression();
        System.out.println("Julie是已婚女性吗？" + isMarriedWoman.interpret("Married Julie"));
    }
}
