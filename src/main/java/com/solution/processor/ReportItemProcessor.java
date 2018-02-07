package com.solution.processor;

import org.springframework.batch.item.ItemProcessor;
import com.solution.model.Report;

public class ReportItemProcessor implements ItemProcessor <Report, Report> {
	
	@Override
	public Report process (Report item) throws Exception{
			
		 String name = item.getName().toUpperCase();
		
		 Report itemSalida = new Report(item.getRefId(), name, item.getAge());
		
		return itemSalida;
	
	
	}

}
