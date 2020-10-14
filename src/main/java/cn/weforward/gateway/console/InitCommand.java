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

import cn.weforward.common.ResultPage;
import cn.weforward.common.util.ListUtil;
import cn.weforward.common.util.ResultPageHelper;
import cn.weforward.protocol.Access;
import cn.weforward.protocol.ServiceName;
import cn.weforward.protocol.gateway.Keeper;
import cn.weforward.protocol.gateway.vo.RightTableItemVo;
import cn.weforward.protocol.gateway.vo.RightTableItemWrap;
import cn.weforward.protocol.ops.AccessExt;
import cn.weforward.protocol.ops.secure.RightTable;

/**
 * 'init'命令
 * 
 * @author zhangpengji
 *
 */
public class InitCommand extends Command {

	protected InitCommand(Console console) {
		super(console);
	}

	@Override
	String getName() {
		return "init";
	}

	@Override
	String getSynopsis() {
		return "首次部署网关后，执行初始化";
	}

	@Override
	List<String> getUsages() {
		return Collections.singletonList("init");
	}

	@Override
	List<Option> getOptions() {
		return Collections.emptyList();
	}

	@Override
	void executeInner(List<Argument> args) {
		AccessExt access = initDevopsServiceAccess();

		initKeeperApiRightTable(access);

		initDevopsServiceRightTable();

		initServiceRegisterApiRightTable();
	}

	AccessExt initDevopsServiceAccess() {
		Keeper keeper = getKeeper();
		String serviceName = "devops";
		AccessExt serviceAccess = null;
		ResultPage<AccessExt> accesses = keeper.listAccess(Access.KIND_SERVICE, null, serviceName);
		for (AccessExt acc : ResultPageHelper.toForeach(accesses)) {
			if (acc.getSummary().equals(serviceName)) {
				serviceAccess = acc;
				break;
			}
		}
		PrintStream ps = getPrintStream();
		if (null == serviceAccess) {
			serviceAccess = keeper.createAccess(Access.KIND_SERVICE, AccessExt.DEFAULT_GROUP, serviceName);
			ps.println("创建微服务'" + serviceName + "'的访问凭证");
		} else {
			ps.println("微服务'" + serviceName + "'的访问凭证已存在");
		}
		AccessCommand.show(Collections.singletonList(serviceAccess), ps);
		return serviceAccess;
	}

	void initDevopsServiceRightTable() {
		Keeper keeper = getKeeper();
		String serviceName = "devops";
		RightTable table = keeper.getRightTable(serviceName);
		PrintStream ps = getPrintStream();
		if (null == table || ListUtil.isEmpty(table.getItems())) {
			RightTableItemVo ri = new RightTableItemVo();
			ri.allow = true;
			ri.name = "user";
			ri.accessKind = Access.KIND_USER;
			table = keeper.appendRightRule(serviceName, new RightTableItemWrap(ri));
			ri.allow = true;
			ri.name = "service";
			ri.accessKind = Access.KIND_SERVICE;
			table = keeper.appendRightRule(serviceName, new RightTableItemWrap(ri));
			ri.allow = true;
			ri.name = "guest";
			table = keeper.appendRightRule(serviceName, new RightTableItemWrap(ri));
			ps.println("创建微服务'" + serviceName + "'权限表");
		} else {
			ps.println("微服务'" + serviceName + "'权限表已存在");
		}
		RightCommand.show(table, ps);
	}

	void initKeeperApiRightTable(AccessExt devopsAccess) {
		Keeper keeper = getKeeper();
		String apiName = ServiceName.KEEPER.name;
		RightTable table = keeper.getRightTable(apiName);
		PrintStream ps = getPrintStream();
		if (null != table && table.getItems().size() >= 2) {
			ps.println("Api'" + apiName + "'权限表已存在");
		} else {
			RightTableItemVo vo = new RightTableItemVo();
			vo.accessId = devopsAccess.getAccessId();
			vo.allow = true;
			vo.name = "init";
			vo.description = "devops";
			table = keeper.appendRightRule(apiName, new RightTableItemWrap(vo));
			ps.println("追加微服务'devops'访问Api'" + apiName + "'的权限");
		}
		RightCommand.show(table, ps);
	}

	void initServiceRegisterApiRightTable() {
		Keeper keeper = getKeeper();
		String apiName = ServiceName.SERVICE_REGISTER.name;
		RightTable table = keeper.getRightTable(apiName);
		PrintStream ps = getPrintStream();
		if (null == table || ListUtil.isEmpty(table.getItems())) {
			RightTableItemVo vo = new RightTableItemVo();
			vo.accessKind = Access.KIND_SERVICE;
			vo.allow = true;
			vo.name = "init";
			table = keeper.appendRightRule(apiName, new RightTableItemWrap(vo));
			ps.println("创建Api'" + apiName + "'权限表");
		} else {
			ps.println("Api'" + apiName + "'权限表已存在");
		}
		RightCommand.show(table, ps);
	}
}
