package com.solution.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;

import com.solution.model.Report;
import com.solution.processor.*;


@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	//argumentos definidos en application.yml
	@Value("${step.orig}")
	private String orig;
	
	@Value("${step.dest}")
	private String dest;
	
	//argumentos pasados por Eclipse en pestaña Arguments
	@Value("${argumentName}")
	private String ruta;
	
	@Value("${argumentNameDos}")
	private String rutaSalida;
	
	
    @Bean(name="itemReaderStep1")
    public ItemReader<Report> reader() {
        FlatFileItemReader<Report> reader = new FlatFileItemReader<Report>();
    //   reader.setResource(new ClassPathResource("files/input.csv"));
     	reader.setResource(new FileSystemResource(new File(orig)));
        
        reader.setLineMapper(new DefaultLineMapper<Report>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] {"refId", "name", "age"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Report>() {{
                setTargetType(Report.class);
            }});
        }});
        return reader;
    }
    @Bean(name="itemWriterStep1")
    public ItemWriter<Report> writer() {
    	FlatFileItemWriter<Report> writer = new FlatFileItemWriter<Report>();
    	//writer.setResource(new FileSystemResource(new File(dest)));
    	writer.setResource(new FileSystemResource(new File(ruta)));
    	DelimitedLineAggregator<Report> delLineAgg = new DelimitedLineAggregator<Report>();
    	delLineAgg.setDelimiter(",");
    	BeanWrapperFieldExtractor<Report> fieldExtractor = new BeanWrapperFieldExtractor<Report>();
    	fieldExtractor.setNames(new String[] {"refId","name","age"});
    	delLineAgg.setFieldExtractor(fieldExtractor);
    	writer.setLineAggregator(delLineAgg);
        return writer;
    }
    
    //Un procesador despues del otro. CompositeItemProcessor
    @Bean(name="itemProcessorStep1")
    public ItemProcessor<Report, Report> processor() {
        CompositeItemProcessor<Report, Report> processor = new CompositeItemProcessor<>();
        
        //Opcion 1
        //processor.setDelegates(Arrays.asList(new ReportItemProcessor(), new ReportItemProcessorDos()));
        //return processor;
        
        //Opcion 2
        List<ItemProcessor<Report, Report>> itemProcessors=new ArrayList<>();
        itemProcessors.add(new ReportItemProcessor());
        itemProcessors.add(new ReportItemProcessorDos());
        processor.setDelegates(itemProcessors);
        return processor;
        
    }
    
    
    @Bean(name="itemReaderStep2")
    public ItemReader<Report> readerStep2() {
        FlatFileItemReader<Report> reader = new FlatFileItemReader<Report>();
        reader.setResource(new FileSystemResource(new File(ruta)));
        
        reader.setLineMapper(new DefaultLineMapper<Report>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] {"refId", "name", "age"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Report>() {{
                setTargetType(Report.class);
            }});
        }});
        return reader;
    }
    @Bean(name="itemWriterStep2")
    public ItemWriter<Report> writerStep2() {
    	FlatFileItemWriter<Report> writer = new FlatFileItemWriter<Report>();
    	writer.setResource(new FileSystemResource(new File(rutaSalida)));
    	DelimitedLineAggregator<Report> delLineAgg = new DelimitedLineAggregator<Report>();
    	delLineAgg.setDelimiter(",");
    	BeanWrapperFieldExtractor<Report> fieldExtractor = new BeanWrapperFieldExtractor<Report>();
    	fieldExtractor.setNames(new String[] {"refId","name","age"});
    	delLineAgg.setFieldExtractor(fieldExtractor);
    	writer.setLineAggregator(delLineAgg);
        return writer;
    }
    
    //Un solo procesador
    @Bean("itemProcessorStep2")
    public ItemProcessor<Report, Report> processorStep2() {
        return new ReportItemProcessorTres();
    }
    
    //CreateReport en Un solo paso 
    /*
    @Bean
    public Job createReport(JobBuilderFactory jobs, @Qualifier("Step2")Step step2) {
        return jobs.get("createReport")
                .flow(step2)
                .end()
                .build();
        
    }
    */
    
    //CreateReport con dos steps.
    @Bean
    public Job createReport(JobBuilderFactory jobs
    		, @Qualifier("Step1")Step step1
    		, @Qualifier("Step2")Step step2) {
        return jobs.get("createReport")
        		.start(step1)
        		.next(step2)
                .build();
        
    }
    @Bean(name="Step1")
    public Step step1(StepBuilderFactory stepBuilderFactory, @Qualifier("itemReaderStep1")ItemReader<Report> reader,
            @Qualifier("itemWriterStep1")ItemWriter<Report> writer, @Qualifier("itemProcessorStep1")ItemProcessor<Report, Report> processor) {
        return stepBuilderFactory.get("step1")
                .<Report, Report> chunk(1)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    
    @Bean(name="Step2")
    public Step step2(StepBuilderFactory stepBuilderFactory, @Qualifier("itemReaderStep2")ItemReader<Report> reader,
    		@Qualifier("itemWriterStep2")ItemWriter<Report> writer, @Qualifier("itemProcessorStep2")ItemProcessor<Report, Report> processor) {
        return stepBuilderFactory.get("step2")
                .<Report, Report> chunk(1)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
} 