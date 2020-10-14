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
package cn.weforward.gateway.console.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import cn.weforward.common.util.StringUtil;

/**
 * 表格样式输出。
 * <p>
 * 由于中文字符与英文字符的宽度不一定成整数倍关系（取决于显示终端是否为等宽字体）， 所以只能假设一个中文字符等于两个英文字符的宽度，
 * 可以保证在大部分终端显示正常，eclipse控制台显示无法对齐。
 * 
 * @author zhangpengji
 *
 */
public class FitTable {

	List<List<Cell>> m_Cells;
	int m_RowIndex;
	// 每列宽度
	List<Integer> m_ColWidths;
	boolean m_Border;
	int m_LeftPadding;
	int m_RightPadding;

	public FitTable() {
		this(false, 0, 4);
	}

	public FitTable(boolean border, int leftPadding, int rightPadding) {
		m_Cells = new ArrayList<List<Cell>>();
		m_RowIndex = 0;
		m_ColWidths = new ArrayList<Integer>();
		m_Border = border;
		m_LeftPadding = leftPadding;
		m_RightPadding = rightPadding;
	}

	public void addCell(String value) {
		List<Cell> row;
		if (m_Cells.size() == m_RowIndex) {
			row = new ArrayList<FitTable.Cell>();
			m_Cells.add(row);
		} else {
			row = m_Cells.get(m_RowIndex);
		}
		Cell cell = Cell.valueOf(value);
		row.add(cell);

		// 更新列宽度
		if (m_ColWidths.size() < row.size()) {
			m_ColWidths.add(cell.getWidth());
		} else {
			int wight = m_ColWidths.get(row.size() - 1);
			if (wight < cell.getWidth()) {
				m_ColWidths.set(row.size() - 1, cell.getWidth());
			}
		}
	}

	public void nextRow() {
		if (m_Cells.size() <= m_RowIndex) {
			throw new IllegalStateException("不允许空行");
		}
		m_RowIndex++;
	}

	public void print(PrintStream ps) {
		if (0 == m_Cells.size()) {
			return;
		}
		printHorizontalBorder(ps);
		for (int ri = 0; ri < m_Cells.size(); ri++) {
			if (ri > 0) {
				printHorizontalBorder(ps);
			}
			List<Cell> row = m_Cells.get(ri);
			for (int ci = 0; ci < row.size(); ci++) {
				printVerticalBorder(ps);
				printLeftPadding(ps);

				Cell cell = row.get(ci);
				String value = stretch(cell.getValue(), cell.getWidth(), m_ColWidths.get(ci));
				ps.print(value);

				printRightPadding(ps);
			}
			ps.println();
		}
		printHorizontalBorder(ps);
	}

	private void printLeftPadding(PrintStream ps) {
		if (0 == m_LeftPadding) {
			return;
		}
		for (int i = 0; i < m_LeftPadding; i++) {
			ps.print(' ');
		}
	}

	private void printRightPadding(PrintStream ps) {
		if (0 == m_RightPadding) {
			return;
		}
		for (int i = 0; i < m_RightPadding; i++) {
			ps.print(' ');
		}
	}

	private void printHorizontalBorder(PrintStream ps) {
		if (!m_Border) {
			return;
		}

		ps.println();
	}

	private void printVerticalBorder(PrintStream ps) {
		if (!m_Border) {
			return;
		}
		ps.print('|');
	}

	private String stretch(String value, int width, int maxWidth) {
		if (width >= maxWidth) {
			return value;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(value);
		while (width < maxWidth) {
			sb.append(' ');
			width++;
		}
		return sb.toString();
	}

	private static class Cell {

		String m_Value;
		int m_Width;

		private Cell() {
			m_Value = "";
			m_Width = 0;
		}

		Cell(String value) {
			StringBuilder newValue = null;
			int width = 0;
			for (int i = 0; i < value.length(); i++) {
				char ch = value.charAt(i);
				if ((ch >= 0 && ch <= 31) || 127 == ch) {
					// 控制字符替换为空格
					if (null == newValue) {
						newValue = new StringBuilder(value.length());
						if (i > 0) {
							newValue.append(value.subSequence(0, i));
						}
					}
					ch = ' ';
					width += 1;
				} else if ((ch > 0x0100 && ch <= 0xD7AF) || (ch > 0xF900 && ch <= 0xFFFF)) {
					// 中日韩等字符占两个宽度
					width += 2;
				} else {
					width += 1;
				}
				if (null != newValue) {
					newValue.append(ch);
				}
			}

			if (null != newValue) {
				m_Value = newValue.toString();
			} else {
				m_Value = value;
			}
			m_Width = width;
		}

		static Cell valueOf(String value) {
			if (StringUtil.isEmpty(value)) {
				return EMPTY_CELL;
			}
			return new Cell(value);
		}

		String getValue() {
			return m_Value;
		}

		int getWidth() {
			return m_Width;
		}
	}

	static final Cell EMPTY_CELL = new Cell();
}
