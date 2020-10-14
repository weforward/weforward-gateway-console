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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import cn.weforward.common.io.BytesOutputStream;
import cn.weforward.common.json.JsonOutputStream;
import cn.weforward.common.util.Bytes;
import cn.weforward.common.util.StringUtil;
import cn.weforward.protocol.datatype.DtObject;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.serial.JsonSerialEngine;

public class ServiceDebugCommand extends Command {

	Option m_Name;
	Option m_No;
	Option m_ScriptSrc;
	Option m_ScriptCharset;
	Option m_ScriptName;
	Option m_ScriptArgs;
	List<Option> m_Options;

	protected ServiceDebugCommand(Console console) {
		super(console);

		m_Name = new Option("name").setValues("service-name").setDesc("微服务名");
		m_No = new Option("no").setValues("service-no").setDesc("微服务编号");
		m_ScriptSrc = new Option("src").setValues("script-srouce").setDesc("脚本源码文件路径");
		m_ScriptCharset = new Option("charset").setValues("script-srouce-charset").setDesc("脚本源码文件编码，默认：UTF-8");
		m_ScriptName = new Option("class").setValues("script-class-name").setDesc("脚本源码类名");
		m_ScriptArgs = new Option("args").setValues("script-args").setDesc("脚本源码参数");

		m_Options = Arrays.asList(m_Name, m_No, m_ScriptSrc, m_ScriptName, m_ScriptArgs);
	}

	@Override
	String getName() {
		return "servicedebug";
	}

	@Override
	String getSynopsis() {
		return "微服务调试（执行脚本）";
	}

	@Override
	List<String> getUsages() {
		String up = "servicedebug -name service-name -no service-no -src './HelloWorld.java'";
		String exe = "servicedebug -name service-name -no service-no -class HelloWorld -args param1=value2&param2=value2";
		return Arrays.asList(up, exe);
	}

	@Override
	List<Option> getOptions() {
		return m_Options;
	}

	@Override
	void executeInner(List<Argument> args) throws IOException {
		String name = Argument.getValue(args, m_Name);
		String no = Argument.getValue(args, m_No);
		String src = Argument.getValue(args, m_ScriptSrc);
		String charset = Argument.getValue(args, m_ScriptCharset);
		String scriptName = Argument.getValue(args, m_ScriptName);
		String scriptArgs = Argument.getValue(args, m_ScriptArgs);

		debugService(name, no, src, charset, scriptName, scriptArgs);
	}

	void debugService(String serviceName, String serviceNo, String scriptSourcePath, String scriptSourceCharset,
			String scriptName, String scriptArgs) throws IOException {
		Keeper keeper = getKeeper();
		String scriptSource = null;
		if (!StringUtil.isEmpty(scriptSourcePath)) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(scriptSourcePath);
				BytesOutputStream out = new BytesOutputStream(in);
				Bytes bytes = out.getBytes();
				out.close();
				scriptSourceCharset = StringUtil.isEmpty(scriptSourceCharset) ? "UTF-8" : scriptSourceCharset;
				scriptSource = new String(bytes.getBytes(), bytes.getOffset(), bytes.getSize(), scriptSourceCharset);
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch (Exception e) {
					}
				}
			}
		}
		DtObject result = keeper.debugService(serviceName, serviceNo, scriptSource, scriptName, scriptArgs);
		PrintStream ps = getPrintStream();
		ps.println("执行结果:");
		JsonSerialEngine.formatObject(result, new JsonOutputStream(ps));
	}
}
