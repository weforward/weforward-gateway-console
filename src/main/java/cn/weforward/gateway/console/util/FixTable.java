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
import java.util.Collections;
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
public class FixTable {

	List<List<Cell>> m_Cells;
	int m_RowIndex;
	// 每列宽度
	List<Integer> m_ColWidths;
	// 每行高度
	List<Integer> m_RowHeights;
	boolean m_Border;
	int m_LeftPadding;
	int m_RightPadding;

	public FixTable() {
		this(false, 0, 4);
	}

	public FixTable(boolean border, int leftPadding, int rightPadding) {
		m_Cells = new ArrayList<List<Cell>>();
		m_RowIndex = 0;
		m_ColWidths = new ArrayList<Integer>();
		m_RowHeights = new ArrayList<Integer>();
		m_Border = border;
		m_LeftPadding = leftPadding;
		m_RightPadding = rightPadding;
	}

	public void addCell(String value) {
		addCell(value, 0);
	}

	public void addCell(String value, int maxStretchWidth) {
		List<Cell> row;
		if (m_Cells.size() == m_RowIndex) {
			row = new ArrayList<FixTable.Cell>();
			m_Cells.add(row);
		} else {
			row = m_Cells.get(m_RowIndex);
		}
		Cell cell = Cell.valueOf(value, maxStretchWidth);
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
		// 更新行高度
		if (m_RowHeights.size() == m_RowIndex) {
			m_RowHeights.add(cell.getHeight());
		} else {
			int height = m_RowHeights.get(m_RowIndex);
			if (height < cell.getHeight()) {
				m_RowHeights.set(m_RowIndex, cell.getHeight());
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
		printHorizontalBorder(ps, 1);
		for (int ri = 0; ri < m_Cells.size(); ri++) {
			if (ri > 0) {
				printHorizontalBorder(ps, 2);
			}
			List<Cell> row = m_Cells.get(ri);
			int height = m_RowHeights.get(ri);
			for (int hi = 0; hi < height; hi++) {
				if (hi > 0) {
					ps.println();
				}
				for (int ci = 0; ci < row.size(); ci++) {
					printVerticalBorder(ps);
					printLeftPadding(ps);

					Cell cell = row.get(ci);
					String value = stretch(cell.getLine(hi), cell.getLineWidth(hi), m_ColWidths.get(ci));
					ps.print(value);

					printRightPadding(ps);
				}
				printVerticalBorder(ps);
			}
			ps.println();
		}
		printHorizontalBorder(ps, 3);
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

	private void printHorizontalBorder(PrintStream ps, int borderStyle) {
		if (!m_Border) {
			return;
		}

		for (int i = 0; i < m_ColWidths.size(); i++) {
			if (0 == i) {
				if (1 == borderStyle) {
					ps.print('┌');
				} else if (2 == borderStyle) {
					ps.print('├');
				} else {
					ps.print('└');
				}
			} else {
				if (1 == borderStyle) {
					ps.print('┬');
				} else if (2 == borderStyle) {
					ps.print('┼');
				} else {
					ps.print('┴');
				}
			}

			int width = m_ColWidths.get(i) + m_LeftPadding + m_RightPadding;
			for (int j = 0; j < width; j++) {
				ps.print('─');
			}
		}
		if (1 == borderStyle) {
			ps.print('┐');
		} else if (2 == borderStyle) {
			ps.print('┤');
		} else {
			ps.print('┘');
		}
		ps.println();
	}

	private void printVerticalBorder(PrintStream ps) {
		if (!m_Border) {
			return;
		}
		ps.print('│');
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

		List<String> m_Lines;
		List<Integer> m_LineWigths;
		int m_MaxWidth;

		Cell(String value) {
			this(value, 0);
		}

		Cell(String value, int maxStretchWidth) {
			List<String> lines = new ArrayList<>();
			List<Integer> lineWidths = new ArrayList<Integer>();
			int maxWidth = 0;
			int lineWigth = 0;
			boolean hasTab = false;
			int lineBegin = 0;
			for (int i = 0; i <= value.length(); i++) {
				char ch;
				boolean forceLine = false;
				if (i == value.length()) {
					// 末尾以'\n'表示
					ch = '\n';
				} else if (maxStretchWidth > 0 && lineWigth >= maxStretchWidth) {
					// 超过最大拉伸宽度，强行换行
					ch = '\n';
					forceLine = true;
				} else {
					ch = value.charAt(i);
				}
				if ((ch >= 0 && ch <= 31) || 127 == ch) {
					// 控制字符不占宽度
					if ('\t' == ch) {
						// 水平制表符替换为空格，占1个宽度
						lineWigth += 1;
						hasTab = true;
					} else if ('\n' == ch) {
						// 换行
						String line;
						if (0 == lineBegin && value.length() == i) {
							line = value;
						} else {
							line = value.substring(lineBegin, i);
						}
						if (hasTab) {
							line = line.replace('\t', ' ');
						}
						lines.add(line);
						lineWidths.add(lineWigth);
						if (lineWigth > maxWidth) {
							maxWidth = lineWigth;
						}
						lineWigth = 0;
						hasTab = false;
						if (forceLine) {
							lineBegin = i;
							i--;
						} else {
							lineBegin = i + 1;
						}
						forceLine = false;
					}
				} else if ((ch > 0x0100 && ch <= 0xD7AF) || (ch > 0xF900 && ch <= 0xFFFF)) {
					// 中日韩等字符占两个宽度
					lineWigth += 2;
				} else {
					lineWigth += 1;
				}
			}

			if (1 == lines.size()) {
				m_Lines = Collections.singletonList(lines.get(0));
				m_LineWigths = Collections.singletonList(lineWidths.get(0));
			} else {
				m_Lines = lines;
				m_LineWigths = lineWidths;
			}
			m_MaxWidth = maxWidth;
		}

		static Cell valueOf(String value, int maxStretchWidth) {
			if (StringUtil.isEmpty(value)) {
				return EMPTY_CELL;
			}
			return new Cell(value, maxStretchWidth);
		}

		int getWidth() {
			return m_MaxWidth;
		}

		int getHeight() {
			return m_Lines.size();
		}

		String getLine(int height) {
			if (height >= m_Lines.size()) {
				return "";
			}
			return m_Lines.get(height);
		}

		int getLineWidth(int height) {
			if (height >= m_LineWigths.size()) {
				return 0;
			}
			return m_LineWigths.get(height);
		}
	}

	static final Cell EMPTY_CELL = new Cell("");
}
