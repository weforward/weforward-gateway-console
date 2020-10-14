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
import cn.weforward.protocol.Access;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.gateway.vo.RightTableItemVo;
import cn.weforward.protocol.gateway.vo.RightTableItemWrap;
import cn.weforward.protocol.ops.secure.RightTable;
import cn.weforward.protocol.ops.secure.RightTableItem;

/**
 * 'right'命令
 * 
 * @author zhangpengji
 *
 */
public class RightCommand extends Command {

	Option m_Print;
	Option m_Append;
	Option m_Insert;
	Option m_Replace;
	Option m_Move;
	Option m_Delete;
	Option m_ServiceName;
	Option m_Id;
	Option m_Kind;
	Option m_Group;
	Option m_Allow;
	Option m_Title;
	Option m_Description;
	Option m_Index;
	Option m_FromIndex;
	Option m_ToIndex;
	List<Option> m_Options;

	protected RightCommand(Console console) {
		super(console);

		m_Print = new Option("P").setDesc("显示微服务的权限表");
		m_Append = new Option("A").setDesc("追加一个权限规则");
		m_Insert = new Option("I").setDesc("插入一个权限规则");
		m_Replace = new Option("R").setDesc("替换一个权限规则");
		m_Move = new Option("M").setDesc("移动权限规则的位置");
		m_Delete = new Option("D").setDesc("删除权限规则的位置");
		m_ServiceName = new Option("name").setSecondOption(true).setValues("service-name").setDesc("微服务名称");
		m_Id = new Option("id").setSecondOption(true).setValues("access-id").setDesc("访问凭证的标识（Access Id）");
		m_Kind = new Option("kind").setSecondOption(true).setValues("access-kind")
				.setDesc("访问凭证的类型（Access Kind）: " + Access.KIND_SERVICE + " , " + Access.KIND_USER);
		m_Group = new Option("group").setSecondOption(true).setValues("access-group").setDesc("访问凭证的组（Access Group）");
		m_Allow = new Option("allow").setSecondOption(true).setValues("true|false").setDesc("是否允许访问");
		m_Title = new Option("title").setSecondOption(true).setValues("rule-title").setDesc("规则项的标题");
		m_Description = new Option("desc").setSecondOption(true).setValues("rule-description").setDesc("规则项的描述");
		m_Index = new Option("index").setSecondOption(true).setValues("number").setDesc("规则项的序号（从0开始）");
		m_FromIndex = new Option("from-index").setSecondOption(true).setValues("number").setDesc("当前序号（从0开始）");
		m_ToIndex = new Option("to-index").setSecondOption(true).setValues("number").setDesc("目标序号（从0开始）");

		m_Options = Arrays.asList(m_Print, m_Append, m_Insert, m_Replace, m_Move, m_Delete, m_ServiceName, m_Id, m_Kind,
				m_Group, m_Allow, m_Title, m_Description, m_Index, m_FromIndex, m_ToIndex);
	}

	@Override
	String getName() {
		return "right";
	}

	@Override
	String getShortName() {
		return "rt";
	}

	@Override
	String getSynopsis() {
		return "微服务的访问权限管理";
	}

	@Override
	List<String> getUsages() {
		String show = "rt -P --name service-name";
		String append = "rt -A --name service-name --allow true|false [--id access-id]"
				+ " [--kind access-kind] [--group access-group] [--title rule-title] [--desc rule-description]";
		String insert = "rt -I --name service-name --allow true|false --index number [--id access-id]"
				+ " [--kind access-kind] [--group access-group] [--title rule-title] [--desc rule-description]";
		String replace = "rt -R --name service-name --allow true|false --index number [--id access-id]"
				+ " [--kind access-kind] [--group access-group] [--title rule-title] [--desc rule-description]";
		String move = "rt -M --name service-name --from-index number --to-index number";
		String delete = "rt -D --name service-name --index number";
		return Arrays.asList(show, append, insert, replace, move, delete);
	}

	@Override
	List<Option> getOptions() {
		return m_Options;
	}

	@Override
	void executeInner(List<Argument> args) {
		String name = Argument.getValue(args, m_ServiceName);
		String accessId = Argument.getValue(args, m_Id);
		String accessKind = Argument.getValue(args, m_Kind);
		String accessGroup = Argument.getValue(args, m_Group);
		Boolean allow = Argument.getBooleanValue(args, m_Allow);
		String title = Argument.getValue(args, m_Title);
		String desc = Argument.getValue(args, m_Description);
		Integer index = Argument.getIntValue(args, m_Index);
		Integer fromIndex = Argument.getIntValue(args, m_FromIndex);
		Integer toIndex = Argument.getIntValue(args, m_ToIndex);
		if (null == name) {
			throw ArgumentException.missArgument(m_ServiceName);
		}

		if (Argument.contain(args, m_Print)) {
			showRightTable(name);
		} else if (Argument.contain(args, m_Append)) {
			appendRightTable(name, accessId, accessKind, accessGroup, allow, title, desc);
		} else if (Argument.contain(args, m_Insert)) {
			insertRightTable(name, accessId, accessKind, accessGroup, allow, title, desc, index);
		} else if (Argument.contain(args, m_Replace)) {
			replaceRightTable(name, accessId, accessKind, accessGroup, allow, title, desc, index);
		} else if (Argument.contain(args, m_Move)) {
			moveRightTable(name, fromIndex, toIndex);
		} else if (Argument.contain(args, m_Delete)) {
			deleteRightTable(name, index);
		} else {
			throw ArgumentException.help(getName());
		}
	}

	void showRightTable(String serviceName) {
		Keeper keeper = getKeeper();
		RightTable table = keeper.getRightTable(serviceName);
		show(serviceName, table);
	}

	void appendRightTable(String serviceName, String accessId, String accessKind, String accessGroup, Boolean allow,
			String title, String desc) {
		if (null == allow) {
			throw ArgumentException.missArgument(m_Allow);
		}
		Keeper keeper = getKeeper();
		RightTableItemVo item = new RightTableItemVo();
		item.name = title;
		item.accessId = accessId;
		item.accessKind = accessKind;
		item.accessGroup = accessGroup;
		item.allow = allow.booleanValue();
		item.description = desc;
		RightTable table = keeper.appendRightRule(serviceName, new RightTableItemWrap(item));
		show(serviceName, table);
	}

	void insertRightTable(String serviceName, String accessId, String accessKind, String accessGroup, Boolean allow,
			String title, String desc, Integer index) {
		if (null == allow) {
			throw ArgumentException.missArgument(m_Allow);
		}
		if (null == index) {
			throw ArgumentException.missArgument(m_Index);
		}
		Keeper keeper = getKeeper();
		RightTableItemVo item = new RightTableItemVo();
		item.name = title;
		item.accessId = accessId;
		item.accessKind = accessKind;
		item.accessGroup = accessGroup;
		item.allow = allow;
		item.description = desc;
		RightTable table = keeper.insertRightRule(serviceName, new RightTableItemWrap(item), index);
		show(serviceName, table);
	}

	void replaceRightTable(String serviceName, String accessId, String accessKind, String accessGroup, Boolean allow,
			String title, String desc, Integer index) {
		if (null == allow) {
			throw ArgumentException.missArgument(m_Allow);
		}
		if (null == index) {
			throw ArgumentException.missArgument(m_Index);
		}
		Keeper keeper = getKeeper();
		RightTableItemVo item = new RightTableItemVo();
		item.name = title;
		item.accessId = accessId;
		item.accessKind = accessKind;
		item.accessGroup = accessGroup;
		item.allow = allow;
		item.description = desc;
		RightTable table = keeper.replaceRightRule(serviceName, new RightTableItemWrap(item), index, null);
		show(serviceName, table);
	}

	void moveRightTable(String serviceName, Integer from, Integer to) {
		if (null == from) {
			throw ArgumentException.missArgument(m_FromIndex);
		}
		if (null == to) {
			throw ArgumentException.missArgument(m_ToIndex);
		}
		Keeper keeper = getKeeper();
		RightTable table = keeper.moveRightRule(serviceName, from, to);
		show(serviceName, table);
	}

	void deleteRightTable(String serviceName, Integer index) {
		if (null == index) {
			throw ArgumentException.missArgument(m_Index);
		}
		Keeper keeper = getKeeper();
		RightTable table = keeper.removeRightRule(serviceName, index, null);
		show(serviceName, table);
	}

	void show(String serviceName, RightTable rt) {
		PrintStream ps = getPrintStream();
		ps.println(serviceName + "的权限表");
		show(rt, ps);
	}

	static void show(RightTable rt, PrintStream ps) {
		if (null == rt) {
			ps.println("未配置");
			return;
		}
		List<RightTableItem> items = rt.getItems();
		if (ListUtil.isEmpty(items)) {
			ps.println("未配置");
			return;
		}
		FitTable table = new FitTable(true, 1, 1);
		table.addCell("序号");
		table.addCell("标题");
		table.addCell("access id");
		table.addCell("access kind");
		table.addCell("access group");
		table.addCell("策略");
		table.addCell("描述");
		for (int i = 0; i < items.size(); i++) {
			table.nextRow();
			RightTableItem item = items.get(i);
			table.addCell(String.valueOf(i));
			table.addCell(item.getName());
			table.addCell(item.getAccessId());
			table.addCell(item.getAccessKind());
			table.addCell(item.getAccessGroup());
			table.addCell(item.isAllow() ? "允许" : "禁止");
			table.addCell(item.getDescription());
		}
		table.print(ps);
	}
}
