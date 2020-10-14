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
import java.util.Collections;
import java.util.List;

import cn.weforward.common.ResultPage;
import cn.weforward.common.util.NumberUtil;
import cn.weforward.common.util.StringUtil;
import cn.weforward.gateway.console.exception.ArgumentException;
import cn.weforward.gateway.console.exception.CommonException;
import cn.weforward.gateway.console.util.FitTable;
import cn.weforward.protocol.Access;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.ops.AccessExt;

/**
 * 'access'命令
 * 
 * @author zhangpengji
 *
 */
public class AccessCommand extends Command {

	Option m_Print;
	Option m_ListGroups;
	Option m_Add;
	Option m_Modify;
	Option m_Id;
	Option m_Kind;
	Option m_Group;
	Option m_Summary;
	Option m_Valid;
	Option m_Keyword;
	Option m_Page;
	Option m_PageSize;
	List<Option> m_Options;

	protected AccessCommand(Console console) {
		super(console);

		m_Print = new Option("P").setDesc("列举所有Access");
		m_ListGroups = new Option("G").setDesc("列举已创建的访问凭证的组（Access Group）");
		m_Add = new Option("A").setDesc("添加Access");
		m_Modify = new Option("M").setDesc("修改Access");
		m_Id = new Option("id").setSecondOption(true).setValues("access id").setDesc("访问凭证的标识（Access Id）");
		m_Kind = new Option("kind").setSecondOption(true).setValues("access kind").setDesc("访问凭证的类型（Access Kind）");
		m_Group = new Option("group").setSecondOption(true).setValues("access group").setDesc("访问凭证的组（Access Group）");
		m_Summary = new Option("summary").setSecondOption(true).setValues("summary").setDesc("访问凭证的描述");
		m_Valid = new Option("valid").setSecondOption(true).setValues("true|false").setDesc("访问凭证是否有效");
		m_Keyword = new Option("keyword").setSecondOption(true).setValues("keyword").setDesc("访问凭证的描述的部分");
		m_Page = new Option("page").setSecondOption(true).setValues("page").setDesc("分页数据集的当前页");
		m_PageSize = new Option("page-size").setSecondOption(true).setValues("page size").setDesc("分页数据集的页面项数");

		m_Options = Arrays.asList(m_Print, m_ListGroups, m_Add, m_Modify, m_Id, m_Kind, m_Group, m_Summary, m_Valid,
				m_Keyword, m_Page, m_PageSize);
	}

	@Override
	String getName() {
		return "access";
	}

	@Override
	String getShortName() {
		return "acc";
	}

	@Override
	String getSynopsis() {
		return "访问凭证（Access）管理。访问凭证的类型有：服务凭证(H)、用户凭证(US)、网关凭证(GW)，允许添加、修改的仅有服务凭证(H)";
	}

	@Override
	List<String> getUsages() {
		String list = "acc -P [--kind H|GW] [--group access-group] [--keyword keyword] [--page 1] [--pagesize 10]";
		String groups = "acc -G";
		String add = "acc -A [--kind H] --group default --summary summary";
		String modify = "acc -M --id H-xxx [--summary summary] [--valid true|false]";
		return Arrays.asList(list, groups, add, modify);
	}

	@Override
	List<Option> getOptions() {
		return m_Options;
	}

	@Override
	void executeInner(List<Argument> args) {
		String id = Argument.getValue(args, m_Id);
		String kind = Argument.getValue(args, m_Kind);
		String group = Argument.getValue(args, m_Group);
		String summary = Argument.getValue(args, m_Summary);
		Boolean valid = Argument.getBooleanValue(args, m_Valid);
		String keyword = Argument.getValue(args, m_Keyword);
		String page = Argument.getValue(args, m_Page);
		String pageSize = Argument.getValue(args, m_PageSize);

		if (Argument.contain(args, m_Print)) {
			list(kind, group, keyword, page, pageSize);
		} else if (Argument.contain(args, m_ListGroups)) {
			listGroups();
		} else if (Argument.contain(args, m_Add)) {
			add(kind, group, summary);
		} else if (Argument.contain(args, m_Modify)) {
			modify(id, summary, valid);
		} else {
			throw ArgumentException.help(getName());
		}
	}

	void list(String kind, String group, String keyword, String pageStr, String pageSizeStr) {
		if (StringUtil.isEmpty(kind)) {
			// throw new CommonException("'kind'不能为空");
			kind = Access.KIND_SERVICE;
		}
		Keeper keeper = getKeeper();
		ResultPage<AccessExt> rp = keeper.listAccess(kind, group, keyword);
		if (0 == rp.getCount()) {
			getPrintStream().println("总项数：" + rp.getCount());
			return;
		}
		int page = NumberUtil.toInt(pageStr, 1);
		int pageSize = NumberUtil.toInt(pageSizeStr, 10);
		rp.setPageSize(pageSize);
		rp.gotoPage(page);
		getPrintStream().println("总项数：" + rp.getCount() + "，当前页：" + page + "，每页项数：" + pageSize);
		show(rp);
	}

	void listGroups() {
		List<String> groups = getKeeper().listAccessGroup(Access.KIND_SERVICE);
		PrintStream ps = getPrintStream();
		ps.println("个数：" + groups.size());
		if (0 == groups.size()) {
			return;
		}
		int i = 0;
		for (String g : groups) {
			if (i++ != 0) {
				ps.print(',');
			}
			if (0 == i % 5) {
				ps.println();
			} else {
				ps.print(' ');
			}
			ps.print(g);
		}
		ps.println();
	}

	void add(String kind, String group, String summary) {
		if (StringUtil.isEmpty(group) || StringUtil.isEmpty(summary)) {
			throw new CommonException("'kind','group','summary'不能为空");
		}
		if(StringUtil.isEmpty(kind)) {
			kind = Access.KIND_SERVICE;
		}
		AccessExt access = getKeeper().createAccess(kind, group, summary);
		show(access);
	}

	void modify(String id, String summary, Boolean valid) {
		if (StringUtil.isEmpty(id)) {
			throw new CommonException("'id'不能为空");
		}
		AccessExt access = getKeeper().updateAccess(id, summary, valid);
		show(access);
	}

	void show(AccessExt access) {
		show(Collections.singletonList(access));
	}

	void show(Iterable<AccessExt> accesses) {
		show(accesses, getPrintStream());
	}

	static void show(Iterable<AccessExt> accesses, PrintStream ps) {
		FitTable table = new FitTable(true, 1, 1);
		table.addCell("id");
		table.addCell("key");
		table.addCell("组");
		table.addCell("有效");
		table.addCell("摘要");
		for (AccessExt acc : accesses) {
			table.nextRow();
			table.addCell(acc.getAccessId());
			table.addCell(acc.getAccessKeyHex());
			table.addCell(acc.getGroupId());
			table.addCell(acc.isValid() ? "是" : "否");
			table.addCell(acc.getSummary());
		}
		table.print(ps);
	}
}
