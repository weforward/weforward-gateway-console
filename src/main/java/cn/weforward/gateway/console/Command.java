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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.weforward.common.util.ListUtil;
import cn.weforward.common.util.StringUtil;
import cn.weforward.gateway.console.exception.ArgumentException;
import cn.weforward.gateway.console.exception.CommonException;
import cn.weforward.gateway.console.util.FitTable;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.gateway.exception.KeeperException;

/**
 * 命令
 * 
 * @author zhangpengji
 *
 */
public abstract class Command {

	static final Comparator<Command> CMP = new Comparator<Command>() {

		@Override
		public int compare(Command o1, Command o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	Console m_Console;

	protected Command(Console console) {
		m_Console = console;
	}

	/**
	 * 名称
	 * 
	 * @return
	 */
	abstract String getName();

	/**
	 * 短名
	 * 
	 * @return
	 */
	String getShortName() {
		return null;
	}

	/**
	 * 摘要
	 * 
	 * @return
	 */
	abstract String getSynopsis();

	/**
	 * 用法
	 * 
	 * @return
	 */
	abstract List<String> getUsages();

	/**
	 * 选项
	 * 
	 * @return
	 */
	abstract List<Option> getOptions();

	// /**
	// * 示例
	// *
	// * @return
	// */
	// abstract List<String> getExamples();

	public PrintStream getPrintStream() {
		return m_Console.getPrintStream();
	}

	public PrintStream getErrorStream() {
		return m_Console.getErrorStream();
	}

	public Keeper getKeeper() {
		return m_Console.getKeeper();
	}

	void showHelpInfo() {
		PrintStream ps = getPrintStream();
		ps.print(getName());
		String shortName = getShortName();
		if (!StringUtil.isEmpty(shortName)) {
			ps.print("/");
			ps.print(shortName);
		}
		ps.println();

		ps.println();
		ps.println(getSynopsis());

		ps.println();
		ps.println("用法：");
		for (String usage : getUsages()) {
			ps.println("  " + usage);
		}

		List<Option> options = getOptions();
		if (!ListUtil.isEmpty(options)) {
			ps.println();
			ps.println("选项：");
			FitTable optionsTable = new FitTable();
			for (Option op : options) {
				StringBuilder sb = new StringBuilder();
				if (op.hasKey()) {
					sb.append("  ");
					sb.append(op.getPrefix()).append(op.getKey());
				}
				if (null != op.getValues() && op.getValues().length > 0) {
					for (String value : op.getValues()) {
						sb.append(" ");
						sb.append(value);
					}
				}
				optionsTable.addCell(sb.toString());
				optionsTable.addCell(op.getDesc());
				optionsTable.nextRow();
			}
			optionsTable.print(ps);
		}
	}

	public void execute(List<String> argList) {
		try {
			execute0(argList);
		} catch (CommonException e) {
			getErrorStream().println(e.getMessage());
			if (e.isPrintDetail()) {
				e.printStackTrace(getErrorStream());
			}
		} catch (KeeperException e) {
			getErrorStream().println("网关执行出错:" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace(getErrorStream());
		}
	}

	/**
	 * 执行
	 * 
	 * @param args
	 * @throws Exception
	 */
	private void execute0(List<String> argList) throws Exception {
		List<Argument> args = Collections.emptyList();
		if (!ListUtil.isEmpty(argList)) {
			List<Option> options = getOptions();
			if (ListUtil.isEmpty(options)) {
				throw ArgumentException.illegalArgument(argList.toString());
			}
			args = new ArrayList<>();
			// 所有选项只匹配一次，匹配成功后，使用null占位
			options = new ArrayList<>(options);
			Argument curr = null;
			for (String str : argList) {
				str = str.trim();
				if (StringUtil.isEmpty(str)) {
					continue;
				}
				if (null != curr) {
					if (!Option.isKey(str)) {
						// 追加参数值
						curr.addValue(str);
						if (curr.enoughValues()) {
							curr = null;
						}
						continue;
					} else {
						// 参数应该没完整
						curr.check();
					}
				}

				// 切换到下一个参数
				curr = null;

				Option op = findAndRemove(str, options);
				if (null == op) {
					throw ArgumentException.illegalOption(str);
				}
				Argument arg = new Argument(op);
				if (!Option.isKey(str)) {
					arg.addValue(str);
				}
				if (!arg.enoughValues()) {
					// 还有参数值
					curr = arg;
				}
				args.add(arg);
			}

			if (null != curr) {
				// 参数应该没完整
				curr.check();
			}
		}
		if (args.size() > 1) {
			Collections.sort(args, Argument.CMP_BY_NAME);
		}
		executeInner(args);
	}

	static Option findAndRemove(String arg, List<Option> options) {
		boolean isKey = Option.isKey(arg);
		int idx = -1;
		for (int i = 0; i < options.size(); i++) {
			Option op = options.get(i);
			if (null == op) {
				continue;
			}
			if (isKey) {
				if (op.equalyKey(arg)) {
					idx = i;
					break;
				}
			} else {
				if (!op.hasKey()) {
					idx = i;
					break;
				}
			}
		}
		if (-1 == idx) {
			return null;
		}
		Option result = options.get(idx);
		options.set(idx, null);
		return result;
	}

	/**
	 * 执行
	 * 
	 * @param args
	 */
	abstract void executeInner(List<Argument> args) throws Exception;
}
