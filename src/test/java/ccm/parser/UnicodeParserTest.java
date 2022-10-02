package ccm.parser;

import java.util.List;

import org.junit.Test;

public class UnicodeParserTest {

	@Test
	public void parseTest() {
		UnicodeParser parser = new UnicodeParser();
		String javaFile = "D:/erp/mp/src/com/icsc/mp/util/mpjcUtilUnicode.java";
		List<UnicodeParser.Model> models = parser.parse(javaFile);
		System.out.println("models=" + models);

	}

}
