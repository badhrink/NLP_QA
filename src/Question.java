import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;

/**
 * Created by badhr on 4/7/2016.
 */
public class Question {
    public Question(String qn){
        this.text = qn;
    }
    public class TokenNER{
        String token;
        String ner;
        public TokenNER(String tokenArg, String nerArg){
            this.token = tokenArg;
            this.ner = nerArg;
        }
    }
    String text;
    String qnCategory;
    String dependencyRoot;
    List<String> nounList;
    List<String> verbList;
    List<Tree> parseTree;
    SemanticGraph dependencyGraph;
    List<TokenNER> tokenNerList;
    String whQuestionType;
    public void AddTokenNer(String tokenArg, String nerArg){
        TokenNER tN = new TokenNER(tokenArg,nerArg);
        if(tokenNerList == null){
            tokenNerList = new ArrayList<TokenNER>();
        }
        tokenNerList.add(tN);
       
    }
    public void printResult(){
    	System.out.println("<QUESTION> " + this.text);
    	System.out.println("<CATEGORY> " + this.qnCategory);
    	System.out.println("<PARSETREE>"/* + this.parseTree.get(0).toString() + "\n"*/);
    	this.parseTree.get(0).pennPrint();
    	System.out.println("");
    }
}
