import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class I18nGenerator {

	public void generate(String directory) throws Exception {

		File dir = new File(directory);
		List<String> enLines = FileUtils.readLines(new File(dir,
				"en.properties"), "UTF-8");

		for (File file : dir.listFiles()) {
			if ("languages.properties".equals(file.getName())
					|| "en.properties".equals(file.getName()))
				continue;

			List<String> newLines = new ArrayList<String>();
			List<String> langLines = FileUtils.readLines(file, "UTF-8");

			int j = 0;
			for (int i = 0; i < enLines.size(); i++) {

				String enLine = enLines.get(i);
				String langLine = langLines.get(j);

				if (StringUtils.isNotBlank(enLine)) {
					
					String key = enLine.split("=")[0];
					if (langLine.startsWith(key)) {
						newLines.add(langLine);
					} else {
						newLines.add(enLine + " ####### Needs translation");
						j--;
					}
				} else {
					newLines.add(null);
					if (StringUtils.isNotBlank(langLine)) {
						j--;
					}
				}
				j++;
			}
			newLines.add(null);
			FileUtils.writeLines(file, "UTF-8", newLines);

		}
	}
}
