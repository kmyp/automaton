package jp.ac.nitech.en.kim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * Auther:Y.J.kim
 * date:2016 may 26
 * 名工大2年のオートマトンの課題を解く
 * というかそのオートマトンを
 * プログラムとして実装してみた。
 * できるだけわかりやすく書くので
 * ゆっくりしてってね～
 *
 * 状態遷移図をもとにガリガリやってくよ。
 * 状態遷移図は教科書の回答にのってるやつを使うね。
 * 基本メイン関数以外は説明しないよ。
 * 何をする関数なのかは関数名で推測してね。
 * 勉強のために読んでみることをオヌヌメするよ
 *
 * しゃーなしでハードコーディングしています。
 * 受理常態から遷移する場合の実装してないです。
 * あと、左端のblankの実装してないよ・・・笑
 * ↑忘れてた
 *
 *
 *
 * 教科書の課題4.2をやってくよ～
 *
 */


public class automaton001 {

	static ArrayList<String> tape = new ArrayList<String>();
	static int tapePointer;

	public static void main(String[] args) throws InterruptedException {
		//とりあえず文字列を入力で受け取るよ
		Scanner stdIn  = new Scanner(System.in);
		System.out.print("input strings:");
		String str = stdIn.nextLine();
		stdIn.close();

		//aの連続以外はじくよ～ 突然のtry catch節
		if(!checkInput(str)){
			try {
				throw new Exception("invalid input("+str+") exception...");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//テープは頭が＃ お尻が＄にしたよ。
		tape = convetStrToTape(str);
		tapePointer = 1;
		State now;
		HashMap<String,State> states;
		printTape();

		//各状態遷移はcreateStates()でハードコーディングしているよ
		states = createStates();
		now = states.get("q0");

		//テープを読んで状態を遷移するよ
		while(!now.isAcception()){//accept:受理するまでループループ
			System.out.println("now:"+now.getName());
			if(tapePointer>=tape.size()){
				System.out.println("input:blank");
			}else{
 				System.out.println("input:"+tape.get(tapePointer));
			}
			System.out.println("index:"+tapePointer);
			String trans = null;
			try {
				trans = now.transision();
			} catch (Exception e) {
				if(tapePointer>=tape.size()){
					System.out.println("input:blank");
				}else{
					System.out.println("input:"+tape.get(tapePointer));
				}
				now.debug();
				e.printStackTrace();
			}
			printTape();
			now = states.get(trans);
		}
		System.out.println("input accepted!!\n");
	}


	//テープ表示
	static void printTape(){
		for(String chara: tape){
			System.out.print("["+chara+"]");
		}
		System.out.println();
	}

	//入力をテープに、テープの形は複雑にした... blanとか先頭後尾を実装するため
	private static ArrayList<String> convetStrToTape(String str) {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add("start");
		for(int i=0;i<str.length();i++){
			Character ch = new Character (str.toCharArray()[i]);
			ret.add(ch.toString());
		}
		ret.add("fin");

		return ret;
	}

	//いろいろ考えた結果ハードコーディングするよ
	private static HashMap<String, State> createStates() {
		HashMap<String, State> ret = new HashMap<String, State>();


		//q0
		State q0 = new State("q0", false);
		q0.addTransision("A'/a,R", "q0");
		q0.addTransision("a/A,R", "q1");
		q0.addTransision("fin/fin,L", "qf");
		ret.put("q0", q0);

		//q1
		State q1 = new State("q1", false);
		q1.addTransision("A'/A',R", "q1");
		q1.addTransision("a/a,R", "q1");
		q1.addTransision("fin/A',R", "q2");
		ret.put("q1", q1);

		//q2
		State q2 = new State("q2", false);
		q2.addTransision("blank/fin,L", "q3");
		ret.put("q2", q2);

		//q3
		State q3 = new State("q3", false);
		q3.addTransision("A'/A',L", "q3");
		q3.addTransision("a/a,L", "q3");
		q3.addTransision("A/a,R", "q0");
		ret.put("q3", q3);

		//qf
		State qf = new State("qf", true);
		ret.put("qf", qf);

		return ret;
	}

	//入力のチェック　今回はaのみかどうか
	private static boolean checkInput(String str) {
		boolean ret=false;
		Pattern p = Pattern.compile("^a+$");
		Matcher m = p.matcher(str);
		ret = m.find();
		return ret;
	}

	//状態のためのクラス　隣の状態と　入力に対する遷移を保持する　それが受理常態かどうかも
	static class State{
		HashMap<String,String> connection;
		HashMap<String,String[]> code;
		private boolean accept=false;
		private String stateName;
		State(String name,boolean acc){
			connection = new HashMap<String, String>();
			code = new HashMap<String, String[]>();
			stateName = name;
			accept=acc;
		}

		String getName(){
			return stateName;
		}

		void addTransision(String code,String s2){//入力からテープを変えるのと遷移先を保存
			code=code.replace("/", ",");
			String[] codes = code.split(",");
			connection.put(codes[0], s2);
			String[] tmp = {codes[1],codes[2]};
			this.code.put(codes[0], tmp);
		}

		String transision() throws Exception{//入力から遷移先を返す
			String input;
			if(tapePointer>=tape.size()){
				input = "blank";
			}else{
				input = tape.get(tapePointer);
			}
			String[] str = this.code.get(input);
			if(str==null){
				throw new Exception("unaccepted...");
			}
			if(tapePointer<tape.size()){
				tape.remove(tapePointer);
			}
			tape.add(tapePointer,str[0]);
			if(str[1].equals("R")){
				tapePointer++;
			}else if(str[1].equals("L")){
				tapePointer--;
			}else{
				throw new Exception("unaccepted...");
			}
			return connection.get(input);
		}

		boolean isAcception(){
			return accept;
		}

		void debug() throws InterruptedException{
			System.out.println("--------------------------------debug info-----------------------------------\npointer");
			System.out.println(tapePointer+"\ncon");
			for (Entry<String, String> entry : connection.entrySet()) {
			    System.out.println(entry.getKey() + " => " + entry.getValue());
			}
			System.out.println("-------------------------------------------------------------------\ncode");

			for (Entry<String, String[]> entry : code.entrySet()) {
			    System.out.println(entry.getKey() + " => " + entry.getValue()[0]+","+entry.getValue()[1]);
			}
			Thread.sleep(500);
		}
	}
}
