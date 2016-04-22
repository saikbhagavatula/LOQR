package kbs1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

/**
 * 
 * Team Members: Sai Kiran Bhagavatula Shubhanshu Sharma
 *
 */
public class LOQRAlgorithm {

	public void createtable(String main, String[] others, String param,
			String mathopr) throws Exception {
		int c;
		File datasetFile = new File(KBSAlgorithmConstants.PATH + "DataSet.csv");
		DataContext context = DataContextFactory
				.createCsvDataContext(datasetFile);
		Schema schema = context.getDefaultSchema();
		Table[] schematables = schema.getTables();

		assert schematables.length == 1;

		Table tab = schematables[0];

		ArrayList<Column> tableColumns = new ArrayList<Column>();
		StringBuilder strBuilder = new StringBuilder();
		for (int j = 0; j < others.length; j++) {
			tableColumns.add(tab.getColumnByName(others[j]));
		}

		for (c = 0; c < tableColumns.size(); c++) {
			if (c == tableColumns.size() - 1) {
				strBuilder.append(tableColumns.get(c).getName());
			} else {
				strBuilder.append(tableColumns.get(c).getName());
				strBuilder.append(",");
			}
		}

		Query q = context.query().from(tab).select(others).toQuery();

		DataSet set = context.executeQuery(q);
		StringBuffer strBuffer = new StringBuffer();
		final String split = System.getProperty("line.separator");
		strBuffer.append(strBuilder);
		strBuffer.append(split);
		while (set.next()) {
			double paramX = Double.parseDouble((String) set.getRow().getValue(
					tab.getColumnByName(main)));
			double paramY = Double.parseDouble(param);

			for (int j = 0; j < tableColumns.size(); j++) {
				if (j == tableColumns.size() - 1) {
					if (main.equalsIgnoreCase(tableColumns.get(j).getName()
							.trim())) {
						if (paramX > paramY) {
							strBuffer.append("YES");
						} else {
							strBuffer.append("NO");
						}
					} else {
						strBuffer.append(set.getRow().getValue(
								tableColumns.get(j)));
					}

				} else {
					if (main.equalsIgnoreCase(tableColumns.get(j).getName()
							.trim())) {
						if (paramX < paramY) {
							strBuffer.append("YES");
							strBuffer.append(",");
						} else {
							strBuffer.append("NO");
							strBuffer.append(",");
						}
					} else {
						strBuffer.append(set.getRow().getValue(
								tableColumns.get(j)));
						strBuffer.append(",");
					}
				}
			}
			strBuffer.append(split);
		}
		set.close();

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(main
					+ ".csv"));
			writer.write(strBuffer.toString());
			writer.close();
		} catch (Exception e) {
		}
		String csvfile = "", arfffile = "";
		CSVLoader csvloader = new CSVLoader();
		csvfile = main + ".csv";
		csvloader.setSource(new File(csvfile));
		Instances data = csvloader.getDataSet();
		ArffSaver arffsaver = new ArffSaver();
		arffsaver.setInstances(data);
		arfffile = main + ".arff";
		arffsaver.setFile(new File(arfffile));
		arffsaver.writeBatch();
	}

	public void j48(String[] file_pointer, String[] par, String[] mathoper,
			String query) throws Exception {
		Vector<String> vector = null;
		Vector<String> outVector = new Vector<String>();
		String temp = null;
		String[] temparr;
		for (int i = 0; i < file_pointer.length; i++) {
			BufferedReader reader = new BufferedReader(new FileReader(
					file_pointer[i] + ".arff"));
			Instances data = new Instances(reader);
			reader.close();
			J48 tree = new J48();
			data.setClassIndex(i);
			tree.buildClassifier(data);
			PART part = new PART();
			part.buildClassifier(data);
			Vector<?> child = part.m_root.getRules();
			vector = new Vector<String>();
			Enumeration<?> inputEnum = child.elements();
			while (inputEnum.hasMoreElements()) {
				temp = inputEnum.nextElement().toString();
				temparr = temp.split(":");
				if (!((temparr[0].contains("YES")) || (temparr[0]
						.contains("NO"))))
					if (!temparr[0].equalsIgnoreCase("")) {
						String Statement = temparr[0] + " ^ " + file_pointer[i]
								+ " " + mathoper[i] + " " + par[i];
						vector.add(Statement);
					}
			}
			for (int j = 0; j < vector.size(); j++) {
				outVector.add(vector.get(j));
			}
		}
		System.out.println("\nDiscovered Rules : \n");
		String prStr = outVector.toString().replace("AND", "\n");
		System.out.println(prStr);
		String operations[] = { "<", ">=", ">", "!=", "<=" }, s1;
		String[] str = query.split("\\^");
		String query_string[] = new String[str.length], op[] = new String[str.length], para[] = new String[str.length];
		for (int i = 0; i < str.length; i++) {
			for (int j = 0; j < operations.length; j++) {
				if (str[i].contains(operations[j])) {
					String[] test = str[i].split(operations[j]);
					query_string[i] = test[0].trim();
					op[i] = operations[j].trim();
					para[i] = test[1].trim();
				}
			}
		}
		double[] distArr = new double[outVector.size()];
		String temps;
		double tempVal;
		String nextLine;
		double dist;
		int minIndex = 0;
		int index = -1;
		int i = 0;

		HashMap<Integer, String> columns = new HashMap<Integer, String>();
		BufferedReader colbr = new BufferedReader(new FileReader(
				KBSAlgorithmConstants.PATH + "Attributes1"));
		while ((nextLine = colbr.readLine()) != null) {
			columns.put(i, nextLine);
			i++;
		}

		i = 0;
		HashMap<String, Double> highervalue = new HashMap<String, Double>();
		BufferedReader maxbr = new BufferedReader(new FileReader(
				KBSAlgorithmConstants.PATH + "Attributes2"));
		while ((nextLine = maxbr.readLine()) != null) {
			highervalue.put(columns.get(i), Double.parseDouble(nextLine));
			i++;
		}

		i = 0;
		HashMap<String, Double> lowervalue = new HashMap<String, Double>();
		BufferedReader minbr = new BufferedReader(new FileReader(
				KBSAlgorithmConstants.PATH + "Attributes3"));
		while ((nextLine = minbr.readLine()) != null) {
			lowervalue.put(columns.get(i), Double.parseDouble(nextLine));
			i++;
		}

		i = 0;
		Enumeration<String> enu = outVector.elements();
		while (enu.hasMoreElements()) {
			index++;
			s1 = enu.nextElement().toString().replace("AND", "^");
			String[] splits = s1.split("\\^");
			String a1[] = new String[splits.length], a2[] = new String[splits.length], a3[] = new String[splits.length];
			for (i = 0; i < splits.length; i++) {
				for (int j = 0; j < operations.length; j++) {
					if (splits[i].contains(operations[j])) {
						String[] example = splits[i].split(operations[j]);
						a1[i] = example[0];
						a2[i] = operations[j];
						a3[i] = example[1];
					}
				}
			}
			for (i = 0; i < query_string.length; i++) {
				temps = query_string[i];
				for (int j = 0; j < a1.length; j++) {
					if (a1[j].trim().equalsIgnoreCase(temps)) {
						tempVal = Double.parseDouble(a3[j]);
						dist = (Math.abs(Double.parseDouble(para[i]) - tempVal))
								/ (highervalue.get(temps) - lowervalue
										.get(temps));
						distArr[index] = distArr[index] + dist;
					}
				}
			}
		}
		colbr.close();
		maxbr.close();
		minbr.close();
		String h = query;
		String g = outVector.get(minIndex).toString().replace("AND", " ^ ");
		String tempp = "";
		String z = g.replace(" <= ", " < ").replace(" >= ", " > ");
		String[] z1 = h.split("\\^");
		String[] z2 = z.split("\\^");
		String z3[] = { "<", ">=", ">", "!=", "<=" };
		String z4[] = new String[z1.length], arrx[] = new String[z1.length], arry[] = new String[z1.length];

		for (i = 0; i < z1.length; i++) {
			for (int j = 0; j < z3.length; j++) {
				if (z1[i].contains(z3[j])) {
					String[] exam = z1[i].split(z3[j]);
					z4[i] = exam[0];
					arrx[i] = z3[j];
					arry[i] = exam[1];
				}
			}
		}

		String newstr[] = new String[z2.length], newstr2[] = new String[z2.length], newstr3[] = new String[z2.length];
		for (i = 0; i < z2.length; i++) {
			for (int j = 0; j < z3.length; j++) {
				if (z2[i].contains(z3[j])) {
					String[] test = z2[i].split(z3[j]);
					newstr[i] = test[0];
					newstr2[i] = z3[j];
					newstr3[i] = test[1];
				}
			}
		}

		for (i = 0; i < z4.length; i++) {
			for (int j = 0; j < newstr.length; j++) {
				if (z4[i].trim().equals(newstr[j].trim())) {
					if (arrx[i].matches("<") || arrx[i].matches(">")) {
						if (newstr2[j].matches("<") || newstr2[j].matches(">")) {
							if (newstr2[j].equalsIgnoreCase(arrx[i])) {
								newstr3[j] = high(arry[i], newstr3[j]);
							} else if ((arrx[i].matches("<") || arrx[i]
									.matches(">"))
									&& (newstr2[j].matches(">") || newstr2[j]
											.matches("<"))) {
								String s4 = newstr[j] + newstr2[j] + newstr3[j]
										+ " ^ " + z4[i] + arrx[i] + arry[i];
								if (method(s4)) {
									newstr3[j] = high(arry[i], newstr3[j]);
									newstr2[j] = "<";
									tempp = " ^ " + newstr[j] + ">"
											+ low(arry[i], newstr3[j]);
								} else {
									newstr[j] = "";
									newstr2[j] = "";
									newstr3[j] = "";
								}
							}
						}
					}
				}
			}
		}
		String s3 = "";
		for (i = 0; i < newstr.length; i++) {
			if (i == newstr.length - 1) {
				s3 = s3 + newstr[i] + newstr2[i] + newstr3[i];
			} else {
				if (!newstr[i].equalsIgnoreCase("")) {
					s3 = s3 + newstr[i] + newstr2[i] + newstr3[i] + " ^";
				}
			}

		}
		s3 = s3 + tempp;

		String print = s3.replace("^ ^", "^");
		print = print.replace("\n", " ");
		System.out.println("\nRelaxed Query:\n" + print);
	}

	private static String low(String q, String w) {
		if (Double.parseDouble(q) < Double.parseDouble(w))
			return q;
		else
			return w;
	}

	private static String high(String q, String w) {
		if (Double.parseDouble(q) > Double.parseDouble(w))
			return q;
		else
			return w;
	}

	public static boolean method(String s3) {
		String[] ps = s3.split("\\^");
		File file = new File(KBSAlgorithmConstants.PATH + "DataSet.csv");
		DataContext dataContext = DataContextFactory.createCsvDataContext(file);
		Schema excelSchema = dataContext.getDefaultSchema();
		Table[] tabs = excelSchema.getTables();
		assert tabs.length == 1;
		Table table = tabs[0];
		ArrayList<Column> columnsCount = new ArrayList<Column>();
		String firstcolname = "MIRtoolbox1.3.4";
		columnsCount.add(table.getColumnByName(firstcolname));
		Query query = new Query();
		query.from(table);
		query.select(firstcolname);

		for (int j = 0; j < ps.length; j++) {
			query.where(ps[j]);
		}
		DataSet dt = dataContext.executeQuery(query);
		if (dt.next()) {
			return true;
		} else {
			return false;
		}
	}

	public static void display(String outStr) {
		String[] out = outStr.split("\\^");
		File file = new File("DataSet.csv");
		DataContext context = DataContextFactory.createCsvDataContext(file);
		Schema sch = context.getDefaultSchema();
		Table[] tb = sch.getTables();
		assert tb.length == 1;
		Table table = tb[0];
		ArrayList<Column> cols = new ArrayList<Column>();
		String alltable_columns = "MIRtoolbox1.3.4";
		cols.add(table.getColumnByName(alltable_columns));
		Query query = new Query();
		query.from(table);
		query.select(alltable_columns);
		for (int v = 0; v < out.length; v++) {
			query.where(out[v]);
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Welcome to the LOQR Implementation");
		System.out.println("Format :     a > num1 ^ b < num2  ^  .. \n");
		String oper[] = { "<", ">" };
		StringTokenizer tokenizer;
		// Taking input from the user
		Scanner in = new Scanner(System.in);
		System.out.println("Please Enter Query :     ");
		String userQuery = in.nextLine();
		tokenizer = new StringTokenizer(userQuery, "^");
		int atoms = 0;
		String input[] = new String[tokenizer.countTokens()];
		String query[] = new String[input.length], optor[] = new String[input.length], arg[] = new String[input.length];
		for (int l = 0; tokenizer.hasMoreTokens(); l++, atoms++)
			input[l] = tokenizer.nextToken();
		// Condition to check whether the input query contains 4 or more atomic
		// terms
		if (atoms != 0 && atoms < 4) {
			System.out.println("Enter atleast 4 terms. Program will exit now.");
			System.exit(0);
		}
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < oper.length; j++) {
				if (input[i].contains(oper[j])) {
					String[] tmp = input[i].split(oper[j]);
					query[i] = tmp[0].trim();
					optor[i] = oper[j].trim();
					arg[i] = tmp[1].trim();
				}
			}
		}
		LOQRAlgorithm loqr = new LOQRAlgorithm();
		for (int i = 0; i < query.length; i++) {
			loqr.createtable(query[i], query, arg[i], optor[i]);
		}
		loqr.j48(query, arg, optor, userQuery);
	}
}
