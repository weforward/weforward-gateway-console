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

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import cn.weforward.common.util.ListUtil;
import cn.weforward.common.util.StringUtil;
import cn.weforward.gateway.console.util.FitTable;

/**
 * 'help'命令
 * 
 * @author zhangpengji
 *
 */
public class HelpCommand extends Command {

	List<Option> m_Options;

	public HelpCommand(Console main) {
		super(main);
		Option commandOp = new Option().setValues("command").setValuesRequire(true).setDesc("命令的名称");
		m_Options = Collections.singletonList(commandOp);
	}

	@Override
	String getName() {
		return "help";
	}

	@Override
	String getSynopsis() {
		return "显示命令的帮助信息";
	}

	@Override
	List<String> getUsages() {
		return Collections.singletonList("help command");
	}

	@Override
	List<Option> getOptions() {
		return m_Options;
	}

	// @Override
	// List<String> getExamples() {
	// return Collections.emptyList();
	// }

	@Override
	void executeInner(List<Argument> args) {
		if (!ListUtil.isEmpty(args)) {
			String cmdName = args.get(0).getValue(0);
			Command cmd = m_Console.find(cmdName);
			if (null == cmd) {
				getPrintStream().println("找不到命令：" + cmdName);
			} else {
				cmd.showHelpInfo();
			}
		} else {
			showAllCommand();
		}
	}

	void showAllCommand() {
		PrintStream ps = getPrintStream();
		ps.println("help command  查看命令详情");
		ps.println();
		ps.println("全部命令：");
		FitTable table = new FitTable();
		for (Command cmd : m_Console.getCommands()) {
			StringBuilder name = new StringBuilder();
			name.append("  ").append(cmd.getName());
			String shortName = cmd.getShortName();
			if (!StringUtil.isEmpty(shortName)) {
				name.append("/").append(shortName);
			}
			table.addCell(name.toString());
			table.addCell("    " + cmd.getSynopsis());
			table.nextRow();
		}
		table.print(getPrintStream());
	}
}
