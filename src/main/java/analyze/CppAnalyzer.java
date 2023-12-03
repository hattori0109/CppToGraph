package analyze;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.Deque;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.scitools.understand.Database;
import com.scitools.understand.Entity;
import com.scitools.understand.Reference;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import grammar.*;
import variable.*;

/*
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.antlr.v4.gui.TreeViewer;
import java.util.Arrays;
*/


public class CppAnalyzer {
	public int count = 0;
	String fileName = null;
	private Map<String, Deque<VariableLocation>> multiVarNameMap;
	private Map<Integer, VariableData> idVarMap;
	private Map<String, Integer> singleCheckMap;
	private Database database;

	public CppAnalyzer(Database database) {
		this.database = database;
	}

	public void analyzeFile(int skill, PrintWriter pw, File file) throws ParseCancellationException{
		try {
			fileName = file.getCanonicalPath();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		String input = preProcessor(file);
		CharStream stream = CharStreams.fromString(input, file.toString());

		//字句解析
		CPP14Lexer lexer = new CPP14Lexer(stream);
		//構文解析
		CommonTokenStream streamT = new CommonTokenStream(lexer);
		CPP14Parser parser = new CPP14Parser(streamT);
		parser.removeErrorListeners();
		parser.addErrorListener(ThrowingErrorListener.INSTANCE);

		ParseTreeWalker walker = ParseTreeWalker.DEFAULT;

		udbAnalyze(file.getAbsolutePath());

		MyListener listener = new MyListener(fileName, skill, multiVarNameMap, idVarMap, singleCheckMap);
		ParseTree tree = null;
		tree = parser.translationUnit();

		//System.out.println(tree.toStringTree(parser));
		walker.walk(listener, tree);
		if(count != 0) {
			pw.print(", ");
		}
		listener.outputGraph(pw);
		count++;
		//show AST in GUI
        //viewAST(parser, tree);

	}

	//CloneDetectorからコピー
	// プリプロセッサ
	// マクロの除去
	private String preProcessor(File file) {
		StringBuilder buf = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				// #から始まる行の削除
				if (line.startsWith("#")) {
					buf.append("\n");
					// 行の最後が'\'で終わる場合次の行もマクロとして削除
					while (line.matches(".*?\\\\s*")) {
						// System.out.println(file);
						// System.out.println(line);
						line += reader.readLine();
						buf.append("\n");

					}

					// else | elif を見つけたら，マクロによって中括弧の破たんがないか調査
					if (line.matches("#\\s*(else|elif).*")) {
						// if(line.contains("\\")) System.out.println(line);
						//int count = 0;
						int loc = 0, ifcnt = 0;
						StringBuilder subbuf = new StringBuilder();
						while ((line = reader.readLine()) != null) {
							// 中括弧の数を調べる
							//count += line.replaceAll("\\}", "").length() - line.replaceAll("\\{", "").length();
							if (line.matches("#\\s*if.*")) {
								// ifdefのネスト関係を調べる
								ifcnt++;
							} else if (line.matches("#\\s*endif.*")) {
								if (ifcnt != 0) {
									ifcnt--;
								} else {
//									if (count != 0) {
//										// 中括弧の対応関係が破たんしていた場合，その箇所の削除
//										for (int i = 0; i <= loc; i++) {
//											buf.append("\n");
//										}
//										// System.out.println(subbuf.toString());
//									} else {
//										// 中括弧の対応関係が破たんしていない場合，その箇所は残す
//										buf.append(subbuf.toString());
//										buf.append("\n");
//									}
									for (int i = 0; i <= loc; i++) {
										buf.append("\n");
									}
									break;
								}

							}
							subbuf.append(line);
							subbuf.append("\n");
							loc++;
						}
					}
				} else {
					buf.append(line);
					buf.append("\n");
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return buf.toString();
	}

	/*
	private static void viewAST(CPP14Parser parser, ParseTree tree) {
		JFrame frame = new JFrame("Antlr AST");
        JPanel panel = new JPanel();
        TreeViewer viewer = new TreeViewer(Arrays.asList(
                parser.getRuleNames()),tree);
        viewer.setScale(0.7); // Scale a little
        panel.add(viewer);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
	}*/

	private void udbAnalyze(String file) {
		idVarMap = new HashMap<>();
		singleCheckMap = new HashMap<>();
		multiVarNameMap = new HashMap<>();

		String uniquename = "c" + file + "@file=" + file;
		Entity fileEntity = database.lookup_uniquename(uniquename);

		if(fileEntity==null) {
			System.out.println("\\u001b[00;31m指定したUnderstandプロジェクト内に存在しないファイルの解析が行われました\\u001b[00m");
			return;
		}
		else {
			//GlobalVariables
			Reference[] globalVar = fileEntity.refs("", "Global Object",false);
			analyzeRefs(globalVar);
			//Functions
			Reference[] functions = fileEntity.refs("Define", "Function",false);
			for(Reference func : functions) {

				//LocalVariables
				Reference[] localVar = func.ent().refs("", "Local Object", true);
				analyzeRefs(localVar);

				//Parameters
				Reference[] parameters = func.ent().refs("", "Parameter", true);
				analyzeRefs(parameters);

				//StructMemberVariables
				Reference[] structs = func.ent().refs("", "Public Object", true);
				analyzeRefs(structs);
			}
		}
	}

	private void analyzeRefs(Reference[] refs) {
		for(Reference ref: refs) {
			analyzeEntity(ref.ent());
		}
	}

	private void analyzeEntity(Entity ent) {
		Integer nullCheck = null;
		String entName = ent.longname(false);
		Reference[] refs = ent.refs(null, null, false);
		VariableData entData = new VariableData(ent.id(), refs);
		VariableData entityCheck = idVarMap.put(ent.id(), entData);
		if(entityCheck!=null) {return;}
		nullCheck = singleCheckMap.put(entName, ent.id());
		if(nullCheck != null) {
			Deque<VariableLocation> varlocList = multiVarNameMap.get(entName);
			if(varlocList == null) {
				varlocList = idVarMap.get(nullCheck).data;
			}
			multiVarNameMap.put(entName, varlocMerge(entData.data, varlocList));
		}
	}

	private Deque<VariableLocation> varlocMerge(Deque<VariableLocation> deque1, Deque<VariableLocation> deque2){
		Deque<VariableLocation> sortedDeque = new ArrayDeque<>();
		VariableLocation loc1 = deque1.poll();
		VariableLocation loc2 = deque2.poll();
		while(loc1!=null || loc2!=null) {
			if(loc1==null) {
				sortedDeque.add(loc2);
				loc2 = deque2.poll();
			}else if(loc2==null) {
				sortedDeque.add(loc1);
				loc1 = deque1.poll();
			}else {
				boolean comp = varlocCompare(loc1, loc2);
				if(comp==true) {
					sortedDeque.add(loc2);
					loc2 = deque2.poll();
				}else {
					sortedDeque.add(loc1);
					loc1 = deque1.poll();
				}
			}
		}
		return sortedDeque;
	}

	private boolean varlocCompare(VariableLocation loc1, VariableLocation loc2) {
		int line1 = loc1.getLine();
		int line2 = loc2.getLine();
		if(line1 > line2) {return true;}
		else if(line1 < line2) {return false;}
		else {
			int column1 = loc1.getColumn();
			int column2 = loc2.getColumn();
			if(column1 > column2) {return true;}
			else {return false;}
		}
	}
}
