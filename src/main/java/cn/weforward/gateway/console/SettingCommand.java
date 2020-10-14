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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.weforward.common.util.StringUtil;
import cn.weforward.gateway.console.exception.CommonException;

/**
 * 'setting'命令
 * 
 * @author zhangpengji
 *
 */
public class SettingCommand extends Command {

	Option m_LoadOption;
	Option m_SaveOption;
	Option m_PrintOption;
	Option m_UrlOption;
	Option m_SecretOption;
	Option m_IdOption;
	Option m_KeyOption;
	List<Option> m_Options;

	public SettingCommand(Console main) {
		super(main);

		m_Options = new ArrayList<Option>();

		m_LoadOption = new Option("load").setValues("path").setValuesRequire(false)
				.setDesc("从properties文件加载参数。不指定path时，加载jar包所在目录");
		m_Options.add(m_LoadOption);

		m_SaveOption = new Option("save").setValues("path").setValuesRequire(false)
				.setDesc("保存参数到properties文件。不指定path时，保存到jar包所在目录");
		m_Options.add(m_SaveOption);

		m_PrintOption = new Option("print").setDesc("显示当前配置");
		m_Options.add(m_PrintOption);

		m_UrlOption = new Option("url").setValues("url").setDesc("设置keeper接口链接，默认为：" + Console.KEEPER_API_PRE_URL);
		m_Options.add(m_UrlOption);

		m_SecretOption = new Option("secret").setValues("secret")
				.setDesc("设置调用keeper的secret（网关gateway.properties所配置的）。当设置时，将覆盖access-id,access-key");
		m_Options.add(m_SecretOption);

		m_IdOption = new Option("id").setValues("access-id").setDesc("设置调用keeper的（Access Id）");
		m_Options.add(m_IdOption);

		m_KeyOption = new Option("key").setValues("access-key").setDesc("设置调用keeper的（Access Key）");
		m_Options.add(m_KeyOption);
	}

	@Override
	String getName() {
		return "setting";
	}

	@Override
	String getSynopsis() {
		return "设置调用keeper api的参数";
	}

	@Override
	List<Option> getOptions() {
		return m_Options;
	}

	@Override
	List<String> getUsages() {
		String secret = "setting -url url -secret secret";
		String accessId = "setting -url url -id access-id -key access-key";
		String load = "setting -load path";
		String save = "setting -save path";
		String show = "setting -print";
		return Arrays.asList(secret, accessId, load, save, show);
	}

	@Override
	void executeInner(List<Argument> args) {
		String loadPath = Argument.getValue(args, m_LoadOption);
		String savePath = Argument.getValue(args, m_SaveOption);
		String url = Argument.getValue(args, m_UrlOption);
		String secret = Argument.getValue(args, m_SecretOption);
		String accessId = Argument.getValue(args, m_IdOption);
		String accessKey = Argument.getValue(args, m_KeyOption);

		if (Argument.contain(args, m_LoadOption)) {
			loadProperties(loadPath);
		} else if (Argument.contain(args, m_SaveOption)) {
			saveProperties(savePath);
		} else {
			if (!StringUtil.isEmpty(url)) {
				setUrl(url);
			}
			if (!StringUtil.isEmpty(accessId)) {
				setAccessId(accessId);
			}
			if (!StringUtil.isEmpty(accessKey)) {
				setAccessKey(accessKey);
			}
			if (!StringUtil.isEmpty(secret)) {
				setSecret(secret);
			}
		}
		if (Argument.contain(args, m_PrintOption)) {
			showSetting();
		}
	}

	void loadProperties(String path) {
		try {
			File file = m_Console.loadProperties(path);
			getPrintStream().println("加载配置成功：" + file);
		} catch (IOException e) {
			throw new CommonException("加载配置文件出错，" + e.toString());
		}
	}

	void saveProperties(String path) {
		try {
			File file = m_Console.saveProperties(path);
			getPrintStream().println("保存配置成功：" + file);
		} catch (IOException e) {
			throw new CommonException("保存配置文件出错，" + e.toString());
		}
	}

	void showSetting() {
		getPrintStream().println("url=" + StringUtil.toString(m_Console.getUrl()));
		getPrintStream().println("secret=" + StringUtil.toString(m_Console.getSecret()));
		getPrintStream().println("access-id=" + StringUtil.toString(m_Console.getAccessId()));
		getPrintStream().println("access-key=" + StringUtil.toString(m_Console.getAccessKey()));
	}

	void setUrl(String url) {
		m_Console.setApiPreUrl(url);
		// getPrintStream().println("设置url：" + m_Console.getUrl());
	}

	void setAccessId(String id) {
		m_Console.setAccessId(id);
		// getPrintStream().println("设置access_id:" + m_Console.getAccessId());
	}

	void setAccessKey(String key) {
		m_Console.setAccessKey(key);
		// getPrintStream().println("设置access_key:" + m_Console.getAccessKey());
	}

	void setSecret(String secret) {
		m_Console.setSecret(secret);
	}
}
