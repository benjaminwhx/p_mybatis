package com.github.base.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: 吴海旭
 * Date: 2017-06-17
 * Time: 上午11:09
 */
public class MyPage<T> {

	private int pageNo = 1;	// 页码，默认第一页
	private int pageSize = 15;	// 每页显示的记录数，默认15
	private int totalRecord;	// 总记录数
	private int totalPage;	// 总页数
	private List<T> results;	// 对应的当前记录页数
	private Map<String, Object> params = new HashMap<>();	// 其他的参数我们把它封装成一个map对象

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
		//在设置总页数的时候计算出对应的总页数，在下面的三目运算中加法拥有更高的优先级，所以最后可以不加括号。
		int totalPage = totalRecord%pageSize==0 ? totalRecord/pageSize : totalRecord/pageSize + 1;
		this.setTotalPage(totalPage);
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MyPage [pageNo=").append(pageNo).append(", pageSize=")
				.append(pageSize).append(", results=").append(results).append(
				", totalPage=").append(totalPage).append(
				", totalRecord=").append(totalRecord).append("]");
		return builder.toString();
	}
}
