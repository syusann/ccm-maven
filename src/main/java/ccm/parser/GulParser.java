package ccm.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <pre>
 * 由 gul取出 form及 field存到map,並print方便處理 model
 * </pre>
 * 
 * @author I14348
 *
 */
public class GulParser {
	Log log = LogFactory.getLog(GulParser.class);

	public Map<String, List<Map<String, String>>> getFormMap(String gul) {
		Map<String, List<Map<String, String>>> map = new LinkedHashMap<>();
		String formKey = "";
		try (BufferedReader br = new BufferedReader(new FileReader(gul))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.matches(".*<form .*")) {
					// System.out.println("line=" + line);
					String formId = getId(line);
					formKey = formId;
					map.put(formKey, new ArrayList<Map<String, String>>());
					continue;
				}
				if (line.matches(".*<grid .*")) {
					// System.out.println("line=" + line);
					String gridId = getId(line);
					formKey = gridId;
					map.put(formKey, new ArrayList<Map<String, String>>());
					continue;
				}

				if (line.matches(".*<field .*")) {
					// System.out.println("line=" + line);
					Map<String, String> mapf = getFieldMap(line);
					// System.out.println(mapf);

					List<Map<String, String>> list = map.get(formKey);
					list.add(mapf);
					map.put(formKey, list);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return map;
	}

	private Map<String, String> getFieldMap(String line) {
		Map<String, String> map = new HashMap<>();
		Pattern pattern = Pattern.compile("type=\"[a-zA-Z]+\"|type='[a-zA-Z]+'");
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			map.put("type", StringUtils.substring(matcher.group(0), 6, matcher.group(0).length() - 1));
		}
		pattern = Pattern.compile("name=\"[a-zA-Z0-9_]+\"|name='[a-zA-Z0-9_]+'");
		matcher = pattern.matcher(line);
		if (matcher.find()) {
			map.put("name", StringUtils.substring(matcher.group(0), 6, matcher.group(0).length() - 1));
		}

		// 可以處理中文，有可能還要加入一些符號
		pattern = Pattern
				.compile("label=\"[a-z0-9A-Z:-_/(){} \\u4e00-\\u9fa5]+\"|label='[a-z0-9A-Z:-_/(){} \\u4e00-\\u9fa5]+'");
		matcher = pattern.matcher(line);
		if (matcher.find()) {
			map.put("label", StringUtils.substring(matcher.group(0), 7, matcher.group(0).length() - 1));
		}
		return map;
	}

	private String getId(String line) {
		Pattern pattern = Pattern.compile("id=\"[a-z0-9A-Z_]+\"|id='[a-z0-9A-Z_]+'");
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			return StringUtils.substring(matcher.group(0), 4, matcher.group(0).length() - 1);
		} else {
			System.out.println("??? id not found in line:" + line);
			return null;
		}
	}

	/**
	 * 
	 * @param map
	 * @param warn 是否提示name無_的欄位
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void printForModel(Map<String, List<Map<String, String>>> map, boolean warn) {
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			List<Map<String, String>> val = (List<Map<String, String>>) entry.getValue();

			if (key.matches("f0")) { // skip f0
				continue;
			}
			if (key.startsWith("g")) {
				System.out.println(gridPrint(key));
			} else {
				System.out.println(formPrint(key));
			}

			for (Map<String, String> mapf : val) {
				String fieldPrintStr = fieldPrint(mapf, warn);
				if (StringUtils.isNotBlank(fieldPrintStr)) {
					System.out.println(fieldPrintStr);
				}
			}
		}

	}

	private String formPrint(String formId) {
		// public Frm frm = new Frm();
		String clz = StringUtils.substring(formId, 0, 1).toUpperCase() + StringUtils.substring(formId, 1);
		return (String.format("public %s %s = new %s();", clz, formId, clz));
	}

	private String gridPrint(String gridId) {
		// public List<G1row> g1 = new ArrayList<>();
		String clz = StringUtils.substring(gridId, 0, 1).toUpperCase() + StringUtils.substring(gridId, 1);
		return (String.format("public List<%srow> %s = new ArrayList<>();", clz, gridId));
	}

	private String fieldPrint(Map<String, String> mapf, boolean warn) {
		String result = "";
		if (mapf.get("type") == null)
			return "";
		// type="btn" 和 type="adapt" 没有 name
		if (mapf.get("name") == null)
			return "";
		String type = mapf.get("type");
		String name = getName(mapf.get("name"), warn);
		String label = StringUtils.defaultIfBlank(mapf.get("label"), "");

		// 有用到的再加
		switch (type) {
		case "label":
		case "checkBox":
		case "comboBox":
		case "radioBox":
		case "trigger":
		case "textarea":
		case "date":
		case "txt":
			// public String tblId; // 表格代號
			result = String.format("	public String %s; // %s: %s", name, type, label);
			break;
		case "num":
			// public BigDecimal poAmt; // 訂購金額
			result = String.format("	public BigDecimal %s; // %s: %s", name, type, label);
			break;
		default:
			System.out.println("??unknown type : " + type);
			break;
		}

		return result;
	}

	/**
	 * v1_tabId => tblId
	 * 
	 * @param name
	 * @return
	 */
	private String getName(String name, boolean warn) {
		if (name.indexOf("_") < 0) {
			if (warn) {
				System.out.println("??? 没有_ ,name=" + name);
			}
			return name;
		}
		return StringUtils.substring(name, name.indexOf("_") + 1);
	}

	public void printForModelToBean(Map<String, List<Map<String, String>>> map) {
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			List<Map<String, String>> val = (List<Map<String, String>>) entry.getValue();

			if (key.matches("f0")) { // skip f0
				continue;
			}
			if (key.startsWith("g")) {
				System.out.println(gridPrint(key));
			} else {
				System.out.println(formPrint(key));
			}

			for (Map<String, String> mapf : val) {
				String fieldPrintStr = modelToBeanPrint(mapf, key);
				if (StringUtils.isNotBlank(fieldPrintStr)) {
					System.out.println(fieldPrintStr);
				}
			}
		}

	}

	private String modelToBeanPrint(Map<String, String> mapf, String key) {
		String result = "";
		if (mapf.get("type") == null)
			return "";
		if (mapf.get("name") == null)
			return "";
		String type = mapf.get("type");
		String name = getName(mapf.get("name"), false);
		String label = StringUtils.defaultIfBlank(mapf.get("label"), "");

		switch (type) {
		case "label":
		case "checkBox":
		case "comboBox":
		case "trigger":
		case "textarea":
		case "date":
		case "txt":
		case "num":
			String getter = StringUtils.substring(name, 0, 1).toUpperCase() + StringUtils.substring(name, 1);
			if (key.startsWith("g")) {
				// g1row.mtrl = bean.getMtrl();
				result = String.format("	%srow.%s = bean.get%s(); // %s", key, name, getter, label);
			} else {
				// model.v1.poAmt = bean.getPoAmt();
				result = String.format("	model.%s.%s = bean.get%s(); // %s", key, name, getter, label);
			}
			break;
		default:
			break;
		}

		return result;
	}

	public void printForBeanToModel(Map<String, List<Map<String, String>>> map) {
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			List<Map<String, String>> val = (List<Map<String, String>>) entry.getValue();

			if (key.matches("f0")) { // skip f0
				continue;
			}
			if (key.startsWith("g")) {
				System.out.println(gridPrint(key));
			} else {
				System.out.println(formPrint(key));
			}

			for (Map<String, String> mapf : val) {
				String fieldPrintStr = beanToModelPrint(mapf, key);
				if (StringUtils.isNotBlank(fieldPrintStr)) {
					System.out.println(fieldPrintStr);
				}
			}
		}

	}

	private String beanToModelPrint(Map<String, String> mapf, String key) {
		String result = "";
		if (mapf.get("type") == null)
			return "";
		if (mapf.get("name") == null)
			return "";
		String type = mapf.get("type");
		String name = getName(mapf.get("name"), false);
		String label = StringUtils.defaultIfBlank(mapf.get("label"), "");

		switch (type) {
		case "label":
		case "checkBox":
		case "comboBox":
		case "trigger":
		case "textarea":
		case "date":
		case "txt":
		case "num":
			String getter = StringUtils.substring(name, 0, 1).toUpperCase() + StringUtils.substring(name, 1);
			if (key.startsWith("g")) {
				// bean.setItem(g1row.mtrl);
				result = String.format("	bean.set%s(%srow.%s); // %s", getter, key, name, label);
			} else {
				// popBean.setPoNo(model.v1.poNo);
				result = String.format("	bean.set%s(model.%s.%s); // %s", getter, key, name, label);
			}
			break;
		default:
			break;
		}

		return result;
	}
}
