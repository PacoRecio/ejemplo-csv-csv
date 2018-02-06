package com.solution.config;

import java.io.File;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;

import com.solution.model.Report;
import com.solution.processor.ReportItemProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
	@Value("${step.dest}")
	private String dest;
	
    @Bean
    public ItemReader<Report> reader() {
        FlatFileItemReader<Report> reader = new FlatFileItemReader<Report>();
        reader.setResource(new ClassPathResource("files/input.csv"));
        reader.setLineMapper(new DefaultLineMapper<Report>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] {"refId", "name", "age" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Report>() {{
                setTargetType(Report.class);
            }});
        }});
        return reader;
    }
    @Bean
    public ItemWriter<Report> writer() {
    	FlatFileItemWriter<Report> writer = new FlatFileItemWriter<Report>();
    	writer.setResource(new FileSystemResource(new File(dest)));
    	DelimitedLineAggregator<Report> delLineAgg = new DelimitedLineAggregator<Report>();
    	delLineAgg.setDelimiter(",");
    	BeanWrapperFieldExtractor<Report> fieldExtractor = new BeanWrapperFieldExtractor<Report>();
    	fieldExtractor.setNames(new String[] {"refId", "name", "age"});
    	delLineAgg.setFieldExtractor(fieldExtractor);
    	writer.setLineAggregator(delLineAgg);
        return writer;
    }
    @Bean
    public ItemProcessor<Report, Report> processor() {
        return new ReportItemProcessor();
    }
    @Bean
    public Job createReport(JobBuilderFactory jobs, Step step) {
        return jobs.get("createReport")
                .flow(step)
                .end()
                .build();
    }
    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory, ItemReader<Report> reader,
            ItemWriter<Report> writer, ItemProcessor<Report, Report> processor) {
        return stepBuilderFactory.get("step")
                .<Report, Report> chunk(1)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
} 