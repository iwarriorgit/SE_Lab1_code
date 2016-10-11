package com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.*;

public class Main {
	public static Pattern p = Pattern.compile("\\+|\\-|\\*|\\^|[a-zA-Z]+|\\d+|\\(|\\)");
	public static Pattern pnum = Pattern.compile("\\d+");
	public static Pattern pmul = Pattern.compile("\\*");
	public static Pattern palph = Pattern.compile("[a-zA-Z]+");
	public static Pattern pop = Pattern.compile("\\+|\\-|\\*|\\^");
	public static Pattern pil = Pattern.compile("[^\\+\\-\\*\\^a-zA-Z0-9\\(\\)\\s]");
	
	public static List<String> split(String str, List<Boolean>index_neg) {
		StringBuffer s = new StringBuffer(str);;
		List<String> re = new LinkedList<String>();
		int plus = s.indexOf("+");
		int minus = s.indexOf("-");
		while (plus != -1 || minus != -1) {
			if (plus != -1 && minus != -1) {
				if (plus < minus) {
					re.add(new String(s.substring(0, plus)));
					s.delete(0, plus + 1);
					index_neg.add(false);
				} else {
					re.add(new String(s.substring(0, minus)));
					s.delete(0, minus + 1);
					index_neg.add(true);
				}
			} else if (plus != -1) {
				re.add(new String(s.substring(0, plus)));
				s.delete(0, plus + 1);
				index_neg.add(false);
			} else {
				re.add(new String(s.substring(0, minus)));
				s.delete(0, minus + 1);
				index_neg.add(true);
			}
			plus = s.indexOf("+");
			minus = s.indexOf("-");
		}
		re.add(new String(s));
		return re;
	}
	public static String refactor(String exp){
		String re = "";
		List<Boolean> index_neg = new ArrayList<Boolean>();
		List<String> exps = split(exp, index_neg);
		int index = 0;
		for (String e : exps) {
			String[] items = e.split("\\*");
			String num = "";
			Map<String, Integer> sym = new HashMap<String, Integer>();
			for (String item : items) {
				if (pnum.matcher(item).matches()) {
					if (num.isEmpty()) {
						num = item;
					} else {
						num += "*" + item;
					}
				} else if (!palph.matcher(item).matches()) {
					String[] tmp = item.split("\\^");
					if (sym.containsKey(tmp[0])) {
						sym.put(tmp[0], sym.get(tmp[0]) + Integer.parseInt(tmp[1]));
					} else {
						sym.put(tmp[0], Integer.parseInt(tmp[1]));
					}
				} else {
					if (sym.containsKey(item)) {
						sym.put(item, sym.get(item) + 1);
					} else {
						sym.put(item, 1);
					}
				}
			}
			String i = num;
			if (!sym.isEmpty()) {
				for (String k : sym.keySet()) {
					if (sym.get(k) != 1) {
						i += "*" + k + "^" + sym.get(k);
					} else {
						i += "*" + k;
					}
						
				}
			}
			if (re.isEmpty()) {
				re = i;
			} else if (index_neg.get(index)) {
				re += "-" + i;
				index += 1;
			} else {
				re += "+" + i;
				index += 1;
			}
		}
		return re;
		
	}
	
	public static List<String> generate_items(String str) {
		Matcher m = p.matcher(str);
        Matcher mil = pil.matcher(str);
        List<String> exp = new LinkedList<String>();
        
        if (mil.find()) {
        	System.out.println("illegal charaters");
    		System.exit(1);
        }
        while(m.find()) {
        	exp.add(m.group());
        }
        for (int i = 0; i < exp.size() - 1; i += 1) {
        	if ((pnum.matcher(exp.get(i)).matches() && (palph.matcher(exp.get(i+1)).matches() || exp.get(i+1).equals("(")))
        		|| (palph.matcher(exp.get(i)).matches() && (pnum.matcher(exp.get(i+1)).matches() || exp.get(i+1).equals("(")))) {
        		exp.add(i+1, new String("*"));
        	}
        	if ((exp.get(i).equals("^") && !pnum.matcher(exp.get(i+1)).matches())
        		|| (pop.matcher(exp.get(i)).matches() && (pop.matcher(exp.get(i+1)).matches() 
        				|| exp.get(i+1).equals(")")))
        		|| (exp.get(i).equals("(") && pop.matcher(exp.get(i)).matches())
        		|| pop.matcher(exp.get(0)).matches()) {
        		System.out.println("invalid expression");
        		System.exit(1);
        	}
        }
        return exp;
	}
	
	public static List<String> all_sym(String str) {
		Matcher m = palph.matcher(str);
        List<String> sym = new ArrayList<String>();
        while(m.find()) {
        	sym.add(m.group());
        }
        return sym;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String str = "3+2*x+x*y*3*x*z";
		
		System.out.println("Enter 'q' to quit.");
		Scanner in = new Scanner(System.in);
		Node tree = null;
        Map<String, String> symbol_table = new HashMap<String, String>();
        List<String> all_sym = new ArrayList<String>();
		while (true) {
			str = in.nextLine();
			if (str.equals("q")) {
				break;
			} else if (str.startsWith("!simplify")) {
				String[] str2 = str.split(" ");
				for (int i = 1; i < str2.length; i += 1) {
					String[] sim =  str2[i].split("=");
					symbol_table.put(sim[0], sim[1]);
				}
		        String r1 = tree.calculate();
		        Node sim_tree = new Node(generate_items(r1));
		        sim_tree.expression();
		        String r2 = sim_tree.calculate(); 
		        System.out.println(r2);
		        symbol_table.clear();
			} else if (str.startsWith("!d/d")) {
				String dsym = str.substring(4, str.length());
				if (!all_sym.contains(dsym)) {
					System.out.println("Error, no variable");
				} else {
					Node.set_dsym(dsym);
					String r1 = tree.derivative();
				    Node sim_tree = new Node(generate_items(r1));
				    sim_tree.expression();
				    String r2 = sim_tree.calculate(); 
				    System.out.println(r2);
				}
			} else {
				all_sym = all_sym(str);
				tree = new Node(generate_items(refactor(str)));
				Node.set_table(symbol_table);
				tree.expression();
			}
		}   
	}
}
