# CppToGraph
[Learning to Represent Programs with Graphs](https://arxiv.org/abs/1711.00740)で考案されたソースコードのグラフ変換をC++コードに対して実行する．プログラムの解析にはUnderstandとantlrを利用する．グラフは構文木に以下の2種類のエッジを追加したもので構成される．
- トークンの前後関係を表すエッジ
- 同一変数の参照情報を表すエッジ

[sample.cpp](data/sample.cpp)をUnderstandで解析し，このプログラムを実行することで[sample.json](data/sample.json)が出力される．このjsonファイルに変換されたグラフのノード情報とエッジ情報が格納される．

# 開発環境
Java: OpenJDK8 
Gradle: Gradle 5.6.1  
Understand: 5.1

# 利用方法
## 事前準備
事前に解析を行うソースコードを含むディレクトリを解析したUnderstandプロジェクトを用意する．

以下のコマンドをコマンドプロンプト上で実行すると，ディレクトリfugafugaを解析するUnderstandプロジェクトhoge.udbを作成し，解析が行われる．詳細はUnderstandのマニュアルを確認
### 例
```
und create -db .\und\hoge.udb –languages c++ add fugafuga analyze –all
```
## ビルド・実行
Gradleでビルドを行う．Understandのライブラリはローカル依存となるため，build.gradle内の記述とローカル環境に違いがないか確認しておく．  
ビルドを行うとbuild\libs以下にjarファイルが生成される．

jarファイルにskill(目的変数)，解析対象のディレクトリ(またはファイル)，Understandプロジェクト，結果を出力するディレクトリの順に引数を与えて実行すると解析が行われる．jarファイルの実行はWindows Powershell上で行うことを推奨．  

### 例
```
.\gradlew build  
java -jar .\build\libs\CppToGraphWithUnd.jar 1 fugafuga .\und\hoge.udb .\data\graph\
```

## 解析中のエラー
基本的にエラーメッセージや，そのエラーメッセージを出力している付近のソースコードを確認すると原因は特定できる．ただし，正しく実行しているにもかかわらず，"指定したUnderstandプロジェクト内に存在しないファイルの解析が行われました"と表示される場合，解析ファイルの名前からUnderstandプロジェクト内のファイルのデータに正しくアクセスできていない可能性が高い．

ファイルのデータへのアクセスにはuniquenameと呼ばれるものを利用している．解析ファイルの名前を加工してuniquenameの生成を試みているが，環境によってuniquenameに絶対パスが利用されていたり相対パスが利用されていたりすることがある．このエラーが発生する場合はuniquename周りを確認すること．なお，解析対象ディレクトリをEドライブ，UnderstandプロジェクトをCドライブに配置し，Windows Powershell上で実行した場合は正しく動作することを確認している．(コマンドプロンプト上ではエラーが発生した)