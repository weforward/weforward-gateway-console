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

import java.util.Comparator;

import cn.weforward.common.util.StringUtil;

/**
 * 命令的选项
 * 
 * @author zhangpengji
 *
 */
public class Option {

	static final String KEY_PREFIX = "-";
	static final String KEY_PREFIX_SECOND = "--";

	static final Comparator<Option> CMP = new Comparator<Option>() {

		@Override
		public int compare(Option o1, Option o2) {
			return 0;
		}
	};

	/** 前缀 */
	String m_Prefix;
	/** 关键字 */
	String m_Key;
	/** 值的类型 */
	String[] m_Values;
	/** 描述 */
	String m_Desc;
	/** 值是否必需 */
	boolean m_ValuesRequire = true;

	public Option() {

	}

	public Option(String key) {
		m_Prefix = KEY_PREFIX;
		m_Key = key;
	}

	public String getKey() {
		return m_Key;
	}

	public String getPrefix() {
		return m_Prefix;
	}

	public Option setSecondOption(boolean bool) {
		m_Prefix = bool ? KEY_PREFIX_SECOND : KEY_PREFIX;
		return this;
	}

	public String getFullKey() {
		return m_Prefix + m_Key;
	}

	public String[] getValues() {
		return m_Values;
	}

	public Option setValues(String... values) {
		m_Values = values;
		return this;
	}

	public String getDesc() {
		return m_Desc;
	}

	public Option setDesc(String desc) {
		m_Desc = desc;
		return this;
	}

	public Option setValuesRequire(boolean bool) {
		m_ValuesRequire = bool;
		return this;
	}

	public boolean isValuesRequire() {
		return m_ValuesRequire;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public String toString() {
		/*
		 * \t 表示制表符，相当于制表符
		 * 前面的输出内容位数为8的倍数，\t将输出8个空格
		 * 前面的输出内容位数不是8的倍数，\t将补足8位
		 */
		StringBuilder sb = new StringBuilder();
		if (!StringUtil.isEmpty(m_Key)) {
			sb.append(m_Prefix).append(m_Key);
		}
		if (null != m_Values && m_Values.length > 0) {
			for (String value : m_Values) {
				sb.append(' ');
				sb.append(value);
			}
		}
		int i = sb.length() / 8;
		if (i >= 4) {
			// 直接换行
			sb.append('\n');
			i = 0;
		} else if (0 != sb.length() % 8) {
			// 补充一个制表符
			sb.append('\t');
			i++;
		}
		for (; i < 4; i++) {
			sb.append('\t');
		}
		sb.append(m_Desc);
		return sb.toString();
	}

	public int getValueSize() {
		return (null == m_Values) ? 0 : m_Values.length;
	}

	public boolean equalyKey(String key) {
		if (StringUtil.isEmpty(this.m_Key) || StringUtil.isEmpty(key)) {
			return false;
		}
		if (key.length() != (this.m_Prefix.length() + this.m_Key.length())) {
			return false;
		}
		return key.endsWith(this.m_Key);
	}

	public static boolean isKey(String str) {
		if (StringUtil.isEmpty(str)) {
			return false;
		}
		return str.startsWith(KEY_PREFIX);
	}

	public boolean hasKey() {
		return !StringUtil.isEmpty(m_Key);
	}
}
