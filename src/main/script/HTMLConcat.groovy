import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLConcat {
	
	private static final Pattern PATTERN = Pattern.compile('\\$\\{(.+?)\\}');
	
	public void concat(String source, String prefix, String destination, String i18nPath)
			throws IOException {
		for(File i18n : new File(i18nPath).listFiles()) {
			StringBuilder sb = new StringBuilder();
			sb.append("<div>");
			for (File file : new File(source).listFiles()) {
				sb.append(SystemUtils.LINE_SEPARATOR);
				sb.append(String.format(
						"<script type=\"text/ng-template\" id=\"%s%s\">",
						prefix, file.getName()));
				sb.append(SystemUtils.LINE_SEPARATOR);
				sb.append(translate(FileUtils.readFileToString(file), i18n));
				sb.append(SystemUtils.LINE_SEPARATOR);
				sb.append("</script>");
				sb.append(SystemUtils.LINE_SEPARATOR);
			}
			sb.append("</div>");
			String dest = destination.substring(0, destination.lastIndexOf(".")) + "." + i18n.getName().split("\\.")[0] + ".html";
			FileUtils.writeStringToFile(new File(dest), sb.toString(), "UTF-8");
		}
	}
	
	public String translate(String content, File i18n) {
		 Properties props = new Properties();
		 props.load(new InputStreamReader(new FileInputStream(i18n), "UTF-8"));
		 return replace(content, props);
	}
	
	public String replace(String content, Properties props) {
		Matcher m = PATTERN.matcher(content);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String var = m.group(1);
			Object replacement = props.get(var);
			m.appendReplacement(sb, replacement == null ? var : replacement.toString().split("#")[0]);
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
