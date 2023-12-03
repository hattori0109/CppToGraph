import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import analyze.CppAnalyzer;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import com.scitools.understand.Understand;
import com.scitools.understand.UnderstandException;
import com.scitools.understand.Database;

public class CppToGraph {
	public static int countTotal = 0;
	public static int countError = 0;
	public static int skill = 0;
	private static Database database = null;

	public static void main(String[] args) {
		if(args.length != 4) {
			System.out.println("\u001b[00;31m引数にはskill+解析ディレクトリ(ファイル)+Understandプロジェクト+出力ディレクトリを指定してください\u001b[00m");
			System.exit(1);
		}
		try {
			skill = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.println("\u001b[00;31mskillの値が正しくありません\u001b[00m");
			System.exit(1);
		}
		try {
			database = Understand.open(args[2]);
		} catch(UnderstandException e) {
			System.out.println("\u001b[00;31mUnderstandプロジェクトの読み込みに失敗しました\u001b[00m");
			System.exit(1);
		}
		File file = new File(args[1]);
		File output = new File(args[3]);
		if(!output.isDirectory()) {
			System.out.println("\u001b[00;31m出力ディレクトリが正しくありません\u001b[00m");
			System.exit(1);
		}
		if(file.isFile()) {
			FileWriter fw = null;
			PrintWriter pw = null;
			try {
				fw = new FileWriter(output.getAbsolutePath() + "\\" + getFileName(file) + ".json");
				pw = new PrintWriter(new BufferedWriter(fw));
				CppAnalyzer cppAnalyzer = new CppAnalyzer(database);
				cppAnalyzer.analyzeFile(skill, pw, file);
				pw.close();
			} catch (IOException e) {}
			catch (ParseCancellationException e) {
				System.err.println("cannot parse : " + getFileName(file));
				pw.close();
				File df = new File(output.getAbsolutePath() + "\\" + getFileName(file) + ".json");
				System.out.println(df.delete());
				return;
			}
		}else if(file.isDirectory()) {
			analyzeDirectory(file, output);
			System.out.println("Total file: " + countTotal);
			System.out.println("parsing error: " + countError);
			System.out.println("parsed successfully: " + (countTotal-countError));
		}else {System.out.println("\u001b[00;31m解析対象に指定したディレクトリ(ファイル)は存在しません\u001b[00m");}
		database.close();
	}

	private static String getFileName(File file) {
		String basename = file.getName();
        String woext = basename.substring(0,basename.lastIndexOf('.'));
        return woext;
	}

	private static void analyzeDirectory(File directory, File output) {
		int count = 0;
		CppAnalyzer cppAnalyzer = new CppAnalyzer(database);
		FileWriter fw = null;
		try {
			fw = new FileWriter(output.getAbsolutePath() + "\\" + directory.getName() + ".json");
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
		pw.print("[");
		try {
			File[] fileList = directory.listFiles();
			int length = fileList.length;

			for(File file : fileList) {
				count += 1;
				System.out.print("\r");
				System.out.print("Analyzing " + directory.getAbsolutePath() + "  ... " + count + "/" + length);
				if(file.isFile()) {
					try {
						countTotal += 1;
						cppAnalyzer.analyzeFile(skill, pw, file);
					}catch (ParseCancellationException e) {
						countError += 1;
						System.out.println();
						System.out.println("\u001b[00;31m cannot parse : " + file.getCanonicalPath() + "\u001b[00m");
					}
				}else if(file.isDirectory()) {
					//analyzeDirectory(file, output);
				}
			}

			if(cppAnalyzer.count > 0) {
				pw.print("]");
			}
			System.out.print("\n");
			pw.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}

