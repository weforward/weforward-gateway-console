/**
 * Copyright (c) 2019,2020 honintech
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package cn.weforward.gateway.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import cn.weforward.common.util.StringUtil;
import cn.weforward.gateway.console.exception.CommonException;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.gateway.http.HttpKeeper;

/**
 * 网关控制台主程序
 * 
 * @author zhangpengji
 *
 */
public class Console {

	static final String KEEPER_API_PRE_URL = "http://127.0.0.1:5661/";

	static final String CONFIG_FILE_NAME = "console.properties";
	static final String CONFIG_API_PRE_URL = "url";
	static final String CONFIG_ACCESS_ID = "id";
	static final String CONFIG_ACCESS_KEY = "key";
	static final String CONFIG_SECRET = "secret";

	String m_HomeDir;

	HelpCommand m_Help;
	QuitCommand m_Quit;
	List<Command> m_Commands;

	String m_ApiPreUrl = KEEPER_API_PRE_URL;
	String m_AccessId;
	String m_AccessKey;
	String m_Secret;
	Keeper m_Keeper;

	Console() throws IOException {
		m_Help = new HelpCommand(this);
		m_Quit = new QuitCommand(this);

		ArrayList<Command> cmds = new ArrayList<Command>();
		InitCommand init = new InitCommand(this);
		cmds.add(init);
		SettingCommand setting = new SettingCommand(this);
		cmds.add(setting);
		AccessCommand access = new AccessCommand(this);
		cmds.add(access);
		ServiceCommand service = new ServiceCommand(this);
		cmds.add(service);
		RightCommand right = new RightCommand(this);
		cmds.add(right);
		TrafficCommand traffic = new TrafficCommand(this);
		cmds.add(traffic);
		ServiceDebugCommand serviceDebug = new ServiceDebugCommand(this);
		cmds.add(serviceDebug);
		cmds.add(m_Help);
		cmds.add(m_Quit);

		// Collections.sort(cmds, Command.CMP);
		m_Commands = cmds;

		m_HomeDir = findHomeDir();
	}

	private String findHomeDir() throws IOException {
		Class<?> sourceClass = getClass();
		ProtectionDomain domain = (sourceClass != null) ? sourceClass.getProtectionDomain() : null;
		CodeSource codeSource = (domain != null) ? domain.getCodeSource() : null;
		URL location = (codeSource != null) ? codeSource.getLocation() : null;
		File source = (location != null) ? findSource(location) : null;
		if (source != null && source.exists()) {
			return source.getParent();
		}
		return null;
	}

	private File findSource(URL location) throws IOException {
		URLConnection connection = location.openConnection();
		if (connection instanceof JarURLConnection) {
			String name = ((JarURLConnection) connection).getJarFile().getName();
			int separator = name.indexOf("!/");
			if (separator > 0) {
				name = name.substring(0, separator);
			}
			return new File(name);
		}
		return new File(location.getPath());
	}

	void init() throws IOException {
		try {
			File f = loadProperties(null);
			getPrintStream().println("已加载配置：" + f);
			getPrintStream().println();
		} catch (IOException e) {
			// 忽略
		}
	}

	File loadProperties(String filePath) throws FileNotFoundException, IOException {
		File file = genPropertiesFile(filePath);
		Properties prop = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			prop.load(in);
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		String url = prop.getProperty(CONFIG_API_PRE_URL);
		if (!StringUtil.isEmpty(url)) {
			setApiPreUrl(url);
		}
		String accessId = prop.getProperty(CONFIG_ACCESS_ID);
		if (!StringUtil.isEmpty(accessId)) {
			setAccessId(accessId);
		}
		String accessKey = prop.getProperty(CONFIG_ACCESS_KEY);
		if (!StringUtil.isEmpty(accessKey)) {
			setAccessKey(accessKey);
		}
		String secret = prop.getProperty(CONFIG_SECRET);
		if (!StringUtil.isEmpty(secret)) {
			setSecret(secret);
		}
		return file;
	}

	File saveProperties(String filePath) throws FileNotFoundException, IOException {
		File file = genPropertiesFile(filePath);
		Properties prop = new Properties();
		if (!StringUtil.isEmpty(m_ApiPreUrl)) {
			prop.setProperty(CONFIG_API_PRE_URL, m_ApiPreUrl);
		}
		if (!StringUtil.isEmpty(m_AccessId)) {
			prop.setProperty(CONFIG_ACCESS_ID, m_AccessId);
		}
		if (!StringUtil.isEmpty(m_AccessKey)) {
			prop.setProperty(CONFIG_ACCESS_KEY, m_AccessKey);
		}
		if (!StringUtil.isEmpty(m_Secret)) {
			prop.setProperty(CONFIG_SECRET, m_Secret);
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			prop.store(out, null);
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
		return file;
	}

	File genPropertiesFile(String filePath) {
		File file;
		if (StringUtil.isEmpty(filePath)) {
			file = new File(m_HomeDir + File.separator + CONFIG_FILE_NAME);
		} else {
			file = new File(filePath);
			if (file.isDirectory()) {
				file = new File(file.getAbsolutePath() + File.separator + CONFIG_FILE_NAME);
			}
		}
		return file;
	}

	void start() throws IOException {
		getPrintStream().println("'" + m_Help.getName() + "'查看支持的命令");
		getPrintStream().println("'" + m_Quit.getName() + "'退出");
		getPrintStream().println();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		StringBuilder argBuffer = new StringBuilder();
		while (true) {
			String line = reader.readLine();
			if (StringUtil.isEmpty(line)) {
				continue;
			}
			line = line.trim();
			if (argBuffer.length() > 0) {
				argBuffer.append(line);
				line = argBuffer.toString();
				argBuffer.setLength(0);
			}

			if ('\\' == line.charAt(line.length() - 1)) {
				line = line.substring(0, line.length() - 1);
				argBuffer.append(line);
				getPrintStream().print('>');
				continue;
			}
			List<String> args;
			try {
				args = splitArgument(line);
			} catch (ArgumentNotEndException e) {
				argBuffer.append(line);
				getPrintStream().print('>');
				continue;
			}

			execute(args);

			getPrintStream().println();
		}
	}

	void execute(List<String> args) {
		Command cmd = find(args.get(0));
		if (null == cmd) {
			getPrintStream().println("无效命令：" + args.get(0));
		} else {
			cmd.execute(args.subList(1, args.size()));
		}
	}

	void stop() {
		System.exit(0);
	}

	private List<String> splitArgument(String argString) {
		if ('\\' == argString.charAt(argString.length() - 1)) {
			throw NOT_END;
		}
		List<String> result = new ArrayList<String>();
		char quote = 0;
		StringBuilder arg = new StringBuilder();
		for (int i = 0; i < argString.length(); i++) {
			char ch = argString.charAt(i);
			if ('"' == ch || '\"' == ch) {
				if (0 == quote) {
					// 引号开始
					quote = ch;
					continue;
				} else if (quote == ch) {
					// 引号配对
					quote = 0;
					continue;
				}
			}
			if (0 != quote) {
				// 在引号中
				arg.append(ch);
				continue;
			}
			if (' ' != ch) {
				// 过滤特殊字符
				if ('\\' != ch) {
					arg.append(ch);
				}
				continue;
			}
			// 发现空格
			if (0 != arg.length()) {
				result.add(arg.toString());
				arg.setLength(0);
			}
		}
		if (0 != quote) {
			// 引号未结束
			throw NOT_END;
		}
		if (0 != arg.length()) {
			result.add(arg.toString());
		}
		return result;
	}

	@SuppressWarnings("serial")
	private static class ArgumentNotEndException extends RuntimeException {

		ArgumentNotEndException() {
			super(null, null, false, false);
		}
	}

	private static final ArgumentNotEndException NOT_END = new ArgumentNotEndException();

	List<Command> getCommands() {
		return m_Commands;
	}

	Command find(String name) {
		for (Command cmd : m_Commands) {
			if (name.equals(cmd.getName()) || name.equals(cmd.getShortName())) {
				return cmd;
			}
		}
		return null;
	}

	public String getUrl() {
		return m_ApiPreUrl;
	}

	public void setApiPreUrl(String url) {
		m_ApiPreUrl = url;
		m_Keeper = null;
	}

	public String getAccessId() {
		return m_AccessId;
	}

	public void setAccessId(String accessId) {
		m_AccessId = accessId;
		m_Secret = null;
		m_Keeper = null;
	}

	public String getAccessKey() {
		return m_AccessKey;
	}

	public void setAccessKey(String accessKey) {
		m_AccessKey = accessKey;
		m_Secret = null;
		m_Keeper = null;
	}

	public String getSecret() {
		return m_Secret;
	}

	public void setSecret(String secret) {
		m_Secret = secret;
		m_AccessId = null;
		m_AccessKey = null;
		m_Keeper = null;
	}

	public void setCommands(List<Command> commands) {
		m_Commands = commands;
	}

	public PrintStream getPrintStream() {
		return System.out;
	}

	public PrintStream getErrorStream() {
		return System.err;
	}

	public Keeper getKeeper() {
		Keeper keeper = m_Keeper;
		if (null != keeper) {
			return keeper;
		}
		synchronized (this) {
			keeper = m_Keeper;
			if (null != keeper) {
				return keeper;
			}
			if (StringUtil.isEmpty(m_ApiPreUrl)) {
				throw new CommonException("请使用setting命令设置url参数");
			}
			if (!StringUtil.isEmpty(m_Secret)) {
				try {
					m_Keeper = new HttpKeeper(m_ApiPreUrl, m_Secret);
				} catch (NoSuchAlgorithmException e) {
					throw new CommonException("初始keeper错误", e);
				}
			} else if (!StringUtil.isEmpty(m_AccessId) && !StringUtil.isEmpty(m_AccessKey)) {
				m_Keeper = new HttpKeeper(m_ApiPreUrl, m_AccessId, m_AccessKey);
			} else {
				throw new CommonException("请使用setting命令设置secret或access-id、access-key参数");
			}
			keeper = m_Keeper;
		}
		return keeper;
	}

	public static void main(String[] args) throws IOException {
		Console console = new Console();
		console.init();

		if (null != args && args.length > 0) {
			console.execute(Arrays.asList(args));
		} else {
			console.start();
		}
	}
}
