package com.solution.processor;

import org.springframework.batch.item.ItemProcessor;
import com.solution.model.Report;

public class ReportItemProcessor implements ItemProcessor <Report, Report> {
	
	@Override
	public Report process (Report item) throws Exception{
		
		return item;
	
}

}
