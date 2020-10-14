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
package cn.weforward.gateway.console.exception;

import cn.weforward.gateway.console.Option;

/**
 * 参数异常
 * 
 * @author zhangpengji
 *
 */
public class ArgumentException extends CommonException {

	private static final long serialVersionUID = 1L;

	public ArgumentException(String message) {
		super(message, null, false, false);
	}

	public static ArgumentException illegalOption(String option) {
		return new ArgumentException("无效选项：" + option);
	}

	public static ArgumentException illegalArgument(String arg) {
		return new ArgumentException("无效参数：" + arg);
	}

	public static ArgumentException help(String cmd) {
		return new ArgumentException("无效参数组合，请输入：help " + cmd + "，查看命令使用方法");
	}

	public static ArgumentException missArgument(Option op) {
		return new ArgumentException("缺少参数'" + op.getFullKey() + "'");
	}
}
