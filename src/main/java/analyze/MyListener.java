package analyze;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.PrintWriter;

import graph.*;
import variable.VariableData;
import variable.VariableLocation;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import grammar.CPP14ParserBaseListener;
import grammar.CPP14Parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

 public class MyListener extends CPP14ParserBaseListener{

	 private String fileName = null;
	 private int skill;
	 private int nodeNum = 0;
	 private int prevTokenNum = 0;
	 private boolean lastLexicalUseFlag = false;
	 private List<Node> nodeList= new ArrayList<Node>();
	 private List<List<Integer>> childEdgeList= new ArrayList<List<Integer>>();
	 private List<List<Integer>> nextEdgeList= new ArrayList<List<Integer>>();
	 private List<List<Integer>> lastLexicalUseEdgeList= new ArrayList<List<Integer>>();
	 private Stack<Node> nodeStack = new Stack<Node>();
	 private Stack<Boolean> isToPopStack = new Stack<Boolean>();
	 private Map<String, Deque<VariableLocation>> multiVarNameMap;
	 private Map<Integer, VariableData> idVarMap;
	 private Map<String, Integer> singleCheckMap;


	 public MyListener(String fileName, int skill, Map<String, Deque<VariableLocation>> multiVarNameMap, Map<Integer, VariableData> idVarMap, Map<String, Integer> singleCheckMap) {
		 this.fileName = fileName;
		 this.skill = skill;
		 this.multiVarNameMap = multiVarNameMap;
		 this.idVarMap = idVarMap;
		 this.singleCheckMap = singleCheckMap;
	 }

	 @Override
	 public void enterEveryRule(ParserRuleContext ctx) {
		 Node node = new Node();
		 List<Integer> childEdge = new ArrayList<Integer>();
		 Boolean isToPop = true;;

		 //現在のノードの情報を入れる
		 node.setNodeNum(nodeNum);
		 node.setNodeName(CPP14Parser.ruleNames[ctx.getRuleIndex()]);

		 //親ノードがあるときにエッジを追加
		 if(!nodeStack.empty()) {
			 Node parentNode = (Node) nodeStack.pop();
			 childEdge.add(parentNode.getNodeNum());
			 childEdge.add(nodeNum);
			 //子が複数またはトークンに接続しているノードは追加する
			 if(ctx.getChildCount()!=1 || ctx.getChild(0).getChildCount()==0) {
				 childEdgeList.add(childEdge);
				 nodeList.add(node);
				 nodeStack.push(parentNode);
				 nodeNum += 1;
				 nodeStack.push(node);
				 isToPop = true;
			 }else {
				 nodeStack.push(parentNode);
				 isToPop = false;
			 }
		 }else {
			 nodeStack.push(node);
			 nodeList.add(node);
			 nodeNum += 1;
		 }
		 isToPopStack.push(isToPop);
	 }

	 @Override
	 public void exitEveryRule(ParserRuleContext ctx) {
		 if(isToPopStack.pop()) {
			 nodeStack.pop();
		 }
	 }

	 @Override
	 public void enterUnqualifiedId(CPP14Parser.UnqualifiedIdContext ctx) {
		 lastLexicalUseFlag = true;
	 }

	 @Override
	 public void enterClassName(CPP14Parser.ClassNameContext ctx) {
		 lastLexicalUseFlag = true;
	 }

	 @Override
	 public void visitTerminal(TerminalNode node) {
		 Node addNode = new Node();
		 List<Integer> childEdge = new ArrayList<Integer>();
		 List<Integer> nextEdge = new ArrayList<Integer>();
		 addNode.setNodeName(node.getText());
		 addNode.setNodeNum(nodeNum);
		 nodeList.add(addNode);

		 Node parentNode = (Node) nodeStack.pop();
		 childEdge.add(parentNode.getNodeNum());
		 childEdge.add(nodeNum);
		 childEdgeList.add(childEdge);
		 nodeStack.push(parentNode);

		 if(prevTokenNum != 0) {
			 nextEdge.add(prevTokenNum);
			 nextEdge.add(nodeNum);
			 nextEdgeList.add(nextEdge);
		 }

		 if(lastLexicalUseFlag) {
			 Deque<VariableLocation> var = multiVarNameMap.get(node.getText());
			 Integer id = null;
			 if(var!=null) {
				 VariableLocation loc = var.poll();
				 if(loc!=null) {
					 id = loc.getId();
				 }
			 }else {
				 id = singleCheckMap.get(node.getText());
			 }
			 if(id!=null) {
				 VariableData data = idVarMap.get(id);
				 int lastLexicalUseNode = data.getLastLexicalUse();
				 if(lastLexicalUseNode > 0) {
					 List<Integer> lastLexicalUseEdge = new ArrayList<Integer>();
					 lastLexicalUseEdge.add(lastLexicalUseNode);
					 lastLexicalUseEdge.add(nodeNum);
					 lastLexicalUseEdgeList.add(lastLexicalUseEdge);
				 }
				 data.setLastLexicalUse(nodeNum);
			 }
			 lastLexicalUseFlag = false;

		 }

		 prevTokenNum = nodeNum;
		 nodeNum += 1;
	 }

	 public void outputGraph(PrintWriter pw) {
		 Edges edges = new Edges(childEdgeList, nextEdgeList, lastLexicalUseEdgeList);
		 Map<String, String> nodeLabels = createNodeLabels();
		 ContextGraph cg = new ContextGraph(edges, nodeLabels);
		 Graph graph = new Graph(fileName, cg, skill);

		 ObjectMapper mapper = new ObjectMapper();
		 try {
			 String jsonString = mapper.writeValueAsString(graph);
			 pw.print(jsonString);
		 }catch(JsonProcessingException e) {
			 e.printStackTrace();
		 }
	 }

	 private LinkedHashMap<String, String> createNodeLabels() {
		 LinkedHashMap<String, String> nodeLabels = new LinkedHashMap<String, String>();
		 nodeList.forEach(node -> nodeLabels.put(String.valueOf(node.getNodeNum()), node.getNodeName()));
		 return nodeLabels;
	 }

}
