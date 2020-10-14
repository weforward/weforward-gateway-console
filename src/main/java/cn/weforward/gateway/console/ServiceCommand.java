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

import java.util.Arrays;
import java.util.List;

import cn.weforward.common.ResultPage;
import cn.weforward.common.util.ListUtil;
import cn.weforward.common.util.NumberUtil;
import cn.weforward.common.util.TimeUtil;
import cn.weforward.gateway.console.exception.ArgumentException;
import cn.weforward.gateway.console.util.FitTable;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.gateway.SearchServiceParams;
import cn.weforward.protocol.ops.ServiceExt;

/**
 * 'service'命令
 * 
 * @author zhangpengji
 *
 */
public class ServiceCommand extends Command {

	Option m_Print;
	Option m_Keyword;
	Option m_Page;
	Option m_PageSize;
	List<Option> m_Options;

	protected ServiceCommand(Console console) {
		super(console);

		m_Print = new Option("P").setDesc("列举所有微服务");
		m_Keyword = new Option("keyword").setSecondOption(true).setValues("keyword").setDesc("搜索关键字");
		m_Page = new Option("page").setSecondOption(true).setValues("page").setDesc("分页数据集的当前页");
		m_PageSize = new Option("page-size").setSecondOption(true).setValues("page-size").setDesc("分页数据集的页面项数");
		m_Options = Arrays.asList(m_Print, m_Keyword, m_Page, m_PageSize);
	}

	@Override
	String getName() {
		return "service";
	}

	@Override
	String getShortName() {
		return "svc";
	}

	@Override
	String getSynopsis() {
		return "微服务查看";
	}

	@Override
	List<String> getUsages() {
		String list = "sc -P [--keyword keyword] [--page 1] [--page-size 10]";
		return Arrays.asList(list);
	}

	@Override
	List<Option> getOptions() {
		return m_Options;
	}

	@Override
	void executeInner(List<Argument> args) {
		String keyword = Argument.getValue(args, m_Keyword);
		String page = Argument.getValue(args, m_Page);
		String pageSize = Argument.getValue(args, m_PageSize);

		if (Argument.contain(args, m_Print)) {
			searchServices(keyword, page, pageSize);
		} else {
			throw ArgumentException.help(getName());
		}
	}

	void searchServices(String keyword, String pageStr, String pageSizeStr) {
		Keeper keeper = getKeeper();
		SearchServiceParams searchParams = new SearchServiceParams();
		searchParams.setKeyword(keyword);
		ResultPage<ServiceExt> services = keeper.searchService(searchParams);
		int page = NumberUtil.toInt(pageStr, 1);
		int pageSize = NumberUtil.toInt(pageSizeStr, 10);
		services.setPageSize(pageSize);
		services.gotoPage(page);
		getPrintStream().println("总项数：" + services.getCount() + "，当前页：" + page + "，每页项数：" + pageSize);
		show(services);
	}

	void show(Iterable<ServiceExt> services) {
		FitTable table = new FitTable(true, 1, 1);
		table.addCell("名称");
		table.addCell("编号");
		table.addCell("版本");
		table.addCell("心跳时间");
		table.addCell("状态");
		table.addCell("链接");
		StringBuilder sb = new StringBuilder();
		for (ServiceExt sc : services) {
			table.nextRow();
			table.addCell(sc.getName());
			table.addCell(sc.getNo());
			table.addCell(sc.getVersion());
			table.addCell(TimeUtil.formatDateTime(sc.getHeartbeat()));

			String state;
			sb.setLength(0);
			if (sc.isInaccessible()) {
				sb.append("不可达\n");
			}
			if (sc.isUnavailable()) {
				sb.append("不可用\n");
			}
			if (sc.isOverload()) {
				sb.append("过载\n");
			}
			if (sc.isTimeout()) {
				sb.append("宕机");
			}
			if (0 == sb.length()) {
				sb.append("正常");
			}
			state = sb.toString();
			table.addCell(state);

			List<String> urls = sc.getUrls();
			sb.setLength(0);
			if (!ListUtil.isEmpty(urls)) {
				for (String url : urls) {
					if (sb.length() > 0) {
						sb.append(' ');
					}
					sb.append(url);
				}
			}
			table.addCell(sb.toString());
		}
		table.print(getPrintStream());
	}
}
