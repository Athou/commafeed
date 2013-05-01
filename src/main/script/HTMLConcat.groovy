import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

public class HTMLConcat {
	public void concat(String source, String prefix, String destination)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("<div>");
		for (File file : new File(source).listFiles()) {
			if (file.isFile()) {
				sb.append(SystemUtils.LINE_SEPARATOR);
				sb.append(String.format(
						"<script type=\"text/ng-template\" id=\"%s%s\">",
						prefix, file.getName()));
				sb.append(SystemUtils.LINE_SEPARATOR);
				sb.append(FileUtils.readFileToString(file));
				sb.append(SystemUtils.LINE_SEPARATOR);
				sb.append("</script>");
				sb.append(SystemUtils.LINE_SEPARATOR);
			}
		}
		sb.append("</div>");
		FileUtils.writeStringToFile(new File(destination), sb.toString());
	}
}
