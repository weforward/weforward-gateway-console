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
import java.util.Arrays;
import java.util.List;

import cn.weforward.common.util.ListUtil;
import cn.weforward.gateway.console.exception.ArgumentException;
import cn.weforward.gateway.console.util.FitTable;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.gateway.vo.TrafficTableItemVo;
import cn.weforward.protocol.gateway.vo.TrafficTableItemWrap;
import cn.weforward.protocol.ops.traffic.TrafficTable;
import cn.weforward.protocol.ops.traffic.TrafficTableItem;

/**
 * 'traffic'命令
 * 
 * @author zhangpengji
 *
 */
public class TrafficCommand extends Command {

	Option m_Print;
	Option m_Append;
	Option m_Insert;
	Option m_Replace;
	Option m_Move;
	Option m_Delete;
	Option m_ServiceName;
	Option m_Title;
	Option m_ServiceNo;
	Option m_ServiceVersion;
	Option m_Weight;
	Option m_MaxConcurrent;
	Option m_MaxFails;
	Option m_FailTimeout;
	Option m_ReadTimeout;
	Option m_Index;
	Option m_FromIndex;
	Option m_ToIndex;
	List<Option> m_Options;

	protected TrafficCommand(Console console) {
		super(console);

		m_Print = new Option("P").setDesc("显示微服务的流量表");
		m_Append = new Option("A").setDesc("追加一个流量规则");
		m_Insert = new Option("I").setDesc("插入一个流量规则");
		m_Replace = new Option("R").setDesc("替换一个流量规则");
		m_Move = new Option("M").setDesc("移动流量规则的位置");
		m_Delete = new Option("D").setDesc("删除流量规则的位置");
		m_ServiceName = new Option("name").setSecondOption(true).setValues("service-name").setDesc("微服务名称");
		m_Title = new Option("title").setSecondOption(true).setValues("rule-title").setDesc("规则项的标题");
		m_ServiceNo = new Option("no").setSecondOption(true).setValues("service-no").setDesc("微服务实例编号");
		m_ServiceVersion = new Option("version").setSecondOption(true).setValues("service-version").setDesc("微服务实例版本");
		m_Weight = new Option("weight").setSecondOption(true).setValues("number")
				.setDesc("权重。由1~100，-100表示其为后备，0表示屏蔽。默认值：" + TrafficTableItem.WEIGHT_DEFAULT);
		m_MaxConcurrent = new Option("max-concurrent").setSecondOption(true).setValues("number")
				.setDesc("最大并发数。控制并发量防止过载。默认值：" + TrafficTableItem.MAX_CONCURRENT_DEFAULT);
		m_MaxFails = new Option("max-fails").setSecondOption(true).setValues("number")
				.setDesc("最大连续失败次数。连续失败此值后将标记项为失败且不使用，直至fail_timeout后才重新使用。默认值：" + TrafficTableItem.MAX_FAILS_DEFAULT);
		m_FailTimeout = new Option("fail-timeout").setSecondOption(true).setValues("number")
				.setDesc("标记为失败的项重新再使用的时间（秒）。默认值：" + TrafficTableItem.FAIL_TIMEOUT_DEFAULT);
		m_ReadTimeout = new Option("read-timeout").setSecondOption(true).setValues("number")
				.setDesc("网关等待微服务响应的超时值（秒）。默认值：" + TrafficTableItem.READ_TIMEOUT_DEFAULT);
		m_Index = new Option("index").setSecondOption(true).setValues("number").setDesc("规则项的序号（从0开始）");
		m_FromIndex = new Option("from-index").setSecondOption(true).setValues("number").setDesc("当前序号（从0开始）");
		m_ToIndex = new Option("to-index").setSecondOption(true).setValues("number").setDesc("目标序号（从0开始）");

		m_Options = Arrays.asList(m_Print, m_Append, m_Insert, m_Replace, m_Move, m_Delete, m_ServiceName, m_Title,
				m_ServiceNo, m_ServiceVersion, m_Weight, m_MaxConcurrent, m_MaxFails, m_FailTimeout, m_ReadTimeout,
				m_Index, m_FromIndex, m_ToIndex);
	}

	@Override
	String getName() {
		return "traffic";
	}

	@Override
	String getShortName() {
		return "tt";
	}

	@Override
	String getSynopsis() {
		return "微服务的访问流量管理";
	}

	@Override
	List<String> getUsages() {
		String show = "tt -P --name service-name";

		String append = "tt -A --name service-name [--no service-no]"
				+ " [--version service-version] [--weight number] [--max-concurrent number] [--max-fails number]"
				+ " [--fail-timeout number] [--read-timeout number] [--title rule-title]";

		String insert = "tt -I --name service-name --index number [--no service-no]"
				+ " [--version service-version] [--weight number] [--max-concurrent number] [--max-fails number]"
				+ " [--fail-timeout number] [--read-timeout number] [--title rule-title]";

		String replace = "tt -R --name service-name --index number [--no service-no]"
				+ " [--version service-version] [--weight number] [--max-concurrent number] [--max-fails number]"
				+ " [--fail-timeout number] [--read-timeout number] [--title rule-title]";

		String move = "tt -M --name service-name --from-index number --to-index number";

		String delete = "tt -D --name service-name --index number";
		return Arrays.asList(show, append, insert, replace, move, delete);
	}

	@Override
	List<Option> getOptions() {
		return m_Options;
	}

	@Override
	void executeInner(List<Argument> args) {
		String name = Argument.getValue(args, m_ServiceName);
		String serviceNo = Argument.getValue(args, m_ServiceNo);
		String serviceVersion = Argument.getValue(args, m_ServiceVersion);
		String title = Argument.getValue(args, m_Title);
		Integer weight = Argument.getIntValue(args, m_Weight);
		Integer maxConcurrent = Argument.getIntValue(args, m_MaxConcurrent);
		Integer maxFails = Argument.getIntValue(args, m_MaxFails);
		Integer failTimeout = Argument.getIntValue(args, m_FailTimeout);
		Integer readTimeout = Argument.getIntValue(args, m_ReadTimeout);
		Integer index = Argument.getIntValue(args, m_Index);
		Integer fromIndex = Argument.getIntValue(args, m_FromIndex);
		Integer toIndex = Argument.getIntValue(args, m_ToIndex);
		if (null == name) {
			throw ArgumentException.missArgument(m_ServiceName);
		}

		if (Argument.contain(args, m_Print)) {
			showTrafficTable(name);
		} else if (Argument.contain(args, m_Append)) {
			TrafficTableItemVo vo = createItem(title, serviceNo, serviceVersion, weight, maxConcurrent, maxFails,
					failTimeout, readTimeout);
			appendTrafficTable(name, vo);
		} else if (Argument.contain(args, m_Insert)) {
			TrafficTableItemVo vo = createItem(title, serviceNo, serviceVersion, weight, maxConcurrent, maxFails,
					failTimeout, readTimeout);
			insertTrafficTable(name, vo, index);
		} else if (Argument.contain(args, m_Replace)) {
			TrafficTableItemVo vo = createItem(title, serviceNo, serviceVersion, weight, maxConcurrent, maxFails,
					failTimeout, readTimeout);
			replaceTrafficTable(name, vo, index);
		} else if (Argument.contain(args, m_Move)) {
			moveTrafficTable(name, fromIndex, toIndex);
		} else if (Argument.contain(args, m_Delete)) {
			deleteTrafficTable(name, index);
		} else {
			throw ArgumentException.help(getName());
		}
	}

	void showTrafficTable(String name) {
		Keeper keeper = getKeeper();
		TrafficTable table = keeper.getTrafficTable(name);
		show(name, table);
	}

	void appendTrafficTable(String name, TrafficTableItemVo vo) {
		Keeper keeper = getKeeper();
		TrafficTable table = keeper.appendTrafficRule(name, new TrafficTableItemWrap(vo));
		show(name, table);
	}

	void insertTrafficTable(String name, TrafficTableItemVo vo, Integer index) {
		if (null == index) {
			throw ArgumentException.missArgument(m_Index);
		}
		Keeper keeper = getKeeper();
		TrafficTable table = keeper.insertTrafficRule(name, new TrafficTableItemWrap(vo), index);
		show(name, table);
	}

	void replaceTrafficTable(String name, TrafficTableItemVo vo, Integer index) {
		if (null == index) {
			throw ArgumentException.missArgument(m_Index);
		}
		Keeper keeper = getKeeper();
		TrafficTable table = keeper.replaceTrafficRule(name, new TrafficTableItemWrap(vo), index, null);
		show(name, table);
	}

	void moveTrafficTable(String name, Integer fromIndex, Integer toIndex) {
		if (null == fromIndex) {
			throw ArgumentException.missArgument(m_FromIndex);
		}
		if (null == toIndex) {
			throw ArgumentException.missArgument(m_ToIndex);
		}
		Keeper keeper = getKeeper();
		TrafficTable table = keeper.moveTrafficRule(name, fromIndex, toIndex);
		show(name, table);
	}

	void deleteTrafficTable(String name, Integer index) {
		if (null == index) {
			throw ArgumentException.missArgument(m_Index);
		}
		Keeper keeper = getKeeper();
		TrafficTable table = keeper.removeTrafficRule(name, index, null);
		show(name, table);
	}

	TrafficTableItemVo createItem(String title, String serviceNo, String serviceVersion, Integer weight,
			Integer maxConcurrent, Integer maxFails, Integer failTimeout, Integer readTimeout) {
		TrafficTableItemVo item = new TrafficTableItemVo(serviceNo, serviceVersion);
		item.name = title;
		if (null != weight) {
			item.weight = weight;
		}
		if (null != maxConcurrent) {
			item.maxConcurrent = maxConcurrent;
		}
		if (null != maxFails) {
			item.maxFails = maxFails;
		}
		if (null != failTimeout) {
			item.failTimeout = failTimeout;
		}
		if (null != readTimeout) {
			item.readTimeout = readTimeout;
		}
		return item;
	}

	void show(String serviceName, TrafficTable tt) {
		PrintStream ps = getPrintStream();
		ps.println(serviceName + "的流量表");
		if (null == tt) {
			ps.println("未配置");
			return;
		}
		List<TrafficTableItem> items = tt.getItems();
		if (ListUtil.isEmpty(items)) {
			ps.println("未配置");
			return;
		}
		FitTable table = new FitTable(true, 1, 1);
		table.addCell("index");
		table.addCell("title");
		table.addCell("no");
		table.addCell("version");
		table.addCell("weight");
		table.addCell("max-concurrent");
		table.addCell("max-fails");
		table.addCell("fail-timeout");
		table.addCell("read-timeout");
		for (int i = 0; i < items.size(); i++) {
			table.nextRow();
			TrafficTableItem item = items.get(i);
			table.addCell(String.valueOf(i));
			table.addCell(item.getName());
			table.addCell(item.getServiceNo());
			table.addCell(item.getServiceVersion());
			table.addCell(String.valueOf(item.getWeight()));
			table.addCell(String.valueOf(item.getMaxConcurrent()));
			table.addCell(String.valueOf(item.getMaxFails()));
			table.addCell(String.valueOf(item.getFailTimeout()));
			table.addCell(String.valueOf(item.getReadTimeout()));
		}
		table.print(ps);
	}
}
