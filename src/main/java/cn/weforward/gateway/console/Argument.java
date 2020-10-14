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

import java.util.List;

import cn.weforward.common.util.ComparatorExt;
import cn.weforward.common.util.ListUtil;
import cn.weforward.common.util.StringUtil;
import cn.weforward.gateway.console.exception.ArgumentException;

/**
 * 参数
 * 
 * @author zhangpengji
 *
 */
public class Argument {

	static final ComparatorExt<Argument, String> CMP_BY_NAME = new ComparatorExt<Argument, String>() {

		@Override
		public int compare(Argument o1, Argument o2) {
			return StringUtil.compareTo(o1.getOption().getKey(), o2.getOption().getKey());
		}

		@Override
		public int compareTo(Argument element, String key) {
			return StringUtil.compareTo(element.getOption().getKey(), key);
		}
	};

	Option m_Option;
	String[] m_Values;
	int m_ValueIdx;

	public Argument(Option op) {
		m_Option = op;
		m_Values = new String[op.getValueSize()];
		m_ValueIdx = 0;
	}

	void addValue(String value) {
		if (('"' == value.charAt(0) && '"' == value.charAt(value.length() - 1))
				|| ('\'' == value.charAt(0) && '\'' == value.charAt(value.length() - 1))) {
			// 去除引号
			value = value.substring(1, value.length() - 1);
		}
		m_Values[m_ValueIdx++] = value;
	}

	boolean enoughValues() {
		return m_ValueIdx >= m_Values.length;
	}

	Option getOption() {
		return m_Option;
	}

	String getValue(int idx) {
		return m_Values[idx];
	}

	int getValueSize() {
		return m_ValueIdx;
	}

	void check() throws ArgumentException {
		if (enoughValues() || !m_Option.isValuesRequire()) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("缺少参数值");
		for (String v : m_Option.getValues()) {
			sb.append('\'').append(v).append('\'').append(' ');
		}
		sb.setLength(sb.length() - 1);
		throw new ArgumentException(sb.toString());
	}

	public static boolean contain(List<Argument> args, Option op) {
		return ListUtil.binarySearch(args, op.getKey(), Argument.CMP_BY_NAME) >= 0;
	}

	public static String getValue(List<Argument> args, Option op) {
		int idx = ListUtil.binarySearch(args, op.getKey(), Argument.CMP_BY_NAME);
		if (idx >= 0) {
			return args.get(idx).getValue(0);
		}
		return null;
	}

	public static Boolean getBooleanValue(List<Argument> args, Option op) {
		int idx = ListUtil.binarySearch(args, op.getKey(), Argument.CMP_BY_NAME);
		if (idx >= 0) {
			String str = args.get(idx).getValue(0);
			if ("true".equalsIgnoreCase(str) || "t".equalsIgnoreCase(str)) {
				return true;
			} else if ("false".equalsIgnoreCase(str) || "f".equalsIgnoreCase(str)) {
				return false;
			} else {
				throw new ArgumentException("不是有效的布尔值：" + str);
			}
		}
		return null;
	}

	public static Integer getIntValue(List<Argument> args, Option op) {
		int idx = ListUtil.binarySearch(args, op.getKey(), Argument.CMP_BY_NAME);
		if (idx >= 0) {
			String str = args.get(idx).getValue(0);
			try {
				return Integer.valueOf(str);
			} catch (NumberFormatException e) {
				throw new ArgumentException("不是有效的整型值：" + str);
			}
		}
		return null;
	}

	public static String getValue(List<Argument> args, int idx) {
		int i = 0;
		for (Argument arg : args) {
			if (!StringUtil.isEmpty(arg.getOption().getKey())) {
				continue;
			}
			if (i == idx) {
				return arg.getValue(0);
			} else {
				idx++;
			}
		}
		return null;
	}
}
