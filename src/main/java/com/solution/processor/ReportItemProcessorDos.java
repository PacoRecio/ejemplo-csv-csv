package com.solution.processor;

import org.springframework.batch.item.ItemProcessor;

import com.solution.model.Report;

public class ReportItemProcessorDos implements ItemProcessor <Report, Report> {
	
	@Override
	public Report process (Report item) throws Exception{
		
		//return item;
		 
		 int edad = item.getAge();
	     int sumaEdad = edad + 10;
			
		 Report itemSalida = new Report(item.getRefId(), item.getName(), sumaEdad);
		
		return itemSalida;
	
	}

}